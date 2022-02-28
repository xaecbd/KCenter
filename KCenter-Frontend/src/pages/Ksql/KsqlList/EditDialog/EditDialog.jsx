import React, { Component } from 'react';
import { Dialog, Grid, Input, Message, Select, Loading } from '@alifd/next';
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
  ksqlAddress: '',
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
      teamData: [],
      teamLoading: false,
    };
  }

  componentDidMount() {
    this.fetchTeamData();
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
      value: {},
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

  checkKsqlAddress = (rule, values, callback) => {
    callback();
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
        
        const postData = this.state.value;
        postData.clusterId = this.state.value.clusterName;
        postData.teamIds = this.state.value.teams.join(',');
     

        
        axios.post(`/ksql/add_ksql`,postData)
          .then((response) => {
            if (response.data.code === 200) {
              this.props.fetchData();
              this.handelDialog();
              Message.success(response.data.message);
            } else {
              this.onCreateLoading();
              Message.error(response.data.message);
            }
          })
          .catch(() => {
            this.onCreateLoading();
            Message.error('Create has error.');
          });
      });
    });
  };

  onFormChange = (value) => {
    this.setState({
      value,
    });
  };

  fetchTeamData = () =>{
    axios.get('/team')
      .then((response) => {
        if (response.data.code === 200) {
          this.setState({
            teamData: response.data.data,           
          });
        } else {
        
          Message.error(response.data.message);
        }
      })
      .catch((e) => {
        Message.error('Create has error.');
      });
   
  }

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
        title="New KsqlSrver"
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
                  <label style={styles.formLabel}>KSQL Address:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder
                    name="ksqlUrl"
                    required
                    min={2}
                    max={50}
                    triggerType="onBlur"
                    validator={this.checkKsqlAddress}
                  >
                    <Input
                      style={styles.input}
                      placeholder="127.0.0.1:8088"
                    />
                  </IceFormBinder>
                  <IceFormError name="ksqlAddress" />
                </Col>
              </Row>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Teams:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder
                    name="teams"
                    required
                    triggerType="onBlur"
                    // validator={this.checkKsqlAddress}
                  >
                 

                    <Select mode="multiple"  showSearch   
                      dataSource={this.state.teamData}
                      style={styles.input} />

                  </IceFormBinder>
                  <IceFormError name="teams" />
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
