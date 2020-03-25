import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '../../components/CustomBreadcrumb';
import ApproveList from './components/TopicList';

export default class ApproveTask extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Approve',
      },
      {
        link: '',
        text: 'Task List',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="Task List" />
        <IceContainer style={styles.container} >
          <ApproveList />
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
