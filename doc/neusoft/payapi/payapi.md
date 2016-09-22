####支付宝与微信支付开发
##### pom文件加入支付组件包

>        <dependency>
			<groupId>com.vonchange</groupId>
			<artifactId>pay_util</artifactId>
			<version>0.0.2-SNAPSHOT</version>
		</dependency>

##### 支付宝开发
1. 编写获取订单详情的url地址，需组装为AlipayOrder对象，请查看[getOrderInfo方法](java/MyAlipayController.java)
2. [AlipayOrder 实体相关信息](java/AlipayOrder.java)
>    //我方唯一订单号<br/>
    private String outTradeNo;<br/>
    //商品名称<br/>
    private String orderName;<br/>
    // 商品描述<br/>
    private String body;<br/>v
    //商品金额(元)<br/>
    private String totalFee;<br/>
    // 用户手机号:如果是微信端，会自动填入<br/>
    private String callPhone;<br/>
    //商品描述url(退出的url)<br/>
    //*要求必填<br/>
    private String showUrl;<br/>
    //支付宝相关配置实体<br/>
    private AlipayConfig alipayConfig;<br/>

     [AlipayConfig 实体相关信息](java/AlipayConfig.java)
 >    // 签约的支付宝账号对应的支付宝唯一用户号，以2088开头的16位纯数字组成。<br/>
    String merchantId;<br/>
     //商户密钥<br/>
    String secret;<br/>
    //支付宝异步通知url<br/>
    private String notifyUrl;<br/>
    //支付宝前端跳转url :手机网页端<br/>
    private String wapReturnUrl;<br/>
    // 支付宝前端跳转url :pc网页端<br/>
    private String webReturnUrl;<br/>
    //支付宝在微信内的手机号 的返回回调 优化使用<br/>
    private String phoneCallbackUrl;<br/>

3. 调用支付宝支付api
 http://pay.geexek.com/alipay/web?orderUrl=你的获取订单详情的url地址

4. 同步回调,请查看[returnUrl方法](java/MyAlipayController.java)
5. 异步回调，请查看[updateEnrollOrderByNotify方法](java/MyAlipayController.java)
6. 在微信内进行支付宝支付，回调用户填写的手机号（phone）和订单号（out_trade_no）给你,相关方法
[phone](java/MyAlipayController.java)

##### 微信支付
1. 编写获取订单详情的url地址，需组装为WxpayOrder对象，请查看[getOrderInfo方法](java/MyWxpayController.java)
2. [WxpayOrder 实体相关信息](java/WxpayOrder.java)
>   // 我方唯一订单号
    private String outTradeNo;
    //商品名称
    private String orderName;
   // 商品描述
    private String body;
    //商品金额(元) ***不是分
    private String totalFee;
    //商品描述url(退出的url)
    //* 必填
    private String showUrl;
    //微信支付相关配置实体
    private WxpayConfig wxpayConfig;
    
    [WxpayConfig 实体相关信息](java/WxpayConfig.java)
>   //微信APP ID
    private  String appId;
    // 微信APP Key
    private  String appKey;
    // 商户ID
    String merchantId;
    //商户密钥
    String secret;
    // 微信异步通知url
    private String notifyUrl;
    //微信前端跳转url :手机网页端
    private String wapReturnUrl;
    //微信前端跳转url :pc网页端
    private String webReturnUrl;
    // 微信支付授权目录 如果空 则是pay.geexek.com/wxpay/  否则需要自定义开发(代理请求)
    //一般为空 **
    private String myWxPayUrl;

3. 调用微信支付api
http://pay.geexek.com/wxpay/web?orderUrl=你的获取订单详情的url地址

4. 同步回调,请查看[returnUrl方法](java/MyWxpayController.java)
5. 异步回调，请查看[updateEnrollOrderByNotify方法](java/MyWxpayController.java)

##### 微信和支付宝支付
如果不是单一支付，请使用该方式
1.  组装为List<ComPay>对象，请查看[getPayTypeList方法](java/MyWxpayController.java)
2.  [comPay对象相关信息](java/ComPay.java)

 >  //1为支付宝 2为微信
 	private Integer payType;
    //支付宝或微信支付url 
    //比如 http://pay.geexek.com/wxpay/web?orderUrl=你的获取订单详情的url地址
	private String orderUrl;

3.  调用支付api
http://pay.geexek.com/com/choose?payTypeListUrl=你的获取订单url列表地址（getPayTypeList方法地址）

4.该方式需要先生成订单，生成订单可参照[generateOrder方法](java/MyWxpayController.java)