module.exports = [
  {
    context: (pathname, req) => pathname === '/login' && req.method === 'POST',
    target: 'http://localhost:9100',
    secure: false,
    changeOrigin: false,
  },
  {
    context: ['/api', '/oauth2', '/.well-known', '/actuator', '/userinfo', '/connect'],
    target: 'http://localhost:9100',
    secure: false,
    changeOrigin: false,
  },
];
