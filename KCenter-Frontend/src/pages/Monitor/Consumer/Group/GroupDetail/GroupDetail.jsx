import React, { Component } from 'react';
import Detail from './Detail';

export default class GroupDetail extends Component {
  render() {
    return (
      <div style={styles.container} >
        <Detail />
      </div>
    );
  }
}

const styles = {
  container: {
  //  margin: '20px',
    padding: '10px 20px 20px',
  },
};
