import React, { Component } from 'react';
import { Table, Loading, Message, Button } from '@alifd/next';
import { withRouter } from 'react-router-dom';
import axios from '@utils/axios';
import { formatSizeUnits } from '@utils/dataFormat';
import FoundationSymbol from '@icedesign/foundation-symbol';
import ConsumberGroup from './ConsumberGroup';

import './TopicDetail.scss';

@withRouter
export default class TopicDetail extends Component {
  constructor(props) {
    super(props);
    this.state = {
      brokerMsg: [],
      zkMsg: [],
      isConsumberLoading: false,
      clusterName: this.props.match.params.clusterName,
    };
  }
  componentDidMount() {
    const data = {
      topic: this.props.match.params.topicName,
      clusterID: this.props.match.params.id,
    };
    // this.fetchCluster(data);
    // this.fetchMetric(data);
    this.fetchDetail(data);
  }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  };


  fetchDetail = (data) => {
    this.setState(
      {
        isConsumberLoading: true,
      },
      () => {
        axios.defaults.timeout = 180000;
        axios
          .post('/monitor/topic/consumer_offsets', data)
          .then((response) => {
            if (this.mounted) {
              if (response.data.code === 200) {
                this.setState({
                  isConsumberLoading: false,
                });
                if (response.data.data.length > 0) {
                  this.restructurConsumberData(response.data.data);
                }
              } else {
                Message.error(response.data.message);
                this.setState({
                  isConsumberLoading: false,
                });
              }
            }
          })
          .catch((error) => {
            console.error(error);
            Message.error('Get Consumer Offset has error.');
            this.setState({
              isConsumberLoading: false,
            });
          });
      }
    );
  };

  fetchCluster = (data) => {
    axios
      .get(`/cluster/get?id=${data.clusterID}`)
      .then((response) => {
        if (response.data.code === 200) {
          if (response.data.data) {
            if (this.mounted) {
              this.setState({
                clusterName: response.data.data.name,
              });
            }
          }
        }
      })
      .catch((error) => {
        console.error(error);
      });
  }
  restructurConsumberData = (data) => {
    const zkResult = [];
    const brokerResult = [];
    const record = {
      topicName: this.props.match.params.topicName,
      clusterID: this.props.match.params.id,
      clusterName: this.props.match.params.clusterName,
    };
    if (data) {
      data.map((obj) => {
        if (obj.consumerMethod === 'broker') {
          let logEndOffset = 0;
          let lag = 0;
          let offset = 0;
          let isConsumber = false;
          const isSimpleConsumerGroup = obj.simpleConsumerGroup;
          const consumerGroupState = obj.consumerGroupState;

          if (obj.partitionAssignmentStates) {
            obj.partitionAssignmentStates.map((objs) => {
              logEndOffset = parseInt(objs.logEndOffset, 10) + logEndOffset;
              if (objs.clientId) {
                isConsumber = true;
              }
              if (parseInt(objs.offset, 10) >= 0) {
                lag = parseInt(objs.lag, 10) + lag;
                offset = parseInt(objs.offset, 10) + offset;
              }
            });
            obj.partitionAssignmentStates.splice(0, 0, {
              group: obj.groupId,
              logEndOffset,
              offset,
              lag,
              status: '',
            });
            const entry = {
              isSimpleConsumerGroup,
              consumerGroupState,
              isZk: false,
              isConsumber,
            };
            brokerResult.push({
              title: obj.groupId,
              content: (
                <ConsumberGroup
                  datasource={obj.partitionAssignmentStates}
                  config={entry}
                  record={record}
                  consumerMethod={obj.consumerMethod}
                />
              ),
            });
          }
        } else {
          let isConsumber = false;
          let logEndOffset = 0;
          let lag = 0;
          let offset = 0;
          const isSimpleConsumerGroup = obj.simpleConsumerGroup;
          const consumerGroupState = '';

          if (obj.partitionAssignmentStates) {
            obj.partitionAssignmentStates.map((objs) => {
              logEndOffset = parseInt(objs.logEndOffset, 10) + logEndOffset;

              if (objs.clientId) {
                isConsumber = true;
              }
              if (parseInt(objs.offset, 10) >= 0) {
                lag = parseInt(objs.lag, 10) + lag;
                offset = parseInt(objs.offset, 10) + offset;
              }
            });
            obj.partitionAssignmentStates.splice(0, 0, {
              group: obj.groupId,
              logEndOffset,
              offset,
              lag,
              status: '',

            });
            const entry = {
              isSimpleConsumerGroup,
              consumerGroupState,
              isZk: true,
              isConsumber,
            };
            zkResult.push({
              title: obj.groupId,

              content: (
                <ConsumberGroup
                  datasource={obj.partitionAssignmentStates}
                  config={entry}
                  record={record}
                  consumerMethod={obj.consumerMethod}
                />
              ),
            });
          }
        }
      });
    }
    this.setState({
      zkMsg: zkResult,
      brokerMsg: brokerResult,
    });
  };
  backward = () => {
    window.location.href = '#/monitor/consumer';
  };
  handleRefresh = () => {
    const data = {
      topic: this.props.match.params.topicName,
      clusterID: this.props.match.params.id,
    };
    this.fetchDetail(data);
  };

  handleProduct=() => {
    this.props.history.push(`/monitor/producer/metric/${this.props.match.params.id}/${this.state.clusterName}/${this.props.match.params.topicName}`);
  }

  render() {
    return (
      <div style={styles.container}>
        <div style={styles.listTitle}>
          <FoundationSymbol
            onClick={() => this.backward()}
            style={styles.backward}
            size="large"
            type="backward"
          />
          cluster:&nbsp;<span style={styles.listTitles}>{this.state.clusterName}</span>&nbsp;&nbsp;topic:&nbsp;<span style={styles.listTitles}>{this.props.match.params.topicName}</span>
        </div>
        <div style={{ width: '100%', height: '5px' }}>

          <Button
            type="secondary"
            style={{ float: 'right' }}
            onClick={(e) => {
                this.handleRefresh();
              }}
          >  Refresh&nbsp;&nbsp;
            <FoundationSymbol size="small" type="exchange" />
          </Button>
          {' '}
          <Button
            type="secondary"
            style={{ float: ' right', marginRight: '7px' }}
            onClick={() => this.handleProduct()}
          >
            Producer&nbsp;&nbsp;
            <FoundationSymbol size="small" type="link" />
          </Button>
        </div>
        <div style={styles.metricTitle}> Consumers Offsets
        </div>
        <Loading visible={this.state.isConsumberLoading} style={styles.loading}>
          {this.state.brokerMsg.length > 0 ? (
            <div>
              <div style={styles.metricTitle}>
                Offsets Committed to Broker
              </div>
              <div
                style={{
                  minHeight: '15rem',
                  padding: '15px',
                  background: '#fff',
                }}
              >
                {this.state.brokerMsg.map((obj) => {
                  return (
                    <div style={{ marginTop: '10px' }} key={obj.title}>
                      <div>{obj.content}</div>
                    </div>
                  );
                })}
              </div>
            </div>
          ) : null}
          {this.state.zkMsg.length > 0 ? (
            <div>
              <div style={styles.metricTitle}>
                Offsets Committed to ZK
              </div>
              <div
                style={{
                  minHeight: '15rem',
                  padding: '15px',
                  background: '#fff',
                }}
              >
                {this.state.zkMsg.map((obj) => {
                  return (
                    <div style={{ marginTop: '10px' }} key={obj.title}>
                      <div>{obj.content}</div>
                    </div>
                  );
                })}
              </div>
            </div>
          ) : null}
        </Loading>
      </div>
    );
  }
}

const styles = {
  listTitle: {
    marginBottom: '10px',
    fontSize: '30px',
  },
  listTitles: {
    marginBottom: '10px',
    fontSize: '30px',
    fontWeight: 'bold',
  },
  metricTitle: {
    marginBottom: '10px',
    fontSize: '18px',
    fontWeight: 'bold',
    marginTop: '10px',
  },
  loading: {
    width: '100%',
  },
  backward: {
    display: 'inline-block',
    minWidth: '40px',
    marginBottom: '15px',
    cursor: 'pointer',
    color: '#0066FF',
  },
  container: {
    //  margin: '20px',
    padding: '10px 20px 20px',
    minHeight: '600px',
  },
};
