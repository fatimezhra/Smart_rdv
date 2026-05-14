const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  app.use(
    createProxyMiddleware(['/auth', '/reservations', '/api'], {
      target: 'http://localhost:8081',
      changeOrigin: true,
      secure: false,
    })
  );
};
