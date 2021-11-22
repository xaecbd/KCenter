import React, { useState, useEffect } from 'react';
import { Input, Message, Form, Button } from '@alifd/next';
import { useAuth } from 'ice';

import { getUrlCookie, removeUrl } from '@utils/cookies';
import { isUndefined, isNull } from 'util';
import axios from '@utils/axios';
import logo from './image/logo.png';
import styles from './index.module.scss';

const orstyles = {
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
const { Item } = Form;
const DEFAULT_DATA = {
  name: '',
  password: '',
};

const LoginBlock = (
  props = {
    dataSource: DEFAULT_DATA,
  }
) => {
  const [oauth, setOauth] = useState({ genericEnable: false, serviceName: '' });
  const [postData, setPostData] = useState(props.DEFAULT_DATA);
  const [auth, setAuth] = useAuth();

  const redirectToHome = (user) => {
    const userJson = JSON.stringify(user);
    sessionStorage.setItem('user', userJson);

    setAuth({ role: user.role === 'ADMIN' ? 'admin' : 'member' });

    let urls = getUrlCookie('url');

    if (
      isUndefined(urls) ||
      isNull(urls) ||
      urls === '' ||
      urls.includes('user/login')
    ) {
      urls = '/#/home/page';
    }
    removeUrl('url');
    window.location.href = urls;
  };

  const loginCheck = () => {
    axios
      .get('/login/check')
      .then((res) => {
        if (
          res.data.code === 200 &&
          res.data != null &&
          res.data.data != null &&
          res.data !== undefined &&
          res.data.data !== undefined
        ) {
          const user = res.data.data;
          redirectToHome(user);
        } else {
          console.log(' please login.');
        }
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const fetchOauth = () => {
    axios
      .get('/config/oauth2')
      .then((res) => {
        if (res.data.code === 200 && res.data.data != null) {
          const data = res.data.data;
          setOauth({
            genericEnable: data.enable,
            serviceName: data.name,
          });
          loginCheck();
          sessionStorage.setItem('oauthEnable', data.enable);
        }
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const formChange = (values) => {
    setPostData(values);
  };

  const handleSubmit = (values, errors) => {
    if (errors) {
      console.log('errors', errors);
      return;
    }

    axios
      .post('/login/system', JSON.stringify(postData), {
        headers: {
          'Content-Type': 'application/json',
        },
      })
      .then((res) => {
        if (
          res.data != null &&
          res.data !== undefined &&
          res.data.data !== undefined &&
          res.data.code === 200 &&
          res.data.data != null
        ) {
          redirectToHome(res.data.data);
        } else {
          Message.error(res.data.message);
        }
      })
      .catch((e) => {
        console.log(e);
      });
  };

  const accountForm = (
    <>
      <Item required requiredMessage="Must">
        <Input name="name" maxLength={20} placeholder="User Account" />
      </Item>
      <Item
        required
        requiredMessage="Must"
        style={{
          marginBottom: 0,
        }}
      >
        <Input.Password
          name="password"
          htmlType="password"
          placeholder="Password"
        />
      </Item>
    </>
  );

  const genericLogin = () => {
    window.location.href = '/login/oauth2';
  };

  // 第一次加载调用，类似于componentDidMount
  useEffect(() => {
    fetchOauth();
  }, []);

  return (
    <div className={styles.LoginBlock}>
      <div className={styles.innerBlock}>
        <a href="#">
          <img className={styles.logo} src={logo} alt="logo" />
        </a>
        <div className={styles.desc}>
          <h4 className={styles.login}>Login in</h4>
        </div>

        <Form value={postData} onChange={formChange} size="large">
          {accountForm}
          <Item />
          <Item>
            <Form.Submit
              type="secondary"
              onClick={handleSubmit}
              className={styles.submitBtn}
              validate
            >
              Sign in
            </Form.Submit>
          </Item>

          <Item>
            {oauth.genericEnable ? (
              <div>
                <div style={orstyles.divider}>
                  <div style={orstyles.border} />
                  <span style={orstyles.text}> or </span>
                  <div style={orstyles.border} />
                </div>
                <Button
                  type="primary"
                  size="large"
                  onClick={genericLogin}
                  className={styles.submitBtn}
                >
                  Sign in with&nbsp;&nbsp;{oauth.serviceName}
                </Button>
              </div>
            ) : null}
          </Item>
        </Form>
      </div>
    </div>
  );
};

export default LoginBlock;
