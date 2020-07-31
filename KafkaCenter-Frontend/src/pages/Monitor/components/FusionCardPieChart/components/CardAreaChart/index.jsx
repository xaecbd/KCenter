import React from 'react';
import { Card } from '@alifd/next';
import { Chart, Geom } from 'bizcharts';
import mock from './mock.js';
import styles from './index.module.scss';

const DEFAULT_DATA = {
  title: '渲染时间',
  subTitle: '首次渲染时间',
  value: mock.value,
  chartData: mock.saleList,
  cardFoot: `日访问用户数  ${mock.dailySale}`,
  chartHeight: 300,
};

const FusionCardAreaChart = (props = DEFAULT_DATA) => {
  const { title, subTitle, value, chartData, chartHeight } = { ...DEFAULT_DATA, ...props };
  return (
    <Card free className={styles.areaChart}>
      {title ? (
        <React.Fragment>
          <Card.Header title={<span className={styles.title}>{title}</span>} />
          <Card.Divider />
        </React.Fragment>
      ) : null}
      <Card.Content>
        <div className={styles.subTitle}>{subTitle}</div>
        <div className={styles.value}>{value}</div>
        <div>周同比: 10.1%↑</div>
        <Chart
          data={chartData}
          height={chartHeight}
          scale={{
            date: {
              range: [0, 1],
            },
          }}
          width={10}
          forceFit
          padding={['auto', '0']}
        >
          <Geom type="area" position="date*value" color="#2B7FFB" shape="smooth" opacity={1} />
        </Chart>
      </Card.Content>
    </Card>
  );
};

export default FusionCardAreaChart;
