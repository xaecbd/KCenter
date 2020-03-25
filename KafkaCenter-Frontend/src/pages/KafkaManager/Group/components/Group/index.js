import React, { Component } from 'react';
import { isNullOrUndefined } from 'util';
import { withRouter } from 'react-router-dom';
import { Table, Message, Loading, Icon, Dialog, Grid, Select, Input } from '@alifd/next';
import axios from '@utils/axios';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import FoundationSymbol from '@icedesign/foundation-symbol';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import { sortDataByOrder } from '@utils/dataFormat';
import { getPersonalityCluster } from '@utils/cookies';

const { Row, Col } = Grid;
@withRouter
export default class GroupTable extends Component {
  state = {
    isLoading: false,
    pageData: [],
    filterDataSource: [],
    dataSource: [],
    isMobile: false,
    visable: false,
    value: {},
    topicInfo: [],
  };


  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  fetchData = (clusterId) => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.get(`/manager/group?cluster=${clusterId}`).then((response) => {
          if (response.data.code === 200) {
            const data = sortDataByOrder(response.data.data, 'consummerGroup', 'asc');
            if (this.mounted) {
              this.setState({
                filterDataSource: data,
                dataSource: data,
                isLoading: false,
              });
            }
          } else {
            this.setState({
              isLoading: false,
            });
            Message.error({
              content: response.data.message,
              duration: 10000,
              closeable: true,
            });
          }
        }).catch((error) => {
          this.setState({
            isLoading: false,
          });
          Message.error({
            content: error,
            duration: 10000,
            closeable: true,
          });
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

  handleParaChange = (consumerApiGroup) => {
    const data = this.filterComponent.getFilterData(this.state.dataSource);
    const result = this.filterByPagePara(data, consumerApiGroup);
    this.setState({
      filterDataSource: result,
    });
  }

  /**
   * consumer api filter
   */
  consumerApiFilter = (filterParams) => {
    let value;
    Object.keys(filterParams).forEach((key) => {
      const selectedKeys = filterParams[key].selectedKeys;
      if (selectedKeys.length) {
        value = selectedKeys;
      }
    });
    if (isNullOrUndefined(value)) {
      value = 'ALL';
    }
    value = value.toString();
    this.handleParaChange(value);
  }

  // group detail
  renderGroup = (value, index, record) => {
    return (
      <div>
        <a style={styles.groupLink} onClick={() => this.handelDetail(record)}>
          {record.consummerGroup}
        </a>
      </div>
    );
  };
    redrawPageData=(value) => {
      this.setState({
        pageData: value,
      });
    }


  handleDeleteGroup = (record) => {
    Dialog.confirm({
      title: 'Delete Group',
      content: `confirm delete this group: ${record.consummerGroup}?`,
      onOk: () => this.deleteGroup(record),
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
  }

  handleRestOffset=(record) => {
    const data = {};
    data.group = record.consummerGroup;
    data.clusterId = record.clusterID;
    data.topic = '';

    this.fetchTopic(data).then((value) => {
      this.setState({
        value: data,
        visable: true,
        topicInfo: value,
      });
    });
  }

  fetchTopic=(record) => {
    return new Promise(((resolve, reject) => {
      axios.post('/manager/group/topic', record).then((response) => {
        if (response.data.code === 200) {
          const data = [];
          const value = response.data.data;
          value.map((str) => {
            data.push({
              label: str,
              value: str,
            });
          });
          resolve(data);
        }
      }).catch((error) => {
        console.log('error', error);
      });
    }));
  }
  onOk = () => {
    const { validateFields } = this.refs.form;
    validateFields((errors, values) => {
      if (errors) {
        return;
      }
      const data = this.state.value;
      data.clusterId = this.state.value.clusterId;
      axios.post('/manager/group/rest/offset', data).then((response) => {
        if (response.data.code === 200) {
          Message.success(response.data.message);
          this.handelDialog();
        }
      }).catch((error) => {
        console.log('error', error);
      });
    });
  }

  handelDialog = () => {
    this.setState({
      visable: !this.state.visable,
    });
  }

  renderRestDialog = () => {
    const { isMobile } = this.state;
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };
    // 响应式处理
    if (isMobile) {
      simpleFormDialog.width = '300px';
    }

    const okProps = { children: 'OK' };
    const cancelProps = { children: 'Cancel' };
    return (
      <div>
        <Dialog
          className="simple-form-dialog"
          style={simpleFormDialog}
          autoFocus={false}
          footerAlign="center"
          title="Reset Offset"
          onOk={this.onOk}
          onCancel={this.handelDialog}
          onClose={this.handelDialog}
          isFullScreen
          visible={this.state.visable}
          okProps={okProps}
          cancelProps={cancelProps}
        >
          <IceFormBinderWrapper
            ref="form"
            value={this.state.value}
            onChange={this.onFormChange}
          >
            <div style={styles.dialogContent}>
              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Group:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="group" required>
                    <span>{this.state.value.group}</span>
                  </IceFormBinder>
                  <IceFormError name="group" />
                </Col>
              </Row>

              <Row style={styles.formRow}>
                <Col span={`${isMobile ? '6' : '4'}`}>
                  <label style={styles.formLabel}>Topic Name:</label>
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="topic" required>
                    <Select
                      showSearch
                      dataSource={this.state.topicInfo}
                      placeholder="please select topic"
                      style={{ width: '100%' }}
                    />

                  </IceFormBinder>
                  <span style={{ color: 'red', fontWeight: '500' }}>Please stop group consumption first, otherwise offset reset is invalid!</span>
                  <IceFormError name="topic" />
                </Col>
              </Row>
            </div>
          </IceFormBinderWrapper>
        </Dialog>
      </div>
    );
  }

  /**
   * 删除group
   */
  deleteGroup = (record) => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.post('/manager/delete/group', record)
          .then((response) => {
            if (response.data.code === 200) {
              this.fetchData(getPersonalityCluster('kafkaManagerGroup').id);
            } else {
              Message.error({
                content: response.data.message,
                duration: 10000,
                closeable: true,
              });
              this.setState({
                isLoading: false,
              });
            }
          })
          .catch((e) => {
            console.error(e);
            this.setState({
              isLoading: false,
            });
            Message.error({
              content: 'delete Group is error, please contact maintenance staff!',
              duration: 10000,
              closeable: true,
            });
          });
      }
    );
  }

  renderDeleteGroup = (value, index, record) => {
    return (
      <div>
        <span title="reset offset" style={styles.operBtn} onClick={() => { this.handleRestOffset(record); }}>
          <FoundationSymbol size="xxs" type="exchange" />
        </span>

        <span style={styles.separator} />
        <span title="Delete this Group" style={styles.operBtn}>
          <Icon size="xxs" type="close" onClick={() => { this.handleDeleteGroup(record); }} />
        </span>
      </div>
    );
  };

  handelDetail = (record) => {
    // this.props.history.push(`/monitor/group/detail/${record.clusterID}/${record.consummerGroup}`);
    this.props.history.push(`/monitor/consumer/group/detail/${record.clusterID}/${record.clusterName}/${record.consummerGroup}`);
  };

  /**
   * 根据consumerApi进行过滤
   */
  filterByPagePara = (data, consumerApiGroup) => {
    let result = data;
    if (!isNullOrUndefined(consumerApiGroup) && consumerApiGroup !== 'ALL' && consumerApiGroup !== '') {
      result = this.searchdata(result, 'consumereApi', consumerApiGroup);
    }
    return result;
  }

  /**
   * 根据数组中对象的某个属性值进行搜索
   */
  searchdata = (data, filterField, filterValue) => {
    return data.filter(v => v[filterField].toLocaleLowerCase().search(filterValue.toLocaleLowerCase()) !== -1);
  }

  /**
   * 绑定子组件的方法
   * 调用方式: this.filterComponent.getFilterData(data);
   */
  onRef = (componentMethod) => {
    this.filterComponent = componentMethod;
  }

  render() {
    const { isLoading } = this.state;
    const view = this.renderRestDialog();
    return (
      <div>
        <Loading
          visible={isLoading}
          style={styles.loading}
        >
          <CustomTableFilter
            dataSource={this.state.dataSource}
            refreshTableData={this.refreshTableData}
            refreshDataSource={this.fetchData}
            selectTitle="Cluster"
            selectField="clusterName"
            searchTitle="Filter"
            searchField="consummerGroup"
            searchPlaceholder="Input Group Name"
            id="kafkaManagerGroup"
            onRef={this.onRef}
          />
          <Table
            dataSource={this.state.pageData}
            hasBorder={false}
            onSort={(value, order) => this.onSort(value, order)}
            onFilter={this.consumerApiFilter}
          >
            <Table.Column title="Group Name" dataIndex="consummerGroup" cell={this.renderGroup} sortable />
            <Table.Column title="Consumer Api" dataIndex="consumereApi" filters={[{ label: 'ZK', value: 'ZK' }, { label: 'BROKER', value: 'BROKER' }]} filterMode="single" />
            <Table.Column title="Cluster" dataIndex="clusterName" />
            <Table.Column title="Operation" cell={this.renderDeleteGroup} />
          </Table>
          <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
        </Loading>
        {view}
      </div>
    );
  }
}

const styles = {
  groupLink: {
    margin: '0 5px',
    color: '#1111EE',
    cursor: 'pointer',
    textDecoration: 'none',
  },
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
  formRow: { marginTop: 20 },
  simpleFormDialog: { width: '640px' },
  formLabel: { lineHeight: '26px' },
};
