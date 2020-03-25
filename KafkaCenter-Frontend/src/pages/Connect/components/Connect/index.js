import React, { Component } from 'react';
import { Message } from '@alifd/next';
import axios from '@utils/axios';

export default class Connect extends Component {
    state = {
      url: null,
    };
    componentDidMount() {
      axios.get('/config').then((response) => {
        if (response.data.code === 200) {
          if (this.mounted) {
            this.setState({
              url: response.data.data.connection_url,
            });
          }
        } else {
          Message.error(response.data.message);
        }
      }).catch((error) => {
        console.error(error);
      });
    }

    componentWillMount() {
      this.mounted = true;
    }
      componentWillUnmount = () => {
        this.mounted = false;
      }
      render() {
        return (
        // eslint-disable-next-line jsx-a11y/iframe-has-title
          <iframe src={this.state.url} width="100%" height="90%" style={styles.iframe} />
        );
      }
}

const styles = {
  iframe: {
    border: 'none',
    overflow: 'hidden',
  },
};
