/* eslint-disable indent */
import React, { Component } from 'react';
import { Table, Icon } from '@alifd/next';
import { withRouter } from 'react-router-dom';
import FoundationSymbol from '@icedesign/foundation-symbol';
import dayjs from 'dayjs';
import IceImg from '@icedesign/img';
import '../TopicDetail.scss';


const renderHost = (value, index, record) => {
  if (value) {
    return value.toString().replace('/', '');
  }
      return '';
};

const splitTimer = (value) => {
  if (value) {
    let timer = new Array();
    let result;
    timer = value.toString().split('-');
    const resp = '\\d{10}|\\d{13}';
    timer.map((objs) => {
      if (objs.match(resp)) {
        result = objs;
      }
    });

    // eslint-disable-next-line radix
    return dayjs(parseInt(result)).format('YYYY-MM-DD HH:mm:ss');
  }
  return '';
};

const getCellProps = (rowIndex, colIndex) => {
    if (rowIndex === 0) {
       return propsConf;
    }
};

const propsConf = {
    style: { background: '#dddddd' },
    onDoubleClick: () => {

    },
};
@withRouter
export default class ConsumerGroup extends Component {
  constructor(props) {
    super(props);
    this.state = {
      display: false,
    };
  }

  onClick = () => {
     this.setState({
         display: !this.state.display,
     });
  }
  componentDidMount() {
        let taryger;
        if (this.props.config.isZk) {
             taryger = document.getElementsByClassName('zktest')[0];
        } else {
            taryger = document.getElementsByClassName('custom-table')[0];
        }
        // taryger.getElementsByTagName('thead');
        taryger.getElementsByTagName('thead')[0].style.display = 'table-header-group';
  }

  componentWillMount() {
    this.mounted = true;
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  handelGroupChart = (record) => {
    let zkName;
    if (this.props.config.isZk) {
        zkName = 'zk';
    } else {
        zkName = 'broker';
    }
    this.props.history.push(`/monitor/topic/consumer_offsets/chart/${this.props.record.clusterID}/${this.props.record.topicName}/${record.group}/${zkName}`);
  };

  renderNumber = (value) => {
      if (value < 0) {
         return '-';
      }
      return value;
  }

  render() {
    let type;
    let data = this.props.datasource;
    if (this.state.display) {
        type = <Icon type="arrow-up" size="xs" onClick={(e) => { this.onClick(e); }} />;
    } else {
        type = <Icon type="arrow-down" size="xs" onClick={(e) => { this.onClick(e); }} />;
        data = data.slice(0, 1);
    }
    const renderObj = (value) => {
      if (value === '') {
        if (this.props.config.isZk) { return <div id="advanced">      
        <IceImg
       title={this.props.config.consumerGroupState}
       height={26}
       width={27}
        src={require('../../../../../../../public/images/green.svg')}
        style={{ 'textAlign':'center','verticalAlign':'middle'}}
     //  className="circle" 
      />
      </div>;
    
    }
          return <div id="advanced">      
          <IceImg
         title={this.props.config.consumerGroupState}
         height={26}
          width={27}
          src={require('../../../../../../../public/images/green.svg')}
          style={{ 'textAlign':'center','verticalAlign':'middle'}}
      //   className="circle" 
        />
        </div>;
      }
      return <div id="advanced">      
      <IceImg
     title={this.props.config.consumerGroupState}
     height={26}
     width={27}
      src={require('../../../../../../../public/images/green.svg')}
      style={{ 'textAlign':'center','verticalAlign':'middle'}}
    // className="circle" 
    />
    </div>;
    };
    const render = (value, index, record) => {
      if (index !== 0) {
          return '';
      }
      if (this.props.config.isSimpleConsumerGroup && !this.props.config.isZk) {
          return <div>{type}<a style={styles.topicLink} onClick={() => this.handelGroupChart(record)}>{value}</a><span title="SimpleConsumerGroup" className="onMeous"><FoundationSymbol type="customize" size="small" /></span></div>;
      }
      return <div>{type}&nbsp;<a target="_blank" style={styles.topicLink} onClick={() => this.handelGroupChart(record)}>{value}</a></div>;
    };
    const renderYellowObj = (value) => {
        if (value === '') {
          if (this.props.config.isZk) {  return <div >  
          <IceImg
          title="SimpleConsumerGroup无法判断其状态"
          height={26}
          width={27}
          src={require('../../../../../../../public/images/warning.svg')}
          style={{ 'textAlign':'center','verticalAlign':'middle'}}
       //  className="yellowcricle" 
        />
        </div>; }
            return <div   id="contions" >  
            <IceImg
            title="SimpleConsumerGroup无法判断其状态"
            height={26}
            width={27}
            src={require('../../../../../../../public/images/warning.svg')}
            style={{ 'textAlign':'center','verticalAlign':'middle'}}
        //  className="yellowcricle" 
          />
          </div>;
        }
        return <div   id="contions" >  
        <IceImg
        title="SimpleConsumerGroup无法判断其状态"
        height={26}
        width={27}
        src={require('../../../../../../../public/images/warning.svg')}
        style={{ 'textAlign':'center','verticalAlign':'middle'}}
      //  className="yellowcricle" 
      />
      </div>;
      };
      const StopObj = (value) => {
        if (this.props.config.isZk) return <div className="circleStop">
             <IceImg
       title={this.props.config.consumerGroupState}
       height={26}
       width={27}
        src={require('../../../../../../../public/images/error.svg')}
        style={{ 'textAlign':'center','verticalAlign':'middle'}}
       // className="circleStop" 
      />
        </div>;
        return <div> 
        <IceImg
       title={this.props.config.consumerGroupState}
       height={26}
       width={27}
        src={require('../../../../../../../public/images/error.svg')}
        style={{ 'textAlign':'center','verticalAlign':'middle'}}
      //  className="circleStop" 
      />
      </div>;
      };

      let result = null;
      if (this.props.config.isSimpleConsumerGroup && !this.props.config.isZk) {
        result = <Table.Column title="Running Status" dataIndex="status" cell={renderYellowObj} width={100} />;
      } else if (this.props.config.consumerGroupState === 'EMPTY') {
            result = <Table.Column title="Running Status" dataIndex="status" titile={this.props.config.consumerGroupState} cell={StopObj} width={100} />;
          } else if (this.props.config.consumerGroupState === 'STABLE') {
            //if()
            if (this.props.config.isConsumber){
                result = <Table.Column title="Running Status" dataIndex="status" cell={renderObj} width={100} />;
            }else{
                result = <Table.Column title="Running Status" dataIndex="status" cell={StopObj} width={100} />;
            }
            
          } else {
            result = <Table.Column title="Running Status" dataIndex="status" cell={renderYellowObj} width={100} />;
          }
      if (this.props.config.isZk) {
          if (this.props.config.isConsumber) {
            result = <Table.Column title="Running Status" dataIndex="status" cell={renderObj} width={100} />;
          } else {
            result = <Table.Column dataIndex="status" title="Running Status" cell={StopObj} width={100} />;
          }
      }

    return (
      <div className={this.props.config.isZk ? 'zktest' : ''}>

        <Table className="custom-table" dataSource={data} cellProps={getCellProps}>
          <Table.Column title="Consumer Group" dataIndex="group" cell={render} width={150} />
          <Table.Column title="Partition" dataIndex="partition" width={50} />
          <Table.Column title="Offset" dataIndex="offset" width={60} cell={this.renderNumber} />
          <Table.Column title="LogSize" dataIndex="logEndOffset" width={100} />
          <Table.Column title="Lag" dataIndex="lag" width={60} cell={this.renderNumber} />

          {this.props.config.isZk ? null : <Table.Column title="Host" dataIndex="host" width={100} cell={renderHost} />}

          {
              this.props.config.isZk ? <Table.Column title="Last Seen" dataIndex="clientId" cell={splitTimer} width={100} /> : null }
          <Table.Column title="Owner" dataIndex="clientId" width={350} />

          { result}


        </Table>
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
};
