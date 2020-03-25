/* eslint react/no-string-refs:0 */
import React, { Component } from 'react';
import Login from './components/UserLogin';

class UserLogin extends Component {
  static displayName = 'UserLogin';

  render() {
    return (
      <div >
        <Login />

      </div>
    );
  }
}


export default UserLogin;
