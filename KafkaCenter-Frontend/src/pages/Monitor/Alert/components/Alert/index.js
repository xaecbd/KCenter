import React, { Component } from 'react';
import { Table, Message, Icon, Loading, Dialog, Button, Grid, Tag, Switch } from '@alifd/next';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import { sortDataByOrder } from '@utils/dataFormat';
import { getPersonalityCluster } from '@utils/cookies';
import axios from '@utils/axios';
import FoundationSymbol from '@icedesign/foundation-symbol';
import EditDialog from '../EditDialog';
import style from  '../../index.module.scss';

const { Col } = Grid;
export default class Alter extends Component {
  state = {
    loading: false,
    visible: false,
    filterDataSource: [],
    dataSource: [],
    pageData: [],
    endValue: {
      clusterId: '',
      topicName: '',
      consummerGroup: '',
      consummerApi: 'ALL',
      threshold: '',
      diapause: '',
      disableAlerta: false,
      mailTo: '',
      webhook: '',
      disabled: false,
      enable: true,
    },
  };

  //   componentDidMount() {
  //     this.fetchData(getPersonalityCluster('monitorAlert').id);
  //   }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  };

  fetchData = (clusterId) => {
    if (clusterId == null || clusterId == '' || clusterId == undefined) {
      clusterId = getPersonalityCluster('monitorAlert').id;
    }
    this.setState(
      {
        loading: true,
      },
      () => {
        axios
          .get(`/monitor/alert?cluster=${clusterId}`)
          .then((response) => {
            if (response.data.code === 200) {
              if (this.mounted) {
                let data = response.data.data;
                const oldData = data;
                const searhValue = sessionStorage.getItem('monitorAlertSenarch');
                if (searhValue !== undefined && searhValue != null) {
                  data = data.filter(v =>
                    v.topicName.toLowerCase()
                      .search(searhValue.toLowerCase()) !== -1
                    // console.log('v:'+typeof v.topicName)

                  );
                }
                this.setState(
                  {
                    filterDataSource: data,
                    dataSource: oldData,
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

  handleDelete = (record) => {
    Dialog.confirm({
      content: 'Do you want to Delete?',
      onOk: () => {
        this.handleDeletes(record);
      },
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
  };

  handleDeletes = (record) => {
    this.setState(
      {
        loading: true,
      },
      () => {
        axios
          .delete(`/monitor/alert/delete/${record.id}`)
          .then((response) => {
            if (response.data.code === 200) {
              this.fetchData();

              Message.success(response.data.message);
            } else {
              Message.error(response.data.message);
            }
            this.setState({
              loading: false,
            });
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  };
  redrawPageData=(value) => {
    this.setState({
      pageData: value,
    });
  }

  handleEdit = (record) => {
    const data = record;
    const obj = Object.create(data);
    obj.clusterId = record.clusterId;
    obj.consummerGroup = record.consummerGroup;
    obj.consummerApi = record.consummerApi;
    obj.threshold = record.threshold;
    obj.mailTo = record.mailTo;
    obj.diapause = record.diapause;
    obj.webhook = record.webhook;
    obj.clusterName = record.clusterName;
    obj.topicName = record.topicName;
    obj.disableAlerta = record.disableAlerta;
    obj.disabled = true;
    obj.enable = record.enable;
    obj.id = record.id;
    
    this.setState({
      visible: !this.state.visible,
      endValue: obj,
    });
  };

  handleSwitch = (record) => {
    Dialog.confirm({
      title: record.enable ? 'Disable Alert' : 'Enable Alert',
      content: this.renderSwitch(record),
      onOk: () => {
        this.handleChange(record);
      },
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },

    });
  }

  renderSwitch = (record) => {
    if (record.enable) {
      return 'Do you want to disable this task?';
    }
    return 'Do you want to enable this task?';
  }

  handleChange = (record) => {
    const data = record;
    data.enable = !record.enable;
    if (!record.disableAlerta) {
      data.disableAlerta = !record.disableAlerta;
    }
    this.setState(
      {
        loading: true,
      },
      () => {
        axios
          .put('/monitor/alert/update/enable', data)
          .then((response) => {
            if (response.data.code === 200) {
              this.fetchData();
              Message.success(response.data.message);
            } else {
              Message.error(response.data.message);
            }
            this.setState({
              loading: false,
            });
          })
          .catch((error) => {
            console.error(error);
          });
      }
    );
  }
  // monitor
  renderOper = (value, index, record) => {
    const bell = record.enable == true ? styles.green : styles.red;
    return (
      <div style={styles.oper}>
        <span title="Edit" style={styles.operBtn}>
          <FoundationSymbol size="small"
            type="edit2"
            onClick={() => {
                const recordValue = this.state.pageData[index];
                this.handleEdit(recordValue);
          }}
          />
        </span>
        <span style={styles.separator} />
        <span title={record.enable ? 'Disable Alert' : 'Enable Alert'} style={styles.operBtn} style={bell}>
          <FoundationSymbol size="small"
            type="bell"
            onClick={() => {
            this.handleSwitch(record);
          }}
          />
        </span>

        <span style={styles.separator} />
        <span title="Delete" style={styles.operBtn}>
          <FoundationSymbol size="small"
            type="cross"
            onClick={() => {
                this.handleDelete(record);
          }}
          />
        </span>
      </div>
    );
  };

  renderApi = (value, index, record) => {
    return (
      <div style={styles.oper}>
        <span title={value}> <Tag className={style.tags} size="small">{value}</Tag></span>
      </div>
    );
  }

  renderEnable = (value, index, record) => {
    return (
      <div style={styles.oper}>
        <span title={value ? 'enable' : 'disable'}> <Tag size="small" type="primary" color={value === true ? 'green' : 'red'}>{value ? 'Y' : 'N'}</Tag></span>
      </div>
    );
  }
  handelDialog = () => {
    const data = {
      clusterId: '',
      topicName: '',
      consummerGroup: '',
      consummerApi: 'ALL',
      threshold: 10,
      diapause: '',
      mailTo: '',
      webhook: '',
      disabled: false,
    };
    this.setState({
      visible: !this.state.visible,
      endValue: data,
    });
  };

  hideDialog = () => {
    this.setState({
      visible: !this.state.visible,
    });
  }

  render() {
    const { isLoading } = this.state;
    const view = (<Col align="center"><Button type="secondary" onClick={this.handelDialog}><Icon type="add" />Create Alert</Button></Col>);
    return (
      <div>
        <Loading visible={this.state.loading} style={styles.loading}>
          <EditDialog
            visible={this.state.visible}
            handelDialog={this.hideDialog}
            fetchData={this.fetchData}
            value={this.state.endValue}
          />
          <CustomTableFilter
            dataSource={this.state.dataSource}
            refreshTableData={this.refreshTableData}
            refreshDataSource={this.fetchData}
            selectTitle="Cluster"
            selectField="clusterName"
            searchTitle="Filter"
            searchField="topicName,consummerGroup"
            searchPlaceholder="Input Topic Or Group Name"
            otherComponent={view}
            id="monitorAlert"
          />
          <Table
            loading={isLoading}
            dataSource={this.state.pageData}
            hasBorder={false}
            onSort={(value, order) => this.onSort(value, order)}
          >
            <Table.Column title="Cluster" dataIndex="clusterName" />
            <Table.Column title="Topic Name" dataIndex="topicName" sortable />
            <Table.Column title="Consummer Group" dataIndex="consummerGroup" />
            <Table.Column title="Consumer Api" dataIndex="consummerApi" cell={this.renderApi} />
            <Table.Column title="Threshold" dataIndex="threshold" />
            <Table.Column title="Owner" dataIndex="owner" />
            <Table.Column title="Enable" dataIndex="enable" cell={this.renderEnable} />
            <Table.Column title="Operation" cell={this.renderOper} />
          </Table>
          <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
        </Loading>
      </div>
    );
  }
}

const styles = {
  loading: {
    width: '100%',
  },
  separator: {
    margin: '0 8px',
    display: 'inline-block',
    height: '12px',
    width: '1px',
    verticalAlign: 'middle',
    background: '#e8e8e8',
  },
  operBtn: {
    display: 'inline-block',
    width: '24px',
    height: '24px',
    borderRadius: '999px',
    color: '#929292',
    background: '#f2f2f2',
    textAlign: 'center',
    cursor: 'pointer',
    lineHeight: '24px',
    marginRight: '6px',
  },
  tags: {
    borderColor: '#d9ecff !important',
    backgroundColor: '#ecf5ff !important',
    color: '#409eff !important',

  },
  green: {
    color: 'green',
    display: 'inline-block',
    width: '24px',
    height: '24px',
    borderRadius: '999px',
    background: '#f2f2f2',
    textAlign: 'center',
    cursor: 'pointer',
    lineHeight: '24px',
    marginRight: '6px',
  },
  red: {
    color: 'red',
    display: 'inline-block',
    width: '24px',
    height: '24px',
    borderRadius: '999px',
    background: '#f2f2f2',
    textAlign: 'center',
    cursor: 'pointer',
    lineHeight: '24px',
    marginRight: '6px',
  },
};
