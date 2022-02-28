import React, { useState, useEffect } from 'react';
import { Table, Loading, Button, Select, Grid } from '@alifd/next';
import { withRouter } from 'react-router-dom';
import axios from '@utils/axios';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import IceContainer from '@icedesign/container';
import { formatSizeUnits , thousandSplit, bytesToSize } from '@utils/dataFormat';
import FoundationSymbol from '@icedesign/foundation-symbol';
import GroupDatePicker from '@components/GroupDatePicker';

import dayjs from 'dayjs';

import style from './index.module.scss';
import MetricCharts from './MetricChart';

const Option = Select.Option;
const { Row, Col } = Grid;

function Metric(props) {
  const [metric, setMetric] = useState([]);
  const [clusterName, setClusterName] = useState(
    props.match.params.clusterName
  );
  const [isMetricLoading, setIsMetricLoading] = useState(false);
  const [startTime, setStartTime] = useState(dayjs().subtract(1, 'days'));
  const [endTime, setEndTime] = useState(dayjs());
  const [metricValue, setMetricValue] = useState('MessagesInPerSec');
  const [isOffsetLoading,setIsOffsetLoading] = useState(false);
  const [offsetData,setOffsetData] = useState({});
  const [metricData,setMetricData] = useState([]);
  const [config, setConfig] = useState({
    topic: props.match.params.topicName,
    clusterId: props.match.params.id,
    start: startTime.valueOf(),
    end: endTime.valueOf(),
    metric: metricValue,
  });

  const fetchMetric = (data) => {
    setIsMetricLoading(true);
    axios.defaults.timeout = 180000;
    axios
      .post('/monitor/topic/consumer_offsets/topic_metric', data)
      .then((response) => {
        if (response.data.code === 200) {
          if (response.data.data) {
            setMetric(response.data.data);
            setIsMetricLoading(false);
          } else {
            setIsMetricLoading(false);
          }
        }
      })
      .catch((error) => {
        console.error(error);
      });
  };


  const fetchLogSizeAndOffset = (data) =>{
    setIsOffsetLoading(true);
    axios.defaults.timeout = 180000;
    axios
      .get( `/monitor/topic/offset?clusterId=${data.clusterID}&topic=${data.topic}`)
      .then((response) => {
        if (response.data.code === 200) {
          if (response.data.data) {
            let fileSize = response.data.data.fileSize;
            const logSize = response.data.data.logSize;
            if(fileSize === -1 || fileSize===null || fileSize===undefined){
              fileSize='-';

            }else{
              fileSize = bytesToSize(fileSize);
              
            }
            const offsetData = {
              fileSize,
              logSize: thousandSplit(logSize)
            };
            setOffsetData(offsetData);
          }
          setIsOffsetLoading(false);
        }
      })
      .catch((error) => {
        console.error(error);
      });
  }


  useEffect(() => {
    const data = {
      topic: props.match.params.topicName,
      clusterID: props.match.params.id,
    };
    fetchMetric(data);
    fetchLogSizeAndOffset(data);
  }, []);

  const backward = () => {
    window.location.href = '#/monitor/producer';
  };

  const handleRefresh = () => {
    const data = {
      topic: props.match.params.topicName,
      clusterID: props.match.params.id,
    };

    fetchMetric(data);
    fetchLogSizeAndOffset(data);
    const configs = config;
    configs.start = dayjs().subtract(1, 'days').valueOf();
    configs.end = dayjs().valueOf();
    setConfig(configs);
    // this.fetchDetail(data);
  };

  const handleConsumer = () => {
    props.history.push(
      `/monitor/consumer/topic/consumer_offsets/${props.match.params.id}/${clusterName}/${props.match.params.topicName}`
    );
  };

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

  const handleApply = (picker) => {
    const startDate = dayjs(picker.startDate).valueOf();
    const endDate = dayjs(picker.endDate).valueOf();
    const data = {
      start: startDate,
      end: endDate,
      // clientId: this.state.cid,
      // interval: this.state.interval,
    };
    setStartTime(startDate);
    setEndTime(endDate);
    const configs = config;
    configs.start = startDate;
    configs.end = endDate;
    setConfig(configs);
  };

  function handleChange(value) {
    setMetricValue(value);
    const configs = config;
    configs.metric = value;
    setConfig(configs);
  }


  return (
    <div className={style.contain}>
      <CustomBreadcrumb items={breadcrumb} title="Producer Metric" />
      <IceContainer style={styles.container}>
        <div style={{ width: '100%', height: '5px', margin: '0px 0px 28px 0',paddingBottom:'10px'}}>
          <FoundationSymbol
            onClick={() => backward()}
            style={styles.backward}
            size="large"
            type="backward"
          />
          <Button
            type="secondary"
            style={{ float: 'right' }}
            onClick={() => {
              handleRefresh();
            }}
          >
              Refresh&nbsp;&nbsp;
            <FoundationSymbol size="small" type="exchange" />
          </Button>

          <Button
            type="secondary"
            style={{ float: ' right', marginRight: '7px' }}
            onClick={() => handleConsumer()}
          >
              Consumer&nbsp;&nbsp;
            <FoundationSymbol size="small" type="link" />
          </Button>
        </div>

        <Loading visible={isOffsetLoading} style={styles.loading}>
          <Row wrap gutter="20" style={{ justifyContent: 'center' }}>
            <Col l="3">
              <div style={styles.item}>
                <p style={styles.itemTitle}>Cluster</p>
                <h4 className={style.mb-0}>{clusterName}</h4>
              </div>
            </Col>
            <Col l="9">
              <div style={styles.item}>
                <p style={styles.itemTitle}>Topic</p>
                <h4 className={style.mb-0} title={props.match.params.topicName}>{props.match.params.topicName}</h4>
              </div>
            </Col>
            <Col l="4">
              <div style={styles.item}>
                <p style={styles.itemTitle}>Log Size(LEO)</p>
                <h4 className={style.mb-0} title={offsetData.logSize}>{offsetData.logSize}</h4>
              </div>
            </Col>
            <Col l="4">
              <div style={styles.item}>
                <p style={styles.itemTitle}>File Size</p>
                <h4 className={style.mb-0} title={offsetData.fileSize}>{offsetData.fileSize}</h4>
              </div>
            </Col>
          </Row>
        </Loading>
        <div className={style.tables}>
          <Table dataSource={metric}>
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
          </Table>{' '}
        </div>
        <div className={style.leftChart}>
          <GroupDatePicker
            style={styles.row}
            onDataChange={handleApply}
            startTime={dayjs().subtract(1, 'days').valueOf()}
          />
          <Select
            id="basic-demo"
            style={styles.select}
            showSearch
            value={metricValue}
            onChange={handleChange}
          >
            <Option value="BytesInPerSec">BytesInPerSec</Option>
            <Option value="BytesOutPerSec">BytesOutPerSec</Option>
            <Option value="MessagesInPerSec">MessagesInPerSec</Option>
            <Option value="BytesRejectedPerSec">BytesRejectedPerSec</Option>
            <Option value="FailedFetchRequestsPerSec">
                  FailedFetchRequestsPerSec
            </Option>
            <Option value="FailedProduceRequestsPerSec">
                  FailedProduceRequestsPerSec
            </Option>
          </Select>
          <MetricCharts config={config} />
        </div>
      </IceContainer>
    </div>
  );
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
    // minHeight: '600px',
    // padding: '10px 20px 20px',
    marginBottom:'5px',
    padding:'15px 10px 5px 5px',
    minHeight:'600px'
  },
  link: {
    margin: '0 5px',
    color: 'rgba(49, 128, 253, 0.65)',
    cursor: 'pointer',
    textDecoration: 'none',
  },
  row: {
    margin: '10px',
    float: 'right',
  },
  select: {
    margin: '10px',
    width: '28%',
    float: 'right',
  },
  item: {
    height: '120px',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    // overflow:'hidden',
    // whiteSpace:'nowrap',
    // textOverflow:'ellipsis',
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
    height:'70px',
    // display: 'inline-block',
    // whiteSpace: 'nowrap',
    // overflow: 'hidden',
    // textOverflow: 'ellipsis',
    // width: '50px',
  },
};

export default withRouter(Metric);
