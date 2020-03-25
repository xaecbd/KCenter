import React, { Component } from 'react';
import { Dialog, Button, Message, Input, Select, NumberPicker, Loading } from '@alifd/next';
import IceContainer from '@icedesign/container';
import axios from '@utils/axios';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
} from '@icedesign/form-binder';

export default class CheckDialog extends Component {
  static displayName = 'Dialog';
  constructor(props) {
    super(props);
    this.state = {
      dialogObj: this.props.dialogObj,
      approvalCommentsVisible: false,
      approvalComments: '',
      clusterData: [],
      selectmode: 'single',
      selectValue: '',
      errormess: '',
      approvemess: '',
      approvalLoading: false,
      formValue: this.props.dialogObj.record,
      RejectLoading: false,
    };
  }

  selectValue = (value) => {
    if (value !== '') {
      this.setState({
        errormess: '',
      });
    }
    this.setState({
      selectValue: value,
    });
  }
  // 接受父类props改变，修改子类中的属性
  componentWillReceiveProps(nextProps) {
    const tempList = [];
    this.setState({
      dialogObj: nextProps.dialogObj,
      formValue: nextProps.dialogObj.record,
    });
    if (nextProps.dialogObj.record.location === 'ALL') {
      this.setState({
        selectmode: 'multiple',
      });
    } else {
      this.setState({
        selectmode: 'single',
      });
    }
    if (nextProps.dialogObj.record.location) {
      axios.get(`/cluster/by_location/${nextProps.dialogObj.record.location}`)
        .then((response) => {
          if (response.data.code === 200) {
            response.data.data.forEach((cluster) => {
              tempList.push({ value: cluster.id, label: cluster.name });
            });
            this.setState({
              clusterData: tempList,
            });
          } else {
            Message.error(response.data.message);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    }
  }
  handelDialog = () => {
    this.setState({
      errormess: '',
      selectValue: '',
      approvalLoading: false,
      RejectLoading: false,
    });
    this.props.handelDialog();
  }

  onApproveLoading = () => {
    this.setState({
      approvalLoading: !this.state.approvalLoading,
    });
  }

  onApprove = () => {
    if (this.state.selectValue.length === 0) {
      this.setState({
        errormess: 'cluster is required',
      });
    } else {
      this.setState({
        approvalLoading: true,
      },
      () => {
        const data = {};
        data.partition = this.state.formValue.partition;
        data.replication = this.state.formValue.replication;
        data.clusterId = this.state.selectValue.toString().replace('[').replace(']');
        data.id = this.state.dialogObj.record.id;
        axios.post('topic/task/approve', data).then((response) => {
          if (response.data.code === 200) {
            this.handelDialog();
            Message.success(response.data.message);
          } else {
            Message.error({
              title: response.data.message,
              duration: 6000,
            });
            this.onApproveLoading();
          }
        }).catch(() => {
          this.onApproveLoading();
          Message.error('Task approve has error.');
        });
      });
    }
  };

  onReject = () => {
    this.setState({
      approvalCommentsVisible: true,
    });
  };

  onCloseApprovalComments = () => {
    this.setState({
      approvalCommentsVisible: false,
      approvemess: '',
    });
  };


  /** 把审批意见追加到task */
  onSureReject = () => {
    if (this.state.approvalComments.trim() === '') {
      this.setState({
        approvemess: 'approve opinions is must',
      });
    } else {
      this.setState(
        {
          approvalCommentsVisible: false,
          approvalLoading: true,
          RejectLoading: true,
        },
        () => {
          axios.get(`/topic/task/reject/${this.state.dialogObj.record.id}/${this.state.approvalComments}`).then((response) => {
            if (response.data.code === 200) {
              Message.success(response.data.message);
              this.setState({
                approvalComments: '',
              });
              this.handelDialog();
            } else {
              Message.error(response.data.message);
            }
          }).catch(() => {
            Message.error('Task Reject has error.');
          });
        }
      );
    }
  };

  onChangeComments = (value) => {
    if (value.trim() !== '') {
      this.setState({
        approvemess: '',
      });
    } else {
      this.setState({
        approvemess: 'approve opinions is must',
      });
    }
    this.setState({
      approvalComments: value,
    });
  };

  render() {
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };
    const okProps = { children: 'OK' };
    const cancelProps = { children: 'Cancel' };

    return (
      <div>
        <Dialog
          title="Please fill in comments"
          visible={this.state.approvalCommentsVisible}
          footer={<div><Button warning type="primary" onClick={this.onSureReject}>Reject</Button> &nbsp;&nbsp; <Button type="primary" onClick={this.onCloseApprovalComments}>Cancel</Button></div>}
          onOk={this.onSureReject}
          onCancel={this.onCloseApprovalComments}
          onClose={this.onCloseApprovalComments}
        >
          <Loading visible={this.state.RejectLoading} style={{ width: '100%' }}>
            <Input.TextArea placeholder="Enter reject comments" rows={4} onChange={this.onChangeComments} />
            <div style={styles.errormess}>{this.state.approvemess}</div>
          </Loading>
        </Dialog>

        <Dialog
          className="simple-form-dialog"
          style={simpleFormDialog}
          autoFocus={false}
          footer={<div><Button type="primary" onClick={this.onApprove}>Approve</Button> &nbsp;&nbsp;<Button warning type="normal" onClick={this.onReject}>Reject</Button></div>}
          footerAlign="center"
          onCancel={this.handelDialog}
          onClose={this.handelDialog}
          isFullScreen
          visible={this.state.dialogObj.visible}
        >
          <IceFormBinderWrapper
            value={this.state.formValue}
            onChange={this.onFormChange}
          >
            <div className="detail-table">
              <IceContainer title="Topic Task">
                <Loading visible={this.state.approvalLoading} style={{ width: '100%' }}>
                  <ul style={styles.detailTable}>
                    <li style={styles.detailItem}>
                      <div style={styles.detailTitle}>Topic Name:</div>
                      <div style={styles.detailBody}>{this.state.dialogObj.record.topicName}</div>
                      <div style={styles.detailTitle}>Location:</div>
                      <div style={styles.detailBody}>{this.state.dialogObj.record.location}</div>
                    </li>
                    <li style={styles.detailItem}>
                      <div style={styles.detailTitle}>Kafka Cluster:</div>
                      <div style={styles.detailBody}>
                        <Select
                          showSearch
                          dataSource={this.state.clusterData}
                          placeholder="please select cluster"
                          style={{ width: '50%' }}
                          mode={this.state.selectmode}
                          onChange={this.selectValue}
                        />
                        <div style={styles.errormess}>{this.state.errormess}</div>
                      </div>
                    </li>
                    <li style={styles.detailItem}>
                      <div style={styles.detailTitle}>Message Rate:</div>
                      <div style={styles.detailBody}>{this.state.dialogObj.record.messageRate}&nbsp;(message/s)</div>
                    </li>
                    <li style={styles.detailItem}>
                      <div style={styles.detailTitle}>TTL:</div>
                      <div style={styles.detailBody}>{this.state.dialogObj.record.ttl}&nbsp;(hour)</div>
                    </li>
                    <li style={styles.detailItem}>
                      <div style={styles.detailTitle}>Owner:</div>
                      <div style={styles.detailBody}>{this.state.dialogObj.record.owner ? this.state.dialogObj.record.owner.name : ''}</div>
                      <div style={styles.detailTitle}>Team:</div>
                      <div style={styles.detailBody}>{this.state.dialogObj.record.team ? this.state.dialogObj.record.team.name : ''}</div>
                    </li>
                    <li style={styles.detailItem}>
                      <div style={styles.detailTitle}>Partitions:</div>
                      <div style={styles.detailBody}>
                        <IceFormBinder name="partition" >
                          <NumberPicker />
                        </IceFormBinder>
                      </div>
                      <div style={styles.detailTitle}>Replication Factor:</div>
                      <div style={styles.detailBody}>
                        <IceFormBinder name="replication" >
                          <NumberPicker />
                        </IceFormBinder>
                      </div>
                    </li>
                    <li style={styles.detailItem}>
                      <div style={styles.detailTitle}>Comments:</div>
                      <div style={styles.detailBody}>
                        {this.state.dialogObj.record.comments}
                      </div>
                    </li>
                  </ul>
                </Loading>
              </IceContainer>
            </div>
          </IceFormBinderWrapper>
        </Dialog>
      </div>
    );
  }
}
const styles = {
  simpleFormDialog: { width: '640px' },
  detailItem: {
    padding: '15px 0px',
    display: 'flex',
    borderTop: '1px solid #EEEFF3',
  },
  detailTitle: {
    marginRight: '30px',
    textAlign: 'right',
    width: '120px',
    color: '#999999',
  },
  detailBody: {
    flex: 1,
  },
  errormess: {
    color: 'red',
  },
};
