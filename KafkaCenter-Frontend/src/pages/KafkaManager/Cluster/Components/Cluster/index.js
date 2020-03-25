import React, { Component } from 'react';
import { Icon, Grid, Loading, Dialog, Message, Input, Switch, Select, Tag, Button } from '@alifd/next';
import FoundationSymbol from '@icedesign/foundation-symbol';
import axios from '@utils/axios';
import { resturctData } from '@utils/dataFormat';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import IceContainer from '@icedesign/container';
import MetricMonitor from '../../../../Home/MonitorDetail/components/MonitorMetric';

import './Cluster.scss';

const { Row, Col } = Grid;
export default class Cluster extends Component {
  state = {
    data: [],
    isLoading: false,
    visable: false,
    title: '',
    formValue: {},
    isMobile: false,
    endValues: {},
    locations: [],
    isClusterLoading: false,
    metricVisable: false,
    monitorVisable: false,
    clusterObj: {},
  };

  componentWillMount() {
    this.mounted = true;
    this.fetchData();
    this.fetchLocations();
  }
  componentWillUnmount = () => {
    this.mounted = false;
  }

  componentDidMount() {

  }

  handleAdd = () => {
    this.setState({
      visable: !this.state.visable,
      title: 'Add',
      formValue: {},
    });
  };

  onCancel = () => {
    const data = this.state.endValues;
    this.setState({
      visable: !this.state.visable,
      formValue: data,
    }, () => {
    });
  }

  handleEdit = (e, item) => {
    const items = Object.assign({}, item);
    this.setState({
      formValue: items,
      title: 'Edit',
      visable: !this.state.visable,
      endValues: items,
    });
  };

  handleMetricMonitor = (e, item) => {
    this.setState({
      metricVisable: !this.state.metricVisable,
      clusterObj: item,
    });
  }

  handleMonitor = (e, item) => {
    this.setState({
      monitorVisable: !this.state.monitorVisable,
      clusterObj: item,
    });
  }

  backward = () => {
    this.setState({
      metricVisable: !this.state.metricVisable,

    });
  }
  metricMonitor = () => {
    return (
      <IceContainer>
        <h3> <FoundationSymbol
          onClick={() => this.backward()}
          style={styles.backward}
          size="large"
          type="backward"
        />{this.state.clusterObj.name}
        </h3><hr />
        <MetricMonitor id={this.state.clusterObj.id} />
      </IceContainer>
    );
  }

  monitorView = (e, item) => {
    window.open(item.grafAddr, '_blank');
  }

  handleDel = (e, item) => {
    const contenText = <div><p style={{ fontFamily: 'sans-serif', fontSize: '16px', fontWeight: '600' }}>Do you want to delete?</p><p style={{ fontSize: '13px', fontFamily: 'initial' }}>If you are sure, We will delete the data for all the modules associated with the cluster, such as the Task, Topic, Alert module </p></div>;
    Dialog.confirm({
      content: contenText,
      okProps: { children: 'OK' },
      cancelProps: { children: 'Cancel' },
      onOk: () => {
        axios.delete(`/cluster/${item.id}`).then((response) => {
          if (response.data.code === 200) {
            Message.success(response.data.message);
            this.fetchData();
          } else {
            Message.error(response.data.message);
          }
        }).catch((error) => {
          console.error(error);
        });
      },
    });
  }

  fetchLocations = () => {
    axios.get('/config').then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          this.setState({
            locations: resturctData(response.data.data.remotelocations, false),
          });
        }
      }
    }).catch((e) => {
      console.error(e);
    });
  }

  validateAllFormField = () => {
    this.form.validateAll((errors, values) => {
      if (errors) {
        return;
      }
      if (this.state.title === 'Add') {
        this.setState({
          isClusterLoading: true,
        }, () => {
          axios.post('/cluster/add', values).then((response) => {
            if (response.data.code === 200) {
              Message.success(response.data.message);
              this.fetchData();
              this.handleAdd();
            } else {
              Message.error(response.data.message);
            }
            this.setState({
              isClusterLoading: false,
            });
          }).catch((error) => {
            console.error(error);
          });
        });
      } else {
        this.setState({
          isClusterLoading: true,
        }, () => {
          axios.put('/cluster/update', values).then((response) => {
            if (response.data.code === 200) {
              Message.success(response.data.message);
              this.onCancel();
              this.fetchData();
            } else {
              Message.error(response.data.message);
            }
            this.setState({
              isClusterLoading: false,
            });
          }).catch((error) => {
            console.error(error);
          });
        });
      }
    });
  };

  fetchData = () => {
    this.setState(
      {
        isLoading: true,
      },
      () => {
        axios.get('/cluster').then((response) => {
          if (response.data.code === 200) {
            if (this.mounted) {
              this.setState({
                data: response.data.data,
                isLoading: false,
              });
            }
          } else {
            Message.error(response.data.message);
          }
        }).catch((error) => {
          console.error(error);
        });
      }
    );
  };

  onFormChange = (value) => {
    this.setState({
      formValue: value,
    });
  };

  checkKafkaBroker = (rule, values, callback) => {
    let isHealth = false;
    if (!values) {
      callback('required');
    } else if (values.trim() === '') {
      callback('please input a vaild value');
    } else {
      axios.get(`/cluster/validateKafkaAddress?kafkaAddress=${values}`).then((response) => {
        if (response.data.code === 200) {
          isHealth = response.data.data;
          if (isHealth) {
            callback();
          } else {
            callback('kafkaAddress Inactive ');
          }
        } else {
          callback('kafkaAddress Inactive ');
        }
      });
    }
  }

  checkbland = (rule, values, callback) => {
    if (!values) {
      callback('required');
    } else if (values.trim() === '') {
      callback('please input a vaild value');
    } else {
      callback();
    }
  }

  checkBrokerSize = (rule, values, callback) => {
    if (this.state.formValue.enable) {
      if (!values) {
        callback('required');
      } else if (values.toString().trim() === '') {
        callback('please input a vaild value');
      } else {
        callback();
      }
    } else {
      callback();
    }
  }


  checkZK = (rule, values, callback) => {
    let isHealth = false;
    if (!values) {
      callback('required');
    } else if (values.indexOf('/') != -1) {
      const end = values.substring(values.indexOf('/'), values.length);
      if (end.indexOf(',') != -1) {
        callback('Path location must be in the last server');
      } else {
        axios.get(`/cluster/validateZKAddress?zkAddress=${values}`).then((response) => {
          if (response.data.code === 200) {
            isHealth = response.data.data;
            if (isHealth) {
              callback();
            } else {
              callback('zkAddress Inactive ');
            }
          } else {
            callback('zkAddress Inactive ');
          }
        });
      }
    } else {
      axios.get(`/cluster/validateZKAddress?zkAddress=${values}`).then((response) => {
        if (response.data.code === 200) {
          isHealth = response.data.data;
          if (isHealth) {
            callback();
          } else {
            callback('zkAddress Inactive ');
          }
        } else {
          callback('zkAddress Inactive ');
        }
      });
    }
  };

  clusterView = () => {
    return (
      <Loading
        visible={this.state.isLoading}
        style={styles.loading}
      >
        <Row wrap gutter="20">
          <Col l="8" xs="48" xxs="48" onClick={this.handleAdd}>
            <div style={{ ...styles.card, ...styles.createScheme }}>
              <Icon type="add" style={styles.addIcon} />
              <span>Add New Cluster</span>
            </div>
          </Col>
          {this.state.data.map((item, index) => {
            //    console.log('items:'+JSON.stringify(item));
            const items = item;
            return (
              <Col l="8" xs="48" xxs="48" key={index}>
                <div style={styles.card}>
                  <div style={styles.head}>
                    <h4 style={styles.title}>{item.name}</h4>
                    {/* <Icon type="ashbin" size="xs" style={styles.deleteIcon} onClick={(e) => { this.handleDel(e, item); }} /> */}
                  </div>
                  <div style={styles.body}>
                    <Row wrap gutter="20" style={styles.formItems}>
                      <Col l="5" xs="5" xxs="6" className="test">
                        <span>ZK:</span>
                      </Col>
                      <Col l="18" xs="12" xxs="24" className="test1">
                        <span title={item.zkAddress}> <Tag className="tags" size="small">{item.zkAddress}</Tag></span>
                      </Col>
                    </Row>
                    <Row wrap gutter="20" style={styles.formItems}>
                      <Col l="5" xs="5" xxs="6" className="test">
                        <span>Broker:</span>
                      </Col>
                      <Col l="18" xs="12" xxs="24" className="test1">
                        <span title={item.broker}> <Tag className="tags" size="small">{item.broker}</Tag></span>
                      </Col>
                    </Row>
                    <Row wrap gutter="20" style={styles.formItems}>
                      <Col l="5" xs="5" xxs="6" className="test">
                        <span>Kafka Version:</span>
                      </Col>
                      <Col l="18" xs="12" xxs="24" className="test1" >
                        <span title={item.kafkaVersion}> <Tag className="tags" size="small">{item.kafkaVersion}</Tag></span>
                      </Col>
                    </Row>
                    <Row wrap gutter="20" style={styles.formItems}>
                      <Col l="5" xs="5" xxs="6" className="test">
                        <span>Broker Size:</span>
                      </Col>
                      <Col l="18" xs="12" xxs="24" className="test1" >
                        <span title={item.brokerSize}> <Tag className="tags" size="small">{item.brokerSize}</Tag></span>
                      </Col>
                    </Row>
                    <Row wrap gutter="20" style={styles.formItems}>
                      <Col l="5" xs="5" xxs="6" className="test">
                        <span>Location:</span>
                      </Col>
                      <Col l="18" xs="12" xxs="24" className="test1" >
                        <span title={item.location}> <Tag className="tags" size="small">{item.location}</Tag></span>
                      </Col>
                    </Row>


                    {/* <div>ZK:<span title={item.zkAddress}><Tag className="tags" size="small">{item.zkAddress}</Tag></span></div>
                                        <div style={{ marginTop: '1.5%' }}>Broker:<span title={item.broker}><Tag className="tags" size="small" >{item.broker}</Tag></span></div>
                                        <div style={{ marginTop: '1.5%' }}>Kafka Version:<span title={item.kafkaVersion}><Tag className="tags" size="small" >{item.kafkaVersion}</Tag></span></div>
                                        <div style={{ marginTop: '1.5%' }}>Broker Size:<span title={item.brokerSize}><Tag className="tags" size="small" >{item.brokerSize}</Tag></span></div>
                                        <div style={{ marginTop: '1.5%' }}>Location:<span title={item.location}><Tag className="tags" size="small" >{item.location}</Tag></span></div> */}
                    <Row wrap gutter="20" style={styles.formItemes} >
                      <button className="btn warnbtn" disabled={!!((item.grafAddr === null || item.grafAddr === ''))} onClick={(e) => { this.monitorView(e, item); }}><FoundationSymbol
                        title="Grafana Monitor"
                        type="eye"
                        size="small"
                        className="icon"
                      />
                      </button>
                      <button className="btn warnbtn"><FoundationSymbol
                        title=" Metric Monitor"
                        type="chart"
                        size="small"
                        className="icon"
                        onClick={(e) => { this.handleMetricMonitor(e, item); }}
                      />
                      </button>
                      <button className="btn pribtn"><FoundationSymbol
                        title="Edit"
                        type="edit2"
                        size="small"
                        className="icon"
                        onClick={(e) => { this.handleEdit(e, this.state.data[index]); }}
                      />
                      </button>
                      <button className="btn deletBtn"><FoundationSymbol
                        title="Delete"
                        type="delete"
                        size="small"
                        className="icon"
                        onClick={(e) => { this.handleDel(e, item); }}
                      />
                      </button>
                    </Row>
                  </div>
                </div>
              </Col>
            );
          })}
        </Row>
      </Loading>
    );
  }

  clusterDialog = () => {
    const { isMobile } = this.state;
    // 响应式处理
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };
    if (isMobile) {
      simpleFormDialog.width = '300px';
    }
    const okProps = { children: 'OK' };
    const cancelProps = { children: 'Cancel' };
    return (
      <Loading
        visible={this.state.isClusterLoading}
        style={styles.loading}
        fullScreen
      >
        <Dialog
          visible={this.state.visable}
          className="simple-form-dialog"
          onOk={this.validateAllFormField}
          onCancel={this.onCancel}
          footerAlign="center"
          autoFocus={false}
          isFullScreen
          onClose={this.onCancel}
          style={simpleFormDialog}
          title={this.state.title}
          okProps={okProps}
          cancelProps={cancelProps}
        >

          <IceFormBinderWrapper ref={(form) => {
            this.form = form;
          }}
            value={this.state.formValue}
            onChange={this.onFormChange}
          >

            <div style={styles.formContent}>
              <Row style={styles.formItem} >
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                  ClusterName:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="name" required validator={this.checkbland}>
                    <Input
                      style={styles.inputItem}
                      placeholder="cluster name"
                    />
                  </IceFormBinder>
                  <IceFormError name="name" />
                </Col>
              </Row>
              <Row style={styles.formItem}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                  ZK:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="zkAddress" required triggerType="onBlur" validator={this.checkZK}>
                    <Input
                      style={styles.inputItem}
                      placeholder="ip+port eg:127.0.0.1:8181"
                    />
                  </IceFormBinder>
                  <IceFormError name="zkAddress" />
                </Col>
              </Row>

              <Row style={styles.formItem}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                  Broker:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="broker" required triggerType="onBlur" validator={this.checkKafkaBroker}>
                    <Input
                      style={styles.inputItem}

                      placeholder="ip+port eg:127.0.0.1:9092"
                    />
                  </IceFormBinder>
                  <IceFormError name="broker" />
                </Col>
              </Row>

              <Row style={styles.formItem}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                  Location:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="location" required validator={this.checkbland}>
                    <Select dataSource={this.state.locations} defaultValue={this.state.formValue.location} />

                  </IceFormBinder>
                  <IceFormError name="location" />
                </Col>
              </Row>

              <Row style={styles.formItem}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                  Kafka Version:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="kafkaVersion" required validator={this.checkbland}>
                    <Input
                      style={styles.inputItem}

                      placeholder="please input kafka version"
                    />
                  </IceFormBinder>
                  <IceFormError name="kafkaVersion" />
                </Col>
              </Row>
              <Row style={styles.formItem}>
                <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                  Advance:
                </Col>
                <Col span={`${isMobile ? '18' : '16'}`}>
                  <IceFormBinder name="enable">
                    <Switch defaultChecked={this.state.formValue.enable} checked={this.state.formValue.enable} />
                  </IceFormBinder>
                  <IceFormError name="enable" />
                </Col>
              </Row>

              {
                this.state.formValue.enable ? <div><Row style={styles.formItem}>
                  <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                    Broker Size:
                  </Col>
                  <Col span={`${isMobile ? '18' : '16'}`}>
                    <IceFormBinder name="brokerSize" validator={this.checkBrokerSize}>
                      <Input
                        style={styles.inputItem}

                        placeholder="please input broker count"
                      />
                    </IceFormBinder>
                    <IceFormError name="brokerSize" />
                  </Col>
                </Row>
                  <Row style={styles.formItem}>
                    <Col span={`${isMobile ? '6' : '6'}`} style={styles.label}>
                      Grafana Address:
                    </Col>
                    <Col span={`${isMobile ? '18' : '16'}`}>
                      <IceFormBinder name="grafAddr">
                        <Input
                          style={styles.inputItem}
                          placeholder="please input grafana alert address"
                        />
                      </IceFormBinder>
                      <IceFormError name="grafAddr" />
                    </Col>
                  </Row>
                </div> : null
              }
            </div>

          </IceFormBinderWrapper>

        </Dialog>
      </Loading>
    );
  }


  render() {
    let result;
    if (this.state.metricVisable) {
      result = this.metricMonitor();
    } else {
      result = this.clusterView();
    }
    //  document.getElementsByName('framses').contentWindow.document.getElementsByTagName('sidemenu').removeTags();

    return (
      <div style={styles.container}>
        {result}
        {this.clusterDialog()}
        <div id="mon" />
      </div>

    );
  }
}

const styles = {
  tags: {
    borderColor: '#d9ecff !important',
    backgroundColor: '#ecf5ff !important',
    color: '#409eff !important',
  },
  container: {
    margin: '20px',
    height: '100%',
    minHeight: '600px',
  },
  loading: {
    width: '100%',
    minHeight: '500px',
  },
  createScheme: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '250px',
    cursor: 'pointer',
  },
  card: {
    displayName: 'flex',
    marginBottom: '20px',
    background: '#fff',
    borderRadius: '6px',
    height: '250px',
  },
  head: {
    position: 'relative',
    padding: '16px 16px 8px',
    borderBottom: '1px solid #e9e9e9',
  },
  title: {
    margin: '0 0 5px',
    width: '90%',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
    fontSize: '16px',
    fontWeight: '500',
    color: 'rgba(0,0,0,.85)',
  },
  body: {
    position: 'relative',
    padding: '16px',
  },
  addIcon: {
    marginRight: '10px',
  },
  editIcon: {
    // right: '10px',
    // bottom: '0',
    // cursor: 'pointer',
    // display: 'inline-table',
    // borderRadius:'50%'
  },
  formContent: {
    alignItems: 'center',
  },

  formItems: {
    alignItems: 'center',
    // position: 'relative',
    marginTop: 0,
  },
  formItemes: {
    alignItems: 'center',
    float: 'right',
    marginTop: 0,
  },
  formItem: {
    alignItems: 'center',
    position: 'relative',
    marginTop: 20,
  },
  inputItem: {
    width: '100%',
  },
  simpleFormDialog: { width: '640px' },
  label: {
    textAlign: 'left',
    paddingLeft: '5%',
    lineHeight: '26px',
  },
  deleteIcon: {
    position: 'absolute',
    right: '5px',
    cursor: 'pointer',
    top: '0',
  },
  backward: {
    display: 'inline-block',
    minWidth: '40px',
    marginBottom: '15px',
    cursor: 'pointer',
    color: '#0066FF',
  },
  iframe: {
    border: 'none',
    overflow: 'hidden',
    //  height:'1800px',
  },

};

