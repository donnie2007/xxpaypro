package me.zohar.runscore.api.controller;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.merchant.param.StartOrderParam;
import me.zohar.runscore.merchant.service.MerchantOrderService;
import me.zohar.runscore.merchant.vo.OrderGatheringCodeVO;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class ApiController {

	@Autowired
	private MerchantOrderService platformOrderService;

	@PostMapping("/startOrder")
	@ResponseBody
	public Result startOrder(StartOrderParam param) {
		return Result.success().setData(platformOrderService.startOrder(param));
	}

	@GetMapping("/getOrderGatheringCode")
	@ResponseBody
	public Result getOrderGatheringCode(String orderNo) {
		OrderGatheringCodeVO vo = platformOrderService.getOrderGatheringCode(orderNo);
		return Result.success().setData(vo);
	}

	@PostMapping("/startOrderTest")
	@ResponseBody
	public Result startOrderTest(StartOrderParam param) {



		String merchantNum = param.getMerchantNum();// 商户号
		String orderNo = param.getOrderNo();// 商户订单号
		String amount = param.getAmount();// 支付金额
		String notifyUrl = param.getNotifyUrl();// 异步通知地址"localhost:8080/paySuccessNotice"
		String returnUrl = "localhost:8080/paySuccessNotice";// 同步通知地址
/*		String payType = "wechat";// 请求支付类型

		param.setPayType(payType);*/
		param.setReturnUrl(returnUrl);
		String secretKey =param.getAttch();//商户密钥
		String sign = merchantNum + orderNo + amount + notifyUrl + secretKey;
		sign = new Digester(DigestAlgorithm.MD5).digestHex(sign);// md5签名
		param.setSign(sign);
		return Result.success().setData(platformOrderService.startOrder(param));
	}


}
