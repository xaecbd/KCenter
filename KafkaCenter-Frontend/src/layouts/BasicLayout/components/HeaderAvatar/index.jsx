import React, {useEffect  } from 'react';
import { useHistory } from 'react-router-dom';
import { Avatar, Overlay, Menu, Icon, Button } from '@alifd/next';
import { setUrl } from '@utils/cookies';
import styles from './index.module.scss';
import imageAvatar from './images/avatar.png'

const { Item } = Menu;
const { Popup } = Overlay;

const loginUrl = '/#/user/login';

const UserProfile = (user) => (
  <div className={styles.profile}>
    <div className={styles.avatar}>
      <Avatar src={user && user.picture ? `${user.picture}` : imageAvatar} alt="avatar" />
    </div>
    <div className={styles.content}>
      <h4>{user.name}</h4>
    </div>
  </div>
);

const logout = () => {
  sessionStorage.clear();
  window.location.href = '/login/logout';
};


const checkUser = () => {
  if (sessionStorage.getItem('user')!=null) {
    return true;
  }
  return false;
}




const HeaderAvatar = () => {
  const user = JSON.parse(sessionStorage.getItem('user'));
  const history = useHistory();
  const updateProfile=() => {
    if (checkUser()) {
      if (user.name !== 'admin') {
        history.push('/users');
      }
    } else {
      setUrl('url', '/users');
      window.location.href = loginUrl;
    }
  }
  useEffect(()=>{
    if (!checkUser()) {
      const url=window.location.href;
      setUrl('url', url);
      window.location.href = loginUrl;
    }
  },[]); 


  return (
    <Popup
      trigger={
        <div className={styles.headerAvatar}>
          <Avatar src={user && user.picture ? `${user.picture}` : imageAvatar} alt="avatar" />
          <span
            style={{
              marginLeft: 10,
            }}
          >
            {user?user.name:''}
          </span>
        </div>
      }
      triggerType="click"
    >
      <div className={styles.avatarPopup}>
        <UserProfile {...user} />
        <Menu className={styles.menu}>
          {user && user.name !== 'admin' ? 
            <Item>
              <Button text onClick={updateProfile}>
                <div>
                  <Icon size="small" type="account" />
            Setting
                </div>
              </Button>
            </Item>
            : null}
          <Item>
            <Button text onClick={logout}>
              <div>
                <Icon size="small" type="exit" />
            Sign out
              </div>
            </Button>
          </Item>
        </Menu>
      </div>
    </Popup>
  );
};

HeaderAvatar.defaultProps = {
  name: 'MyName',
  mail: 'name@gmail.com',
  avatar: 'https://img.alicdn.com/tfs/TB1.ZBecq67gK0jSZFHXXa9jVXa-904-826.png',
};
export default HeaderAvatar;
