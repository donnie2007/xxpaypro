var accountManageVM = new Vue({
	el : '#account-manage',
	data : {
		accountTypeDictItems : [],
		rebateDictItems : [],
		accountStateDictItems : [],
		userName : '',
		realName : '',

		addUserAccountFlag : false,
		accountEditFlag : false,
		selectedAccount : {},

		modifyLoginPwdFlag : false,
		newLoginPwd : '',

		modifyMoneyPwdFlag : false,
		newMoneyPwd : '',

		bindBankInfoFlag : false,
		bankInfo : {},

		adjustCashDepositFlag : false,
		accountChangeTypeCode : '',
		accountChangeAmount : '',

		receiveOrderChannelFlag : '',
		gatheringChannelDictItems : [],
		accountReceiveOrderChannelStateDictItems : [],
		accountReceiveOrderChannels : [],

	},
	computed : {},
	created : function() {
	},
	mounted : function() {
		this.loadAccountTypeItem();
		this.loadAccountStateItem();
		this.loadAccountReceiveOrderChannelStateItem();
		this.loadGatheringChannelDictItem();
		this.initTable();
	},
	methods : {

		loadAccountTypeItem : function() {
			var that = this;
			that.$http.get('/dictconfig/findDictItemInCache', {
				params : {
					dictTypeCode : 'accountType'
				}
			}).then(function(res) {
				this.accountTypeDictItems = res.body.data;
			});
		},

		loadAccountStateItem : function() {
			var that = this;
			that.$http.get('/dictconfig/findDictItemInCache', {
				params : {
					dictTypeCode : 'accountState'
				}
			}).then(function(res) {
				this.accountStateDictItems = res.body.data;
			});
		},

		loadAccountReceiveOrderChannelStateItem : function() {
			var that = this;
			that.$http.get('/dictconfig/findDictItemInCache', {
				params : {
					dictTypeCode : 'accountReceiveOrderChannelState'
				}
			}).then(function(res) {
				this.accountReceiveOrderChannelStateDictItems = res.body.data;
			});
		},

		loadGatheringChannelDictItem : function() {
			var that = this;
			that.$http.get('/gatheringChannel/findAllGatheringChannel').then(function(res) {
				that.gatheringChannelDictItems = res.body.data;
			});
		},

		initTable : function() {
			var that = this;
			$('.account-manage-table').bootstrapTable({
				classes : 'table table-hover',
				url : '/userAccount/findUserAccountDetailsInfoByPage',
				pagination : true,
				sidePagination : 'server',
				pageNumber : 1,
				pageSize : 10,
				pageList : [ 10, 25, 50, 100 ],
				sortable : true,
				sortName : 'cashDeposit',
				sortOrder : 'desc',
				queryParamsType : '',
				queryParams : function(params) {
					var condParam = {
						pageSize : params.pageSize,
						pageNum : params.pageNumber,
						propertie : params.sortName,
						direction : params.sortOrder,
						userName : that.userName,
						realName : that.realName
					};
					console.log(params);
					return condParam;
				},
				responseHandler : function(res) {
					return {
						total : res.data.total,
						rows : res.data.content
					};
				},
				detailView : true,
				detailFormatter : function(index, row, element) {
					var html = template('bank-card-info', {
						accountInfo : row
					});
					return html;
				},
				columns : [ {
					title : '?????????',
					formatter : function(value, row, index) {
						var userName = row.userName;
						if (row.accountType == 'admin') {
							return userName + '(' + row.accountTypeName + ')';
						}
						if (row.accountType == 'merchant') {
							return userName + '(' + row.accountTypeName + ')';
						}
						return userName + '(' + row.accountLevel + '???' + row.accountTypeName + ')';
					},
					cellStyle : {
						classes : 'user-name'
					}
				}, {
					field : 'realName',
					title : '????????????'
				}, {
					field : 'mobile',
					title : '?????????'
				}, {
					field : 'cashDeposit',
					title : '?????????',
					sortable : true,
					order : 'asc'
				}, {
					field : 'stateName',
					title : '????????????'
				}, {
					field : 'receiveOrderStateName',
					title : '????????????'
				}, {
					field : 'inviterUserName',
					title : '?????????'
				}, {
					field : 'registeredTime',
					title : '????????????',
					sortable : true,
					order : 'asc'
				}, {
					field : 'latelyLoginTime',
					title : '??????????????????',
				}, {
					title : '??????',
					formatter : function(value, row, index) {
						return [ '<button type="button" class="account-edit-btn btn btn-outline-primary btn-sm" style="margin-right: 4px;">??????</button>', '<button type="button" class="account-receive-order-channel-btn btn btn-outline-warning btn-sm" style="margin-right: 4px;">????????????</button>', '<button type="button" class="modify-login-pwd-btn btn btn-outline-secondary btn-sm" style="margin-right: 4px;">??????????????????</button>', '<button type="button" class="adjust-cash-deposit-btn btn btn-outline-info btn-sm" style="margin-right: 4px;">???????????????</button>', '<button type="button" class="del-account-btn btn btn-outline-danger btn-sm">??????</button>' ].join('');
					},
					events : {
						'click .account-edit-btn' : function(event, value, row, index) {
							that.showAccountEditModal(row);
						},
						'click .account-receive-order-channel-btn' : function(event, value, row, index) {
							that.showReceiveOrderChannelModal(row);
						},
						'click .modify-login-pwd-btn' : function(event, value, row, index) {
							that.showModifyLoginPwdModal(row);
						},
						'click .adjust-cash-deposit-btn' : function(event, value, row, index) {
							that.showAdjustCashDepositModal(row);
						},
						'click .del-account-btn' : function(event, value, row, index) {
							that.delAccount(row);
						}
					}
				} ]
			});
		},

		refreshTable : function() {
			$('.account-manage-table').bootstrapTable('refreshOptions', {
				pageNumber : 1
			});
		},

		showAccountRebateModal : function(row) {

		},

		showAdjustCashDepositModal : function(row) {
			this.selectedAccount = row;
			this.adjustCashDepositFlag = true;
			this.logAccountChangeLog = true;
			this.accountChangeTypeCode = '';
			this.accountChangeAmount = '';
		},

		adjustCashDeposit : function() {
			var that = this;
			if (that.accountChangeTypeCode == null || that.accountChangeTypeCode == '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (that.accountChangeAmount == null || that.accountChangeAmount == '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/userAccount/adjustCashDeposit', {
				userAccountId : that.selectedAccount.id,
				accountChangeTypeCode : that.accountChangeTypeCode,
				accountChangeAmount : that.accountChangeAmount
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('????????????!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.adjustCashDepositFlag = false;
				that.refreshTable();
			});
		},

		openAddAccountModal : function() {
			this.addUserAccountFlag = true;
			this.selectedAccount = {
				inviterUserName : header.userName,
				userName : '',
				realName : '',
				mobile : '',
				loginPwd : '',
				accountType : '',
				state : ''
			}
		},

		addUserAccount : function() {
			var that = this;
			var selectedAccount = that.selectedAccount;
			if (selectedAccount.userName === null || selectedAccount.userName === '') {
				layer.alert('??????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (selectedAccount.realName === null || selectedAccount.realName === '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (selectedAccount.mobile === null || selectedAccount.mobile === '') {
				layer.alert('??????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (selectedAccount.loginPwd === null || selectedAccount.loginPwd === '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (selectedAccount.accountType === null || selectedAccount.accountType === '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (selectedAccount.state === null || selectedAccount.state === '') {
				layer.alert('???????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/userAccount/addUserAccount', selectedAccount, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('????????????!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.addUserAccountFlag = false;
				that.refreshTable();
			});
		},

		delAccount : function(row) {
			var that = this;
			layer.confirm('????????????????????????????', {
				icon : 7,
				title : '??????'
			}, function(index) {
				layer.close(index);
				that.$http.get('/userAccount/delUserAccount', {
					params : {
						userAccountId : row.id
					}
				}).then(function(res) {
					layer.alert('????????????!', {
						icon : 1,
						time : 3000,
						shade : false
					});
					that.refreshTable();
				});
			});
		},

		showAccountEditModal : function(row) {
			var that = this;
			that.$http.get('/userAccount/findUserAccountDetailsInfoById', {
				params : {
					userAccountId : row.id
				}
			}).then(function(res) {
				that.selectedAccount = res.body.data;
				that.accountEditFlag = true;
			});
		},

		updateUserAccount : function() {
			var that = this;
			var selectedAccount = that.selectedAccount
			if (selectedAccount.userName === null || selectedAccount.userName === '') {
				layer.alert('??????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (selectedAccount.realName === null || selectedAccount.realName === '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (selectedAccount.mobile === null || selectedAccount.mobile === '') {
				layer.alert('??????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (selectedAccount.accountType === null || selectedAccount.accountType === '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (selectedAccount.state === null || selectedAccount.state === '') {
				layer.alert('???????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/userAccount/updateUserAccount', selectedAccount, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('????????????!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.accountEditFlag = false;
				that.refreshTable();
			});
		},

		showModifyLoginPwdModal : function(row) {
			this.selectedAccount = row;
			this.newLoginPwd = '';
			this.modifyLoginPwdFlag = true;
		},

		modifyLoginPwd : function() {
			var that = this;
			if (that.newLoginPwd == null || that.newLoginPwd == '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/userAccount/modifyLoginPwd', {
				userAccountId : that.selectedAccount.id,
				newLoginPwd : that.newLoginPwd
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('????????????!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.modifyLoginPwdFlag = false;
				that.refreshTable();
			});
		},

		showModifyMoneyPwdModal : function(row) {
			this.selectedAccount = row;
			this.newMoneyPwd = '';
			this.modifyMoneyPwdFlag = true;
		},

		modifyMoneyPwd : function() {
			var that = this;
			if (that.newMoneyPwd == null || that.newMoneyPwd == '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/userAccount/modifyMoneyPwd', {
				userAccountId : that.selectedAccount.id,
				newMoneyPwd : that.newMoneyPwd
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('????????????!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.modifyMoneyPwdFlag = false;
				that.refreshTable();
			});
		},

		showBindBankInfoModal : function(row) {
			var that = this;
			that.$http.get('/userAccount/getBankInfo', {
				params : {
					userAccountId : row.id
				}
			}).then(function(res) {
				that.selectedAccount = row;
				that.bankInfo = res.body.data;
				that.bindBankInfoFlag = true;
			});
		},

		bindBankInfo : function() {
			var that = this;
			var bankInfo = that.bankInfo;
			if (bankInfo.openAccountBank == null || that.openAccountBank == '') {
				layer.alert('?????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (bankInfo.accountHolder == null || that.accountHolder == '') {
				layer.alert('????????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (bankInfo.bankCardAccount == null || that.bankCardAccount == '') {
				layer.alert('????????????????????????', {
					title : '??????',
					icon : 7,
					time : 3000
				});
				return;
			}
			that.$http.post('/userAccount/bindBankInfo', {
				userAccountId : that.selectedAccount.id,
				openAccountBank : bankInfo.openAccountBank,
				accountHolder : bankInfo.accountHolder,
				bankCardAccount : bankInfo.bankCardAccount
			}, {
				emulateJSON : true
			}).then(function(res) {
				layer.alert('????????????!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.bindBankInfoFlag = false;
				that.refreshTable();
			});
		},

		showReceiveOrderChannelModal : function(row) {
			var that = this;
			that.$http.get('/userAccount/findAccountReceiveOrderChannelByAccountId', {
				params : {
					accountId : row.id
				}
			}).then(function(res) {
				that.accountReceiveOrderChannels = res.body.data;
				that.receiveOrderChannelFlag = true;
				that.selectedAccount = row;
			});
		},

		addReceiveOrderChannel : function() {
			this.accountReceiveOrderChannels.push({
				channelId : '',
				rebate : '',
				state : '1'
			});
		},

		saveAccountReceiveOrderChannel : function() {
			var that = this;
			var accountReceiveOrderChannels = that.accountReceiveOrderChannels;
			var channelMap = new Map();
			for (var i = 0; i < accountReceiveOrderChannels.length; i++) {
				var channel = accountReceiveOrderChannels[i];
				if (channel.channelId === null || channel.channelId === '') {
					layer.alert('???????????????', {
						title : '??????',
						icon : 7,
						time : 3000
					});
					return;
				}
				if (channel.rebate === null || channel.rebate === '') {
					layer.alert('???????????????', {
						title : '??????',
						icon : 7,
						time : 3000
					});
					return;
				}
				if (channel.state === null || channel.state === '') {
					layer.alert('???????????????', {
						title : '??????',
						icon : 7,
						time : 3000
					});
					return;
				}
				var key = channel.channelId;
				if (channelMap.get(key) != null) {
					layer.alert('???????????????????????????', {
						title : '??????',
						icon : 7,
						time : 3000
					});
					return;
				}
				channelMap.set(key, key);
			}
			that.$http.post('/userAccount/saveAccountReceiveOrderChannel', {
				userAccountId : that.selectedAccount.id,
				channels : accountReceiveOrderChannels
			}).then(function(res) {
				layer.alert('????????????!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.receiveOrderChannelFlag = false;
				that.refreshTable();
			});
		}

	}
});