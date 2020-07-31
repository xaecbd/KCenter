import React, { Component } from 'react';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import SummaryChart from './components/SummaryChart';


export default class Summary extends Component {
  render() {
    const breadcrumb = [
      {
        // eslint-disable-next-line no-script-url
        link: '#/home/page',
        text: 'Home',
      },
      {
        // eslint-disable-next-line no-script-url
        link: '',
        text: 'Cluster Summary',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="" />
        <div style={styles.container} >
          <SummaryChart />
        </div>
      </div>
    );
  }
}

const styles = {
  container: {
    padding: '10px 20px 20px',
  },
};
