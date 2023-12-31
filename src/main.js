import Vue from 'vue'
import Cookies from 'js-cookie'
import '@/styles/index.scss' // global css
import ElementUI from 'element-ui'
import Fit2CloudUI from 'fit2cloud-ui'
// import axios from 'axios'
// import VueAxios from 'vue-axios'
import i18n from './lang' // internationalization
import App from './App'
import store from './store'
import router from './router'
import message from './utils/message'
import '@/icons' // icon
import '@/permission' // permission control
import api from '@/api/index.js'
import filter from '@/filter/filter'
import directives from './directive'
import VueClipboard from 'vue-clipboard2'
import widgets from '@/components/widget'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import './utils/dialog'
import DeComplexInput from '@/components/business/condition-table/DeComplexInput'
import DeComplexSelect from '@/components/business/condition-table/DeComplexSelect'
import '@/components/canvas/custom-component' // 注册自定义组件
Vue.config.productionTip = false
Vue.use(VueClipboard)
Vue.use(widgets)
//在vue原型上挂载api，便于全局使用，不用每个页面都引入api，直接this.$api就可以使用。
Vue.prototype.$api = api

import * as echarts from 'echarts'

Vue.prototype.$echarts = echarts

import UmyUi from 'umy-ui'
Vue.use(UmyUi)

import vcolorpicker from 'vcolorpicker'
Vue.use(vcolorpicker)

// 全屏插件
import fullscreen from 'vue-fullscreen'
Vue.use(fullscreen)

// import TEditor from '@/components/Tinymce/index.vue'
// Vue.component('TEditor', TEditor)

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online ! ! !
 */
if (process.env.NODE_ENV === 'production') {
//   const { mockXHR } = require('../mock')
//   mockXHR()
}

// set ElementUI lang to EN
// Vue.use(ElementUI, { locale })
// 如果想要中文版 element-ui，按如下方式声明
Vue.use(ElementUI, {
  size: Cookies.get('size') || 'medium', // set element-ui default size
  i18n: (key, value) => i18n.t(key, value)
})
Vue.use(Fit2CloudUI, {
  i18n: (key, value) => i18n.t(key, value)
})
// Vue.use(VueAxios, axios)
Vue.use(filter)
Vue.use(directives)
Vue.use(message)
// 在vue中全局注册组件，便于使用，不用每个页面都引入组件，直接<treeselect>就可以使用。
Vue.component('Treeselect', Treeselect)
Vue.component('DeComplexInput', DeComplexInput)
Vue.component('DeComplexSelect', DeComplexSelect)
Vue.config.productionTip = false

// 在vue原型上添加一个hasDataPermission方法，便于全局使用，
// 不用每个页面都引入hasDataPermission方法，直接this.hasDataPermission就可以使用。
// 这个hasDataPermission有两个参数，第一个参数是要判断的权限，第二个参数是当前用户的权限。
Vue.prototype.hasDataPermission = function(pTarget, pSource) {
  // this.$store是vuex的store，在store文件夹中的index.js中定义的。
  if (this.$store.state.user.user.isAdmin) {
    console.log('admin')
    console.log(this.$store.state)
    console.log(Co)
    return true
  }
  //如果不是管理员就会对比权限，看当先用户的权限是否足够。
  if (pSource && pTarget) {
    return pSource.indexOf(pTarget) > -1
  }
  return false
}

/**
 * 就是一个判断权限的方法，传入一个数组，里面是要判断的权限，返回true或者false
 * @param {*} pers 是一个数组，里面是要判断的权限
 * @returns 
 */
Vue.prototype.checkPermission = function(pers) {
  // 从vuex store中获取当前用户的所有权限
  const permissions = store.getters.permissions
  // 使用every方法，遍历pers数组，如果每一项都在permissions中，就返回true，否则返回false
  const hasPermission = pers.every(needP => {
    const result = permissions.includes(needP)
    return result
  })
  return hasPermission
}


// 创建一个vue实例，挂载到#app上，这个#app在public/index.html中定义的。
// 这个实例中包含了路由、vuex、i18n等。
new Vue({

  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')
