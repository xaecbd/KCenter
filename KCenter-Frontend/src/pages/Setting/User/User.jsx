import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import UserTable from './components/User';

export default class User extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Setting',
      },
      {
        link: '',
        text: 'User',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="User" />
        <IceContainer style={styles.container}>
          <UserTable />
        </IceContainer>
      </div>
    );
  }
}

const styles = {
  container: {
    margin: '20px',
    padding: '10px 20px 20px',
    minHeight: '600px',
  },
};
