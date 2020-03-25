import React, { Component } from 'react';
import { Table, Message, Grid, Input, Dialog, Button } from '@alifd/next';
import CustomPagination from '@components/CustomPagination';
import { withRouter } from 'react-router-dom';
import IceContainer from '@icedesign/container';
import axios from '@utils/axios';

const { Row, Col } = Grid;
@withRouter
export default class Tables extends Component {
    state = {
      ksqlServerId: this.props.match.params.ksqlServerId,
      clusterName: this.props.match.params.clusterName,
      isLoading: false,
      filterDataSource: [],
      dataSource: [],
      pageData: [],
      visible: false,
      detailRecord: {},
    };

    componentDidMount() {
      this.getTables(this.state.ksqlServerId, this.state.clusterName);
    }
    renderOper = (value, index, record) => {
      return (
        <div>
          <a style={styles.link} onClick={() => this.handelDetail(record)}>
            Describe
          </a>
          <span style={styles.separator} />
          <a style={styles.link} onClick={() => this.handleDelete(record)}>
            Drop
          </a>
        </div>
      );
    };
    handelDetail = (record) => {
      this.setState({
        visible: !this.state.visible,
        detailRecord: record,
      });
    };
    hideDetailDialog= () => {
      this.setState({
        visible: false,
      });
    };
    handleDelete = (record) => {
      Dialog.confirm({
        content: `Do you want to Drop  ${record.name}?`,
        onOk: () => {
          this.handleDeletes(record);
        },
        okProps: { children: 'OK' },
        cancelProps: { children: 'Cancel' },
      });
    };
    handleDeletes = (record) => {
      const serverId = this.state.ksqlServerId;
      const clusterName = this.state.clusterName;
      this.setState(
        {
          isLoading: true,
        },
        () => {
          axios
            .delete(`/ksql/drop_table?ksqlServerId=${serverId}&clusterName=${clusterName}&tableName=${record.name}`)
            .then((response) => {
              if (response.data.code === 200) {
                this.getTables(serverId, clusterName);
                Message.success(response.data.message || 'drop table success');
              } else {
                Message.error(response.data.message || 'drop table has error');
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
    handleFilterChange = (searchValue) => {
      const dataSource = this.state.dataSource;

      let filterData = [];
      if (searchValue.trim().length === 0) {
        filterData = dataSource;
      } else {
        dataSource.forEach((v) => {
          if (v.name.toLocaleLowerCase().includes(searchValue.toLocaleLowerCase())) {
            filterData.push(v);
          }
        });
      }
      this.setState({
        searchValue,
      });
      this.refreshTableData(filterData);
    };
    getTables = (id, name) => {
      this.setState(
        {
          isLoading: true,
        },
        () => {
          axios.post(`/ksql/show_tables?ksqlServerId=${id}&clusterName=${name}`).then((response) => {
            if (response.data.code === 200) {
              const data = response.data.data;
              this.setState({
                dataSource: data,
                filterDataSource: data,
                isLoading: false,
              });
            } else {
              Message.error(response.data.message);
              this.setState(
                {
                  isLoading: false,
                });
            }
          }).catch((error) => {
            console.error(error);
          });
        }
      );
    };


    render() {
      return (
        <div>
          <Row style={styles.row}>
            <Col align="center">
              <span style={{ fontWeight: '600' }}>Table Name:&nbsp;&nbsp;&nbsp;</span>
              <Input
                placeholder="TableName"
                hasClear
                onChange={this.handleFilterChange}
                style={{ width: '200px' }}
                value={this.state.searchValue}
              />
            </Col>
          </Row>
          <Dialog
            className="simple-form-dialog"
            style={styles.simpleFormDialog}
            autoFocus={false}
            footerAlign="center"
            footer={<Button type="primary" onClick={this.hideDetailDialog}>Cancel</Button>}
            onClose={this.hideDetailDialog}
            cancelProps={{ children: 'Cancel' }}
            isFullScreen
            visible={this.state.visible}
          >

            <div>
              <IceContainer title="Describe Table ">
                <ul>
                  <li style={styles.detailItem}>
                    <div style={styles.detailTitle}>Table:</div>
                    <div>
                      {this.state.detailRecord.name}
                    </div>
                  </li>
                  <li style={styles.detailItem}>
                    <div style={styles.detailTitle}>TopicName:</div>
                    <div>
                      {this.state.detailRecord.topic}
                    </div>
                  </li>
                  <li style={styles.detailItem}>
                    <div style={styles.detailTitle}>Format:</div>
                    <div>
                      {this.state.detailRecord.format}
                    </div>
                  </li>
                  <li style={styles.detailItem}>
                    <div style={styles.detailTitle}>Partitions:</div>
                    <div>
                      {this.state.detailRecord.partitions}
                    </div>
                  </li>
                  <li style={styles.detailItem}>
                    <div style={styles.detailTitle}>Replication:</div>
                    <div>
                      {this.state.detailRecord.replication}
                    </div>
                  </li>
                  <li style={styles.detailItem}><div style={styles.detailTitle}>Schema:</div></li>
                  <li>
                    <Table size="small" dataSource={this.state.detailRecord.fields}>
                      <Table.Column title="Name" dataIndex="name" />
                      <Table.Column title="Type" dataIndex="schema.type" />
                    </Table>
                  </li>
                </ul>

              </IceContainer>
            </div>
          </Dialog>
          <Table
            loading={this.state.isLoading}
            dataSource={this.state.pageData}
            hasBorder={false}
          >
            <Table.Column title="Table" dataIndex="name" />
            <Table.Column title="Topic Name" dataIndex="topic" />
            <Table.Column title="Data Format" dataIndex="format" />
            <Table.Column title="Operation" cell={this.renderOper} />
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
  row: {
    margin: '20px 4px 20px',
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
  detailItem: {
    padding: '15px 0px',
    display: 'flex',
    borderTop: '1px solid #EEEFF3',
  },
  detailTitle: {
    marginRight: '30px',
    textAlign: 'left',
    width: '50px',
    color: '#999999',
  },
  simpleFormDialog: { width: '640px' },
};
