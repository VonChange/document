package com.neusoft.race.wallet.action;



import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.neusoft.enroll.pay.order.bean.RegisterIncomeRecordx;
import com.neusoft.enroll.pay.order.service.IRegisterIncomeRecordxService;
import com.neusoft.enroll.pay.partner.bean.Partnerx;
import com.neusoft.enroll.pay.partner.service.IPartnerxService;
import com.neusoft.pay.common.bean.ComPay;
import com.neusoft.utils.config.SystemConfig;
import com.neusoft.utils.http.HttpUtil;
import com.neusoft.utils.map.HashMap;
import com.neusoft.utils.web.Result;

@Controller
@RequestMapping("/pay/mypay/")
public class MyPayController {
	@Resource
	private IPartnerxService partnerxService;
	@Resource
	private IRegisterIncomeRecordxService registerIncomeRecordxService;
	@RequestMapping("/{orderNo}")
	public ModelAndView  web(ModelAndView mav, @PathVariable("orderNo") String orderNo, HttpServletRequest request, HttpServletResponse resp) {
		String payUrl=SystemConfig.getProperty("pay.url");//可能要改 放到common里
		String enrollUrl=SystemConfig.getProperty("enroll.url");
		mav.setViewName("redirect:"+HttpUtil.concatUrl(payUrl+"com/choose.do", 
				new HashMap<String, String>().set("payTypeListUrl", HttpUtil.concatUrl(enrollUrl+"pay/mypay/getPayTypeList.do", 
						new HashMap<String, String>().set("orderNo",orderNo)))));
		return mav;
	}
	@RequestMapping("/getPayTypeList")
	@ResponseBody
	public List<ComPay>  getPayTypeList(@RequestParam("orderNo") String orderNo, HttpServletRequest request, HttpServletResponse resp) {
	    List<Partnerx> partnerxs =	partnerxService.queryListByOrderNo(orderNo);
	    if(null==partnerxs||partnerxs.isEmpty()){
	    	return null;
	    }
	    List<ComPay> list=new ArrayList<ComPay>();
	    ComPay comPay=null;
		String enrollUrl=SystemConfig.getProperty("enroll.url");
	    for (Partnerx partnerx : partnerxs) {
	    	if(null==partnerx){
	    		return null;
	    	}
			if(partnerx.getType()==1){//支付宝
				comPay=new ComPay();
				comPay.setPayType(partnerx.getType());
				String orderUrl= HttpUtil.concatUrl(enrollUrl+"pay/myalipay/getOrderInfo.do", 
						new HashMap<String, String>().set("orderNo",orderNo));
				comPay.setOrderUrl(orderUrl);
				list.add(comPay);
			}
            if(partnerx.getType()==2){//微信
            	comPay=new ComPay();
				comPay.setPayType(partnerx.getType());
				String orderUrl=HttpUtil.concatUrl(enrollUrl+"pay/mywxpay/getOrderInfo.do", 
						new HashMap<String, String>().set("orderNo",orderNo));
				comPay.setOrderUrl(orderUrl);
				list.add(comPay);
			}
		}
		return list;
	}
	/**
	 * 个人生成订单
	 * @param userMapId
	 * @return
	 */
	@RequestMapping(value = "/generateOrder")
	@ResponseBody
	public Result generateOrder(@RequestParam("userMapId")Integer userMapId){
	    RegisterIncomeRecordx registerIncomeRecord=	registerIncomeRecordxService.generateOrder(userMapId);
		return new Result().setResult(registerIncomeRecord);
	}
	@RequestMapping(value = "/generateOrderByTeamId")
	@ResponseBody
	public Result generateOrderByTeamId(@RequestParam("teamId")Integer teamId){
	    RegisterIncomeRecordx registerIncomeRecord=	registerIncomeRecordxService.generateOrderByTeamId(teamId);
		return new Result().setResult(registerIncomeRecord);
	}
}
