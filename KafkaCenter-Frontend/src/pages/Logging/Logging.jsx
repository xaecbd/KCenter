/* eslint react/no-string-refs:0 */
import React, { useEffect } from 'react';
import { useAuth } from 'ice';
import { Message } from '@alifd/next';
import { getUrlCookie, removeUrl } from '@utils/cookies';
import axios from '../../utils/axios';

import './Logging.scss';

const Logging = () =>{

  const [auth, setAuth] = useAuth();
  const redirectToHome = () => {
    
    let urls = getUrlCookie('url');
    if (urls === undefined || urls === null || urls === '') {
      urls = '/#/home/page';
    }
    removeUrl('url');
    window.location.href = urls;
  }

  const  writeUserInfo = (user) => {
    const userJson = JSON.stringify(user);
    sessionStorage.setItem('user', userJson);
    const role = user.role;
    if (role) {
      setAuth({ role:user.role === 'ADMIN'?'admin':'member' });
      redirectToHome();
    }
  }

  const userInfo = (e) => {
    axios.get(`/login/user?code=${e}`).then((res) => {
      if (res.data.code === 200 && res.data.data != null) {
        writeUserInfo(res.data.data);
      } else {
        Message.error('Login wish nas error.');
      }
    }).catch((error) => {
      console.log(error);
    });
  };

  useEffect(()=>{
    const href = window.location.href;
    if (href.indexOf('code=') !== -1) {
      const codepre = href.split('=')[1];
      const code = codepre.split('#')[0];
      userInfo(code);
    }
  },[]);

  return(
    <div className="content">
      <div className="loader">
        <span>Loading...</span>
      </div>
    </div>
  );
}

export default Logging;
