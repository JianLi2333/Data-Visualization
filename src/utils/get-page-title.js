import defaultSettings from '@/settings'
import { getSysUI } from '@/utils/auth'

let title = defaultSettings.title || 'Vue Admin Template'

// 这个函数会返回settings.js中的title，如果系统设置了ui.title，则返回ui.title的paramValue，
// 如果页面有传入pageTitle，则返回pageTitle + ' - ' + title，否则返回title。
export default function getPageTitle(pageTitle) {
  // 从cookie中将系统UI信息取出来
  const uiInfo = getSysUI()
  if (uiInfo && uiInfo['ui.title'] && uiInfo['ui.title'].paramValue) {
    title = uiInfo['ui.title'].paramValue
  }
  if (pageTitle) {
    return `${pageTitle} - ${title}`
  }
  return `${title}`
}
