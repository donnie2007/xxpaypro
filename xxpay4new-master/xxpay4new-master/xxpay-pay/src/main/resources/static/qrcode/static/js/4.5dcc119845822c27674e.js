webpackJsonp([4],{LEdV:function(t,e){},tKjj:function(t,e,a){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var o=a("CJJO"),i={components:{Keyboard:a("1WYs").a},data:function(){return{htmlProportion:"",htmlWidth:"",htmlheight:"",yenSymbol:"¥",isAllowModifyAmount:!0,keyboardOption:{show:!1,clickShowDom:"",value:""},payToken:"",storeId:"",operatorId:"",tradeOrderId:"",merchantName:" -- ",showMask:!1,remark:"",headSculpture:a("e6AH")}},mounted:function(){this.judge_shuru()},created:function(){var t=this;this.htmlWidth=document.documentElement.clientWidth||document.body.clientWidth,this.htmlheight=document.documentElement.clientHeight||document.body.clientHeight,this.htmlProportion=this.htmlWidth/750;var e=this.$route.query.alipayUserId;if(!e)return alert("参数有误");this.alipayUserId=e;var a=this.$route.query.mchId;if(!a)return alert("参数有误");this.mchId=a;var i=this.$route.query.storeId;i&&(this.storeId=i);var s=this.$route.query.operatorId;s&&(this.operatorId=s);var r=this.$route.query.payAmount;r?this.keyboardOption.value=r/100:this.isAllowModifyAmount=!0;var n=this.$route.query.tradeOrderId;n&&(this.tradeOrderId=n);var d=this.$route.query.payToken;if(!d)return alert("参数有误");this.payToken=d,Object(o.d)(this.payToken,this.mchId,this.storeId,this.operatorId).then(function(e){t.merchantName=e.data.mchName}).catch(function(t){})},methods:{showKeyboard:function(){this.keyboardOption.clickShowDom=this.$refs.keyboard,this.keyboardOption.show=!0},judge_shuru:function(){this.isAllowModifyAmount&&(this.showKeyboard(),this.money_shuru=!0)},payment:function(){var t=this;this.showMask=!0;var e=this;if(!this.keyboardOption.value)return this.showMask=!1,alert("请输入正确的金额！");var a=(100*this.keyboardOption.value).toFixed(0);if(a<=0)return this.showMask=!1,alert("请输入正确的金额！");Object(o.b)(this.alipayUserId,a,this.storeId,this.operatorId,this.mchId,this.tradeOrderId,this.payToken).then(function(t){var a=JSON.parse(t.data.payParam).alipayTradeNo;window.AlipayJSBridge?e.doAlipay(a,t.data):document.addEventListener("AlipayJSBridgeReady",function(){e.doAlipay(a,t.data)},!1)}).catch(function(e){t.showMask=!1})},doAlipay:function(t,e){var a=this;AlipayJSBridge.call("tradePay",{tradeNO:t},function(t){"9000"==t.resultCode?a.$router.push({path:"/paySuccess",query:{orderAmount:e.orderAmount,discountAmount:e.discountAmount,amount:e.amount,tradeOrderId:e.tradeOrderId,payType:2}}):a.showMask=!1})},toPayFunc:function(){var t=this;this.$nextTick(function(){t.payment()})}}},s={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticStyle:{background:"rgba(242, 242, 242, 1)"}},[a("div",{staticClass:"lf-store-name"},[a("div",{staticClass:"lf-content-payPanel"},[a("div",[a("div",{staticClass:"one"},[a("span",{},[t._v("付款给 "),a("span",{domProps:{textContent:t._s(this.merchantName)}})]),t._v(" "),a("div",[a("img",{staticClass:"headSculpture",attrs:{src:t.headSculpture,alt:""}})])])])])]),t._v(" "),a("div",{staticClass:"bottom",staticStyle:{height:"37rem"}},[a("div",{staticClass:"jiner"},[a("div",{staticClass:"text"},[t._v("金额")]),t._v(" "),a("div",{staticClass:"money"},[a("span",{staticClass:"money_fuhao"},[t._v("￥")]),t._v(" "),t.isAllowModifyAmount?a("span",{ref:"keyboard",staticClass:"text-lg pay-price-input",domProps:{textContent:t._s(t.keyboardOption.value)},on:{click:t.showKeyboard}}):a("span",{staticClass:"text-lg pay-price-input",domProps:{textContent:t._s(t.keyboardOption.value)}}),t._v(" "),a("Keyboard",{attrs:{keyboardOption:t.keyboardOption,toPayFunc:t.toPayFunc}})],1)]),t._v(" "),0==t.keyboardOption.show?a("div",{staticClass:"fukuan"},[a("div",{staticClass:"fukuan_text",on:{click:t.payment}},[a("span",{staticClass:"zhifu-text1"},[t._v("付款"+t._s(t.keyboardOption.value)+"元")])])]):t._e()])])},staticRenderFns:[]};var r=a("VU/8")(i,s,!1,function(t){a("LEdV")},"data-v-34f49209",null);e.default=r.exports}});
//# sourceMappingURL=4.5dcc119845822c27674e.js.map