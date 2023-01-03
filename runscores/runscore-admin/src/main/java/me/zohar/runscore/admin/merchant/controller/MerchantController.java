package me.zohar.runscore.admin.merchant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.SecureUtil;
import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.merchant.param.AddMerchantParam;
import me.zohar.runscore.merchant.param.MerchantEditParam;
import me.zohar.runscore.merchant.param.MerchantQueryCondParam;
import me.zohar.runscore.merchant.param.MerchantSettlementRecordQueryCondParam;
import me.zohar.runscore.merchant.service.MerchantService;

@Controller
@RequestMapping("/merchant")
public class MerchantController {

	@Autowired
	private MerchantService merchantService;

	@GetMapping("/settlementApproved")
	@ResponseBody
	public Result settlementApproved(String id, String note) {
		merchantService.settlementApproved(id, note);
		return Result.success();
	}

	@GetMapping("/settlementNotApproved")
	@ResponseBody
	public Result settlementNotApproved(String id, String note) {
		merchantService.settlementNotApproved(id, note);
		return Result.success();
	}

	@GetMapping("/settlementConfirmCredited")
	@ResponseBody
	public Result settlementConfirmCredited(String id) {
		merchantService.settlementConfirmCredited(id);
		return Result.success();
	}

	@GetMapping("/findByMerchantSettlementRecordId")
	@ResponseBody
	public Result findByMerchantSettlementRecordId(String id) {
		return Result.success().setData(merchantService.findByMerchantSettlementRecordId(id));
	}

	@GetMapping("/findMerchantSettlementRecordByPage")
	@ResponseBody
	public Result findMerchantSettlementRecordByPage(MerchantSettlementRecordQueryCondParam param) {
		return Result.success().setData(merchantService.findMerchantSettlementRecordByPage(param));
	}

	@GetMapping("/findAllMerchant")
	@ResponseBody
	public Result findAllMerchant() {
		return Result.success().setData(merchantService.findAllMerchant());
	}

	@PostMapping("/modifyLoginPwd")
	@ResponseBody
	public Result modifyLoginPwd(String id, String newLoginPwd) {
		merchantService.modifyLoginPwd(id, newLoginPwd);
		return Result.success();
	}

	@GetMapping("/generateSecretKey")
	@ResponseBody
	public Result generateSecretKey() {
		return Result.success().setData(SecureUtil.md5(UUID.fastUUID().toString()));
	}

	@PostMapping("/addMerchant")
	@ResponseBody
	public Result addMerchant(AddMerchantParam param) {
		merchantService.addMerchant(param);
		return Result.success();
	}

	@PostMapping("/updateMerchant")
	@ResponseBody
	public Result updateMerchant(MerchantEditParam param) {
		merchantService.updateMerchant(param);
		return Result.success();
	}

	@GetMapping("/findMerchantById")
	@ResponseBody
	public Result findMerchantById(String id) {
		return Result.success().setData(merchantService.findMerchantById(id));
	}

	@GetMapping("/delMerchantById")
	@ResponseBody
	public Result delMerchantById(String id) {
		merchantService.delMerchantById(id);
		return Result.success();
	}

	@GetMapping("/findMerchantByPage")
	@ResponseBody
	public Result findPlatformOrderByPage(MerchantQueryCondParam param) {
		return Result.success().setData(merchantService.findMerchantByPage(param));
	}

}
