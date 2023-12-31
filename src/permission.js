import router from '@/router'
import store from './store'
// import { Message } from 'element-ui'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import { getToken } from '@/utils/auth' // get token from cookie
import getPageTitle from '@/utils/get-page-title'
import { buildMenus } from '@/api/system/menu'
import { filterAsyncRouter } from '@/store/modules/permission'

// NProgress是一个进度条组件，这个就是设置在加载进度条的时候，是否显示加载动画。
NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['/login', '/401', '/404'] // no redirect whitelist


// 路由前置守卫，当路由发生变化之前，这个方法就会执行。
router.beforeEach(async(to, from, next) => {
  // start progress bar
  NProgress.start()

  // set page title
  document.title = getPageTitle(to.meta.title)

  // determine whether the user has logged in
  const hasToken = getToken()



  // 判断是否登录了
  /*---------------------------------------------------------------------------------------------*/
  
  // 登录了
  if (hasToken) {
    // 如果你都登录了还要去登录页面，那就直接跳转到首页
    if (to.path === '/login') {
      // if is logged in, redirect to the home page
      next({ path: '/' })
      NProgress.done()
    } else {
      // 从store中获取用户名字，如果有就说明已经登录了，如果没有就说明还没有登录
      const hasGetUserInfo = store.getters.name
      // console.log(hasGetUserInfo)
      if (hasGetUserInfo || to.path.indexOf('/preview/') > -1) {
        next()
        // store.dispatch方法用于store的某个mould中的一个方法，第一个参数是方法名，第二个参数是方法的参数。
        store.dispatch('permission/setCurrentPath', to.path)
        console.log(store.state)
      } else {
        if (store.getters.roles.length === 0) { // 判断当前用户是否已拉取完user_info信息
          // get user info
          store.dispatch('user/getInfo').then(() => {
            store.dispatch('lic/getLicInfo').then(() => {
              loadMenus(next, to)
            }).catch(() => {
              loadMenus(next, to)
            })
          }).catch(() => {
            store.dispatch('user/logout').then(() => {
              location.reload() // 为了重新实例化vue-router对象 避免bug
            })
          })
        } else if (store.getters.loadMenus) {
          // 修改成false，防止死循环
          store.dispatch('user/updateLoadMenus')
          store.dispatch('lic/getLicInfo').then(() => {
            loadMenus(next, to)
          }).catch(() => {
            loadMenus(next, to)
          })
        } else {
          next()
        }
      }
    }
  } else {
    /* has no token，也就是还没有登录*/


    /*---------------------------------------------------------------------------------------------*/
    /*---------------------------------------------------------------------------------------------*/ 
    
    // 这个判断是判断是否在白名单中，如果在白名单index会返回他的索引然后直接不用登录直接跳转，
    // 如果不在就会返回-1，然后调用next()，跳转到登录页面。
    if (whiteList.indexOf(to.path) !== -1) {
      // in the free login whitelist, go directly
      next()
    } else {
      console.log(to.path)
      // other pages that do not have permission to access are redirected to the login page.
      next(`/login?redirect=${to.path}`)
      // 结束进度条
      NProgress.done()
    }
  }
})



// 加载菜单
export const loadMenus = (next, to) => {
  // buildMenus方法是从后端获取菜单数据，然后根据权限过滤菜单，最后将菜单数据转换成路由数据。
  buildMenus().then(res => {
    // 根据权限过滤路由
    const filterDatas = filterRouter(res.data)
    // 组装路由
    const asyncRouter = filterAsyncRouter(filterDatas)
    // addMsgMenu(asyncRouter)
    // 404必须放到最后面
    asyncRouter.push({ path: '*', redirect: '/404', hidden: true })
    // 将路由存储到store中
    store.dispatch('permission/GenerateRoutes', asyncRouter).then(() => { // 存储路由
      // 将组装好的路由规则加入到vue实例的路由中
      router.addRoutes(asyncRouter)
      // 验证当前要跳转的路由是否在路由表中,如果不在就跳转到跟目录下
      if (pathValid(to.path, asyncRouter)) {
        next({ ...to, replace: true })
      } else {
        next('/')
      }
    })
  })
}

export const addMsgMenu = asyncRouter => {
  const menu = {
    path: 'system-msg-web',
    component: () => import('@/views/msg/index'),
    name: 'sys-msg-web',
    meta: { title: '站内消息', icon: 'all-msg' },
    children: [
      {
        path: 'all',
        component: () => import('@/views/msg/all'),
        name: 'sys-msg-web-all',
        meta: { title: '所有消息', icon: 'web-msg' }
      },
      {
        path: 'unread',
        component: () => import('@/views/msg/unread'),
        name: 'sys-msg-web-unread',
        meta: { title: '未读消息', icon: 'unread-msg' }

      },
      {
        path: 'readed',
        component: () => import('@/views/msg/readed'),
        name: 'sys-msg-web-readed',
        meta: { title: '已读消息', icon: 'readed-msg' }
      }
    ]
  }
  asyncRouter.forEach(element => {
    if (element.name === 'system') {
      if (element.children) {
        element.children.splice(0, 0, menu)
      }
      // element.children.push(menu)
    }
  })
}
/**
 * 验证path是否有效
 * @param {*} path
 * @param {*} routers
 * @returns
 */
const pathValid = (path, routers) => {
  const temp = path.startsWith('/') ? path.substr(1) : path
  const locations = temp.split('/')
  if (locations.length === 0) {
    return false
  }

  // 验证是否有当前路由
  return hasCurrentRouter(locations, routers, 0)
}
/**
 * 递归验证every level
 * @param {*} locations
 * @param {*} routers
 * @param {*} index
 * @returns
 */
const hasCurrentRouter = (locations, routers, index) => {
  const location = locations[index]
  let kids = []
  const isvalid = routers.some(router => {
    kids = router.children
    return (router.path === location || ('/' + location) === router.path)
  })
  if (isvalid && index < locations.length - 1) {
    return hasCurrentRouter(locations, kids, index + 1)
  }
  return isvalid
}



// 根据权限过滤菜单
const filterRouter = routers => {
  // 在store中的permissions在获取用户信息的时候注进去了
  const user_permissions = store.getters.permissions
  // if (!user_permissions || user_permissions.length === 0) {
  //   return routers
  // }

  // 这里会对菜单进行过滤，如果菜单要求权限，但是当前用户没有权限，那就会把这个菜单移除。
  const tempResults = routers.filter(router => hasPermission(router, user_permissions))
  // 如果是一级菜单(目录) 没有字菜单 那就移除
  return tempResults.filter(item => {
    if (item.type === 0 && (!item.children || item.children.length === 0)) {
      return false
    }
    return true
  })
}


// 判断是否有权限
const hasPermission = (router, user_permissions) => {
  // 菜单要求权限 但是当前用户权限没有包含菜单权限
  if (router.permission && !user_permissions.includes(router.permission)) {
    return false
  }

  if (!filterLic(router)) {
    return false
  }
  // 如果有字菜单 则 判断是否满足 ‘任意一个子菜单有权限’
  if (router.children && router.children.length) {
    const permissionChilds = router.children.filter(item => hasPermission(item, user_permissions))
    router.children = permissionChilds
    return router.children.length > 0
  }
  return true
}

// 判断是不是插件或者是否有许可证
const filterLic = (router) => {
  return !router.isPlugin || store.getters.validate
}
router.afterEach(() => {
  // finish progress bar
  NProgress.done()
})
