package me.zohar.runscore.statisticalanalysis.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cn.hutool.core.date.DateUtil;
import me.zohar.runscore.common.valid.ParamValid;
import me.zohar.runscore.statisticalanalysis.domain.merchant.MerchantChannelTradeSituation;
import me.zohar.runscore.statisticalanalysis.domain.merchant.MerchantEverydayStatistical;
import me.zohar.runscore.statisticalanalysis.domain.merchant.MerchantTradeSituation;
import me.zohar.runscore.statisticalanalysis.param.MerchantIndexQueryParam;
import me.zohar.runscore.statisticalanalysis.repo.merchant.MerchantChannelTradeSituationRepo;
import me.zohar.runscore.statisticalanalysis.repo.merchant.MerchantEverydayStatisticalRepo;
import me.zohar.runscore.statisticalanalysis.repo.merchant.MerchantTradeSituationRepo;
import me.zohar.runscore.statisticalanalysis.vo.IndexStatisticalVO;
import me.zohar.runscore.statisticalanalysis.vo.MerchantChannelTradeSituationVO;
import me.zohar.runscore.statisticalanalysis.vo.MerchantTradeSituationVO;

@Validated
@Service
public class MerchantStatisticalAnalysisService {

	@Autowired
	private MerchantTradeSituationRepo merchantTradeSituationRepo;

	@Autowired
	private MerchantEverydayStatisticalRepo everydayStatisticalRepo;

	@Autowired
	private MerchantChannelTradeSituationRepo merchantChannelTradeSituationRepo;

	@Transactional(readOnly = true)
	public List<MerchantChannelTradeSituationVO> findMerchantChannelTradeSituationByMerchantId(String merchantId) {
		List<MerchantChannelTradeSituation> tradeSituations = merchantChannelTradeSituationRepo
				.findByMerchantId(merchantId);
		return MerchantChannelTradeSituationVO.convertFor(tradeSituations);
	}

	@Transactional(readOnly = true)
	public MerchantTradeSituationVO findMerchantTradeSituationById(String merchantId) {
		return MerchantTradeSituationVO.convertFor(merchantTradeSituationRepo.getOne(merchantId));
	}

	@Transactional(readOnly = true)
	public List<MerchantTradeSituationVO> findMerchantTradeSituation() {
		List<MerchantTradeSituation> merchantTradeSituations = merchantTradeSituationRepo.findAll();
		return MerchantTradeSituationVO.convertFor(merchantTradeSituations);
	}

	@ParamValid
	@Transactional(readOnly = true)
	public List<IndexStatisticalVO> findEverydayStatistical(MerchantIndexQueryParam param) {
		List<MerchantEverydayStatistical> statisticals = everydayStatisticalRepo
				.findByMerchantIdAndEverydayGreaterThanEqualAndEverydayLessThanEqualOrderByEveryday(
						param.getMerchantId(), DateUtil.beginOfDay(param.getStartTime()),
						DateUtil.beginOfDay(param.getEndTime()));
		return IndexStatisticalVO.convertForEvery(statisticals);
	}

}
