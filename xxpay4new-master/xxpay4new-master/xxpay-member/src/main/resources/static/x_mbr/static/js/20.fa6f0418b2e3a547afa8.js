webpackJsonp([20],{QWGK:function(t,i){},vMmA:function(t,i,s){"use strict";Object.defineProperty(i,"__esModule",{value:!0});var a=s("CJJO"),n=(s("s0MJ").dateUtils,{data:function(){return{htmlProportion:"",htmlWidth:"",htmlheight:"",background:"#FFFFFF",color:"#FF9000",couponNo:"",remarkShow:!1,couponCodeImgUrl:"",couponBarCodeImgUrl:"",coupon:{}}},created:function(){this.htmlWidth=document.documentElement.clientWidth||document.body.clientWidth,this.htmlheight=document.documentElement.clientHeight||document.body.clientHeight,this.htmlProportion=this.htmlWidth/750,this.couponNo=this.$route.query.couponNo,this.couponDetail(),this.getCode()},methods:{couponDetail:function(){var t=this;Object(a.m)(t.couponNo).then(function(i){t.coupon=i.data,t.coupon.payAmountLimit="满"+i.data.payAmountLimit/100+"减"+i.data.couponAmount/100,t.coupon.couponAmount="￥"+i.data.couponAmount/100,t.coupon.validateEnd=i.data.validateEnd,t.coupon.singleUserLimit=-1==i.data.singleUserLimit?"无限制":i.data.singleUserLimit+"张",t.coupon.storeLimitType=0==i.data.storeLimitType?"不限门店":i.data.storeNames,t.remarkShow=!!i.data.remark}).catch(function(t){console.log(t)})},getCode:function(){this.couponCodeImgUrl=a.c+"/payment/code?codeNo="+this.couponNo,this.couponBarCodeImgUrl=a.c+"/payment/barCode?barNo="+this.couponNo},jump:function(){this.$router.go(-1)}}}),o={render:function(){var t=this,i=t.$createElement,s=t._self._c||i;return s("div",{staticClass:"center",style:{height:t.htmlheight+"px"}},[s("mu-container",{staticStyle:{background:"#FFFFFF","padding-bottom":"11px"}},[s("div",{staticClass:"coupon-item"},[s("div",{staticClass:"coupon-detail"},[s("div",{staticClass:"coupon-money"},[s("span",{staticClass:"text-black text-B"},[t._v(t._s(t.coupon.payAmountLimit))])]),t._v(" "),s("div",{staticClass:"coupon-endTime"},[t._v("使用期限:"+t._s(t.coupon.validateEnd))])]),t._v(" "),s("div",{staticClass:"coupon-get",staticStyle:{"margin-top":"0"}},[s("span",{staticClass:"text-B text-mid-big"},[t._v(t._s(t.coupon.couponAmount))])])])]),t._v(" "),s("div",{staticClass:"uni-list margin-top"},[s("div",{staticClass:"uni-media-list"},[s("div",{staticClass:"uni-media-list-body"},[s("span",[t._v("核销码")]),t._v(" "),s("span",[t._v(t._s(t.couponNo))])])]),t._v(" "),s("div",{staticClass:"uni-media-list"},[s("div",{staticClass:"uni-media-list-body",staticStyle:{"justify-content":"center"}},[s("img",{style:{height:"100px",width:630*t.htmlProportion+"px"},attrs:{src:t.couponBarCodeImgUrl}})])]),t._v(" "),s("div",{staticClass:"uni-media-list"},[s("div",{staticClass:"uni-media-list-body",staticStyle:{"justify-content":"center"}},[s("img",{staticStyle:{width:"200px",height:"200px"},attrs:{src:t.couponCodeImgUrl}})])])]),t._v(" "),s("div",{staticClass:"uni-list margin-top"},[t._m(0),t._v(" "),s("div",{staticClass:"uni-media-list"},[s("div",{staticClass:"uni-media-list-body"},[s("span",{staticClass:"text-gray"},[t._v("截止日期:")]),t._v(" "),s("span",{staticClass:"margin-left-20"},[t._v(t._s(t.coupon.validateEnd))])])]),t._v(" "),s("div",{staticClass:"uni-media-list"},[s("div",{staticClass:"uni-media-list-body"},[s("span",{staticClass:"text-gray"},[t._v("每人限领:")]),t._v(" "),s("span",{staticClass:"margin-left-20"},[t._v(t._s(t.coupon.singleUserLimit))])])]),t._v(" "),s("div",{staticClass:"uni-media-list"},[s("div",{staticClass:"uni-media-list-body"},[s("span",{staticClass:"text-gray"},[t._v("使用时段:")]),t._v(" "),s("span",{staticClass:"margin-left-20"},[t._v(t._s(t.coupon.useTimeConfig))])])])]),t._v(" "),s("div",{staticClass:"uni-list margin-top"},[t._m(1),t._v(" "),s("div",{staticClass:"uni-media-list"},[s("div",{staticClass:"uni-media-list-body"},[s("span",{staticClass:"text-gray"},[t._v(t._s(t.coupon.storeLimitType))])])])]),t._v(" "),t.remarkShow?s("div",{staticClass:"uni-list margin-top"},[t._m(2),t._v(" "),s("div",{staticClass:"uni-media-list"},[s("div",{staticClass:"uni-media-list-body"},[s("span",{staticClass:"text-gray"},[t._v(t._s(t.coupon.remark))])])])]):t._e(),t._v(" "),s("mu-flex",{staticClass:"bg-white",attrs:{"justify-content":"center"}},[s("mu-button",{staticStyle:{width:"80%","margin-top":"20px","margin-bottom":"20px"},attrs:{round:"",color:"#FF9000"},on:{click:function(i){return t.jump()}}},[t._v("返回")])],1)],1)},staticRenderFns:[function(){var t=this.$createElement,i=this._self._c||t;return i("div",{staticClass:"uni-media-list"},[i("div",{staticClass:"uni-media-list-body"},[i("span",{staticClass:"text-mid"},[this._v("使用须知")])])])},function(){var t=this.$createElement,i=this._self._c||t;return i("div",{staticClass:"uni-media-list"},[i("div",{staticClass:"uni-media-list-body"},[i("span",{staticClass:"text-mid"},[this._v("适用门店")])])])},function(){var t=this.$createElement,i=this._self._c||t;return i("div",{staticClass:"uni-media-list"},[i("div",{staticClass:"uni-media-list-body"},[i("span",{staticClass:"text-mid"},[this._v("卡券说明")])])])}]};var e=s("C7Lr")(n,o,!1,function(t){s("QWGK")},null,null);i.default=e.exports}});