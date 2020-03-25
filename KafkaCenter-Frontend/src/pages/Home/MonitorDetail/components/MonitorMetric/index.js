import React, { Component } from 'react';
import {
  Loading,
  Message,
  Table,
} from '@alifd/next';
import axios from '@utils/axios';
import { transToNumer, formatSizeUnits, sortData } from '@utils/dataFormat';
import IceContainer from '@icedesign/container';
import { withRouter } from 'react-router-dom';
import MonitorChart from '../MonitorChart';

import '../../../Cluster.scss';


@withRouter
export default class MonitorMetric extends Component {
  state = {
    id: this.props.id,
    data: [],
    clusterData: [],
    isLoading: false,

  };


  componentWillMount() {
    this.mounted = true;
    this.fetchData();
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  fetchData = () => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios
          .get(`/home/detail/metric/${this.state.id}`)
          .then((response) => {
            if (response.data.code === 200) {
              const resp = response.data.data;
              let clusterData = [];
              let brokerData = [];
              Object.keys(resp).forEach((key, index) => {
                if (key === 'Count') {
                  clusterData = resp[key];
                } else if (key === 'Single') {
                  brokerData = resp[key];
                }
              });
              const result = sortData(brokerData, 'broker');
              if (this.mounted) {
                this.setState({
                  data: result,
                  clusterData,
                  isLoading: false,
                });
              }
            } else {
              Message.error(response.data.message);
            }
            if (this.mounted) {
              this.setState({
                isLoading: false,
              });
            }
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  };

  onSort(value, order) {
    let dataSource = [];
    if (value === 'broker') {
      dataSource = this.state.data.sort((a, b) => {
        a = a[value];
        b = b[value];
        if (order === 'asc') {
          return a.localeCompare(b);
        }
        return b.localeCompare(a);
      });
    } else {
      dataSource = this.state.data.sort((a, b) => {
        a = parseInt(a[value], 10);
        b = parseInt(b[value], 10);
        if (order === 'asc') {
          return a - b;
        } else if (order === 'desc') {
          return b - a;
        }
      });
    }

    this.setState(
      {
        data: dataSource,
      }
    );
  }

  render() {
    return (
      <div>
        <IceContainer>
          <Loading
            visible={this.state.isLoading}
            style={styles.loading}
          >
            <div style={{ width: '50%', float: 'left' }}>

              <Table dataSource={this.state.data} onSort={(value, order) => this.onSort(value, order)}>
                <Table.Column title="Broker" dataIndex="broker" width={35} style={styles.text} sortable />
                <Table.Column title="Port" dataIndex="port" width={12} style={styles.text} />
                <Table.Column title="JmxPort" dataIndex="jmxPort" width={12} style={styles.text} />
                <Table.Column
                  title="Message"
                  dataIndex="msgInOneMin"
                  width={12}
                  cell={transToNumer}
                  style={styles.text}
                  sortable
                />
                <Table.Column
                  title="Bytes In"
                  dataIndex="byteInOneMin"
                  width={12}
                  cell={formatSizeUnits}
                  style={styles.text}
                />
                <Table.Column
                  title="Bytes Out"
                  dataIndex="byteOutOneMin"
                  width={12}
                  cell={formatSizeUnits}
                  style={styles.text}
                />
              </Table>

            </div>

            <div style={{ width: '50%', float: 'right' }}>
              <Table dataSource={this.state.clusterData}>
                <Table.Column
                  title="MetricName"
                  dataIndex="metricName"
                  width={80}
                  style={styles.text}
                />
                <Table.Column title="MeanRate" dataIndex="meanRate" width={12} cell={formatSizeUnits} style={styles.text} />
                <Table.Column
                  title="OneMinuteRate"
                  dataIndex="oneMinuteRate"
                  width={12}
                  cell={formatSizeUnits}
                  style={styles.text}
                />
                <Table.Column
                  title="FiveMinuteRate"
                  dataIndex="fiveMinuteRate"
                  width={12}
                  cell={formatSizeUnits}
                  style={styles.text}
                />
                <Table.Column
                  title="FifteenMinuteRate"
                  dataIndex="fifteenMinuteRate"
                  width={15}
                  cell={formatSizeUnits}
                  style={styles.text}
                />
              </Table>
            </div>
          </Loading>
        </IceContainer>
        <IceContainer>
          <MonitorChart cid={this.state.id} formatSizeUnits={this.formatSizeUnits} />
        </IceContainer>
      </div>
    );
  }
}
const styles = {
  loading: {
    width: '100%',
  },
  text: {
    textAlign: 'center',
  },
};

