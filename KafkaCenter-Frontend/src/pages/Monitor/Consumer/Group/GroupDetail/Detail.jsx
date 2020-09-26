import React, {  useState, useEffect } from 'react';
import { Button, Loading, Message } from '@alifd/next';
import { withRouter } from 'react-router-dom';
import FoundationSymbol from '@icedesign/foundation-symbol';
import axios from '@utils/axios';
import ConsumberGroup from './ConsumerGroup';




function Detail(props) {
  const[brokerMsg,setBrokerMsg] = useState([]);
  const[zkMsg,setZkMsg] = useState([]);
  const[isConsumberLoading,setIsConsumberLoading] = useState(false);
  const[clusterName,setClusterName] = useState(props.match.params.clusterName);
  const[groupName,setGroupName] = useState(props.match.params.consummerGroup);


  const restructurConsumberData = (data) => {
    const zkResult = [];
    const brokerResult = [];
    const record = {
      topicName: props.match.params.topicName,
      clusterID: props.match.params.clusterID,
      clusterName: props.match.params.clusterName,
    };
    if (data) {
      let broker = 0;
      let zk = 0;
      data.map((obj) => {
        let logEndOffset = 0;
        let lag = 0;
        let offset = 0;
        const isSimpleConsumerGroup = obj.simpleConsumerGroup;
        const consumerGroupState = obj.consumerGroupState;
        const kafkaCenterGroupState = obj.kafkaCenterGroupState;

        if (obj.partitionAssignmentStates) {
          obj.partitionAssignmentStates.map((objs) => {
            logEndOffset = parseInt(objs.logEndOffset, 10) + logEndOffset;
            if (parseInt(objs.offset, 10) >= 0) {
              lag = parseInt(objs.lag, 10) + lag;
              offset = parseInt(objs.offset, 10) + offset;
            }
          });
          obj.partitionAssignmentStates.splice(0, 0, {
            group: obj.groupId,
            topic: obj.topic,
            logEndOffset,
            offset,
            lag,
            status: '',
          });
          if (obj.consumerMethod === 'broker') {
            
            const entry = {
              isSimpleConsumerGroup,
              consumerGroupState,
              kafkaCenterGroupState,
              isZk: false,
              hasHeard:broker==0
            };
            brokerResult.push({
              topic: obj.topic,
              content: <ConsumberGroup datasource={obj.partitionAssignmentStates} config={entry} record={record} groupName={groupName} consumerMethod={obj.consumerMethod} />,
            });
            broker +=1;
          }else{
            const entry = {
              isSimpleConsumerGroup,
              consumerGroupState,
              kafkaCenterGroupState,
              isZk: true,
              hasHeard:zk==0
            };
            zkResult.push({
              topic: obj.topic,
              content: <ConsumberGroup datasource={obj.partitionAssignmentStates} config={entry} record={record} groupName={groupName} consumerMethod={obj.consumerMethod} />,
            });
            zk +=1;
          }
            
        }
      });
    }
    setZkMsg(zkResult);
    setBrokerMsg(brokerResult);
  }

  const fetchDetail = (data) => {
    setIsConsumberLoading(true);
    axios.defaults.timeout = 180000;
    axios.post('/monitor/group/detail', data).then((response) => {
      if (response.data.code === 200) {
        setIsConsumberLoading(false);
        if (response.data.data.length > 0) {
          restructurConsumberData(response.data.data);
        }
      } else {
        Message.error(response.data.message);
        setIsConsumberLoading(false);
      }
    }).catch((error) => {
      Message.error('Get Consumer Offset has error.');
      setIsConsumberLoading(false);
    });
  }

  

  useEffect(()=>{
    const data = {
      clusterID: props.match.params.clusterID,
      consummerGroup: props.match.params.consummerGroup,
    };
    fetchDetail(data);
  },[]);
  


 

  const backward = () => {
    props.history.goBack();
  }

  const handleRefresh = () => {
    const data = {
      clusterID: props.match.params.clusterID,
      consummerGroup: props.match.params.consummerGroup,
    };
    fetchDetail(data);
  }
  return (
    <div>
      <Loading
        visible={isConsumberLoading}
        style={styles.loading}
      >

        <div style={styles.listTitle}><FoundationSymbol onClick={() => backward()} style={styles.backward} size="large" type="backward" />
          cluster:&nbsp;<span style={styles.listTitles}>{clusterName}</span>&nbsp;&nbsp;Group:&nbsp;<span style={styles.listTitles}>{props.match.params.consummerGroup}</span>
        </div>
        <div style={{ width: '100%', height: '5px' }}>
          {' '}
          <Button
            type="secondary"
            style={{float: 'right' }}
            onClick={(e) => {
              handleRefresh();
            }}
          >Refresh&nbsp;&nbsp;
            <FoundationSymbol size="small" type="exchange" />
          </Button>
        </div>
        {brokerMsg.length > 0 ?
          <div><div style={styles.metricTitle}>Consumers Offsets Committed to Broker</div>
            <div style={{ minHeight: '32rem', padding: '15px', background: '#fff' }} >
              {
                brokerMsg.map((obj) => {
                  return <div style={{ marginTop: '10px' }} key={obj.topic}><div >{obj.content}</div></div>;
                })
              }
            </div>
          </div> : null}

        {zkMsg.length > 0 ?
          <div><div style={styles.metricTitle}>Consumers Offsets Committed to ZK</div>
            <div style={{ minHeight: '32rem', padding: '15px', background: '#fff' }}>
              {
                zkMsg.map((obj) => {
                  return <div style={{ marginTop: '10px' }} key={obj.topic}><div >{obj.content}</div></div>;
                })
              }
            </div>
          </div> : null}

        {/* 两种消费信息都为空 显示一个固定大小白框 */}
        {zkMsg.length <= 0 && brokerMsg.length <= 0 ?
          <div>
            <div style={{ minHeight: '32rem', padding: '15px', background: '#fff' }} />
          </div> : null}
      </Loading>
    </div>
  );
}

const styles = {
  listTitle: {
    marginBottom: '10px',
    fontSize: '30px',
    fontWeight: 'bold',

  },
  metricTitle: {
    marginBottom: '10px',
    fontSize: '18px',
    fontWeight: 'bold',
    marginTop: '10px',

  },
  loading: {
    width: '100%',
  },
  backward: {
    display: 'inline-block',
    minWidth: '40px',
    marginBottom: '15px',
    cursor: 'pointer',
    color: '#0066FF',
  },
};
export default withRouter(Detail);