import React, { Component } from 'react';
import { Loading } from '@alifd/next';
import axios from '@utils/axios.js';
import dayjs from 'dayjs';
import { transToNumer, bytesToSize, sortData } from '@utils/dataFormat';
import IceContainer from '@icedesign/container';
import { View } from '@antv/data-set';
import { Chart, Geom, Axis, Tooltip, Legend } from 'bizcharts';
import ClusterGroupDatePicker from '@components/GroupDatePicker';

export default class MonitorChart extends Component {
  constructor(props) {
    super(props);
    this.state = {
      startTime: dayjs().subtract(1, 'days'),
      endTime: dayjs(),
      interval: '5m',
      data: [],
      cid: this.props.cid,
      isLoading: false,
    };
  }

  handleApply = (picker) => {
    const data = {
      start: dayjs(picker.startDate).valueOf(),
      end: dayjs(picker.endDate).valueOf(),
      clientId: this.state.cid,
      interval: this.state.interval,
    };
    this.setState({
      startTime: dayjs(picker.startDate).valueOf(),
      endTime: dayjs(picker.endDate).valueOf(),
    });
    this.fetchData(data);
  };

  componentWillMount() {
    this.mounted = true;
    const data = {
      start: this.state.startTime.valueOf(),
      end: this.state.endTime.valueOf(),
      clientId: this.state.cid,
      interval: this.state.interval,
    };
    this.fetchData(data);
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
          .post('/home/detail/trend', data)
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

  render() {
    const data = this.state.data;
    let byteOut = [];
    let byteIn = [];
    let messageIn = [];
    Object.keys(data).forEach((key, index) => {
      if (key === 'BytesOutPerSec') {
        byteOut = sortData(data[key], 'broker');
      } else if (key === 'BytesInPerSec') {
        byteIn = sortData(data[key], 'broker');
      } else if (key === 'MessagesInPerSec') {
        messageIn = sortData(data[key], 'broker');
      }
    });
    const byteInDiv = new View().source(byteIn);
    const byteOutDiv = new View().source(byteOut);
    const msgInDiv = new View().source(messageIn);

    const scale = {
      timestamp: {
        type: 'time',
        mask: 'MM-DD HH:mm:ss',
        tickCount: 10,
      },
      oneMinuteRate: {
        formatter(oneMinuteRate) {
          return `${bytesToSize(oneMinuteRate)}/s`;
        },
      },
    };
    const ByteInscale = {
      timestamp: {
        type: 'time',
        mask: 'MM-DD HH:mm:ss',
        tickCount: 10,
      },
      oneMinuteRate: {
        formatter(oneMinuteRate) {
          return `${bytesToSize(oneMinuteRate)}/s`;
        },
      },
    };

    const msgScale = {
      timestamp: {
        type: 'time',
        mask: 'MM-DD HH:mm:ss',
        tickCount: 10,
      },
      oneMinuteRate: {
        formatter(oneMinuteRate) {
          const res = /^\d+$/;
          const trans = transToNumer(oneMinuteRate);
          if (res.test(trans)) {
            return trans;
          }
          return `${trans}/s`;
        },
      },
    };


    return (
      <div>
        <Loading visible={this.state.isLoading} style={styles.loading}>
          <IceContainer>
            <ClusterGroupDatePicker
              onDataChange={this.handleApply}
              style={styles.row}
              startTime={dayjs().subtract(1, 'days').valueOf()}
            />
          </IceContainer>

          {messageIn.length > 0 ? (
            <IceContainer>
              <div>MessageInPerSec</div>
              <Chart height={400} data={msgInDiv} scale={msgScale} forceFit>
                <Axis name="timestamp" />
                <Axis name="oneMinuteRate" />
                <Legend />
                <Tooltip
                  crosshairs={{
                    type: 'line',
                  }}
                />
                <Geom type="areaStack" position="timestamp*oneMinuteRate" color="broker" />
                <Geom
                  type="areaStack"
                  position="timestamp*oneMinuteRate"
                  size={2}
                  color="broker"
                />
              </Chart>
            </IceContainer>
          ) : null}

          {byteOut.length > 0 ? (
            <IceContainer>
              <div>ByteOutPerSec</div>
              <Chart height={400} data={byteOutDiv} scale={scale} forceFit>
                <Axis name="timestamp" />
                <Axis name="oneMinuteRate" />
                <Legend />
                <Tooltip />
                <Geom type="areaStack" position="timestamp*oneMinuteRate" color="broker" />
                <Geom
                  type="areaStack"
                  position="timestamp*oneMinuteRate"
                  size={2}
                  color="broker"
                />
              </Chart>
            </IceContainer>
          ) : null}

          {byteIn.length > 0 ? (
            <IceContainer>
              <div>ByteInPerSec</div>
              <Chart height={400} data={byteInDiv} scale={ByteInscale} forceFit>
                <Axis name="timestamp" />
                <Axis name="oneMinuteRate" />
                <Legend />
                <Tooltip
                  crosshairs={{
                    type: 'line',
                  }}
                />
                <Geom type="areaStack" position="timestamp*oneMinuteRate" color="broker" />
                <Geom
                  type="areaStack"
                  position="timestamp*oneMinuteRate"
                  size={2}
                  color="broker"
                />
              </Chart>
            </IceContainer>
          ) : null}


        </Loading>
      </div>
    );
  }
}

const styles = {
  row: {
    margin: '10px',
    float: 'right',
  },
  loading: {
    width: '100%',
  },
};
