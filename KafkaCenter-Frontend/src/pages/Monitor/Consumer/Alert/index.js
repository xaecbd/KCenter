import React, { Component } from 'react';
import { Dialog, Grid, Input, Message, NumberPicker, Loading, Switch } from '@alifd/next';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import { getPersonalityCluster } from '@utils/cookies';
import axios from '../../../../utils/axios';

const { Row, Col } = Grid;


export default class EditDialog extends Component {
  static displayName = 'EditDialog';
  constructor(props) {
    super(props);
    this.state = {
      visible: this.props.visible,
      value: this.props.value,
      isMobile: false,
      isGroupLoading: false,
    };
  }

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
  // 接受父类props改变，修改子类中的属性
  componentWillReceiveProps(nextProps) {
    if (nextProps.visible) {
      this.setState({
        value: nextProps.value,
      });
    }
    this.setState({
      visible: nextProps.visible,
    });
  }

  handelDialog = () => {
    this.props.handelDialog();
  };

  onOk = () => {
    this.refForm.validateAll((error) => {
      if (error) {
        return;
      }
      const postData = this.state.value;
      if (this.state.value.clusterId === '') {
        postData.clusterId = this.state.value.clusterName;
      }
      postData.team = null;
      postData.owner = null;

      this.setState({
        isGroupLoading: true,
      }, () => {
        axios
          .post('/monitor/alert/add', postData)
          .then((response) => {
            if (response.data.code === 200) {
              this.handelDialog();
              Message.success(response.data.message);
              const ids = getPersonalityCluster('monitorAlert').id;
              this.props.fetchData(ids);
            } else {
              Message.error(response.data.message);
            }
            this.setState({
              isGroupLoading: false,
            });
          })
          .catch((e) => {
            console.log(e);
            Message.error('Create Alter has error.');
            this.setState({
              isGroupLoading: false,
            });
          });
      });
    });
  };

  onFormChange = (value) => {
    this.setState({
      value,
    });
  };


  checkMailTo = (rule, values, callback) => {
    if (values.trim() === '') {
      callback('mail is required');
    } else {
      callback();
    }
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
      <Loading visible={this.state.isGroupLoading} style={styles.loading} fullScreen >
        <Dialog
          className="simple-form-dialog"
          style={simpleFormDialog}
          autoFocus={false}
          footerAlign="center"
          title="Alert Task"
          onOk={this.onOk}
          onCancel={this.handelDialog}
          onClose={this.handelDialog}
          isFullScreen
          visible={this.state.visible}
          okProps={okProps}
          cancelProps={cancelProps}
        >
          <IceFormBinderWrapper
            ref={(ref) => {
            this.refForm = ref;
          }}
            value={this.state.value}
            onChange={this.onFormChange}
          >
            <div style={styles.dialogContent}>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>Cluster Name:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <span style={{ width: '100%' }}>{this.state.value.clusterName}</span>
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>Topic Name:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <span style={{ width: '100%' }}>{this.state.value.topicName}</span>
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>Group Name:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <span style={{ width: '100%' }}>{this.state.value.consummerGroup}</span>
                </Col>
              </Row>
              
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>Consumer API:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <span style={{ width: '100%' }}>{this.state.value.consummerApi}</span>
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '6'}`}>
                  <label style={styles.formLabel}>Threshold:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="threshold" required>
                    <NumberPicker min={1} style={{ width: '100%' }} placeholder="Threshold" />
                  </IceFormBinder>
                  <IceFormError name="threshold" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '6'}`}>
                  <label style={styles.formLabel}>Time Window:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="dispause" required>
                    <NumberPicker min={1} style={{ width: '100%' }} placeholder="Time Window(Minutes)" />
                  </IceFormBinder>
                  <IceFormError name="dispause" />
                </Col>
              </Row>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.formLabel}>
                  Disable Alerta:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="disableAlerta">
                    <Switch defaultChecked={this.state.value.disableAlerta} checked={this.state.value.disableAlerta} />
                  </IceFormBinder>
                  <IceFormError name="disableAlerta" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '6'}`}>
                  <label style={styles.formLabel}>Mail To:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="mailTo">
                    <Input.TextArea
                      style={styles.input}
                      placeholder=""
                      value={this.state.value.mailTo}
                      rows={3}
                      // eslint-disable-next-line react/jsx-no-duplicate-props
                      placeholder="you can enter a mail list split by ;"
                    />
                  </IceFormBinder>
                  <IceFormError name="mailTo" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '6'}`}>
                  <label style={styles.formLabel}>Webhook:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="webhook">
                    <Input.TextArea
                      style={styles.input}
                      placeholder=""
                      value={this.state.value.webhook}
                      rows={3}
                      // eslint-disable-next-line react/jsx-no-duplicate-props
                      placeholder="you can enter a webHook"
                    />
                  </IceFormBinder>
                  <IceFormError name="webhook" />
                </Col>
              </Row>
            </div>
          </IceFormBinderWrapper>
        </Dialog>
      </Loading>
    );
  }
}
const styles = {
  simpleFormDialog: { width: '640px' },
  dialogContent: {},
  formRow: { marginTop: 20 },
  input: { width: '100%' },
  formLabel: { lineHeight: '26px' },
  loading: { width: '100%' },
};
