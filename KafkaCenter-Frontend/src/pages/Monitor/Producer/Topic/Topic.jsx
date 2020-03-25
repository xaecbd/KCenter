import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import TopicTable from './TopicTable';


export default class Topic extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Monitor',
      },
      {
        link: '',
        text: 'Producer',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="Producer" />
        <IceContainer style={styles.container} >

          <TopicTable />
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
