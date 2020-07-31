/* eslint-disable indent */
import React, { Component, useEffect, useState } from 'react';
import { Table, Icon, Loading } from '@alifd/next';
import FoundationSymbol from '@icedesign/foundation-symbol';
import { withRouter } from 'react-router-dom';
import IceImg from '@icedesign/img';
import dayjs from 'dayjs';
import warning from '@images/warning.svg';
import green from '@images/green.svg';
import error from '@images/error.svg';
import alert from '@images/alert.svg';
import axios from '@utils/axios';
import './GroupDetail.scss';
import EditDialog from '../../Alert';

function ConsumerGroup(props) {
  const [loading, setLoading] = useState(false);
  const [clusterName, setClusterName] = useState(props.record.clusterName);
  const [clusterID, setClusterID] = useState(props.record.clusterID);
  const [consumerMethod, setConsumerMethod] = useState(props.consumerMethod);

  const [display, setDisplay] = useState(false);
  const [visible, setVisible] = useState(false);
  const [alertValue, setAlertValue] = useState({});
  const [alertData, setAlertData] = useState({});
  const [isZk, setIsZk] = useState(props.config.isZk);

  const renderHost = (value, index, record) => {
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

      return dayjs(parseInt(result)).format('YYYY-MM-DD HH:mm:ss');
    }
    return '';
  };

  const getCellProps = (rowIndex, colIndex) => {
    if (rowIndex === 0) {
      return propsConf;
    }
  };

  const propsConf = {
    style: { background: '#dddddd' },
    onDoubleClick: () => {},
  };

  const onClick = () => {
    setDisplay(!display);
  };

  useEffect(() => {
    let taryger;
    if (isZk) {
      taryger = document.getElementsByClassName('zktest')[0];
    } else {
      taryger = document.getElementsByClassName('custom-table')[0];
    }
    taryger.getElementsByTagName('thead')[0].style.display =
      'table-header-group';
  }, []);

  const handelGroupChart = (record) => {
    let zkName;
    if (isZk) {
      zkName = 'zk';
    } else {
      zkName = 'broker';
    }
    props.history.push(
      `/monitor/consumer/topic/consumer_offsets/chart/${props.match.params.clusterID}/${record.topic}/${props.match.params.consummerGroup}/${zkName}`
    );
  };

  const renderNumber = (value) => {
    if (value < 0) {
      return '-';
    }
    return value;
  };
  const handelDialog = (record) => {
    const data = record;
    const obj = Object.create(data);
    obj.clusterId = clusterID;
    obj.consummerGroup = props.groupName;
    obj.topicName = record.topic;
    obj.clusterName = clusterName;
    obj.consummerApi = consumerMethod.toUpperCase();
    setLoading(true);
    axios
          .post('/monitor/alert/getalert', obj)
          .then((response) => {
            if (response.data.code === 200) {
              setAlertData(response.data.data);
              setLoading(false);
              const newData = alertData;
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
  const hideDialog = () => {
    setVisible(!visible);
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
  const renderObj = (value) => {
    return renderIceImag(green, props.config.consumerGroupState);
  };
  const render = (value, index, record) => {
    if (index !== 0) {
      return '';
    }
    if (
      props.config.isSimpleConsumerGroup &&
      props.consumerMethod == 'broker'
    ) {
      return (
        <div>
          {type}
          <a
            style={styles.topicLink}
            onClick={() => handelGroupChart(record)}
          >
            {value}
          </a>
          <span title="SimpleConsumerGroup" className="onMeous">
            <FoundationSymbol type="customize" size="small" />
          </span>
        </div>
      );
    }
    return (
      <div>
        {type}&nbsp;
        <a
          style={styles.topicLink}
          onClick={() => handelGroupChart(record)}
        >
          {value}
        </a>
      </div>
    );
  };
  const renderYellowObj = (value) => {
    return renderIceImag(warning, 'SimpleConsumerGroup无法判断其状态');
  };
  const StopObj = (value) => {
    return renderIceImag(error, props.config.consumerGroupState);
  };

  let result = null;
  if (props.config.consumerGroupState === 'STABLE') {
    result = (
      <Table.Column
        title="Status"
        dataIndex="status"
        cell={renderObj}
        width={10}
      />
    );
  } else if (props.config.consumerGroupState === 'DEAD') {
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
    <div className={isZk ? 'zktest' : ''}>
      <Loading visible={loading} style={styles.loading}>
        <EditDialog
          visible={visible}
          handelDialog={hideDialog}
          value={alertValue}
          fetchData={fetchData}
        />
      </Loading>
      <Table
        className="custom-table"
        dataSource={data}
        cellProps={getCellProps}
      >
        <Table.Column
          title="Topic"
          dataIndex="topic"
          cell={render}
          width={190}
        />
        <Table.Column title="Partition" dataIndex="partition" width={50} />
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
            width={100}
            cell={renderHost}
          />
        )}

        {isZk ? (
          <Table.Column
            title="Last Seen"
            dataIndex="clientId"
            cell={splitTimer}
            width={90}
          />
        ) : null}
        <Table.Column title="Owner" dataIndex="clientId" width={240} />
        {result}
        <Table.Column title="Alert" width={10} cell={renderAlert} />
      </Table>
    </div>
  );
}

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

export default withRouter(ConsumerGroup);