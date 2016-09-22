package com.neusoft.race.wallet.action;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.neusoft.enroll.pay.order.bean.RegisterIncomeRecordx;
import com.neusoft.enroll.pay.order.service.IRegisterIncomeRecordxService;
import com.neusoft.enroll.pay.partner.bean.Partnerx;
import com.neusoft.enroll.pay.partner.service.IPartnerxService;
import com.neusoft.pay.wxpay.core.Wepay;
import com.neusoft.pay.wxpay.core.WepayBuilder;
import com.neusoft.pay.wxpay.model.WxpayConfig;
import com.neusoft.pay.wxpay.model.WxpayOrder;
import com.neusoft.pay.wxpay.util.Maps;
import com.neusoft.pay.wxpay.util.WxpayUtil;
import com.neusoft.race.util.Constants;
import com.neusoft.race.wallet.bean.Partner;
import com.neusoft.race.wallet.service.IEnrollPayService;
import com.neusoft.utils.config.SystemConfig;
import com.neusoft.utils.convert.ConvertUtils;
import com.neusoft.utils.http.HttpUtil;
import com.neusoft.utils.map.HashMap;
import com.neusoft.utils.string.StringUtil;
import com.neusoft.utils.web.RequestUtils;

@Controller
@RequestMapping("/pay/mywxpay")
public class MyWxpayController {
	private static final Logger logger = LoggerFactory.getLogger(MyWxpayController.class);
	@Resource
	private IRegisterIncomeRecordxService registerIncomeRecordxService;
	@Resource
	private IPartnerxService partnerxService;
	@Autowired
	private IEnrollPayService enrollPayService;

	@RequestMapping("/web")
	public ModelAndView web(ModelAndView mav, @RequestParam("orderNo") String orderNo, HttpServletRequest request, HttpServletResponse resp) {
		String payUrl = SystemConfig.getProperty("pay.url");// 可能要改 放到common里
		String enrollUrl = SystemConfig.getProperty("enroll.url");
		mav.setViewName("redirect:" + HttpUtil.concatUrl(payUrl + "wxpay/web.do", new HashMap<String, String>().set("orderUrl", HttpUtil.concatUrl(enrollUrl + "pay/myalipay/getOrderInfo.do", new HashMap<String, String>().set("orderNo", orderNo)))));
		return mav;
	}

	@RequestMapping("/getOrderInfo")
	@ResponseBody
	public WxpayOrder getOrderInfo(ModelAndView mav, @RequestParam("orderNo") String orderNo, HttpServletRequest request, HttpServletResponse resp) {
		RegisterIncomeRecordx registerIncomeRecordx = registerIncomeRecordxService.queryOrderByOrderNo(orderNo);
		if (null == registerIncomeRecordx) {
			return null;
		}
		WxpayOrder wxpayOrder = new WxpayOrder();
		wxpayOrder.setOutTradeNo(registerIncomeRecordx.getApplyId());
		wxpayOrder.setOrderName(registerIncomeRecordx.getSubject());
		wxpayOrder.setTotalFee(ConvertUtils.toString(registerIncomeRecordx.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
		String enrollUrl = SystemConfig.getProperty("enroll.url");
		wxpayOrder.setShowUrl(HttpUtil.concatBase64Url(enrollUrl + "web/cmpt/info.html", new HashMap<String, String>().set("cId", ConvertUtils.toString(registerIncomeRecordx.getCmptId()))));
		Partnerx partnerx = partnerxService.queryByCode(registerIncomeRecordx.getPartner(), 2);
		if (null == partnerx) {
			return null;
		}
		WxpayConfig wxpayConfig = new WxpayConfig();
		wxpayConfig.setAppId(partnerx.getAccount());
		wxpayConfig.setAppKey(partnerx.getPrivateKey());
		wxpayConfig.setMerchantId(partnerx.getPartner());
		wxpayConfig.setSecret(partnerx.getSecret());
		if(null!=partnerx.getIsOld()&&partnerx.getIsOld().intValue()==1){
			wxpayConfig.setMyWxPayUrl(enrollUrl + "web/enroll/wxpay");
		}
		wxpayConfig.setNotifyUrl(enrollUrl + "pay/mywxpay/updateEnrollOrderByNotify");
		wxpayConfig.setWapReturnUrl(enrollUrl + "pay/mywxpay/returnUrl");
		wxpayConfig.setWebReturnUrl(enrollUrl + "pay/mywxpay/returnUrl");
		wxpayOrder.setWxpayConfig(wxpayConfig);
		return wxpayOrder;
	}

	@RequestMapping("/returnUrl")
	public String returnUrl(ModelAndView mav, HttpServletRequest request, HttpServletResponse response) {
		String outTradeNo = request.getParameter("out_trade_no"); // 商户订单号
		RegisterIncomeRecordx registerIncomeRecordx = new RegisterIncomeRecordx();
		registerIncomeRecordx.setApplyId(outTradeNo);
		// 记录同步状态
		registerIncomeRecordx.setReturnStatus("TRADE_SUCCESS");
		registerIncomeRecordxService.updateByIdOrApplyId(registerIncomeRecordx);
		String redirectUrl = HttpUtil.concatBase64Url("/web/enroll/ins_pay.html", new HashMap<String, String>().set("applyId", outTradeNo));
		return "redirect:" + redirectUrl;
	}

	@RequestMapping("/updateEnrollOrderByNotify")
	@ResponseBody
	public String updateEnrollOrderByNotify(ModelAndView mav, HttpServletRequest request, HttpServletResponse response) {
		String notifyXml = RequestUtils.getPostRequestBody(request);
		if (StringUtil.isBlank(notifyXml)) {
			return WxpayUtil.notifyNotOk("body为空");
		}
		Map<String, Object> notifyParams = Maps.toMap(notifyXml);
		Map<String, String> params=new java.util.HashMap<String, String>();
		for (Entry<String, Object> entry : notifyParams.entrySet()) {
			params.put(entry.getKey(), ConvertUtils.toString(entry.getValue()));
		}
		if(!"SUCCESS".equalsIgnoreCase(params.get("result_code"))){
			logger.error(params.get("result_code")+"----"+params.get("return_msg"));
			return WxpayUtil.notifyOk();
		}
		String outTradeNo=params.get("out_trade_no");
		Partner partner = enrollPayService.getParterByOrderNo(outTradeNo,Constants.Pay.PayType.WXPAY);
		Wepay  wepay=WepayBuilder.newBuilder(partner.getAccount(), partner.getPrivateKey(), partner.getPartner()).build();
		boolean verify=wepay.notifies().verifySign(notifyParams);
		RegisterIncomeRecordx registerIncomeRecordx = new RegisterIncomeRecordx();
		registerIncomeRecordx.setApplyId(outTradeNo);
		if(verify){
			RegisterIncomeRecordx record = registerIncomeRecordxService.queryOrderByOrderNo(outTradeNo);
			String status = record.getTradeStatus();
			if (StringUtil.isNotBlank(status) && ("TRADE_SUCCESS".equals(status) || "TRADE_RETURE".equals(status) || "TRADE_RETURNING".equals(status))) {// 如果查出这个订单已经成功，后续操作不进行
				logger.info("多次回调记录->订单编号：" + record.getApplyId());
				return WxpayUtil.notifyOk();
			}
			record.setApplyId(outTradeNo);
			record.setBuyerId(params.get("openid"));
			record.setTradeNo(params.get("transaction_id"));
			record.setNotifyAmount(ConvertUtils.toBigDecimal(ConvertUtils.toFloat(params.get("total_fee"))/100.0));
			record.setTradeStatus("TRADE_SUCCESS");
			record.setPayType(2);
			String result = enrollPayService.updateEnrollPayOrder(record);
			if(null!=result&&result.equalsIgnoreCase("success")){
				return WxpayUtil.notifyOk();
			}
		}
		logger.error("回调失败");
		return WxpayUtil.notifyNotOk("回调失败");
	}
}
