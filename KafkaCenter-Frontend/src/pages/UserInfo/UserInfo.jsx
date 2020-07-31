/* eslint react/no-string-refs:0 */
import React, { Component } from 'react';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import Users from './components/UserInfo';


class UserInfo extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Setting',
      },
      {
        link: '',
        text: 'My Profile',
      },
    ];
    return (
      <div >
        <CustomBreadcrumb items={breadcrumb} title="My Profile" />
        <div style={styles.container}>
          <Users />
        </div>


      </div>
    );
  }
}


const styles = {
  container: {
    margin: '20px',
    padding: '10px 20px 20px',
  },
};
export default UserInfo;
