import React from 'react';
import PropTypes from 'prop-types';
import { Link, withRouter, useAuth   } from 'ice';
import { Nav } from '@alifd/next';

import { asideMenuConfig } from '../../menuConfig';

const { SubNav } = Nav;
const NavItem = Nav.Item;

function getNavMenuItems(menusData, initIndex,auth) {
  if (!menusData) {
    return [];
  }

  return menusData
    .filter(item => item.name && !item.hideInMenu)
    .map((item, index) => getSubMenuOrItem(item, `${initIndex}-${index}`,auth));
}

const isHasPower=(auth,item)=>{
  if(Object.getOwnPropertyNames(item).includes('authority')){
    const authority = item.authority.toString();
    if(auth.role!==authority.toLocaleLowerCase()){
      return false;
    }else{
      return true;
    }
  }else{
    return true
  }
}

function getSubMenuOrItem(item, index,auth) {
  if(!isHasPower(auth,item)){
    return null;
  }

  if (item.children && item.children.some(child => child.name)) {
    const childrenItems = getNavMenuItems(item.children, index,auth);

    if (childrenItems && childrenItems.length > 0) {
      const name = `\u00a0\u00a0${item.name}`;
      const subNav = (
        <SubNav key={index} icon={item.icon} label={name}>
          {childrenItems}
        </SubNav>
      );
      return subNav;
    }

    return null;
  }



  const navItem = (

    <NavItem key={item.path} icon={item.icon}>
      <Link to={item.path}>&nbsp;&nbsp;{item.name}</Link>
    </NavItem>
  );
  
  return navItem;
}

const Navigation = (props, context) => {
  const { location } = props;
  const { pathname } = location;
  const { isCollapse } = context;
  const [auth] = useAuth();
  return (
    <Nav
      type="normal"
      selectedKeys={[pathname]}
      defaultSelectedKeys={[pathname]}
      embeddable
      activeDirection="right"
      openMode="single"
      iconOnly={isCollapse}
      hasArrow={false}
      mode={isCollapse ? 'popup' : 'inline'}
    >
      {getNavMenuItems(asideMenuConfig, 0,auth)}
    </Nav>
  );
};

Navigation.contextTypes = {
  isCollapse: PropTypes.bool,
};
const PageNav = withRouter(Navigation);
export default PageNav;
