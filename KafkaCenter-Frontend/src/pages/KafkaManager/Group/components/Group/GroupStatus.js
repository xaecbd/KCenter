import React, { Component } from 'react';
import { isNullOrUndefined } from 'util';
import { withRouter } from 'react-router-dom';
import { Table, Message, Loading, Icon, Dialog, Grid, Select, Input, Tab,Tag } from '@alifd/next';
import axios from '@utils/axios';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import FoundationSymbol from '@icedesign/foundation-symbol';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import style  from './index.module.scss';
import dayjs from 'dayjs';
import { sort,thousandSplit } from '@utils/dataFormat';
import { getPersonalityCluster } from '@utils/cookies';

const { Row, Col } = Grid;
@withRouter
export default class GroupStatus extends Component { 

  constructor(props){
     super(props);
     this.state = {
      isLoading: false,
      pageData: [],
      filterDataSource: [],
      dataSource: [],
      isMobile: false,
      visable: false,
      value: {},
      topicInfo: [],
    };
  }


  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }



  fetchData = () =>{
      this.setState({
        isLoading:true,
        }
        ,()=>{
          axios.get(`/manager/group/status?cluster=${this.props.id}`).then((response) => {
            if (response.data.code === 200) {
              const data = response.data.data;
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
      });
  }

  onSort(value, order) {
    const dataSource = sort(this.state.filterDataSource, value, order);
    this.refreshTableData(dataSource);
  }

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

  /**
   * 绑定子组件的方法
   * 调用方式: this.filterComponent.getFilterData(data);
   */
  onRef = (componentMethod) => {
    this.filterComponent = componentMethod;
  }

   renderTime = (value, index, record) =>{
    return dayjs(value).format('MM-DD HH:mm')
  }

  
  render() {
    const { isLoading } = this.state;
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
            searchTitle="Filter"
            searchField="group,topic"
            searchPlaceholder="Input Group,Topic or Status Name"
            id="kafkaManagerGroupStatus"
            onRef={this.onRef}
          />
           <Table
            hasBorder={false}
            dataSource={this.state.pageData}
            className={style.Table}
            onSort={(value, order) => this.onSort(value, order)}

          >
            <Table.Column
              title="Cluster"
              dataIndex="clusterName"
              key="clusterName"
              width='8%'
            />
            <Table.Column
              title="Topic"
              dataIndex="topic"
              key="topic"
              width='25%'
              sortable
              cell={(value,index,record)=>{
                return <span title={value}>{value}</span>
              }}
            />
            <Table.Column
              title="Group"
              dataIndex="group"
              key="group"
              width='25%'
              sortable
              cell={(value,index,record)=>{
                return <span title={value}>{value}</span>
              }}
            />

            <Table.Column
              title="Consumer Api"
              dataIndex="method"
              width='10%'
            />
            <Table.Column
              title="Lag"
              dataIndex="lag"
              key="lag"
              width='10%'
              sortable
              cell={(value,index,record)=> { return <span title={thousandSplit(value)}>{thousandSplit(value)}</span> }}
            />
            <Table.Column
              title="State"
              dataIndex="status"
              width='10%'
              sortable
              cell={(value,index,record)=>{ 
                if(value==='ACTIVE') return  <span style={{ 'color':'green'}}>{value}</span>
                else if(value==='DEAD') return <span style={{ 'color':'red'}}>{value}</span>
                else if(value==='UNKNOWN') return <span style={{ 'color':'#e6c619'}}>{value}</span>
              }}
            />
            <Table.Column
              title="Time"
              dataIndex="currentTime"
              key="currentTime"
              width='10%'
              cell={this.renderTime}
            />
            </Table>
          <CustomPagination dataSource={this.state.filterDataSource} redrawPageData={this.redrawPageData}  pageList={[5,10,50,100]} pageSize={5}/>
        </Loading>
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
