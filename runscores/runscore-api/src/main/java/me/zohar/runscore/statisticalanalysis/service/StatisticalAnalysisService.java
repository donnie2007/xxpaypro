package me.zohar.runscore.statisticalanalysis.service;

import java.util.List;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.alicp.jetcache.anno.Cached;

import me.zohar.runscore.statisticalanalysis.domain.CashDepositBounty;
import me.zohar.runscore.statisticalanalysis.domain.TodayAccountReceiveOrderSituation;
import me.zohar.runscore.statisticalanalysis.domain.TotalAccountReceiveOrderSituation;
import me.zohar.runscore.statisticalanalysis.domain.TradeSituation;
import me.zohar.runscore.statisticalanalysis.repo.CashDepositBountyRepo;
import me.zohar.runscore.statisticalanalysis.repo.TodayAccountReceiveOrderSituationRepo;
import me.zohar.runscore.statisticalanalysis.repo.TotalAccountReceiveOrderSituationRepo;
import me.zohar.runscore.statisticalanalysis.repo.TradeSituationRepo;
import me.zohar.runscore.statisticalanalysis.vo.AccountReceiveOrderSituationVO;
import me.zohar.runscore.statisticalanalysis.vo.BountyRankVO;
import me.zohar.runscore.statisticalanalysis.vo.CashDepositBountyVO;
import me.zohar.runscore.statisticalanalysis.vo.TradeSituationVO;

@Validated
@Service
public class StatisticalAnalysisService {

	@Autowired
	private TotalAccountReceiveOrderSituationRepo totalAccountReceiveOrderSituationRepo;

	@Autowired
	private TodayAccountReceiveOrderSituationRepo todayAccountReceiveOrderSituationRepo;

	@Autowired
	private CashDepositBountyRepo cashDepositBountyRepo;

	@Autowired
	private TradeSituationRepo tradeSituationRepo;

	@Transactional(readOnly = true)
	public AccountReceiveOrderSituationVO findMyTodayReceiveOrderSituation(@NotBlank String userAccountId) {
		return AccountReceiveOrderSituationVO
				.convertForToday(todayAccountReceiveOrderSituationRepo.findByReceivedAccountId(userAccountId));
	}

	@Transactional(readOnly = true)
	public AccountReceiveOrderSituationVO findMyTotalReceiveOrderSituation(@NotBlank String userAccountId) {
		return AccountReceiveOrderSituationVO
				.convertForTotal(totalAccountReceiveOrderSituationRepo.findByReceivedAccountId(userAccountId));
	}

	@Cached(name = "totalTop10BountyRank", expire = 300)
	@Transactional(readOnly = true)
	public List<BountyRankVO> findTotalTop10BountyRank() {
		List<TotalAccountReceiveOrderSituation> receiveOrderSituations = totalAccountReceiveOrderSituationRepo
				.findTop10ByOrderByBountyDesc();
		return BountyRankVO.convertFor(receiveOrderSituations);
	}

	@Cached(name = "todayTop10BountyRank", expire = 300)
	@Transactional(readOnly = true)
	public List<BountyRankVO> findTodayTop10BountyRank() {
		List<TodayAccountReceiveOrderSituation> todayReceiveOrderSituations = todayAccountReceiveOrderSituationRepo
				.findTop10ByOrderByBountyDesc();
		return BountyRankVO.convertForToday(todayReceiveOrderSituations);
	}

	@Transactional(readOnly = true)
	public CashDepositBountyVO findCashDepositBounty() {
		CashDepositBounty cashDepositBounty = cashDepositBountyRepo.findTopBy();
		return CashDepositBountyVO.convertFor(cashDepositBounty);
	}

	@Transactional(readOnly = true)
	public TradeSituationVO findTradeSituation() {
		TradeSituation tradeSituation = tradeSituationRepo.findTopBy();
		return TradeSituationVO.convertFor(tradeSituation);
	}

}
