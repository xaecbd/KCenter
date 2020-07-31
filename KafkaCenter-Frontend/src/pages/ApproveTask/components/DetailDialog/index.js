import React, { Component } from 'react';
import { Dialog } from '@alifd/next';
import IceContainer from '@icedesign/container';

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

export default class DetailDialog extends Component {
  static displayName = 'Dialog';

  state = {
    detailObj: this.props.detail,
  };

  constructor(props) {
    super(props);
  }


  // 接受父类props改变，修改子类中的属性
  componentWillReceiveProps(nextProps) {
    this.setState({
      detailObj: nextProps.detail,
    });
  }

handelDetailDialog = () => {
  const data = this.state.detailObj;
  data.viewVisable = !this.state.detailObj.viewVisable;
  this.setState({
    detailObj: data,
  });
}

render() {
  const simpleFormDialog = {
    ...styles.simpleFormDialog,
  };
  const okProps = { children: 'OK' };
  const cancelProps = { children: 'Cancel' };

  let approveName = this.state.detailObj.data.approved===0?'':'admin';
  if(this.state.detailObj.data.approve ){
    approveName = this.state.detailObj.data.approve.name;
  }
  return (
    <div>
      <Dialog
        className="simple-form-dialog"
        style={simpleFormDialog}
        autoFocus={false}
        footerAlign="center"
        onCancel={this.handelDetailDialog}
        onClose={this.handelDetailDialog}
        onOk={this.handelDetailDialog}
        isFullScreen
        visible={this.state.detailObj.viewVisable}
        okProps={okProps}
        cancelProps={cancelProps}
      >
        <div className="detail-table">
          <IceContainer title="Task Info">
            <ul style={styles.detailTable}>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Topic Name:</div>
                <div style={styles.detailBody}>{this.state.detailObj.data.topicName}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Location:</div>
                <div style={styles.detailBody}>{this.state.detailObj.data.location}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Kafka Cluster:</div>
                <div style={styles.detailBody}>{this.state.detailObj.data.clusterNames}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Message Rate:</div>
                <div style={styles.detailBody}>{this.state.detailObj.data.messageRate}&nbsp;(message/s)</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>TTL:</div>
                <div style={styles.detailBody}>{this.state.detailObj.data.ttl}&nbsp;(hour)</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Partitions:</div>
                <div style={styles.detailBody}>
                  {this.state.detailObj.data.partition}
                </div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Replication Factor:</div>
                <div style={styles.detailBody}>
                  {this.state.detailObj.data.replication}
                </div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Owner:</div>
                <div style={styles.detailBody}>{this.state.detailObj.data.owner ? this.state.detailObj.data.owner.name : ''}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Team:</div>
                <div style={styles.detailBody}>{this.state.detailObj.data.team ? this.state.detailObj.data.team.name : ''}</div>
              </li>
              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>By Approved:</div>
                <div style={styles.detailBody}>{approveName}</div>
              </li>

              <li style={styles.detailItem}>
                <div style={styles.detailTitle}>Comments:</div>
                <div style={styles.detailBody}>
                  {this.state.detailObj.data.comments}
                </div>
              </li>
            </ul>
          </IceContainer>

        </div>
      </Dialog>
    </div>
  );
}
}

