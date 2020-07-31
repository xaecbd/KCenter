import React from 'react';
import { Card } from '@alifd/next';
import { Chart, Geom, Coord, Axis, Legend } from 'bizcharts';
import styles from './index.module.scss';

const DEFAULT_DATA = {
  title: '用户浏览器占比',
  value: 183112,
  chartData: [
    {
      type: 'chrome',
      value: 40,
      title: 'chrome | 40.00%',
    },
    {
      type: 'IE',
      value: 21,
      title: 'IE | 22.12%',
    },
    {
      type: 'FireFox',
      value: 17,
      title: 'FireFox | 16.59%',
    },
    {
      type: 'safari',
      value: 13,
      title: 'safari | 13.11%',
    },
    {
      type: 'Opera',
      value: 9,
      title: 'Opera |  9.29%',
    },
  ],
  chartHeight: 400,
};

const FusionCardLineChart = props => {
  const { cardConfig = DEFAULT_DATA } = props;
  const { title, chartData, chartHeight } = cardConfig;
  return (
    <Card free>
      <Card.Header title={<span className={styles.title}>{title}</span>} />
      <Card.Divider />
      <Card.Content>
        <Chart width={10} height={chartHeight} forceFit data={chartData} padding={['auto', 'auto']}>
          <Coord type="theta" radius={0.75} innerRadius={0.6} />
          <Axis name="percent" />
          <Legend
            position="right-center"
            textStyle={{
              fill: '#666',
              fontSize: 14,
            }}
            itemMarginBottom={24}
          />
          <Geom
            type="intervalStack"
            position="value"
            color="title"
            style={{
              lineWidth: 1,
              stroke: '#fff',
            }}
          />
        </Chart>
      </Card.Content>
    </Card>
  );
};

export default FusionCardLineChart;
