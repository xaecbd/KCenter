/* eslint react/no-string-refs:0 */
import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Input, Button, Message } from '@alifd/next';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import IceIcon from '@icedesign/foundation-symbol';
import axios from '@utils/axios';
import { setAuthority } from '@utils/authority';
import { reloadAuthorized } from '@utils/Authorized';
import { getUrlCookie, removeUrl } from '@utils/cookies';
import { isUndefined, isNull } from 'util';

@withRouter
class UserLogin extends Component {
  static displayName = 'UserLogin';

  static propTypes = {};

  static defaultProps = {};

  constructor(props) {
    super(props);
    this.state = {
      genericEnable: false,
      serviceName: '',
      value: {
        name: 'admin',
        password: '',
        checkbox: false,
      },
    };
  }

  formChange = (value) => {
    this.setState({
      value,
    });
  };

  handleSubmit = (e) => {
    axios.post('/login/system', JSON.stringify(this.state.value), {
      headers: {
        'Content-Type': 'application/json',
      },
    }).then((res) => {
      if (res.data.code === 200 && res.data.data != null) {
        // this.loginCheck();
        this.systemConfig(res.data.data);
      } else {
        Message.error(res.data.message);
      }
    }).catch((e) => {
      console.log(e);
    });
  };

  redirectToHome = (user) => {
    const userJson = JSON.stringify(user);
    sessionStorage.setItem('user', userJson);
    reloadAuthorized();
    let urls = getUrlCookie('url');
    urls = '/#/home/page';
    if (isUndefined(urls) || isNull(urls) || urls === '') {
      urls = '/#/home/page';
    }
    removeUrl('url');
    window.location.href = urls;
  }

  fetchLogin = () => {
    axios.get('/config/oauth2').then((res) => {
      if (res.data.code === 200 && res.data.data != null) {
        const data = res.data.data;
        this.setState({
          genericEnable: data.enable,
          serviceName: data.name,
        }, () => {
          sessionStorage.setItem('oauthEnable', data.enable);
          this.loginCheck();
        });
      }
    }).catch((error) => {
      console.log(error);
    });
  }

  // 检测用户时候已经登录
  loginCheck = () => {
    axios.get('/login/check').then((res) => {
      if (res.data.code === 200 && res.data.data != null) {
        const user = res.data.data;
        this.redirectToHome(user);
      } else {
        console.log(' please login.');
      }
    }).catch((error) => {
      console.log(error);
    });
  };

    systemConfig = (user) => {
      const userJson = JSON.stringify(user);
      sessionStorage.setItem('user', userJson);
      const role = user.role;
      if (role) {
        if (role.toLocaleLowerCase() === 'admin') {
          setAuthority('admin');
        } else {
          setAuthority('member');
        }
        this.redirectToHome(user);
      }
    }

    genericLogin = () => {
      window.location.href = '/login/oauth2';
    }

    componentWillMount() {
      this.fetchLogin();
    }

    render() {
      return (
        <div style={styles.container}>
          <h4 style={styles.title}>Login in</h4>
          <IceFormBinderWrapper
            value={this.state.value}
            onChange={this.formChange}
            ref="form"
          >
            <div style={styles.formItems}>
              <div style={styles.formItem}>
                <IceIcon type="person" size="small" style={styles.inputIcon} />
                <IceFormBinder name="name" required message="must">
                  <Input
                    size="large"
                    maxLength={20}
                    placeholder="User name"
                    style={styles.inputCol}
                  />
                </IceFormBinder>
                <IceFormError name="name" />
              </div>

              <div style={styles.formItem}>
                <IceIcon type="lock" size="small" style={styles.inputIcon} />
                <IceFormBinder name="password" required message="must">
                  <Input
                    size="large"
                    htmlType="password"
                    placeholder="Password"
                    style={styles.inputCol}
                  />
                </IceFormBinder>
                <IceFormError name="password" />
              </div>

              <div style={styles.footer}>

                <Button type="secondary"
                  size="large"
                  onClick={this.handleSubmit}
                  style={styles.submitBtn}
                >
                Sign in
                </Button>
                {this.state.genericEnable ? <div>
                  <div style={styles.divider}>
                    <div style={styles.border} />
                    <span style={styles.text}> or </span>
                    <div style={styles.border} />
                  </div>
                  <Button
                    type="primary"
                    size="large"
                    onClick={(e) => { this.genericLogin(e); }}
                    style={styles.submitBtn}
                  >
               Sign in with&nbsp;&nbsp;{this.state.serviceName}
                  </Button>
                </div>
                 : null}
              </div>
            </div>
          </IceFormBinderWrapper>
        </div>
      );
    }
}

const styles = {
  container: {
    width: '400px',
    padding: '40px',
    background: '#fff',
    borderRadius: '6px',
  },
  title: {
    margin: '0 0 40px',
    color: 'rgba(0, 0, 0, 0.8)',
    fontSize: '28px',
    fontWeight: '500',
    textAlign: 'center',
  },
  formItem: {
    position: 'relative',
    marginBottom: '20px',
  },
  inputIcon: {
    position: 'absolute',
    left: '12px',
    top: '12px',
    color: '#666',
  },
  inputCol: {
    width: '100%',
    paddingLeft: '20px',
  },
  submitBtn: {
    width: '100%',
  },
  divider: {
    margin: '10px 0',
    display: 'flex',
    alignLontent: 'center',
    alignItems: 'center',
  },
  border: {
    flex: 1,
    borderTop: '1px dashed #d3d3d3',

  },
  text: {
    width: '40px',
    textAlign: 'center',
  },
};

export default UserLogin;
