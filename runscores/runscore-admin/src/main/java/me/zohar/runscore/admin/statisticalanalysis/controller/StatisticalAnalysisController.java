package me.zohar.runscore.admin.statisticalanalysis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.statisticalanalysis.service.MerchantStatisticalAnalysisService;
import me.zohar.runscore.statisticalanalysis.service.StatisticalAnalysisService;

@Controller
@RequestMapping("/statisticalAnalysis")
public class StatisticalAnalysisController {

	@Autowired
	private StatisticalAnalysisService statisticalAnalysisService;

	@Autowired
	private MerchantStatisticalAnalysisService merchantStatisticalAnalysisService;

	@GetMapping("/findMerchantTradeSituation")
	@ResponseBody
	public Result findMerchantTradeSituation() {
		return Result.success().setData(merchantStatisticalAnalysisService.findMerchantTradeSituation());
	}

	@GetMapping("/findTodayTop10BountyRank")
	@ResponseBody
	public Result findTodayTop10BountyRank() {
		return Result.success().setData(statisticalAnalysisService.findTodayTop10BountyRank());
	}

	@GetMapping("/findTotalTop10BountyRank")
	@ResponseBody
	public Result findTotalTop10BountyRank() {
		return Result.success().setData(statisticalAnalysisService.findTotalTop10BountyRank());
	}

	@GetMapping("/findCashDepositBounty")
	@ResponseBody
	public Result findCashDepositBounty() {
		return Result.success().setData(statisticalAnalysisService.findCashDepositBounty());
	}

	@GetMapping("/findTradeSituation")
	@ResponseBody
	public Result findTradeSituation() {
		return Result.success().setData(statisticalAnalysisService.findTradeSituation());
	}

}
