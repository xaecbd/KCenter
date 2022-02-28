import React, { Component } from 'react';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import ClusterList from './Components/Cluster';

export default class Cluster extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Kafka Manager',
      },
      {
        link: '',
        text: 'Cluster',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="Cluster" />
        <ClusterList />
      </div>
    );
  }
}
