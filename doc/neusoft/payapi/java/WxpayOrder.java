package com.neusoft.pay.wxpay.model;

public class WxpayOrder {
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
     *  商品描述url(退出的url)
     */
    private String showUrl;
    private WxpayConfig wxpayConfig;

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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}

	public String getShowUrl() {
		return showUrl;
	}

	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}

	public WxpayConfig getWxpayConfig() {
		return wxpayConfig;
	}

	public void setWxpayConfig(WxpayConfig wxpayConfig) {
		this.wxpayConfig = wxpayConfig;
	}
    
}
