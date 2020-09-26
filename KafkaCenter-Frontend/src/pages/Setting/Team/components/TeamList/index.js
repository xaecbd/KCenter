import React, { Component } from 'react';
import { Table, Message, Icon, Dialog, Button, Grid } from '@alifd/next';
import axios from '@utils/axios';
import Auth from '@components/Auth'
import { sortData, sortDataByOrder } from '@utils/dataFormat';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import TeamInfoDialog from '../TeamInfo';

const { Col } = Grid;

const styles = {
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
};
export default class TeamList extends Component {
  constructor(){
    super();
    this.state = {
      isLoading: false,
      dataSource: [],
      teamInfoDialog: false,
      teamInfoData: {},
      teamInfoDialogTitle: '',
      filterDataSource: [],
      pageData: [],
    };
  }

  componentWillMount() {
    this.mounted = true;
  }

  componentWillUnmount = () => {
    this.mounted = false;
  }

  onSort(value, order) {
    const dataSource = sortDataByOrder(this.state.filterDataSource, value, order);
    this.refreshTableData(dataSource);
  }

  handleEdit = (record) => {
    this.setState({
      teamInfoDialog: true,
      teamInfoDialogTitle: 'Edit Team',
      teamInfoData: Object.create(
        Object.getPrototypeOf(record),
        Object.getOwnPropertyDescriptors(record)
      ),
    });
  };

  addNewTeam = () => {
    this.setState({
      teamInfoDialog: true,
      teamInfoDialogTitle: 'New Team',
      teamInfoData: {},
    });
  }

  hideTeamInfoDialog = () => {
    this.setState({
      teamInfoDialog: false,
      teamInfoData: {},
      teamInfoDialogTitle: '',
    });
  }

  handleDelete = (record) => {
    Dialog.confirm({
      title: 'Delete Team',
      content: `confirm delete this team: ${record.name}?`,
      onOk: () => this.deleteTeam(record.id),
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
  }

  handleAddUser = (record) => {
    this.props.showTeamUserInfo(record);
  }

  deleteTeam = (teamId) => {
    axios.delete(`/team/del/${teamId}`).then((response) => {
      if (response.data.code === 200) {
        this.fetchData();
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
    });
  }

  fetchData = () => {
    this.setState(
      {
        isLoading: true,
        teamInfoDialog: false,
      },
      () => {
        axios.get('/team/').then((response) => {
          if (response.data.code === 200) {
            if (this.mounted) {
              const data = sortData(response.data.data, 'name');
              this.setState({
                dataSource: data,
                isLoading: false,
                filterDataSource: data,
              });
              // this.handlePaginationChange();
            }
          } else {
            Message.error(response.data.message);
            this.setState({
              isLoading: false,
            });
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

  

 


  renderOper = (value, index, record) => {
    return (
      <div style={styles.oper}>
        <span
          title="AddUser"
          style={styles.operBtn}
        >
          <Icon size="xs" type="account" onClick={() => { this.handleAddUser(record); }} />
        </span>
        <Auth rolename="admin">
          <span style={styles.separator} />
          <span title="Edit" style={styles.operBtn} >
            <Icon size="xs" type="edit" onClick={() => { this.handleEdit(record); }} />
          </span>
          <span style={styles.separator} />
          <span title="Delete" style={styles.operBtn} >
            <Icon size="xs" type="close" onClick={() => { this.handleDelete(record); }} />
          </span>
        </Auth>
      </div>
    );
  };


  render() {
    const { isLoading } = this.state;
    const team = (
      <Col align="center">
        <Auth rolename="admin">
          <Button type="secondary" onClick={this.addNewTeam} ><Icon type="add" />New Team</Button>
        </Auth>
      </Col>
    );
    return (
      <div>
        <TeamInfoDialog
          title={this.state.teamInfoDialogTitle}
          teamInfo={this.state.teamInfoData}
          visible={this.state.teamInfoDialog}
          hideTeamInfoDialog={this.hideTeamInfoDialog}
          fetchData={this.fetchData}
        />
        <CustomTableFilter
          dataSource={this.state.dataSource}
          refreshTableData={this.refreshTableData}
          refreshDataSource={this.fetchData}
          searchTitle="Filter"
          searchField="name"
          searchPlaceholder="Input team Name"
          otherComponent={team}
          id="settingTeam"
        />
        <Table loading={isLoading} dataSource={this.state.pageData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)} primaryKey="id">
          <Table.Column title="Name" dataIndex="name" sortable />
          <Table.Column title="Alarm Group" dataIndex="alarmGroup"/>
          <Table.Column title="Operation" cell={this.renderOper} />
        </Table>
        <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
      </div>
    );
  }
}


