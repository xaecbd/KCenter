import React, { Component } from 'react';
import {
  Message,
  Icon,
  Select,
  Input,
  Grid,
  NumberPicker,
  Checkbox,
  Button,
  Loading,
} from '@alifd/next';
import IcePanel from '@icedesign/panel';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import axios from '@utils/axios';
import JSONResult from '@components/JSONResult';

const { Row, Col } = Grid;
const consumer = {
  groupID: 'TestGroup',
  waitTime: 10000,
  recordNum: 1,
  isCommit: false,
  clusterName: '',
  topicName: '',
  isByPartition: '',
};

export default class Queries extends Component {
  constructor(props) {
    super(props);
    this.state = {
      clusterId: '',
      clusterInfo: [],
      topicInfo: [],
      topic: '',
      consumer,
      visible: false,
      consumerResult: {},
      partition: [],
      partitionValue: '',
      offset: 0,
      partitionDiv: { display: 'none' },
      isPartitionRequired: false,
    };
  }

  componentDidMount() {
    this.fecthClusters();
  }
  componentWillMount() {
    this.setState({
      consumer: {
        groupID: 'TestGroup',
        waitTime: 10000,
        recordNum: 1,
        isCommit: false,
        isByPartition: false,
      },
    });
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  };

  // 获取集群信息
  fecthClusters = () => {
    axios
      .get('/monitor/cluster')
      .then((response) => {
        if (response.data.code === 200) {
          const data = this.resouceData(response.data.data);
          this.setState({
            clusterInfo: data,
          });
        } else {
          Message.error(response.data.message);
        }
      })
      .catch(() => {
        Message.error('Get cluster  has error.');
      });
  };

  resouceData = (data) => {
    const dataSource = [];
    data.forEach((obj) => {
      const entry = {
        value: obj.id,
        label: obj.name,
      };
      dataSource.push(entry);
    });
    return dataSource;
  };

  // 级联获取cluster所属的topic
  fetchTopicData = (clusterId) => {
    axios
      .get(`/monitor/alert/topic/${clusterId}`)
      .then((response) => {
        if (response.data.code === 200) {
          const data = this.resouceTopicData(response.data.data);
          this.setState({
            topicInfo: data,
          });
        } else {
          Message.error(response.data.message);
        }
      })
      .catch(() => {
        Message.error('Create Task has error.');
      });
  }
  // 级联获取topic所属的partition
  fetchPartition(value) {
    axios.get(`/topic/query/partition?clusterId=${this.state.clusterId}&topicName=${value}`)
      .then((response) => {
        if (response.data.code === 200) {
          const data = this.resoucePartitionData(response.data.data);
          this.setState({
            partition: data,
          });
        } else {
          Message.error(response.data.message);
        }
      })
      .catch(() => {
        Message.error('Get partition has error.');
      });
  }

  resouceTopicData = (data) => {
    const dataSource = [];
    data.forEach((obj) => {
      const entry = {
        value: obj,
        label: obj,
      };
      dataSource.push(entry);
    });
    return dataSource;
  };
  resoucePartitionData = (data) => {
    const dataSource = [];
    data.forEach((obj) => {
      const entry = {
        value: obj,
        label: obj,
      };
      dataSource.push(entry);
    });
    return dataSource;
  };

  onClusterChange = (value) => {
    const consumer = this.state.consumer;
    consumer.topicName = '';
    this.setState({
      clusterId: value,
      consumer,
    });
    this.fetchTopicData(value);
  };

  onTopicChange = (value) => {
    this.setState({
      topic: value,
    });
    this.fetchPartition(value);
  };
  onPartionChange = (value) => {
    this.setState({
      partitionValue: value,
    });
  };
  onOffsetChange = (value) => {
    this.setState({
      offset: value,
    });
  };
  onByConsumerChange = (value) => {
    let consumer = this.state.consumer;
    consumer.isByPartition = value;
    consumer.groupID = 'TestGroup';
    consumer.waitTime=10000;
    consumer.recordNum=1;
    this.setState({
      consumer,
    });
    if (value) {
      this.setState({
        partitionDiv: { display: 'block' },
        isPartitionRequired: true,
      });
    } else {
      this.setState({
        partitionDiv: { display: 'none' },
        offset: 0,
        isPartitionRequired: false,
      });
    }
  }

  onQuery = () => {
    this.refForm.validateFields((errors) => {
      if (errors) {
        return;
      }
      this.setState({
        visible: true,
        consumerResult: null,
      });
      axios.defaults.timeout = this.state.consumer.waitTime + 5000;
      const postData = this.state.consumer;
      postData.clusterID = this.state.clusterId;
      postData.topicName = this.state.topic;
      postData.partition = this.state.partitionValue;
      postData.offset = this.state.offset;
      axios
        .post('/topic/list/consumer', postData)
        .then((response) => {
          if (response.data.code === 200) {
            if(response.data.data.length===0){
               Message.success({ content:"topic has not consumer data", duration: 5000 });
            }
            this.setState({
                    consumerResult: Object.assign({}, response.data.data),
                    visible: false,
            });
            
           
          } else {
            Message.error({ content: response.data.message, duration: 5000 });
            this.setState({
              visible: false,
            });
          }
        })
        .catch(() => {
          this.setState({
            visible: false,
          });
        });
    });
  };

  checkGroupID = (rule, values, callback) => {
    if (values.trim() === '') {
      callback('groupId is required');
    } else {
      callback();
    }
  };

  render() {
    return (
      <div>
        <Loading
          tip="Query..."
          visible={this.state.visible}
          fullScreen
          shape="fusion-reactor"
        >
          <Row align="top">
            <Col span="12" style={{ height: '720px', lineHeight: '50px' }}>
              <IcePanel.Body>
                <IceFormBinderWrapper
                  ref={(ref) => {
                    this.refForm = ref;
                  }}
                  value={this.state.consumer}
                >
                  <div>
                    <Row style={styles.formRow}>
                      <Col span="6">
                        <label style={styles.formLabel}>Kafka Cluster:</label>
                      </Col>
                      <Col span="16">
                        <IceFormBinder name="clusterName" required message="required">
                          <Select
                            showSearch
                            dataSource={this.state.clusterInfo}
                            placeholder="please select cluster"
                            style={{ width: '80%' }}
                            onChange={(value) => {
                              this.onClusterChange(value);
                            }}
                          />
                        </IceFormBinder>
                        <IceFormError name="clusterName" style={styles.formItemError} />
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="6">
                        <label style={styles.formLabel}>Topic Name:</label>
                      </Col>
                      <Col span="16">
                        <IceFormBinder name="topicName" required message="required">
                          <Select
                            showSearch
                            dataSource={this.state.topicInfo}
                            placeholder="please select topic"
                            style={{ width: '80%' }}
                            // defaultValue={this.state.topic}
                            // value={this.state.topic}
                            onChange={(value) => {
                              this.onTopicChange(value);
                            }}
                          />
                        </IceFormBinder>
                        <IceFormError name="topicName" style={styles.formItemError} />
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="6">
                        <label style={styles.formLabel}>Group ID:</label>
                      </Col>
                      <Col span="16">
                        <IceFormBinder
                          name="groupID"
                          required
                          message="required"
                          min={2}
                          max={50}
                          validator={this.checkGroupID}
                        >
                          <Input style={{ width: '80%' }} placeholder="" />
                        </IceFormBinder>
                        <IceFormError name="groupID" style={styles.formItemError} />
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="6">
                        <label style={styles.formLabel}>Wait Time(ms):</label>
                      </Col>
                      <Col span="16">
                        <IceFormBinder
                          name="waitTime"
                          required
                          message="required"
                        >
                          <NumberPicker min={100} style={{ width: '80%' }} />
                        </IceFormBinder>
                        <IceFormError name="waitTime" style={styles.formItemError} />
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="6">
                        <label style={styles.formLabel}>
                          Number Of Records:
                        </label>
                      </Col>
                      <Col span="16">
                        <IceFormBinder
                          name="recordNum"
                          required
                          message="required"
                        >
                          <NumberPicker
                            min={1}
                            max={100}
                            style={{ width: '80%' }}
                          />
                        </IceFormBinder>
                        <IceFormError name="recordNum" style={styles.formItemError} />
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="6">
                        <label style={styles.formLabel}>
                          Additional Options:
                        </label>
                      </Col>
                      <Col>
                        <IceFormBinder name="isCommit">
                          <Checkbox> Commit The Record Consumed</Checkbox>
                        </IceFormBinder>
                        <IceFormError name="isCommit" style={styles.formItemError} />
                      </Col>
                    </Row>
                    <Row style={styles.formRow}>
                      <Col span="6">
                        <label style={styles.formLabel}>
                          Consumer Options:
                        </label>
                      </Col>
                      <Col>
                        <IceFormBinder name="isByPartition">
                          <Checkbox onChange={(value) => { this.onByConsumerChange(value); }}> Consumer Topic By Offset</Checkbox>
                        </IceFormBinder>
                        <IceFormError name="isByPartition" style={styles.formItemError} />
                      </Col>
                    </Row>
                    <div style={this.state.partitionDiv}>
                      <Row style={styles.formRow} >
                        <Col span="6">
                          <label style={styles.formLabel}>Partition:</label>
                        </Col>
                        <Col span="16">
                          <IceFormBinder name="partition" required={this.state.isPartitionRequired} message="required">
                            <Select
                              showSearch
                              hasClear
                              dataSource={this.state.partition}
                              placeholder="please select partition"
                              style={{ width: '80%' }}
                              onChange={(value) => {
                              this.onPartionChange(value);
                              }}
                            />
                          </IceFormBinder>
                          <IceFormError name="partition" style={styles.formItemError} />
                        </Col>
                      </Row>
                      <Row style={styles.formRow}>
                        <Col span="6">
                          <label style={styles.formLabel}>Offset:</label>
                        </Col>
                        <Col span="16">
                          <IceFormBinder
                            name="offset"
                            message="required"
                            required={this.state.isPartitionRequired}
                          >
                            <Input style={{ width: '80%' }} placeholder="" onChange={(value) => { this.onOffsetChange(value); }} />
                          </IceFormBinder>
                          <IceFormError name="offset" style={styles.formItemError} />
                        </Col>
                      </Row>
                    </div>
                    <Row style={styles.formRow} />
                    <hr />
                    <Row style={styles.formRow}>
                      <Col span="24" style={{ textAlign: 'center' }}>
                        <Button
                          type="primary"
                          size="large"
                          onClick={this.onQuery}
                        >
                          {' '}
                          <Icon type="search" /> Query
                        </Button>
                      </Col>
                    </Row>
                  </div>
                </IceFormBinderWrapper>
              </IcePanel.Body>
            </Col>
            <Col
              span="12"
              style={{ height: '650px', fontSize: '15px', lineHeight: '20px' }}
            >
              <IcePanel style={styles.panelConsole}>
                <IcePanel.Body>
                  {JSON.stringify(this.state.consumerResult) !== '{}' ? (
                    <JSONResult result={this.state.consumerResult} />
                  ) : (
                    ''
                  )}
                </IcePanel.Body>
              </IcePanel>
            </Col>
          </Row>
        </Loading>
      </div>
    );
  }
}


const styles = {
  formRow: { marginTop: 20 },
  formLabel: { lineHeight: '26px', fontWeight: '700', textAlign: 'right' },
  panelConsole: {
    height: '650px',
    backgroundColor: '#002b36',
    overflowY: 'scroll',
  },
  formItemError: {
    marginLeft: '10px',
  },
};
