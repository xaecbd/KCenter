/* eslint no-undef:0, no-unused-expressions:0, array-callback-return:0 */
import { Link } from 'react-router-dom';
import { withRouter } from 'react-router';
import FoundationSymbol from '@icedesign/foundation-symbol';
import Layout from '@icedesign/layout';
import { Nav } from '@alifd/next';
import React, { Component } from 'react';
import { asideMenuConfig } from '../../../../menuConfig';
import Authorized from '../../../../utils/Authorized';
import './index.scss';

const { SubNav, Item } = Nav;
@withRouter
export default class Aside extends Component {
  constructor(props) {
    super(props);
    const openKeys = this.getDefaultOpenKeys();
    this.state = {
      openKeys,
    };
    this.openKeysCache = openKeys;
  }
  /**
   * 当前展开的菜单项
   */
  onOpenChange = (openKeys) => {
    this.setState({
      openKeys,
    });
    this.openKeysCache = openKeys;
  };
  /**
   * 获取当前展开的菜单项
   */
  getDefaultOpenKeys = () => {
    const { location = {} } = this.props;
    const { pathname } = location;

    let openKeys = [];
    if (Array.isArray(asideMenuConfig)) {
      asideMenuConfig.forEach((item, index) => {
        if (pathname.startsWith(item.path)) {
          openKeys = [`${index}`];
        }
      });
    }
    return openKeys;
  };

  /**
   * 权限检查
   */
  checkPermissionItem = (authority, ItemDom) => {
    if (Authorized.check) {
      const { check } = Authorized;
      return check(authority, ItemDom);
    }
    return ItemDom;
  };

  /**
   * 获取菜单项数据
   */
  getNavMenuItems = (menusData) => {
    if (!menusData) {
      return [];
    }
    return menusData
      .filter(item => item.name && !item.hideInMenu)
      .map((item, index) => {
        const ItemDom = this.getSubMenuOrItem(item, index);
        return this.checkPermissionItem(item.authority, ItemDom);
      })
      .filter(item => item);
  };


  /**
   * 二级导航
   */
  getSubMenuOrItem = (item, index) => {
    if (item.children && item.children.some(child => child.name)) {
      const childrenItems = this.getNavMenuItems(item.children);

      if (childrenItems && childrenItems.length > 0) {
        return (
          <SubNav
            key={index}
            label={
              <span>
                {item.icon ? (
                  <FoundationSymbol size="small" type={item.icon} />
                        ) : null}
                <span className="ice-menu-collapse-hide">
                  {item.name}
                </span>
              </span>
                    }
          >
            {childrenItems}
          </SubNav>
        );
      }
      return null;
    }

    const linkProps = {};
    if (item.newWindow) {
      linkProps.href = item.path;
      linkProps.target = '_blank';
    } else if (item.external) {
      linkProps.href = item.path;
    } else {
      linkProps.to = item.path;
    }
    return (
      <Item key={item.path}>
        <Link {...linkProps}>
          <span>
            {item.icon ? (
              <FoundationSymbol size="small" type={item.icon} />
            ) : null}
            <span className="ice-menu-collapse-hide">{item.name}</span>
          </span>
        </Link>
      </Item>
    );
  };

  render() {
    const { location } = this.props;
   let { pathname } = location;
    if (pathname.search('/') !== -1) {    
        const path=  pathname.split('/');
        if(path.length>3){
            pathname=  pathname.substring(0,pathname.indexOf(path[3])-1);
        }      
    //  / pathname = pathname.substring(2, pathname.indexOf('/'));
    }
    return (
      <Layout.Aside width="240" theme="light" className="custom-aside">
        <Nav
          defaultSelectedKeys={[pathname]}
          selectedKeys={[pathname]}
          onOpen={this.onOpenChange}
          openKeys={this.state.openKeys}
          className="custom-menu"
        >
          {this.getNavMenuItems(asideMenuConfig)}
        </Nav>
        {/* 侧边菜单项 end */}
      </Layout.Aside>
    );
  }
}
