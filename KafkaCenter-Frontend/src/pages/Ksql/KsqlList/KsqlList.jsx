import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import IceContainer from '@icedesign/container';
import IceImg from '@icedesign/img';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import { sortData, sortDataByOrder } from '@utils/dataFormat';

import {
  Grid,
  Input,
  Table,
  Loading,
  Message,
  Button,
  Icon,
  Dialog,
} from '@alifd/next';
import axios from '@utils/axios';
import CustomPagination from '@components/CustomPagination';

import EditDialog from './EditDialog';
import './Ksql.scss';

const { Row, Col } = Grid;
@withRouter
export default class KsqlList extends Component {
  state = {
    isLoading: false,
    searchValue: '',
    filterDataSource: [],
    dataSource: [],
    pageData: [],
    visible: false,
    hideDialog: false,
  };
  componentDidMount() {
    this.fetchList();
  }

  componentWillMount() {
    this.mounted = true;
  }

  componentWillUnmount() {
    this.mounted = false;
  }
  renderOper = (value, index, record) => {
    return (
      <div>
        <a style={styles.link} onClick={() => this.handleDetail(record)}>
          Detail
        </a>

        <span style={styles.separator} />
        <a style={styles.link} onClick={() => this.handleDelete(record)}>
          Delete
        </a>
      </div>
    );
  };
  handleDelete = (record) => {
    Dialog.confirm({
      content: `Do you want to delete  ${record.ksqlServerId}?`,
      onOk: () => {
        this.handleDeletes(record);
      },
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
  };

  contents = (record) => {
    return (
      <ul style={styles.detailTable}>
        <li style={styles.detailItem}>
          <div style={styles.detailTitle}>KSQL Address:</div>
          <div style={styles.detailBody}>{record.ksqlAddress}</div>
        </li>
      </ul>
    );
  };
  handleDetail = (record) => {
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };
    Dialog.show({
      isFullScreen: true,
      style: simpleFormDialog,
      title: 'KSQL Detail',
      content: this.contents(record),
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
  };

  onClose = () => {
    this.state({
      hideDialog: !this.state.hideDialog,
    });
  };

  handleDeletes = (record) => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios
          .delete(
            `/ksql/del_ksql?clusterName=${record.clusterName}&ksqlServiceId=${record.ksqlServerId}`
          )
          .then((response) => {
            if (response.data.code === 200) {
              this.fetchList();
              Message.success(response.data.message);
            } else {
              Message.error(response.data.message);
            }
            this.setState({
              isLoading: false,
            });
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  };
  fetchList = () => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios
          .get('/ksql/list')
          .then((response) => {
            if (response.data.code === 200) {
              if (this.mounted) {
                const data = sortData(response.data.data, 'ksqlServerId');
                this.setState({
                  dataSource: data,
                  filterDataSource: data,
                  isLoading: false,
                });
                return data;
              }
            } else {
              Message.error(response.data.message);
            }
          })
          .catch((error) => {
            console.error(error);
            this.setState({
              isLoading: false,
            });
          });
      }
    );
  };

  handleFilterChange = (searchValue) => {
    const dataSource = this.state.dataSource;

    let filterData = [];
    if (searchValue.trim().length === 0) {
      filterData = dataSource;
    } else {
      dataSource.forEach((v) => {
        if (
          v.ksqlServerId
            .toLocaleLowerCase()
            .includes(searchValue.toLocaleLowerCase())
        ) {
          filterData.push(v);
        }
      });
    }
    this.setState({
      searchValue,
    });
    this.refreshTableData(filterData);
  };
  onSort(value, order) {
    const dataSource = sortDataByOrder(this.state.dataSource, value, order);
    this.refreshTableData(dataSource);
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

  redrawPageData = (value) => {
    this.setState({
      pageData: value,
    });
  };

  refreshTableData = (value) => {
    this.setState({
      filterDataSource: value,
    });
  };

  selectKsqlServer = (record) => {
    this.props.history.push(
      `/ksql/${record.clusterName}/${record.ksqlServerId}/console`
    );
  };

  render() {
    const { visible } = this.state;
    const breadcrumb = [
      {
        link: '',
        text: 'KSQL',
      },
      {
        link: '/ksql/list',
        text: 'List',
      },
    ];
    const renderKsqlServerId = (value, index, record) => {
      return (
        <a style={styles.link} onClick={() => this.selectKsqlServer(record)}>
          {value}
        </a>
      );
    };
    const renderHealth = (value, index, record) => {
      if (value) {
        return (
          <IceImg
            height={26}
            width={27}
            src={require('../../../../public/images/green.svg')}
          />
        );
      }
      if (record.version.replace('.', '') < 540) {
        return '-';
      }
      return (
        <IceImg
          height={26}
          width={27}
          src={require('../../../../public/images/error.svg')}
        />
      );
    };
    return (
      <div>
        <CustomBreadcrumb items={[breadcrumb]} title="KSQL List" />
        <IceContainer style={styles.container}>
          <Loading visible={this.state.isLoading} style={styles.loading}>
            <EditDialog
              visible={visible}
              handelDialog={this.handelDialog}
              fetchData={this.fetchList}
            />
            <Row style={styles.row}>
              <Col align="center">
                <span style={{ fontWeight: '600' }}>
                  Ksql Server Id:&nbsp;&nbsp;&nbsp;
                </span>
                <Input
                  placeholder="Ksql Server Id"
                  hasClear
                  onChange={this.handleFilterChange}
                  style={{ width: '300px' }}
                  value={this.state.searchValue}
                />
              </Col>
              <Col align="center">
                <Button type="secondary" onClick={this.handelDialog}>
                  <Icon type="add" />
                  New KsqlServer
                </Button>
              </Col>
            </Row>

            <Table dataSource={this.state.pageData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)}>
              <Table.Column
                title="KsqlServerId"
                dataIndex="ksqlServerId"
                sortable
                cell={renderKsqlServerId}
              />
              <Table.Column
                title="ClusterName"
                dataIndex="clusterName"
                sortable
              />
              <Table.Column title="Version" dataIndex="version" sortable />
              <Table.Column
                title="Ksql Healthy"
                dataIndex="ksqlHealthy"
                align="center"
                cell={renderHealth}
              />
              <Table.Column
                title="Kafka Healthy"
                dataIndex="kafkaHealthy"
                align="center"
                cell={renderHealth}
              />
              <Table.Column
                title="Metastore Healthy"
                dataIndex="metastoreHealthy"
                align="center"
                cell={renderHealth}
              />
              <Table.Column title="Operation" cell={this.renderOper} />
            </Table>
            <CustomPagination
              dataSource={this.state.filterDataSource}
              redrawPageData={this.redrawPageData}
            />
          </Loading>
        </IceContainer>
      </div>
    );
  }
}

const styles = {
  container: {
    margin: '20px',
    padding: '10px 20px 20px',
    minHeight: '600px',
  },
  loading: {
    width: '100%',
  },
  row: {
    margin: '4px 0 20px',
  },
  link: {
    margin: '0 5px',
    color: '#1111EE',
    cursor: 'pointer',
    textDecoration: 'none',
  },
  separator: {
    margin: '0 8px',
    display: 'inline-block',
    height: '12px',
    width: '1px',
    verticalAlign: 'middle',
    background: '#e8e8e8',
  },
  detailItem: {
    padding: '15px 0px',
    display: 'flex',
    borderTop: '1px solid #EEEFF3',
  },
  detailTitle: {
    marginRight: '30px',
    textAlign: 'right',
    width: '120px',
    color: '#999999',
  },
  detailBody: {
    flex: 1,
  },
  simpleFormDialog: { width: '500px' },
};
