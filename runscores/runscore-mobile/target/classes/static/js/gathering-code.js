var gatheringCodeVM = new Vue({
	el : '#gatheringCode',
	data : {
		global : GLOBAL,
		auditGatheringCode : false,
		unfixedGatheringCodeReceiveOrder : false,
		gatheringChannelId : '',
		gatheringChannelDictItems : [],
		accountTime : dayjs().format('YYYY-MM-DD'),
		gatheringCodes : [],
		pageNum : 1,
		totalPage : 1,
		showGatheringCodeFlag : true,
		editGatheringCode : {
			gatheringChannelId : '',
			state : '',
			gatheringAmount : '',
			payee : '',
			bankCode:'',
			bankAddress:'',
			bankUserame:''
		},
		showEditGatheringCodeFlag : false
	},
	computed : {
        bankCardSettingViewFlag:function(){
        	if(this.gatheringChannelId=='1149365394574671872'){
        		return true;
			}else{
        		return false;
			}
		}
	},
	created : function() {
	},
	mounted : function() {
		var that = this;
		headerVM.title = '收款码';
		headerVM.showBackFlag = true;
		that.loadReceiveOrderSetting();
		that.loadReceiveOrderChannel();

		$('.gathering-code-pic').on('fileuploaded', function(event, data, previewId, index) {
			that.editGatheringCode.storageId = data.response.data.join(',');
			that.addOrUpdateGatheringCodeInner();
		});
	},
	methods : {

		loadReceiveOrderSetting : function() {
			var that = this;
			that.$http.get('/masterControl/getReceiveOrderSetting').then(function(res) {
				that.auditGatheringCode = res.body.data.auditGatheringCode;
				that.unfixedGatheringCodeReceiveOrder = res.body.data.unfixedGatheringCodeReceiveOrder;
			});
		},

		loadReceiveOrderChannel : function() {
			var that = this;
			that.$http.get('/userAccount/findMyReceiveOrderChannel').then(function(res) {
				that.gatheringChannelDictItems = res.body.data;
				if (that.gatheringChannelDictItems.length > 0) {
					that.switchTab(that.gatheringChannelDictItems[0].channelId);
				}
			});
		},

		/**
		 * 加载收款通道字典项
		 */
		loadGatheringChannelDictItem : function() {
			var that = this;
			that.$http.get('/gatheringChannel/findAllGatheringChannel').then(function(res) {
				that.gatheringChannelDictItems = res.body.data;
				if (that.gatheringChannelDictItems.length > 0) {
					that.switchTab(that.gatheringChannelDictItems[0].id);
				}
			});
		},

		switchTab : function(gatheringChannelId) {
			this.gatheringChannelId = gatheringChannelId;
			this.query();
		},

		query : function() {
			this.pageNum = 1;
			this.loadGatheringCodeByPage();
		},

		prePage : function() {
			this.pageNum = this.pageNum - 1;
			this.loadGatheringCodeByPage();
		},

		nextPage : function() {
			this.pageNum = this.pageNum + 1;
			this.loadGatheringCodeByPage();
		},

		loadGatheringCodeByPage : function() {
			var that = this;
			that.$http.get('/gatheringCode/findMyGatheringCodeByPage', {
				params : {
					pageSize : 5,
					pageNum : that.pageNum,
					gatheringChannelId : that.gatheringChannelId
				}
			}).then(function(res) {
				that.gatheringCodes = res.body.data.content;
				that.pageNum = res.body.data.pageNum;
				that.totalPage = res.body.data.totalPage;
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

		switchGatheringAmountMode : function() {
			if (!this.editGatheringCode.fixedGatheringAmount) {
				this.editGatheringCode.gatheringAmount = '';
			}
		},

		showEditGatheringCodePage : function() {
			var that = this;
			that.editGatheringCode = {
				gatheringChannelId : that.gatheringChannelId,
				state : '',
				fixedGatheringAmount : true,
				gatheringAmount : '',
				payee : ''
			};
			that.showEditGatheringCodePageInner();
			that.initFileUploadWidget();
		},

		showEditGatheringCodePageInner : function() {
			headerVM.showBackFlag = false;
			headerVM.title = '新增收款码';
			this.showGatheringCodeFlag = false;
			this.showEditGatheringCodeFlag = true;
		},

		hideEditGatheringCodePage : function() {
			headerVM.showBackFlag = true;
			headerVM.title = '收款码';
			this.showGatheringCodeFlag = true;
			this.showEditGatheringCodeFlag = false;
		},

		addOrUpdateGatheringCode : function() {
			var that = this;
			var editGatheringCode = that.editGatheringCode;
			if (editGatheringCode.gatheringChannelId === null || editGatheringCode.gatheringChannelId === '') {
				layer.alert('请选择收款通道', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (editGatheringCode.payee == null || editGatheringCode.payee == '') {
				layer.alert('请选择收款人', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (!that.unfixedGatheringCodeReceiveOrder) {
				if (editGatheringCode.gatheringAmount == null || editGatheringCode.gatheringAmount == '') {
					layer.alert('请输入收款金额', {
						title : '提示',
						icon : 7,
						time : 3000
					});
					return;
				}
			}

			if ($('.gathering-code-pic').fileinput('getPreview').content.length != 0 || this.bankCardSettingViewFlag) {
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
			that.$http.post('/gatheringCode/addGatheringCode', that.editGatheringCode).then(function(res) {
				var msg = '操作成功!';
				if (that.auditGatheringCode) {
					msg = '操作成功,已通知系统进行审核!';
				}
				layer.alert(msg, {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.hideEditGatheringCodePage();
				that.query();
			});
		},

		delGatheringCode : function(gatheringCodeId) {
			var that = this;
			that.$http.get('/gatheringCode/delMyGatheringCodeById', {
				params : {
					id : gatheringCodeId,
				}
			}).then(function(res) {
				var msg = '操作成功!';
				if (that.auditGatheringCode) {
					msg = '操作成功,已通知系统进行审核!';
				}
				layer.alert(msg, {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.hideEditGatheringCodePage();
				that.query();
			});
		}
	}
});