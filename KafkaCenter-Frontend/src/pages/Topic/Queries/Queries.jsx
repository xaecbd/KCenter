import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import QueriesTable from './components/Queries';

export default class Queries extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Topic',
      },
      {
        // eslint-disable-next-line no-script-url
        link: '',
        text: 'Queries',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="Kafka Consumer" />
        <IceContainer style={styles.container}>
          <QueriesTable />
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
