import React, { Component } from 'react';
import IcePanel from '@icedesign/panel';
import { Grid, Input, Button, Message, Loading } from '@alifd/next';
import FoundationSymbol from '@icedesign/foundation-symbol';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';

import axios from '@utils/axios';

const { Row, Col } = Grid;

const producer = {
  key: '',
  value: '',
};

export default class Producer extends Component {
  static displayName = 'Producer';
  constructor(props) {
    super(props);
    this.state = {
      queriesRecord: this.props.record,
      producer,
      producerResult: '',
      visible: false,
    };
  }
  onFormChange = (value) => {
    this.setState({
      producer: value,
    });
  };

  onReset = () => {
    this.setState({
      producer: {
        key: '',
        value: '',
      },
    });
  }

  onSend = () => {
    this.refForm.validateFields((errors) => {
      if (errors) {
        // show validate error
        return;
      }

      this.setState({
        visible: true,
      });
      const postData = this.state.producer;
      postData.clusterID = this.state.queriesRecord.cluster.id;
      postData.topicName = this.state.queriesRecord.topicName;
      axios.post('/topic/list/producer', postData).then((response) => {
        if (response.data.code === 200) {
          this.setState({
            producerResult: response.data.message,
            visible: false,
          });
        } else {
          Message.error({ content: response.data.message, duration: 5000 });
          this.setState({
            visible: false,
          });
        }
      }).catch((e) => {
        console.error(e);
        this.setState({
          visible: false,
        });
      });
    });
  };

  // 接受父类props改变，修改子类中的属性
  componentWillReceiveProps(nextProps) {
    this.setState({
      queriesRecord: nextProps.record,
    });
  }

  componentWillMount() {
    this.setState({
      producer: {
        key: '',
        value: '',
      },
    });
  }

  checkKey = (rule, values, callback) => {
    if (values.trim() === '') {
      callback('Key is required');
    } else {
      callback();
    }
  };

  checkValue = (rule, values, callback) => {
    if (values.trim() === '') {
      callback('Value is required');
    } else {
      callback();
    }
  };

  render() {
    return (
      <Loading tip="Query..." visible={this.state.visible} fullScreen shape="fusion-reactor">
        <Row align="top">
        <Col span="12" style={{ height: '650px', lineHeight: '50px' }}>
            <IcePanel style={styles.panel}>
              <IcePanel.Body>
                <IceFormBinderWrapper
                  ref={(ref) => {
                      this.refForm = ref;
                    }}
                  value={this.state.producer}
                  onChange={this.onFormChange}
                >
                  <div>
                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Kafka Cluster:</label>
                      </Col>
                      <Col span="16">
                        {this.state.queriesRecord.cluster == null ? '' : this.state.queriesRecord.cluster.name}
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Topic Name:</label>
                      </Col>
                      <Col span="16">
                        {this.state.queriesRecord.topicName}
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Key:</label>
                      </Col>
                      <Col span="16">
                        <IceFormBinder
                          name="key"
                          required
                          message="required"
                          validator={this.checkKey}
                        >
                          <Input
                            style={styles.input}
                            placeholder="Enter key"
                          />
                        </IceFormBinder>
                        <IceFormError name="key" />
                      </Col>
                    </Row>

                    <Row style={styles.formRow}>
                      <Col span="8">
                        <label style={styles.formLabel}>Value:</label>
                      </Col>
                      <Col span="16">
                        <IceFormBinder name="value" required message="required" validator={this.checkValue}>
                          <Input.TextArea
                            style={styles.input}
                            placeholder="Enter value"
                            rows={15}
                          />
                        </IceFormBinder>
                        <IceFormError name="value" />
                      </Col>
                    </Row>

                    <Row style={styles.formRow} />
                    <hr />
                    <Row style={styles.formRow}>
                      <Col span="24" style={{ textAlign: 'center' }}>
                        <Button type="primary" size="large" onClick={this.onSend}><FoundationSymbol type="forward" /> Send</Button>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <Button type="normal" size="large" onClick={this.onReset}><FoundationSymbol type="exchange" /> Reset</Button>
                      </Col>
                    </Row>
                  </div>
                </IceFormBinderWrapper>
              </IcePanel.Body>
            </IcePanel>
          </Col>
          <Col span="12" style={{ height: '650px', fontSize: '10px',lineHeight: '20px' }}>
            <IcePanel style={styles.panelConsole}>
              <IcePanel.Body>
                {this.state.producerResult}
              </IcePanel.Body>
            </IcePanel>
          </Col>
        </Row>
      </Loading>
    );
  }
}

const styles = {
  panelConsole: {
    height: '650px',
    backgroundColor: '#002b36',
    overflowY: 'scroll',
    color: 'greenyellow',
  },
  panel: {
    height: '550px',
    marginBottom: '10px',
  },
  formRow: { marginTop: 20 },
  formLabel: { lineHeight: '26px', fontWeight: '700', textAlign: 'right' },
};
