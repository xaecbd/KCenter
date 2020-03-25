/* eslint-disable react/jsx-closing-tag-location */
import React, { Component } from 'react';
import { Dialog } from '@alifd/next';
import IceContainer from '@icedesign/container';

export default class DetailDialog extends Component {
  static displayName = 'DetailDialog';
  constructor(props) {
    super(props);
    this.state = {
      dialogObj: this.props.dialogObj,
    };
  }
  // 接受父类props改变，修改子类中的属性
  componentWillReceiveProps(nextProps) {
    this.setState({
      dialogObj: nextProps.dialogObj,
    });
  }


  hideDetailDialog = () => {
    this.props.hideDetailDialog();
  }
  onOk = () => {
    this.hideDetailDialog();
  };

  render() {
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
        onOk={this.onOk}
        onCancel={this.hideDetailDialog}
        onClose={this.hideDetailDialog}
        isFullScreen
        okProps={okProps}
        cancelProps={cancelProps}
        visible={this.state.dialogObj.visible}
      >

        <div className="detail-table">
          <IceContainer title="Topic Task">
            <ul style={styles.detailTable}>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Topic Name:</div>
                <div style={styles.detailBody}>{this.state.dialogObj.record.topicName}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Location:</div>
                <div style={styles.detailBody}>{this.state.dialogObj.record.location}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Kakfka Cluster:</div>
                <div style={styles.detailBody}>{this.state.dialogObj.record.clusterNames}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Owner:</div>
                <div style={styles.detailBody}>{this.state.dialogObj.record.owner ? this.state.dialogObj.record.owner.name : ''}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Team:</div>
                <div style={styles.detailBody}>{this.state.dialogObj.record.team ? this.state.dialogObj.record.team.name : ''}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Message Rate:</div>
                <div style={styles.detailBody}>
                  {this.state.dialogObj.record.messageRate}
                </div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>TTL:</div>
                <div style={styles.detailBody}>{this.state.dialogObj.record.ttl}&nbsp;(hour)</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Partitions:</div>
                <div style={styles.detailBody}>
                  {this.state.dialogObj.record.partition}
                </div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Replication Factor:</div>
                <div style={styles.detailBody}>
                  {this.state.dialogObj.record.replication}
                </div>
              </li>

              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Comments:</div>
                <div style={styles.detailBody}>
                  {this.state.dialogObj.record.comments}
                </div>
              </li>

              {this.state.dialogObj.record.approvalOpinions !== '' ? <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Approval Opinion:</div>
                <div style={styles.detailBody}>
                  {this.state.dialogObj.record.approvalOpinions}
                </div>
              </li> : ''}
            </ul>
          </IceContainer>
        </div>
      </Dialog>
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
};
