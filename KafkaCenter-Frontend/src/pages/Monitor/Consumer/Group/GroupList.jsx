import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Table, Message, Loading } from '@alifd/next';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import { sortDataByOrder } from '@utils/dataFormat';
import { getPersonalityCluster } from '@utils/cookies';
import axios from '@utils/axios';

@withRouter
export default class GroupTable extends Component {
  state = {
    isLoading: false,
    // 当前页的数据
    pageData: [],
    // 当前条件下的所有数据
    totalData: [],
    // 无条件时的所有数据
    allData: [],
  };

  //   componentDidMount() {
  //    // console.log('id:'+getPersonalityCluster('monitorGroup').id);
  //    // this.fetchData(getPersonalityCluster('monitorGroup').id);

  //   }
  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  fetchData = (clusterId) => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.get(`/monitor/group?cluster=${clusterId}`).then((response) => {
          if (response.data.code === 200) {
            const data = sortDataByOrder(response.data.data, 'consummerGroup', 'asc');
            if (this.mounted) {
              this.setState({
                totalData: data,
                allData: data,
                isLoading: false,
              });
              // this.getPageData(data);
            }
          } else {
            Message.error(response.data.message);
          }
        }).catch((error) => {
          console.error(error);
          this.setState({
            isLoading: false,
          });
        });
      }
    );
  };

  onSort(value, order) {
    this.setState({
      isLoading: true,
    });
    const dataSource = sortDataByOrder(this.state.totalData, value, order);
    this.refreshTableData(dataSource);
    this.setState({
      isLoading: false,
    });
  }
  // group detail
  renderGroup = (value, index, record) => {
    return (
      <div>
        <a style={styles.groupLink} onClick={() => this.handelDetail(record)}>
          {record.consummerGroup}
        </a>
      </div>
    );
  };

  handelDetail = (record) => {
    this.props.history.push(`/monitor/consumer/group/detail/${record.clusterID}/${record.clusterName}/${record.consummerGroup}`);
  };

  /**
   * 刷新当前table的数据
   */
  refreshTableData = (value) => {
    this.setState({
      totalData: value,
    });
  }

  redrawPageData=(value) => {
    this.setState({
      pageData: value,
    });
  }

  render() {
    const { isLoading, pageData } = this.state;
    return (
      <div>
        <Loading
          visible={isLoading}
          style={styles.loading}
        >
          <CustomTableFilter
            dataSource={this.state.allData}
            refreshDataSource={this.fetchData}
            selectField="clusterName"
            selectTitle="Cluster"
            searchField="consummerGroup"
            searchTitle="Group"
            refreshTableData={this.refreshTableData}
            id="monitorGroup"
          />
          <Table dataSource={pageData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)} primaryKey="id">
            <Table.Column title="Group Name" dataIndex="consummerGroup" cell={this.renderGroup} sortable />
            <Table.Column title="Cluster" dataIndex="clusterName" />
          </Table>
          <CustomPagination dataSource={this.state.totalData} redrawPageData={this.redrawPageData} />
        </Loading>
      </div>
    );
  }
}

const styles = {
  groupLink: {
    margin: '0 5px',
    color: '#1111EE',
    cursor: 'pointer',
    textDecoration: 'none',
  },
  loading: {
    width: '100%',
  },
};
