import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import AlertList from './components/Alert';


export default class Alert extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Monitor',
      },
      {
        link: '',
        text: 'Alert',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="Alert" />
        <IceContainer style={styles.container} >
          <AlertList />
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
