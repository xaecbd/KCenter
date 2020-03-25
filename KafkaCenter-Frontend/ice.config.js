const { resolve } = require('path');

module.exports = {
  entry: 'src/index.js',
  publicPath: './',
  outputDir: resolve('../KafkaCenter-Core/src/main/resources/static'),
  minify: true,
  proxy: {
    '/**': {
      enable: true,
      target: 'http://127.0.0.1:8080',
    },
  },
  plugins: [

    ['ice-plugin-moment-locales', {
        locales: ['zh-cn'],
      }],
    ['ice-plugin-fusion', {
      themePackage: '@icedesign/theme',
      themeConfig: {
        'icon-font-path':'"/font/font_107674/font_107674_ps09fo1i2q8semi"',
        'font-custom-path': '"/font/robot/"',
        
      },
      uniteBaseComponent: '@alife/next'
    }],
    ['ice-plugin-css-assets-local', {
        outputPath: 'assets',
        relativeCssPath: '/'
      }],



  ],
  alias: {
    '@utils': resolve(__dirname, 'src/utils/'),
    '@components': resolve(__dirname, 'src/components/'),
    '@images': resolve(__dirname, 'public/images'),
  },
};
