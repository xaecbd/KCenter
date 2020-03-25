import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Table, Message, Loading, Grid, Button, Icon } from '@alifd/next';
import axios from '@utils/axios';
import { sortData, sortDataByOrder, transToHours, resturctData } from '@utils/dataFormat';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import EditDialog from '../EditDialog';

const { Col } = Grid;
@withRouter
export default class TopicList extends Component {
   state = {
     isLoading: false,
     filterDataSource: [],
     dataSource: [],
     pageData: [],
     visible: false,
   };

   componentWillMount() {
     this.mounted = true;
   }

   componentWillUnmount() {
     this.mounted = false;
   }


  fetchTopicList = (clusterId) => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        // const switchValue = sessionStorage.getItem(`kafkaManagerTopicSwitch`)==null?false:sessionStorage.getItem(`kafkaManagerTopicSwitch`);
        axios.get(`/manager/topic/list?cluster=${clusterId}`).then((response) => {
          if (response.data.code === 200) {
            if (this.mounted) {
              const data = sortData(response.data.data, 'topicName');
              //   const oldData = data;
              //   if (!switchValue || switchValue === "false"){
              //     data = data.filter(v => !v['topicName'].startsWith("_"));
              //   }
              this.setState({
                dataSource: data,
                filterDataSource: data,
                isLoading: false,
              }, () => {

              });
              return data;
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
  }

  handelDetail = (record) => {
    this.props.history.push(`/kafka-manager/topic/${record.clusterId}/${record.topicName}`);
  };

  renderTopic = (value, index, record) => {
    return (
      <div>
        <a style={styles.topicLink} onClick={() => this.handelDetail(record)}>
          {record.topicName}
        </a>
      </div>
    );
  };

  renderTTL= (value, index, record) => {
    if (value !== null && value) {
      return transToHours(value);
    }
    return '-';
  };

  redrawPageData=(value) => {
    this.setState({
      pageData: value,
    });
  }

  refreshTableData = (value) => {
    this.setState({
      filterDataSource: value,
    });
  }

  onSort = (value, order) => {
    const data = sortDataByOrder(this.state.filterDataSource, value, order);
    this.refreshTableData(data);
  }

  renderUnderRep = (value) => {
    if (value !== 0) {
      return `${value}%`;
    }
    return value;
  }
  handelDialog = () => {
    this.setState({
      visible: !this.state.visible,
    });
  };

  hideDialog = () => {
    this.setState({
      visible: !this.state.visible,
    });
  };

  render() {
    //   const url = `/manager/topic/list?cluster=${clusterId}`;
    const view = (<Col align="center"><Button type="secondary" onClick={this.handelDialog}><Icon type="add" />Create Topic</Button></Col>);
    const { visible } = this.state;
    return (
      <div>
        <Loading
          visible={this.state.isLoading}
          style={styles.loading}
        >
          <EditDialog visible={visible} handelDialog={this.handelDialog} fetchData={this.fetchTopicList} />
          <CustomTableFilter
            dataSource={this.state.dataSource}
            refreshTableData={this.refreshTableData}
            refreshDataSource={this.fetchTopicList}
            selectTitle="Cluster"
            selectField="cluster"
            searchTitle="Filter"
            searchField="topicName"
            searchPlaceholder="Input Topic Name"
            switchField="topicName"
            otherComponent={view}
            id="kafkaManagerTopic"
          />
          <Table dataSource={this.state.pageData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)}>
            <Table.Column title="Topic Name" dataIndex="topicName" cell={this.renderTopic} sortable />
            <Table.Column title="Partition" dataIndex="partition" />
            <Table.Column title="Replication" dataIndex="replication" />
            <Table.Column title="TTL(hours)" dataIndex="ttl" cell={this.renderTTL} />
            <Table.Column title="Cluster" dataIndex="cluster" />
            <Table.Column title="Under Replication(%)" dataIndex="under_replication" cell={this.renderUnderRep} />

          </Table>
          <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
        </Loading>
      </div>
    );
  }
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
