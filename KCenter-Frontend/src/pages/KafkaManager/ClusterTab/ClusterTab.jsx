import React, { useState } from 'react';
import { Tab } from '@alifd/next';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import IceContainer from '@icedesign/container';

import Broker from '../Broker/components/Broker';
import Topic from '../Topic/TopicList/components/TopicList';
import Group from '../Group/components/Group';
import Monitor from '../../Home/MonitorDetail/components/MonitorMetric';


import styles from './index.module.scss';



export default function ClusterTab(props){

  const [clusterId] = useState(props.match.params.clusterID);
  const [topic,setTopic] = useState(<Topic id={props.match.params.clusterID}/>);
  const [group,setGroup] = useState(<Group id={props.match.params.clusterID}/>);
  const [monitor,setMonitor] = useState(<Monitor id={props.match.params.clusterID}/>);


  const breadcrumb = [
    {
      link: '#/cluster',
      text: 'Cluster',
    },
    {
      link: '',
      text: `${props.match.params.clusterName}`,
    },
  ];

  const onTabChanged = (key) => {
    const url = `/cluster/${props.match.params.clusterID}/${props.match.params.clusterName}/${key}`;
    props.history.push(url);
    const timestamp = new Date().getTime();
    if (key === 'topic') {
      setTopic(<Topic key={timestamp} id={clusterId}/>);
    } else if (key === 'group') {
      setGroup(<Group key={timestamp} id={clusterId}/>);
    }else if(key==='monitor'){
      setMonitor(<Monitor key={timestamp} id={clusterId}/>);
    }
  }

  
  return(
    <div>
      <CustomBreadcrumb items={breadcrumb} title="" />
      <IceContainer className={styles.container}>
        <Tab className={styles.tab} size="small" onChange={onTabChanged} defaultActiveKey={props.match.params.tab}>
          <Tab.Item title="Broker" key="broker"><div className={styles.div}><Broker id={clusterId}/> </div></Tab.Item>
          <Tab.Item title="Topic" key="topic">{topic}</Tab.Item>
          <Tab.Item title="Group" key="group">{group}</Tab.Item>
          <Tab.Item title="Monitor" key="monitor">{monitor} </Tab.Item>
        </Tab>
      </IceContainer>
    </div>
  );
}