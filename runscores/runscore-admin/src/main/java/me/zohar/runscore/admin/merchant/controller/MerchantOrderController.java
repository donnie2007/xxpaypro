package me.zohar.runscore.admin.merchant.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.merchant.param.ManualStartOrderParam;
import me.zohar.runscore.merchant.param.MerchantOrderQueryCondParam;
import me.zohar.runscore.merchant.service.MerchantOrderService;

@Controller
@RequestMapping("/merchantOrder")
public class MerchantOrderController {

	@Autowired
	private MerchantOrderService merchantOrderService;

	@GetMapping("/findMerchantOrderByPage")
	@ResponseBody
	public Result findMerchantOrderByPage(MerchantOrderQueryCondParam param) {
		return Result.success().setData(merchantOrderService.findMerchantOrderByPage(param));
	}

	@GetMapping("/cancelOrderRefund")
	@ResponseBody
	public Result cancelOrderRefund(String id) {
		merchantOrderService.cancelOrderRefund(id);
		return Result.success();
	}

	@GetMapping("/cancelOrder")
	@ResponseBody
	public Result cancelOrder(String id) {
		merchantOrderService.cancelOrder(id);
		return Result.success();
	}

	@GetMapping("/forceCancelOrder")
	@ResponseBody
	public Result forceCancelOrder(String id) {
		merchantOrderService.forceCancelOrder(id);
		return Result.success();
	}

	@GetMapping("/resendNotice")
	@ResponseBody
	public Result resendNotice(String id) {
		return Result.success().setData(merchantOrderService.paySuccessAsynNotice(id));
	}

	@GetMapping("/confirmToPaid")
	@ResponseBody
	public Result confirmToPaid(String userAccountId, String orderId) {
		merchantOrderService.userConfirmToPaid(userAccountId, orderId);
		return Result.success();
	}

	@GetMapping("/confirmToPaidWithCancelOrderRefund")
	@ResponseBody
	public Result confirmToPaidWithCancelOrderRefund(String orderId) {
		merchantOrderService.confirmToPaidWithCancelOrderRefund(orderId);
		return Result.success();
	}

	@PostMapping("/startOrder")
	@ResponseBody
	public Result startOrder(@RequestBody List<ManualStartOrderParam> params) {
		merchantOrderService.manualStartOrder(params);
		return Result.success();
	}

	@PostMapping("/updateNote")
	@ResponseBody
	public Result updateNote(String id, String note) {
		merchantOrderService.updateNoteInner(id, note);
		return Result.success();
	}
}
