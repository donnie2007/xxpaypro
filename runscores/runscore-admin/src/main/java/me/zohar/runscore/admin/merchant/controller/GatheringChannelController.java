package me.zohar.runscore.admin.merchant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.merchant.param.AddOrUpdateGatheringChannelParam;
import me.zohar.runscore.merchant.param.BatchSettingRateParam;
import me.zohar.runscore.merchant.param.GatheringChannelQueryCondParam;
import me.zohar.runscore.merchant.param.QuickSettingRebateParam;
import me.zohar.runscore.merchant.service.GatheringChannelService;

@Controller
@RequestMapping("/gatheringChannel")
public class GatheringChannelController {

	@Autowired
	private GatheringChannelService gatheringChannelService;

	@PostMapping("/addRebate")
	@ResponseBody
	public Result addRebate(String channelId, Double rebate) {
		gatheringChannelService.addRebate(channelId, rebate);
		return Result.success();
	}

	@GetMapping("/delRebate")
	@ResponseBody
	public Result delRebate(String id) {
		gatheringChannelService.delRebate(id);
		return Result.success();
	}

	@PostMapping("/resetRebate")
	@ResponseBody
	public Result resetRebate(@RequestBody QuickSettingRebateParam param) {
		gatheringChannelService.resetRebate(param);
		return Result.success();
	}

	@GetMapping("/findAllGatheringChannelRebate")
	@ResponseBody
	public Result findAllGatheringChannelRebate() {
		return Result.success().setData(gatheringChannelService.findAllGatheringChannelRebate());
	}

	@GetMapping("/findGatheringChannelRateByMerchantId")
	@ResponseBody
	public Result findGatheringChannelRateByMerchantId(String merchantId) {
		return Result.success().setData(gatheringChannelService.findGatheringChannelRateByMerchantId(merchantId));
	}

	@PostMapping("/saveGatheringChannelRate")
	@ResponseBody
	public Result saveGatheringChannelRate(@RequestBody BatchSettingRateParam param) {
		gatheringChannelService.saveGatheringChannelRate(param.getMerchantId(), param.getChannelRates());
		return Result.success();
	}

	@PostMapping("/addOrUpdateGatheringChannel")
	@ResponseBody
	public Result addOrUpdateGatheringChannel(AddOrUpdateGatheringChannelParam param) {
		gatheringChannelService.addOrUpdateGatheringChannel(param);
		return Result.success();
	}

	@GetMapping("/findGatheringChannelById")
	@ResponseBody
	public Result findGatheringChannelById(String id) {
		return Result.success().setData(gatheringChannelService.findGatheringChannelById(id));
	}

	@GetMapping("/delGatheringChannelById")
	@ResponseBody
	public Result delGatheringChannelById(String id) {
		gatheringChannelService.delGatheringChannelById(id);
		return Result.success();
	}

	@GetMapping("/findGatheringChannelByPage")
	@ResponseBody
	public Result findGatheringChannelByPage(GatheringChannelQueryCondParam param) {
		return Result.success().setData(gatheringChannelService.findGatheringChannelByPage(param));
	}

	@GetMapping("/findAllGatheringChannel")
	@ResponseBody
	public Result findAllGatheringChannel() {
		return Result.success().setData(gatheringChannelService.findAllGatheringChannel());
	}

}
