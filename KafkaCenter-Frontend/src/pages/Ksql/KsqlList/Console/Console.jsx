import React, { Component } from 'react';
import { Grid, Button, Divider, Card, Input, Icon, Message } from '@alifd/next';
import IceContainer from '@icedesign/container';
import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/mode-sql';
import 'ace-builds/src-noconflict/theme-xcode';
import { withRouter } from 'react-router-dom';
import JSONPretty from 'react-json-pretty';
import 'brace/ext/language_tools';


const { Row, Col } = Grid;


@withRouter
export default class Console extends Component {
  state = {
    id: `${this.props.match.params.clusterName}|${this.props.match.params.ksqlServerId}`,
    consoleValue: '',
    runStatus: true,
    stopStatus: true,
    queryPropertiesStatus: false,
    queryProperties: new Map(),
    queryPropertiesShow: [],
    queryPropertiesShowMap: new Map(),
    results: [],
    schema: [],
    jsonView: null,
    mounted: true,
  };
  ws = new WebSocket(window.location.host.indexOf('4444') > 0 ? 'ws://127.0.0.1:8080/ksql_console' : `ws://${window.location.host}/ksql_console`);


  customAceEditorCompleter = {
    getCompletions(editor, session, pos, prefix, callback) {
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
    },
  };


  componentDidMount() {
    this.ws.onopen = () => {
      this.setState({ runStatus: false, stopStatus: true });
    };

    this.ws.onclose = () => {
      if (!this.state.mounted) {
        return;
      }
      this.setState({ runStatus: true, stopStatus: true });
    };

    this.ws.onmessage = (msg) => {
      if (!this.state.mounted) {
        return;
      }
      try {
        const value = this.state.consoleValue.toLowerCase();
        const json = JSON.parse(msg.data);
        if (value.startsWith('select') || json.errorMessage) {
          if (this.state.schema.length === 0) {
            const str = json.header.schema;
            const heads = str.split(',');
            let schema = [];
            for (let index = 0; index < heads.length; index += 1) {
              const element = heads[index].substring(heads[index].indexOf('`') + 1, heads[index].lastIndexOf('`'));
              schema = [...schema, element];
            }
            this.setState({ schema });
          } else {
            try {
              const columns = json.row.columns;
              const stateSchema = this.state.schema;
              const result = {};
              for (let index = 0; index < stateSchema.length; index += 1) {
                result[stateSchema[index]] = columns[index];
              }

              const results = [result, ...this.state.results];
              this.setState({ results });
            } catch (error) {
              const jsonView = (<JSONPretty id="json-pretty" data={msg.data} keyStyle="color:#008080;font-size:1.5em" valueStyle="color:#0f1e78;font-size:1.5em" />);
              this.setState({ jsonView });
              console.warn(error);
            }
          }
        } else {
          const jsonView = (<JSONPretty id="json-pretty" data={msg.data} keyStyle="color:#008080;font-size:1.5em" valueStyle="color:#0f1e78;font-size:1.5em" />);
          this.setState({ jsonView, runStatus: false, stopStatus: true });
        }
      } catch (error) {
        const jsonView = (<JSONPretty id="json-pretty" data={msg.data} keyStyle="color:#008080;font-size:1.5em" valueStyle="color:#0f1e78;font-size:1.5em" />);
        this.setState({ jsonView, runStatus: false, stopStatus: true });
        console.error(error);
      }
    };
  }

  componentWillUnmount() {
    const socketData = {
      id: this.state.id,
      message: '',
      operate: 'stop',
    };
    this.setState({
      mounted: false,
    });
    this.ws.send(JSON.stringify(socketData));
  }


  onChange = (value) => {
    this.setState({
      consoleValue: value,
    });
  }

  onRun = () => {
    let consoleValue = this.state.consoleValue;
    if (consoleValue == null || consoleValue.trim().length === 0) {
      Message.error('can not be empty!');
    } else {
      this.setState({
        results: [],
        jsonView: null,
      });
      let type = 'ksql';
      const value = consoleValue.toLowerCase();
      if (value.startsWith('select')) {
        type = 'query';
      }

      const propsMap = this.state.queryProperties;
      const streamsProperties = {};
      propsMap.forEach((v) => {
        if ((v.n != null && v.n.trim().length > 0) && (v.v != null && v.v.trim().length > 0)) {
          streamsProperties[v.n] = v.v;
        }
      });

      if (!consoleValue.endsWith(';')) {
        consoleValue += ';';
      }


      const socketData = {
        id: this.state.id,
        message: JSON.stringify({ ksql: consoleValue, streamsProperties }),
        operate: 'run',
        type,
      };
      this.ws.send(JSON.stringify(socketData));
      this.setState({
        runStatus: true,
        stopStatus: false,
      });
    }
  }

  onStop = () => {
    const socketData = {
      id: this.state.id,
      message: '',
      operate: 'stop',
    };
    this.ws.send(JSON.stringify(socketData));
    this.setState({
      runStatus: false,
      stopStatus: true,
    });
  }

  onAddQueryProperties=() => {
    if (this.state.queryPropertiesShow.length === 0) {
      const obj = (<div key={0}><Row>&nbsp;</Row><Row><Col> <Input defaultValue="auto.offset.reset" onChange={this.onPropertiesChange} name="0n" />&nbsp;=&nbsp;<Input defaultValue="latest" onChange={this.onPropertiesChange} name="0v" />&nbsp;&nbsp; <Button primary="true" text onClick={() => this.onRemoveProperties(0)}><Icon type="ashbin" /> </Button></Col></Row></div>);
      const map = new Map();
      map.set(0, obj);
      const propsMap = this.state.queryProperties;
      propsMap.set('0', { n: 'auto.offset.reset', v: 'latest' });
      this.setState({
        queryPropertiesStatus: !this.state.queryPropertiesStatus,
        queryPropertiesShow: [obj],
        queryPropertiesShowMap: map,
        queryProperties: propsMap,
      });
    } else {
      this.setState({
        queryPropertiesStatus: !this.state.queryPropertiesStatus,
      });
    }
  }
  onRemoveProperties=(index) => {
    index = index ? Number(index.index) : 0;
    const map = this.state.queryPropertiesShowMap;
    const propsMap = this.state.queryProperties;
    propsMap.delete(`${index}`);
    map.delete(index);
    const array = [];
    map.forEach((element) => { array.push(element); });
    if (map.size === 0) {
      this.setState({
        queryPropertiesStatus: !this.state.queryPropertiesStatus,
      });
    }
    this.setState({
      queryPropertiesShow: array,
      queryPropertiesShowMap: map,
      queryProperties: propsMap,
    });
  }

  onPropertiesChange=(v, e) => {
    const name = e.currentTarget.name;
    const index = name.substring(0, 1);
    const lable = name.substring(1, 2);

    const queryPropertiesTemp = this.state.queryProperties;

    const obj = queryPropertiesTemp.get(index) ? queryPropertiesTemp.get(index) : {};
    obj[lable] = v;
    queryPropertiesTemp.set(index, obj);
    this.setState({
      queryProperties: queryPropertiesTemp,
    });
  }


  onAddAnotherProperties=() => {
    const index = this.state.queryPropertiesShow.length;
    const obj = (<div key={index}><Row>&nbsp;</Row><Row><Col> <Input onChange={this.onPropertiesChange} name={`${index}n`} />&nbsp;=&nbsp;<Input onChange={this.onPropertiesChange} name={`${index}v`} />&nbsp;&nbsp; <Button primary="true" text onClick={() => this.onRemoveProperties({ index })}><Icon type="ashbin" /> </Button></Col></Row></div>);
    const map = this.state.queryPropertiesShowMap;
    map.set(index, obj);
    this.setState({
      queryPropertiesShow: [...this.state.queryPropertiesShow, obj],
      queryPropertiesShowMap: map,
    });
  }


  render() {
    const { results, jsonView } = this.state;
    const nomessage = (
      <div style={styles.messageStyle}>
        <h1>No new messages</h1>
        The message browser shows messages that have arrived since this page was opened.
      </div>
    );
    return (
      <IceContainer>
        <Card
          className="free-card custom"
          free
        >
          <div className="free-card-main">

            <Card.Content>
              <Row>
                <Col span="24" >
                  <AceEditor
                    placeholder="Example: SELECT field1, field2, field3 FROM mystream WHERE field1 = ‘somevalue’ EMIT CHANGES;"
                    mode="sql"
                    height="100px"
                    value={this.state.consoleValue}
                    width="100%"
                    theme="xcode"
                    name="blah2"
                    onChange={this.onChange}
                    fontSize={14}
                    showPrintMargin
                    showGutter
                    highlightActiveLine
                    setOptions={{
          enableBasicAutocompletion: [this.customAceEditorCompleter],
          enableLiveAutocompletion: true,
          showLineNumbers: false,
          tabSize: 2,
        }}
                  />
                </Col>
              </Row>
            </Card.Content>
            <Card.Actions>
              <Divider dashed />
              <Row>
                <Col span="12" >
                  <Row><a href="javascript:void(0)" onClick={this.onAddQueryProperties} style={{ display: 'block', textAlign: 'left', textDecoration: 'underline' }} >Add query properties</a></Row>
                  <div style={{ display: this.state.queryPropertiesStatus ? '' : 'none' }}>
                    {this.state.queryPropertiesShow.map((obj) => { return obj; })}
                    <Row>&nbsp;</Row>
                    <Row><a href="javascript:void(0)" onClick={this.onAddAnotherProperties} style={{ display: 'block', textAlign: 'left', textDecoration: 'underline' }} >Add another field</a></Row>
                  </div>

                </Col>
                <Col span="12">
                  <span style={{ display: this.state.runStatus ? '' : 'none' }}>Running...&nbsp;&nbsp;</span>
                  <span>
                    <Button type="primary" disabled={this.state.runStatus} onClick={this.onRun}>&nbsp;&nbsp; Run&nbsp;&nbsp;</Button> &nbsp;&nbsp;
                    <Button type="secondary" disabled={this.state.stopStatus} onClick={this.onStop}>&nbsp;&nbsp;Stop&nbsp;&nbsp;</Button>
                  </span>
                </Col>
              </Row>
            </Card.Actions>
          </div>
        </Card>
        <Card
          style={styles.resultStyle}
          className="free-card custom"
          free
        >
          <div className="free-card-main">
            <Card.Content>
              {jsonView || results.length > 0 ? null : nomessage}
              { results.map((str) => { return <p key={str.ROWTIME}>{JSON.stringify(str)}</p>; })}
              { jsonView }
            </Card.Content>
          </div>
        </Card>
      </IceContainer>
    );
  }
}

const styles = {
  resultStyle: {
    marginTop: '50px',
    minHeight: '200px',
  },
  messageStyle: {
    textAlign: 'center',
  },
};
