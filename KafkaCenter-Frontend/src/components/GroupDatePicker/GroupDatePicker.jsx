import React, { Component } from 'react';
import { Grid, Select } from '@alifd/next';
import DateRangePicker from 'react-bootstrap-daterangepicker';
import dayjs from 'dayjs';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap-daterangepicker/daterangepicker.css';
import { isNullOrUndefined } from 'util';

const { Row, Col } = Grid;
const Option = Select.Option;

export default class GroupDatePicker extends Component {
  constructor(props) {
    super(props);
    this.state = {
      startTime: this.props.startTime,
      endTime: dayjs().valueOf(),
      ranges: {},
    };
  }

  handleApply = (event, picker) => {
    this.setState({
      startTime: picker.startDate,
      endTime: picker.endDate,
    });
    this.props.onDataChange(picker);
  }

  refreshRanges = () => {
    if (this.mounted) {
      this.setState({
        ranges: {
          'Last 1 Hours': [dayjs(dayjs().subtract(1, 'hour').valueOf()), dayjs()],
          'Last 6 Hours': [dayjs(dayjs().subtract(6, 'hour').valueOf()), dayjs()],
          'Last 24 Hours': [dayjs(dayjs().subtract(24, 'hour').valueOf()), dayjs()],
          'Last 3 Days': [dayjs(dayjs().subtract(2, 'day').valueOf()), dayjs()],
          'Last 7 Days': [dayjs(dayjs().subtract(6, 'day').valueOf()), dayjs()],
        },
      });
    }
  }

  refreshData = () => {
    this.props.refreshData();
  }

  componentWillMount() {
    this.mounted = true;
    this.refreshRanges();
  }

  componentWillUnmount = () => {
    this.mounted = false;
  }
  render() {
    const custom = this.props.custom;
    const record = this.props.record;
    let view = null;
    if (!isNullOrUndefined(record)) {
      view = record;
    }
    return (
      <Row style={!isNullOrUndefined(view) ? styles.row : styles.rows}>
        {view}
        <Col >
          <DateRangePicker timePicker onApply={this.handleApply} onShow={this.refreshRanges} ranges={this.state.ranges}>
            <div>
              <div id="reportrange" className="pull-right"  style={!isNullOrUndefined(view) ? styles.datePicker : styles.datePickers}>
                <i className="ice-icon-stable-large ice-icon-stable ice-icon-stable-clock" />&nbsp;
                <span>{`${dayjs(this.state.startTime).format('YYYY/MM/DD,HH:mm')}-${dayjs(this.state.endTime).format('YYYY/MM/DD,HH:mm')}`}</span> <b className="caret" />
              </div>
            </div>
          </DateRangePicker>
        </Col>
        <Col style={{ marginLeft: '30px' }}>
          {!isNullOrUndefined(custom) ? custom : null}
        </Col>
      </Row>
    );
  }
}

const styles = {
  row: {
    margin: '10px',
  },
  rows: {
    margin: '10px',
    float: 'right',
  },
  label: {
    textAlign: 'right',
    marginRight: '10px',
    fontWeight: 'bold',
  },
  text: {
    whiteSpace: 'nowrap',
  },
  datePicker: {
    background: '#fff',
    cursor: 'pointer',
    padding: '5px 10px',
    border: '1px solid #ccc',
    width: '100%',
  },
  datePickers: {
    background: '#fff',
    cursor: 'pointer',
    padding: '5px 10px',
    border: '1px solid #ccc',
    width: '200%',
  },
  datePicker2: {
    width: '260px',
    height: '36.19px',
  },


};
