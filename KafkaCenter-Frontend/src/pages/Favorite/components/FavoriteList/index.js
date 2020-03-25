import React, { Component } from 'react';
import { Table, Message, Loading, Button } from '@alifd/next';
import axios from '@utils/axios';
import { sortData, sortDataByOrder } from '@utils/dataFormat';
import { withRouter } from 'react-router-dom';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import { getPersonalityCluster } from '@utils/cookies';
import collect from '@images/collect.jpg';
import collects from '@images/collects.jpg';
import IceImg from '@icedesign/img';


@withRouter
export default class FavoriteList extends Component {
  state = {
    loading: false,
    dataSource: [],
    filterDataSource: [],
    pageData: [],
  };

  componentDidMount() {
    // this.fetchData();
  }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  fetchData = (clusterId) => {
    this.setState(
      {
        loading: true,
      },
      () => {
        const type = 'monitor_topic';
        axios
          .get(`/monitor/favorite?type=${type}&cluster=${clusterId}`)
          .then((response) => {
            if (response.data.code === 200) {
              if (this.mounted) {
                const data = sortData(response.data.data, 'topicName');
                this.setState(
                  {
                    dataSource: data,
                    filterDataSource: data,
                    loading: false,
                  }
                );
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

  onSort(value, order) {
    const data = sortDataByOrder(this.state.filterDataSource, value, order);
    this.refreshTableData(data);
  }

  refreshTableData = (value) => {
    this.setState({
      filterDataSource: value,
    });
  }

  redrawPageData=(value) => {
    this.setState({
      pageData: value,
    });
  }

  handleCollect = (record) => {
    const type = 'monitor_topic';
    axios.get(`monitor/topic/collection?name=${record.topicName}&&collection=${record.collections}&&clusterId=${record.clusterID}&&type=${type}`).then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          const ids = getPersonalityCluster('myFavorite').id;
          const isAll = getPersonalityCluster('myFavorite').isAll;
          if (isAll) {
            this.fetchData(-1);
          } else {
            this.fetchData(ids);
          }
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
    this.props.history.push(`/monitor/topic/consumer_offset/${record.clusterID}/${record.topicName}`);
  };

  renderOption = (value, index, record) => {
    return (
      <div>
        <a style={styles.link} onClick={() => this.handlePrdouct(record)}>
          Producer
        </a>
        <span style={styles.separator} />
        <span title="Consumer" style={styles.operBtn} >
          <a style={styles.link} onClick={() => this.handelConsumer(record)}>
            Consumer
          </a>
        </span>
      </div>
    );
  }

  handlePrdouct = (record) => {
    this.props.history.push(`/monitor/producer/metric/${record.clusterID}/${record.clusterName}/${record.topicName}`);
  }

  handelConsumer = (record) => {
    this.props.history.push(`/monitor/consumer/topic/consumer_offsets/${record.clusterID}/${record.clusterName}/${record.topicName}`);
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

  render() {
    const { isLoading } = this.state;
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
            searchField="topicName"
            searchPlaceholder="Input Topic Name"
            id="myFavorite"
          />
          <Table
            loading={isLoading}
            dataSource={this.state.pageData}
            hasBorder={false}
            onSort={(value, order) => this.onSort(value, order)}
          >
            <Table.Column title="Topic Name" dataIndex="topicName" sortable />
            <Table.Column title="Cluster" dataIndex="clusterName" />

            <Table.Column title="" cell={this.rendercollection} />
            <Table.Column title="Options" cell={this.renderOption} />
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
  editIcon: {
    color: '#999',
    cursor: 'pointer',
  },
  separator: {
    margin: '0 8px',
    display: 'inline-block',
    height: '12px',
    width: '1px',
    verticalAlign: 'middle',
    background: '#e8e8e8',
  },
  link: {
    margin: '0 5px',
    color: 'rgba(49, 128, 253, 0.65)',
    cursor: 'pointer',
    textDecoration: 'none',
  },
};
