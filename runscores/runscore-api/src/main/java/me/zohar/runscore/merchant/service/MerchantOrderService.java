package me.zohar.runscore.merchant.service;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.zengtengpeng.annotation.Lock;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import me.zohar.runscore.common.exception.BizError;
import me.zohar.runscore.common.exception.BizException;
import me.zohar.runscore.common.utils.ThreadPoolUtils;
import me.zohar.runscore.common.valid.ParamValid;
import me.zohar.runscore.common.vo.PageResult;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.gatheringcode.domain.GatheringCode;
import me.zohar.runscore.gatheringcode.repo.GatheringCodeRepo;
import me.zohar.runscore.mastercontrol.domain.ReceiveOrderSetting;
import me.zohar.runscore.mastercontrol.repo.ReceiveOrderSettingRepo;
import me.zohar.runscore.merchant.domain.ActualIncomeRecord;
import me.zohar.runscore.merchant.domain.GatheringChannel;
import me.zohar.runscore.merchant.domain.GatheringChannelRate;
import me.zohar.runscore.merchant.domain.Merchant;
import me.zohar.runscore.merchant.domain.MerchantOrder;
import me.zohar.runscore.merchant.domain.MerchantOrderPayInfo;
import me.zohar.runscore.merchant.domain.OrderRebate;
import me.zohar.runscore.merchant.param.LowerLevelAccountReceiveOrderQueryCondParam;
import me.zohar.runscore.merchant.param.ManualStartOrderParam;
import me.zohar.runscore.merchant.param.MerchantOrderQueryCondParam;
import me.zohar.runscore.merchant.param.MyReceiveOrderRecordQueryCondParam;
import me.zohar.runscore.merchant.param.StartOrderParam;
import me.zohar.runscore.merchant.repo.ActualIncomeRecordRepo;
import me.zohar.runscore.merchant.repo.GatheringChannelRateRepo;
import me.zohar.runscore.merchant.repo.GatheringChannelRepo;
import me.zohar.runscore.merchant.repo.MerchantOrderPayInfoRepo;
import me.zohar.runscore.merchant.repo.MerchantOrderRepo;
import me.zohar.runscore.merchant.repo.MerchantRepo;
import me.zohar.runscore.merchant.repo.OrderRebateRepo;
import me.zohar.runscore.merchant.vo.MerchantOrderDetailsVO;
import me.zohar.runscore.merchant.vo.MerchantOrderVO;
import me.zohar.runscore.merchant.vo.MyWaitConfirmOrderVO;
import me.zohar.runscore.merchant.vo.MyWaitReceivingOrderVO;
import me.zohar.runscore.merchant.vo.OrderGatheringCodeVO;
import me.zohar.runscore.merchant.vo.ReceiveOrderRecordVO;
import me.zohar.runscore.merchant.vo.StartOrderSuccessVO;
import me.zohar.runscore.useraccount.domain.AccountChangeLog;
import me.zohar.runscore.useraccount.domain.AccountReceiveOrderChannel;
import me.zohar.runscore.useraccount.domain.UserAccount;
import me.zohar.runscore.useraccount.repo.AccountChangeLogRepo;
import me.zohar.runscore.useraccount.repo.AccountReceiveOrderChannelRepo;
import me.zohar.runscore.useraccount.repo.UserAccountRepo;

@Validated
@Slf4j
@Service
public class MerchantOrderService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private MerchantOrderRepo merchantOrderRepo;

	@Autowired
	private MerchantOrderPayInfoRepo merchantOrderPayInfoRepo;

	@Autowired
	private MerchantRepo merchantRepo;

	@Autowired
	private UserAccountRepo userAccountRepo;

	@Autowired
	private GatheringCodeRepo gatheringCodeRepo;

	@Autowired
	private AccountChangeLogRepo accountChangeLogRepo;

	@Autowired
	private ReceiveOrderSettingRepo receiveOrderSettingRepo;

	@Autowired
	private OrderRebateRepo orderRebateRepo;

	@Autowired
	private AccountReceiveOrderChannelRepo accountReceiveOrderChannelRepo;

	@Autowired
	private GatheringChannelRateRepo gatheringChannelRateRepo;

	@Autowired
	private GatheringChannelRepo gatheringChannelRepo;

	@Autowired
	private ActualIncomeRecordRepo actualIncomeRecordRepo;

	@Transactional
	public void timingPublishOrder() {
		List<MerchantOrder> orders = merchantOrderRepo.findByOrderStateAndSubmitTimeLessThan(Constant.??????????????????_????????????,
				new Date());
		for (MerchantOrder order : orders) {
			order.setOrderState(Constant.??????????????????_????????????);
			merchantOrderRepo.save(order);
		}

	}

	@Transactional(readOnly = true)
	public MerchantOrderDetailsVO findMerchantOrderDetailsById(@NotBlank String orderId) {
		MerchantOrderDetailsVO vo = MerchantOrderDetailsVO.convertFor(merchantOrderRepo.getOne(orderId));
		return vo;
	}

	/**
	 * ??????????????????
	 * 
	 * @param orderId
	 */
	@Transactional
	public void cancelOrderRefund(@NotBlank String orderId) {
		MerchantOrder merchantOrder = merchantOrderRepo.getOne(orderId);
		if (!(Constant.??????????????????_?????????.equals(merchantOrder.getOrderState())
				|| Constant.??????????????????_?????????.equals(merchantOrder.getOrderState()))) {
			throw new BizException(BizError.??????????????????????????????????????????????????????????????????????????????);
		}
		UserAccount userAccount = merchantOrder.getReceivedAccount();
		Double cashDeposit = NumberUtil.round(userAccount.getCashDeposit() + merchantOrder.getGatheringAmount(), 4)
				.doubleValue();
		userAccount.setCashDeposit(cashDeposit);
		userAccountRepo.save(userAccount);
		merchantOrder.customerCancelOrderRefund();
		merchantOrderRepo.save(merchantOrder);
		accountChangeLogRepo.save(AccountChangeLog.buildWithCustomerCancelOrderRefund(userAccount, merchantOrder));
	}

	@Transactional(readOnly = true)
	public OrderGatheringCodeVO getOrderGatheringCode(@NotBlank String orderNo) {
		MerchantOrder order = merchantOrderRepo.findByOrderNo(orderNo);
		if (order == null) {
			log.error("?????????????????????;orderNo:{}", orderNo);
			throw new BizException(BizError.?????????????????????);
		}
		OrderGatheringCodeVO vo = OrderGatheringCodeVO.convertFor(order);
		return vo;
	}

	@Transactional(readOnly = true)
	public String getGatheringCodeStorageId(String receivedAccountId, String gatheringChannelCode,
			Double gatheringAmount) {
		ReceiveOrderSetting merchantOrderSetting = receiveOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (merchantOrderSetting.getUnfixedGatheringCodeReceiveOrder()) {
			GatheringCode gatheringCode = gatheringCodeRepo
					.findTopByUserAccountIdAndGatheringChannelChannelCodeAndStateAndFixedGatheringAmountFalseAndInUseTrue(
							receivedAccountId, gatheringChannelCode, Constant.???????????????_??????);
			if (gatheringCode != null) {
				return gatheringCode.getStorageId();
			}
		} else {
			GatheringCode gatheringCode = gatheringCodeRepo
					.findTopByUserAccountIdAndGatheringChannelChannelCodeAndGatheringAmountAndStateAndInUseTrue(
							receivedAccountId, gatheringChannelCode, gatheringAmount, Constant.???????????????_??????);
			if (gatheringCode != null) {
				return gatheringCode.getStorageId();
			}
		}
		return null;
	}

	@Transactional(readOnly = true)
	public GatheringCode getGatheringCodeStorage(String receivedAccountId, String gatheringChannelCode,
											Double gatheringAmount) {
		ReceiveOrderSetting merchantOrderSetting = receiveOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (merchantOrderSetting.getUnfixedGatheringCodeReceiveOrder()) {
			GatheringCode gatheringCode = gatheringCodeRepo
					.findTopByUserAccountIdAndGatheringChannelChannelCodeAndStateAndFixedGatheringAmountFalseAndInUseTrue(
							receivedAccountId, gatheringChannelCode, Constant.???????????????_??????);
			if (gatheringCode != null) {
				return gatheringCode;
			}
		} else {
			GatheringCode gatheringCode = gatheringCodeRepo
					.findTopByUserAccountIdAndGatheringChannelChannelCodeAndGatheringAmountAndStateAndInUseTrue(
							receivedAccountId, gatheringChannelCode, gatheringAmount, Constant.???????????????_??????);
			if (gatheringCode != null) {
				return gatheringCode;
			}
		}
		return null;
	}

	@Transactional
	public void userConfirmToPaid(@NotBlank String userAccountId, @NotBlank String orderId) {
		MerchantOrder merchantOrder = merchantOrderRepo.findByIdAndReceivedAccountId(orderId, userAccountId);
		if (merchantOrder == null) {
			throw new BizException(BizError.?????????????????????);
		}
		if (!Constant.??????????????????_?????????.equals(merchantOrder.getOrderState())) {
			throw new BizException(BizError.???????????????????????????????????????????????????);
		}
		merchantOrder.confirmToPaid(null);
		merchantOrderRepo.save(merchantOrder);
		receiveOrderSettlement(merchantOrder);
	}

	@Transactional
	public void confirmToPaidWithCancelOrderRefund(@NotBlank String orderId) {
		MerchantOrder merchantOrder = merchantOrderRepo.getOne(orderId);
		if (merchantOrder == null) {
			throw new BizException(BizError.?????????????????????);
		}
		if (!Constant.??????????????????_??????????????????.equals(merchantOrder.getOrderState())) {
			throw new BizException(BizError.????????????????????????????????????????????????????????????????????????);
		}
		UserAccount userAccount = userAccountRepo.getOne(merchantOrder.getReceivedAccountId());
		Double cashDeposit = NumberUtil.round(userAccount.getCashDeposit() - merchantOrder.getGatheringAmount(), 4)
				.doubleValue();
		if (cashDeposit < 0) {
			throw new BizException(BizError.???????????????????????????);
		}

		// ????????????
		userAccount.setCashDeposit(cashDeposit);
		userAccountRepo.save(userAccount);
		merchantOrder.setOrderState(Constant.??????????????????_?????????);
		merchantOrderRepo.save(merchantOrder);
		accountChangeLogRepo.save(AccountChangeLog.buildWithReceiveOrderDeduction(userAccount, merchantOrder));

		// ???????????????
		userConfirmToPaid(userAccount.getId(), orderId);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param orderId
	 */
	@Transactional
	public void customerServiceConfirmToPaid(@NotBlank String orderId, String note) {
		MerchantOrder platformOrder = merchantOrderRepo.findById(orderId).orElse(null);
		if (platformOrder == null) {
			throw new BizException(BizError.?????????????????????);
		}
		if (!Constant.??????????????????_?????????.equals(platformOrder.getOrderState())) {
			throw new BizException(BizError.???????????????????????????????????????????????????);
		}
		platformOrder.confirmToPaid(note);
		merchantOrderRepo.save(platformOrder);
		receiveOrderSettlement(platformOrder);
	}

	/**
	 * ????????????
	 */
	@Transactional
	public void receiveOrderSettlement(MerchantOrder merchantOrder) {
		UserAccount userAccount = merchantOrder.getReceivedAccount();
		double bounty = NumberUtil.round(merchantOrder.getGatheringAmount() * merchantOrder.getRebate() * 0.01, 4)
				.doubleValue();
		merchantOrder.updateBounty(bounty);
		merchantOrderRepo.save(merchantOrder);
		userAccount.setCashDeposit(NumberUtil.round(userAccount.getCashDeposit() + bounty, 4).doubleValue());
		userAccountRepo.save(userAccount);
		accountChangeLogRepo
				.save(AccountChangeLog.buildWithReceiveOrderBounty(userAccount, bounty, merchantOrder.getRebate()));
		generateOrderRebate(merchantOrder);
		generateActualIncomeRecord(merchantOrder);
		ThreadPoolUtils.getPaidMerchantOrderPool().schedule(() -> {
			redisTemplate.opsForList().leftPush(Constant.????????????ID, merchantOrder.getId());
		}, 1, TimeUnit.SECONDS);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param merchantOrder
	 */
	public void generateActualIncomeRecord(MerchantOrder merchantOrder) {
		double actualIncome = merchantOrder.getGatheringAmount() * (100 - merchantOrder.getRate()) / 100;
		actualIncome = NumberUtil.round(actualIncome, 4).doubleValue();
		ActualIncomeRecord actualIncomeRecord = ActualIncomeRecord.build(actualIncome, merchantOrder.getId(),
				merchantOrder.getMerchantId());
		actualIncomeRecordRepo.save(actualIncomeRecord);
	}

	/**
	 * ??????????????????
	 * 
	 * @param
	 */
	public void generateOrderRebate(MerchantOrder merchantOrder) {
		UserAccount userAccount = merchantOrder.getReceivedAccount();
		UserAccount superior = merchantOrder.getReceivedAccount().getInviter();
		while (superior != null) {
			// ???????????????????????????
			if (Constant.????????????_?????????.equals(superior.getAccountType())) {
				break;
			}
			AccountReceiveOrderChannel userAccountRebate = accountReceiveOrderChannelRepo
					.findByUserAccountIdAndChannelId(userAccount.getId(), merchantOrder.getGatheringChannelId());
			AccountReceiveOrderChannel superiorRebate = accountReceiveOrderChannelRepo
					.findByUserAccountIdAndChannelId(superior.getId(), merchantOrder.getGatheringChannelId());
			if (superiorRebate == null) {
				log.error("???????????????????????????????????????,??????????????????;????????????id:{},????????????id:{},????????????:{}", userAccount.getId(), superior.getId(),
						merchantOrder.getGatheringChannel().getChannelCode());
				break;
			}
			double rebate = NumberUtil.round(superiorRebate.getRebate() - userAccountRebate.getRebate(), 4)
					.doubleValue();
			if (rebate < 0) {
				log.error("??????????????????,?????????????????????????????????????????????;????????????id:{},????????????id:{}", userAccount.getId(), superior.getId());
				break;
			}
			double rebateAmount = NumberUtil.round(merchantOrder.getGatheringAmount() * rebate * 0.01, 4).doubleValue();
			OrderRebate orderRebate = OrderRebate.build(rebate, rebateAmount, merchantOrder.getId(), superior.getId());
			orderRebateRepo.save(orderRebate);
			userAccount = superior;
			superior = superior.getInviter();
		}
	}

	@Transactional(readOnly = true)
	public void orderRebateAutoSettlement() {
		List<OrderRebate> orderRebates = orderRebateRepo.findBySettlementTimeIsNullAndAvailableFlagTrue();
		for (OrderRebate orderRebate : orderRebates) {
			redisTemplate.opsForList().leftPush(Constant.????????????ID, orderRebate.getId());
		}
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param orderId
	 */
	@Transactional(readOnly = true)
	public void noticeOrderRebateSettlement(@NotBlank String orderId) {
		List<OrderRebate> orderRebates = orderRebateRepo.findByMerchantOrderIdAndAvailableFlagTrue(orderId);
		for (OrderRebate orderRebate : orderRebates) {
			redisTemplate.opsForList().leftPush(Constant.????????????ID, orderRebate.getId());
		}
	}

	@Transactional(readOnly = true)
	public void actualIncomeRecordAutoSettlement() {
		List<ActualIncomeRecord> actualIncomeRecords = actualIncomeRecordRepo
				.findBySettlementTimeIsNullAndAvailableFlagTrue();
		for (ActualIncomeRecord actualIncomeRecord : actualIncomeRecords) {
			redisTemplate.opsForList().leftPush(Constant.??????????????????ID, actualIncomeRecord.getId());
		}
	}

	@Transactional(readOnly = true)
	public void noticeActualIncomeRecordSettlement(@NotBlank String orderId) {
		ActualIncomeRecord actualIncomeRecord = actualIncomeRecordRepo
				.findTopByMerchantOrderIdAndAvailableFlagTrue(orderId);
		if (actualIncomeRecord != null) {
			redisTemplate.opsForList().leftPush(Constant.??????????????????ID, actualIncomeRecord.getId());
		}
	}

	@Transactional
	public void actualIncomeRecordSettlement(@NotBlank String actualIncomeRecordId) {
		ActualIncomeRecord actualIncomeRecord = actualIncomeRecordRepo.getOne(actualIncomeRecordId);
		if (actualIncomeRecord.getSettlementTime() != null) {
			log.warn("????????????????????????????????????,??????????????????;id:{}", actualIncomeRecordId);
			return;
		}
		actualIncomeRecord.setSettlementTime(new Date());
		actualIncomeRecordRepo.save(actualIncomeRecord);
		Merchant merchant = merchantRepo.getOne(actualIncomeRecord.getMerchantId());
		double withdrawableAmount = merchant.getWithdrawableAmount() + actualIncomeRecord.getActualIncome();
		merchant.setWithdrawableAmount(NumberUtil.round(withdrawableAmount, 4).doubleValue());
		merchantRepo.save(merchant);
	}

	/**
	 * ??????????????????
	 */
	@Transactional
	public void orderRebateSettlement(@NotBlank String orderRebateId) {
		OrderRebate orderRebate = orderRebateRepo.getOne(orderRebateId);
		if (orderRebate.getSettlementTime() != null) {
			log.warn("????????????????????????????????????,??????????????????;id:{}", orderRebateId);
			return;
		}
		orderRebate.settlement();
		orderRebateRepo.save(orderRebate);
		UserAccount userAccount = orderRebate.getRebateAccount();
		double cashDeposit = userAccount.getCashDeposit() + orderRebate.getRebateAmount();
		userAccount.setCashDeposit(NumberUtil.round(cashDeposit, 4).doubleValue());
		userAccountRepo.save(userAccount);
		accountChangeLogRepo.save(AccountChangeLog.buildWithOrderRebate(userAccount, orderRebate));
	}

	@Transactional(readOnly = true)
	public List<MyWaitConfirmOrderVO> findMyWaitConfirmOrder(@NotBlank String userAccountId) {
		return MyWaitConfirmOrderVO
				.convertFor(merchantOrderRepo.findByOrderStateInAndReceivedAccountIdOrderBySubmitTimeDesc(
						Arrays.asList(Constant.??????????????????_?????????), userAccountId));
	}

	@Transactional(readOnly = true)
	public List<MyWaitReceivingOrderVO> findMyWaitReceivingOrder(@NotBlank String userAccountId) {
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);
		List<AccountReceiveOrderChannel> accountReceiveOrderChannels = accountReceiveOrderChannelRepo
				.findByUserAccountIdAndChannelDeletedFlagFalse(userAccountId);
		if (CollectionUtil.isEmpty(accountReceiveOrderChannels)) {
			throw new BizException(BizError.?????????????????????????????????);
		}
		ReceiveOrderSetting merchantOrderSetting = receiveOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (merchantOrderSetting.getShowAllOrder()) {
			List<MerchantOrder> specifiedOrders = merchantOrderRepo.findTop10ByOrderStateAndSpecifiedReceivedAccountId(
					Constant.??????????????????_????????????, userAccount.getUserName());
			List<MerchantOrder> waitReceivingOrders = merchantOrderRepo
					.findTop10ByOrderStateAndSpecifiedReceivedAccountIdIsNull(Constant.??????????????????_????????????);
			// Collections.shuffle(waitReceivingOrders);
			specifiedOrders.addAll(waitReceivingOrders);
			return MyWaitReceivingOrderVO
					.convertFor(specifiedOrders.subList(0, specifiedOrders.size() >= 10 ? 10 : specifiedOrders.size()));
		}
		if (merchantOrderSetting.getUnfixedGatheringCodeReceiveOrder()) {
			List<GatheringCode> gatheringCodes = gatheringCodeRepo.findByFixedGatheringAmount(false);
			if (CollectionUtil.isEmpty(gatheringCodes)) {
				throw new BizException(BizError.??????????????????????????????);
			}

			Map<String, String> gatheringChannelCodeMap = new HashMap<>();
			for (GatheringCode gatheringCode : gatheringCodes) {
				if (!Constant.???????????????_??????.equals(gatheringCode.getState())) {
					continue;
				}
				if (!gatheringCode.getInUse()) {
					continue;
				}
				boolean flag = false;
				for (AccountReceiveOrderChannel accountReceiveOrderChannel : accountReceiveOrderChannels) {
					if (gatheringCode.getGatheringChannelId().equals(accountReceiveOrderChannel.getChannelId())
							&& Constant.????????????????????????_?????????.equals(accountReceiveOrderChannel.getState())) {
						flag = true;
					}
				}
				if (!flag) {
					continue;
				}
				gatheringChannelCodeMap.put(gatheringCode.getGatheringChannel().getChannelCode(),
						gatheringCode.getGatheringChannel().getChannelCode());
			}
			List<MerchantOrder> specifiedOrders = merchantOrderRepo.findTop10ByOrderStateAndSpecifiedReceivedAccountId(
					Constant.??????????????????_????????????, userAccount.getUserName());
			List<MerchantOrder> waitReceivingOrders = merchantOrderRepo
					.findTop10ByOrderStateAndGatheringAmountIsLessThanEqualAndGatheringChannelChannelCodeInAndAndSpecifiedReceivedAccountIdIsNullOrderBySubmitTimeDesc(
							Constant.??????????????????_????????????, userAccount.getCashDeposit(),
							new ArrayList<>(gatheringChannelCodeMap.keySet()));
			// Collections.shuffle(waitReceivingOrders);
			specifiedOrders.addAll(waitReceivingOrders);
			return MyWaitReceivingOrderVO
					.convertFor(specifiedOrders.subList(0, specifiedOrders.size() >= 10 ? 10 : specifiedOrders.size()));
		}
		List<GatheringCode> gatheringCodes = gatheringCodeRepo.findByFixedGatheringAmount(true);
		if (CollectionUtil.isEmpty(gatheringCodes)) {
			throw new BizException(BizError.??????????????????????????????);
		}
		Map<String, List<Double>> gatheringChannelCodeMap = new HashMap<>();
		for (GatheringCode gatheringCode : gatheringCodes) {
			if (!Constant.???????????????_??????.equals(gatheringCode.getState())) {
				continue;
			}
			if (!gatheringCode.getInUse()) {
				continue;
			}
			boolean flag = false;
			for (AccountReceiveOrderChannel accountReceiveOrderChannel : accountReceiveOrderChannels) {
				if (gatheringCode.getGatheringChannelId().equals(accountReceiveOrderChannel.getChannelId())
						&& Constant.????????????????????????_?????????.equals(accountReceiveOrderChannel.getState())) {
					flag = true;
				}
			}
			if (!flag) {
				continue;
			}
			if (userAccount.getCashDeposit() < gatheringCode.getGatheringAmount()) {
				continue;
			}
			if (gatheringChannelCodeMap.get(gatheringCode.getGatheringChannel().getChannelCode()) == null) {
				gatheringChannelCodeMap.put(gatheringCode.getGatheringChannel().getChannelCode(), new ArrayList<>());
			}
			gatheringChannelCodeMap.get(gatheringCode.getGatheringChannel().getChannelCode())
					.add(gatheringCode.getGatheringAmount());
		}
		List<MerchantOrder> waitReceivingOrders = new ArrayList<>();
		for (Entry<String, List<Double>> entry : gatheringChannelCodeMap.entrySet()) {
			if (CollectionUtil.isEmpty(entry.getValue())) {
				continue;
			}
			List<MerchantOrder> tmpOrders = merchantOrderRepo
					.findTop10ByOrderStateAndGatheringAmountInAndGatheringChannelChannelCodeAndSpecifiedReceivedAccountIdIsNullOrderBySubmitTimeDesc(
							Constant.??????????????????_????????????, entry.getValue(), entry.getKey());
			waitReceivingOrders.addAll(tmpOrders);
		}
		List<MerchantOrder> specifiedOrders = merchantOrderRepo
				.findTop10ByOrderStateAndSpecifiedReceivedAccountId(Constant.??????????????????_????????????, userAccount.getUserName());
		// Collections.shuffle(waitReceivingOrders);
		specifiedOrders.addAll(waitReceivingOrders);
		return MyWaitReceivingOrderVO
				.convertFor(specifiedOrders.subList(0, specifiedOrders.size() >= 10 ? 10 : specifiedOrders.size()));
	}

	/**
	 * ??????
	 * 
	 * @param userAccountId
	 * @param orderId
	 * @return
	 */
	@Lock(keys = "'receiveOrder_' + #orderId")
	@Transactional
	public void receiveOrder(@NotBlank String userAccountId, @NotBlank String orderId) {
		ReceiveOrderSetting receiveOrderSetting = receiveOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (receiveOrderSetting.getStopStartAndReceiveOrder()) {
			throw new BizException(BizError.???????????????????????????);
		}
		MerchantOrder merchantOrder = merchantOrderRepo.getOne(orderId);
		if (merchantOrder == null) {
			throw new BizException(BizError.?????????????????????);
		}
		if (!Constant.??????????????????_????????????.equals(merchantOrder.getOrderState())) {
			throw new BizException(BizError.???????????????????????????);
		}
		AccountReceiveOrderChannel receiveOrderChannel = accountReceiveOrderChannelRepo
				.findByUserAccountIdAndChannelId(userAccountId, merchantOrder.getGatheringChannelId());
		if (receiveOrderChannel == null) {
			throw new BizException(BizError.?????????????????????);
		}
		if (Constant.????????????????????????_?????????.equals(receiveOrderChannel.getState())) {
			throw new BizException(BizError.?????????????????????);
		}
		if (Constant.????????????????????????_?????????.equals(receiveOrderChannel.getState())) {
			throw new BizException(BizError.?????????????????????);
		}
		// ???????????????????????????,???????????????
		List<MyWaitConfirmOrderVO> waitConfirmOrders = findMyWaitConfirmOrder(userAccountId);
		if (waitConfirmOrders.size() >= receiveOrderSetting.getReceiveOrderUpperLimit()) {
			throw new BizException(BizError.???????????????????????????);
		}
		// ??????????????????
		if (receiveOrderSetting.getBanReceiveRepeatOrder()) {
			for (MyWaitConfirmOrderVO waitConfirmOrder : waitConfirmOrders) {
				if (waitConfirmOrder.getGatheringChannelId().equals(merchantOrder.getGatheringChannelId())
						&& waitConfirmOrder.getGatheringAmount().compareTo(merchantOrder.getGatheringAmount()) == 0) {
					throw new BizException(BizError.??????????????????);
				}
			}
		}
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);
		if (receiveOrderSetting.getCashDepositMinimumRequire() != null) {
			if (userAccount.getCashDeposit() < receiveOrderSetting.getCashDepositMinimumRequire()) {
				throw new BizException(BizError.????????????????????????????????????);
			}
		}
		Double cashDeposit = NumberUtil.round(userAccount.getCashDeposit() - merchantOrder.getGatheringAmount(), 4)
				.doubleValue();
		if (cashDeposit - receiveOrderSetting.getCashPledge() < 0) {
			throw new BizException(BizError.???????????????????????????);
		}
		GatheringCode gatheringCode = getGatheringCodeStorage(userAccountId,
				merchantOrder.getGatheringChannel().getChannelCode(), merchantOrder.getGatheringAmount());

		if(!"1149365394574671872".equals(gatheringCode.getGatheringChannelId())){
			String gatheringCodeStorageId = gatheringCode.getStorageId();
			if (StrUtil.isBlank(gatheringCodeStorageId)) {
				throw new BizException(BizError.?????????????????????????????????????????????);
			}
		}


		userAccount.setCashDeposit(cashDeposit);
		userAccountRepo.save(userAccount);
		Integer orderEffectiveDuration = receiveOrderSetting.getOrderPayEffectiveDuration();
		merchantOrder.updateReceived(userAccount.getId(), gatheringCode, receiveOrderChannel.getRebate());
		merchantOrder.updateUsefulTime(
				DateUtil.offset(merchantOrder.getReceivedTime(), DateField.MINUTE, orderEffectiveDuration));
		merchantOrderRepo.save(merchantOrder);
		accountChangeLogRepo.save(AccountChangeLog.buildWithReceiveOrderDeduction(userAccount, merchantOrder));
	}

	@Transactional(readOnly = true)
	public PageResult<MerchantOrderVO> findMerchantOrderByPage(MerchantOrderQueryCondParam param) {
		Specification<MerchantOrder> spec = new Specification<MerchantOrder>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MerchantOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo()));
				}
				if (StrUtil.isNotBlank(param.getMerchantName())) {
					predicates.add(builder.equal(root.join("merchant", JoinType.INNER).get("merchantName"),
							param.getMerchantName()));
				}
				if (StrUtil.isNotBlank(param.getMerchantOrderNo())) {
					predicates.add(builder.equal(root.join("payInfo", JoinType.INNER).get("orderNo"),
							param.getMerchantOrderNo()));
				}
				if (StrUtil.isNotBlank(param.getGatheringChannelCode())) {
					predicates.add(builder.equal(root.join("gatheringChannel", JoinType.INNER).get("channelCode"),
							param.getGatheringChannelCode()));
				}
				if (StrUtil.isNotBlank(param.getOrderState())) {
					predicates.add(builder.equal(root.get("orderState"), param.getOrderState()));
				}
				if (StrUtil.isNotBlank(param.getReceiverUserName())) {
					predicates.add(builder.equal(root.join("receivedAccount", JoinType.INNER).get("userName"),
							param.getReceiverUserName()));
				}
				if (param.getSubmitStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("submitTime").as(Date.class),
							DateUtil.beginOfDay(param.getSubmitStartTime())));
				}
				if (param.getSubmitEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("submitTime").as(Date.class),
							DateUtil.endOfDay(param.getSubmitEndTime())));
				}
				if (param.getReceiveOrderStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.beginOfDay(param.getReceiveOrderStartTime())));
				}
				if (param.getReceiveOrderEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.endOfDay(param.getReceiveOrderEndTime())));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<MerchantOrder> result = merchantOrderRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("submitTime"))));
		PageResult<MerchantOrderVO> pageResult = new PageResult<>(MerchantOrderVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

	@Transactional(readOnly = true)
	public PageResult<ReceiveOrderRecordVO> findMyReceiveOrderRecordByPage(MyReceiveOrderRecordQueryCondParam param) {
		Specification<MerchantOrder> spec = new Specification<MerchantOrder>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MerchantOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getGatheringChannelCode())) {
					predicates.add(builder.equal(root.join("gatheringChannel", JoinType.INNER).get("channelCode"),
							param.getGatheringChannelCode()));
				}
				if (StrUtil.isNotBlank(param.getReceiverUserName())) {
					predicates.add(builder.equal(root.join("receivedAccount", JoinType.INNER).get("userName"),
							param.getReceiverUserName()));
				}
				if (param.getReceiveOrderTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.beginOfDay(param.getReceiveOrderTime())));
					predicates.add(builder.lessThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.endOfDay(param.getReceiveOrderTime())));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<MerchantOrder> result = merchantOrderRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("submitTime"))));
		PageResult<ReceiveOrderRecordVO> pageResult = new PageResult<>(
				ReceiveOrderRecordVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	/**
	 * ????????????
	 * 
	 * @param id
	 */
	@Transactional
	public void cancelOrder(@NotBlank String id) {
		MerchantOrder platformOrder = merchantOrderRepo.getOne(id);
		if (!Constant.??????????????????_????????????.equals(platformOrder.getOrderState())) {
			throw new BizException(BizError.???????????????????????????????????????????????????);
		}
		platformOrder.setOrderState(Constant.??????????????????_????????????);
		platformOrder.setDealTime(new Date());
		merchantOrderRepo.save(platformOrder);
	}

	@Transactional
	public void forceCancelOrder(@NotBlank String id) {
		MerchantOrder merchantOrder = merchantOrderRepo.getOne(id);
		if (!Constant.??????????????????_?????????.equals(merchantOrder.getOrderState())) {
			throw new BizException(BizError.??????????????????????????????????????????????????????);
		}
		merchantOrder.customerCancelOrderRefund();
		merchantOrderRepo.save(merchantOrder);
		UserAccount userAccount = merchantOrder.getReceivedAccount();
		Double cashDeposit = NumberUtil.round(userAccount.getCashDeposit() + merchantOrder.getGatheringAmount(), 4)
				.doubleValue();
		userAccount.setCashDeposit(cashDeposit);
		userAccountRepo.save(userAccount);
		accountChangeLogRepo.save(AccountChangeLog.buildWithCustomerCancelOrderRefund(userAccount, merchantOrder));
		cashDeposit = NumberUtil.round(userAccount.getCashDeposit() - merchantOrder.getBounty(), 4).doubleValue();
		if (cashDeposit < 0) {
			throw new BizException(BizError.???????????????);
		}
		userAccount.setCashDeposit(cashDeposit);
		userAccountRepo.save(userAccount);
		accountChangeLogRepo
				.save(AccountChangeLog.buildWithCustomerCancelOrderDeductBounty(userAccount, merchantOrder));
		List<OrderRebate> orderRebates = orderRebateRepo.findByMerchantOrderIdAndAvailableFlagTrue(id);
		for (OrderRebate orderRebate : orderRebates) {
			orderRebate.setAvailableFlag(false);
			orderRebateRepo.save(orderRebate);
			UserAccount superiorAccount = orderRebate.getRebateAccount();
			Double superiorAccountCashDeposit = NumberUtil
					.round(superiorAccount.getCashDeposit() - orderRebate.getRebateAmount(), 4).doubleValue();
			if (superiorAccountCashDeposit < 0) {
				throw new BizException(BizError.???????????????);
			}
			superiorAccount.setCashDeposit(superiorAccountCashDeposit);
			userAccountRepo.save(superiorAccount);
			accountChangeLogRepo
					.save(AccountChangeLog.buildWithCustomerCancelOrderDeductRebate(superiorAccount, orderRebate));
		}

		ActualIncomeRecord actualIncomeRecord = actualIncomeRecordRepo.findTopByMerchantOrderIdAndAvailableFlagTrue(id);
		if (actualIncomeRecord != null) {
			actualIncomeRecord.setAvailableFlag(false);
			actualIncomeRecordRepo.save(actualIncomeRecord);
			Merchant merchant = actualIncomeRecord.getMerchant();
			Double withdrawableAmount = NumberUtil
					.round(merchant.getWithdrawableAmount() - actualIncomeRecord.getActualIncome(), 4).doubleValue();
			if (withdrawableAmount < 0) {
				throw new BizException(BizError.?????????????????????);
			}
			merchant.setWithdrawableAmount(withdrawableAmount);
			merchantRepo.save(merchant);
		}
	}

	@Transactional
	public void orderTimeoutDeal() {
		Date now = new Date();
		List<MerchantOrder> orders = merchantOrderRepo.findByOrderStateAndUsefulTimeLessThan(Constant.??????????????????_????????????, now);
		for (MerchantOrder order : orders) {
			order.setDealTime(now);
			order.setOrderState(Constant.??????????????????_????????????);
			merchantOrderRepo.save(order);
		}
	}

	@Transactional
	public void manualStartOrder(@NotEmpty List<ManualStartOrderParam> params) {
		for (ManualStartOrderParam param : params) {
			Merchant merchant = merchantRepo.findByMerchantNumAndDeletedFlagIsFalse(param.getMerchantNum());
			String amount = new DecimalFormat("###################.###########").format(param.getGatheringAmount());
			StartOrderParam startOrderParam = new StartOrderParam();
			startOrderParam.setMerchantNum(param.getMerchantNum());
			startOrderParam.setOrderNo(param.getOrderNo());
			startOrderParam.setPayType(param.getGatheringChannelCode());
			startOrderParam.setAmount(amount);
			startOrderParam.setNotifyUrl(param.getNotifyUrl());
			startOrderParam.setReturnUrl(param.getReturnUrl());
			startOrderParam.setAttch(param.getAttch());
			startOrderParam.setSpecifiedReceivedAccountId(param.getSpecifiedReceivedAccountId());
			startOrderParam.setPublishTime(param.getPublishTime());
			String sign = startOrderParam.getMerchantNum() + startOrderParam.getOrderNo() + amount
					+ param.getNotifyUrl() + merchant.getSecretKey();
			sign = new Digester(DigestAlgorithm.MD5).digestHex(sign);
			startOrderParam.setSign(sign);
			startOrder(startOrderParam);
		}

	}

	@ParamValid
	@Transactional
	public StartOrderSuccessVO startOrder(StartOrderParam param) {
		ReceiveOrderSetting receiveOrderSetting = receiveOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (receiveOrderSetting.getStopStartAndReceiveOrder()) {
			throw new BizException(BizError.?????????????????????????????????);
		}
		Merchant merchant = merchantRepo.findByMerchantNumAndDeletedFlagIsFalse(param.getMerchantNum());
		if (merchant == null) {
			throw new BizException(BizError.???????????????);
		}
		if (!NumberUtil.isNumber(param.getAmount())) {
			throw new BizException(BizError.?????????????????????);
		}
		if (Double.parseDouble(param.getAmount()) <= 0) {
			throw new BizException(BizError.???????????????????????????0);
		}
		String sign = param.getMerchantNum() + param.getOrderNo() + param.getAmount() + param.getNotifyUrl()
				+ merchant.getSecretKey();
		sign = new Digester(DigestAlgorithm.MD5).digestHex(sign);
		if (!sign.equals(param.getSign())) {
			throw new BizException(BizError.???????????????);
		}
		//?????????????????????
		GatheringChannel gatheringChannel = gatheringChannelRepo
				.findByChannelCodeAndDeletedFlagIsFalse(param.getPayType());
		if (gatheringChannel == null) {
			throw new BizException(BizError.?????????????????????????????????);
		}
		if (!gatheringChannel.getEnabled()) {
			throw new BizException(BizError.?????????????????????????????????);
		}
		//???????????????????????????????????????????????????????????????
		GatheringChannelRate gatheringChannelRate = gatheringChannelRateRepo
				.findByMerchantIdAndChannelChannelCode(merchant.getId(), param.getPayType());
		if (gatheringChannelRate == null) {
			throw new BizException(BizError.????????????????????????????????????);
		}
		if (!gatheringChannelRate.getEnabled()) {
			throw new BizException(BizError.????????????????????????????????????);
		}

		Integer orderEffectiveDuration = receiveOrderSetting.getReceiveOrderEffectiveDuration();
		MerchantOrder merchantOrder = param.convertToPo(merchant.getId(), gatheringChannel.getId(),
				gatheringChannelRate.getRate(), orderEffectiveDuration);
		MerchantOrderPayInfo payInfo = param.convertToPayInfoPo(merchantOrder.getId());
		merchantOrder.setPayInfoId(payInfo.getId());
		merchantOrderRepo.save(merchantOrder);
		merchantOrderPayInfoRepo.save(payInfo);
		return StartOrderSuccessVO.convertFor(merchantOrder.getOrderNo());
	}

	/**
	 * ????????????????????????
	 * 
	 * @param merchantOrderId
	 */
	@Transactional
	public String paySuccessAsynNotice(@NotBlank String merchantOrderId) {
		MerchantOrder merchantOrder = merchantOrderRepo.getOne(merchantOrderId);
		MerchantOrderPayInfo payInfo = merchantOrderPayInfoRepo.findByMerchantOrderId(merchantOrderId);
		if (Constant.??????????????????????????????_????????????.equals(payInfo.getNoticeState())) {
			log.warn("?????????????????????????????????,??????????????????;????????????id???{}", merchantOrderId);
			return Constant.?????????????????????????????????;
		}
		Merchant merchant = merchantRepo.findByMerchantNumAndDeletedFlagIsFalse(payInfo.getMerchantNum());
		if (merchant == null) {
			throw new BizException(BizError.???????????????);
		}

		String sign = Constant.???????????????????????? + payInfo.getMerchantNum() + payInfo.getOrderNo() + payInfo.getAmount()
				+ merchant.getSecretKey();
		sign = new Digester(DigestAlgorithm.MD5).digestHex(sign);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantNum", payInfo.getMerchantNum());
		paramMap.put("orderNo", payInfo.getOrderNo());
		paramMap.put("platformOrderNo", payInfo.getMerchantOrder().getOrderNo());
		paramMap.put("amount", payInfo.getAmount());
		paramMap.put("actualPayAmount", merchantOrder.getGatheringAmount());
		paramMap.put("attch", payInfo.getAttch());
		paramMap.put("state", Constant.????????????????????????);
		paramMap.put("payTime",
				DateUtil.format(payInfo.getMerchantOrder().getConfirmTime(), DatePattern.NORM_DATETIME_PATTERN));
		paramMap.put("sign", sign);
		String result = "fail";
		// ??????3???
		for (int i = 0; i < 3; i++) {
			try {
				result = HttpUtil.get(payInfo.getNotifyUrl(), paramMap, 2500);
				if (Constant.?????????????????????????????????.equals(result)) {
					break;
				}
			} catch (Exception e) {
				result = e.getMessage();
				log.error(MessageFormat.format("??????????????????????????????????????????????????????,id???{0}", merchantOrderId), e);
			}
		}
		payInfo.setNoticeState(
				Constant.?????????????????????????????????.equals(result) ? Constant.??????????????????????????????_???????????? : Constant.??????????????????????????????_????????????);
		merchantOrderPayInfoRepo.save(payInfo);
		return result;
	}

	@Transactional(readOnly = true)
	public PageResult<ReceiveOrderRecordVO> findLowerLevelAccountReceiveOrderRecordByPage(
			LowerLevelAccountReceiveOrderQueryCondParam param) {
		UserAccount currentAccount = userAccountRepo.getOne(param.getCurrentAccountId());
		UserAccount lowerLevelAccount = currentAccount;
		if (StrUtil.isNotBlank(param.getUserName())) {
			lowerLevelAccount = userAccountRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
			if (lowerLevelAccount == null) {
				throw new BizException(BizError.??????????????????);
			}
			// ??????????????????????????????????????????????????????????????????
			if (!lowerLevelAccount.getAccountLevelPath().startsWith(currentAccount.getAccountLevelPath())) {
				throw new BizException(BizError.???????????????????????????????????????????????????????????????);
			}
		}
		String lowerLevelAccountId = lowerLevelAccount.getId();
		String lowerLevelAccountLevelPath = lowerLevelAccount.getAccountLevelPath();

		Specification<MerchantOrder> spec = new Specification<MerchantOrder>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MerchantOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getUserName())) {
					predicates.add(builder.equal(root.get("receivedAccountId"), lowerLevelAccountId));
				} else {
					predicates.add(builder.like(root.join("receivedAccount", JoinType.INNER).get("accountLevelPath"),
							lowerLevelAccountLevelPath + "%"));
				}
				if (StrUtil.isNotBlank(param.getOrderState())) {
					predicates.add(builder.equal(root.get("orderState"), param.getOrderState()));
				}
				if (StrUtil.isNotBlank(param.getGatheringChannelCode())) {
					predicates.add(builder.equal(root.join("gatheringChannel", JoinType.INNER).get("channelCode"),
							param.getGatheringChannelCode()));
				}
				if (param.getStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.beginOfDay(param.getStartTime())));
				}
				if (param.getEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.endOfDay(param.getEndTime())));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<MerchantOrder> result = merchantOrderRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("submitTime"))));
		PageResult<ReceiveOrderRecordVO> pageResult = new PageResult<>(
				ReceiveOrderRecordVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	@Transactional
	public void updateNote(@NotBlank String id, String note, String merchantId) {
		MerchantOrder merchantOrder = merchantOrderRepo.getOne(id);
		if (!merchantOrder.getMerchantId().equals(merchantId)) {
			throw new BizException(BizError.??????????????????????????????);
		}
		updateNoteInner(id, note);
	}

	@Transactional
	public void updateNoteInner(@NotBlank String id, String note) {
		MerchantOrder merchantOrder = merchantOrderRepo.getOne(id);
		merchantOrder.setNote(note);
		merchantOrderRepo.save(merchantOrder);
	}

}
