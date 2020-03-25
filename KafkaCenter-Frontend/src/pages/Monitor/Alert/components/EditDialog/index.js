import React, { Component } from 'react';
import { Dialog, Grid, Input, Radio, Message, Select, NumberPicker, Loading, Switch } from '@alifd/next';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import { getPersonalityCluster } from '@utils/cookies';
import axios from '../../../../../utils/axios';

const RadioGroup = Radio.Group;
const { Row, Col } = Grid;

const consumerapi = [
  {
    value: 'ALL',
  },
  {
    value: 'ZK',
  },
  {
    value: 'Broker',
  },
];

export default class EditDialog extends Component {
  static displayName = 'EditDialog';
  constructor(props) {
    super(props);
    this.state = {
      visible: this.props.visible,
      value: this.props.value,
      isMobile: false,
      clusterInfo: [],
      topicInfo: [],
      groupInfo: [],
      clusterId: '',
      consumerapi,
      isGroupLoading: false,
    };
  }

  componentDidMount() {
    this.fectgClusters();
  }
  fectgClusters = () => {
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
              //  this.props.fetchData();
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

  fetchTopicData = (clusterId) => {
    this.setState({
      isGroupLoading: true,
    }, () => {
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
          this.setState({
            isGroupLoading: false,
          });
        })
        .catch((e) => {
          console.log(e);
          Message.error('Fetch Task Data has error.');
          this.setState({
            isGroupLoading: false,
          });
        });
    });
  };

  fetchGroupData = (data) => {
    this.setState({
      isGroupLoading: true,
    }, () => {
      axios
        .post('/monitor/alert/group/', data)
        .then((response) => {
          if (response.data.code === 200) {
            const datas = this.resouceTopicData(response.data.data);
            this.setState({
              groupInfo: datas,
              isGroupLoading: false,
            });
          } else {
            Message.error(response.data.message);
            this.setState({
              isGroupLoading: false,
            });
          }
        })
        .catch((e) => {
          console.log(e);
          Message.error('Fetch Consumer Group Date Faild.');
          this.setState({
            isGroupLoading: false,
          });
        });
    });
  };

  resouceTopicData = (data) => {
    const dataSource = [];
    data.map((obj) => {
      const entry = {
        value: obj,
        label: obj,
      };
      dataSource.push(entry);
    });
    return dataSource;
  };

  onClusterChange = (value, action, item) => {
    const data = this.state.value;
    data.topicName = '';
    data.consummerGroup = '';
    this.setState({
      clusterId: value,
      value: data,
    });
    this.fetchTopicData(value);
  };

  onTopicChange = (value, action, item) => {
    const datas = this.state.value;
    datas.consummerGroup = '';
    const data = {
      clusterID: this.state.clusterId,
      topic: value,
    };
    this.setState({
      value: datas,
    });
    this.fetchGroupData(data);
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
                      onChange={(value, action, item) => {
                      this.onClusterChange(value, action, item);
                    }}
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
                  <IceFormBinder name="topicName" required>
                    <Select
                      showSearch
                      dataSource={this.state.topicInfo}
                      placeholder="please select topic"
                      style={{ width: '100%' }}
                      onChange={(value, action, item) => {
                      this.onTopicChange(value, action, item);
                    }}
                      defaultValue={this.state.value.topicName}
                      disabled={this.state.value.disabled}
                    />
                  </IceFormBinder>
                  <IceFormError name="topicName" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Group Name:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="consummerGroup" required>
                    <Select
                      showSearch
                      dataSource={this.state.groupInfo}
                      placeholder="please select group"
                      style={{ width: '100%' }}
                      defaultValue={this.state.value.consummerGroup}
                      disabled={this.state.value.disabled}
                    />
                  </IceFormBinder>
                  <IceFormError name="consummerGroup" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Consumer API:</label>
                </Col>
                <Col>
                  <IceFormBinder name="consummerApi" required>
                    <RadioGroup
                      value={this.state.value.consummerApi}
                      aria-labelledby="groupId"
                    >
                      <Radio value="ALL">ALL</Radio>
                      <Radio value="ZK">ZK</Radio>
                      <Radio value="BROKER">BROKER</Radio>
                    </RadioGroup>
                  </IceFormBinder>
                  <IceFormError name="consummerApi" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
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
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Diapause:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="dispause" required>
                    <NumberPicker min={1} style={{ width: '100%' }} placeholder="Diapause(Minutes)" />
                  </IceFormBinder>
                  <IceFormError name="dispause" />
                </Col>
              </Row>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`} style={styles.formLabel}>
                  DisableAlerta:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="disableAlerta">
                    <Switch defaultChecked={this.state.value.disableAlerta} checked={this.state.value.disableAlerta} />
                  </IceFormBinder>
                  <IceFormError name="disableAlerta" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Mail To:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="mailTo">
                    <Input.TextArea
                      style={styles.input}
                      placeholder=""
                      value={this.state.value.mailTo}
                      rows={3}
                      placeholder="you can enter a mail list split by ;"
                    />
                  </IceFormBinder>
                  <IceFormError name="mailTo" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Webhook:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="webhook">
                    <Input.TextArea
                      style={styles.input}
                      placeholder=""
                      value={this.state.value.webhook}
                      rows={3}
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
