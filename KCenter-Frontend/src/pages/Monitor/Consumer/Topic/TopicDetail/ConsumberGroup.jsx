import React, { useState, useEffect } from 'react';
import { Table, Icon, Loading } from '@alifd/next';
import { withRouter } from 'react-router-dom';
import FoundationSymbol from '@icedesign/foundation-symbol';
import dayjs from 'dayjs';
import IceImg from '@icedesign/img';
import warnning from '@images/warning.svg';
import green from '@images/green.svg';
import error from '@images/error.svg';
import alert from '@images/alert.svg';
import axios from '@utils/axios';
import style from './index.module.scss';
import EditDialog from '../../Alert';

function ConsumerGroup(props) {
  const [loading, setLoading] = useState(false);
  const [display, setDisplay] = useState(false);
  const [topic, setTopic] = useState(props.record.topicName);
  const [clusterName, setClusterName] = useState(props.record.clusterName);
  const [clusterID, setClusterID] = useState(props.record.clusterID);
  const [consumerMethod, setConsumerMethod] = useState(props.consumerMethod);
  const [visible, setVisible] = useState(false);
  const [alertValue, setAlertValue] = useState({});
  const [alertData, setAlertData] = useState({});
  const [isZk, setIsZk] = useState(props.config.isZk);

  const styles = {
    topicLink: {
      margin: '0 5px',
      color: '#1111EE',
      cursor: 'pointer',
      textDecoration: 'none',
    },
    alertIcon: {
      cursor: 'pointer',
    },
    loading: {
      width: '100%',
    },
  };

  useEffect(() => {
    // Update the document title using the browser API
    //  let taryger;
    // if (isZk) {
    //   taryger = document.getElementsByClassName(`${style.zktest}`)[0];
    // } else {
    //   taryger = document.getElementsByClassName(`${style.customTable}`)[0];
    // }
    // // taryger.getElementsByTagName('thead');
    // taryger.getElementsByTagName(`${style.thead}`)[0].style.display =
    //   'table-header-group';
  });

  const onClick = () => {
    setDisplay(!display);
  };

  const renderHost = (value) => {
    if (value) {
      return value.toString().replace('/', '');
    }
    return '';
  };

  const splitTimer = (value) => {
    if (value) {
      let timer = [];
      let result;
      timer = value.toString().split('-');
      const resp = '\\d{10}|\\d{13}';
      timer.map((objs) => {
        if (objs.match(resp)) {
          result = objs;
        }
      });

      // eslint-disable-next-line radix
      return dayjs(parseInt(result)).format('YYYY-MM-DD HH:mm:ss');
    }
    return '';
  };

  const propsConf = {
    style: { background: '#dddddd' },
    onDoubleClick: () => {},
  };

  const getCellProps = (rowIndex, colIndex) => {
    if (rowIndex === 0) {
      return propsConf;
    }
  };

  const handelGroupChart = (record) => {
    let zkName;
    if (isZk) {
      zkName = 'zk';
    } else {
      zkName = 'broker';
    }
    props.history.push(
      `/monitor/consumer/topic/consumer_offsets/chart/${props.record.clusterID}/${props.record.topicName}/${record.group}/${zkName}`
    );
  };

  const renderNumber = (value) => {
    if (value < 0) {
      return '-';
    }
    return value;
  };

  const renderIceImag = (img, title) => {
    return (
      <div id="advanced">
        <IceImg
          title={title}
          height={26}
          width={27}
          src={img}
          style={{ textAlign: 'center', verticalAlign: 'middle' }}
        />
      </div>
    );
  };

  const hideDialog = () => {
    setVisible(!visible);
  };

  const handelDialog = (record) => {
    // 先根据已有的数据去查询alert是否已存在
    const data = record;
    const obj = Object.create(data);
    obj.clusterId = clusterID;
    obj.consummerGroup = record.group;
    obj.topicName = topic;
    obj.clusterName = clusterName;
    obj.consummerApi = consumerMethod.toUpperCase();
    setLoading(true);

    axios
      .post('/monitor/alert/getalert', obj)
      .then((response) => {
        if (response.data.code === 200) {
          setAlertData(response.data.data);
          setLoading(false);
          const newData = response.data.data;
          if (obj.consummerApi !== newData.consummerApi) {
            newData.consummerApi = 'ALL';
          }
          const newObj = Object.create(newData);
          newObj.clusterId = newData.clusterId;
          newObj.consummerGroup = newData.consummerGroup;
          newObj.consummerApi = newData.consummerApi;
          newObj.topicName = newData.topicName;
          newObj.clusterName = clusterName;
          newObj.disableAlerta = newData.disableAlerta;
          newObj.disabled = true;
          newObj.enable = newData.enable;
          newObj.id = newData.id;
          if (newObj.id === null) {
            newObj.diapause = '';
            newObj.diapause = '';
            newObj.mailTo = '';
            newObj.webhook = '';
          } else {
            newObj.diapause = newData.diapause;
            newObj.threshold = newData.threshold;
            newObj.mailTo = newData.mailTo;
            newObj.webhook = newData.webhook;
          }
          setVisible(!visible);
          setAlertValue(newObj);
        }
      })
      .catch(() => {
        console.error(error);
      });
  };

  const renderAlert = (value, index, record) => {
    if (index !== 0) {
      return '';
    }
    return (
      <div>
        <a
          target="_blank"
          style={styles.alertIcon}
          onClick={() => handelDialog(record)}
        >
          <IceImg
            title="alert"
            height={26}
            width={27}
            src={alert}
            style={{ textAlign: 'center', verticalAlign: 'middle' }}
          />
        </a>
      </div>
    );
  };
  const fetchData = () => {};

  let type;
  let data = props.datasource;
  if (display) {
    type = (
      <Icon
        type="arrow-up"
        size="xs"
        onClick={(e) => {
          onClick(e);
        }}
      />
    );
  } else {
    type = (
      <Icon
        type="arrow-down"
        size="xs"
        onClick={(e) => {
          onClick(e);
        }}
      />
    );
    data = data.slice(0, 1);
  }
  const renderObj = () => {
    return renderIceImag(green, props.config.consumerGroupState);
  };
  const render = (value, index, record) => {
    if (index !== 0) {
      return '';
    }
    if (props.config.isSimpleConsumerGroup && !isZk) {
      return (
        <div>
          {type}
          <a style={styles.topicLink} onClick={() => handelGroupChart(record)}>
            {value}
          </a>
          <span title="SimpleConsumerGroup" className={style.onMeous}>
            <FoundationSymbol type="customize" size="small" />
          </span>
        </div>
      );
    }
    return (
      <div>
        {type}&nbsp;
        <a
          target="_blank"
          style={styles.topicLink}
          onClick={() => handelGroupChart(record)}
        >
          {value}
        </a>
      </div>
    );
  };
  const renderYellowObj = () => {
    if(props.config.isSimpleConsumerGroup){
      return renderIceImag(warnning, 'SimpleConsumerGroup无法判断其状态');
    }else{
      return renderIceImag(warnning, props.config.consumerGroupState);
    }
   
  };
  const StopObj = () => {
    return renderIceImag(error, props.config.consumerGroupState);
  };

  let result = null;
  if (props.config.kafkaCenterGroupState === 'ACTIVE') {
    result = (
      <Table.Column
        title="Status"
        dataIndex="status"
        cell={renderObj}
        width={10}
      />
    );
  } else if (props.config.kafkaCenterGroupState === 'DEAD') {
    result = (
      <Table.Column
        title="Status"
        dataIndex="status"
        cell={StopObj}
        width={10}
      />
    );
  } else {
    result = (
      <Table.Column
        title="Status"
        dataIndex="status"
        cell={renderYellowObj}
        width={10}
      />
    );
  }

  return (
    <div className={isZk ? 'style.zktest' : ''}>
      <Loading visible={loading} style={styles.loading}>
        <EditDialog
          visible={visible}
          handelDialog={hideDialog}
          value={alertValue}
          fetchData={fetchData}
        />
      </Loading>
      <Table
        className={style.customTable}
        dataSource={data}
        cellProps={getCellProps}
        hasHeader={props.config.hasHeard}
      >
        <Table.Column
          title="Consumer Group"
          dataIndex="group"
          cell={render}
          width={150}
        />
        <Table.Column title="Partition" dataIndex="partition" width={60} />
        <Table.Column
          title="Offset"
          dataIndex="offset"
          width={100}
          cell={renderNumber}
        />
        <Table.Column title="LogSize" dataIndex="logEndOffset" width={100} />
        <Table.Column
          title="Lag"
          dataIndex="lag"
          width={100}
          cell={renderNumber}
        />

        {isZk ? null : (
          <Table.Column
            title="Host"
            dataIndex="host"
            width={90}
            cell={renderHost}
          />
        )}

        {isZk ? (
          <Table.Column
            title="Last Seen"
            dataIndex="clientId"
            cell={splitTimer}
            width={100}
          />
        ) : null}
        <Table.Column title="Owner" dataIndex="clientId" width={240} />

        {result}
        <Table.Column title="Alert" width={10} cell={renderAlert} />
      </Table>
    </div>
  );
}



export default withRouter(ConsumerGroup);
