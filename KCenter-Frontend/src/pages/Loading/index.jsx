/* eslint react/no-string-refs:0 */
import React, { useEffect } from 'react';
import { useAuth } from 'ice';
import { Message } from '@alifd/next';
import axios from '../../utils/axios';

import './index.scss';

const Loading = () =>{

  const [auth, setAuth] = useAuth();

  const receiveMessage =  ( event ) => {

    if(event!==undefined && event.data!==undefined && event.data.type!=='webpackOk'){
      const user = event.data;
      const postData = {sessionId:user.sessionId,email:user.email,name:user.name};
      axios.post('/login/verify', JSON.stringify(postData), {
        headers: {
          'Content-Type': 'application/json',
        },
      }).then((res) => {
        if (res.data.code === 200 && res.data !=null && res.data.data != null && res.data!==undefined && res.data.data !==undefined) {
          const userJson = res.data.data;
          sessionStorage.setItem('user', JSON.stringify(userJson));
          const role = userJson.role;
          if (role) {
            setAuth({role:role === 'ADMIN'?'admin':'member'});
            window.location.href = '/#/home/page';
          }
         
        } else {
          Message.error(res.data.message);
        }
      }).catch((e) => {
        console.log(e);
      });
    }
  }

  useEffect(() => {
    // initiate the event handler
    window.addEventListener('message', receiveMessage, false);
    // this will clean up the event every time the component is re-rendered
    return function cleanup() {
      window.removeEventListener('message', receiveMessage);
    };
  });

  return(
    <div className="content">
      <div className="loader">
        <span>Loading...</span>
      </div>
    </div>
  );
}

export default Loading;
