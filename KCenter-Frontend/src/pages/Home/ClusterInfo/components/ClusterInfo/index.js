import React, { Component } from 'react';
import {
  Grid,
  Loading,
  Message,
} from '@alifd/next';
import axios from '@utils/axios';
import IceContainer from '@icedesign/container';

import { withRouter } from 'react-router-dom';
import cluster from '@images/cluster.jpg';
import broker from '@images/broken_line.jpg';
import version from '@images/Version.jpg';
import control from '@images/controller.jpg';
import box from '@images/box.jpg';
import chart from '@images/chart.jpg';
import TopAnalyze from '../../../TopAnalyze';


const { Row, Col } = Grid;

@withRouter
export default class ClusterInfo extends Component {
  state = {
    data: [],
    isLoading: false,
    contLoading: false,
    mockData: [],
  };

  componentDidMount() {

  }
  componentWillMount() {
    this.mounted = true;
    this.fetchData();
    this.fetchCountData();
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  fetchData = () => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios
          .get('/home/page/cluster_statis')
          .then((response) => {
            const result = response.data.data;
            if (response.data.code === 200) {
              if (this.mounted) {
                this.setState({
                  data: result,
                  isLoading: false,
                });
              }
            } else {
              Message.error(response.data.message);
            }
            if (this.mounted) {
              this.setState({
                isLoading: false,
              });
            }
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  };

  fetchCountData = () => {
    this.setState(
      {
        contLoading: true,
      },
      () => {
        axios
          .get('/home/page/cluster_info')
          .then((response) => {
            const result = response.data.data;
            if (response.data.code === 200) {
              if (this.mounted) {
                this.setState({
                  mockData: result,
                  contLoading: false,
                });
              }
            } else {
              Message.error(response.data.message);
            }
            if (this.mounted) {
              this.setState({
                contLoading: false,
              });
            }
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  };
  onDetail = (item) => {
    this.props.history.push(`/home/detail/${item.id}`);
  }
  handleCluster = () => {
    this.props.history.push('/home/cluster');
  }

  render() {
    const data = this.state.data;
    const mockDate = this.state.mockData;
    return (
      <div style={styles.container}>

        <IceContainer style={styles.container}>
          <Loading visible={this.state.contLoading} style={styles.loading}>
            <Row wrap gutter="20" style={{ justifyContent: 'center' }}>
              <Col l="3">
                <div style={styles.item}>
                  <img src={box} alt="" />
                </div>
              </Col>
              <Col l="3">
                <div style={styles.item}>
                  <p style={styles.itemTitle}>Cluster</p>
                  <p style={styles.itemValue}>{mockDate.clusterSize}</p>
                </div>
              </Col>
              <Col l="3">
                <div style={styles.item}>
                  <p style={styles.itemTitle}>Broker</p>
                  <p style={styles.itemValue}>{mockDate.brokerSize}</p>
                </div>
              </Col>
              <Col l="3">
                <div style={styles.item}>
                  <p style={styles.itemTitle}>Topics</p>
                  <p style={styles.itemValue}>{mockDate.topicSize}</p>
                </div>
              </Col>
              <Col l="3" >
                <div style={styles.item}>
                  <p style={styles.itemTitle}>Groups</p>
                  <p style={styles.itemValue}>{mockDate.groupSize}</p>
                </div>
              </Col>
              <Col l="3">
                <div style={styles.item}>
                  <p style={styles.itemTitle}>Alert</p>
                  <p style={styles.itemValue}>{mockDate.alertSize}</p>
                </div>
              </Col>
              <Col l="3">
                <div style={styles.item}>
                  <p style={styles.itemTitles} >Summary</p>
                  <img src={chart} alt="" onClick={e => this.handleCluster()} style={{ cursor: 'pointer' }} />
                </div>
              </Col>
            </Row>
          </Loading>
        </IceContainer>


        <div style={styles.container}>
          <TopAnalyze/>
       
        </div>

      </div>
    );
  }
}

const styles = {
  container: {
    // margin: '20px',
    marginTop:'5px',
    padding:'0 !important',
  },
  loading: {
    width: '100%',
  },
  head: {
    position: 'relative',
    // padding: '16px 16px 8px',
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
    // padding: '16px',
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
