import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import { Select, Grid } from '@alifd/next';
import dayjs from 'dayjs';
import {
  Chart,
  Geom,
  Axis,
  Tooltip,
  Legend,
} from 'bizcharts';
import { withRouter } from 'react-router-dom';
import axios from '@utils/axios';
import { thousandSplit } from '@utils/dataFormat';

import GroupDatePicker from '@components/GroupDatePicker';
import './index.scss';

const Option = Select.Option;
const { Row, Col } = Grid;
@withRouter
export default class ConsumerChart extends Component {
  static displayName = 'DoubleAxesChart';

    static propTypes = {};

    static defaultProps = {};

    constructor(props) {
      super(props);
      this.state = {
        metric: [],
        isMetricLoading: false,
        interval: '10m',
        start: '',
        end: '',
        coverInterval: false,
      };
    }

    componentDidMount() {
      const data = {
        type: this.props.match.params.type,
        group: this.props.match.params.group,
        topic: this.props.match.params.topic,
        clusterID: this.props.match.params.id,
        interval: this.state.interval,
      };
      this.fetchMetric(data);
    }

    componentWillMount() {
      this.mounted = true;
    }
    componentWillUnmount = () => {
      this.mounted = false;
    }

    fetchMetric = (data) => {
      this.setState({
        requestData: data,
        isMetricLoading: true,
      }, () => {
        axios.post('/monitor/topic/consumer_offsets/chart/interval', data).then((response) => {
          if (this.mounted) {
            if (response.data.code === 200) {
              if (response.data.data) {
                this.setState({
                  metric: response.data.data,
                  isMetricLoading: false,
                }, () => {

                });
              } else {
                this.setState({
                  isMetricLoading: false,
                });
              }
            }
          }
        }).catch((error) => {
          console.error(error);
        });
      });
    }

    refreshData = () => {
      if (!this.state.isMetricLoading) {
        this.fetchMetric(this.state.requestData);
      }
    }

    onTimeInterval = (value) => {
      this.setState({
        interval: value,
        coverInterval: true,
      });
      const data = {
        type: this.props.match.params.type,
        group: this.props.match.params.group,
        topic: this.props.match.params.topic,
        clusterID: this.props.match.params.id,
        start: this.state.start,
        end: this.state.end,
        interval: value,
      };

      this.fetchMetric(data);
    }

    onDataChange = (picker) => {
      const data = {
        type: this.props.match.params.type,
        group: this.props.match.params.group,
        topic: this.props.match.params.topic,
        clusterID: this.props.match.params.id,
        start: dayjs(picker.startDate).valueOf(),
        end: dayjs(picker.endDate).valueOf(),
        interval: this.state.interval,
      };
      this.setState({
        start: dayjs(picker.startDate).valueOf(),
        end: dayjs(picker.endDate).valueOf(),
      });
      if (this.state.coverInterval === false) {
        if ((data.end - data.start) <= 1 * 60 * 60 * 1000) {
          data.interval = '10m';
          this.setState({
            interval: '10m',
          });
        }
        if ((data.end - data.start) <= 6 * 60 * 60 * 1000 && (data.end - data.start) > 1 * 60 * 60 * 1000) {
          data.interval = '30m';
          this.setState({
            interval: '30m',
          });
        }
        if ((data.end - data.start) <= 24 * 60 * 60 * 1000 && (data.end - data.start) > 6 * 60 * 60 * 1000) {
          data.interval = '3h';
          this.setState({
            interval: '3h',
          });
        }
        if ((data.end - data.start) <= 72 * 60 * 60 * 1000 && (data.end - data.start) > 24 * 60 * 60 * 1000) {
          data.interval = '12h';
          this.setState({
            interval: '12h',
          });
        }
        if ((data.end - data.start) > 72 * 60 * 60 * 1000) {
          data.interval = '1d';
          this.setState({
            interval: '1d',
          });
        }
      }


      this.fetchMetric(data);
    }

    handleChange = (value) => {
      this.onTimeInterval(value);
    }

    render() {
      const cols = {
        offset: {
          formatter(offset) {
            return thousandSplit(offset);
          },

        },
        lag: {
          formatter(lag) {
            return thousandSplit(lag);
          },
        },
      };
      const data = this.state.metric;
      const resData = [];
      data.map((obj) => {
        const timestamp = obj.timestamp;
        const time = dayjs(timestamp).format('MM/DD HH:mm:ss');
        resData.push({
          timestamp: time,
          offset: obj.offset,
          lag: obj.lag,
        });
      });


      const custom = (<Select id="basic-demo" onChange={this.handleChange} defaultValue="Time Interval" showSearch ><Option value="5m">5m</Option><Option value="10m">10m</Option>
        <Option value="30m">30m</Option>
        <Option value="1h">1h</Option>
        <Option value="6h">6h</Option>
        <Option value="12h">12h</Option>
        <Option value="1d">1d</Option>
        <Option value="7d">7d</Option>
                      </Select>);

      const recordView = (
        <Row style={{ width: '56%' }}>
          <Col span="2">
            <div style={styles.label}>Topic:</div>
          </Col>
          <Col span="10">
            <div style={styles.text}>{this.props.match.params.topic}</div>
          </Col>
          <Col span="2">
            <div style={styles.label}>Group: </div>
          </Col>
          <Col span="10">
            <div style={styles.text}>{this.props.match.params.group}</div>
          </Col>
        </Row>);

      let chartIns = null;
      return (
        <div style={{ minHeight:'600px'}}>
          <GroupDatePicker onDataChange={this.onDataChange} refreshData={this.refreshData} record={recordView} custom={custom} startTime={dayjs().subtract(1, 'hour').valueOf()} />
          <IceContainer>
            <Chart
              height={400}
              scale={cols}
              forceFit
              data={resData}
              onGetG2Instance={(chart) => {
                chartIns = chart;
              }}
              padding={{ left: 100, top: 20, right: 100, bottom: 80 }}
            >
              <Legend />
              <Axis
                name="offset"
                title
                position="left"
                grid={null}
                label={{
                  textStyle: {
                    fill: '#3182bd',
                  },
                  autoRotate: false,
                }}
              />
              <Axis name="lag"
                title
                position="right"
                label={{
                  textStyle: {
                    fill: '#fdae6b',
                  },
                  autoRotate: false,
                }}
              />
              <Tooltip crosshairs={{
              type: 'y',
            }}
              />
              <Geom type="interval" position="timestamp*offset" color="#3182bd" />
              <Geom type="line" position="timestamp*lag" color="#fdae6b" size={3} shape="smooth" />
            </Chart>
            <span>Interval：{this.state.interval}</span>
          </IceContainer>
        </div>
      );
    }
}

const styles = {
  label: {
    textAlign: 'right',
    marginRight: '10px',
    fontWeight: 'bold',
  },
  text: {
    whiteSpace: 'nowrap',
  },
};
