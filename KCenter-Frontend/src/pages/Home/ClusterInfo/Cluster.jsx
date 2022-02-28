import React, { Component } from 'react';
import ClusterInfo from './components/ClusterInfo';

export default class Cluster extends Component {
  render() {
    return (
      <div>
        <div style={styles.container} >
          <ClusterInfo />
        </div>
      </div>
    );
  }
}

const styles = {
  container: {
    // padding: '10px 20px 20px',
    minHeight: '600px',
  },
};
