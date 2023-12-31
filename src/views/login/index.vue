<template>
  <div class="login-background">
    <div class="login-container">
      <!-- 这是一个element的一个行布局组件 -->
      <el-row v-loading="loading" type="flex">
        <!-- 这是一个element的一个列布局组件 -->
        <el-col :span="12">
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" size="default">
            <div class="login-logo">
              <!-- 这里是根据是否获取到数据库中的登录表单logo的地址，来确认logo -->
              <svg-icon v-if="!loginLogoUrl && axiosFinished" icon-class="DataEase" custom-class="login-logo-icon" />
              <img v-if="loginLogoUrl && axiosFinished" :src="loginLogoUrl" alt="">
            </div>
            <div v-if="uiInfo && uiInfo['ui.loginTitle'] && uiInfo['ui.loginTitle'].paramValue" class="login-welcome">
              {{ uiInfo['ui.loginTitle'].paramValue }}
            </div>
            <div v-else class="login-welcome">
              {{ $t('login.welcome') + (uiInfo && uiInfo['ui.title'] && uiInfo['ui.title'].paramValue || ' DataEase') }}
            </div>
            <div class="login-form">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" placeholder="ID" autofocus />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  :placeholder="$t('login.password')"
                  show-password
                  maxlength="30"
                  show-word-limit
                  autocomplete="new-password"
                  @keypress.enter.native="handleLogin"
                />
              </el-form-item>
            </div>
            <div class="login-btn">
              <el-button type="primary" class="submit" size="default" @click.native.prevent="handleLogin">
                {{ $t('commons.login') }}
              </el-button>
              <!-- 显示登录账号demo -->
              <div v-if="uiInfo && uiInfo['ui.demo.tips'] && uiInfo['ui.demo.tips'].paramValue" class="demo-tips">
                {{ uiInfo['ui.demo.tips'].paramValue }}
              </div>
            </div>
            <div class="login-msg">
              {{ msg }}
            </div>
          </el-form>
        </el-col>
        <el-col v-loading="!axiosFinished" :span="12">
          <!-- 默认图片在style中定义了 -->
          <div v-if="!loginImageUrl && axiosFinished" class="login-image" />
          <div v-if="loginImageUrl && axiosFinished" class="login-image-de" :style="{background:'url(' + loginImageUrl + ') no-repeat', 'backgroundSize':'contain'}" />   
          <!-- <div v-if="loginImageUrl && axiosFinished" class="login-image-de" :style="{background:'url(loginImageUrl) no-repeat', 'backgroundSize':'contain'}" />              -->
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>

import { encrypt } from '@/utils/rsaEncrypt'
// import { validateUserName } from '@/api/user'
import { getSysUI } from '@/utils/auth'
export default {
  name: 'Login',
  data() {
    // const validateUsername = (rule, value, callback) => {
    //   const userName = value.trim()
    //   validateUserName({ userName: userName }).then(res => {
    //     if (res.data) {
    //       callback()
    //     } else {
    //       callback(this.$t('login.username_error'))
    //     }
    //   }).catch(() => {
    //     callback(this.$t('login.username_error'))
    //   })
    // //   if (!validUsername(value)) {
    // //     callback(new Error('Please enter the correct user name'))
    // //   } else {
    // //     callback()
    // //   }
    // }
    // const validatePassword = (rule, value, callback) => {
    //   if (value.length < 8) {
    //     callback(this.$t('login.password_error'))
    //   } else {
    //     callback()
    //   }
    // }
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', message: this.$t('commons.input_id') }],
        password: [{ required: true, trigger: 'blur', message: this.$t('commons.input_pwd') }]
      },
      loading: false,
      passwordType: 'password',
      redirect: undefined,
      uiInfo: null,
      loginImageUrl: null,
      loginLogoUrl: null,
      axiosFinished: false
    }
  },
  computed:             {
    msg() {
      // 根据store中的user组件中的loginMsg来显示登录信息
      return this.$store.state.user.loginMsg
    }
  },
  // 监听路由变化，如果有redirect参数，则将其赋值给redirect
  watch: {
    $route: {
      handler: function(route) {
        // 将redirect参数赋值给redirect，如果没有redirect参数，则redirect为undefined
        this.redirect = route.query && route.query.redirect
        console.log(this.redirect)
      },
      // 该属性表示是否立即触发调用处理函数，就是当组件加载完成后，就会调用handler函数，而不是等到$route变化后才调用handler函数
      immediate: true
    }
  },

  // 在页面加载时，获取系统配置信息
  created() {
    // 调用vuex中的getUI方法，获取系统配置信息，里面会将获取到的ui信息存在cookie中
    this.$store.dispatch('user/getUI').then(() => {
      // const uiLists = this.$store.state.user.uiInfo
      // this.uiInfo = format(uiLists)
      this.axiosFinished = true
      this.showLoginImage()
      console.log(this.loginImageUrl)
    }).catch(err => {
      console.error(err)
    })
  },
  methods: {
    // 显示登录页面的图片
    showLoginImage() {
      // 调用utils/auth.js中的getSysUI方法，从cookie中获取系统配置信息
      this.uiInfo = getSysUI()
      // 如果uiInfo中有ui.loginImage和ui.loginLogo的配置信息，则将图片显示出来
      if (this.uiInfo['ui.loginImage'] && this.uiInfo['ui.loginImage'].paramValue) {
        // this.loginImageUrl = '/system/ui/image/' + this.uiInfo['ui.loginImage'].paramValue
        this.loginImageUrl = this.uiInfo['ui.loginImage'].paramValue
      }
      // 如果uiInfo中有ui.loginLogo的配置信息，则将图片显示出来
      if (this.uiInfo['ui.loginLogo'] && this.uiInfo['ui.loginLogo'].paramValue) {
        this.loginLogoUrl = '/system/ui/image/' + this.uiInfo['ui.loginLogo'].paramValue
      }
    },

    // 登录
    handleLogin() {
      // $refs是一个对象，它存储了组件中所有的ref注册的DOM元素和组件实例。
      // loginForme已经被注册进refs中了，validate是el-form的方法，用于校验表单。
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          // 
          this.loading = true
          const user = {
            username: this.loginForm.username,
            password: this.loginForm.password
          }
          // 使用公钥对密码进行加密
          user.password = encrypt(user.password)
          // 调用vuex中的login方法，dispatch用于分发action
          this.$store.dispatch('user/login', user).then(() => {
            // 登录成功后，如果redirect有值，则跳转到redirect指定的页面，否则跳转到首页
            this.$router.push({ path: this.redirect || '/' })
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "../../styles/variables";

@mixin login-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-background {
  background-color: $--background-color-base;
  height: 100vh;
  @include login-center;
}

.login-container {
  min-width: 900px;
  width: 1280px;
  height: 520px;
  background-color: #FFFFFF;
  @media only screen and (max-width: 1280px) {
    width: 900px;
    height: 380px;
  }

  .login-logo {
    margin-top: 50px;
    text-align: center;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }
    img{
      /*width: 240px;*/
      width: auto;
      max-height: 60px;
      @media only screen and (max-width: 1280px) {
        /*width: 200px;*/
        width: auto;
        max-height: 50px;
      }
    }
  }

  .login-title {
    margin-top: 50px;
    font-size: 32px;
    letter-spacing: 0;
    text-align: center;
    color: #999999;

    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }
  }

  .login-border {
    height: 2px;
    margin: 20px auto 20px;
    position: relative;
    width: 80px;
    background: $--color-primary;
    @media only screen and (max-width: 1280px) {
      margin: 20px auto 20px;
    }
  }

  .login-welcome {
    margin-top: 20px;
    font-size: 14px;
    color: $--color-primary;
    letter-spacing: 0;
    line-height: 18px;
    text-align: center;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }
  }

  .demo-tips {
    margin-top: 20px;
    font-size: 18px;
    color: $--color-danger;
    letter-spacing: 0;
    line-height: 18px;
    text-align: center;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }
  }

  .login-form {
    margin-top: 80px;
    padding: 0 40px;

    @media only screen and (max-width: 1280px) {
      margin-top: 40px;
    }

    & ::v-deep .el-input__inner {
      border-radius: 20px;
      border: 1px solid transparent;
      background: $colorBg;
    }
    & :focus {
      border: 1px solid $--color-primary;
    }
  }

  .login-btn {
    margin-top: 22px;
    padding: 0 40px;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }

    .submit {
      width: 100%;
      border-radius: 20px;
    }
  }

  .login-msg {
    margin-top: 10px;
    padding: 0 40px;
    color: $--color-danger;
    text-align: center;
  }

  .login-image {
    background: url(../../assets/login-desc.png) no-repeat;
    background-size: cover;
    width: 100%;
    height: 520px;
    @media only screen and (max-width: 1280px) {
      height: 380px;
    }
  }
  .login-image-de {
    background-size: cover;
    width: 100%;
    height: 200%;
    @media only screen and (max-width: 1280px) {
      height: 380px;
    }
  }
}
</style>
