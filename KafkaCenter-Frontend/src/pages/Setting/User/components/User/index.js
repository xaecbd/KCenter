import React, { Component } from 'react';
import { Table, Message, Icon, Dialog, Select, Grid, Input, Button } from '@alifd/next';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import { sortData, sortDataByOrder } from '@utils/dataFormat';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import axios from '@utils/axios';

const { Row, Col } = Grid;
export default class User extends Component {
  chanageSelect = (role, record) => {
    if (role === '') {
      Message.error('Please select role');
    } else {
      Dialog.confirm({
        title: 'Update User Role',
        content: `Update permissions for ${record.name} : [${role}]`,
        onOk: () => this.updateUserRole(record.id, role),
        okProps: { children: 'OK' },
        cancelProps: { children: 'Cancel' },
      });
    }
  }

  state = {
    isLoading: false,
    dataSource: [],
    selected: '',
    pageData: [],
    filterDataSource: [],
    oauthEnable: false,
    formValue: {},
    addUserVisable: false,
    title: 'Add User',
  };

  componentDidMount() {
  //  this.fetchData();
  }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  handleDelete = (record) => {
    Dialog.confirm({
      title: 'Delete User',
      content: `confirm delete this user: ${record.name}?`,
      onOk: () => this.deleteUser(record.id),
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
  }

  deleteUser = (userId) => {
    axios.delete(`/users/del/${userId}`).then((response) => {
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

  updateUserRole = (userId, role) => {
    axios.put('users/role', {
      id: userId,
      role,
    }).then((response) => {
      if (response.data.code === 200) {
        Message.success('Update role success');
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
      },
      () => {
        axios.get('/users').then((response) => {
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
          }
        }).catch((error) => {
          console.error(error);
        });
      }
    );
  };

  onSort(value, order) {
    const dataSource = sortDataByOrder(this.state.filterDataSource, value, order);
    this.refreshTableData(dataSource);
  }

  refreshTableData = (value) => {
    this.setState({
      filterDataSource: value,
    });
  }
  redrawPageData = (value) => {
    this.setState({
      pageData: value,
    });
  }

  validateAllFormField = () => {
    this.form.validateAll((errors, values) => {
      if (!errors) {
        axios.post('users/register', values).then((response) => {
          if (response.data.code === 200) {
            Message.success(response.data.message);
            this.changeUserVisable();
            this.fetchData();
          } else {
            Message.error(response.data.message);
          }
        }).catch((error) => {
          console.error(error);
        });
      }
    });
  }

   validateEmail = (rule, value, callback) => {
     const reg = /^[A-Za-z0-9\_\-\.]+@[A-Za-z0-9\_\-]+\.[A-Za-z]{2,}$/;
     if (!reg.test(value)) {
       return callback(new Error('email address is invaild,please check!'));
     }
     return callback();
   }

  onCancel=() => {
    this.changeUserVisable();
  }

  addUser = () => {
    this.setState({
      formValue: {},
    });
    this.changeUserVisable();
  }

  changeUserVisable = () => {
    this.setState({
      addUserVisable: !this.state.addUserVisable,
    });
  }

  renderOper = (value, index, record) => {
    return (
      <div style={styles.oper}>
        <span title="Delete" style={styles.operBtn} >
          <Icon size="xxs" type="close" onClick={() => { this.handleDelete(record); }} />
        </span>
      </div>
    );
  };


  renderRole = (value, index, record) => {
    const Option = Select.Option;
    return (
      <span title="Role">
        <Select id="basic-demo" value={record.role} defaultValue="" onChange={e => this.chanageSelect(e, record)} showSearch>
          <Option value="ADMIN">Admin</Option>
          <Option value="MEMBER">Member</Option>
        </Select>
      </span>
    );
  };

  addUserView = () => {
    const { isMobile } = this.state;
    const Option = Select.Option;
    // 响应式处理
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };
    if (isMobile) {
      simpleFormDialog.width = '300px';
    }
    const okProps = { children: 'OK' };
    const cancelProps = { children: 'Cancel' };
    return (
      <Dialog
        visible={this.state.addUserVisable}
        className="simple-form-dialog"
        onOk={this.validateAllFormField}
        onCancel={this.onCancel}
        footerAlign="center"
        isFullScreen
        onClose={this.onCancel}
        style={simpleFormDialog}
        title={this.state.title}
        okProps={okProps}
        cancelProps={cancelProps}
      >

        <IceFormBinderWrapper ref={(form) => {
          this.form = form;
        }}
          value={this.state.formValue}
          onChange={this.onFormChange}
        >

          <div style={styles.formContent}>
            <Row style={styles.formItem} >
              <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                User Name:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder name="name" required >
                  <Input
                    style={styles.inputItem}
                    placeholder="User name"
                  />
                </IceFormBinder>
                <IceFormError name="name" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                Email:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder name="email" required triggerType="onBlur" validator={this.validateEmail}>
                  <Input
                    style={styles.inputItem}

                    placeholder=""
                  />
                </IceFormBinder>
                <IceFormError name="email" />
              </Col>
            </Row>

            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                Role:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder name="role" required triggerType="onBlur">
                  <Select id="basic-demo" defaultValue="" style={styles.inputItem}>
                    <Option value="ADMIN">Admin</Option>
                    <Option value="MEMBER">Member</Option>
                  </Select>
                </IceFormBinder>
                <IceFormError name="role" />
              </Col>
            </Row>

            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                Password:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <span>123456</span>
              </Col>
            </Row>
          </div>

        </IceFormBinderWrapper>

      </Dialog>
    );
  }

  render() {
    const { isLoading } = this.state;
    const user = (
      <Col align="center">
        <Button type="secondary" onClick={this.addUser} ><Icon type="add" />Add User</Button>

      </Col>
    );

    return (
      <div>
        <CustomTableFilter
          dataSource={this.state.dataSource}
          refreshTableData={this.refreshTableData}
          refreshDataSource={this.fetchData}
          searchTitle="Filter"
          searchField="name"
          searchPlaceholder="Input User Name"
          otherComponent={user}
          id="settingUser"
        />
        <Table loading={isLoading} dataSource={this.state.pageData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)} primaryKey="id">
          <Table.Column title="Name" dataIndex="name" sortable />
          <Table.Column title="Email" dataIndex="email" />
          <Table.Column title="Role" dataIndex="role" cell={this.renderRole} />
          <Table.Column title="Operation" cell={this.renderOper} />
        </Table>
        <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
        {this.addUserView()}
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
  formContent: {
    alignItems: 'center',
  },

  formItems: {
    alignItems: 'center',
    // position: 'relative',
    marginTop: 0,
  },
  formItemes: {
    alignItems: 'center',
    float: 'right',
    marginTop: 0,
  },
  formItem: {
    alignItems: 'center',
    position: 'relative',
    marginTop: 20,
  },
  inputItem: {
    width: '100%',
  },
  simpleFormDialog: { width: '640px' },
  label: {
    textAlign: 'left',
    paddingLeft: '5%',
    lineHeight: '26px',
  },
};
