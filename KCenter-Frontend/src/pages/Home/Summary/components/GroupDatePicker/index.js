import React, { Component } from 'react';
import { Grid } from '@alifd/next';
import DateRangePicker from 'react-bootstrap-daterangepicker';
import dayjs from 'dayjs';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap-daterangepicker/daterangepicker.css';
import { isNullOrUndefined } from 'util';

const { Row, Col } = Grid;

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
          'Last 3 Days': [dayjs(dayjs().subtract(3, 'day').valueOf()), dayjs()],
          'Last 7 Days': [dayjs(dayjs().subtract(7, 'day').valueOf()), dayjs()],
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

  caseTime = () =>{
      const start = this.state.startTime;
      const end = this.state.endTime;
      const diff = end - start;
      const mills = parseInt(diff/1000);
      if(mills>0){
          const minutes = parseInt(mills/60);
          const hours = parseInt(minutes/60);
          const days = parseInt(hours/24);
          if(hours == 1){
            return 'Last 1 Hours'
          }else if(hours == 6){
            return 'Last 6 Hours'
          }else if(hours == 24){
            return 'Last 24 Hours'
          }else if(days == 3 ){
              return 'Last 3 Days';
          }else if(days==7){
              return 'Last 7 Days';
          }else{
              return '';
          }
    }else{
        return '';
    }
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
        <div>
          <DateRangePicker timePicker onApply={this.handleApply} onShow={this.refreshRanges} ranges={this.state.ranges}>
            <div>
              <div id="reportrange" className="pull-right" style={this.caseTime()==''?styles.datePicker:styles.dateNoPicker}>
                <i className="ice-icon-stable-large ice-icon-stable ice-icon-stable-clock" />&nbsp;
                {
                    this.caseTime()==''? (<span>{`${dayjs(this.state.startTime).format('YYYY/MM/DD,HH:mm')}-${dayjs(this.state.endTime).format('YYYY/MM/DD,HH:mm')}`}</span>):
                    <span>{this.caseTime()}</span> 
                }
               <b className="caret" />
              </div>
            </div>
          </DateRangePicker>
        </div>
        {/* <div style={{ marginLeft: '20px' }}>
          {!isNullOrUndefined(custom) ? custom : null}
        </div> */}
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
  datePicker: {
    background: '#fff',
    cursor: 'pointer',
    padding: '5px 10px',
    border: '1px solid #ccc',
    width: '100%',
  },
  dateNoPicker: {
    background: '#fff',
    cursor: 'pointer',
    padding: '5px 10px',
    border: '1px solid #ccc',
  },
};
