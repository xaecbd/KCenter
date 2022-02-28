import React, { useState, useEffect } from 'react';
import { Loading, Message, Button } from '@alifd/next';
import { withRouter } from 'react-router-dom';
import axios from '@utils/axios';
import FoundationSymbol from '@icedesign/foundation-symbol';
import ConsumberGroup from './ConsumberGroup';




function TopicDetail(props) {

  const styles = {
    listTitle: {
      marginBottom: '10px',
      fontSize: '30px',
    },
    listTitles: {
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
    container: {
      //  margin: '20px',
      padding: '10px 20px 20px',
      minHeight: '600px',
    },
  };

  const[brokerMsg,setBrokerMsg] = useState([]);
  const[zkMsg,setZkMsg] = useState([]);
  const[isConsumberLoading,setIsConsumberLoading] = useState(false);
  const[clusterName] = useState(props.match.params.clusterName);


  const restructurConsumberData = (data) => {
    const zkResult = [];
    const brokerResult = [];
    const record = {
      topicName: props.match.params.topicName,
      clusterID: props.match.params.id,
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
            logEndOffset,
            offset,
            lag,
            status: '',
          });
          
          if(obj.consumerMethod === 'broker'){
           
            const entry = {
              isSimpleConsumerGroup,
              consumerGroupState,
              kafkaCenterGroupState,
              isZk: false,
              hasHeard:broker==0
            };
            brokerResult.push({
              title: obj.groupId,
              content: (
                <ConsumberGroup
                  datasource={obj.partitionAssignmentStates}
                  config={entry}
                  record={record}
                  consumerMethod={obj.consumerMethod}
                />
              ),
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
              title: obj.groupId,
              content: (
                <ConsumberGroup
                  datasource={obj.partitionAssignmentStates}
                  config={entry}
                  record={record}
                  consumerMethod={obj.consumerMethod}
                />
              ),
            });
            zk +=1;
          } 
        }  
      });
    }
    setZkMsg(zkResult);
    setBrokerMsg(brokerResult);
  };

  const fetchDetail = (data) => {
    setIsConsumberLoading(true);
    axios.defaults.timeout = 180000;
    axios
      .post('/monitor/topic/consumer_offsets', data)
      .then((response) => {
        if (response.data.code === 200) {
          setIsConsumberLoading(false);
          if (response.data.data.length > 0) {
            restructurConsumberData(response.data.data);
          }
        } else {
          Message.error(response.data.message);
          setIsConsumberLoading(false);
        }
      })
      .catch((error) => {
        console.error(error);
        Message.error('Get Consumer Offset has error.');
        setIsConsumberLoading(false);
      });
  };


  useEffect(()=>{
    const data = {
      topic: props.match.params.topicName,
      clusterID: props.match.params.id,
    };
    fetchDetail(data);
  },[]);
 
  
  const backward = () => {
    window.location.href = '#/monitor/consumer';
  };
  const handleRefresh = () => {
    const data = {
      topic: props.match.params.topicName,
      clusterID: props.match.params.id,
    };
    fetchDetail(data);
  };

  const handleProduct=() => {
    props.history.push(`/monitor/producer/metric/${props.match.params.id}/${clusterName}/${props.match.params.topicName}`);
  }


  return (
    <div style={styles.container}>
      <div style={styles.listTitle}>
        <FoundationSymbol
          onClick={() => backward()}
          style={styles.backward}
          size="large"
          type="backward"
        />
        cluster:&nbsp;<span style={styles.listTitles}>{clusterName}</span>&nbsp;&nbsp;topic:&nbsp;<span style={styles.listTitles}>{props.match.params.topicName}</span>
      </div>
      <div style={{ width: '100%', height: '5px' }}>

        <Button
          type="secondary"
          style={{ float: 'right' }}
          onClick={(e) => {
            handleRefresh();
          }}
        >  Refresh&nbsp;&nbsp;
          <FoundationSymbol size="small" type="exchange" />
        </Button>
        {' '}
        <Button
          type="secondary"
          style={{ float: ' right', marginRight: '7px' }}
          onClick={() => handleProduct()}
        >
          Producer&nbsp;&nbsp;
          <FoundationSymbol size="small" type="link" />
        </Button>
      </div>
      <div style={styles.metricTitle}> Consumers Offsets
      </div>
      <Loading visible={isConsumberLoading} style={styles.loading}>
        {brokerMsg.length > 0 ? (
          <div>
            <div style={styles.metricTitle}>
              Offsets Committed to Broker
            </div>
            <div
              style={{
                minHeight: '15rem',
                padding: '15px',
                background: '#fff',
              }}
            >
              {brokerMsg.map((obj) => {
                return (
                  <div style={{ marginTop: '10px' }} key={obj.title}>
                    <div>{obj.content}</div>
                  </div>
                );
              })}
            </div>
          </div>
        ) : null}
        {zkMsg.length > 0 ? (
          <div>
            <div style={styles.metricTitle}>
              Offsets Committed to ZK
            </div>
            <div
              style={{
                minHeight: '15rem',
                padding: '15px',
                background: '#fff',
              }}
            >
              {zkMsg.map((obj) => {
                return (
                  <div style={{ marginTop: '10px' }} key={obj.title}>
                    <div>{obj.content}</div>
                  </div>
                );
              })}
            </div>
          </div>
        ) : null}
      </Loading>
    </div>
  );
}



export default withRouter(TopicDetail);
