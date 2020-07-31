/* eslint-disable array-callback-return */
import React, { Component } from 'react';
import { Table, Message, Select, Dialog } from '@alifd/next';
import axios from '@utils/axios';
import CustomPagination from '@components/CustomPagination';
import TableFilter from '@components/TableFilter';
import CheckDialog from '../CheckDialog';
import DetailDialog from '../DetailDialog';

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
export default class TopicList extends Component {
  state = {
    isLoading: false,
    dialogObj: {
      record: {},
      visible: false,
    },
    filterValue: 'all',
    dataSource: [],
    pageData: [],
    filterDataSource: [],
    detail:{
      data:{},
      viewVisable:false,
    }
  };

  componentDidMount() {
    this.fetchData();
  }

  componentWillMount() {
    this.mounted = true;
  }

  componentWillUnmount = () => {
    this.mounted = false;
  }


  handelDialog = () => {
    this.setState({
      dialogObj: {
        record: {},
        visible: !this.state.dialogObj.visible,
      },
    });

    this.fetchData();
  };

  handelCheck = record => () => {
    this.setState({
      dialogObj: {
        record,
        visible: !this.state.dialogObj.visible,
      },
    });
  };

  handelDetail = record => () =>{
    this.setState({
      detail:{
        data:record,
        viewVisable:!this.state.detail.viewVisable,
            
      }
    });
  }

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
                  filterDataSource: response.data.data,
                  isLoading: false,
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
    });
  };

  handleDelete = record => () => {
    Dialog.confirm({
      title: 'Delete Task',
      content: `confirm delete this topic task: ${record.topicName}?`,
      onOk: () => this.deleteTask(record.id),
    });
  }

  redrawPageData=(value) => {
    this.setState({
      pageData: value,
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

  renderOper = (value, index, record) => {
    let html;
    if (record.approved === 0) {
      html = (<span title="Check" ><a style={styles.link} onClick={this.handelCheck(record)}>Check</a></span>);
    } else if (record.approved === -1) {
      html = (<span title="Delete" ><a style={styles.link} onClick={this.handleDelete(record)}>Delete</a></span>);
    }
    return (
      <div>
        <span title="Detail" >
          <a style={styles.link} onClick={this.handelDetail(record)}>Detail</a>
        </span>
        {html}
      </div>
    );
  };

  renderStatus = (value) => {
    let context = <span style={{ 'color':'#ffd700'}}>To Approval</span>;
    switch (value) {
      case 1:
        context = <span style={{ 'color':'#00a000'}}>Approve</span>;
        break;
      case -1:
        context = <span style={{ 'color':'red'}}>Reject</span>;
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


  render() {
    const { isLoading, dialogObj,detail } = this.state;
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
    return (
      <div>
        <CheckDialog dialogObj={dialogObj} handelDialog={this.handelDialog} fetchData={this.fetchData} />
        <DetailDialog detail={detail}/>
        <TableFilter
          selecttext="Filter"
          selectCompont={select}
        />
        <Table loading={isLoading} dataSource={this.state.pageData} hasBorder={false} isZebra onSort={(value, order) => this.onSort(value, order)}>
          <Table.Column title="Topic Name" dataIndex="topicName" sortable width={250} />
          <Table.Column title="Location" dataIndex="location" width={250} />
          <Table.Column title="Cluster" dataIndex="clusterNames" width={250} />
          <Table.Column title="Approval Status" dataIndex="approved" width={200} cell={this.renderStatus} sortable />
          <Table.Column title="Comments" dataIndex="comments" />
          <Table.Column title="Operation" cell={this.renderOper} width={150} />
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
};
