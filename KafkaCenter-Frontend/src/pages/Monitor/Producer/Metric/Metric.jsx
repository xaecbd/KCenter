import React, { Component } from 'react';
import { Table, Loading, Button } from '@alifd/next';
import { withRouter } from 'react-router-dom';
import axios from '@utils/axios';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import IceContainer from '@icedesign/container';
import { formatSizeUnits } from '@utils/dataFormat';
import FoundationSymbol from '@icedesign/foundation-symbol';


import './Metric.scss';

@withRouter
export default class Metric extends Component {
  constructor(props) {
    super(props);
    this.state = {
      metric: [],
      clusterName: this.props.match.params.clusterName,
    };
  }
  componentDidMount() {
    const data = {
      topic: this.props.match.params.topicName,
      clusterID: this.props.match.params.id,
    };
    this.fetchMetric(data);
  }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  };

  fetchMetric = (data) => {
    this.setState(
      {
        isMetricLoading: true,
      },
      () => {
        axios.defaults.timeout = 180000;
        axios
          .post('/monitor/topic/consumer_offsets/topic_metric', data)
          .then((response) => {
            if (response.data.code === 200) {
              if (response.data.data) {
                if (this.mounted) {
                  this.setState({
                    metric: response.data.data,
                    isMetricLoading: false,
                  });
                }
              } else {
                this.setState({
                  isMetricLoading: false,
                });
              }
            }
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  };


  backward = () => {
    window.location.href = '#/monitor/producer';
  };
  handleRefresh = () => {
    const data = {
      topic: this.props.match.params.topicName,
      clusterID: this.props.match.params.id,
    };

    this.fetchMetric(data);
    // this.fetchDetail(data);
  };

  handleConsumer= () => {
    this.props.history.push(`/monitor/consumer/topic/consumer_offsets/${this.props.match.params.id}/${this.state.clusterName}/${this.props.match.params.topicName}`);
  }

  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Monitor',
      },
      {
        link: '#/monitor/producer',
        text: 'Producer',
      },
    ];
    return (
      <div className="contain">

        <CustomBreadcrumb items={breadcrumb} title="Producer Metric" />
        <IceContainer style={styles.container} >
          <Loading visible={this.state.isMetricLoading} style={styles.loading}>
            <div style={styles.listTitle}>
              <FoundationSymbol
                onClick={() => this.backward()}
                style={styles.backward}
                size="large"
                type="backward"
              />
            cluster:&nbsp;<span style={styles.listTitles}>{this.state.clusterName}</span>&nbsp;&nbsp;topic:&nbsp;<span style={styles.listTitles}>{this.props.match.params.topicName}

            </span>
            </div>

            <div style={{ width: '100%', height: '5px', margin: '0px 0px 28px 0' }}>
              <Button
                type="secondary"
                style={{ float: 'right' }}
                onClick={() => {
                this.handleRefresh();
              }}
              >
                Refresh&nbsp;&nbsp;
                <FoundationSymbol size="small" type="exchange" />
              </Button>

              <Button
                type="secondary"
                style={{float: ' right', marginRight: '7px'}}
                onClick={() => this.handleConsumer()}
              >
                Consumer&nbsp;&nbsp;
                <FoundationSymbol size="small" type="link" />
              </Button>
            </div>

            <div
              className="table"
            >
              <Table dataSource={this.state.metric}>
                <Table.Column title="Rate" dataIndex="metricName" />
                <Table.Column
                  title="Mean"
                  dataIndex="meanRate"
                  cell={formatSizeUnits}
                />
                <Table.Column
                  title="1 min"
                  dataIndex="oneMinuteRate"
                  cell={formatSizeUnits}
                />
                <Table.Column
                  title="5 min"
                  dataIndex="fiveMinuteRate"
                  cell={formatSizeUnits}
                />
                <Table.Column
                  title="15 min"
                  dataIndex="fifteenMinuteRate"
                  cell={formatSizeUnits}
                />
              </Table>
            </div>
          </Loading>
        </IceContainer>
      </div>
    );
  }
}

const styles = {
  listTitle: {
    marginBottom: '10px',
    fontSize: '30px',
  },
  listTitles: {
    marginBottom: '10px',
    fontSize: '30px',
    fontWeight: 'bold',
  },
  metricTitle: {
    marginBottom: '10px',
    fontSize: '18px',
    marginTop: '10px',
  },
  loading: {
    width: '100%',
  },
  backward: {
    display: 'inline-block',
    minWidth: '40px',
    marginBottom: '15px',
    cursor: 'pointer',
    color: '#0066FF',
  },
  container: {
    //  margin: '20px',
    minHeight: '600px',
    padding: '10px 20px 20px',
  },
  link: {
    margin: '0 5px',
    color: 'rgba(49, 128, 253, 0.65)',
    cursor: 'pointer',
    textDecoration: 'none',
  },
};
