import React, { Component } from 'react';
import IcePanel from '@icedesign/panel';
import { Grid, Input, Checkbox, NumberPicker, Button, Icon, Message, Loading, Select } from '@alifd/next';
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
  topicName: '',
  isByPartition: '',
};

export default class Consumer extends Component {
  static displayName = 'Consumer';
  constructor(props) {
    super(props);
    this.state = {
      queriesRecord: this.props.record,
      consumer,
      consumerResult: {},
      visible: false,
      partition: [],
      partitionValue: '',
      offset: 0,
      partitionDiv: { display: 'none' },
      isPartitionRequired: false,
    };
  }
  onFormChange = (value) => {
    this.setState({
      consumer: value,
    });
  };

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
      postData.clusterID = this.state.queriesRecord.cluster.id;
      postData.topicName = this.state.queriesRecord.topicName;
      postData.partition = this.state.partitionValue;
      postData.offset = this.state.offset;
      postData.isByPartition = this.state.isByPartition;
      axios.post('/topic/list/consumer', postData).then((response) => {
        if (response.data.code === 200) {
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
      }).catch((e) => {
        console.error(e);
        this.setState({
          visible: false,
        });
      });
    });
  };
  // 获取topic所属的partition
  fetchPartition() {
    axios.get(`/topic/query/partition?clusterId=${this.state.queriesRecord.cluster.id}&topicName=${this.state.queriesRecord.topicName}`)
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

  // 接受父类props改变，修改子类中的属性
  componentWillReceiveProps(nextProps) {
    this.setState({
      queriesRecord: nextProps.record,
    });
  }
  componentWillMount() {
    this.setState({
      consumer: {
        groupID: 'TestGroup',
        waitTime: 10000,
        recordNum: 1,
        isCommit: false,
      },
    });
  }

  onByConsumerChange = (value) => {
    this.setState({
      isByPartition: value,
    });
    if (value) {
      this.setState({
        partitionDiv: { display: 'block' },
        isPartitionRequired: true,
      });
      this.fetchPartition();
    } else {
      this.setState({
        partitionDiv: { display: 'none' },
        offset: 0,
        isPartitionRequired: false,
      });
    }
  }
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

  checkGroupID = (rule, values, callback) => {
    if (values.trim() === '') {
      callback('groupId is required');
    } else {
      callback();
    }
  };

  render() {
    return (
      <Loading tip="Query..." visible={this.state.visible} fullScreen shape="fusion-reactor">
        <Row align="top">
          <Col span="12" style={{ height: '730px', lineHeight: '50px' }}>
            <IcePanel style={styles.panel}>
              <IcePanel.Body>
                <IceFormBinderWrapper
                  ref={(ref) => {
                      this.refForm = ref;
                    }}
                  value={this.state.consumer}
                  onChange={this.onFormChange}
                >
                  <div>
                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Kafka Cluster:</label>
                      </Col>
                      <Col span="16">
                        {this.state.queriesRecord.cluster == null ? '' : this.state.queriesRecord.cluster.name}
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Topic Name:</label>
                      </Col>
                      <Col span="16">
                        {this.state.queriesRecord.topicName}
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Group ID:</label>
                      </Col>
                      <Col span="16">
                        <IceFormBinder
                          name="groupID"
                          required
                          min={2}
                          max={50}
                          validator={this.checkGroupID}
                        >
                          <Input
                            style={{ width: '190px' }}
                            placeholder=""
                          />
                        </IceFormBinder>
                        <IceFormError name="groupID" />
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Wait Time(ms):</label>
                      </Col>
                      <Col span="16">
                        <IceFormBinder name="waitTime" required message="required">
                          <NumberPicker min={100} style={{ width: '190px' }} />
                        </IceFormBinder>
                        <IceFormError name="waitTime" />
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Number Of Records:</label>
                      </Col>
                      <Col>
                        <IceFormBinder name="recordNum" required message="required">
                          <NumberPicker min={1} max={100} style={{ width: '190px' }} />
                        </IceFormBinder>
                        <IceFormError name="recordNum" />
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Additional Options:</label>
                      </Col>
                      <Col>
                        <IceFormBinder name="isCommit">
                          <Checkbox> Commit the record consumed</Checkbox>
                        </IceFormBinder>
                        <IceFormError name="isCommit" />
                      </Col>
                    </Row>
                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>
                          Consumer Options:
                        </label>
                      </Col>
                      <Col>
                        <IceFormBinder name="isByPartition">
                          <Checkbox onChange={(value) => { this.onByConsumerChange(value); }}> Consumer Topic By Offset</Checkbox>
                        </IceFormBinder>
                        <IceFormError name="isByPartition" />
                      </Col>
                    </Row>
                    <div style={this.state.partitionDiv}>
                      <Row style={styles.formRow} >
                        <Col span="8">
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
                        <Col span="8">
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
                        <Button type="primary" size="large" onClick={this.onQuery}> <Icon type="search" /> Query</Button>
                      </Col>
                    </Row>
                  </div>
                </IceFormBinderWrapper>
              </IcePanel.Body>
            </IcePanel>
          </Col>
          <Col span="12" style={{ height: '650px', fontSize: '15px', lineHeight: '20px' }}>
            <IcePanel style={styles.panelConsole}>
              <IcePanel.Body>
                {JSON.stringify(this.state.consumerResult) !== '{}' ? <JSONResult result={this.state.consumerResult} /> : ''}
              </IcePanel.Body>
            </IcePanel>
          </Col>
        </Row>
      </Loading>
    );
  }
}

const styles = {
  panelConsole: {
    height: '650px',
    backgroundColor: '#002b36',
    overflowY: 'scroll',
  },
  panel: {
    height: '550px',
    marginBottom: '10px',
  },
  formRow: { marginTop: 20 },
  formLabel: { lineHeight: '26px', fontWeight: '700', textAlign: 'right' },
  formItemError: {
    marginLeft: '10px',
  },
};
