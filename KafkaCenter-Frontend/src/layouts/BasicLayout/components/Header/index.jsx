import React, { Component } from 'react';
import { Link, withRouter } from 'react-router-dom';
import { Balloon, Icon, Nav } from '@alifd/next';
import FoundationSymbol from '@icedesign/foundation-symbol';
import IceImg from '@icedesign/img';
import { headerMenuConfig } from '../../../../menuConfig';
import Logo from '../Logo';
import './index.scss';
import { removeAuthority } from '../../../../utils/authority';
import { setUrl } from '@utils/cookies';


const { SubNav, Item } = Nav;
@withRouter
export default class Header extends Component {
  constructor(props) {
    super(props);
  }

    state={
      user: null,
    }

  logout = () => {
    sessionStorage.clear();
    removeAuthority();
    window.location.href = '/login/logout';
  };

  updateProfile=() => {
    if (this.checkUser()) {
      if (this.state.user.name !== 'admin') {
        this.props.history.push('/users');
      }
    } else {
      window.location.href = '/user/login';
    }
  }


  checkUser = () => {
    if (sessionStorage.getItem('user')) {
      this.setState({
        user: JSON.parse(sessionStorage.getItem('user')),
      });
      return true;
    }
    return false;
  }

  componentWillMount() {
    if (!this.checkUser()) {
      if (window.location.href !== '/user/login') {
        setUrl('url', window.location.href);
      }
    }
  }

  render() {
    const user = this.state.user;
    //  const user =  logcheck();
    const { location = {} } = this.props;
    const { pathname } = location;
    return (
      <div className="header-container">
        <div className="header-content">
          <Logo isDark />
          <div className="header-navbar">
            <Nav
              className="header-navbar-menu"
              onClick={this.handleNavClick}
              selectedKeys={[pathname]}
              defaultSelectedKeys={[pathname]}
              direction="hoz"
              type="primary"
            >
              {headerMenuConfig &&
                headerMenuConfig.length > 0 &&
                headerMenuConfig.map((nav, index) => {
                  if (nav.children && nav.children.length > 0) {
                    return (
                      <SubNav
                        triggerType="click"
                        key={index}
                        title={
                          <span>
                            {nav.icon ? (
                              <FoundationSymbol size="small" type={nav.icon} />
                            ) : null}
                            <span className="nav-name-text">{nav.name}</span>
                          </span>
                        }
                      >
                        {nav.children.map((item) => {
                          const linkProps = {};
                          if (item.external) {
                            if (item.newWindow) {
                              linkProps.target = '_blank';
                            }

                            linkProps.href = item.path;
                            return (
                              <Item key={item.path}>
                                <a {...linkProps}>
                                  <span>{item.name}</span>
                                </a>
                              </Item>
                            );
                          }
                          linkProps.to = item.path;
                          return (
                            <Item key={item.path}>
                              <Link {...linkProps}>
                                <span>{item.name}</span>
                              </Link>
                            </Item>
                          );
                        })}
                      </SubNav>
                    );
                  }
                  const linkProps = {};
                  if (nav.external) {
                    if (nav.newWindow) {
                      linkProps.target = '_blank';
                    }
                    linkProps.href = nav.path;
                    return (
                      <Item key={nav.path}>
                        <a {...linkProps}>
                          <span>
                            {nav.icon ? (
                              <FoundationSymbol size="small" type={nav.icon} />
                            ) : null}
                            <span className="nav-name-text">{nav.name}</span>
                          </span>
                        </a>
                      </Item>
                    );
                  }
                  linkProps.to = nav.path;
                  return (
                    <Item key={nav.path}>
                      <Link {...linkProps}>
                        <span>
                          {nav.icon ? (
                            <FoundationSymbol size="small" type={nav.icon} />
                          ) : null}
                          <span className="nav-name-text">{nav.name}</span>
                        </span>
                      </Link>
                    </Item>
                  );
                })}
            </Nav>
          </div>

          <Balloon
            trigger={
              <div
                className="ice-design-header-userpannel"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  fontSize: 12,
                }}
              >
                <IceImg
                  height={40}
                  width={40}
                  src={user && user.picture ? `${user.picture}` : require('./images/avatar.png')}
                  // src={require('./images/avatar.png')}
                  className="user-avatar"
                />
                <div className="user-profile">
                  <span className="user-name" style={{ fontSize: '13px' }}>
                    {user ? user.name : null}
                  </span>
                </div>
                <Icon
                  type="arrow-down-filling"
                  size="xxs"
                  className="icon-down"
                />
              </div>
            }
            closable={false}
            className="user-profile-menu"
          >
            <ul>
              {this.state.user && this.state.user.name !== 'admin' ? <div onClick={(e) => { this.updateProfile(e); }}><li className="user-profile-menu-item" ><FoundationSymbol type="repair" size="small" />Setting</li></div> : null}
              <div onClick={(e) => { this.logout(e); }}><li className="user-profile-menu-item" >  <FoundationSymbol type="compass" size="small" />  LogOut</li></div>
            </ul>
          </Balloon>
        </div>
      </div>
    );
  }
}
