import React from 'react';
import { Card } from '@alifd/next';
import { Chart, Geom, Axis, Tooltip, Legend } from 'bizcharts';
import classNames from 'classnames';
import styles from './index.module.scss';

const DEFAULT_DATA = {
  title: 'JS 错误',
  chartData: [
    {
      date: 1489592630000,
      name: 'rate',
      count: 50,
    },
    {
      date: 1489592630000,
      name: 'fail',
      value: 30,
      count: 50,
    },
    {
      date: 1489592630000,
      name: 'success',
      value: 20,
      count: 50,
    },
    {
      date: 1489592730000,
      name: 'success',
      value: 100,
      count: 150,
    },
    {
      date: 1489592730000,
      name: 'fail',
      value: 50,
      count: 150,
    },
    {
      date: 1489592730000,
      name: 'rate',
      count: 150,
    },
    {
      date: 1489592830000,
      name: 'success',
      value: 200,
      count: 210,
    },
    {
      date: 1489592830000,
      name: 'fail',
      value: 10,
      count: 210,
    },
    {
      date: 1489592830000,
      name: 'rate',
      count: 210,
    },
    {
      date: 1489592930000,
      name: 'success',
      value: 13,
      count: 16,
    },
    {
      date: 1489592930000,
      name: 'fail',
      value: 3,
      count: 16,
    },
    {
      date: 1489592930000,
      name: 'rate',
      count: 16,
    },
    {
      date: 1489593030000,
      name: 'success',
      value: 100,
      count: 101,
    },
    {
      date: 1489593030000,
      name: 'fail',
      value: 1,
      count: 101,
    },
    {
      date: 1489593030000,
      name: 'rate',
      count: 101,
    },
    {
      date: 1489593130000,
      name: 'success',
      value: 100,
      count: 101,
    },
    {
      date: 1489593130000,
      name: 'fail',
      value: 1,
      count: 101,
    },
    {
      date: 1489593130000,
      name: 'rate',
      count: 101,
    },
    {
      date: 1489593230000,
      name: 'success',
      value: 100,
      count: 101,
    },
    {
      date: 1489593230000,
      name: 'fail',
      value: 1,
      count: 101,
    },
    {
      date: 1489593230000,
      name: 'rate',
      count: 101,
    },
    {
      date: 1489593330000,
      name: 'success',
      value: 100,
      count: 101,
    },
    {
      date: 1489593330000,
      name: 'fail',
      value: 1,
      count: 101,
    },
    {
      date: 1489593330000,
      name: 'rate',
      count: 101,
    },
    {
      date: 1489593430000,
      name: 'success',
      value: 100,
      count: 101,
    },
    {
      date: 1489593430000,
      name: 'fail',
      value: 1,
      count: 101,
    },
    {
      date: 1489593430000,
      name: 'rate',
      count: 101,
    },
    {
      date: 1489593530000,
      name: 'success',
      value: 100,
      count: 101,
    },
    {
      date: 1489593530000,
      name: 'fail',
      value: 1,
      count: 101,
    },
    {
      date: 1489593530000,
      name: 'rate',
      count: 101,
    },
    {
      date: 1489593630000,
      name: 'success',
      value: 100,
      count: 101,
    },
    {
      date: 1489593630000,
      name: 'fail',
      value: 1,
      count: 101,
    },
    {
      date: 1489593630000,
      name: 'rate',
      count: 101,
    },
    {
      date: 1489593730000,
      name: 'success',
      value: 100,
      count: 101,
    },
    {
      date: 1489593730000,
      name: 'fail',
      value: 1,
      count: 101,
    },
    {
      date: 1489593730000,
      name: 'rate',
      count: 101,
    },
  ],
  chartHeight: 260,
};

const JSErrorChart = props => {
  const { cardConfig = DEFAULT_DATA } = props;
  const { title, chartData, chartHeight } = cardConfig;
  const scale = {
    name: {
      ticks: ['rate', 'success', 'fail'],
    },
    date: {
      type: 'timeCat',
      range: [0.05, 0.95],
      mask: 'HH:mm',
      tickCount: 12,
    },
  };
  return (
    <Card free>
      <React.Fragment>
        <Card.Header title={<span className={styles.title}>{title}</span>} />
        <Card.Divider />
      </React.Fragment>
      <Card.Content>
        <Chart
          data={chartData}
          scale={scale}
          height={chartHeight}
          width={10}
          forceFit
          padding={[30, 55, 30, 65]}
        >
          <Tooltip />
          <Legend
            position="top"
            useHtml
            itemTpl={alias => {
              let name = '';

              switch (alias) {
                case 'rate':
                  name = '成功率';
                  break;

                case 'fail':
                  name = '错误次数';
                  break;

                case 'success':
                  name = '调用成功次数';
                  break;

                default:
                  break;
              }

              return `<li style="padding:10px;"><i class="${classNames(
                styles[`${alias}Icon`],
              )}"></i><span >${name}</span></li>`;
            }}
          />
          <Geom
            type="intervalStack"
            position="date*value"
            adjust={['fail', 'success']}
            color={['name', ['#2B7FFB', '#00D6CB']]}
          />
          <Axis name="date" title={null} />
          <Axis name="value" title={null} />
          <Axis name="count" title={null} visible={false} />
          <Geom type="line" position="date*count" color="#4492E0" shape="smooth" tooltip={false} />
          <Geom
            type="area"
            position="date*count"
            color="l (90) 0:rgba(68,146,224,0.2) 1:rgba(68,146,224,0.02)"
            shape="smooth"
            tooltip={false}
          />
        </Chart>
      </Card.Content>
    </Card>
  );
};

export default JSErrorChart;
