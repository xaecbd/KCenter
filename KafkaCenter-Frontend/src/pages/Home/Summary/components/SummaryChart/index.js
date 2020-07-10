import React, { Component } from 'react';
import { Grid, Loading, Select, Message } from '@alifd/next';
import dayjs from 'dayjs';
import axios from '@utils/axios.js';
import { transToNumer, bytesToSize, formatSizeUnits } from '@utils/dataFormat';
import IceContainer from '@icedesign/container';
import { View } from '@antv/data-set';
import byteIn from '@images/in.svg';
import byteOut from '@images/out.svg';
import msgIn from '@images/mesg.svg';
import { Chart, Geom, Axis, Tooltip, Legend } from 'bizcharts';
import ClusterGroupDatePicker from '../GroupDatePicker';
import ClusterInfo from '../ClusterInfo';

const { Row, Col } = Grid;
const Option = Select.Option;

export default class SummaryChart extends Component {
  constructor(props) {
    super(props);
    this.state = {
      startTime: dayjs().subtract(7, 'days').valueOf(),
      endTime: dayjs().valueOf(),
      interval: '1d',
      data: [],
      isLoading: false,
      metricData: [],
      metricLoading: false,
      contLoading: false,
      mockData: [],
    };
  }

  handleApply = (picker) => {
    const data = {
      start: dayjs(picker.startDate).valueOf(),
      end: dayjs(picker.endDate).valueOf(),
      interval: this.state.interval,
    };
    this.setState({
      startTime: dayjs(picker.startDate).valueOf(),
      endTime: dayjs(picker.endDate).valueOf(),
    });
    this.fetchData(data);
    this.fetchMetric();
  };

  componentDidMount() {
    const data = {
      start: this.state.startTime,
      end: this.state.endTime,
      interval: this.state.interval,
    };
    this.fetchCountData();
    this.fetchData(data);
    this.fetchMetric();
  }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  };

  fetchData = (data) => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios
          .post('/home/cluster/trend', data)
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
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  };

  fetchMetric = () => {
    const data = {
      start: this.state.startTime.valueOf(),
      end: this.state.endTime.valueOf(),
    };
    this.setState(
      {
        metricLoading: true,
      },
      () => {
        axios
          .post('/home/cluster/metric', data)
          .then((response) => {
            const result = response.data.data;
            if (response.data.code === 200) {
              if (this.mounted) {
                this.setState({
                  metricData: result,
                  metricLoading: false,
                });
              }
            } else {
              Message.error(response.data.message);
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

  renderItem = (data) => {
    const resp = [];
    data.map((obj) => {
      const textData = obj.metricName;
      let view = null;
      if (textData === 'MessagesInPerSec') {
        view = msgIn;
      } else if (textData === 'BytesOutPerSec') {
        view = byteOut;
      } else {
        view = byteIn;
      }
      const texts = textData.toString().replace('PerSec', '');
      resp.push({
        text: texts,
        number: formatSizeUnits(obj.value, '', obj),
        circle: {
          width: 36,
          height: 31,
          icon: view,
        },
      });
    });
    return resp.map((data, idx) => {
      const imgStyle = {
        width: `${data.circle.width}px`,
        height: `${data.circle.height}px`,
      };
      return (
        <Col
          xxs={24}
          xs={12}
          l={8}
          key={idx}
          style={styles.statisticalCardItem}
        >
          <div style={styles.circleWrap}>
            <img src={data.circle.icon} style={imgStyle} alt="图片" />
          </div>
          <div style={styles.statisticalCardDesc}>
            <div style={styles.statisticalCardText}>{data.text}</div>
            <div style={styles.statisticalCardNumber}>{data.number}</div>
          </div>
        </Col>
      );
    });
  };

  sortMetric = (data) => {
    const result = [];
    data.forEach((obj) => {
      if (obj.metricName === 'MessagesInPerSec') {
        result.splice(0, 0, obj);
      } else if (obj.metricName === 'BytesInPerSec') {
        result.splice(1, 0, obj);
      } else {
        result.splice(2, 0, obj);
      }
    });
    return result;
  };

  handleChange = (value) => {
    this.setState({
      interval: value,
    });
    const data = {
      start: this.state.startTime.valueOf(),
      end: this.state.endTime.valueOf(),
      interval: value,
    };
    this.fetchData(data);
    this.fetchMetric();
  };

  render() {
    const data = this.state.data;
    let byteOut = [];
    let byteIn = [];
    let messageIn = [];
    Object.keys(data).forEach((key, index) => {
      if (key === 'BytesOutPerSec') {
        byteOut = data[key];
      } else if (key === 'BytesInPerSec') {
        byteIn = data[key];
      } else if (key === 'MessagesInPerSec') {
        messageIn = data[key];
      }
    });
    const byteInDiv = new View().source(byteIn);
    const byteOutDiv = new View().source(byteOut);
    const msgInDiv = new View().source(messageIn);

    const scale = {
      time: {
        type: 'time',
        mask: 'YYYY-MM-DD HH:mm:ss',
        tickCount: 10,
      },
      value: {
        formatter(value) {
          return bytesToSize(value);
        },
      },
    };
    const ByteInscale = {
      time: {
        type: 'time',
        mask: 'YYYY-MM-DD HH:mm:ss',
        tickCount: 10,
      },
      value: {
        formatter(value) {
          return bytesToSize(value);
        },
      },
    };

    const msgScale = {
      time: {
        type: 'time',
        mask: 'YYYY-MM-DD HH:mm:ss',
        tickCount: 10,
      },
      value: {
        formatter(value) {
          return transToNumer(value);
        },
      },
    };

    const custom = (
      <div style={{ marginLeft: '30px' }}>
        <span style={{ marginRight: '5px' }}>Interval:</span>
        <Select
          id="Interval: "
          onChange={this.handleChange}
          value={this.state.interval}
        >
          {' '}
          <Option value="10m">10m</Option>
          <Option value="30m">30m</Option>
          <Option value="1h">1h</Option>
          <Option value="6h">6h</Option>
          <Option value="12h">12h</Option>
          <Option value="1d">1d</Option>
          <Option value="7d">7d</Option>
        </Select>
      </div>
    );
    return (
      <div>
        <Loading visible={this.state.contLoading} style={styles.loading}>
          <ClusterInfo mockDate={this.state.mockData} />
        </Loading>
        <IceContainer>
          <ClusterGroupDatePicker
            onDataChange={this.handleApply}
            custom={custom}
            startTime={dayjs().subtract(7, 'days').valueOf()}
          />
        </IceContainer>
        <Loading visible={this.state.metricLoading} style={styles.loading}>
          <div className="statistical-card" style={styles.statisticalCard}>
            <IceContainer style={styles.statisticalCardItems}>
              <Row wrap style={{ width: '100%' }}>
                {this.renderItem(this.sortMetric(this.state.metricData))}
              </Row>
            </IceContainer>
          </div>
        </Loading>
        <Loading visible={this.state.isLoading} style={styles.loading}>
          {messageIn.length > 0 ? (
            <IceContainer>
              <div>MessageIn</div>
              <Chart height={400} data={msgInDiv} scale={msgScale} forceFit>
                <Axis name="time" />
                <Axis name="value" />
                <Legend />
                <Tooltip crosshairs={{ type: 'y' }} />
                <Geom type="areaStack" position="time*value" color="name" />
                <Geom
                  type="areaStack"
                  position="time*value"
                  size={2}
                  color="name"
                />
              </Chart>
            </IceContainer>
          ) : null}

          {byteIn.length > 0 ? (
            <IceContainer>
              <div>ByteIn</div>
              <Chart height={400} data={byteInDiv} scale={ByteInscale} forceFit>
                <Axis name="time" />
                <Axis name="value" />
                <Legend />
                <Tooltip
                  crosshairs={{
                    type: 'line',
                  }}
                />
                <Geom type="areaStack" position="time*value" color="name" />
                <Geom
                  type="areaStack"
                  position="time*value"
                  size={2}
                  color="name"
                />
              </Chart>
            </IceContainer>
          ) : null}

          {byteOut.length > 0 ? (
            <IceContainer>
              <div>ByteOut</div>
              <Chart height={400} data={byteOutDiv} scale={scale} forceFit>
                <Axis name="time" />
                <Axis name="value" />
                <Legend />
                <Tooltip
                  crosshairs={{
                    type: 'line',
                  }}
                />
                <Geom type="areaStack" position="time*value" color="name" />
                <Geom
                  type="areaStack"
                  position="time*value"
                  size={2}
                  color="name"
                />
              </Chart>
            </IceContainer>
          ) : null}

          <span>Interval：{this.state.interval}</span>
        </Loading>
      </div>
    );
  }
}

const styles = {
  loading: {
    width: '100%',
  },
  statisticalCardItems: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  statisticalCardItem: {
    display: 'flex',
    flexDirection: 'row',
    margin: '10px 0',
  },
  circleWrap: {
    backgroundColor: '#FFECB3',
    width: '70px',
    height: '70px',
    position: 'relative',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: '50%',
    marginRight: '10px',
  },
  statisticalCardDesc: {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
  },
  statisticalCardText: {
    position: 'relative',
    color: '#333333',
    fontSize: '12px',
    fontWeight: 'bold',
    marginBottom: '4px',
  },
};
