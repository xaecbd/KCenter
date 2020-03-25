import React, { Component } from 'react';
import { Grid, Message, Input, Button } from '@alifd/next';
import FoundationSymbol from '@icedesign/foundation-symbol';
import { withRouter } from 'react-router-dom';
import {
  FormBinderWrapper as IceFormBinderWrapper,
  FormBinder as IceFormBinder,
  FormError as IceFormError,
} from '@icedesign/form-binder';
import IceContainer from '@icedesign/container';
import axios from '@utils/axios';

const { Row, Col } = Grid;
@withRouter
export default class UpdateConfig extends Component {
    state = {
      formValue: {},
      isMobile: false,
      topicName: this.props.match.params.topic,
      clusterId: this.props.match.params.clusterId,
    };

    componentWillMount() {
      this.mounted = true;
      this.fetchData();
    }

    componentWillUnmount() {
      this.mounted = false;
    }

  fetchData = () => {
    const params = {
      topicName: this.state.topicName,
      clusterId: this.state.clusterId,
    };
    axios.post('/manager/topic/desconfig', params).then((response) => {
      if (response.data.code === 200) {
        if (this.mounted) {
          this.setState({
            formValue: response.data.data,
          });
        }
      }
    }).catch((e) => {
      console.error(e);
    });
  }

  handleUpdate = () => {
    const params = {
      topicName: this.state.topicName,
      clusterId: this.state.clusterId,
      entry: this.state.formValue,
    };
    axios.post('/manager/topic/desconfig/update', params).then((response) => {
      if (response.data.code === 200) {
        Message.success(response.data.message);
        this.handleCancel();
      } else {
        Message.error(response.data.message);
      }
    }).catch((e) => {
      console.error(e);
    });
  }

  checkSymol = (rule, values, callback) => {
    if (values) {
      const pattern = new RegExp("[`~!@%#$^&*()=|{}':;',　\\[\\]<>/? ／＼］\\.；：%……+ ￥（）【】‘”“'．。，、？１２３４５６７８９０－＝＿＋～？！＠＃＄％＾＆＊）]");
      if (pattern.test(values)) {
        callback('Values cannot contain special characters');
      }
    }
    callback();
  }

  checkNum = (rule, values, callback) => {
    if (values) {
      const reg = new RegExp('^[0-9]*$');
      if (!reg.test(values)) {
        callback('The value must be a number');
      }
    }

    callback();
  }

  handleCancel = () => {
    this.props.history.push(`/kafka-manager/topic/${this.state.clusterId}/${this.state.topicName}`);
  }

  render() {
    const { isMobile } = this.state;
    const simpleFormDialog = {
      ...styles.simpleFormDialog,
    };
    if (isMobile) {
      simpleFormDialog.width = '300px';
    }
    return (
      <div >
        <div style={styles.listTitle}>
          <FoundationSymbol
            onClick={() => this.handleCancel()}
            style={styles.backward}
            size="large"
            type="backward"
          />
          {this.state.topicName}
        </div>
        <IceFormBinderWrapper
          ref={(form) => {
            this.form = form;
          }}
          value={this.state.formValue}
          onChange={this.onFormChange}
        >
          <div style={styles.formContent}>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                Topic:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <span style={styles.inputItem}>{this.state.topicName}</span>
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                cleanup.policy:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="cleanup_policy"
                  validator={this.checkSymol}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>A string that is either delete or compact. This string designates the retention policy to use on old log segments. The default policy ("delete")
                  will discard old segments when their retention time or size limit has been reached.
                  The compact  setting will enable log compactionon the topic.
                </label>
                <IceFormError name="cleanup_policy" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                compression.type:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="compression_type"
                  validator={this.checkSymol}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>Specify the final compression type for a given topic.
                  This configuration accepts the standard compression codecs (`gzip`, `snappy`, lz4). It additionally accepts `uncompressed` which is equivalent to no compression;
                  and `producer` which means retain the original compression codec set by the producer.
                </label>
                <IceFormError name="compression_type" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                delete.retention.ms:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="delete_retention_ms"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>The amount of time to retain delete tombstone markers for log compacted
                  topics. This setting also gives a bound on the time in which a consumer must complete a read if they begin from offset 0 to ensure that they get a valid snapshot of the final stage
                  (otherwise delete tombstones may be collected before they complete their scan).
                </label>
                <IceFormError name="delete_retention_ms" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                file.delete.delay.ms:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="file_delete_delay_ms"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>The time to wait before deleting a file from the filesystem</label>
                <IceFormError name="file_delete_delay_ms" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                flush.messages:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="flush_messages"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This setting allows specifying an interval at which we will force an fsync of data written to the log.
                  For example if this was set to 1 we would fsync after every message; if it were 5 we would fsync after every five messages.
                  In general we recommend you not set this and use replication for durability and allow the operating system`s background flush capabilities as it is more efficient.
                  This setting can be overridden on a per-topic basis (see the per-topic configuration section).
                </label>
                <IceFormError name="flush_messages" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                flush.ms:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="flush_ms"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This setting allows specifying a time interval at which we will force an fsync of data written to the log. For example if this was set to
                  1000 we would fsync after 1000 ms had passed.
                  In general we recommend you not set this and use replication for durability and allow the operating system`s background flush capabilities as it is more efficient.
                </label>
                <IceFormError name="flush_ms" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                follower.replication.throttled.replicas:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="follower_replication_throttled_replicas"
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>A list of replicas for which log replication should be throttled on the follower side.
                  The list should describe a set of replicas in the form [PartitionId]:[BrokerId],[PartitionId]:[BrokerId]:... or alternatively the wildcard '*'
                  can be used to throttle all replicas for this topic.
                </label>
                <IceFormError name="follower_replication_throttled_replicas" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                index.interval.bytes：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="index_interval_bytes"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This setting controls how frequently Kafka adds an index entry to it`s offset index. The default setting ensures that we index a message roughly every 4096 bytes.
                  More indexing allows reads to jump closer to the exact position in the log but makes the index larger.
                  You probably don`t need to change this.
                </label>
                <IceFormError name="index_interval_bytes" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                leader.replication.throttled.replicas：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="leader_replication_throttled_replicas"               
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>A list of replicas for which log replication should be throttled on the leader side.
                  The list should describe a set of replicas in the form [PartitionId]:[BrokerId],[PartitionId]:[BrokerId]:... or alternatively the wildcard `*`
                  can be used to throttle all replicas for this topic.
                </label>
                <IceFormError name="leader_replication_throttled_replicas" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                max.message.bytes：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="max_message_bytes"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This is largest message size Kafka will allow to be appended.
                  Note that if you increase this size you must also increase your consumer`s fetch size so they can fetch messages this large..
                </label>
                <IceFormError name="max_message_bytes" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                message.format.version：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="message_format_version"                
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>Specify the message format version the broker will use to append messages to the logs.
                  The value should be a valid ApiVersion. Some examples are: 0.8.2, 0.9.0.0, 0.10.0,
                  check ApiVersion for more details. By setting a particular message format version,
                  the user is certifying that all the existing messages on disk are smaller or equal than the specified version.
                  Setting this value incorrectly will cause consumers with older versions to break as they will receive messages with a format that they don`t understand.
                </label>
                <IceFormError name="message_format_version" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                message.timestamp.difference.max.ms：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="message_timestamp_difference_max_ms"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>The maximum difference allowed between the timestamp when a broker receives a message and the timestamp specified in the message.
                  If message.timestamp.type=CreateTime,
                  a message will be rejected if the difference in timestamp exceeds this threshold. This configuration is ignored if message.timestamp.type=LogAppendTime.
                </label>
                <IceFormError name="message_timestamp_difference_max_ms" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                message.timestamp.type：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="message_timestamp_type"
                  validator={this.checkSymol}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>Define whether the timestamp in the message is message create time or log append time.
                  The value should be either `CreateTime` or `LogAppendTime`
                </label>
                <IceFormError name="message_timestamp_type" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                min.cleanable.dirty.ratio：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="min_cleanable_dirty_ratio"
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This configuration controls how frequently the log compactor will attempt to clean the log
                  (assuming log compaction is enabled). By default we will avoid cleaning a log where more than 50% of the log has been compacted.
                  This ratio bounds the maximum space wasted in the log by duplicates (at 50% at most 50% of the log could be duplicates).
                  A higher ratio will mean fewer, more efficient cleanings but will mean more wasted space in the log.
                </label>
                <IceFormError name="min_cleanable_dirty_ratio" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                min.compaction.lag.ms：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="min_compaction_lag_ms"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>The minimum time a message will remain uncompacted in the log. Only applicable for logs that are being compacted.</label>
                <IceFormError name="min_compaction_lag_ms" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                min.insync.replicas：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="min_insync_replicas"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>When a producer sets acks to `all` (or `-1`), min.insync.replicas specifies the minimum number
                  of replicas that must acknowledge a write for the write to be considered successful. If this minimum cannot be met, then the producer will raise an exception
                  (either NotEnoughReplicas or NotEnoughReplicasAfterAppend).When used together, min.insync.replicas and acks allow you to enforce greater durability guarantees. A typical scenario would be to create a topic with a replication factor of 3,
                  set min.insync.replicas to 2, and produce with acks of `all`. This will ensure that the producer raises an exception if a majority of replicas do not receive a write.
                </label>
                <IceFormError name="min_insync_replicas" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                preallocate：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="preallocate"
                  validator={this.checkSymol}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>Should pre allocate file when create new segment?</label>
                <IceFormError name="preallocate" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                retention.bytes:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="retention_bytes"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This configuration controls the maximum size a log can grow to before we will discard old log segments to free up
                  space if we are using the `delete` retention policy. By default there is no size limit only a time limit.
                </label>
                <IceFormError name="retention_bytes" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                retention.ms:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="retention_ms"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This configuration controls the maximum time we will retain a log
                  before we will discard old log segments to free up space if we are using the `delete` retention policy. This represents an SLA on how soon consumers must read their data.
                </label>
                <IceFormError name="retention_ms" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                segment.bytes:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="segment_bytes"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This configuration controls the segment file size for the log.
                  Retention and cleaning is always done a file at a time so a larger segment size means fewer files but less granular control over retention.
                </label>
                <IceFormError name="segment_bytes" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                segment.index.bytes:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="segment_index_bytes"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This configuration controls the size of the index that maps offsets to file positions.
                  We preallocate this index file and shrink it only after log rolls. You generally should not need to change this setting.
                </label>
                <IceFormError name="segment_index_bytes" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                segment.jitter.ms:
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="segment_jitter_ms"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>The maximum random jitter subtracted from the scheduled segment roll time to avoid thundering herds of segment rolling</label>
                <IceFormError name="segment_jitter_ms" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                segment.ms：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="segment_ms"
                  validator={this.checkNum}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>This configuration controls the period of time after which Kafka will force the log to roll even if the segment file isn`t full to ensure that retention can delete or compact old data.</label>
                <IceFormError name="segment_ms" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col span={`${isMobile ? '7' : '7'}`} style={styles.label}>
                unclean.leader.election.enable：
              </Col>
              <Col span={`${isMobile ? '18' : '16'}`}>
                <IceFormBinder
                  name="unclean_leader_election_enable"
                  validator={this.checkSymol}
                >
                  <Input
                    trim
                    style={styles.inputItem}
                  />
                </IceFormBinder>
                <br /><label style={styles.labelText}>Indicates whether to enable replicas not in the ISR set to be elected as leader as a last resort, even though doing so may result in data loss</label>
                <IceFormError name="unclean_leader_election_enable" />
              </Col>
            </Row>
            <Row style={styles.formItem}>
              <Col style={{ textAlign: 'center' }}>
                <Button type="secondary" style={{ marginRight: '15px' }} onClick={e => this.handleUpdate()}>Update Config</Button>
                <Button type="secondary" onClick={e => this.handleCancel()}>Cancel</Button>
              </Col>
            </Row>
          </div>
        </IceFormBinderWrapper>
      </div>

    );
  }
}
const styles = {
  container: {
    margin: '20px',
  },
  loading: {
    width: '100%',
    minHeight: '500px',
  },
  card: {
    displayName: 'flex',
    marginBottom: '20px',
    background: '#fff',
    borderRadius: '6px',
    height: '190px',
  },
  formContent: {
    alignItems: 'center',
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
  labelText: {
    color: '#6c757d!important',
    fontSize: '80%',
    fontWeight: '400',
    marginTop: '5px',
  },
  backward: {
    display: 'inline-block',
    minWidth: '40px',
    marginBottom: '15px',
    cursor: 'pointer',
    color: '#0066FF',
  },
  listTitle: {
    marginBottom: '10px',
    fontSize: '30px',
    fontWeight: 'bold',
  },
  container: {
    margin: '20px',
    padding: '10px 20px 20px',
  },

};

