import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import GroupList from './components/Group';


export default class Topic extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Kafka Manager',
      },
      {
        link: '',
        text: 'Group',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title=''/>
        <IceContainer style={styles.container}>
          <GroupList />
        </IceContainer>
      </div>
    );
  }
}
const styles = {
  container: {
    margin: '5px',
    padding: '10px 20px 20px',
    minHeight: '600px',
  },
};
