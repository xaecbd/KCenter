import React, { Component } from 'react';
import FoundationSymbol from '@icedesign/foundation-symbol';
import { Tab } from '@alifd/next';
import ConsumerLagChart from './ConsumerLag';
import ProducerConsumerLagChart from './ProducerLag';


export default class ConsumerChart extends Component {
  static propTypes = {};
  static defaultProps = {};

  constructor(props) {
    super(props);
    this.state = {};
  }

  backward = () => {
    if (this.props.history.length > 1) {
      this.props.history.goBack();
    } else {
      // group: this.props.match.params.group,
      // topic: this.props.match.params.topic,
      // clusterID: this.props.match.params.id,
      //  this.props.history.push(`/monitor/topic/consumer_offset/${this.props.match.params.id}/${this.props.match.params.topic}`);
      window.location.href = `#/monitor/consumer/topic/consumer_offsets/${this.props.match.params.id}/${this.props.match.params.clusterName}/${this.props.match.params.topic}`;
    }
  }

  render() {
    return (
      <div style={{ minHeight: '600px' }}>
        <div style={styles.listTitle}><FoundationSymbol onClick={() => this.backward()} style={styles.backward} size="large" type="backward" />Chart</div>
        <Tab>
          <Tab.Item title="ConsumerLag">
            <ConsumerLagChart />
          </Tab.Item>
          <Tab.Item title="ProducerConsumerLag">
            <ProducerConsumerLagChart />
          </Tab.Item>
        </Tab>
      </div>
    );
  }
}
const styles = {
  listTitle: {
    margin: '0 7.5px',
    padding: '10px',
    fontSize: '30px',
    fontWeight: 'bold',
  },
  backward: {
    display: 'inline-block',
    minWidth: '40px',
    marginBottom: '15px',
    cursor: 'pointer',
    color: '#0066FF',
  },
};
