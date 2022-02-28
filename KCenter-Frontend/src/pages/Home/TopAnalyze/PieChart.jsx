import React, { useState, useEffect } from 'react';
import { Card } from '@alifd/next';
import { Chart, Geom, Coord, Axis, Legend,Tooltip,Interval,Label } from 'bizcharts';
import axios from '@utils/axios';
import { bytesToSize } from '@utils/dataFormat';
import DataSet from '@antv/data-set';
import styles from './index.module.scss';

const { DataView } = DataSet;

function PieChart(){

  const [chartData,setChartData] = useState([
  ]);
  const [title,setTitle] = useState('File Size Of Topic');
  const  [chartHeight,setChartHeight] = useState(260);
  const [isLoding,setIsLoading] = useState(false);

  const parseResult = (data) =>{
    const result = [];
    Object.keys(data).forEach(key=>{
      const obj = {
        item:key,
        count:data[key],
      };
      result.push(obj);
    });
    const dv = new DataView();
    dv.source(result).transform({
      type: 'percent',
      field: 'count',
      dimension: 'item',
      as: 'percent'
    });
    setChartData(dv);
  }

  const fetchData = async() =>{
    await axios
      .get('/home/topic/file/size')
      .then((response) => {
        if (response.data.code === 200) {
          if (response.data.data) {
            parseResult(response.data.data);
          }
        }
      })
      .catch((error) => {
        console.error(error);
      });
  }

  

  useEffect(()=>{
    fetchData();
  },[]);

  return ( 
    <Card free>
      <Card.Header title={<span className={styles.title}>{title}</span>} />
      <Card.Divider />
      <Card.Content>
        
        <Chart
          width={10}
          height={230}
          forceFit
          data={chartData}
          padding={['auto', 'auto']}
  
        >
          <Coord type="theta" radius={0.75} innerRadius={0.6} />
          <Axis name="percent" />
          <Legend
            position="right"
          />
          <Tooltip
            showTitle={false}
            itemTpl="<li><span style=&quot;background-color:{color};&quot; class=&quot;g2-tooltip-marker&quot;></span>{name}: {value}</li>"
          />
         
          <Geom
            type="intervalStack"
            position="percent"
            color="item"
            tooltip={[
              'item*percent',
              (item, percent) => {
                return {
                  name: item,
                  value: `${parseFloat(percent*100).toFixed(2)}%`
                };
              }
            ]}
            style={{
              lineWidth: 1,
              stroke: '#fff'
            }}
          >
            <Label
              content="count"
              formatter={(val, item) => {
                return `${item.point.item  }: ${ bytesToSize(val)}`;
              }}
            />
          </Geom>
        </Chart>
      </Card.Content>
    </Card>);

}

export default PieChart;