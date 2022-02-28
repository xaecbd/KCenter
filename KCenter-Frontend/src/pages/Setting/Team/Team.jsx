import React, { Component } from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import TeamTable from './components/TeamList';
import TeamUserInfo from './components/UserInfo';

const styles = {
  container: {
    margin: '20px',
    padding: '10px 20px 20px',
    minHeight: '600px',
  },
};


export default class Team extends Component {
  
  constructor(){
    super();
    this.state = {
      teamUserShow: false,
      teamInfoData: {},
    };
  }

  showTeamUserInfo = (teamInfo) => {
    this.setState({
      teamInfoData: teamInfo,
      teamUserShow: true,
    });
  };

  transform = () => {
    this.setState(prevState =>({
      teamUserShow: !prevState .teamUserShow,
    }));
  }

  render() {
    const breadcrumb = [
      {
        link: '',
        text: 'Setting',
      },
      {
        link: '',
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


