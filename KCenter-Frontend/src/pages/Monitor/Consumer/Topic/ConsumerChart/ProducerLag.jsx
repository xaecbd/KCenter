import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import DataSet from '@antv/data-set';
import { Grid } from '@alifd/next';
import dayjs from 'dayjs';
import { Chart, Geom, Axis, Tooltip, Legend } from 'bizcharts';
import { withRouter } from 'react-router-dom';
import axios from '@utils/axios';
import { thousandSplit } from '@utils/dataFormat';


import ProducerGroupDatePicker from '@components/GroupDatePicker';
import './index.scss';

const { Row, Col } = Grid;
@withRouter
export default class ConsumerGroupChart extends Component {
  static propTypes = {};
  static defaultProps = {};

  constructor(props) {
    super(props);
    this.state = {
      metricPCL: [],
      isMetricLoading: false,
    };
  }

  componentDidMount() {
    const data = {
      type: this.props.match.params.type,
      group: this.props.match.params.group,
      topic: this.props.match.params.topic,
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
        requestData: data,
        isMetricLoading: true,
      },
      () => {
        axios
          .post('/monitor/topic/consumer_offsets/chart', data)
          .then((response) => {
            if (response.data.code === 200) {
              if (this.mounted) {
                if (response.data.data) {
                  this.setState({
                    metricPCL: response.data.data,
                    isMetricLoading: false,
                  });
                } else {
                  this.setState({
                    isMetricLoading: false,
                  });
                }
              }
            }
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  };

  refreshData = () => {
    if (!this.state.isMetricLoading) {
      this.fetchMetric(this.state.requestData);
    }
  };

  onDataChange = (picker) => {
    const data = {
      type: this.props.match.params.type,
      group: this.props.match.params.group,
      topic: this.props.match.params.topic,
      clusterID: this.props.match.params.id,
      start: dayjs(picker.startDate).valueOf(),
      end: dayjs(picker.endDate).valueOf(),
    };
    this.fetchMetric(data);
  };
  render() {
    const scale = {
      timestamp: {
        type: 'time',
        mask: 'MM/DD HH:mm',
      },
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

    const ds = new DataSet();
    const dv = ds.createView().source(this.state.metricPCL);
    dv.transform({
      type: 'fold',
      fields: ['logSize', 'offset'],
      // 展开字段集
      key: 'offsetType',
      // key字段
      value: 'offset', // value字段
    });

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
      </Row>
    );
    return (
      <div className="chart-type-line" style={{ minHeight: '600px' }}>
        <ProducerGroupDatePicker
          onDataChange={this.onDataChange}
          refreshData={this.refreshData}
          record={recordView}
          startTime={dayjs().subtract(1, 'hours').valueOf()}
        />
        <IceContainer>
          <Chart
            height={400}
            data={dv}
            scale={scale}
            forceFit
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
                  textAlign: 'end',
                },
                autoRotate: false,
              }}
            />
            <Axis
              name="lag"
              title
              position="right"
              label={{
                textStyle: {
                  fill: '#ff3333',
                },
                autoRotate: false,
              }}
            />
            <Tooltip />

            <Geom
              type="line"
              position="timestamp*offset"
              size={3}
              shape="circle"
              color="offsetType"
            />

            <Geom
              type="line"
              position="timestamp*lag"
              size={3}
              shape="smooth"
              color="#ff3333"
            />
          </Chart>
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
