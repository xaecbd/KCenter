
import React, { useEffect, useState } from 'react';
import { Card } from '@alifd/next';
import { Chart, Geom, Axis, Tooltip, Legend } from 'bizcharts';
import classNames from 'classnames';
import cloneDeep from 'lodash.clonedeep';
import { thousandSplit, bytesToSize,sort } from '@utils/dataFormat';
import styles from './index.module.scss';




function LineChart(props){
  
  const [chartData,setChartData] = useState(cloneDeep(props.data));

  const [title,setTitle] = useState(props.title);
  const  [chartHeight,setChartHeight] = useState(props.chartHeight);
  


  const formatScales={
    value: {
      formatter(value) {
        if(props.format){
          return thousandSplit(value);
        }else{
          return `${bytesToSize(value)}`;
        }       
      },
    },
    date: {
      type: 'time',
      mask: 'MM-DD',
      tickCount: 8,
    }
          
  };

 
 

  useEffect(()=>{
    const valueData = sort(props.data,'value','desc');
    setChartData(valueData);
  },[props.data,props.format]);

  return (
    <Card free>
      <React.Fragment>
        <Card.Header title={<span className={styles.title}>{title}</span>}  />
        <Card.Divider />
      </React.Fragment>
      <Card.Content>
        <Chart data={chartData} scale={formatScales} height={chartHeight} width={200} forceFit padding={[10, 20, 30, 65]}>
          <Axis name="date" />
          <Axis
            name="value"
          />
          <Legend name="topic" useHtml flipPage position="top-center"  
            g2-legend = {{
              top: '-30px !important',
              height: '35px',
              lineHeight:'25px',
              paddingLeft:'5px'
            }}
            g2-slip = {{
              lineHeight:'25px',
              top: '-30px !important',
            }}
   
          />
         
          <Tooltip  crosshairs={{ type: 'y' }}
            itemTpl='<tr class="g2-tooltip-list-item"><td style="color:{color}">{name}</td><td>{value}</td></tr>'
            g2-tooltip={{
              position: 'absolute',
              border : '1px solid #efefef',
              backgroundColor: 'white',
              color: '#000',
              opacity: '0.8',
              fontSize:'5px'
            }}
            offset={350}
          /> 
          <Geom
            type="line"
            position="date*value"
            size={2}
            color="topic"
          />
          <Geom
            type="point"
            position="date*value"
            size={2}
            color="topic"
          />
        </Chart>
      </Card.Content>
    </Card>
  );

}

export default LineChart;