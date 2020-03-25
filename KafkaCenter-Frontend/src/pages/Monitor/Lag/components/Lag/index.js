import { withRouter } from 'react-router-dom';
import React, { Component } from 'react';
import { Table, Loading } from '@alifd/next';
import CustomTableFilter from '@components/CustomTableFilter';
import { sortDataByOrder } from '@utils/dataFormat';
import axios from '@utils/axios';
import CustomPagination from '@components/CustomPagination';
import { getPersonalityCluster } from '@utils/cookies';

const formatterInput = (value) => {
  return value.toString().replace(/(\d)(?=(\d{3})+(?:\.\d+)?$)/g, '$1,');
};

@withRouter
export default class GroupLags extends Component {
  state = {
    pageData: [],
    loading: false,
    filterDataSource: [],
    dataSource: [],
  };

  //   componentDidMount() {
  //     this.fetchData(getPersonalityCluster('monitorLag').id);
  //   }

  fetchData = (clusterId) => {
    this.setState({
      loading: true,
    }, () => {
      axios
        .get(`/monitor/lag?cluster=${clusterId}`).then((response) => {
          if (response.data.code === 200) {
            this.setState({ filterDataSource: response.data.data, dataSource: response.data.data, loading: false });
          }
        }).catch((error) => {
          console.log(error);
        });
    });
  }

  onSort(dataIndex, order) {
    const dataSource = this.state.filterDataSource.sort((a, b) => {
      const result = a[dataIndex] - b[dataIndex];
      return (order === 'desc') ? (result > 0 ? 1 : -1) : (result > 0 ? -1 : 1);
    });
    this.setState({
      filterDataSource: dataSource,
    });
  }
  redrawPageData = (data) => {
    this.setState({
      pageData: data,
    });
  }

  refreshTableData = (value) => {
    this.setState({
      filterDataSource: value,
    });
  }

  renderTopic = (value, index, record) => {
    return (
      <div>
        <a style={styles.topicLink} onClick={() => this.handelDetail(record)}>
          {record.topic}
        </a>
      </div>
    );
  };

  handelDetail = (record) => {
    this.props.history.push(`/monitor/topic/consumer_offset/${record.clusterID}/${record.topic}`);
  };


  render() {
    return (
      <div>
        <Loading visible={this.state.loading} style={styles.loading}>
          <CustomTableFilter
            dataSource={this.state.dataSource}
            refreshTableData={this.refreshTableData}
            refreshDataSource={this.fetchData}
            selectTitle="Cluster"
            selectField="clusterName"
            searchTitle="Filter"
            searchField="topic,group"
            searchPlaceholder="Input Topic Or Group Name"
            id="monitorLag"
          />
          <Table loading={this.state.loading} dataSource={this.state.pageData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)}>
            <Table.Column title="Topic Name" dataIndex="topic" />
            <Table.Column title="Group" dataIndex="group" />
            <Table.Column title="Cluster" dataIndex="clusterName" />
            <Table.Column title="Lag" dataIndex="lag" cell={formatterInput} sortable />
          </Table>

          <div />
          <div />
        </Loading>
        <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
      </div>
    );
  }
}
const styles = {
  loading: {
    width: '100%',
  },
  topicLink: {
    margin: '0 5px',
    color: '#1111EE',
    cursor: 'pointer',
    textDecoration: 'none',
  },
};
