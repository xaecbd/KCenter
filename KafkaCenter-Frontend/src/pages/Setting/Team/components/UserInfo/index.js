import React, { Component } from 'react';
import FoundationSymbol from '@icedesign/foundation-symbol';
import { Table, Message, Icon, Dialog } from '@alifd/next';
import axios from '@utils/axios';
import TeamAddUser from '../AddUser';

export default class UserInfo extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isLoading: false,
      teamInfoData: this.props.teamInfoData,
      userInfoDataList: [],
      selectUserDatas: [],
    };
  }

  handleDelete = (record) => {
    Dialog.confirm({
      title: 'Delete User',
      content: `confirm remove this User: ${record.name}?`,
      onOk: () => this.deleteUser(record.id),
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
  }

  refreshData = (teamId) => {
    this.fetchNotExistInTeamUsers(teamId);
    this.fetchUserInfoByTeamId(teamId);
  }

  fetchNotExistInTeamUsers = (teamId) => {
    axios.get(`/users/all/${teamId}`).then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          this.setState({
            selectUserDatas: response.data.data,
          });
        }
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
    });
  }

  deleteUser = (userId) => {
    const teamId = this.state.teamInfoData.id;
    axios.delete(`/team/del/user?userId=${userId}&teamId=${teamId}`).then((response) => {
      if (response.data.code === 200) {
        this.refreshData(teamId);
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      Message.error('server error!');
      console.error(error);
    });
  }

  componentDidMount() {
    this.refreshData(this.state.teamInfoData.id);
  }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      teamInfoData: nextProps.teamInfoData,
    });
  }

  fetchUserInfoByTeamId = (teamid) => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.get(`/team/userinfos/${teamid}`).then((response) => {
          if (response.data.code === 200) {
            if (this.mounted) {
              this.setState({
                userInfoDataList: response.data.data,
                isLoading: false,
              });
            }
          } else {
            Message.error(response.data.message);
          }
        }).catch((error) => {
          Message.error('server error!');
          console.error(error);
        });
      }
    );
  }

  onSort(value, order) {
    let dataSource = [];
    dataSource = this.state.userInfoDataList.sort((a, b) => {
      a = a[value];
      b = b[value];
      if (order === 'asc') {
        return a.localeCompare(b);
      }
      return b.localeCompare(a);
    });
    this.setState({ userInfoDataList: dataSource });
  }

  backward = () => {
    this.props.teamUserShow();
  }

  renderOper = (value, index, record) => {
    const username = record.name;
    const json = JSON.parse(sessionStorage.getItem('user'));
    const currentUser = json.name;
    const role = json.role;
    if (role === 'ADMIN') {
      return (
        <div style={styles.oper}>
          <span title="Delete" style={styles.operBtn}><Icon size="xs" type="close" onClick={() => { this.handleDelete(record); }} /></span>
        </div>
      );
    } else if (username === currentUser) {
      return (
        <div style={styles.oper}>
          <span title="Delete" style={styles.operBtn}><Icon size="xs" type="close" onClick={() => { this.handleDelete(record); }} /></span>
        </div>
      );
    }
  };

  render() {
    const isLoading = this.state.isLoading;
    const userData = this.state.userInfoDataList;
    const teamId = this.state.teamInfoData.id;
    const selectUserDatas = this.state.selectUserDatas;
    return (
      <div>
        <FoundationSymbol onClick={() => this.backward()} style={styles.backward} size="large" type="backward" /><span style={{ fontSize: '16px', fontWeight: '500' }}>{this.props.teamInfoData.name}</span>
        <TeamAddUser teamId={teamId} selectUserDatas={selectUserDatas} refreshData={this.refreshData} />
        <Table loading={isLoading} dataSource={userData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)} primaryKey="id">
          <Table.Column title="Name" dataIndex="name" sortable />
          <Table.Column title="Email" dataIndex="email" />
          <Table.Column title="Operation" cell={this.renderOper} />
        </Table>
      </div>
    );
  }
}

const styles = {
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
  backward: {
    display: 'inline-block',
    minWidth: '40px',
    marginBottom: '15px',
    cursor: 'pointer',
    color: '#0066FF',
  },
};
