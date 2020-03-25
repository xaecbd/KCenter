import React, { Component } from 'react';
import { Dialog, Grid, Input, NumberPicker, Message, Select, Loading } from '@alifd/next';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';

import axios from '@utils/axios';

const { Row, Col } = Grid;

export default class EditDialog extends Component {
  constructor(props) {
    super(props);
    this.state = {
      value: {},
      topicCreateSelectTeamData: [],
      updateObjDialog: this.props.updateObjDialog,
      taskConfig: this.props.taskConfig,
      editTaskLoading: false,
    };
  }

  componentDidMount() {
    this.handelDialog();
  }
  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  };
  componentWillReceiveProps(nextProps) {
    this.setState({
      updateObjDialog: nextProps.updateObjDialog,
      taskConfig: nextProps.taskConfig,
    });
    if (nextProps.updateObjDialog.visible) {
      this.setState({
        value: {
          approved: 0,
          approvalOpinions: nextProps.updateObjDialog.record.approvalOpinions,
          id: nextProps.updateObjDialog.record.id,
          clusterIds: [],
          topicName: nextProps.updateObjDialog.record.topicName,
          producer: '',
          consumer: '',
          partition: nextProps.updateObjDialog.record.partition,
          replication: nextProps.updateObjDialog.record.replication,
          ttl: nextProps.updateObjDialog.record.ttl,
          comments: nextProps.updateObjDialog.record.comments,
          messageRate: nextProps.updateObjDialog.record.messageRate,
        },
      });
    }
  }

  hideUpdateDialog = () => {
    this.setState({
      editTaskLoading: false,
    });
    this.props.hideUpdateDialog();
  };

  handelDialog = () => {
    axios
      .get('/team/userteam')
      .then((response) => {
        if (response.data.code === 200 && response.data.data !== undefined && response.data.data.length > 0) {
          this.setState({
            topicCreateSelectTeamData: response.data.data,
          });
        }
      })
      .catch((error) => {
        console.log(error);
      });
  };


  checkComments = (rule, values, callback) => {
    if (values.trim() === '') {
      callback('comments is required');
    } else {
      callback();
    }
  };

  onFormChange = (value) => {
    this.setState({
      value,
    });
  };

  onEditTaskLoading = () => {
    this.setState({
      editTaskLoading: !this.state.editTaskLoading,
    });
  }

  onOk = () => {
    this.refForm.validateAll((error) => {
      if (error) {
        return;
      }
      this.setState({
        editTaskLoading: true,
      },
      () => {
        const postData = JSON.parse(JSON.stringify(this.state.value));
        postData.clusterIds = postData.clusterIds.toString();
        axios
          .put('/topic/task/update', postData)
          .then((response) => {
            if (response.data.code === 200) {
              this.hideUpdateDialog();
              this.props.fetchData();
              Message.success(response.data.message);
            } else {
              this.onEditTaskLoading();
              Message.error(response.data.message);
            }
          })
          .catch(() => {
            this.onEditTaskLoading();
            Message.error('Create Task has error.');
          });
      });
    });
  };

  render() {
    const { isMobile } = this.state;
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };
    const okProps = { children: 'OK' };
    const cancelProps = { children: 'Cancel' };
    return (
      <Dialog
        className="simple-form-dialog"
        style={simpleFormDialog}
        autoFocus={false}
        footerAlign="center"
        title="Topic Task"
        isFullScreen
        onOk={this.onOk}
        onCancel={this.hideUpdateDialog}
        onClose={this.hideUpdateDialog}
        okProps={okProps}
        cancelProps={cancelProps}
        visible={this.state.updateObjDialog.visible}
      >
        <IceFormBinderWrapper
          ref={(ref) => {
            this.refForm = ref;
          }}
          value={this.state.value}
          onChange={this.onFormChange}
        >
          <div style={styles.dialogContent}>
            <Loading visible={this.state.editTaskLoading} style={{ width: '100%' }}>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Approval Opinion:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <label style={styles.warnLabel}>{this.state.value.approvalOpinions}</label>
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Topic Name:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="topicName" required min={2} max={50}>
                    <Input style={styles.input} placeholder="" />
                  </IceFormBinder>
                  <IceFormError name="topicName" />
                </Col>
              </Row>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Team Name:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="teamId" required>
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
                  <IceFormBinder name="location" required>
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
                    <Input.TextArea style={styles.input} placeholder="Enter comments" rows={4} />
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
  warnLabel: { lineHeight: '26px', color: 'red' },
};
