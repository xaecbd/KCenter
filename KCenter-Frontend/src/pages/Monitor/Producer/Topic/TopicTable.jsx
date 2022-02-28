import React, { Component, useState } from 'react';
import { withRouter } from 'react-router-dom';
import { Table, Message, Loading, Button } from '@alifd/next';
import axios from '@utils/axios';
import CustomPagination from '@components/CustomPagination';
import { sort } from '@utils/dataFormat';
import CustomTableFilter from '@components/CustomTableFilter';
import { getPersonalityCluster } from '@utils/cookies';
import collect from '@images/collect.jpg';
import collects from '@images/collects.jpg';
import IceImg from '@icedesign/img';

function TopicTable(props){

  const[isLoading,setIsLoading] = useState(false);
  const[pageData,setPageData] = useState([]);
  const[filterDataSource,setFilterDataSource] = useState([]);
  const[dataSource,setDataSource] = useState([]);


  const fetchData = (clusterId) => {

    setIsLoading(true);
    axios.get(`/monitor/topic?cluster=${clusterId}`).then((response) => {
      if (response.data.code === 200) {
        const data = sort(response.data.data, 'topicName','asc');
        setFilterDataSource(data);
        setDataSource(data);
        setIsLoading(false);
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
      setIsLoading(false);
    });
  };

  const onSort = (value, order) => {
    let data = sort(filterDataSource, value, order);
    data = Object.assign([],data)
    setFilterDataSource(data);
  }

  const handelDetail = (record) => {
    props.history.push(`/monitor/producer/metric/${record.clusterID}/${record.clusterName}/${record.topicName}`);
  };

  // detail
  const renderTopic = (value, index, record) => {
    return (
      <div>
        <a style={styles.topicLink} onClick={() => handelDetail(record)}>
          {record.topicName}
        </a>
      </div>
    );
  };



  const handleCollect = (record) => {
    const type = 'monitor_topic';
    axios.get(`monitor/topic/collection?name=${record.topicName}&&collection=${record.collections}&&clusterId=${record.clusterID}&&type=${type}`).then((response) => {
      if (response.data.code === 200) {
        const ids = getPersonalityCluster('monitorTopic').id;
        const isAll = getPersonalityCluster('monitorTopic').isAll;
        if (isAll) {
          fetchData(-1);
        } else {
          fetchData(ids);
        }
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
      setIsLoading(false);
    });
  }

  const rendercollection = (value, index, record) => {
    let view = null;
    if (record.collections) {
      view = (
        <Button text onClick={() => handleCollect(record)}><IceImg
          height={13}
          width={15}
          src={collect}
          style={{ cursor: 'pointer' }}
        />
        </Button>
      );
    } else {
      view = (
        <Button text onClick={() => handleCollect(record)}>
          <IceImg
            height={13}
            width={15}
            src={collects}
            style={{ cursor: 'pointer' }}
          />
        </Button>);
    }
    return (
      <div>
        {view}
      </div>
    );
  };

  const redrawPageData=(value) => {
    setPageData(value);
  }

  const refreshTableData = (value) => {
    setFilterDataSource(value);
  }

  return (
    <div>
      <Loading
        visible={isLoading}
        style={styles.loading}
      >
        <CustomTableFilter
          dataSource={dataSource}
          refreshTableData={refreshTableData}
          refreshDataSource={fetchData}
          selectTitle="Cluster"
          selectField="clusterName"
          searchTitle="Filter"
          searchField="topicName"
          searchPlaceholder="Input Topic Name"
          switchField="topicName"
          id="monitorTopic"
        />
        <Table dataSource={pageData} hasBorder={false} onSort={(value, order) => onSort(value, order)} primaryKey="id">
          <Table.Column title="Topic Name" dataIndex="topicName" cell={renderTopic} sortable />
          <Table.Column title="Cluster" dataIndex="clusterName" />
          <Table.Column title="" cell={rendercollection} />
        </Table>
        <CustomPagination dataSource={filterDataSource} redrawPageData={redrawPageData} />
      </Loading>
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
  loading: {
    width: '100%',
  },
};

export default withRouter(TopicTable);
