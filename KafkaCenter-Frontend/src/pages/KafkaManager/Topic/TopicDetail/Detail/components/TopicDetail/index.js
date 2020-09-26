import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import IceContainer from '@icedesign/container';
import FoundationSymbol from '@icedesign/foundation-symbol';
import { Table, Card, Button, Message, Dialog, Loading } from '@alifd/next';
import axios from '@utils/axios';
import { formatSizeUnits } from '@utils/dataFormat';
import Auth from '@components/Auth'
import AddPartition from '../Partition';


@withRouter
export default class Detail extends Component {
  state = {
    configData: [],
    visable: false,
    isMetricLoading: false,
    metric: [],
    topicsummary: [],
    clusterName: '',
  };

  componentWillMount() {
    this.mounted = true;
    this.fetchCluster();
    this.fetchMetric();
    this.fetchSummary();
    this.fetchData();
  }
  componentWillUnmount() {
    this.mounted = false;
  }


  fetchSummary = () => {
    const params = {
      topicName: this.props.match.params.topic,
      clusterId: this.props.match.params.clusterId,
    };

    axios.post('/manager/topic/summary', params).then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          this.setState({
            topicsummary: response.data.data,
          });
        }
      }
    }).catch((e) => {
      console.error(e);
    });
  }

  fetchCluster = () => {
    axios
      .get(`/cluster/get?id=${this.props.match.params.clusterId}`)
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
  fetchData = () => {
    const params = {
      topicName: this.props.match.params.topic,
      clusterId: this.props.match.params.clusterId,
    };
    axios.post('/manager/topic/config', params).then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          this.recombinat(response.data.data);
        }
      }
    }).catch((e) => {
      console.error(e);
    });
  }

  addPartitions = () => {
    this.setState({
      visable: true,
    });
  }

  deleteTopic = () => {
    Dialog.confirm({
      title: 'Delete',
      content: 'Do you want to delete this topic?',
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
      onOk: () => {
        const params = {
          topicName: this.props.match.params.topic,
          clusterId: parseInt(this.props.match.params.clusterId, 10),
        };
        axios.post('/manager/delete/topic', params).then((response) => {
          if (response.data.code === 200) {
            Message.success(response.data.message);
            this.handleCancel();
          } else {
            Message.error(response.data.message);
          }
        }).catch((e) => {
          console.error(e);
        });
      },
    });
  }


  refreshPage = () => {
    this.fetchSummary();
    this.fetchData();
  }
  recombinat = (data) => {
    let configData = [];
    Object.keys(data).forEach((key, index) => {
      if (key === 'config') {
        configData = data[key];
      }
    });

    this.setState({
      configData,
    });
  }

  changeParationState = () => {
    this.setState({
      visable: !this.state.visable,
    });
  }

  updateConfig = () => {
    this.props.history.push(`/cluster/topic/config/${this.props.match.params.clusterId}/${this.props.match.params.clusterName}/${this.props.match.params.topic}`);
  }

  fetchMetric = () => {
    const data = {
      topic: this.props.match.params.topic,
      clusterID: this.props.match.params.clusterId,
    };
    this.setState(
      {
        isMetricLoading: true,
      },
      () => {
        axios.defaults.timeout = 180000;
        axios
          .post('/monitor/topic/consumer_offsets/topic_metric', data)
          .then((response) => {
            if (response.data.code === 200) {
              if (response.data.data) {
                if (this.mounted) {
                  this.setState({
                    metric: response.data.data,
                    isMetricLoading: false,
                  });
                }
              } else {
                this.setState({
                  isMetricLoading: false,
                });
              }
            }
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  };

  handleCancel = () => {
    this.props.history.push(`/cluster/${this.props.match.params.clusterId}/${this.props.match.params.clusterName}/topic`);
    // this.props.history.go(-1);
  }


  render() {
    const { configData } = this.state;
    const data = {
      topicName: this.props.match.params.topic,
      clusterId: this.props.match.params.clusterId,
    };
    let replica = 0;
    if (this.state.topicsummary.summary) {
      this.state.topicsummary.summary.forEach((obj) => {
        if (obj.name === 'replication') {
          replica = obj.value;
        }
      });
    }
    if (this.state.topicsummary.partition) {
      data.partition = Object.keys(this.state.topicsummary.partition).length;
      data.replica = replica;
    }
    const brokerstaw = (value, index, record) => {
      if (value === 'true') {
        return <font style={{ backgroundColor: '#ffeeba' }}>{value}</font>;
      }
      return value;
    };


    return (
      <div>
        <IceContainer style={styles.container}>
          <div>

            <AddPartition visable={this.state.visable} data={data} changeParationState={this.changeParationState} refreshPage={this.refreshPage} />
            <div style={styles.listTitle}>
              <FoundationSymbol
                onClick={() => this.handleCancel()}
                style={styles.backward}
                size="large"
                type="backward"
              />
              cluster:&nbsp;<span style={styles.listTitles}>{this.state.clusterName} </span>&nbsp;&nbsp;topic:&nbsp;<span style={styles.listTitles}>{this.props.match.params.topic}</span>
            </div>
            <Card style={styles.card} contentHeight="auto">
                <div style={styles.title}>Topic Summary</div>
                <Table dataSource={this.state.topicsummary.summary} hasHeader={false}>
                  <Table.Column dataIndex="name" width={150} />
                  <Table.Column dataIndex="value" width={150} />
                </Table>
                <p />
               
              </Card>
            <Auth rolename="admin"> 
              <Card title="Operations" style={styles.cards} contentHeight="auto">
              
              <Button type="secondary" onClick={this.deleteTopic} style={styles.button}>Delete Topic</Button>&nbsp;&nbsp;&nbsp;
             <Button type="secondary" style={styles.button} disabled> Reassign Partitions </Button>&nbsp;&nbsp;&nbsp;
                <Button type="secondary" style={styles.button} disabled>Generate Partition Assignments</Button><br />
                <Button type="secondary" onClick={this.addPartitions} style={styles.button}>Add Partitions</Button>&nbsp;&nbsp;&nbsp;
                <Button type="secondary" onClick={this.updateConfig} style={styles.button}>Update Config</Button>&nbsp;&nbsp;&nbsp;
                <Button type="secondary" style={styles.button} disabled>Manual Partition Assignments</Button>
              </Card></Auth>
              <Card title="Partitions by Broker" style={styles.cards} contentHeight="auto">
                <Table dataSource={this.state.topicsummary.broker} >
                  <Table.Column
                    title="Broker"
                    dataIndex="broker"
                    width={50}
                  />
                  <Table.Column
                    title="# of Partitions"
                    dataIndex="partitions"
                    width={50}
                  />
                  <Table.Column
                    title="# as Leader"
                    dataIndex="leaderCount"
                    width={50}
                  />
                  <Table.Column
                    title="Partitions"
                    dataIndex="partition"
                    width={100}
                  />
                  <Table.Column
                    title="Skewed?"
                    dataIndex="Skewed"
                    width={100}
                    cell={brokerstaw}
                  />
                  <Table.Column
                    title="Leader Skewed?"
                    dataIndex="LeaderSkewed"
                    width={100}
                    cell={brokerstaw}
                  />
                </Table>
                <div style={styles.title}>Topic Config</div>
                <Table dataSource={configData} hasHeader={false}>
                  <Table.Column dataIndex="config" width={150} />
                  <Table.Column dataIndex="value" width={150} />
                </Table>
              </Card>


              <Card title="Metric" style={styles.metriCard} contentHeight="auto">
                <Loading visible={this.state.isMetricLoading} style={styles.loading}>
                  <Table dataSource={this.state.metric}>
                    <Table.Column title="Rate" dataIndex="metricName" />
                    <Table.Column
                      title="Mean"
                      dataIndex="meanRate"
                      cell={formatSizeUnits}
                    />
                    <Table.Column
                      title="1 min"
                      dataIndex="oneMinuteRate"
                      cell={formatSizeUnits}
                    />
                    <Table.Column
                      title="5 min"
                      dataIndex="fiveMinuteRate"
                      cell={formatSizeUnits}
                    />
                    <Table.Column
                      title="15 min"
                      dataIndex="fifteenMinuteRate"
                      cell={formatSizeUnits}
                    />
                  </Table>
                </Loading>
              </Card>
              <Card title="Partition Information" contentHeight="auto" style={styles.cardPartition}>
                <Table dataSource={this.state.topicsummary.partition}>
                  <Table.Column title="Partition" dataIndex="partition" width={100} />
                  <Table.Column title="Leader" dataIndex="leader" width={100} />
                  <Table.Column title="Replicas" dataIndex="replicas" width={150} />
                  <Table.Column title="In Sync Replicas" dataIndex="InSyncReplicas" width={150} />
                  <Table.Column title="Preferred Leader?" dataIndex="Preferred_Leader" width={150} />
                  <Table.Column title="Under Replicated?" dataIndex="Under_Replicated" width={150} />
                </Table>
              </Card>
          </div>
        </IceContainer>
      </div>
    );
  }
}
const styles = {
  title: {
    fontSize: '18px',
    marginTop: '10px',
  },
  card: {
    width: '50%',
    height: '100%',
    float: 'left',
    marginTop:'0px !important'
  },
  cards: {
    width: '50%',
    height: '100%',
    float: 'right',
    marginTop:'0px !important'
  },
  cardPartition: {
    width: '100%',
    height: '100%',
  },
  button: {
    marginBottom: '10px',
    marginRight: '5px',
  },
  loading: {
    width: '100%',
  },
  backward: {
    display: 'inline-block',
    minWidth: '40px',
    cursor: 'pointer',
    color: '#0066FF',
  },
  listTitle: {
    marginBottom: '10px',
    fontSize: '30px',
  },
  listTitles: {
    marginBottom: '10px',
    fontSize: '30px',
    fontWeight: 'bold',
  },
  metriCard: {
    width: '50%',
    height: '100%',
  },
};
