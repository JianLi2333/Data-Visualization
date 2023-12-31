import Cookies from 'js-cookie'
import Config from '@/settings'

const TokenKey = Config.TokenKey

const linkTokenKey = Config.LinkTokenKey

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}

export function getLinkToken() {
  return Cookies.get(linkTokenKey)
}

export function setLinkToken(token) {
  return Cookies.set(linkTokenKey, token)
}

export function removeLinkToken() {
  return Cookies.remove(linkTokenKey)
}


// 将系统UI信息存储到cookie中，以json格式存储
export function setSysUI(uiInfo) {
  return Cookies.set('sysUiInfo', uiInfo ? JSON.stringify(uiInfo) : null)
}


// 获取系统UI信息，并以json格式返回
export function getSysUI() {
  const json = Cookies.get('sysUiInfo')
  return json ? JSON.parse(json) : null
}

