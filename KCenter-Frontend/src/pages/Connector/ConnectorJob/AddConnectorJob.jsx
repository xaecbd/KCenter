import React, { useEffect, useState } from 'react';
import { Message, Grid, Input, Switch, Select, Button, Field, Form, Card ,Icon, Tag, Dialog  } from '@alifd/next';
import axios from '@utils/axios';
import {withRouter} from 'react-router-dom'
import AceEditor from 'react-ace';
import iconfont from '@/iconfont.js';
import 'brace/mode/json';
import 'ace-builds/src-noconflict/theme-tomorrow';
import 'brace/ext/language_tools';
import styles from './index.module.scss';

const CustomIcon = Icon.createFromIconfontCN(iconfont);


const { Row, Col } = Grid;

function AddConnectorJob(props){

  const [formVal,setFormVal] = useState({});
  const [visable,setVisable] = useState(false);   

  const [plugin,setPlugin] = useState({});
  const [val,setVal] = useState('');

  const [clusterId,setClusterId] = useState(props.match.params.connectorId);
  const [clusterName,setClusterName] = useState(props.match.params.name);
  const [validate,setValidate] = useState([]);

  const [isValidate,setIsValidate] = useState(false);

  const [taskData,setTaskData] = useState([]);
  const [pluginData,setPluginData] = useState([]);

  const [taskInfo,setTaskInfo] = useState({});
  const [taskVisable,setTaskVisable] = useState(false);
  const field = Field.useField({
    values:formVal
  });

 
  const [teamData,setTeamData] = useState([]);

  useEffect(()=>{
    if(props.match.params.id != 0){
      getConnectorJob();
    }else{
      setVisable(true);
      fetchConnectorPlugins();
     
    }
     
  },[]);


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


  function getConnectorJob(){
    axios
      .get(`/connector/job/search?id=${props.match.params.id}`)
      .then((response) => {
        if (
          response.data.code === 200 &&
        response.data.data !== undefined
        ) {
          const json = response.data.data;
          if(Object.keys(json).length>0){
          
            fetchTaskStatus(json.name);
            const data = Object.assign(json,{});
            if(data.team){
              data.teamId = data.team.id;        
            }else{
              delete data.teamId
              delete data.team
            }
      
            restructTeamData(data.team);
            const plugins = plugin;
            plugins.class = data.className;
            plugins.type = data.type;
            setPlugin(plugins);
            setFormVal(data);
            const script = JSON.parse(data.script).config;
            delete script.name;
            setVal(JSON.stringify(script, null, '\t'));
            setVisable(false);
          }
        }
       
      })
      .catch((error) => {
       
        console.log(error);
      });
  }

  function restructTeamData(team){
    const teamData = getTeam();
    if(team){
      const data = [];
      data.push({
        'value':team.id,
        'label':team.name
      });
      setTeamData(data);
    }else{
      setTeamData(teamData);
    }
   
  }

  function fetchTaskStatus(name){
    axios
      .get(`/connector/job/task/status?clusterId=${clusterId}&clusterName=${clusterName}&connectorName=${name}`)
      .then((response) => {
        if (
          response.data.code === 200 &&
        response.data.data !== undefined &&
        response.data.data.length > 0
        ) {
          const data = response.data.data;
          setTaskData(data);
        }
       
      })
      .catch((error) => {
       
        console.log(error);
      });
  }

  function fetchConnectorPlugins(){
   
    axios
      .get(`/connector/job/plugins?clusterId=${clusterId}&clusterName=${clusterName}`)
      .then((response) => {
        if (
          response.data.code === 200 &&
        response.data.data !== undefined &&
        response.data.data.length > 0
        ) {
          const data = response.data.data;         
          const script = { script:''};
          setFormVal(script);
          // setVisable(true);
          setPluginData(data);
          getTeam();
        }
       
      })
      .catch((error) => {
       
        console.log(error);
      });
  }

  const onOk=async()=>{
    const { errors } = await field.validatePromise();  
    if (errors) {
      return;
    }
    const data = field.getValues();
    data.class = plugin.class;
    data.type = plugin.type;
    if(formVal.id){
      data.id = formVal.id;
      if(formVal.owner){
        data.ownerId = formVal.owner.id;
      }      
    }
    props.addConnector(data);

  }
  function handelDialog(){
    props.changeVisable(!props.visable);
  }


  function handleToAddConnector(obj){
    const lastIndex = obj.class.lastIndexOf('.');
    const data = obj.class.substring(lastIndex+1,obj.class.length);
    const value = `{
       "topics": "TopicName_${data}",
      "connector.class": "${obj.class}",
      "tasks.max": 1
    
    }`;
    setVal(value);
    setVisable(!visable);
    setPlugin(obj);
    checkConnectorJob(true,obj,value);

  }

  function validateScript(){      
    checkConnectorJob(true,null,null);
   
  }

  function checkConnectorJob(checked,plugins,value){
    if(checked){
      const params = {};
      params.clusterName = clusterName;
      const job = {};
      if(plugins){
        job.className = plugins.class;
      }else{
        job.className = plugin.class;
      }

      if(value){
        job.script = value.replace(/[\r\n]/g,'');
      }else{
        job.script = val.replace(/[\r\n]/g,'');
      }
      job.clusterId=clusterId;
      params.connectorJob = job;
      axios
        .post('/connector/job/validate',params)
        .then((response) => {
          if (
            response.data.code === 200 && response.data.data.length>0         
          ) {
          
            const data = response.data.data;
            const val = [];
            const values = JSON.parse(job.script);        
            data.forEach(e=>{
              if(e.name!=='name'){
                val.push(e);
                values[e.name]='Test';
              }             
            });
            if(!plugins){          
              setValidate(val);     
              setIsValidate(true); 
              Message.success('validate config success');            
            }else{
              setVal(JSON.stringify(values, null, '\t'));

            }
                  
          }else{
            Message.error(response.data.message);
          }
        })
        .catch((error) => {
      
          console.log(error);
        });
    }else{
      setValidate([]);
    }
    
  }

  function sinkAddConnector(obj){
    handleToAddConnector(obj);
  }


  function scriptChange(value){
    setVal(value);
  }


  function backList(){
    // props.history.go(0);
    // handelDialog();
    // setVisable();
    props.history.push(`/connector/job/${props.match.params.clusterId}/${props.match.params.clusterName}/${props.match.params.name}/${props.match.params.connectorId}`);
   
  }

  function handleTask(task){
    setTaskInfo(task);
    setTaskVisable(!taskVisable);

  }

  function taskRestart(taskInfo){
    axios
      .get(`/connector/job/restart/task?clusterId=${clusterId}&clusterName=${clusterName}&connectorName=${formVal.name}&taskId=${taskInfo.id}`)
      .then((response)=>{
        if(response.data.code===200){
          Message.success(response.data.message);
        }else{
          Message.error(response.data.message);
        }       
      })
      .catch(e=>{
      // Message.error(response.data.message);
        console.log(e);
      });
  }

  function closeTaskVisable(){
    setTaskVisable(false);
  }

  if(!visable){
    return (<Card free >
      <Card.Content>
        <div className={styles.tools}>
          <h4><span>{formVal.id ? formVal.name:'NEW Connector'}  </span></h4>
        </div>
        <div style={{marginBottom:'20px' }}>
          { plugin.type !== 'sink'? <h3 className={styles.h3}><Tag  color="gray" type="primary" size="small">{plugin.class}</Tag>
            <><CustomIcon type="iconchangjiantou"/> <span className={styles.spanText}> Kafka</span></></h3>:<h3 className={styles.h3}> 
            <span className={styles.spanText}>Kafka</span> 
            <><CustomIcon type="iconchangjiantou"/></> <Tag  color="gray" type="primary" size="small" >{plugin.class}</Tag></h3>}
        </div>

        { taskData.length>0 ?
          <Row gutter="2">
            <div>
              <h4 className={styles.h4}>Tasks</h4>
              { taskData.map(task=>{
                let className ;
                if(task.state==='RUNNING'){
                  className=styles.taskId;
                }else if(task.state==='PAUSED'){
                  className=styles.grayTaskId;
                }else{
                  className = styles.redTaskId;
                }
                return (<span onClick={()=>{ handleTask(task)}} style={{ cursor:'pointer'}}><span className={styles.task}>
                  <span className={className}>
                    {task.id}
                  </span>
                  <span className={styles.work}>
                    {task.worker_id}
                  </span>
                </span></span>
                )})
              }
            </div>
            
          </Row>
          :null}
        {
          taskVisable?<> <Row gutter="2">
            <div className={styles.task1}>            
              <Button   className={styles.restartBtn} onClick={()=>{ closeTaskVisable()}}>CLOSE</Button>
              <Button  className={styles.restartBtn} onClick={()=>{ taskRestart(taskInfo)}}>RESTART</Button>
              <h5 className={styles.h5}>Task {taskInfo.id} is {taskInfo.state} </h5>
              { taskInfo.trace?<> <h5 className={styles.h5}> TRACE:</h5>
                <div className={styles.trace}>{taskInfo.trace}</div></>:null}
             
            </div>
          </Row></>:null
        } 
       
        <Form
          fullWidth
          field={field}
          className={styles.HierarchicalForm}
          value={formVal}
        >

          <Row gutter="2">
            <Col>
              <Form.Item
                label="Name"
                required
                requiredMessage="please input the connector name  "
              >
                <Input
                  name="name"                
                  placeholder="please input the connector name"     
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter="2">
            <Col>
              <Form.Item
                label="Team"
                required
                requiredMessage="please select a team  "
              >
                <Select
                  name="teamId"
                  placeholder="please select team"
                  dataSource={teamData}
                  defaultValue=''
                />
              </Form.Item>
            </Col>
          </Row>

         

          <Form.Item
            label="script"
            required
                   
          >
            <AceEditor                     
              mode="json"
              height="400px"
              width="100%"
              theme="tomorrow"
              name="script"
              onChange={scriptChange}
              value={val}
              fontSize={14}
              showPrintMargin
              showGutter
              highlightActiveLine
              setOptions={{
                enableBasicAutocompletion: false,
                enableLiveAutocompletion: true,
                showLineNumbers: true,
                useWrapMode: true,
                tabSize: 2,
              }}
            />
          </Form.Item>
        </Form>

       
        <div className={styles.toolbars}>
          <div style={{ paddingLeft:'80%'}}>
            <Button style={{ marginRight:'5px'}}  type="secondary" onClick={()=>backList()}><CustomIcon type="iconfanhui1"/>Back</Button>

            {isValidate? <Button  type="secondary" style={{ marginRight:'5px'}} onClick={onOk}><CustomIcon type="iconsave"/> {formVal.id ? 'Edit':'Create'} </Button>:
            <Button  type="secondary" style={{ marginRight:'5px'}}  onClick={()=>validateScript()}><CustomIcon type="iconsave"/>Validate</Button>}
           
          </div>          
        </div>
        {validate.length>0?<div>{validate.map(obj=>{
          return (<><span className={styles.span}>{obj.name}:</span><span className={styles.span}>{obj.document}</span><br/></>);
        })}</div>:null}
      </Card.Content></Card>);
  }else{
    return (  
      <Card free >
        <Card.Content>
          <div>
            <h4>New Connector</h4>
          </div>
          <hr />
          <div className={styles.containers}>
             
            <div className={styles.left}>
              <div className={styles.subHeader}><span>Sources</span></div>
              {
                pluginData.map(obj=>{
                  if(obj.type==='source'){
                    const lastIndex = obj.class.lastIndexOf('.');
                    const className =obj.class.substring(lastIndex+1,obj.class.length);
                    return ( 
                      <div className={styles.div} key={obj.class}>
                        <div className={styles.text}>
                          <span ><b><a className={styles.link} onClick={()=>{handleToAddConnector(obj)}}>{className}</a></b></span>
                          <p style={{ fontSize:'12px' }}><span>Version: {obj.version}</span></p>
                        </div>
                      </div>);
                  }
                })
              }              
            </div>
            <div className={styles.right}> 
              <div className={styles.subHeader}><span>Sink</span></div>
              {
                pluginData.map(obj=>{
                  if(obj.type==='sink'){
                    const lastIndex = obj.class.lastIndexOf('.');
                    const className =obj.class.substring(lastIndex+1,obj.class.length);
                    return ( 
                      <div className={styles.div} key={obj.class}>
                        <div className={styles.text}>
                          <span><b><a className={styles.link} onClick={()=>sinkAddConnector(obj)}>{className}</a></b></span>
                          <p style={{ fontSize:'12px' }}><span>Version: {obj.version}</span></p>
                        </div>
                      </div>);
                  }
                })
              }
               
    
            </div> 
           
            {/*  */}
          
          </div>
        </Card.Content>
      </Card>
    );
  }
      


}

export default  withRouter(AddConnectorJob);