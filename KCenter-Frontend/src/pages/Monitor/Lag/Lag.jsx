import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import GroupLag from './components/Lag';


export default class Lag extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Monitor',
      },
      {
        link: '',
        text: 'Lag',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="Lag" />
        <IceContainer style={styles.container}>
          <GroupLag />
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
