package com.neusoft.pay.alipay.model;

import java.io.Serializable;

/**
 * 支付宝PC和WAP公共支付明细
 * Author: haolin
 * Email: haolin.h0@gmail.com
 * Date: 10/11/15
 */
public class AlipayOrder implements Serializable {

    private static final long serialVersionUID = 5892926888312847503L;

    /**
     * 我方唯一订单号
     * {@link me.hao0.alipay.model.enums.AlipayField#OUT_TRADE_NO}
     */
    private String outTradeNo;

    /**
     * 商品名称
     * {@link me.hao0.alipay.model.enums.AlipayField#ORDER_NAME}
     */
    private String orderName;
    /**
     *  商品描述
     */
    private String body;

    /**
     * 商品金额(元)
     * {@link me.hao0.alipay.model.enums.AlipayField#TOTAL_FEE}
     */
    private String totalFee;
    /**
     *  用户手机号:如果是微信端，会自动填入
     */
    private String callPhone;
    /**
     *  商品描述url(退出的url)
     */
    private String showUrl;
    private AlipayConfig alipayConfig;
    
    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(String totalFee) {
        this.totalFee = totalFee;
    }

	public String getCallPhone() {
		return callPhone;
	}

	public void setCallPhone(String callPhone) {
		this.callPhone = callPhone;
	}



	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getShowUrl() {
		return showUrl;
	}

	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}

	public AlipayConfig getAlipayConfig() {
		return alipayConfig;
	}

	public void setAlipayConfig(AlipayConfig alipayConfig) {
		this.alipayConfig = alipayConfig;
	}
	

}
