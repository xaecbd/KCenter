import React, { Component } from 'react';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import IceContainer from '@icedesign/container';

import FavoriteList from './components/FavoriteList';

export default class Favorite extends Component {
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Monitor',
      },
      {
        link: '',
        text: 'My Favorite',
      },
    ];
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="My Favorite" />
        <IceContainer style={styles.container} >
          <FavoriteList />
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
