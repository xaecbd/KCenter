import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import TopicDetail from './components/TopicDetail';


export default class Detail extends Component {
  render() {
    return (
      <div>
        <IceContainer style={styles.container}>
          <TopicDetail />
        </IceContainer>
      </div>
    );
  }
}
const styles = {
  container: {
    margin: '20px',
    padding: '10px 20px 20px',
  },
};
