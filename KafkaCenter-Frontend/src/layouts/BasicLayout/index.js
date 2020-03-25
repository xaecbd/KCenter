/* eslint no-undef:0, no-unused-expressions:0, array-callback-return:0 */
import React, { Component } from 'react';
import Layout from '@icedesign/layout';
import { withRouter } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import Aside from './components/Aside';
import MainRoutes from './MainRoutes';
import { reloadAuthorized } from '@utils/Authorized';
import { setAuthority, removeAuthority } from '@utils/authority';
import axios from '@utils/axios';
import { getAuthority } from '../../utils/authority';


@withRouter
export default class BasicLayout extends Component {
  render() {
    axios.get('/login/check').then((res) => {
      if (res.data.code === 200 && res.data != null && res.data.data != null && res.data != undefined && res.data.data != undefined) {
        const user = res.data.data;
        const role = user.role;
        const userJson = JSON.stringify(user);
        sessionStorage.setItem('user', userJson);
        if (role.toLocaleLowerCase() != getAuthority()) {
          sessionStorage.clear();
          removeAuthority();
          window.location.href = '/login/logout';
        }
      }
    }).catch((error) => {
      console.log(error);
    });

    return (

      <Layout fixable style={styles.layout}>
        {/* 顶部导航  */}

        <Header />

        <Layout.Section style={styles.secion}>
          {/* 侧边导航  */}
          <Aside />

          {/* 主体内容 */}
          <Layout.Main scrollable style={styles.main}>

            <MainRoutes />

            {/* 底部页脚 */}
            <Footer />
          </Layout.Main>
        </Layout.Section>
      </Layout>
    );
  }
}

const styles = {
  secion: {
    flexDirection: 'row',
  },
  main: {
    padding: '0',
    background: '#f2f2f2',
  },
};
