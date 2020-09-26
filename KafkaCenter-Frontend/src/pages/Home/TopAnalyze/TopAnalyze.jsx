
import React, { useEffect, useState } from 'react';
import { Card,Grid,ResponsiveGrid,Table,Tag } from '@alifd/next';
import dayjs from 'dayjs';
import axios from '@utils/axios';
import { thousandSplit } from '@utils/dataFormat';
import LineChart from './LineChart';
import styles from './index.module.scss';
import PieChart from './PieChart';

const { Row, Col } = Grid;
const { Cell } = ResponsiveGrid;

function TopAnalyze(){
  
  const [chartData,setChartData] = useState([ 
  ]);
  
   
  const [title,setTitle] = useState('Top 10 Log Size Of Topic');
  const  [chartHeight,setChartHeight] = useState(260);
  const  [tableData,setTableData] = useState([]);

  const [logSizeData,setLogSizeData] = useState([]);
  const [topFileSizeData,setTopFileSizeData] = useState([]);

  const fetchLogSizeData = async()=>{
    const start = dayjs().subtract(7, 'days').valueOf();
    const end = dayjs().valueOf();
    await axios
      .get(`/home/topic/log/size?start=${start}&end=${end}`)
      .then((response) => {
        if (response.data.code === 200) {
          if (response.data.data) {
            setLogSizeData(response.data.data);
          }
        }
      })
      .catch((error) => {
        console.error(error);
      });
  }

  const fetchFileSizeData = async() =>{
    const start = dayjs().subtract(7, 'days').valueOf();
    const end = dayjs().valueOf();
    await axios
      .get(`/home/topic/top/file/size?start=${start}&end=${end}`)
      .then((response) => {
        if (response.data.code === 200) {
          if (response.data.data) {
            setTopFileSizeData(response.data.data);
          }
        }
      })
      .catch((error) => {
        console.error(error);
      });
  }

  const fetchTableData = async() =>{
    await axios
      .get('/home/topic/consumer/alert')
      .then((response) => {
        if (response.data.code === 200) {
          if (response.data.data) {
            setTableData(response.data.data);
          }
        }
      })
      .catch((error) => {
        console.error(error);
      });
  }

  
  useEffect(()=>{
    fetchLogSizeData();
    fetchFileSizeData();
    fetchTableData();
  },[]);

  const renderTime = (value, index, record) =>{
    return dayjs(value).format('HH:mm')
  }

  const tableView = () =>{
    return (
      <Card free style={{height:'340px'}}>
        <React.Fragment>
          <Card.Header title={<span className={styles.title}>Top 10 Consumer Group State Of Topic</span>}  />
          <Card.Divider />
        </React.Fragment>
        <Card.Content style={{ marginTop:'5px'}}>
          <Table
            hasBorder={false}
            className={styles.Table}
            dataSource={tableData}
          >
            <Table.Column
              title="Cluster"
              dataIndex="clusterName"
              key="clusterName"
              width='10%'
              cell={(value,index,record)=>{
                return <span title={value}>{value}</span>
              }}
            />
            <Table.Column
              title="Topic"
              dataIndex="topic"
              key="topic"

              cell={(value,index,record)=>{
                return <span title={value}>{value}</span>
              }}
            />
            <Table.Column
              title="Group"
              dataIndex="group"
              key="group"
      
              cell={(value,index,record)=>{
                return <span title={value}>{value}</span>
              }}
            />
            <Table.Column
              title="Lag"
              dataIndex="lag"
              key="lag"
              width='10%'
              cell={(value,index,record)=> { return <span title={thousandSplit(value)}>{thousandSplit(value)}</span> }}
            />
            <Table.Column
              title="State"
              dataIndex="status"
              width='10%'
              cell={(value,index,record)=>{ 
                if(value==='ACTIVE') return  <span style={{ 'color':'green'}} title={value}>{value}</span>
                else if(value==='DEAD') return <span style={{ 'color':'red'}} title={value}>{value}</span>
                else if(value==='UNKNOWN') return <span style={{ 'color':'#e6c619'}} title={value}>{value}</span>
               
              }}
            />
            {/*  <Table.Column
              title="Time"
              dataIndex="currentTime"
              key="currentTime"
              width='10%'
              cell={renderTime}
            /> */}
          </Table>
        </Card.Content>
      </Card>
     
    );
  }

  return (
    <div>
      <ResponsiveGrid gap={20} style={{ marginTop:'5px'}}>
        <Cell colSpan={6}>  <LineChart  data={topFileSizeData} title="Top 10 File Size Of Topic" chartHeight={230} format={false}/></Cell>
        <Cell colSpan={6}>  <PieChart/></Cell>
      </ResponsiveGrid>
      <ResponsiveGrid gap={20} style={{ marginTop:'5px'}}>
       
        <Cell colSpan={6}>  <LineChart  data={logSizeData} title={title} chartHeight={230} style={{ width:'800px !important'}} format/></Cell>
        <Cell colSpan={6}> {tableView()}</Cell>
      </ResponsiveGrid>

      
    

    </div>
   
  );

}

export default TopAnalyze;