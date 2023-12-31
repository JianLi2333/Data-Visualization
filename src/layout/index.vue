<template>
  <div :class="classObj" class="app-wrapper">
    <licbar />
    <topbar />

    <de-container style="padding-top: 56px;">
      <de-aside-container v-if="!sidebar.hide" class="le-aside-container">
        <sidebar class="sidebar-container" />
      </de-aside-container>

      <de-main-container class="la-main-container">
        <app-main />
      </de-main-container>
    </de-container>

    <!-- <de-main-container>
      <app-main />
    </de-main-container> -->

    <!-- <div :class="{sidebarHide: sidebar.hide}" class="main-container">
      <app-main />
    </div> -->
  </div>
</template>

<script>
import { Sidebar, AppMain, Topbar, Licbar } from './components'
import ResizeMixin from './mixin/ResizeHandler'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
export default {
  name: 'Layout',
  components: {
    Sidebar,
    AppMain,
    Topbar,
    Licbar,
    DeMainContainer,
    DeContainer,
    DeAsideContainer
  },
  // 这是一个混入对象的数组。混入对象可以实现组件的复用。
  // 混入 (mixins) 是一种分发 Vue 组件中可复用功能的非常灵活的方式。混入对象可以包含任意组件选项。
  // 当组件使用混入对象时，所有混入对象的选项将被“混合”进入该组件本身的选项。
  mixins: [ResizeMixin],

  computed: {
    sidebar() {
      return this.$store.state.app.sidebar
    },
    device() {
      return this.$store.state.app.device
    },
    fixedHeader() {
      return this.$store.state.settings.fixedHeader
    },
    showSettings() {
      return this.$store.state.settings.showSettings
    },
    classObj() {
      return {
        // hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile'
      }
    }
  },
  methods: {
    // 
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "~@/styles/mixin.scss";
  @import "~@/styles/variables.scss";

  .app-wrapper {
    @include clearfix;
    position: relative;
    height: 100%;
    // height: $contentHeight;
    width: 100%;
    &.mobile.openSidebar{
      position: fixed;
      top: 0;
    }
  }
  .drawer-bg {
    background: #000;
    opacity: 0.3;
    width: 100%;
    top: 0;
    height: 100%;
    position: absolute;
    z-index: 999;
  }

  .fixed-header {
    position: fixed;
    top: 0;
    right: 0;
    z-index: 9;
    width: calc(100% - #{$sideBarWidth});
    transition: width 0.28s;
  }

  .hideSidebar .fixed-header {
    width: calc(100% - 56px)
  }

  .mobile .fixed-header {
    width: 100%;
  }

  .la-main-container {
      padding: 0px !important;
  }
  .le-aside-container {
      .sidebar-container {
        width: 100% !important;
        position: initial !important;
        height: calc(100vh - 80px) !important;
        overflow-x: hidden !important;
        overflow-y: auto !important;
      }
  }
</style>
