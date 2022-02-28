import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import { Tab } from '@alifd/next';
import Console from './../Console';
import Streams from './../Streams';
import Tables from './../Tables';
import Queries from './../Queries';

export default class KsqlHome extends Component {
  state={
    streams: <Streams />,
    tables: <Tables />,
    queries: <Queries />,
  }


   onTabChanged = (key) => {
     const url = `/ksql/${this.props.match.params.id}/${this.props.match.params.clusterName}/${this.props.match.params.ksqlServerId}/${key}`;
     this.props.history.push(url);
     const timestamp = new Date().getTime();
     if (key === 'streams') {
       this.setState({ streams: <Streams key={timestamp} /> });
     } else if (key === 'tables') {
       this.setState({ tables: <Tables key={timestamp} /> });
     } else if (key === 'queries') {
       this.setState({ queries: <Queries key={timestamp} /> });
     }
   }
   render() {
     const breadcrumb = [
       {
         link: '',
         text: 'KSQL',
       },
       {
         link: '#/ksql/list',
         text: 'List',
       },
     ];
     return (
       <div>
         <CustomBreadcrumb items={breadcrumb} title="KSQL Home" />
         <IceContainer style={styles.container}>
           <Tab onChange={this.onTabChanged} defaultActiveKey={this.props.match.params.tab}>
             <Tab.Item title="Console" key="console"><Console /> </Tab.Item>
             <Tab.Item title="Streams" key="streams" >{this.state.streams}</Tab.Item>
             <Tab.Item title="Tables" key="tables">{this.state.tables} </Tab.Item>
             <Tab.Item title="Queries" key="queries">{this.state.queries} </Tab.Item>
           </Tab>
         </IceContainer>
       </div>

     );
   }
}

const styles = {
  container: {
    margin: '20px',
    padding: '10px 20px 20px',
    minHeight: '600px',
  },
};
