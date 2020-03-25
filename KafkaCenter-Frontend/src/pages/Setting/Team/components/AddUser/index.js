import React, { Component } from 'react';
import { Grid, Button, Message, Select } from '@alifd/next';
import axios from '@utils/axios';

const { Row, Col } = Grid;

export default class AddUser extends Component {
  constructor(props) {
    super(props);
    this.state = {
      selectUserDatas: this.props.selectUserDatas,
      teamId: this.props.teamId,
      selectValue: '',
      addUserButton: true,
    };
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      selectUserDatas: nextProps.selectUserDatas,
      teamId: nextProps.teamId,
    });
  }

  onClick = () => {
    const userId = this.state.selectUserId;
    const teamId = this.state.teamId;
    axios.post('/team/adduser', { userId: `${userId}`, teamId: `${teamId}` }).then((response) => {
      if (response.data.code === 200) {
        Message.success(response.data.message);
        this.refreshData(teamId);
        this.setState({
          selectValue: '',
          addUserButton: true,
        });
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
    });
  }

  refreshData = (teamId) => {
    this.props.refreshData(teamId);
  }

  onSelectChange = (value, key, extra) => {
    this.setState({
      selectUserId: value,
      selectValue: extra.label,
      addUserButton: false,
    });
  }

  render() {
    const selectUsersData = this.state.selectUserDatas;
    return (
      <div>
        <Row style={styles.row}>
          <Col>
            <Select
              showSearch
              dataSource={selectUsersData}
              placeholder="please select user"
              style={{ width: 300 }}
              onChange={this.onSelectChange}
              value={this.state.selectValue}
            />
            <Button type="secondary" disabled={this.state.addUserButton} onClick={this.onClick} >Add User To Team</Button>
          </Col>
        </Row>
      </div>
    );
  }
}

const styles = {
  row: {
    margin: '4px 0 20px',
  },
};
