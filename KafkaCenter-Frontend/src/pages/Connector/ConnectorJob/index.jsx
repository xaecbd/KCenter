import React, { useEffect, useState ,useRef} from 'react';
import {
  Grid,
  Input,
  Table,
  Message,
  Loading,
  Button,
  Icon,
  Dialog,
  Tag,
} from '@alifd/next';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import CustomPagination from '@components/CustomPagination';
import IceContainer from '@icedesign/container';
import { sortDataByOrder } from '@utils/dataFormat';

import axios from '@utils/axios';
import styles from './index.module.scss';
import AddJob from './AddConnectorJob';

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
function ConnectorJob(props){



  const [isLoading,setIsLoading] = useState(false);   
  const [searchValue,setSearchValue] = useState('');
  const [pageData,setPageData] = useState([]);
  const [filterDataSource,setFilterDataSource] = useState([]);
  const [dataSource,setDataSource] = useState([]);
  const [teamData,setTeamData] = useState([]);
  const [visable,setVisable] = useState(false);
  const [formVal,setFormVal] = useState({});
  const [editVisable,setEditVisable] = useState(false);

  const [isPause,setIspause] = useState(false);

  useEffect(()=>{
    getTeam();
    connectorList();
   
  },[]);


  const renderOperation = (value, index, record) =>{
    return (
      <div>
        <a className={styles.link} onClick={() => handleEdit(record)}>
            Edit
        </a>
  
        <span className={styles.separator} />
        <a className={styles.link} onClick={() => handleDelete(record)}>
            Delete
        </a>

        <span className={styles.separator} />
        <a className={styles.link} onClick={() => handleRestart(record)}>
             Restart
        </a>

        <span className={styles.separator} />
        <a className={styles.link} onClick={() => handlePause(record)}>
            
          {isPause? 'Resume' :'Pause' }
        </a>

        <span className={styles.separator} />
        <a className={styles.link} onClick={() => handleConsumer(record)}>
           Consumer-Group
        </a>
      </div>
    );
  }

  function handleConsumer(record){
    props.history.push(`/monitor/consumer/group/detail/${props.match.params.clusterId}/${props.match.params.clusterName}/connect-${record.name}`);
  }

  const renderStatus = (value, index, record) =>{
    if(value==='RUNNING'){
      return (<Tag  color="green" type="primary" size="small">{value}</Tag>);
    }
    if(value==='FAILED'){
      return (<Tag  color="red" type="primary" size="small">{value}</Tag>);
    }
    return (<Tag  color="gray" type="primary" size="small">{value}</Tag>);
  }
  function handleRestart(record){
    const data = {};
    data.clusterName = props.match.params.name;
    data.connectorJob = record;
    setIsLoading(true);
    Dialog.confirm({
      content: `Do you want to restart  ${record.name}?`,
      onOk: () => {
        axios.post('/connector/job/restart',data)
          .then((response) => {
            if (
              response.data.code === 200
         
            ) {
              Message.success(response.data.message);
              connectorList();
            }else{
              Message.error(response.data.message);
            }
            setIsLoading(false);
           
          })
          .catch((error) => {
            setIsLoading(false);
            console.log(error);
          });
      },
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
    setIsLoading(false);
  }

  function handlePause(record){
    const data = {};
    data.clusterName = props.match.params.name;
    data.connectorJob = record;
    setIsLoading(true);
    let url = '/connector/job/pause';
    if(isPause){
      url = '/connector/job/resume';
    }
    Dialog.confirm({
      content: `Do you want to ${isPause?'Resume':'Pause'}  ${record.name}?`,
      onOk: () => {
        axios.put(url,data)
          .then((response) => {
            if (
              response.data.code === 200
         
            ) {
              Message.success(response.data.message);
              setIspause(!isPause);
              connectorList();
            }else{
              Message.error(response.data.message);
            }
            setIsLoading(false);
           
          })
          .catch((error) => {
            setIsLoading(false);
            console.log(error);
          });
      },
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
    setIsLoading(false);
  }

  function handleEdit(record){
    // const data = Object.assign(record,{});
    // setVisable(true);
    // setEditVisable(false);
    // setFormVal(data);

    props.history.push(`/connector/job/task/${props.match.params.clusterId}/${props.match.params.clusterName}/${props.match.params.name}
    /${props.match.params.id}/${record.id}`);
    
     
  }
  
  function handleDelete(record){
    const data = {};
    data.clusterName = props.match.params.name;
    data.connectorJob = record;
    setIsLoading(true);
    Dialog.confirm({
      content: `Do you want to delete  ${record.name}?`,
      onOk: () => {
        axios.delete('/connector/job/delete',{data})
          .then((response) => {
            if (
              response.data.code === 200
         
            ) {
              Message.success(response.data.message);
              connectorList();
            }else{
              Message.error(response.data.message);
            }
            setIsLoading(false);
           
          })
          .catch((error) => {
            setIsLoading(false);
            console.log(error);
          });
      },
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
    });
    setIsLoading(false);
  }
  

  function connectorList(){
    setIsLoading(true);
    axios
      .get(`/connector/job/list?clusterId=${props.match.params.id}&clusterName=${props.match.params.name}`)
      .then((response) => {
        if (
          response.data.code === 200 &&
          response.data.data !== undefined &&
          response.data.data.length > 0
        ) {
          const data = response.data.data;
          setDataSource(data);
          setFilterDataSource(data);
        }
        setIsLoading(false);
      })
      .catch((error) => {
        setIsLoading(false);
        console.log(error);
      });
  }

  function getTeam(){
    axios
      .get('/team/userteam')
      .then((response) => {
        if (
          response.data.code === 200 &&
            response.data.data !== undefined &&
            response.data.data.length > 0
        ) {
          setTeamData(response.data.data);
        }
      })
      .catch((error) => {
        console.log(error);
      });
  };

  
  const redrawPageData =(value)=>{
    setPageData(value);
  }

  const onSort = (value, order) => {
    let data = sortDataByOrder(filterDataSource, value, order);
    data = Object.assign([],data)
    setFilterDataSource(data);
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

  
  const refreshTableData =(value)=>{
    setFilterDataSource(value);
  }


  function handleAdd(){
    if(teamData.length > 0){
      // setVisable(true);
      // setEditVisable(true);
      props.history.push(`/connector/job/task/${props.match.params.clusterId}/${props.match.params.clusterName}/${props.match.params.name}
      /${props.match.params.id}/0`);
      // props.history.push(`/connector/job/task/${props.match.params.clusterId}/${props.match.params.clusterName}/${props.match.params.name}/${props.match.params.id}/0`);
    }else{
      Dialog.alert({
        title: 'Alert',
        content: 'please join a team',
      });
    }
    
  }

  function changeVisable(value){
    setVisable(value);
    setEditVisable(!value);
  }


  function updateConnector(value){
    const params = {};
    params.clusterId =  props.match.params.id;  
    params.script = value.script.replace(/[\r\n\t]/g,'');
    params.name = value.name;
    params.teamId = value.teamId;
    params.className = value.class;
    params.type=value.type;
    if(value.id){
      params.id = value.id;
      params.ownerId = value.ownerId;
    }
    const result = {};
    result.clusterName = props.match.params.name;
    result.connectorJob = params;
    axios.put('/connector/job/update',result)
      .then((response) => {
        if (
          response.data.code === 200
           
        ) {
          Message.success(response.data.message);
        }else{
          Message.error(response.data.message);
        }
      })
      .catch((error) => {
      
        console.log(error);
      });

  }

  function addConnector(value){
    if(value.id){
      updateConnector(value);
      return;
    }
    const params = {};
    params.clusterId =  props.match.params.id;    
    const script = value.script.replace(/[\r\n\t]/g,'');  
    params.script = `{"name": "${value.name}","config":${script}}`;
    params.className = value.class;
    params.type=value.type;
    params.teamId = value.teamId;
    params.name = value.name;    
    const result = {};
    result.clusterName = props.match.params.name;
    result.connectorJob = params;

    axios.post('/connector/job/insert',result)
      .then((response) => {
        if (
          response.data.code === 200
           
        ) {
          Message.success(response.data.message);
        }else{
          Message.error(response.data.message);
        }
      })
      .catch((error) => {
      
        console.log(error);
      });
  }
  // if(visable){
  //   return (<>
  //     <AddJob visable={editVisable} changeVisable={changeVisable} 
  //       teamData={teamData} addConnector={addConnector} 
  //       clusterId={props.match.params.id}
  //       clusterName={props.match.params.name}
  //       formVal={formVal}
  //     /></>);
  // }else{
    return (
      <div>      
        <CustomBreadcrumb items={[breadcrumb]} title={props.match.params.name} />
        <IceContainer className={styles.container}>
          <Loading visible={isLoading} className={styles.loading}>
            <Row style={styles.row}>
              <Col align="center">
                <span style={{ fontWeight: '600' }}>
                    Name :&nbsp;&nbsp;&nbsp;
                </span>
                <Input  
                  placeholder="Connector job Name"
                  hasClear
                  onChange={handleFilterChange}
                  style={{ width: '300px' }}
                  value={searchValue}
                />
              </Col>
              <Col align="center">
                <Button type="secondary" onClick={()=>handleAdd()}>
                  <Icon type="add" />
                      Add 
                </Button>
              </Col>
            </Row>
            <br/>
        
    
            <Table dataSource={pageData} hasBorder={false} onSort={(value, order) => onSort(value, order)}>
              <Table.Column
                title="Connectors"
                dataIndex="name"
                sortable
                //   cell={renderConnectorName}
              />
              <Table.Column
                title="Status"
                dataIndex="state"
                sortable
                cell={renderStatus}
              />
              <Table.Column
                title="Type"
                dataIndex="type"
                sortable
              />

              <Table.Column
                title="Team"
                dataIndex="team.name"
               
              />

              <Table.Column
                title="Owner"
                dataIndex="owner.name"
               
              />
              <Table.Column title="Operation"
                cell={renderOperation}
              />
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


// }
export default ConnectorJob;