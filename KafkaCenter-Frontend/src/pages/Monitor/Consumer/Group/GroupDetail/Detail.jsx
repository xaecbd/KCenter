import React, { Component } from 'react';
import { Button, Loading, Message } from '@alifd/next';
import { withRouter } from 'react-router-dom';
import FoundationSymbol from '@icedesign/foundation-symbol';
import axios from '@utils/axios';
import ConsumberGroup from './ConsumerGroup';


import './GroupDetail.scss';

@withRouter
export default class Detail extends Component {
  constructor(props) {
    super(props);
    this.state = {
      brokerMsg: [],
      zkMsg: [],
      isConsumberLoading: false,
      clusterName: this.props.match.params.clusterName,
      groupName: this.props.match.params.consummerGroup,
    };
  }
  componentDidMount() {
    const data = {
      clusterID: this.props.match.params.clusterID,
      consummerGroup: this.props.match.params.consummerGroup,
    };
    this.fetchDetail(data);
  }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }


  fetchDetail = (data) => {
    this.setState({
      isConsumberLoading: true,
    }, () => {
      axios.defaults.timeout = 180000;
      axios.post('/monitor/group/detail', data).then((response) => {
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
      }).catch((error) => {
        Message.error('Get Consumer Offset has error.');
        this.setState({
          isConsumberLoading: false,
        });
      });
    });
  }

  restructurConsumberData = (data) => {
    const zkResult = [];
    const brokerResult = [];
    const record = {
      topicName: this.props.match.params.topicName,
      clusterID: this.props.match.params.clusterID,
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
              topic: obj.topic,
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
              topic: obj.topic,
              content: <ConsumberGroup datasource={obj.partitionAssignmentStates} config={entry} record={record} groupName={this.state.groupName} consumerMethod={obj.consumerMethod} />,
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
              group: obj.group,
              topic: obj.topic,
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
              topic: obj.topic,
              content: <ConsumberGroup datasource={obj.partitionAssignmentStates} config={entry} record={record} groupName={this.state.groupName} consumerMethod={obj.consumerMethod} />,
            });
          }
        }
      });
    }
    this.setState({
      zkMsg: zkResult,
      brokerMsg: brokerResult,
    });
  }

  backward = () => {
    this.props.history.goBack();
  }

  handleRefresh = () => {
    const data = {
      clusterID: this.props.match.params.clusterID,
      consummerGroup: this.props.match.params.consummerGroup,
    };
    this.fetchDetail(data);
  }


  render() {
    return (
      <div>
        <Loading
          visible={this.state.isConsumberLoading}
          style={styles.loading}
        >

          <div style={styles.listTitle}><FoundationSymbol onClick={() => this.backward()} style={styles.backward} size="large" type="backward" />
            cluster:&nbsp;<span style={styles.listTitles}>{this.state.clusterName}</span>&nbsp;&nbsp;Group:&nbsp;<span style={styles.listTitles}>{this.props.match.params.consummerGroup}</span>
          </div>
          <div style={{ width: '100%', height: '5px' }}>
            {' '}
            <Button
              type="secondary"
              style={{float: 'right' }}
              onClick={(e) => {
                this.handleRefresh();
              }}
            >Refresh&nbsp;&nbsp;
              <FoundationSymbol size="small" type="exchange" />
            </Button>
          </div>
          {this.state.brokerMsg.length > 0 ?
            <div><div style={styles.metricTitle}>Consumers Offsets Committed to Broker</div>
              <div style={{ minHeight: '32rem', padding: '15px', background: '#fff' }} >
                {
                  this.state.brokerMsg.map((obj) => {
                    return <div style={{ marginTop: '10px' }} key={obj.topic}><div >{obj.content}</div></div>;
                  })
                }
              </div>
            </div> : null}

          {this.state.zkMsg.length > 0 ?
            <div><div style={styles.metricTitle}>Consumers Offsets Committed to ZK</div>
              <div style={{ minHeight: '32rem', padding: '15px', background: '#fff' }}>
                {
                  this.state.zkMsg.map((obj) => {
                    return <div style={{ marginTop: '10px' }} key={obj.topic}><div >{obj.content}</div></div>;
                  })
                }
              </div>
            </div> : null}

          {/* 两种消费信息都为空 显示一个固定大小白框 */}
          {this.state.zkMsg.length <= 0 && this.state.brokerMsg.length <= 0 ?
            <div>
              <div style={{ minHeight: '32rem', padding: '15px', background: '#fff' }} />
            </div> : null}
        </Loading>
      </div>
    );
  }
}

const styles = {
  listTitle: {
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
};
