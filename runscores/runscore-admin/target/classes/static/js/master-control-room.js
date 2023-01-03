var masterControlRoomVM = new Vue({
	el : '#master-control-room',
	data : {
		/**
		 * 系统设置start
		 */
		websiteTitle : '',
		payTechnicalSupport : '',
		currencyUnit : '',
		singleDeviceLoginFlag : false,
		showRankingListFlag : false,

		/**
		 * 注册设置start
		 */
		registerEnabled : '',
		inviteCodeEffectiveDuration : '',
		inviteRegisterEnabled : false,
		onlyOpenMemberAccount : false,
		agentExplain : '',

		/**
		 * 商户订单start
		 */
		receiveOrderEffectiveDuration : '',
		orderPayEffectiveDuration : '',
		receiveOrderUpperLimit : '',
		showAllOrder : false,
		cashDepositMinimumRequire : '',
		cashPledge : '',
		unfixedGatheringCodeReceiveOrder : false,
		stopStartAndReceiveOrder : false,
		banReceiveRepeatOrder : false,
		receiveOrderSound : false,
		auditGatheringCode : false,

		/**
		 * 充值start
		 */
		rechargeOrderEffectiveDuration : '',
		rechargeReturnWaterRate : '',
		rechargeLowerLimit : '',
		rechargeUpperLimit : '',
		rechargeReturnWaterRateEnabled : false,
		quickInputAmount : '',
		rechargeExplain : '',

		/**
		 * 提现start
		 */
		everydayWithdrawTimesUpperLimit : '',
		withdrawLowerLimit : '',
		withdrawUpperLimit : '',
		withdrawExplain : '',

		/**
		 * 客服二维码start
		 */
		qrcodeStorageId : '',
		customerServiceExplain : '',
		uploadQrcodeFlag : false,

		/**
		 * 刷新缓存start
		 */
		refreshConfigItem : true,
		refreshDict : true
	},
	computed : {},
	created : function() {
	},
	mounted : function() {
		var that = this;
		that.loadSystemSetting();
		that.loadRegisterSetting();
		that.loadReceiveOrderSetting();
		that.loadRechargeSetting();
		that.loadWithdrawSetting();
		that.loadCustomerQrcodeSetting();

		$('.qrcode-pic').on('filebatchuploadsuccess', function(event, data) {
			that.qrcodeStorageId = data.response.data.join(',');
			that.uploadQrcodeFlag = false;
		});
	},
	methods : {

		loadSystemSetting : function() {
			var that = this;
			that.$http.get('/masterControl/getSystemSetting').then(function(res) {
				if (res.body.data != null) {
					that.websiteTitle = res.body.data.websiteTitle;
					that.payTechnicalSupport = res.body.data.payTechnicalSupport;
					that.currencyUnit = res.body.data.currencyUnit;
					that.singleDeviceLoginFlag = res.body.data.singleDeviceLoginFlag;
					that.showRankingListFlag = res.body.data.showRankingListFlag;
				}
			});
		},

		updateSystemSetting : function() {
			var that = this;
			var websiteTitle = that.websiteTitle;
			if (websiteTitle === null || websiteTitle === '') {
				layer.alert('请输入网站标题', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var payTechnicalSupport = that.payTechnicalSupport;
			if (payTechnicalSupport === null || payTechnicalSupport === '') {
				layer.alert('请输入支付技术支持', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var currencyUnit = that.currencyUnit;
			if (currencyUnit === null || currencyUnit === '') {
				layer.alert('请输入货币单位', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var singleDeviceLoginFlag = that.singleDeviceLoginFlag;
			if (singleDeviceLoginFlag === null) {
				layer.alert('请设置是否限制单一设备登录', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var showRankingListFlag = that.showRankingListFlag;
			if (showRankingListFlag === null) {
				layer.alert('请设置是否显示排行榜', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}

			that.$http.post('/masterControl/updateSystemSetting', {
				websiteTitle : websiteTitle,
				payTechnicalSupport : payTechnicalSupport,
				currencyUnit : currencyUnit,
				singleDeviceLoginFlag : singleDeviceLoginFlag,
				showRankingListFlag : showRankingListFlag
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.loadSystemSetting();
			});
		},

		loadRegisterSetting : function() {
			var that = this;
			that.$http.get('/masterControl/getRegisterSetting').then(function(res) {
				if (res.body.data != null) {
					that.registerEnabled = res.body.data.registerEnabled;
					that.inviteCodeEffectiveDuration = res.body.data.inviteCodeEffectiveDuration;
					that.inviteRegisterEnabled = res.body.data.inviteRegisterEnabled;
					that.onlyOpenMemberAccount = res.body.data.onlyOpenMemberAccount;
					that.agentExplain = res.body.data.agentExplain;
				}
			});
		},

		updateRegisterSetting : function() {
			var that = this;
			var registerEnabled = that.registerEnabled;
			if (registerEnabled === null) {
				layer.alert('请设置是否开放注册功能', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var inviteCodeEffectiveDuration = that.inviteCodeEffectiveDuration;
			if (inviteCodeEffectiveDuration === null || inviteCodeEffectiveDuration === '') {
				layer.alert('请输入邀请码有效时长', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var inviteRegisterEnabled = that.inviteRegisterEnabled;
			if (inviteRegisterEnabled === null) {
				layer.alert('请选择是否启用邀请码注册模式', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var onlyOpenMemberAccount = that.onlyOpenMemberAccount;
			if (onlyOpenMemberAccount === null) {
				layer.alert('请选择是否限制下级开户只能开会员账号', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var agentExplain = that.agentExplain;
			if (agentExplain === null || agentExplain === '') {
				layer.alert('请输入代理说明', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/masterControl/updateRegisterSetting', {
				registerEnabled : registerEnabled,
				inviteCodeEffectiveDuration : inviteCodeEffectiveDuration,
				inviteRegisterEnabled : inviteRegisterEnabled,
				onlyOpenMemberAccount : onlyOpenMemberAccount,
				agentExplain : agentExplain
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.loadRegisterSetting();
			});
		},

		loadReceiveOrderSetting : function() {
			var that = this;
			that.$http.get('/masterControl/getReceiveOrderSetting').then(function(res) {
				if (res.body.data != null) {
					that.stopStartAndReceiveOrder = res.body.data.stopStartAndReceiveOrder;
					that.receiveOrderEffectiveDuration = res.body.data.receiveOrderEffectiveDuration;
					that.orderPayEffectiveDuration = res.body.data.orderPayEffectiveDuration;
					that.receiveOrderUpperLimit = res.body.data.receiveOrderUpperLimit;
					that.showAllOrder = res.body.data.showAllOrder;
					that.cashDepositMinimumRequire = res.body.data.cashDepositMinimumRequire;
					that.cashPledge = res.body.data.cashPledge;
					that.unfixedGatheringCodeReceiveOrder = res.body.data.unfixedGatheringCodeReceiveOrder;
					that.banReceiveRepeatOrder = res.body.data.banReceiveRepeatOrder;
					that.receiveOrderSound = res.body.data.receiveOrderSound;
					that.auditGatheringCode = res.body.data.auditGatheringCode;
				}
			});
		},

		updateReceiveOrderSetting : function() {
			var that = this;
			var stopStartAndReceiveOrder = that.stopStartAndReceiveOrder;
			if (stopStartAndReceiveOrder === null) {
				layer.alert('请选择是否暂停发单接单', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var banReceiveRepeatOrder = that.banReceiveRepeatOrder;
			if (banReceiveRepeatOrder === null) {
				layer.alert('请选择是否禁止接重复单', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var receiveOrderEffectiveDuration = that.receiveOrderEffectiveDuration;
			if (receiveOrderEffectiveDuration === null || receiveOrderEffectiveDuration === '') {
				layer.alert('请输入接单有效时长', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var orderPayEffectiveDuration = that.orderPayEffectiveDuration;
			if (orderPayEffectiveDuration === null || orderPayEffectiveDuration === '') {
				layer.alert('请输入支付有效时长', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var receiveOrderUpperLimit = that.receiveOrderUpperLimit;
			if (receiveOrderUpperLimit === null || receiveOrderUpperLimit === '') {
				layer.alert('请输入接单数量上限', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var showAllOrder = that.showAllOrder;
			if (showAllOrder === null) {
				layer.alert('请选择是否显示所有订单', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var cashDepositMinimumRequire = that.cashDepositMinimumRequire;
			if (cashDepositMinimumRequire === null || cashDepositMinimumRequire === '') {
				layer.alert('请输入保证金最低要求金额', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var cashPledge = that.cashPledge;
			if (cashPledge === null || cashPledge === '') {
				layer.alert('请输入押金', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var unfixedGatheringCodeReceiveOrder = that.unfixedGatheringCodeReceiveOrder;
			if (unfixedGatheringCodeReceiveOrder === null) {
				layer.alert('请选择是否支持不固定金额收款码接单', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var receiveOrderSound = that.receiveOrderSound;
			if (receiveOrderSound === null) {
				layer.alert('请选择是否开启接单提示音', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var auditGatheringCode = that.auditGatheringCode;
			if (auditGatheringCode === null) {
				layer.alert('请设置是否审核收款码', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/masterControl/updateReceiveOrderSetting', {
				receiveOrderEffectiveDuration : receiveOrderEffectiveDuration,
				orderPayEffectiveDuration : orderPayEffectiveDuration,
				receiveOrderUpperLimit : receiveOrderUpperLimit,
				showAllOrder : showAllOrder,
				cashDepositMinimumRequire : cashDepositMinimumRequire,
				cashPledge : cashPledge,
				unfixedGatheringCodeReceiveOrder : unfixedGatheringCodeReceiveOrder,
				stopStartAndReceiveOrder : stopStartAndReceiveOrder,
				banReceiveRepeatOrder : banReceiveRepeatOrder,
				receiveOrderSound : receiveOrderSound,
				auditGatheringCode : auditGatheringCode
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.loadReceiveOrderSetting();
			});
		},

		loadRechargeSetting : function() {
			var that = this;
			that.$http.get('/masterControl/getRechargeSetting').then(function(res) {
				if (res.body.data != null) {
					that.rechargeOrderEffectiveDuration = res.body.data.orderEffectiveDuration;
					that.rechargeReturnWaterRate = res.body.data.returnWaterRate;
					that.rechargeLowerLimit = res.body.data.rechargeLowerLimit;
					that.rechargeUpperLimit = res.body.data.rechargeUpperLimit;
					that.rechargeReturnWaterRateEnabled = res.body.data.returnWaterRateEnabled;
					that.quickInputAmount = res.body.data.quickInputAmount;
					that.rechargeExplain = res.body.data.rechargeExplain;
				}
			});
		},

		updateRechargeSetting : function() {
			var that = this;
			var rechargeOrderEffectiveDuration = that.rechargeOrderEffectiveDuration;
			if (rechargeOrderEffectiveDuration === null || rechargeOrderEffectiveDuration === '') {
				layer.alert('请输入充值订单有效时长', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var rechargeReturnWaterRate = that.rechargeReturnWaterRate;
			if (rechargeReturnWaterRate === null || rechargeReturnWaterRate === '') {
				layer.alert('请输入充值返水率', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var rechargeReturnWaterRateEnabled = that.rechargeReturnWaterRateEnabled;
			if (rechargeReturnWaterRateEnabled === null) {
				layer.alert('请选择是否启用', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var rechargeLowerLimit = that.rechargeLowerLimit;
			if (rechargeLowerLimit === null || rechargeLowerLimit === '') {
				layer.alert('请输入充值最低限额', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var rechargeUpperLimit = that.rechargeUpperLimit;
			if (rechargeUpperLimit === null || rechargeUpperLimit === '') {
				layer.alert('请输入充值最高限额', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var quickInputAmount = that.quickInputAmount;
			if (quickInputAmount === null || quickInputAmount === '') {
				layer.alert('请输入快速设置金额', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var rechargeExplain = that.rechargeExplain;
			if (rechargeExplain === null || rechargeExplain === '') {
				layer.alert('请输入充值说明', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}

			that.$http.post('/masterControl/updateRechargeSetting', {
				orderEffectiveDuration : rechargeOrderEffectiveDuration,
				returnWaterRate : rechargeReturnWaterRate,
				rechargeLowerLimit : rechargeLowerLimit,
				rechargeUpperLimit : rechargeUpperLimit,
				returnWaterRateEnabled : rechargeReturnWaterRateEnabled,
				quickInputAmount : quickInputAmount,
				rechargeExplain : rechargeExplain
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.loadRechargeSetting();
			});
		},

		loadWithdrawSetting : function() {
			var that = this;
			that.$http.get('/masterControl/getWithdrawSetting').then(function(res) {
				if (res.body.data != null) {
					that.everydayWithdrawTimesUpperLimit = res.body.data.everydayWithdrawTimesUpperLimit;
					that.withdrawLowerLimit = res.body.data.withdrawLowerLimit;
					that.withdrawUpperLimit = res.body.data.withdrawUpperLimit;
					that.withdrawExplain = res.body.data.withdrawExplain;
				}
			});
		},

		updateWithdrawSetting : function() {
			var that = this;
			var everydayWithdrawTimesUpperLimit = that.everydayWithdrawTimesUpperLimit;
			if (everydayWithdrawTimesUpperLimit === null || everydayWithdrawTimesUpperLimit === '') {
				layer.alert('请输入每日提现次数上限', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var withdrawLowerLimit = that.withdrawLowerLimit;
			if (withdrawLowerLimit === null || withdrawLowerLimit === '') {
				layer.alert('请输入提现最低限额', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var withdrawUpperLimit = that.withdrawUpperLimit;
			if (withdrawUpperLimit === null || withdrawUpperLimit === '') {
				layer.alert('请输入提现最高限额', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var withdrawExplain = that.withdrawExplain;
			if (withdrawExplain === null || withdrawExplain === '') {
				layer.alert('请输入提现说明', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}

			that.$http.post('/masterControl/updateWithdrawSetting', {
				everydayWithdrawTimesUpperLimit : everydayWithdrawTimesUpperLimit,
				withdrawLowerLimit : withdrawLowerLimit,
				withdrawUpperLimit : withdrawUpperLimit,
				withdrawExplain : withdrawExplain
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.loadWithdrawSetting();
			});
		},

		loadCustomerQrcodeSetting : function() {
			var that = this;
			that.$http.get('/masterControl/getCustomerQrcodeSetting').then(function(res) {
				if (res.body.data != null) {
					that.customerServiceExplain = res.body.data.customerServiceExplain;
					that.qrcodeStorageId = res.body.data.qrcodeStorageId;
				}
			});
		},

		showUploadQrcodeModal : function() {
			this.uploadQrcodeFlag = true;
			$('.qrcode-pic').fileinput('destroy').fileinput({
				uploadAsync : false,
				browseOnZoneClick : true,
				showBrowse : false,
				showCaption : false,
				showClose : true,
				showRemove : false,
				showUpload : false,
				dropZoneTitle : '点击选择二维码',
				dropZoneClickTitle : '',
				layoutTemplates : {
					footer : ''
				},
				maxFileCount : 1,
				uploadUrl : '/storage/uploadPic',
				enctype : 'multipart/form-data',
				allowedFileExtensions : [ 'jpg', 'png', 'bmp', 'jpeg' ],
				initialPreview : [],
				initialPreviewAsData : true,
				initialPreviewConfig : []
			});
		},

		uploadQrcode : function() {
			var filesCount = $('.qrcode-pic').fileinput('getFilesCount');
			if (filesCount == 0) {
				layer.alert('请选择要上传的二维码', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			$('.qrcode-pic').fileinput('upload');
		},

		updateCustomerQrcodeSetting : function() {
			var that = this;
			var qrcodeStorageId = that.qrcodeStorageId;
			if (qrcodeStorageId === null || qrcodeStorageId === '') {
				layer.alert('请上传二维码', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			var customerServiceExplain = that.customerServiceExplain;
			if (customerServiceExplain === null || customerServiceExplain === '') {
				layer.alert('请输入客服说明', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/masterControl/updateCustomerQrcodeSetting', {
				qrcodeStorageId : that.qrcodeStorageId,
				customerServiceExplain : that.customerServiceExplain
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.loadCustomerQrcodeSetting();
			});
		},

		refreshCache : function() {
			var cacheItems = [];
			var that = this;
			if (that.refreshConfigItem) {
				cacheItems.push('config*');
			}
			if (that.refreshDict) {
				cacheItems.push('dict*');
			}
			if (cacheItems.length == 0) {
				layer.alert('请勾选要刷新的缓存项', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/masterControl/refreshCache', cacheItems).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
			});
		}
	}
});