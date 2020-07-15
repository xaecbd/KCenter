import React, { Component } from 'react';

import CustomBreadcrumb from '@components/CustomBreadcrumb';
import axios from '@utils/axios';
import MonitorMetric from './components/MonitorMetric';

export default class MonitorDetail extends Component {
  state = {
    id: this.props.match.params.item,
    cluster: {},
  }

  componentWillMount() {
    this.mounted = true;
    this.fetchDate();
  }

  componentWillUnmount() {
    this.mounted = false;
  }

  fetchDate = () => {
    axios.get(`/cluster/get?id=${this.state.id}`).then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          this.setState({
            cluster: response.data.data,
          });
        }
      }
    }).catch((error) => {
      console.error(error);
    });
  }

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
        text: this.state.cluster.name,
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="" />
        <div style={styles.container} >
          <MonitorMetric id={this.state.id} />
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
