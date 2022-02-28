import React, { Component } from 'react';
import { Dialog, Grid, Message, NumberPicker, Select } from '@alifd/next';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import axios from '@utils/axios';

const { Row, Col } = Grid;

export default class AddPartition extends Component {
  state = {
    formValue: this.props.data,
    isMobile: false,
    partition: this.props.data.partition,
    replica: this.props.data.replica,
    brokerList: [],
    sureDialog: false,
    errormess: '',
  };

  componentWillReceiveProps(nextProps) {
    const visable = nextProps.visable;
    this.setState({
      visible: visable,
      formValue: nextProps.data,
      partition: nextProps.data.partition,
      replica: nextProps.data.replica,
    }, () => {
    });
  }

  componentWillMount() {
    this.fetchBrokers();
  }

  fetchBrokers = () => {
    axios.post('/manager/topic/broker_list', this.state.formValue).then((response) => {
      if (response.data.code === 200) {
        this.setState({
          brokerList: response.data.data,
        });
      }
    }).catch((e) => {
      console.error(e);
    });
  }

  onOk = () => {
    if (this.state.errormess === '') {
      this.form.validateAll((error) => {
        if (error) {
          return;
        }
        if (this.state.formValue.partition >= 50) {
          this.setState({
            sureDialog: true,
          });
        } else {
          this.sureAddPartition();
        }
      });
    }
  };

  handelDialog = () => {
    this.setState({
      errormess: '',
    });
    this.props.changeParationState();
  }

  closeSureDialog = () => {
    this.setState({
      errormess: '',
      sureDialog: false,
    });
  }

  sureAddPartition = () => {
    this.setState({
      errormess: '',
      sureDialog: false,
    });
    const data = {};
    data.topicName = this.state.formValue.topicName;
    data.partition = this.state.formValue.partition;
    data.clusterId = this.state.formValue.clusterId;
    data.partitions = this.state.formValue.partitions.toString().replace('[').replace(']');
    data.oldPartition = this.state.partition;
    axios.post('/manager/topic/partition', data).then((response) => {
      if (response.data.code === 200) {
        Message.success(response.data.message);
        this.handelDialog();
        this.props.refreshPage();
      } else {
        Message.error(response.data.message);
      }
    }).catch((e) => {
      console.error(e);
    });
  }
  handelChange = (value) => {
    if (value.toString().split(',').length < this.state.replica && value.toString().length !== 0) {
      this.setState({
        errormess: 'You must select broker than replica count',
      });
    } else {
      this.setState({
        errormess: '',
      });
    }
  }

  render() {
    const { isMobile } = this.state;
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };
    if (isMobile) {
      simpleFormDialog.width = '300px';
    }
    const okProps = { children: 'OK' };
    const cancelProps = { children: 'Cancel' };
    return (
      <Dialog
        title="Add Partitions"
        className="simple-form-dialog"
        style={simpleFormDialog}
        autoFocus={false}
        footerAlign="center"
        onOk={this.onOk}
        onCancel={this.handelDialog}
        onClose={this.handelDialog}
        isFullScreen
        visible={this.state.visible}
        okProps={okProps}
        cancelProps={cancelProps}
      >

        <Dialog
          title="Confirm the number of topics"
          visible={this.state.sureDialog}
          onCancel={this.closeSureDialog}
          onOk={this.sureAddPartition}
          onClose={this.closeSureDialog}
          okProps={okProps}
          cancelProps={cancelProps}
        >
          The number of topic partitions has exceeded 50
        </Dialog>
        <IceFormBinderWrapper
          ref={(form) => {
            this.form = form;
          }}
          value={this.state.formValue}
          onChange={this.onFormChange}
        >
          <div style={styles.formContent}>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                Topic:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder name="topicName">
                  <span style={styles.inputItem}>{this.state.formValue.topicName}</span>
                </IceFormBinder>
                <IceFormError name="topicName" />

              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                Add to Partitions:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="partition"
                  validator={(rule, value, callback) => {
                    const errors = [];
                    if (value <= this.state.partition) {
                        errors.push('Cannot add zero partitions topic');
                    }
                    callback(errors);
                  }}
                >
                  <NumberPicker style={{ width: '100%' }} />
                </IceFormBinder>
                <IceFormError name="partition" />
              </Col>
            </Row>

            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                Brokers:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="partitions"
                  required
                  message="Required"
                >
                  <Select mode="multiple" style={styles.inputItem} dataSource={this.state.brokerList} onChange={this.handelChange} />
                </IceFormBinder>
                <label style={styles.warnLabel}>{this.state.errormess}</label>
                <IceFormError name="partitions" />
              </Col>
            </Row>
          </div>
        </IceFormBinderWrapper>
      </Dialog>
    );
  }
}
const styles = {
  container: {
    margin: '20px',
  },
  loading: {
    width: '100%',
    minHeight: '500px',
  },
  formContent: {
    alignItems: 'center',
  },
  formItem: {
    alignItems: 'center',
    position: 'relative',
    marginTop: 20,
  },
  inputItem: {
    width: '100%',
  },
  simpleFormDialog: { width: '640px' },
  label: {
    textAlign: 'left',
    paddingLeft: '5%',
    lineHeight: '26px',
  },
  warnLabel: { lineHeight: '26px', color: 'red' },
};
