import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Table, Message, Loading, Button } from '@alifd/next';
import axios from '@utils/axios';
import CustomPagination from '@components/CustomPagination';
import { sortData } from '@utils/dataFormat';
import CustomTableFilter from '@components/CustomTableFilter';
import { getPersonalityCluster } from '@utils/cookies';
import collect from '@images/collect.jpg';
import collects from '@images/collects.jpg';
import IceImg from '@icedesign/img';

@withRouter
export default class TopicList extends Component {
  state = {
    isLoading: false,
    pageData: [],
    filterDataSource: [],
    dataSource: [],
  };
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
        axios.get(`/monitor/topic?cluster=${clusterId}`).then((response) => {
          if (response.data.code === 200) {
            if (this.mounted) {
              const data = sortData(response.data.data, 'topicName');
              this.setState({
                filterDataSource: data,
                dataSource: data,
                isLoading: false,
              });
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
    let dataSource = [];
    dataSource = this.state.filterDataSource.sort((a, b) => {
      a = a[value];
      b = b[value];
      if (order === 'asc') {
        return a.localeCompare(b);
      }
      return b.localeCompare(a);
    });
    this.setState({
      filterDataSource: dataSource,
    });
  }

  // detail
  renderTopic = (value, index, record) => {
    return (
      <div>
        <a style={styles.topicLink} onClick={() => this.handelDetail(record)}>
          {record.topicName}
        </a>
      </div>
    );
  };

  handelDetail = (record) => {
    this.props.history.push(`/monitor/consumer/topic/consumer_offsets/${record.clusterID}/${record.clusterName}/${record.topicName}`);
  };

  handleCollect = (record) => {
    const type = 'monitor_topic';
    axios.get(`monitor/topic/collection?name=${record.topicName}&&collection=${record.collections}&&clusterId=${record.clusterID}&&type=${type}`).then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          const ids = getPersonalityCluster('monitorTopic').id;
          const isAll = getPersonalityCluster('monitorTopic').isAll;
          if (isAll) {
            this.fetchData(-1);
          } else {
            this.fetchData(ids);
          }

          // this.changePage(1);
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

  rendercollection = (value, index, record) => {
    let view = null;
    if (record.collections) {
      view = (
        <Button text onClick={() => this.handleCollect(record)}><IceImg
          height={13}
          width={15}
          src={collect}
          style={{ cursor: 'pointer' }}
        />
        </Button>
      );
    } else {
      view = (
        <Button text onClick={() => this.handleCollect(record)}>
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

  render() {
    return (
      <div>
        <Loading
          visible={this.state.isLoading}
          style={styles.loading}
        >
          <CustomTableFilter
            dataSource={this.state.dataSource}
            refreshTableData={this.refreshTableData}
            refreshDataSource={this.fetchData}
            selectTitle="Cluster"
            selectField="clusterName"
            searchTitle="Filter"
            searchField="topicName"
            searchPlaceholder="Input Topic Name"
            switchField="topicName"
            id="monitorTopic"
          />
          <Table dataSource={this.state.pageData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)}>
            <Table.Column title="Topic Name" dataIndex="topicName" cell={this.renderTopic} sortable />
            <Table.Column title="Cluster" dataIndex="clusterName" />
            <Table.Column title="" cell={this.rendercollection} />
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
