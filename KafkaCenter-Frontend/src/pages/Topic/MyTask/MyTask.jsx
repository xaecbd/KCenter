import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import TaskTable from './components/TaskTable';

export default class MyTask extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Topic',
      },
      {
        link: '',
        text: 'My Task',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="My Task" />
        <IceContainer style={styles.container} >
          <TaskTable />
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
