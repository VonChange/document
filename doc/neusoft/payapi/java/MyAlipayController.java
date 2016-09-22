package com.neusoft.race.wallet.action;

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.neusoft.enroll.pay.order.bean.RegisterIncomeRecordx;
import com.neusoft.enroll.pay.order.service.IRegisterIncomeRecordxService;
import com.neusoft.enroll.pay.partner.bean.Partnerx;
import com.neusoft.enroll.pay.partner.service.IPartnerxService;
import com.neusoft.pay.alipay.core.AlipayBuilder;
import com.neusoft.pay.alipay.model.AlipayConfig;
import com.neusoft.pay.alipay.model.AlipayOrder;
import com.neusoft.race.user.bean.RegisterInfo;
import com.neusoft.race.user.service.IRegisterInfoService;
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
@RequestMapping("/pay/myalipay")
public class MyAlipayController {
	private static final Logger logger = LoggerFactory.getLogger(MyAlipayController.class);
	@Resource
	private  IRegisterIncomeRecordxService registerIncomeRecordxService;
	@Resource
	private IPartnerxService partnerxService;
	@Resource
	private IEnrollPayService enrollPayService;
	@Resource
	private IRegisterInfoService registerInfoService;
	@RequestMapping("/web")
	public ModelAndView  web(ModelAndView mav, @RequestParam("orderNo") String orderNo, HttpServletRequest request, HttpServletResponse resp) {
		String payUrl=SystemConfig.getProperty("pay.url");//可能要改 放到common里
		String enrollUrl=SystemConfig.getProperty("enroll.url");
		mav.setViewName("redirect:"+HttpUtil.concatUrl(payUrl+"alipay/web.do", 
				new HashMap<String, String>().set("orderUrl", HttpUtil.concatUrl(enrollUrl+"pay/myalipay/getOrderInfo.do", 
						new HashMap<String, String>().set("orderNo",orderNo)))));
		return mav;
	}
	@RequestMapping("/getOrderInfo")
	@ResponseBody
	public AlipayOrder  getOrderInfo(ModelAndView mav, @RequestParam("orderNo") String orderNo, HttpServletRequest request, HttpServletResponse resp) {
		RegisterIncomeRecordx registerIncomeRecordx=	registerIncomeRecordxService.queryOrderByOrderNo(orderNo);
		if(null==registerIncomeRecordx){
			return null;
		}
		AlipayOrder alipayOrder = new AlipayOrder();
		String payPhone=registerIncomeRecordx.getPayPhone();
		if(null!=registerIncomeRecordx.getRegisterId()){
		    String registerPayPhone=	registerInfoService.queryRegisterInfoById(registerIncomeRecordx.getRegisterId()).getPayPhone();
		    if(StringUtil.isNotBlank(registerPayPhone)){
		    	payPhone=registerPayPhone;
		    }
		}
		alipayOrder.setCallPhone(payPhone);
		alipayOrder.setOutTradeNo(registerIncomeRecordx.getApplyId());
		alipayOrder.setOrderName(registerIncomeRecordx.getSubject());
		alipayOrder.setTotalFee(ConvertUtils.toString(registerIncomeRecordx.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
		String enrollUrl=SystemConfig.getProperty("enroll.url");
		alipayOrder.setShowUrl(HttpUtil.concatBase64Url(enrollUrl+"web/cmpt/info.html", new HashMap<String, String>()
				.set("cId", ConvertUtils.toString(registerIncomeRecordx.getCmptId()))));
		Partnerx partnerx= partnerxService.queryByCode(registerIncomeRecordx.getPartner(), 1);
		if(null==partnerx){
			return null;
		}
		AlipayConfig alipayConfig=new AlipayConfig();
		alipayConfig.setMerchantId(partnerx.getPartner());
		alipayConfig.setSecret(partnerx.getKey());
		alipayConfig.setNotifyUrl(enrollUrl+"pay/myalipay/updateEnrollOrderByNotify");
		alipayConfig.setWapReturnUrl(enrollUrl+"pay/myalipay/returnUrl");
		alipayConfig.setWebReturnUrl(enrollUrl+"pay/myalipay/returnUrl");
		alipayConfig.setPhoneCallbackUrl(enrollUrl+"pay/myalipay/phone");
		alipayOrder.setAlipayConfig(alipayConfig);
		return alipayOrder;
	}
	@RequestMapping("/phone")
	@ResponseBody
	public String  phone(@RequestParam("phone")String callPhone,@RequestParam("out_trade_no")String outTradeNo,ModelAndView mav, HttpServletRequest request, HttpServletResponse response) {
		RegisterIncomeRecordx  registerIncomeRecordx= registerIncomeRecordxService.queryOrderByOrderNo(outTradeNo);
		if(null==registerIncomeRecordx||null==registerIncomeRecordx.getRegisterId()){
			return null;
		}
		Integer registerId=registerIncomeRecordx.getRegisterId();
		RegisterInfo registerInfo=new RegisterInfo();
		registerInfo.setUserId(registerId);
		registerInfo.setPayPhone(callPhone);
		registerInfoService.updateById(registerInfo);
		return "success";
	}
	@RequestMapping("/returnUrl")
	public String  returnUrl(ModelAndView mav, HttpServletRequest request, HttpServletResponse response) {
		Map<String,String> params = RequestUtils.getQueryParamStr(request);
		String outTradeNo = request.getParameter("out_trade_no"); // 商户订单号
		String tradeStatus = request.getParameter("trade_status"); // 交易状态
		Partner partner = enrollPayService.getParterByOrderNo(outTradeNo,Constants.Pay.PayType.ZHIFUBAO);
		boolean verify=AlipayBuilder.newBuilder(partner.getPartner(), partner.getKey()).build().verify().md5(params);
		RegisterIncomeRecordx registerIncomeRecordx = new RegisterIncomeRecordx();
		registerIncomeRecordx.setApplyId(outTradeNo);
		if(verify){
			//记录同步状态
			registerIncomeRecordx.setReturnStatus(tradeStatus);
			registerIncomeRecordxService.updateByIdOrApplyId(registerIncomeRecordx);
			if ("TRADE_FINISHED".equalsIgnoreCase(tradeStatus)) {
				//普通即时到账的交易成功状态???
				logger.info(outTradeNo+tradeStatus+"验证成功！");
				String  redirectUrl=HttpUtil.concatBase64Url("/web/enroll/ins_pay.html", 
			    		new HashMap<String, String>().set("applyId", outTradeNo));
				return "redirect:"+redirectUrl;
			}
			if ("TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) {
				    String  redirectUrl=HttpUtil.concatBase64Url("/web/enroll/ins_pay.html", 
				    		new HashMap<String, String>().set("applyId", outTradeNo));
					return "redirect:"+redirectUrl;
			 }
			
		}
		return  "reditect:/web/error/pay_error.html";
	}
	@RequestMapping("/xreturnUrl")
	@ResponseBody
	public String  xreturnUrl(HttpServletRequest request, HttpServletResponse response) {
		String outTradeNo = request.getParameter("out_trade_no"); // 商户订单号
		RegisterIncomeRecordx registerIncomeRecordx = new RegisterIncomeRecordx();
		registerIncomeRecordx.setApplyId(outTradeNo);
		registerIncomeRecordx.setReturnStatus("TRADE_SUCESS");
		registerIncomeRecordxService.updateByIdOrApplyId(registerIncomeRecordx);
		return null;
	}
	@RequestMapping("/updateEnrollOrderByNotify")
	@ResponseBody
	public String  updateEnrollOrderByNotify(ModelAndView mav, HttpServletRequest request, HttpServletResponse response) {
		Map<String,String> params = RequestUtils.getQueryParamStr(request);
		// 商户订单号
		String outTradeNo = request.getParameter("out_trade_no");
		String tradeStatus = request.getParameter("trade_status");
		Partner partner = enrollPayService.getParterByOrderNo(outTradeNo,Constants.Pay.PayType.ZHIFUBAO);
		boolean verify=AlipayBuilder.newBuilder(partner.getPartner(), partner.getKey()).build().verify().md5(params);
		RegisterIncomeRecordx registerIncomeRecordx = new RegisterIncomeRecordx();
		registerIncomeRecordx.setApplyId(outTradeNo);
		if (verify) {// 验证成功
			if ("TRADE_FINISHED".equalsIgnoreCase(tradeStatus)) {
				logger.info(outTradeNo+"验证成功！TRADE_FINISHED-异步");
				registerIncomeRecordx.setTradeStatus(tradeStatus);
				registerIncomeRecordxService.updateByIdOrApplyId(registerIncomeRecordx);
				return "success";
			} 
			if ("TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) {
				logger.info(outTradeNo+"验证成功！TRADE_SUCCESS-异步");
				// 根据订单查询赛事发起者的收入记录
				RegisterIncomeRecordx record=registerIncomeRecordxService.queryOrderByOrderNo(outTradeNo);
				String status=record.getTradeStatus();
				if (StringUtil.isNotBlank(status)&&("TRADE_SUCCESS".equals(status) || "TRADE_RETURE".equals(status) || "TRADE_RETURNING".equals(status))) {// 如果查出这个订单已经成功，后续操作不进行
					logger.info("多次回调记录->订单编号：" + record.getApplyId());
					return "success";
				}
				record.setApplyId(outTradeNo);
				record.setBuyerEmail(params.get("buyer_email"));
				record.setBuyerId(params.get("buyer_id"));
				record.setTradeNo(params.get("trade_no"));
				record.setNotifyAmount(ConvertUtils.toBigDecimal(params.get("total_fee")));
				record.setTradeStatus(tradeStatus);
				record.setPayType(1);
				String result = enrollPayService.updateEnrollPayOrder(record);
				return  result;
			}
			logger.info(outTradeNo+tradeStatus);
			registerIncomeRecordx.setTradeStatus(tradeStatus);
			return null;
		} 
		logger.info(outTradeNo+"验证失败");
		registerIncomeRecordx.setTradeStatus("ERROR_VERIFY");
		return null;
	}
}
