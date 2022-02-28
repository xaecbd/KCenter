
import React, {useState, useEffect,useRef } from 'react';
import { Table, Loading, Button } from '@alifd/next';
import cloneDeep from 'lodash.clonedeep';

import {
  G2,
  Chart,
  Geom,
  Axis,
  Tooltip,
  Coord,
  Label,
  Legend,
  View,
  Guide,
  Shape,
  Facet,
  Util
} from 'bizcharts';

import axios from '@utils/axios';
import { transToNumer, bytesToSize, thousandSplit } from '@utils/dataFormat';
import style from './index.module.scss';

function MetricChart(props){

  const [loading,setLoading] = useState(false);
  const [data,setData] = useState([]);
  const [preconfig,setPreConfig] = useState(cloneDeep(props.config));

  const deepEquals = () =>{
    const oldProps = preconfig;
    const newProps = props.config;
    if(oldProps.clusterId !== newProps.clusterId || oldProps.end !== newProps.end || oldProps.start !== newProps.start || oldProps.topic !== newProps.topic || oldProps.metric !== newProps.metric){
      setPreConfig(cloneDeep(newProps));
      return true;

    }
    return false;
  }

  const fetchData = async() =>{
    setLoading(true);
    await axios
      .get(`/monitor/topic/metric?clusterId=${props.config.clusterId}&topic=${props.config.topic}&start=${props.config.start}&end=${props.config.end}&metric=${props.config.metric}`)
      .then((response) => {
        if (response.data.code === 200) {
          if (response.data.data) {
            setData(response.data.data);
            setLoading(false);
          } else {
            setLoading(false);
          }
        }
      })
      .catch((error) => {
        console.error(error);
      });
  }
 
  useEffect(()=>{
    fetchData();        
  },[props.config]);


  if(deepEquals()){
    fetchData();
  }



  const cols = {
    time: {
      alias: 'time',
      mask: 'MM-DD HH:mm:ss',
    },
    value: {
      formatter(value) {
        if(props.config.metric==='MessagesInPerSec'){
          const trans = thousandSplit(value);
          return trans;
        }else{
          if(value.toString().search('-')!==-1){
            const  values = bytesToSize(value.toString().replace('-',''));
            return `-${values}`;
          }
          return `${bytesToSize(value)}`;
        }        
      },
    },
  };
  

  return(
    <div className={style.metrichart}>
      <Loading visible={loading} style={styles.loading}>
        <Chart height={400} data={data} scale={cols} forceFit>
          <Axis
            name="time"
            line={{
              stroke: '#E6E6E6'
            }}
          />
          <Axis
            name="value"
          />
          <Tooltip />
          <Geom
            type="line"
            position="time*value"
            size={1}
            color="l (270) 0:rgba(255, 146, 255, 1) .5:rgba(100, 268, 255, 1) 1:rgba(215, 0, 255, 1)"
            shape="smooth"
            style={{
              shadowColor: 'l (270) 0:rgba(21, 146, 255, 0)',
              shadowBlur: 60,
              shadowOffsetY: 6
            }}
          />
        </Chart>
      </Loading>
    </div>

  );
}

const styles={
  loading: {
    width: '100%',
  },
};

export default MetricChart;