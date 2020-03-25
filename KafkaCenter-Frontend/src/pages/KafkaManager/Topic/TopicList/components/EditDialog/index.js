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
  clusterId: '',
  topicName: '',
  partition: 10,
  replication: 3,
  ttl: 16,
  comments: '',
};

export default class EditDialog extends Component {
  static displayName = 'EditDialog';
  constructor(props) {
    super(props);
    this.state = {
      visible: this.props.visible,
      value: defaultValue,
      isMobile: false,
      createLoading: false,
      clusterInfo: [],
    };
  }

  componentDidMount() {
    this.enquireScreenRegister();
    this.fectgClusters();
  }
  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  // 接受父类props改变，修改子类中的属性
  componentWillReceiveProps(nextProps) {
    this.setState({
      visible: nextProps.visible,
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
      createLoading: false,
    });
    this.props.handelDialog();
  }

  onCreateLoading = () => {
    this.setState({
      createLoading: !this.state.createLoading,
    });
  }

  fectgClusters = () => {
    axios
      .get('/cluster')
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
      .catch((error) => {
        console.error(error);
      });
  };
  resouceData = (data) => {
    const dataSource = [];
    data.map((obj) => {
      const entry = {
        value: obj.id,
        label: obj.name,
      };
      dataSource.push(entry);
    });
    return dataSource;
  };

  onOk = () => {
    this.refForm.validateAll((error) => {
      if (error) {
        return;
      }
      this.setState({
        createLoading: true,
      },
      () => {
        // const postData = JSON.parse(JSON.stringify(this.state.value));
        const postData = this.state.value;
        if (this.state.value.clusterId === '') {
          postData.clusterId = this.state.value.clusterName;
        }
        axios.post('/topic/admin_create', postData)
          .then((response) => {
            if (response.data.code === 200) {
              this.handelDialog();
              this.props.fetchData(postData.clusterId);
              Message.success(response.data.message);
            } else {
              this.onCreateLoading();
              Message.error(response.data.message);
            }
          })
          .catch(() => {
            this.onCreateLoading();
            Message.error('Create Topic has error.');
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

    return (
      <Dialog
        className="simple-form-dialog"
        style={simpleFormDialog}
        autoFocus={false}
        footerAlign="center"
        title="Create Topic"
        onOk={this.onOk}
        onCancel={this.handelDialog}
        onClose={this.handelDialog}
        isFullScreen
        visible={this.state.visible}
        okProps={{ children: 'OK' }}
        cancelProps={{ children: 'Cancel' }}
      >
        <IceFormBinderWrapper
          ref={(ref) => {
            this.refForm = ref;
          }}
          value={this.state.value}
          onChange={this.onFormChange}
        >
          <div style={styles.dialogContent}>
            <Loading visible={this.state.createLoading} style={{ width: '100%' }}>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Cluster Name:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="clusterName" required>
                    <Select
                      showSearch
                      dataSource={this.state.clusterInfo}
                      placeholder="please select cluster"
                      style={{ width: '100%' }}
                      disabled={this.state.value.disabled}
                    />
                  </IceFormBinder>
                  <IceFormError name="clusterName" />
                </Col>
              </Row>
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
