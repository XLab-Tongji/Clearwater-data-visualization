// vue.config.js
module.exports = {
    devServer: {
        proxy: {
            '/bbs': {
                target: 'http://localhost:8088',
                // secure: false,
                changeOrigin: true,
                ws: true,
                // pathRewrite: {
                //   '^/api': ''
                // }
            }
        },
    },
}