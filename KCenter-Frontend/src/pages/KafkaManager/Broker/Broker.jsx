import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import BrokerList from './components/Broker';


export default class Broker extends Component {
  constructor(props){
    super(props);
  }

  
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Kafka Manager',
      },
      {
        link: '',
        text: 'Broker',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="Broker" />
        <IceContainer style={styles.container}>
          <BrokerList />
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
