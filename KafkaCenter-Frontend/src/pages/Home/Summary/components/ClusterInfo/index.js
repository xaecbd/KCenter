import React, { Component } from 'react';
import { Grid } from '@alifd/next';
import IceContainer from '@icedesign/container';
import box from '@images/box.jpg';

const { Row, Col } = Grid;
export default class index extends Component {
  render() {
    return (
      <IceContainer >
        <Row wrap gutter="20" style={{ justifyContent: 'center' }}>
          <Col l="4">
            <div style={styles.item}>
              <img src={box} alt="" />
            </div>
          </Col>
          <Col l="4">
            <div style={styles.item}>
              <p style={styles.itemTitle}>Cluster</p>
              <p style={styles.itemValue}>{this.props.mockDate.clusterSize}</p>
            </div>
          </Col>
          <Col l="4">
            <div style={styles.item}>
              <p style={styles.itemTitle}>Broker</p>
              <p style={styles.itemValue}>{this.props.mockDate.brokerSize}</p>
            </div>
          </Col>
          <Col l="4">
            <div style={styles.item}>
              <p style={styles.itemTitle}>Topics</p>
              <p style={styles.itemValue}>{this.props.mockDate.topicSize}</p>
            </div>
          </Col>
          <Col l="4">
            <div style={styles.item}>
              <p style={styles.itemTitle}>Groups</p>
              <p style={styles.itemValue}>{this.props.mockDate.groupSize}</p>
            </div>
          </Col>
          <Col l="4">
            <div style={styles.item}>
              <p style={styles.itemTitle}>Alert</p>
              <p style={styles.itemValue}>{this.props.mockDate.alertSize}</p>
            </div>
          </Col>
        </Row>
      </IceContainer>
    );
  }
}

const styles = {
  container: {
    margin: '20px',
  },
  loading: {
    width: '100%',
  },
  head: {
    position: 'relative',
    padding: '16px 16px 8px',
    borderBottom: '1px solid #e9e9e9',
  },
  title: {
    margin: '0 0 5px',
    width: '90%',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
    fontSize: '16px',
    fontWeight: '500',
    color: 'rgba(0,0,0,.85)',
  },
  body: {
    position: 'relative',
    padding: '16px',
  },
  formItems: {
    alignItems: 'center',
    marginTop: 0,
  },
  formItem: {
    alignItems: 'center',
    position: 'relative',
  },
  value: {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    width: '60%',
    color: '#c6cad6',
  },
  item: {
    height: '120px',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
  },
  itemTitle: {
    color: '#697b8c',
    fontSize: '14px',
  },
  itemTitles: {
    color: '#697b8c',
    fontSize: '14px',
    marginTop: '10px',
  },
  itemValue: {
    color: '#314659',
    fontSize: '36px',
    marginTop: '10px',
  },
};
