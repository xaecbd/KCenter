import React, { Component } from 'react';
import { Table, Message, Grid, Input, Dialog, Button, Icon, Loading } from '@alifd/next';
import CustomPagination from '@components/CustomPagination';
import { withRouter } from 'react-router-dom';
import IceContainer from '@icedesign/container';
import iconfont from '@/iconfont.js';
import {transToLocalTimeZoned} from '@/utils/dataFormat';
import axios from '@utils/axios';
import { sortDataByOrder } from '@utils/dataFormat';

import AddStrem from '../AddComponents/AddComponent';
import Img from '@icedesign/img';
const CustomIcon = Icon.createFromIconfontCN(iconfont);

const { Row, Col } = Grid;
@withRouter
export default class Streams extends Component {
  state = {
    ksqlServerId: this.props.match.params.ksqlServerId,
    clusterName: this.props.match.params.clusterName,
    isLoading: false,
    filterDataSource: [],
    dataSource: [],
    pageData: [],
    visible: false,
    detailRecord: {},
    addVisable: false,
    describeLoading: false,
    mockVisable: false,
    teamData: [],
    mockData: [],
    mockLoading: false,
    mockStream:'',
    historyLoading:false,
    historyData:[],
    historyVisable:false,
    historyFilterDataSource:[],
    historyPageData: [],
    formVal:{},
    moduleName:'Add'
  };

  componentDidMount() {
    this.getTeam();
    this.getStreams();
  }
  renderOper = (value, index, record) => {
    return (
      <div >
         <a style={styles.link} onClick={() => this.handleEdit(record)}>
             Edit
          </a>
          <span style={styles.separator} />
        <a style={styles.link} onClick={() => this.handleDelete(record)}>
          Drop
          </a>
          <span style={styles.separator} />
        <a style={styles.link} onClick={() => this.handleMock(record)}>
          Show Data
          </a>
          <span style={styles.separator} />
        <a style={styles.link} onClick={() => this.handelDetail(record)}>
          Describe
          </a>       
        
        <span style={styles.separator} />
        <a style={styles.link} onClick={() => this.handleHistory(record)}>
          History
          </a>
      </div>
    );
  };

  renderToConnect=()=>{
      this.props.history.push('/connector/list');
  }

  renderTopic = (value, index, record) =>{
      if(value){
        return (<span>{value}    <a onClick={this.renderToConnect} style={{ fontSize:'11px'}}>Export data</a></span>);
      }
  }
  
  handleEdit = (record) =>{
    const data = Object.assign(record,{});
    this.setState({
      addVisable: !this.state.addVisable,
      formVal: data,
      moduleName: 'Edit'
    });
  }

  handleHistory = (record) =>{
    this.setState({
      historyLoading: true,
      historyVisable: !this.state.historyVisable,

    },()=>{
      axios
      .get(`/ksql/history/list?ksqlServerId=${this.state.ksqlServerId}&name=${record.name}&type=stream`)
      .then((response) => {
        let data = [];
        if (response.data.code === 200) {
          data = response.data.data;
        }
        this.setState({
          historyLoading: !this.state.historyLoading,
          historyData: data,
          historyFilterDataSource: data,
        }, () => { });
      })
      .catch((error) => {
        console.error(error);
        this.setState({
          historyLoading: false,
        });
      });
    });

  }
  handelDetail = (record) => {
    this.setState(
      {
        describeLoading: true,
        visible: !this.state.visible,
      },
      () => {
        axios
          .get(`/ksql/describe_stream?ksqlServerId=${this.state.ksqlServerId}&clusterName=${this.state.clusterName}&streamName=${record.name}`)
          .then((response) => {
            let data = [];
            if (response.data.code === 200) {
              data = response.data.data;
            }
            this.setState({
              describeLoading: !this.state.describeLoading,
              detailRecord: data
            }, () => { });
          })
          .catch((error) => {
            console.error(error);
            this.setState({
              describeLoading: false,
            });
          });
      }
    );

  };
  hideDetailDialog = () => {
    this.setState({
      visible: false,
    });
  };

  mockDialog = () => {
    this.setState({
      mockVisable: !this.state.mockVisable,
    });
  }
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

  mockLoadingChange = () => {
    this.setState({
      mockLoading: !this.state.mockLoading,
    });
  }

  handleMock = (record) => {
    const serverId = this.state.ksqlServerId;
    const clusterName = this.state.clusterName;
    this.setState({
      mockVisable: true,
      mockLoading: !this.state.mockLoading,
      mockData: [],
      mockStream: record.name,
    }, () => {
      axios.get(`/ksql/select_stream?ksqlServerId=${serverId}&clusterName=${clusterName}&streamName=${record.name}`)
        .then((response) => {
          if (response.data.code == 200) {
            this.setState({
              mockData: response.data.data,
            });
          }
          this.mockLoadingChange();
        }).catch((error) => {
          console.error(error);
          this.mockLoadingChange();
        });
    });


  }
  handleDeletes = (record) => {
    const serverId = this.state.ksqlServerId;
    const clusterName = this.state.clusterName;
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios
          .delete(`/ksql/drop_stream?ksqlServerId=${serverId}&clusterName=${clusterName}&streamName=${record.name}&id=${record.id}`)
          .then((response) => {
            if (response.data.code === 200) {
              this.getStreams();
              Message.success(response.data.message || 'drop streams success');
            } else {
              Message.error(response.data.message || 'drop streams has error');
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
  getStreams = () => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.get(`/ksql/stream/list?clusterId=${this.props.match.params.id}`).then((response) => {
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
          this.setState(
            {
              isLoading: false,
            });
          console.error(error);
        });
      }
    );
  };

  addVisableChange = () => {
    this.setState({
      addVisable: !this.state.addVisable
    }, () => {
    });
  }


  getTeam = () =>{
    axios.get('/team/userteam').then((response) => {
      if (response.data.code === 200 && response.data.data !== undefined && response.data.data.length > 0) {
        this.setState({               
          teamData: response.data.data,          
        });
      }
    })
      .catch((error) => {
        
        console.log(error);
      });
  }


  handelDialog = () => {
 // 判断当前用户是否在team中，不在team 提示加入team,在team 将team值传入子组件
    if(this.state.teamData.length>0){
      this.setState({
        formVal:{},
        addVisable:!this.state.addVisable,
      });
    }else{
      Dialog.alert({
        title: 'Alert',
        content: 'please join a team',
      });
    }

  };

  refreshMockData = (name) =>{
      const data = {'name': name};
      this.handleMock(data);
  }

  
  addStream = (params)=>{
    let kstreamInfo = params.kStreamInfo;
    kstreamInfo.clusterId = this.props.match.params.id;
    params.clusterName = this.state.clusterName;
    params.ksqlServerId = this.state.ksqlServerId; 
  
    let url = '/ksql/create_stream';
    if(this.state.moduleName=='Edit'){
        url = '/ksql/edit_stream';
    }

    axios.post(url,params).then((response) => {
      if (response.data.code === 200) {
        Message.success(response.data.message);         
        this.addVisableChange();  
        this.getStreams();
        } else {
        Message.error(response.data.message);
      }
    }).catch((error) => {
      console.error(error);
    });
  }

  refreshHistoryTable = (value) =>{
    this.setState({
      historyPageData:value,
    });
  }

  renderScript = () =>{

  }
  mockDataHtml = () =>{
    
    let disable = this.state.mockData.length>0?Boolean(false):Boolean(true);
    return (
      <>
      <div style={{ marginTop:'10px',marginBottom:'10px',height:'30px'}}>
        <Button type="secondary" onClick={e=>this.mockDialog()} disabled={Boolean(disable)} style={{ marginRight:'5px',float:'right'}} ><CustomIcon type="iconfanhui1"/></Button>
        <Button type="secondary" onClick={e=>this.refreshMockData(this.state.mockStream)} disabled={Boolean(disable)}  style={{ marginRight:'5px',float:'right'}}><CustomIcon type="iconrefresh"/></Button>           
        </div>
      <Loading visible={this.state.mockLoading} style={{ width: '100%' }}>          
       
        <Table dataSource={this.state.mockData}>
          {this.state.mockData.length > 0 ? Object.keys(this.state.mockData[0]).map(key => {
            if(key==='script'){
              return (<Table.Column title={key} dataIndex={key} key={key} cell={renderScript}/>);
            }else{
              return (<Table.Column title={key} dataIndex={key} key={key}/>);
            }
           
          }) : null
          }
        </Table>               
      </Loading>
    </>
    );
  }

  histortBack = () =>{
      this.setState({
        historyVisable: !this.state.historyVisable
      });
  }

  transTime = (value) =>{
     return transToLocalTimeZoned(value);
  }

  transScript = (value) =>{
      if(value){
        return value;
      }
      return '-';
  }

  historyHtml = () =>{
    return (  <>
    <div style={{ marginTop:'10px',marginBottom:'10px',height:'30px'}}>
        <Button type="secondary" onClick={e=>this.histortBack()} style={{ marginRight:'5px',float:'right'}} ><CustomIcon type="iconfanhui1"/></Button>
      </div>

    <Table dataSource={this.state.historyPageData}  >
             <Table.Column title='Time' dataIndex='date' cell={this.transTime} width={80} />
             <Table.Column title='User' dataIndex='user'  width={80}/>
             <Table.Column title='Operate' dataIndex='operate' width={300}/>
             <Table.Column title='Script' dataIndex='script' cell={this.transScript} width={500}/>
    </Table>
     <CustomPagination dataSource={this.state.historyFilterDataSource} redrawPageData={this.refreshHistoryTable} /></>);
  }


  onSort(value, order) {
    const dataSource = sortDataByOrder(this.state.dataSource, value, order);
    this.refreshTableData(dataSource);
  }

  render() {
    if(this.state.mockVisable){
       return this.mockDataHtml();
    }else if(this.state.historyVisable){
      return this.historyHtml();
    }else{
      return (      
        <div>
          <AddStrem visable={this.state.addVisable} visableChange={this.addVisableChange} teamData={this.state.teamData}            
            saveData={this.addStream} moudlue='Stream' value={this.state.formVal} name={this.state.moduleName}
          />
          <Row style={styles.row}>
            <Col align="center">
              <span style={{ fontWeight: '600' }}>Stream Name:&nbsp;&nbsp;&nbsp;</span>
              <Input
                placeholder="StreamName"
                hasClear
                onChange={this.handleFilterChange}
                style={{ width: '200px' }}
                value={this.state.searchValue}
              />
            </Col>
            <Col align="center">
              <Button type="secondary" onClick={this.handelDialog}><Icon type="add" />Add Stream</Button>
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
            <>
              <Loading visible={this.state.describeLoading} style={{ width: '100%' }}>
                <div>
                  <IceContainer title="Describe Stream ">
                    {/*  */}
                    <ul>
                      <li style={styles.detailItem}>
                        <div style={styles.detailTitle}>Stream:</div>
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
                        <div style={styles.detailTitle}>Key Format:</div>
                        <div>
                          {this.state.detailRecord.keyFormat}
                        </div>
                      </li>
                      <li style={styles.detailItem}>
                        <div style={styles.detailTitle}>Value Format:</div>
                        <div>
                          {this.state.detailRecord.valueFormat}
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
                    {/* */}
                  </IceContainer>
  
                </div>
              </Loading>
            </>
          </Dialog>
          <Table
            loading={this.state.isLoading}
            dataSource={this.state.pageData}
            hasBorder={false}
            onSort={(value, order) => this.onSort(value, order)}
          >
            <Table.Column title="Stream" dataIndex="name" cell={this.renderTopic} sortable/>
            <Table.Column title="Topic Name" dataIndex="topic" />
            <Table.Column title="Owner" dataIndex="owner.name" />
            <Table.Column title="Team" dataIndex="team.name" />
            <Table.Column title="Operation" cell={this.renderOper} />
          </Table>
          <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
        </div>
      );
    }
    
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
    width: '100px',
    color: '#999999',
  },
  simpleFormDialog: { width: '640px' },
  simpleFormDialogs: { width: '1300px' },
};
