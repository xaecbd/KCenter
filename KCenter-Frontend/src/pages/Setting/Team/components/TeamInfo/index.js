import React, { Component } from 'react';
import { Dialog, Grid, Input, Message } from '@alifd/next';
import axios from '@utils/axios';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';

const { Row, Col } = Grid;

export default class TeamInfo extends Component {
  static displayName = 'TeamInfo';

  constructor(props) {
    super(props);
    this.state = {
      title: this.props.title,
      teamInfoData: this.props.teamInfo,
      visible: this.props.visible,
    };
  }

  onOk = () => {
    this.form.validateAll((error) => {
      if (error) {
        return;
      }
      axios.post('/team/upsert', this.state.teamInfoData).then((response) => {
        if (response.data.code === 200) {
          this.props.fetchData();
        } else {
          Message.error(response.data.message);
        }
      }).catch((error1) => {
        console.error(error1);
      });
    });
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      title: nextProps.title,
      teamInfoData: nextProps.teamInfo,
      visible: nextProps.visible,
    });
  }

  hideTeamInfoDialog = () => {
    this.props.hideTeamInfoDialog();
  }

  formChange = (value) => {
    this.setState({
      teamInfoData: value,
    });
  };

  checkbland = (rule, values, callback) => {
    if (!values) {
      callback('required');
    } else if (values.trim() === '') {
      callback('please input a vaild value');
    } else {
      callback();
    }
  };

  render() {
    const okProps = { children: 'OK' };
    const cancelProps = { children: 'Cancel' };

    return (
      <Dialog
        className="simple-form-dialog"
        onOk={this.onOk}
        onCancel={this.hideTeamInfoDialog}
        onClose={this.hideTeamInfoDialog}
        visible={this.state.visible}
        isFullScreen
        style={styles.simpleFormDialog}
        okProps={okProps}
        cancelProps={cancelProps}
      >
        <IceFormBinderWrapper
          value={this.state.teamInfoData}
          onChange={this.formChange}
          ref={(form) => {
            this.form = form;
          }}
        >
          <div style={styles.formContent}>
            <h2 style={styles.formTitle}>{this.state.title}</h2>
            <Row style={styles.formRow}>
              <Col span="6">
                <label style={styles.formLabel}>Team Name: </label>
              </Col>
              <Col span="18">
                <IceFormBinder name="name" required validator={this.checkbland}>
                  <Input
                    size="medium"
                    placeholder="team name"
                    style={styles.input}
                  />
                </IceFormBinder>
                <IceFormError name="name" />
              </Col>
            </Row>
            <Row style={styles.formRow}>
              <Col span="6">
                <label style={styles.formLabel}>Alarm Group: </label>
              </Col>
              <Col span="18">
                <IceFormBinder name="alarmGroup" required validator={this.checkbland}>
                  <Input
                    size="medium"
                    placeholder="alarm group"
                    style={styles.input}
                  />
                </IceFormBinder>
                <IceFormError name="alarmGroup" />
              </Col>
            </Row>
          </div>
        </IceFormBinderWrapper>
      </Dialog>
    );
  }
}

const styles = {
  formContent: {
    width: '100%',
    position: 'relative',
  },
  formTitle: {
    margin: '0 0 20px',
    paddingBottom: '10px',
    borderBottom: '1px solid #eee',
  },
  simpleFormDialog: { width: '500px' },
  formRow: { marginTop: 20 },
  input: { width: '100%' },
  formLabel: { lineHeight: '26px' },
};
