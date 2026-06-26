import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const roleKey = ref(localStorage.getItem('roleKey') || '')

  function setLogin(data) {
    token.value = data.token
    roleKey.value = data.roleKey
    if (data.roleKey === 'reader') {
      userInfo.value = data.reader || {}
    } else {
      userInfo.value = data.admin || {}
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('roleKey', data.roleKey)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    roleKey.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('roleKey')
    localStorage.removeItem('userInfo')
  }

  function isAdmin() {
    return roleKey.value && roleKey.value !== 'reader'
  }

  function isReader() {
    return roleKey.value === 'reader'
  }

  return { token, userInfo, roleKey, setLogin, logout, isAdmin, isReader }
})
