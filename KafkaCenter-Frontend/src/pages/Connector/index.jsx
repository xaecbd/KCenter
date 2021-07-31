import React, { useEffect, useState } from 'react';
import {
  Grid,
  Input,
  Table,
  Message,
  Loading,
  Button,
  Icon,
  Dialog,
} from '@alifd/next';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import CustomPagination from '@components/CustomPagination';
import IceContainer from '@icedesign/container';
import axios from '@utils/axios';
import Auth from '@components/Auth';
import { sortDataByOrder } from '@utils/dataFormat';
import styles from './index.module.scss';
import AddDialog  from './AddClusterDialog';


const { Row, Col } = Grid;
const breadcrumb = [
  {
    link: '',
    text: 'Connect',
  },
  {
    link: '/connect/list',
    text: 'List',
  },
];
function Connector (props){
 
  const [isLoading,setIsLoading] = useState(false);   
  const [searchValue,setSearchValue] = useState('');
  const [pageData,setPageData] = useState([]);
  const [filterDataSource,setFilterDataSource] = useState([]);
  const [visable,setVisable] = useState(false);
  const [dataSource,setDataSource] = useState([]);
  const [formVal,setFormVal]= useState({});

  const [label,setLabel] = useState('Add');

  function onSort(value, order){
    let data = sortDataByOrder(filterDataSource, value, order);
    data = Object.assign([],data)
    setFilterDataSource(data);
  }

  function handleAdd(){
    setVisable(true);
    setFormVal({});
    setLabel('Add');
  }

  function changeVisable(value){   
    setVisable(value);
  }

  useEffect(()=>{
    fetchConnectorList();
 
  },[]);

  function fetchConnectorList(){
    setIsLoading(!isLoading);
    axios.get('/connector/list').then((response)=>{
      if(response.data.code===200){
        const data = response.data.data;
        setFilterDataSource(data);
        setDataSource(data);
        setIsLoading(false);
      }else{
        setIsLoading(false);
      }
    });
  }

  function redrawPageData(value){
    setPageData(value);
  }

  function handleEdit(record){    
    const data = Object.assign(record,{});
    changeVisable(true);
    setFormVal(data);
    setLabel('Update');

  }

  function handleDelete(record){

    Dialog.confirm({
      content: `Do you want to delete  ${record.name}?`,
      onOk: () => {
        handleDeletes(record);
      },
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
  }


  const renderConnectorName = (value, index, record) => {
    return (
      <a className={styles.link} onClick={()=>routeToConnectors(record)}>
        {value}
      </a>
    );
  };

  function routeToConnectors(record){
    props.history.push(`/connector/job/${record.cluster.id}/${record.cluster.name}/${record.name}/${record.id}`);
  }

  function handleDeletes(record){
    setIsLoading(true);
    axios.delete(`/connector/del?id=${record.id}`).then((response)=>{
      if(response.data.code===200){
        Message.success(response.data.message);
        fetchConnectorList();
      }  else{
        Message.error(response.data.message);
      }      
      setIsLoading(false);
    }).catch(e=>{
      console.error(e);
    });
    setIsLoading(false);
  }
  const renderOperation = (value, index, record) =>{
    return (
      <div>
        <Auth rolename="admin">
          <a className={styles.link} onClick={() => handleEdit(record)}>
            Edit
          </a>     
  
          <span className={styles.separator} />
          <a className={styles.link} onClick={() => handleDelete(record)}>
            Delete
          </a>
        </Auth>
      </div>
    );
  }

  function handleFilterChange(searchValue){
    setSearchValue(searchValue);
    let filterData = [];
    if (searchValue.trim().length === 0) {
      filterData = dataSource;
    } else {
      dataSource.forEach((v) => {
        if (
          v.name
            .toLocaleLowerCase()
            .includes(searchValue.toLocaleLowerCase())
        ) {
          filterData.push(v);
        }
      });
    }
   
    refreshTableData(filterData);
  }

  function refreshTableData(value){
    setFilterDataSource(value);
  }

  return (
    <div>
      <AddDialog visible={visable} changeVisable={changeVisable} formVal={formVal} label={label} refreshData={fetchConnectorList}/>
      <CustomBreadcrumb items={[breadcrumb]} title="Kafka Connectors" />
      <IceContainer className={styles.container}>
        <Loading visible={isLoading} className={styles.loading}>
          <Row style={styles.row}>
            <Col align="center">
              <span style={{ fontWeight: '600' }}>
                Connector Name :&nbsp;&nbsp;&nbsp;
              </span>
              <Input  
                placeholder="Connector Name"
                hasClear
                onChange={handleFilterChange}
                style={{ width: '300px' }}
                value={searchValue}
              />
            </Col>
           
            <Col align="center">          
              <Button type="secondary" onClick={()=>handleAdd()}>
                <Icon type="add" />
                  Add Connect 
              </Button>              
            </Col>
            
          </Row>
          <br/>
        

          <Table dataSource={pageData} hasBorder={false} onSort={(value, order) => onSort(value, order)}>
            <Table.Column
              title="Connector Name"
              dataIndex="name"
              sortable
              cell={renderConnectorName}
            />
            <Table.Column
              title="Kafka Cluster"
              dataIndex="cluster.name"
             
            />
            <Table.Column title="Version" dataIndex="version"  />
            <Table.Column title="Operation" cell={renderOperation}/>
          </Table>
          <CustomPagination
            dataSource={filterDataSource}
            redrawPageData={redrawPageData}
          />
        </Loading>
      </IceContainer>
    </div>
  );
}

export default Connector;