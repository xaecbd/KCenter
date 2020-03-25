/* eslint react/no-string-refs:0 */
import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Message } from '@alifd/next';
import axios from '../../utils/axios';
import { setAuthority } from '../../utils/authority';
import { reloadAuthorized } from '../../utils/Authorized';
import { getUrlCookie, removeUrl } from '@utils/cookies';
import './Logging.scss';


@withRouter
class Logging extends Component {
  redirectToHome = () => {
    reloadAuthorized();
    // window.location.href = '/#/home/page';
    let urls = getUrlCookie('url');
    if (urls === undefined || urls === null || urls === '') {
      urls = '/#/home/page';
    }
    removeUrl('url');
    window.location.href = urls;
  }

  writeUserInfo = (user) => {
    const userJson = JSON.stringify(user);
    sessionStorage.setItem('user', userJson);
    const role = user.role;
    if (role) {
      if (role.toLocaleLowerCase() === 'admin') {
        setAuthority('admin');
      } else {
        setAuthority('member');
      }
      this.redirectToHome();
    }
  }

  userInfo = (e) => {
    axios.get(`/login/user?code=${e}`).then((res) => {
      if (res.data.code === 200 && res.data.data != null) {
        this.writeUserInfo(res.data.data);
      } else {
        Message.error('Login wish nas error.');
      }
    }).catch((error) => {
      console.log(error);
    });
  };

  componentWillMount() {
    const href = window.location.href;
    if (href.indexOf('code=') != -1) {
      const codepre = href.split('=')[1];
      const code = codepre.split('#')[0];
      this.userInfo(code);
    }
  }

  render() {
    return (
      <div className="content">
        <div className="loader">
          <span>Loading...</span>
        </div>
      </div>
    );
  }
}

export default Logging;
