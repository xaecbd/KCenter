import React, { Component } from 'react';
import { Grid} from '@alifd/next';
import { isNullOrUndefined } from 'util';
const { Row, Col } = Grid;
export default class TableFilter extends Component { 
  render() {
    const view = this.props.components;
    const selecttext = this.props.selecttext;
    const filters = this.props.filters;
    const filterComponents = this.props.filterComponents;
    const clusterCompent = this.props.selectCompont;
    let filetervies = null;
    if (!isNullOrUndefined(clusterCompent) && !isNullOrUndefined(filterComponents)) {
      if (!isNullOrUndefined(view)) {
        filetervies = (<Col align="center"><span style={{ fontWeight:'600'}}>{filters}:&nbsp;&nbsp;&nbsp;</span>{filterComponents}</Col>);
      }else {
        filetervies = (<Col align="center" offset="10" style={{ marginLeft: '30%' }}><span style={{ fontWeight:'600'}}>{filters}:&nbsp;&nbsp;&nbsp;</span>{filterComponents}</Col>);
      }
    } else if (!isNullOrUndefined(filterComponents) && isNullOrUndefined(clusterCompent)) {
      filetervies = (<Col ><span style={{ fontWeight:'600'}}>{filters}:&nbsp;&nbsp;&nbsp;</span>{filterComponents}</Col>);
    }
    return (
      <Row style={styles.row}>
        {!isNullOrUndefined(clusterCompent) ? <Col align="center">
        <span style={{ fontWeight:'600'}}>{selecttext}:&nbsp;&nbsp;&nbsp;</span>
          {clusterCompent}
                                              </Col> : null}
        {filetervies}
        {!isNullOrUndefined(clusterCompent) ? <Col align="center" > {view}</Col> : <Col align="center" offset="13"> {view}</Col>}
      </Row>
    );
  }
}

const styles = {
  row: {
    margin: '4px 0 20px',
  },
};
