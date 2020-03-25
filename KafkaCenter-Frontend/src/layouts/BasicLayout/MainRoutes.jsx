import React, { Component, Suspense } from 'react';
import { Switch, Route, Redirect } from 'react-router-dom';
import { Loading } from '@alifd/next';
import NotFound from '../../components/NotFound';
import routerData from '../../routerConfig';
import { asideMenuConfig } from '../../menuConfig';
import Authorized from '../../utils/Authorized';

const { AuthorizedRoute } = Authorized;

class MainRoutes extends Component {
  /**
   * 渲染路由组件
   */
  renderNormalRoute = (item, index) => {
    const SelfComponent = item.component;

    return item.component ? (
      <Route
        key={index}
        path={item.path}
        render={props => <SelfComponent {...props} />}
        exact={item.exact}
      />
    ) : null;
  };

  /**
   * 根据菜单取得重定向地址.
   */
  getRedirectData = () => {
    const redirectData = [];
    const getRedirect = (item) => {
      if (item && item.children) {
        if (item.children[0] && item.children[0].path) {
          redirectData.push({
            from: `${item.path}`,
            to: `${item.children[0].path}`,
          });

          item.children.forEach((children) => {
            getRedirect(children);
          });
        }
      }
    };

    asideMenuConfig.forEach(getRedirect);

    return redirectData;
  };

  /**
   * 渲染权限路由组件
   */
  renderAuthorizedRoute = (item, index) => {
    return item.component ? (
      <AuthorizedRoute
        key={index}
        path={item.path}
        component={item.component}
        exact={item.exact}
        authority={item.authority}
        redirectPath="/exception/403"
      />
    ) : null;
  };

  render() {
    const redirectData = this.getRedirectData();
    return (

      <Suspense fallback={<Loading fullScreen />}>

        <Switch>
          {/* 渲染路由表 */}
          {/* {routerData.map(this.renderNormalRoute)} */}

          {/* 渲染权限路由表 */}

          {routerData.map(this.renderAuthorizedRoute)}

          {/* 路由重定向，嵌套路由默认重定向到当前菜单的第一个路由 */}
          {redirectData.map((item, index) => {
          return <Redirect key={index} exact from={item.from} to={item.to} />;
        })}

          {/* 根路由默认重定向到 /dashboard */}
          <Redirect from="/" to="/user/login" />

          {/* 未匹配到的路由重定向到 NotFound */}
          <Route component={NotFound} />
        </Switch>
      </Suspense>
    );
  }
}

export default MainRoutes;
