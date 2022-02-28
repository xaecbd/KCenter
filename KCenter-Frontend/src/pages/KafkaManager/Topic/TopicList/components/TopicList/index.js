import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Table, Message, Loading, Grid, Button, Icon } from '@alifd/next';
import axios from '@utils/axios';
import { sort, transToHours,bytesToSize, resturctData } from '@utils/dataFormat';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import Auth from '@components/Auth'
import EditDialog from '../EditDialog';
import { isNullOrUndefined } from 'util';

const { Col } = Grid;

const formatResponseData=(data)=>{
  data.forEach(element => {
    if(!element.owner){
      element.owner='-';
    }
    if(!element.team){
      element.team='-';
    }
    if(!element.fileSize&&element.fileSize!=0){
      element.fileSize=-1;
    }
    if(!element.ttl){
      element.ttl='-';
    }

  });

  return data;
}

@withRouter
export default class TopicList extends Component {
   state = {
     isLoading: false,
     filterDataSource: [],
     dataSource: [],
     pageData: [],
     visible: false,
   };

   componentWillMount() {
     this.mounted = true;
   }

   componentWillUnmount() {
     this.mounted = false;
   }

 


  fetchTopicList = () => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.get(`/manager/topic/list?cluster=${this.props.id}`).then((response) => {
          if (response.data.code === 200) {
            if (this.mounted) {
              let data = formatResponseData(response.data.data);
              data = sort(data, 'topicName','asc');

              this.setState({
                dataSource: data,
                filterDataSource: data,
                isLoading: false,
              }, () => {

              });
              return data;
            }
          } else {
            Message.error(response.data.message);
          }
        }).catch((error) => {
          console.error(error);
          this.setState({
            isLoading: false,
          });
        });
      }
    );
  }

  onRef = (componentMethod) => {
    this.filterComponent = componentMethod;
  }


   /**
   * 根据consumerApi进行过滤
   */
  filterByPagePara = (data, searchValue,switchValue) => {
    
    let result = data;
    if (!isNullOrUndefined(searchValue) && searchValue!='') {
      result = this.searchdata(result, 'topicName', searchValue);
    }
    if (!switchValue || switchValue === 'false') {
      result = result.filter(v => !v['topicName'].startsWith('_'));
    }
    result = Object.assign([],result);
    this.refreshTableData(result);
    return result;

  }

  /**
   * 根据数组中对象的某个属性值进行搜索
   */
  searchdata = (data, filterField, filterValue) => {   
    return data.filter(v => 
          v[filterField].toLocaleLowerCase().search(filterValue.toLocaleLowerCase()) !== -1
        );
  }


  refreshFilterData = () =>{
    const searchValue = sessionStorage.getItem('kafkaManagerTopicSearch');
    let switchValue = sessionStorage.getItem('kafkaManagerTopicSwitch');
    switchValue = !switchValue?switchValue:false;     
    const data = this.filterComponent.getFilterData(this.state.dataSource);
    const result = this.filterByPagePara(this.state.filterDataSource, searchValue,switchValue);
  }
  



  handelDetail = (record) => {
    this.props.history.push(`/cluster/topic/${record.clusterId}/${record.cluster}/${record.topicName}`);
  };

  renderTopic = (value, index, record) => {
    return (
      <div>
         
          <a style={styles.topicLink} onClick={() => this.handelDetail(record)}>
            {record.topicName}
          </a>
        
      </div>
    );
  };

  renderTTL= (value, index, record) => {
   
    if (value !== null && value) {
      return transToHours(value);
    }
    return '-';
  };

  renderValue= (value, index, record) => {
    if (value !== null && value) {
      return value;
    }
    return '-';
  };

  renderBytes= (value, index, record) => {
    if (value !== null && value) {
      if(value==-1){
        return '-';
      }
      return bytesToSize(value);
    }else if(value==0){
      return bytesToSize(0);
    }
    return '-';
  };

  redrawPageData=(value) => {
    this.setState({
      pageData: value,
    });
  }

  refreshTableData = (value) => {
    this.setState({
      filterDataSource: value,
    });
  }

  onSort = (value, order) => {
    const data = sort(this.state.filterDataSource, value, order);
    this.refreshTableData(data);
  }

  renderUnderRep = (value) => {
    if (value !== 0) {
      return `${value}%`;
    }
    return value;
  }
  handelDialog = () => {
    this.setState({
      visible: !this.state.visible,
    });
  };

  hideDialog = () => {
    this.setState({
      visible: !this.state.visible,
    });
  };

  render() {
    //   const url = `/manager/topic/list?cluster=${clusterId}`;
    const view = (   <Auth rolename="admin"><Col align="center"><Button type="secondary" onClick={this.handelDialog}><Icon type="add" />Create Topic</Button></Col></Auth>);
    const { visible } = this.state;
    return (
      <div>
        <Loading
          visible={this.state.isLoading}
          style={styles.loading}
        >
          <EditDialog visible={visible} handelDialog={this.handelDialog} fetchData={this.fetchTopicList} refreshData= {this.refreshFilterData}/>
          <CustomTableFilter
            dataSource={this.state.dataSource}
            refreshTableData={this.refreshTableData}
            refreshDataSource={this.fetchTopicList}
            // selectTitle="Cluster"
            // selectField="cluster"
            searchTitle="Filter"
            searchField="topicName"
            searchPlaceholder="Input Topic Name"
            switchField="topicName"
            otherComponent={view}
            id="kafkaManagerTopic"
            onRef={this.onRef}
          />
          <Table dataSource={this.state.pageData} hasBorder={false} onSort={(value, order) => this.onSort(value, order)}>
            <Table.Column title="Topic Name" dataIndex="topicName" cell={this.renderTopic} sortable />
            <Table.Column title="Owner" dataIndex="owner" cell={this.renderValue} sortable />
            <Table.Column title="Team" dataIndex="team"  cell={this.renderValue} sortable />
            <Table.Column title="File Size" dataIndex="fileSize" cell={this.renderBytes} sortable />
            <Table.Column title="Partition" dataIndex="partition" sortable />
            <Table.Column title="Replication" dataIndex="replication" sortable />
            <Table.Column title="TTL(hours)" dataIndex="ttl" cell={this.renderTTL} sortable />
            <Table.Column title="Cluster" dataIndex="cluster" />
            <Table.Column title="Under Replication(%)" dataIndex="under_replication" cell={this.renderUnderRep} />

          </Table>
          <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData} />
        </Loading>
      </div>
    );
  }
}

const styles = {
  topicLink: {
    margin: '0 5px',
    color: '#1111EE',
    cursor: 'pointer',
    textDecoration: 'none',
  },
  loading: {
    width: '100%',
  },
};
