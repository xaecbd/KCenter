import React, { Component } from 'react';
import { Tab } from '@alifd/next';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import TopicList from './Topic/TopicList';
import GroupList from './Group/GroupList';

export default class Metric extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Monitor',
      },
      {
        link: '',
        text: 'Consumer',
      },
    ];
    return (
      <div style={{ minHeight: '600px' }}>
        <CustomBreadcrumb items={breadcrumb} title="Consumer" />
        <IceContainer style={styles.container} >
          <Tab>
            <Tab.Item title="Topic" key="1"><TopicList /></Tab.Item>
            <Tab.Item title="Group" key="2"><GroupList /></Tab.Item>
          </Tab>
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
