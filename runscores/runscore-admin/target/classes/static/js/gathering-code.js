var gatheringCodeVM = new Vue({
	el : '#gathering-code',
	data : {
		global : GLOBAL,
		state : '',
		gatheringCodeStateDictItems : [],
		gatheringChannelId : '',
		gatheringChannelDictItems : [],
		payee : '',
		userName : '',

		addOrUpdateGatheringCodeFlag : false,
		gatheringCodeActionTitle : '',
		editGatheringCode : {},

		approvalFlag : false,
	},
	computed : {},
	created : function() {
	},
	mounted : function() {
		var that = this;
		var state = getQueryString('state');
		if (state == '2') {
			that.state = state;
		}
		var todoId = getQueryString('todoId');
		if (todoId != null) {
			that.showAuditGatheringCodeModal(todoId);
		}

		that.loadGatheringCodeStateDictItem();
		that.loadGatheringChannelDictItem();
		that.initTable();

		$('.gathering-code-pic').on('fileuploaded', function(event, data, previewId, index) {
			that.editGatheringCode.storageId = data.response.data.join(',');
			that.addOrUpdateGatheringCodeInner();
		});
	},
	methods : {

		loadGatheringCodeStateDictItem : function() {
			var that = this;
			that.$http.get('/dictconfig/findDictItemInCache', {
				params : {
					dictTypeCode : 'gatheringCodeState'
				}
			}).then(function(res) {
				this.gatheringCodeStateDictItems = res.body.data;
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
			$('.gathering-code-table').bootstrapTable({
				classes : 'table table-hover',
				url : '/gatheringCode/findGatheringCodeByPage',
				pagination : true,
				sidePagination : 'server',
				pageNumber : 1,
				pageSize : 10,
				pageList : [ 10, 25, 50, 100 ],
				queryParamsType : '',
				queryParams : function(params) {
					var condParam = {
						pageSize : params.pageSize,
						pageNum : params.pageNumber,
						state : that.state,
						gatheringChannelId : that.gatheringChannelId,
						payee : that.payee,
						userName : that.userName
					};
					return condParam;
				},
				responseHandler : function(res) {
					return {
						total : res.data.total,
						rows : res.data.content
					};
				},
				columns : [ {
					title : '状态',
					formatter : function(value, row, index, field) {
						if (row.state == '1') {
							return row.stateName;
						}
						return row.auditTypeName + '-' + row.stateName;
					}
				}, {
					title : '通道/收款金额/收款人/所属账号',
					formatter : function(value, row, index, field) {
						var text = '';
						if (row.fixedGatheringAmount) {
							text = row.gatheringAmount;
						} else {
							text = '不固定'
						}
						return row.gatheringChannelName + '/' + text + '/' + row.payee + '/' + row.userName;
					}
				}, {
					field : 'createTime',
					title : '创建时间'
				}, {
					title : '累计收款金额/收款次数/接单次数/成功率',
					formatter : function(value, row, index, field) {
						return row.totalTradeAmount + that.global.systemSetting.currencyUnit + '/' + row.totalPaidOrderNum + '次' + '/' + row.totalOrderNum + '次' + '/' + row.totalSuccessRate + '%';
					}
				}, {
					title : '今日收款金额/收款次数/接单次数/成功率',
					formatter : function(value, row, index, field) {
						return row.todayTradeAmount + that.global.systemSetting.currencyUnit + '/' + row.todayPaidOrderNum + '次' + '/' + row.todayOrderNum + '次' + '/' + row.todaySuccessRate + '%';
					}
				}, {
					title : '操作',
					formatter : function(value, row, index) {
						var btns = [ '<button type="button" class="view-gathering-code-btn btn btn-outline-secondary btn-sm" style="margin-right: 4px;">查看收款码</button>', '<button type="button" class="edit-gathering-code-btn btn btn-outline-success btn-sm" style="margin-right: 4px;">编辑</button>', '<button type="button" class="del-gathering-code-btn btn btn-outline-danger btn-sm">删除</button>' ];
						if (row.state == '2') {
							btns.unshift('<button type="button" class="audit-gathering-code-btn btn btn-outline-info btn-sm" style="margin-right: 4px;">审核</button>');
						}
						return btns.join('');
					},
					events : {
						'click .audit-gathering-code-btn' : function(event, value, row, index) {
							that.showAuditGatheringCodeModal(row.id);
						},
						'click .view-gathering-code-btn' : function(event, value, row, index) {
							if(row.gatheringChannelId=='1149365394574671872'){
								that.viewBankInfo(row);
							}else{
                               that.viewImage('/storage/fetch/' + row.storageId);
							}

						},
						'click .edit-gathering-code-btn' : function(event, value, row, index) {
							that.openEditGatheringCodeModal(row.id);
						},
						'click .del-gathering-code-btn' : function(event, value, row, index) {
							that.delGatheringCode(row.id);
						}
					}
				} ]
			});
		},

		refreshTable : function() {
			$('.gathering-code-table').bootstrapTable('refreshOptions', {
				pageNumber : 1
			});
		},

		showAuditGatheringCodeModal : function(id) {
			var that = this;
			that.$http.get('/gatheringCode/findGatheringCodeById', {
				params : {
					id : id,
				}
			}).then(function(res) {
				that.approvalFlag = true;
				that.editGatheringCode = res.body.data;
			});
		},

		auditDel : function(id) {
			this.approvalFlag = false;
			this.delGatheringCode(this.editGatheringCode.id);
		},

		updateToNormalState : function() {
			var that = this;
			that.$http.get('/gatheringCode/updateToNormalState', {
				params : {
					id : that.editGatheringCode.id
				}
			}).then(function(res) {
				that.approvalFlag = false;
				that.refreshTable();
			});
		},

		initFileUploadWidget : function(storageId) {
			var initialPreview = [];
			var initialPreviewConfig = [];
			if (storageId != null) {
				initialPreview.push('/storage/fetch/' + storageId);
				initialPreviewConfig.push({
					downloadUrl : '/storage/fetch/' + storageId
				});
			}
			$('.gathering-code-pic').fileinput('destroy').fileinput({
				browseOnZoneClick : true,
				showBrowse : false,
				showCaption : false,
				showClose : true,
				showRemove : false,
				showUpload : false,
				dropZoneTitle : '点击选择图片',
				dropZoneClickTitle : '',
				layoutTemplates : {
					footer : ''
				},
				maxFileCount : 1,
				uploadUrl : '/storage/uploadPic',
				enctype : 'multipart/form-data',
				allowedFileExtensions : [ 'jpg', 'png', 'bmp', 'jpeg' ],
				initialPreview : initialPreview,
				initialPreviewAsData : true,
				initialPreviewConfig : initialPreviewConfig
			});
		},

		viewImage : function(imagePath) {
			var image = new Image();
			image.src = imagePath;
			var viewer = new Viewer(image, {
				hidden : function() {
					viewer.destroy();
				},
			});
			viewer.show();
		},
        viewBankInfo(row){
            layer.open({
				title:'银行卡信息',
                type: 1,
                skin: 'layui-layer-demo', //样式类名
                closeBtn: 0, //不显示关闭按钮
                anim: 2,
                area: ['50%', '50%'],
                shadeClose: true, //开启遮罩关闭
                content: '<table class="table common-table" >\n' +
                '\t\t\t\t\t\t\t\t\t<thead>\n' +
                '\t\t\t\t\t\t\t\t\t<tr>\n' +
                '\t\t\t\t\t\t\t\t\t\t<th>银行卡号</th>\n' +
                '\t\t\t\t\t\t\t\t\t\t<th>银行开户行</th>\n' +
                '\t\t\t\t\t\t\t\t\t\t<th>卡户主</th>\n' +
                '\t\t\t\t\t\t\t\t\t</tr>\n' +
                '\t\t\t\t\t\t\t\t\t</thead>\n' +
                '\t\t\t\t\t\t\t\t\t<tbody>\n' +
                '\t\t\t\t\t\t\t\t\t<tr>\n' +
                '\t\t\t\t\t\t\t\t\t\t<td>'+row.bankCode+'</td>\n' +
                '\t\t\t\t\t\t\t\t\t\t<td>'+row.bankAddress+'</td>\n' +
                '\t\t\t\t\t\t\t\t\t\t<td>'+row.bankUsername+'</td>\n' +
                '\t\t\t\t\t\t\t\t\t</tr>\n' +
                '\t\t\t\t\t\t\t\t\t</tbody>\n' +
                '\t\t\t\t\t\t\t\t</table>'
            });
		},
		openAddGatheringCodeModal : function() {
			this.addOrUpdateGatheringCodeFlag = true;
			this.gatheringCodeActionTitle = '新增收款码';
			this.editGatheringCode = {
				userName : '',
				gatheringChannelId : '',
				state : '',
				fixedGatheringAmount : true,
				gatheringAmount : '',
				payee : ''
			};
			this.initFileUploadWidget();
		},

		openEditGatheringCodeModal : function(gatheringCodeId) {
			var that = this;
			that.$http.get('/gatheringCode/findGatheringCodeById', {
				params : {
					id : gatheringCodeId,
				}
			}).then(function(res) {
				that.addOrUpdateGatheringCodeFlag = true;
				that.gatheringCodeActionTitle = '编辑收款码';
				that.editGatheringCode = res.body.data;
				that.initFileUploadWidget(res.body.data.storageId);
			});
		},

		addOrUpdateGatheringCode : function() {
			var that = this;
			var editGatheringCode = that.editGatheringCode;
			if (editGatheringCode.userName == null || editGatheringCode.userName == '') {
				layer.alert('请输入所属账号', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (editGatheringCode.gatheringChannelId === null || editGatheringCode.gatheringChannelId === '') {
				layer.alert('请选择收款通道', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (editGatheringCode.payee == null || editGatheringCode.payee == '') {
				layer.alert('请输入收款人', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (!that.global.receiveOrderSetting.unfixedGatheringCodeReceiveOrder) {
				if (editGatheringCode.gatheringAmount == null || editGatheringCode.gatheringAmount == '') {
					layer.alert('请输入收款金额', {
						title : '提示',
						icon : 7,
						time : 3000
					});
					return;
				}
			}

			if ($('.gathering-code-pic').fileinput('getPreview').content.length != 0) {
				that.addOrUpdateGatheringCodeInner();
			} else {
				var filesCount = $('.gathering-code-pic').fileinput('getFilesCount');
				if (filesCount == 0) {
					layer.alert('请选择要上传的图片', {
						title : '提示',
						icon : 7,
						time : 3000
					});
					return;
				}
				$('.gathering-code-pic').fileinput('upload');
			}
		},

		addOrUpdateGatheringCodeInner : function() {
			var that = this;
			that.$http.post('/gatheringCode/addOrUpdateGatheringCode', that.editGatheringCode).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.addOrUpdateGatheringCodeFlag = false;
				that.refreshTable();
			});
		},

		delGatheringCode : function(gatheringCodeId) {
			var that = this;
			that.$http.get('/gatheringCode/delGatheringCodeById', {
				params : {
					id : gatheringCodeId,
				}
			}).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.addOrUpdateGatheringCodeFlag = false;
				that.refreshTable();
			});
		}
	}
});