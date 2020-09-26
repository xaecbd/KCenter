import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import { Table, Message, Loading ,Icon} from '@alifd/next';
import { transToNumer, formatSizeUnits } from '@utils/dataFormat';
import CustomPagination from '@components/CustomPagination';
import CustomTableFilter from '@components/CustomTableFilter';
import { getPersonalityCluster } from '@utils/cookies';
import axios from '@utils/axios';


export default class Broker extends Component {
  constructor(props){
    super(props);
    this.state = {
      isLoading: false,
      dataSource: [],
      filterDataSource: [],
      pageData: [],
    };
  }
 

  componentWillMount() {
    this.mounted = true;
    this.fetchData(this.props.id);
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
        axios.get(`/manager/broker?cluster=${clusterId}`).then((response) => {
          if (response.data.code === 200) {
            const data = response.data.data;
            if (this.mounted) {
              this.setState({
                dataSource: data,
                filterDataSource: data,
                isLoading: false,
              });
            }
          } else {
            Message.error({
              content: response.data.message == null ? 'server has error.' : response.data.message,
              duration: 10000,
              closeable: true,
            });
          }
        }).catch((error) => {
          this.setState({
            isLoading: false,
          });
          console.error(error);
          Message.error({
            content: 'server has error.',
            duration: 10000,
            closeable: true,
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

  redrawPageData = (value) => {
    this.setState({
      pageData: value,
    });
  }

  render() {
    const { isLoading, pageData, filterDataSource } = this.state;
    return (
      <div>

        <Loading
          visible={isLoading}
          style={styles.loading}
        >
          <Table
            dataSource={pageData}
            hasBorder={false}
          >
            <Table.Column width="2%" cell={(value,index,record) => {
              if(record.controller===true){
                return (<div title="controller"><Icon type="favorites-filling" size="xs"/></div>);
              }
            }}/>
            <Table.Column title="Cluster" dataIndex="clusterName" />
            <Table.Column title="Id" dataIndex="id" cell={(value, index, record) => record.brokerInfo.bid} />
            <Table.Column title="Host" dataIndex="host" cell={(value, index, record) => record.brokerInfo.host} />
            <Table.Column title="Port" dataIndex="port" cell={(value, index, record) => record.brokerInfo.port} />
            <Table.Column title="Topics" dataIndex="topics" />
            <Table.Column title="Partitions" dataIndex="partitions" />
            <Table.Column title="Partitions as Leader" dataIndex="partitionsAsLeader" />
            
          </Table>
          <CustomPagination dataSource={filterDataSource} redrawPageData={this.redrawPageData} />
        </Loading>
      </div>
    );
  }
}
const styles = {
  container: {
    margin: '20px',
    padding: '10px 20px 20px',
  },
  loading: {
    width: '100%',
  },
};
