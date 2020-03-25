import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import UpdateConfig from './components/UpdateConfig';


export default class Topic extends Component {
  render() {
    return (
      <div>
        <IceContainer style={styles.container}>
          <UpdateConfig />
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
