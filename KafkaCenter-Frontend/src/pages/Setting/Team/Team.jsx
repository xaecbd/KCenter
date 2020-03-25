import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import TeamTable from './components/TeamList';
import TeamUserInfo from './components/UserInfo';

export default class Team extends Component {
  state = {
    teamUserShow: false,
    teamInfoData: {},
  };

  showTeamUserInfo = (teamInfo) => {
    this.setState({
      teamInfoData: teamInfo,
      teamUserShow: true,
    });
  };

  transform = () => {
    this.setState({
      teamUserShow: !this.state.teamUserShow,
    });
  }
  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Setting',
      },
      {
        link: 'javascript:window.location.reload();',
        text: 'Team',
      },
    ];
    const showPage = this.state.teamUserShow;
    if (showPage) {
      breadcrumb.push({ link: '', text: 'User' });
    }
    return (
      <div>
        <CustomBreadcrumb items={breadcrumb} title="Team" />
        <IceContainer style={styles.container}>
          {showPage ? (
            <TeamUserInfo teamInfoData={this.state.teamInfoData} teamUserShow={this.transform} />
          ) : (
            <TeamTable showTeamUserInfo={this.showTeamUserInfo} />
          )}
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
