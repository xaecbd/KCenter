import React, { useEffect, useState } from 'react';
import {
  Card,
  Form,
  ResponsiveGrid,
  Grid,
  Field,
  Input,
  Radio,
  Select,
  Button,
  Dialog,
  Icon,
  Message,
} from '@alifd/next';
import axios from '@utils/axios';
import AceEditor from 'react-ace';
import styles from './index.module.scss';
import 'ace-builds/src-noconflict/theme-tomorrow_night_eighties';
import 'brace/ext/language_tools';

const { Row, Col } = Grid;

function AddComponent(props) {
  const [fromVal,setFromVal] = useState({
    script:''
  });


  const [teamData,setTeamData] = useState([]);

  useEffect(()=>{
    const json = JSON.parse(JSON.stringify(props.value));
    if(Object.keys(json).length>0){
      const data = Object.assign(json,{});
      const team = data.team;
      if(team){
        data.teamId = team.id;        
        data.teamName = team.name;
      }else{
        delete data.teamId
      }
      if(!data.script){
        data.script = '';
      }

      if(props.teamData.length<=0){
        restructTeamData(team);
      }else{
        setTeamData(props.teamData);
      }
     
      setFromVal(data);
     
    }else{
      const data = { script:''};
      setFromVal(data);
      setTeamData(props.teamData);
    }
    
  },[props]);

  function restructTeamData(team){
    if(team){
      const data = Object.assign(props.teamData,{}) ;
      data.push({
        'value':team.id,
        'label':team.name
      });
      setTeamData(data);
    }else{
      setTeamData(props.teamData);
    }
   
  }

  const field = Field.useField({
    values: fromVal,
  });

  function addVisableChange(){ 
    props.visableChange(!props.visable);
  }

  
  const customAceEditorCompleter = {
    getCompletions(editor, session, pos, prefix, callback){
      const wordList = ['create', 'create stream', 'create stream as select', 'create table', 'create table as select', 'drop stream', 'drop table', 'as', 'select', 'left', 'full', 'inner', 'join', 'window', 'windowstart', 'where', 'rowkey', 'group', 'by', 'having', 'and', 'emit', 'changes', 'type', 'stream', 'with', 'drop', 'table', 'show', 'types', 'insert', 'into', 'values', 'describe', 'extended', 'function', 'explain', 'if', 'exists', 'delete', 'topic', 'print', 'from', 'beginning', 'interval', 'limit', 'rowtime', 'set', 'cast', 'kafka_topic', 'case', 'when', 'then',
        'else', 'end', 'like', 'between', 'list', 'functions', 'topics', 'streams', 'tables', 'queries', 'properties', 'spool', 'terminate', 'struct', 'abs', 'arraycontains', 'as_array', 'as_map', 'ceil', 'concat', 'unix_date', 'unix_timestamp', 'datetostring', 'elt', 'entries', 'extractjsonfield', 'exp', 'field', 'floor', 'generate_series', 'generate_series', 'geo_distance', 'ifnull', 'initcap', 'lcase', 'len', 'ln', 'mask', 'mask_keep_left', 'mask_keep_right', 'mask_left', 'mask_right', 'random', 'replace',
        'round', 'sign', 'sqrt', 'slice', 'split', 'stringtodate', 'stringtotimestamp', 'substring', 'timestamptostring', 'trim', 'ucase', 'url_decode_param', 'url_encode_param', 'url_extract_fragment', 'url_extract_host', 'url_extract_parameter', 'url_extract_path', 'url_extract_port', 'url_extract_protocol', 'url_extract_query', 'collect_list', 'collect_set', 'count', 'histogram', 'average', 'max', 'min', 'sum', 'topk', 'topkdistinct', 'windowstart', 'windowend', 'explode', 'value_format', 'partition',
        'partitions', 'replicas', 'value_delimiter', 'key', 'timestamp', 'timestamp_format', 'wrap_single_value', 'window_type', 'window_size', 'kafka_ceter is best.'];
      callback(null, [...wordList.map((word) => {
        return {
          caption: word,
          value: word,
          meta: 'static',
        };
      }), ...session.$mode.$highlightRules.$keywordList.map((word) => {
        return {
          caption: word,
          value: word,
          meta: 'keyword',
        };
      })]);
    }
 
    
  };

  

  const saveData = async()=>{
    const { errors } = await field.validatePromise();  
    if (errors) {
      return;
    }
    const params = {};
    const values = field.getValues();
    if(props.moudlue==='Stream'){
      params.kStreamInfo = values;
      params.kStreamInfo.id = fromVal.id;
    }else{
      params.kTableInfo = values;
      params.kTableInfo.id = fromVal.id;
    }
    props.saveData(params);
  }

  
  const onChange = (value) => {
    fromVal.script = value;
    setFromVal(fromVal);
  }
 

  return (
    <>
      <Dialog
        visible={props.visable}
        onClose={addVisableChange}
        onCancel={addVisableChange}
        className={styles.simpleFormDialog}
        onOk={saveData}
        isFullScreen
      >
        <div>
          <h4>{props.name} {props.moudlue}</h4>
        </div>
        <hr />
        <Card free className={styles.Card}>
          <Card.Content>
            <Form
              fullWidth
              field={field}
              className={styles.HierarchicalForm}
              value={fromVal}
            >
              <Row gutter="2">
                <Col>
                  <Form.Item
                    label={`${props.moudlue} Name`}
                    required
                    requiredMessage={`please input the ${props.moudlue} name `}
                  >
                    <Input name="name" placeholder={`${props.moudlue} name`} />
                  </Form.Item>
                </Col>
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
                      defaultValue=""
                      // value={fromVal.teamId}
                    />
                    
                  </Form.Item>
                </Col>
              </Row>
              <ResponsiveGrid
                gap={[0, 15]}
                columns={1}
                className={styles.HierarchicalBlock}
              >
                <ResponsiveGrid.Cell
                  colSpan={{ desktop: 3, tablet: 1, phone: 2 }}
                >
                  <Form.Item
                    label="Query"
                    required
                    requiredMessage={`please input the create ${props.moudlue} script `}
                  >
                    <AceEditor                     
                      placeholder="Example: CREATE TABLE OR CREATE STREMA"
                      mode="sql"
                      height="500px"
                      width="100%"
                      theme="tomorrow_night_eighties"
                      name="script"
                      value={fromVal.script}
                      // value={props.moudlue==='Edit'?fromVal.script:''}
                      onChange={onChange}
                      fontSize={14}
                      showPrintMargin
                      showGutter
                      highlightActiveLine
                      setOptions={{
                        enableBasicAutocompletion: [customAceEditorCompleter],
                        enableLiveAutocompletion: true,
                        showLineNumbers: false,
                        tabSize: 2,
                      }}
                    />
                  </Form.Item>
                </ResponsiveGrid.Cell>
              </ResponsiveGrid>
           
              
            </Form>
          </Card.Content>
        </Card>
      </Dialog>
    </>
  );
}

export default AddComponent;