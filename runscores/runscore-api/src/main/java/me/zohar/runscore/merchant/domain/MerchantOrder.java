package me.zohar.runscore.merchant.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import me.zohar.runscore.gatheringcode.domain.GatheringCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import lombok.Getter;
import lombok.Setter;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.useraccount.domain.UserAccount;

@Getter
@Setter
@Entity
@Table(name = "merchant_order")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MerchantOrder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id
	 */
	@Id
	@Column(name = "id", length = 32)
	private String id;

	/**
	 * 订单号
	 */
	private String orderNo;

	/**
	 * 收款金额
	 */
	private Double gatheringAmount;

	/**
	 * 提交时间
	 */
	private Date submitTime;

	/**
	 * 有效时间
	 */
	private Date usefulTime;

	/**
	 * 订单状态
	 */
	private String orderState;

	/**
	 * 备注
	 */
	private String note;

	@Column(name = "merchant_id", length = 32)
	private String merchantId;

	/**
	 * 接单人账号id
	 */
	@Column(name = "received_account_id", length = 32)
	private String receivedAccountId;

	/**
	 * 接单时间
	 */
	private Date receivedTime;

	private String gatheringCodeStorageId;

	/**
	 * 处理时间
	 */
	private Date dealTime;

	/**
	 * 确认时间
	 */
	private Date confirmTime;

	/**
	 * 费率
	 */
	private Double rate;

	/**
	 * 返点
	 */
	private Double rebate;

	/**
	 * 奖励金
	 */
	private Double bounty;
	
	/**
	 * 指定接单人
	 */
	private String specifiedReceivedAccountId;

	private String payee;

	/**
	 * 乐观锁版本号
	 */
	@Version
	private Long version;

	@Column(name = "gathering_channel_id", length = 32)
	private String gatheringChannelId;



	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gathering_channel_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private GatheringChannel gatheringChannel;

	@Column(name = "gathering_code_id", length = 32)
	private String gatheringCodeId;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gathering_code_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private GatheringCode gatheringCode;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private Merchant merchant;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "received_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private UserAccount receivedAccount;

	@Column(name = "pay_info_id", length = 32)
	private String payInfoId;

	@NotFound(action = NotFoundAction.IGNORE)
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pay_info_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MerchantOrderPayInfo payInfo;

	public void updateBounty(Double bounty) {
		this.setBounty(bounty);
	}

	public void confirmToPaid(String note) {
		this.setOrderState(Constant.商户订单状态_已支付);
		this.setConfirmTime(new Date());
		this.setDealTime(this.getConfirmTime());
		this.setNote(note);
	}

	public void updateReceived(String receivedAccountId, String gatheringCodeStorageId, Double rebate) {
		this.setReceivedAccountId(receivedAccountId);
		this.setGatheringCodeStorageId(gatheringCodeStorageId);
		this.setOrderState(Constant.商户订单状态_已接单);
		this.setReceivedTime(new Date());
		this.setRebate(rebate);
	}

	public void updateReceived(String receivedAccountId, GatheringCode gatheringCode, Double rebate) {
		this.setReceivedAccountId(receivedAccountId);
		this.setGatheringCodeStorageId(gatheringCode.getStorageId());
		this.setGatheringCodeId(gatheringCode.getId());
		this.setPayee(gatheringCode.getPayee());
		this.setOrderState(Constant.商户订单状态_已接单);
		this.setReceivedTime(new Date());
		this.setRebate(rebate);
	}

	public void customerCancelOrderRefund() {
		this.setOrderState(Constant.商户订单状态_取消订单退款);
		this.setConfirmTime(new Date());
		this.setDealTime(this.getConfirmTime());
	}

	public void updateUsefulTime(Date usefulTime) {
		this.setUsefulTime(usefulTime);
	}

}
