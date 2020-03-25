import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import TopicList from './components/TopicList';


export default class Topic extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Kafka Manager',
      },
      {
        link: 'javascript:window.location.reload();',
        text: 'Topic',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="Topic List" />
        <IceContainer style={styles.container}>
          <TopicList />
        </IceContainer>
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
