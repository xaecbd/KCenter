import React, { Component } from 'react';
import { Dialog } from '@alifd/next';
import IceContainer from '@icedesign/container';
import { transToHours } from '@utils/dataFormat';

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
            visible={this.state.dialogObj.visible}
            okProps={okProps}
            cancelProps={cancelProps}
          >

            <div className="detail-table">
              <IceContainer title="Topic Detail">
                <ul style={styles.detailTable}>
                  <li style={styles.detailItem}>
                    <div style={styles.detailTitle}>Topic Name:</div>
                    <div style={styles.detailBody}>{this.state.dialogObj.record.topicName}</div>
                  </li>
                  <li style={styles.detailItem}>
                    <div style={styles.detailTitle}>Kafka Cluster:</div>
                    <div style={styles.detailBody}>{this.state.dialogObj.record.cluster ? this.state.dialogObj.record.cluster.name : ''}</div>
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
                    <div style={styles.detailTitle}>TTL:</div>
                    <div style={styles.detailBody}>
                      {transToHours(this.state.dialogObj.record.ttl)}&nbsp;(hour)
                    </div>
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
};
