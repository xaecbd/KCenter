/* eslint-disable array-callback-return */
import React, { Component } from 'react';
import { Table, Message, Dialog, Button, Icon, Select } from '@alifd/next';
import axios from '@utils/axios';
import TableFilter from '@components/TableFilter';
import CustomPagination from '@components/CustomPagination';
import { resturctData } from '@utils/dataFormat';
import FormDialog from '../FormDialog';
import DetailDialog from '../DetailDialog';
import ReEditDialog from '../EditDialog';

const list = [
  {
    value: 'all',
    label: 'All',
  }, {
    value: 'toApproval',
    label: 'To Approval',
  }, {
    value: 'approved',
    label: 'Approved',
  }, {
    value: 'reject',
    label: 'Reject',
  },
];
export default class TaskTable extends Component {
  state = {
    isLoading: false,
    visible: false,
    dataSource: [],
    pageData: [],
    filterDataSource: [],
    filterValue: 'all',
    dialogObj: {
      record: {},
      visible: false,
    },
    updateObjDialog: {
      record: {},
      visible: false,
    },
    topicCreateSelectTeamData: [],
    taskConfig: {
      remoteLocations: [],
      ttl: '',
    },
  };

  componentDidMount() {
    this.getTaskConfig();
    this.fetchData();
  }
  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }


  handelDialog = () => {
    // 判断当前用户是否在team中，不在team 提示加入team,在team 将team值传入子组件
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.get('/team/userteam').then((response) => {
          if (response.data.code === 200 && response.data.data !== undefined && response.data.data.length > 0) {
            this.setState({
              isLoading: false,
              visible: !this.state.visible,
              topicCreateSelectTeamData: response.data.data,
            });
          } else {
            this.setState({
              isLoading: false,
            });
            Dialog.alert({
              title: 'Alert',
              content: response.data.message,
            });
          }
        })
          .catch((error) => {
            this.setState({
              isLoading: false,
            });
            console.log(error);
          });
      }
    );
  };

  hideDetailDialog = () => {
    this.setState({
      dialogObj: {
        record: {},
        visible: false,
      },
    });
  };

  hideUpdateDialog = () => {
    this.setState({
      updateObjDialog: {
        record: {},
        visible: false,
      },
    });
  };

  renderStatus = (value) => {
    let context = <span style={{ color: '#ffd700' }}>To Approval</span>;
    switch (value) {
      case 1:
        context = <span style={{ color: '#00a000' }}>Approve</span>;
        break;
      case -1:
        context = <span style={{ color: 'red' }}>Reject</span>;
        break;
      default:
        break;
    }
    return (
      <div>
        {context}
      </div>
    );
  };

  /**
   * 获取配置文件相关数据
   */
  getTaskConfig = () => {
    axios.get('/config').then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          const data = this.state.taskConfig;
          data.remoteLocations = resturctData(response.data.data.remotelocations, true);
          data.ttl = response.data.data.ttl;
          this.setState({
            taskConfig: data,
          });
        }
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
    });
  };

  fetchData = () => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.get('/topic/task/list')
          .then((response) => {
            if (response.data.code === 200) {
              if (this.mounted) {
                this.setState({
                  dataSource: response.data.data,
                  isLoading: false,
                  filterDataSource: response.data.data,
                });
                this.onfilter();
              }
            } else {
              Message.error(response.data.message);
            }
          })
          .catch((error) => {
            console.log(error);
          });
      }
    );
  };

  onfilter = () => {
    let newData = this.state.dataSource;
    const filterValue = this.state.filterValue;
    if (filterValue === 'all') {
      newData = this.state.dataSource;
    } else if (filterValue === 'toApproval') {
      newData = newData.filter(v => v.approved === 0);
    } else if (filterValue === 'reject') {
      newData = newData.filter(v => v.approved === -1);
    } else if (filterValue === 'approved') {
      newData = newData.filter(v => v.approved === 1);
    }
    this.setState(
      {
        filterDataSource: newData,
      }
    );
  };

  onSort(value, order) {
    let dataSource = [];
    if (value !== 'approved') {
      dataSource = this.state.filterDataSource.sort((a, b) => {
        a = a[value];
        b = b[value];
        if (order === 'asc') {
          return a.localeCompare(b);
        } else if (order === 'desc') {
          return b.localeCompare(a);
        }
      });
    } else {
      dataSource = this.state.filterDataSource.sort((a, b) => {
        if (order === 'asc') {
          return a[value] - b[value];
        } else if (order === 'desc') {
          return b[value] - a[value];
        }
      });
    }
    this.setState({
      filterDataSource: dataSource,
    });
  }


  handelDetail = record => () => {
    this.setState({
      dialogObj: {
        record,
        visible: !this.state.dialogObj.visible,
      },
    });
  };
  handleFilterChange = (value) => {
    let data = this.state.dataSource;
    if (value === 'all') {
      data = this.state.dataSource;
    } else if (value === 'toApproval') {
      data = data.filter(v => v.approved === 0);
    } else if (value === 'reject') {
      data = data.filter(v => v.approved === -1);
    } else if (value === 'approved') {
      data = data.filter(v => v.approved === 1);
    }
    this.setState({
      filterDataSource: data,
      filterValue: value,
    }, () => {
      this.onfilter();
    });
  };
  redrawPageData=(value) => {
    this.setState({
      pageData: value,
    });
  }

  renderOper = (value, index, record) => {
    if (record.approved === -1) {
      return (
        <div>
          <span title="Detail" >
            <a style={styles.link} onClick={this.handelDetail(record)}>Detail</a>
          </span>
          <span style={styles.separator} />
          <span title="Delete" >
            <a style={styles.link} onClick={this.handleDelete(record)}>Delete</a>
          </span>
          <span style={styles.separator} />
          <span title="re-edit" >
            <a style={styles.link} onClick={this.handelUpdate(record)}>Edit</a>
          </span>
        </div>
      );
    } else if (record.approved === 0) {
      return (
        <div>
          <span title="Detail" >
            <a style={styles.link} onClick={this.handelDetail(record)}>Detail</a>
          </span>
          <span style={styles.separator} />
          <span title="Delete" >
            <a style={styles.link} onClick={this.handleDelete(record)}>Delete</a>
          </span>
        </div>
      );
    }
    return (
      <div>
        <span title="Detail" >
          <a style={styles.link} onClick={this.handelDetail(record)}>
            Detail
          </a>
        </span>
      </div>
    );
  };

  handelUpdate = record => () => {
    this.setState({
      updateObjDialog: {
        record,
        visible: !this.state.updateObjDialog.visible,
      },
    });
  };

  handleDelete = record => () => {
    Dialog.confirm({
      title: 'Delete Task',
      content: `confirm delete this topic task: ${record.topicName}?`,
      onOk: () => this.deleteTask(record.id),
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
  }

  deleteTask = (taskId) => {
    axios.delete(`/topic/task/${taskId}`).then((response) => {
      if (response.data.code === 200) {
        Message.success('Delete success');
        this.fetchData();
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
    });
  }

  render() {
    const { isLoading, visible, dialogObj, taskConfig, updateObjDialog, topicCreateSelectTeamData } = this.state;
    const select = (<Select
      showSearch
      dataSource={list}
      placeholder="please select cluster"
      style={{ width: '300px' }}
      onChange={(value) => {
        this.handleFilterChange(value);
      }}
      value={this.state.filterValue}
    />);
    const view = <Button type="secondary" onClick={this.handelDialog}><Icon type="add" />Create Topic Task</Button>;
    return (
      <div>
        <DetailDialog dialogObj={dialogObj} hideDetailDialog={this.hideDetailDialog} />
        <FormDialog taskConfig={taskConfig} visible={visible} handelDialog={this.handelDialog} fetchData={this.fetchData} topicCreateSelectTeamData={topicCreateSelectTeamData} />
        <ReEditDialog taskConfig={taskConfig} updateObjDialog={updateObjDialog} hideUpdateDialog={this.hideUpdateDialog} fetchData={this.fetchData} />
        <TableFilter components={view} selecttext="Filter" selectCompont={select} />
        <Table loading={isLoading} dataSource={this.state.pageData} hasBorder={false} isZebra onSort={(value, order) => this.onSort(value, order)}>
          <Table.Column title="Topic Name" dataIndex="topicName" sortable width={250} />
          <Table.Column title="Location" dataIndex="location" sortable width={250} />
          <Table.Column title="Approval Status" dataIndex="approved" width={200} cell={this.renderStatus} sortable />
          <Table.Column title="Comments" dataIndex="comments" />
          <Table.Column title="Operation" cell={this.renderOper} width={210} />
        </Table>
        <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
      </div>
    );
  }
}

const styles = {
  link: {
    margin: '0 5px',
    color: 'rgba(49, 128, 253, 0.65)',
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
};
