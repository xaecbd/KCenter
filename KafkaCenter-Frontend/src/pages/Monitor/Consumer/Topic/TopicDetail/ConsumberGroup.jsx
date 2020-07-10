/* eslint-disable indent */
import React, { Component } from 'react';
import { Table, Icon, Loading } from '@alifd/next';
import { withRouter } from 'react-router-dom';
import FoundationSymbol from '@icedesign/foundation-symbol';
import dayjs from 'dayjs';
import IceImg from '@icedesign/img';
import warnning from '@images/warning.svg';
import green from '@images/green.svg';
import error from '@images/error.svg';
import alert from '@images/alert.svg';
import axios from '@utils/axios';
import './TopicDetail.scss';
import EditDialog from '../../Alert/';

const renderHost = (value, index, record) => {
  if (value) {
    return value.toString().replace('/', '');
  }
      return '';
};

const splitTimer = (value) => {
  if (value) {
    let timer = new Array();
    let result;
    timer = value.toString().split('-');
    const resp = '\\d{10}|\\d{13}';
    timer.map((objs) => {
      if (objs.match(resp)) {
        result = objs;
      }
    });

    // eslint-disable-next-line radix
    return dayjs(parseInt(result)).format('YYYY-MM-DD HH:mm:ss');
  }
  return '';
};

const getCellProps = (rowIndex, colIndex) => {
    if (rowIndex === 0) {
       return propsConf;
    }
};

const propsConf = {
    style: { background: '#dddddd' },
    onDoubleClick: () => {

    },
};
@withRouter
export default class ConsumerGroup extends Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: false,
      display: false,
      topic: '',
      clusterName: '',
      clusterID: '',
      consumerMethod: '',
      visible: false,
      alertValue: {},
      alertData: {},
    };
  }

  onClick = () => {
     this.setState({
         display: !this.state.display,
     });
  }
  componentDidMount() {
        // eslint-disable-next-line react/no-did-mount-set-state
        this.setState({
          topic: this.props.record.topicName,
          clusterID: this.props.record.clusterID,
          clusterName: this.props.record.clusterName,
          consumerMethod: this.props.consumerMethod,
        });
        let taryger;
        if (this.props.config.isZk) {
             taryger = document.getElementsByClassName('zktest')[0];
        } else {
            taryger = document.getElementsByClassName('custom-table')[0];
        }
        // taryger.getElementsByTagName('thead');
        taryger.getElementsByTagName('thead')[0].style.display = 'table-header-group';
  }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  handelGroupChart = (record) => {
    let zkName;
    if (this.props.config.isZk) {
        zkName = 'zk';
    } else {
        zkName = 'broker';
    }
    this.props.history.push(`/monitor/consumer/topic/consumer_offsets/chart/${this.props.record.clusterID}/${this.props.record.topicName}/${record.group}/${zkName}`);
  };

  renderNumber = (value) => {
      if (value < 0) {
         return '-';
      }
      return value;
  }

  renderIceImag = (img, title) => {
    return (
      <div id="advanced">
        <IceImg
          title={title}
          height={26}
          width={27}
          src={img}
          style={{ textAlign: 'center', verticalAlign: 'middle' }}
        />
      </div>

    );
  }

  hideDialog = () => {
    this.setState({
      visible: !this.state.visible,
    });
  }

  handelDialog = (record) => {
    // 先根据已有的数据去查询alert是否已存在
    const data = record;
    const obj = Object.create(data);
    obj.clusterId = this.state.clusterID;
    obj.consummerGroup = record.group;
    obj.topicName = this.state.topic;
    obj.clusterName = this.state.clusterName;
    obj.consummerApi = this.state.consumerMethod.toUpperCase();

    this.setState(
      {
        loading: true,
      },
      () => {
        axios
        .post('/monitor/alert/getalert', obj)
          .then((response) => {
            if (response.data.code === 200) {
              this.setState({
                alertData: response.data.data,
                loading: false,
              });
              const newData = this.state.alertData;
              if (obj.consummerApi !== newData.consummerApi) {
                newData.consummerApi = 'ALL';
              }
              const newObj = Object.create(newData);
              newObj.clusterId = newData.clusterId;
              newObj.consummerGroup = newData.consummerGroup;
              newObj.consummerApi = newData.consummerApi;
              newObj.topicName = newData.topicName;
              newObj.clusterName = this.state.clusterName;
              newObj.disableAlerta = newData.disableAlerta;
              newObj.disabled = true;
              newObj.enable = newData.enable;
              newObj.id = newData.id;
              if (newObj.id === null) {
                newObj.diapause = '';
                newObj.diapause = '';
                newObj.mailTo = '';
                newObj.webhook = '';
              } else {
                newObj.diapause = newData.diapause;
                newObj.threshold = newData.threshold;
                newObj.mailTo = newData.mailTo;
                newObj.webhook = newData.webhook;
              }
              this.setState({
                visible: !this.state.visible,
                alertValue: newObj,
              });
            }
          })
          .catch(() => {
            console.error(error);
          });
      }
    );
  };

  renderAlert = (value, index, record) => {
    if (index !== 0) {
      return '';
    }
    return <div><a target="_blank" style={styles.alertIcon} onClick={() => this.handelDialog(record)}><IceImg title="alert" height={26} width={27} src={alert} style={{ textAlign: 'center', verticalAlign: 'middle' }} /></a></div>;
  }
  fetchData = () => {
  }

  render() {
    let type;
    let data = this.props.datasource;
    if (this.state.display) {
        type = <Icon type="arrow-up" size="xs" onClick={(e) => { this.onClick(e); }} />;
    } else {
        type = <Icon type="arrow-down" size="xs" onClick={(e) => { this.onClick(e); }} />;
        data = data.slice(0, 1);
    }
    const renderObj = (value) => {
        if (value === '') {
          if (this.props.config.isZk) {
              return this.renderIceImag(green, this.props.config.consumerGroupState);
           }
           return this.renderIceImag(green, this.props.config.consumerGroupState);
        }
        return this.renderIceImag(green, this.props.config.consumerGroupState);
      };
      const render = (value, index, record) => {
        if (index !== 0) {
            return '';
        }
        if (this.props.config.isSimpleConsumerGroup && !this.props.config.isZk) {
            return <div>{type}<a style={styles.topicLink} onClick={() => this.handelGroupChart(record)}>{value}</a><span title="SimpleConsumerGroup" className="onMeous"><FoundationSymbol type="customize" size="small" /></span></div>;
        }
        return <div>{type}&nbsp;<a target="_blank" style={styles.topicLink} onClick={() => this.handelGroupChart(record)}>{value}</a></div>;
      };
      const renderYellowObj = (value) => {
          if (value === '') {
            if (this.props.config.isZk) {
                return this.renderIceImag(warnning, 'SimpleConsumerGroup无法判断其状态');
            }
            return this.renderIceImag(warnning, 'SimpleConsumerGroup无法判断其状态');
          }
          return this.renderIceImag(warnning, 'SimpleConsumerGroup无法判断其状态');
        };
        const StopObj = (value) => {
          if (this.props.config.isZk) { return this.renderIceImag(error, this.props.config.consumerGroupState); }
           return this.renderIceImag(error, this.props.config.consumerGroupState);
        };

    let result = null;
    if (this.props.config.isSimpleConsumerGroup && !this.props.config.isZk) {
    result = <Table.Column title="Status" dataIndex="status" cell={renderYellowObj} width={10} />;
    } else if (this.props.config.consumerGroupState === 'EMPTY') {
        result = <Table.Column title="Status" dataIndex="status" titile={this.props.config.consumerGroupState} cell={StopObj} width={10} />;
        } else if (this.props.config.consumerGroupState === 'STABLE') {
        // if()
        if (this.props.config.isConsumber) {
            result = <Table.Column title="Status" dataIndex="status" cell={renderObj} width={10} />;
        } else {
            result = <Table.Column title="Status" dataIndex="status" cell={StopObj} width={10} />;
        }
        } else {
        result = <Table.Column title="Status" dataIndex="status" cell={renderYellowObj} width={10} />;
        }
    if (this.props.config.isZk) {
        if (this.props.config.isConsumber) {
        result = <Table.Column title="Status" dataIndex="status" cell={renderObj} width={10} />;
        } else {
        result = <Table.Column dataIndex="status" title="Status" cell={StopObj} width={10} />;
        }
    }

    return (
      <div className={this.props.config.isZk ? 'zktest' : ''}>
        <Loading visible={this.state.loading} style={styles.loading}>
          <EditDialog
            visible={this.state.visible}
            handelDialog={this.hideDialog}
            value={this.state.alertValue}
            fetchData={this.fetchData}
          />
        </Loading>
        <Table className="custom-table" dataSource={data} cellProps={getCellProps}>
          <Table.Column title="Consumer Group" dataIndex="group" cell={render} width={150} />
          <Table.Column title="Partition" dataIndex="partition" width={60} />
          <Table.Column title="Offset" dataIndex="offset" width={100} cell={this.renderNumber} />
          <Table.Column title="LogSize" dataIndex="logEndOffset" width={100} />
          <Table.Column title="Lag" dataIndex="lag" width={100} cell={this.renderNumber} />

          {this.props.config.isZk ? null : <Table.Column title="Host" dataIndex="host" width={90} cell={renderHost} />}

          {
              this.props.config.isZk ? <Table.Column title="Last Seen" dataIndex="clientId" cell={splitTimer} width={100} /> : null }
          <Table.Column title="Owner" dataIndex="clientId" width={240} />

          { result}
          <Table.Column title="Alert" width={10} cell={this.renderAlert} />
        </Table>
      </div>

    );
  }
}

const styles = {
  topicLink: {
    margin: '0 5px',
    color: '#1111EE',
    cursor: 'pointer',
    textDecoration: 'none',
  },
  alertIcon: {
    cursor: 'pointer',
  },
  loading: {
    width: '100%',
  },
};
