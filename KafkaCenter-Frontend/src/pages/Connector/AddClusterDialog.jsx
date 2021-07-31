import React, { useEffect, useState,useRef } from 'react';
import { Dialog, Grid, Input, Message, Select, Loading, Field, Form  } from '@alifd/next';
import axios from '@utils/axios';
import styles from './index.module.scss';

const FormItem = Form.Item;
const { Row, Col } = Grid;

function AddClusterDialog(props) {

  const prevCountRef = useRef(props);

  const [teamData,setTeamData] = useState([]);  
  const [isLoading,setIsLoading] = useState(false);


  const [formVal,setFormVal] = useState(props.formVal);
  const [clusterInfo,setClusterInfo] = useState([]);


  useEffect(()=>{
    if(prevCountRef.current.visible != props.visible){
      fetchCluster();
      fetchTeamData();   
      const json = JSON.parse(JSON.stringify(props.formVal));
      if(Object.keys(json).length>0){
        const val = Object.assign(json,{});
        val.clusterId = val.cluster.id;  
        val.teamIds = val.teamIds.split(',');
        setFormVal(val);
      }
      prevCountRef.current = props;
    }
   
  },[props]);




  const field = Field.useField({
    values: formVal,
  });

  function fetchTeamData(){
    axios.get('/team')
      .then((response) => {
        if (response.data.code === 200) {
          setTeamData(response.data.data);
        } else {
      
          Message.error(response.data.message);
        }
      })
      .catch((e) => {
        Message.error('Create has error.');
      });
  }

  function fetchCluster(){
    axios
      .get('/cluster')
      .then((response) => {
        if (response.data.code === 200) {
          const data = resouceData(response.data.data);
          setClusterInfo(data);
        } else {
          Message.error(response.data.message);
        }
      })
      .catch((error) => {
        console.error(error);
      });
  }

  function  resouceData(data){
    const dataSource = [];
    data.map((obj) => {
      const entry = {
        value: obj.id,
        label: obj.name,
      };
      dataSource.push(entry);
    });
    return dataSource; 
  };

  const onOk = async()=>{
    const { errors } = await field.validatePromise();  
    if (errors) {
      return;
    }
    const data = field.getValues();
    data.teamIds = data.teamIds.join(',');
    let url='/connector/insert';
    if(props.label==='Update'){
      url='/connector/update';
      data.id = formVal.id;
    }
    setIsLoading(true);
    axios.post(url,data).then((response)=>{
      if(response.data.code===200){
        Message.success(response.data.message);           
        props.refreshData();
      }else{
        Message.error(response.data.message);
      }
      setIsLoading(false);
    }).catch(e=>{
      setIsLoading(false);
      console.error(e);
    });
    props.changeVisable(false);
   
  }

  function handelDialog(){
    props.changeVisable(false);
  }
  return (
    <Dialog
      className={styles.simpleFormDialog}
      autoFocus={false}
      footerAlign="center"
      title={`${props.label} Connector`}
      onOk={onOk}
      onCancel={handelDialog}
      onClose={handelDialog}
      isFullScreen
      visible={props.visible}
      okProps={{ children: 'OK' }}
      cancelProps={{ children: 'Cancel' }}
    >
      <Form className={styles.form}  field={field} value={formVal} fullWidth>
        <div style={styles.dialogContent}>
          <Loading visible={isLoading} style={{ width: '100%' }}>
            <Row >
            
              <Col >
                <FormItem label="Name:" required>
                  <Select
                    showSearch
                    name="clusterId" 
                    dataSource={clusterInfo}
                    placeholder="please select cluster"
                    style={{ width: '100%' }}
                  />
                </FormItem>
              </Col>
            </Row>
            <Row gutter="2">
              <Col >
                <FormItem label="Connect Name:" required>
                  <Input style={{ width: '100%' }}  name="name" placeholder="Please input the connector name" />
                </FormItem>
              </Col>
            </Row>
            <Row gutter="2">
              <Col >
                <FormItem label="Connect Address:"  required>
                  <Input style={{ width: '100%' }} name="url" placeholder="127.0.0.1:8088" />
                </FormItem>
              </Col>
            </Row>
            <Row gutter="2">
              <Col>
                <FormItem label="Teams:"  required>
                  <Select
                    mode="multiple"
                    name="teamIds"
                    showSearch
                    dataSource={teamData}
                    style={{ width: '100%' }}
                  />
                </FormItem>
                             
              </Col>
            </Row>
          </Loading>
        </div>
      </Form>
    </Dialog>
  );

}

export default AddClusterDialog;