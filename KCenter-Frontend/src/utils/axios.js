import axios from 'axios';
// 使用由库提供的配置的默认值来创建实例 此时超时配置的默认值是 `0`

const axiosInstance = axios.create();
// 覆写库的超时默认值 现在，在超时前，所有请求都会等待 1分钟
axiosInstance.defaults.timeout = 120000;

// axiosInstance.defaults.baseURL = 'http://localhost:8080';

axiosInstance.interceptors.response.use((response) => {
  return response;
}, (error) => {
  if (error.response.status === 401) {
    window.location = '#/user/login';
  } else {
    return Promise.reject(error);
  }
});

export default axiosInstance;
