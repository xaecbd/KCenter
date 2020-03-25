import React, { Component } from 'react';
import { Dialog, Grid, Input, NumberPicker, Message, Select, Loading } from '@alifd/next';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import { enquireScreen } from 'enquire-js';
import axios from '@utils/axios';

const { Row, Col } = Grid;
const defaultValue = {
  topicName: '',
  partition: 10,
  replication: 3,
  ttl: 16,
  comments: '',
  location: 'ALL',
};

export default class FormDialog extends Component {
  static displayName = 'FormDialog';
  constructor(props) {
    super(props);
    this.state = {
      visible: this.props.visible,
      value: defaultValue,
      topicCreateSelectTeamData: this.props.topicCreateSelectTeamData,
      taskConfig: this.props.taskConfig,
      isMobile: false,
      createTaskLoading: false,
    };
  }

  componentDidMount() {
    this.enquireScreenRegister();
  }
  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  // 接受父类props改变，修改子类中的属性
  componentWillReceiveProps(nextProps) {
    if (nextProps.visible) {
      // 显示dialog，初始化数据
      this.setState({
        value: {
          topicName: '',
          producer: '',
          consumer: '',
          partition: 10,
          replication: 3,
          ttl: nextProps.taskConfig.ttl,
          comments: '',
          location: 'ALL',
        },
        taskConfig: nextProps.taskConfig,
        topicCreateSelectTeamData: nextProps.topicCreateSelectTeamData,
      });
    }
    this.setState({
      visible: nextProps.visible,
      topicCreateSelectTeamData: nextProps.topicCreateSelectTeamData,
    });
  }

  enquireScreenRegister = () => {
    const mediaCondition = 'only screen and (max-width: 720px)';

    enquireScreen((mobile) => {
      if (this.mounted) {
        this.setState({
          isMobile: mobile,
        });
      }
    }, mediaCondition);
  };

  checkComments = (rule, values, callback) => {
    if (values.trim() === '') {
      callback('comments is required');
    } else {
      callback();
    }
  };

  checkTopicName = (rule, values, callback) => {
    const reg = /^[0-9a-zA-Z\.\-_]{3,100}$/;
    if (values.trim() === '') {
      callback('topicName is required');
    } else if (!reg.test(values)) {
      callback('topicName can only consist of letters, numbers, underscores, dashes, and dots, and the length is between 3-100');
    } else {
      callback();
    }
  };


  handelDialog = () => {
    this.setState({
      createTaskLoading: false,
    });
    this.props.handelDialog();
  }

  onCreateTaskLoading = () => {
    this.setState({
      createTaskLoading: !this.state.createTaskLoading,
    });
  }

  onOk = () => {
    this.refForm.validateAll((error) => {
      if (error) {
        return;
      }
      this.setState({
        createTaskLoading: true,
      },
      () => {
        const postData = JSON.parse(JSON.stringify(this.state.value));
        axios.post('/topic/task/add', postData)
          .then((response) => {
            if (response.data.code === 200) {
              this.handelDialog();
              this.props.fetchData();
              Message.success(response.data.message);
            } else {
              this.onCreateTaskLoading();
              Message.error(response.data.message);
            }
          })
          .catch(() => {
            this.onCreateTaskLoading();
            Message.error('Create Task has error.');
          });
      });
    });
  };

  onFormChange = (value) => {
    this.setState({
      value,
    });
  };
  render() {
    const { isMobile } = this.state;
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };
    // 响应式处理
    if (isMobile) {
      simpleFormDialog.width = '300px';
    }
    const okProps = { children: 'OK' };
    const cancelProps = { children: 'Cancel' };
    return (
      <Dialog
        className="simple-form-dialog"
        style={simpleFormDialog}
        autoFocus={false}
        footerAlign="center"
        title="Topic Task"
        onOk={this.onOk}
        onCancel={this.handelDialog}
        onClose={this.handelDialog}
        isFullScreen
        okProps={okProps}
        cancelProps={cancelProps}
        visible={this.state.visible}
      >
        <IceFormBinderWrapper
          ref={(ref) => {
            this.refForm = ref;
          }}
          value={this.state.value}
          onChange={this.onFormChange}
        >
          <div style={styles.dialogContent}>
            <Loading visible={this.state.createTaskLoading} style={{ width: '100%' }}>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Topic Name:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder
                    name="topicName"
                    required
                    min={2}
                    max={50}
                    validator={this.checkTopicName}
                  >
                    <Input
                      style={styles.input}
                      placeholder=""
                    />
                  </IceFormBinder>
                  <IceFormError name="topicName" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Team Name:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder
                    name="teamId"
                    required
                  >
                    <Select
                      showSearch
                      dataSource={this.state.topicCreateSelectTeamData}
                      placeholder="please select team"
                      style={{ width: '100%' }}
                    />
                  </IceFormBinder>
                  <IceFormError name="teamId" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Location:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder
                    name="location"
                    required
                  >
                    <Select
                      showSearch
                      dataSource={this.state.taskConfig.remoteLocations}
                      placeholder="please select location"
                      style={{ width: '100%' }}
                    />
                  </IceFormBinder>
                  <IceFormError name="location" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Message Rate:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="messageRate" required>
                    <NumberPicker step={50} min={1} />
                  </IceFormBinder>
                  <IceFormError name="messageRate" />
                  &nbsp;&nbsp;(message/s)
                </Col>
              </Row>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Partitions:</label>
                </Col>
                <Col>
                  <IceFormBinder name="partition" required>
                    <NumberPicker min={1} />
                  </IceFormBinder>
                  <IceFormError name="partition" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Replication Factor:</label>
                </Col>
                <Col>
                  <IceFormBinder name="replication" required>
                    <NumberPicker min={1} max={10} />
                  </IceFormBinder>
                  <IceFormError name="replication" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>TTL:</label>
                </Col>
                <Col>
                  <IceFormBinder name="ttl" required>
                    <NumberPicker min={1} />
                  </IceFormBinder>
                  <IceFormError name="ttl" />
                  &nbsp;&nbsp;(H)
                </Col>
              </Row>


              <Row style={styles.formRow}>
                <Col>
                  <IceFormBinder name="comments" required validator={this.checkComments}>
                    <Input.TextArea
                      style={styles.input}
                      placeholder="Enter comments"
                      rows={4}
                    />
                  </IceFormBinder>
                  <IceFormError name="comments" />
                </Col>
              </Row>
            </Loading>
          </div>

        </IceFormBinderWrapper>
      </Dialog>
    );
  }
}
const styles = {
  simpleFormDialog: { width: '640px' },
  dialogContent: {},
  formRow: { marginTop: 20 },
  input: { width: '100%' },
  formLabel: { lineHeight: '26px' },
};
