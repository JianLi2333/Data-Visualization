'use strict'
// path模块提供了一些工具函数，用于处理文件与目录的路径。path.join()方法用于连接路径。
const path = require('path')
// require函数用于导入模块，require函数的参数是模块名，可以是相对路径，也可以是绝对路径，
// .js后缀可以省略。require函数返回一个对象，这个对象代表的是引入的模块。
// 这里引入项目源码目录下的settings.js文件，settings.js文件主要用于定义项目的一些配置项。
const defaultSettings = require('./src/settings.js')

const CopyWebpackPlugin = require('copy-webpack-plugin')

function resolve(dir) {
  return path.join(__dirname, dir)
}

const name = defaultSettings.title || 'vue Admin Template' // page title

// 定义访问端口
const port = process.env.port || process.env.npm_config_port || 9528 // dev port
module.exports = {

  // 在生产模式下会生成一个sourcemap文件，
  // 这个文件可以在浏览器中调试源码，定位到源码的位置。
  productionSourceMap: true,


  //开发服务器的配置
  devServer: {
    port: port,
    // 配置代理，用来解决开发环境的跨域问题，所有不是以/login开头的请求都会被代理到http://localhost:8081/上。
    proxy: {
      '^(?!/login)': {
        target: 'http://localhost:8086/',
        ws: false
      }
    },
    open: true,
    // 是否在启动服务器之后显示编译警告和错误的通知。
    overlay: {
      warnings: false,
      errors: true
    },

    // 在服务器启动之前执行mock-server.js文件，用于启动mock服务器。
    // ToDo
    before: require('./mock/mock-server.js')
  },

  pages: {
    index: {
      entry: 'src/main.js',
      template: 'public/index.html',
      filename: 'index.html'
    },
    link: {
      entry: 'src/link/link.js',
      template: 'public/link.html',
      filename: 'link.html'
    },
    nolic: {
      entry: 'src/nolic/nolic.js',
      template: 'public/nolic.html',
      filename: 'nolic.html'
    }
  },
  configureWebpack: {
    name: name,
    devtool: 'source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    },
    plugins: [
      new CopyWebpackPlugin([
        {
          from: path.join(__dirname, 'static'),
          to: path.join(__dirname, 'dist/static')
        }
      ])
    ]
  },
  chainWebpack: config => {
    config.module.rules.delete('svg') // 删除默认配置中处理svg,
    // const svgRule = config.module.rule('svg')
    // svgRule.uses.clear()
    config.module
      .rule('svg-sprite-loader')
      .test(/\.svg$/)
      .include
      .add(resolve('src/icons')) // 处理svg目录
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
  }

}
