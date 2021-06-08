import React, { Component } from 'react';
import { Table, Message, Dialog, Grid, Select } from '@alifd/next';
import FoundationSymbol from '@icedesign/foundation-symbol';
import dayjs from 'dayjs';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import axios from '@utils/axios';
import Auth from '@components/Auth'
import {  sort,bytesToSize } from '@utils/dataFormat';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';

import { getPersonalityCluster } from '@utils/cookies';
import Producer from '../Producer';
import DetailDialog from '../DetailDialog';
import { withRouter } from 'react-router-dom';


const { Row, Col } = Grid;

@withRouter
export default class TopicTable extends Component {
  

  constructor(){
    super();
    this.state = {
      isQueries: false,
      queriesRecord: {},
      isLoading: false,
      pageData: [],
      isMobile: false,
      uvisable: false,
      dialogObj: {
        record: {},
        visible: false,
      },
      value: {},
      teamData: [],
      ownerData: [],
      spanCluster: '',
      filterDataSource: [],
      dataSource: [],
    };
  }

  componentWillMount() {
    this.mounted = true;
  }

  componentWillUnmount = () => {
    this.mounted = false;
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (this.state.clusterData !== nextState.clusterData) {
      return false;
    }
    return true;
  }

  fetchTeam = () => {
    axios.get('/team/').then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          // eslint-disable-next-line no-undef
          const data = this.resouceData(response.data.data);
          this.setState({
            teamData: data,
          });
        }
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
    });
  }

  fetchUser = (teamId) => {
    axios.get(`/team/userinfos/${teamId}`).then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          // eslint-disable-next-line no-undef
          const data = this.resouceData(response.data.data);
          this.setState({
            ownerData: data,
          });
        }
      } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
    });
  }

  resouceData = (data) => {
    const dataSource = [];
    data.forEach((obj) => {
      const entry = {
        value: obj.id,
        label: obj.name,
      };
      dataSource.push(entry);
    });
    return dataSource;
  };

  fetchData = (clusterId) => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.get(`/topic/list?cluster=${clusterId}`).then((response) => {
          if (response.data.code === 200) {
            if (this.mounted) {
              const switchValue = sessionStorage.getItem('topicTopicListSwitch') == null ? false : sessionStorage.getItem('topicTopicListSwitch');
              let data = sort(response.data.data, 'topicName');
              const oldData = data;
              if (!switchValue || switchValue === 'false') {
                data = data.filter(v => !v.topicName.startsWith('_'));
              }
              const searhValue = sessionStorage.getItem('topicTopicListSearch');
              if (searhValue !== undefined && searhValue != null && searhValue !== '') {
                data = data.filter(v =>
                  v.topicName.toLowerCase()
                    .search(searhValue.toLowerCase()) !== -1
                );
              }

              this.setState({
                filterDataSource: data,
                dataSource: oldData,
                isLoading: false,
              });
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
    const dataSource = sort(this.state.dataSource, value, order);
    this.refreshTableData(dataSource);
  }

  refreshTableData = (value) => {
    this.setState({
      filterDataSource: value,
    });
  }

  renderBytes= (value) => {
    if (value !== null && value) {
      if(value===-1){
        return '-';
      }
      return bytesToSize(value);
    }else if(value===0){
      return bytesToSize(0);
    }
    return '-';
  };


  handelDetail = (record) => {
    this.setState({
      dialogObj: {
        record,
        visible: !this.state.dialogObj.visible,
      } });
  };

  handelQueries = (record) => {
    this.setState(
      {
        isQueries: true,
        queriesRecord: record,
      }
    );
  }

  handleUpdate = (record) => {
    this.fetchTeam();
    const clusterName = record.cluster.name;
    this.setState({
      uvisable: !this.state.uvisable,
      value: record,
      spanCluster: clusterName,
    });
  }

  renderOper = (value, index, record) => {
    return (
      <div>
        <a style={styles.link} onClick={() => this.handelDetail(record)}>
          Detail
        </a>
        <span style={styles.separator} />
        <span title="Mock" style={styles.operBtn} >
          <a style={styles.link} onClick={() => this.handelQueries(record)}>
            Mock
          </a>
        </span>
        <Auth rolename="admin">
          <span style={styles.separator} />
          <span title="Modify" style={styles.operBtn} >
            <a style={styles.link} onClick={() => this.handleUpdate(record)}>
              Modify
            </a>
          </span>
        </Auth>
      </div>
    );
  };

  hideDetailDialog= () => {
    this.setState({
      dialogObj: {
        record: {},
        visible: false,
      },
    });
  };

  backward = () => {
    this.setState(
      {
        isQueries: false,
      }
    );
    const ids = getPersonalityCluster('topicTopicList').id;
    this.fetchData(ids);
  }

  onOk = () => {
    this.refForm.validateAll((validateError) => {
      if (validateError) {
        return;
      }
      const value = this.state.value;
      axios.post('/topic/update', value).then((response) => {
        if (response.data.code === 200) {
          if (this.mounted) {
            Message.success(response.data.message);
          }
        } else {
          Message.error(response.data.message);
        }
        this.onCancel();
        const ids = getPersonalityCluster('topicTopicList').id;
        this.fetchData(ids);
      }).catch((error) => {
        console.error(error);
      });
    });
  };

  onCancel = () => {
    this.setState({
      uvisable: !this.state.uvisable,
    });
  }

  onTeamChange = (value) => {
    this.fetchUser(value);
  }


  renderTopic = (value) => {
    return value ? value.name : '';
  }

  renderTime = (value) => {
    if (value) {
      return dayjs(value).format('YYYY-MM-DD HH:mm:ss');
    }
    return '-';
  }

  redrawPageData=(value) => {
    this.setState({
      pageData: value,
    });
  }

  render() {
    const { isLoading, dialogObj, isQueries, isMobile } = this.state;
    let view = null;
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };

    const okProps = { children: 'OK' };
    const cancelProps = { children: 'Cancel' };
    // 响应式处理
    if (isMobile) {
      simpleFormDialog.width = '300px';
    }
    if (isQueries) {
      view = (
        <div>
          <span><h3 style={{ margin: '0px 0px 0px', fontSize: '20px', fontWeight: '500', color: 'rgba(0, 0, 0, 0.85)' }}><FoundationSymbol onClick={() => this.backward()} style={styles.backward} size="large" type="backward" />Kafka Producer</h3></span>
          <Producer record={this.state.queriesRecord} />
        </div>);
    } else {
      view = (
        <div>
          <DetailDialog dialogObj={dialogObj} hideDetailDialog={this.hideDetailDialog} />
          <CustomTableFilter
            dataSource={this.state.dataSource}
            refreshTableData={this.refreshTableData}
            refreshDataSource={this.fetchData}
            selectTitle="Cluster"
            selectField="cluster.name"
            searchTitle="Filter"
            searchField="topicName"
            searchPlaceholder="Input Topic Name"
            switchField="topicName"
            id="topicTopicList"
          />
          <Table loading={isLoading} dataSource={this.state.pageData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)}>
            <Table.Column title="Topic Name" dataIndex="topicName" sortable />
            <Table.Column title="File Size" dataIndex="fileSize" cell={this.renderBytes} sortable />
            <Table.Column title="Cluster" dataIndex="cluster.name" />
            <Table.Column title="Create Date" dataIndex="createTime" cell={this.renderTime} />
            <Table.Column title="Owner" dataIndex="owner" cell={this.renderTopic} />
            <Table.Column title="Team" dataIndex="team" cell={this.renderTopic} />
            <Table.Column title="Operation" cell={this.renderOper} />
          </Table>
          <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
          <Dialog
            visible={this.state.uvisable}
            className="simple-form-dialog"
            onOk={this.onOk}
            onCancel={this.onCancel}
            footerAlign="center"
            autoFocus={false}
            isFullScreen
            onClose={this.onCancel}
            style={simpleFormDialog}
            closeable="esc,mask,close"
            title="Update"
            okProps={okProps}
            cancelProps={cancelProps}
          >
            <IceFormBinderWrapper ref={(ref) => {
              this.refForm = ref;
            }}
            value={this.state.value}
            >
              <div >
                <Row style={styles.formRow} >
                  <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                    Cluster:
                  </Col>
                  <Col span={`${isMobile ? '18' : '16'}`}>
                    <span style={{ width: '100%' }}>{this.state.spanCluster}</span>
                  </Col>
                </Row>
                <Row style={styles.formRow} >
                  <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                    Topic Name:
                  </Col>
                  <Col span={`${isMobile ? '18' : '16'}`}>

                    <span style={{ width: '100%' }}>{this.state.value.topicName}</span>

                  </Col>
                </Row>
                <Row style={styles.formRow} >
                  <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                    Team:
                  </Col>
                  <Col span={`${isMobile ? '18' : '16'}`}>
                    <IceFormBinder name="teamId" required message="Required">
                      <Select
                        showSearch
                        dataSource={this.state.teamData}
                        placeholder="please select team"
                        style={{ width: '100%' }}
                        onChange={(value, action, item) => {
                          this.onTeamChange(value, action, item);
                        }}
                        disabled={this.state.value.disabled}
                      />
                    </IceFormBinder>
                    <IceFormError name="teamId" />
                  </Col>
                </Row>
                <Row style={styles.formRow}>
                  <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                    Owner:
                  </Col>
                  <Col span={`${isMobile ? '18' : '16'}`}>
                    <IceFormBinder name="ownerId" required message="Required">
                      <Select
                        showSearch
                        dataSource={this.state.ownerData}
                        placeholder="please select team user"
                        style={{ width: '100%' }}
                        disabled={this.state.value.disabled}
                      />
                    </IceFormBinder>
                    <IceFormError name="ownerId" />
                  </Col>
                </Row>
              </div>

            </IceFormBinderWrapper>
          </Dialog>
        </div>
      );
    }
    return view;
  }
}

const styles = {
  link: {
    margin: '0 5px',
    color: 'rgba(49, 128, 253, 0.65)',
    cursor: 'pointer',
    textDecoration: 'none',
    fontSize:'13px',
    textOverflow:'ellipsis',
    overflow:'hidden',
    whiteSpace: 'nowrap'
  },
  separator: {
    margin: '0 8px',
    display: 'inline-block',
    height: '12px',
    width: '1px',
    verticalAlign: 'middle',
    background: '#e8e8e8',
  },
  backward: {
    display: 'inline-block',
    minWidth: '40px',
    marginBottom: '15px',
    cursor: 'pointer',
    color: '#0066FF',
  },
  formRow: { marginTop: 20 },
  simpleFormDialog: { width: '640px' },
  
  test:{
  //  margin: 0 0 8px;
    fontSize:'13px',
    textOverflow:'ellipsis',
    overflow:'hidden',
    whiteSpace: 'nowrap'
  }
};
