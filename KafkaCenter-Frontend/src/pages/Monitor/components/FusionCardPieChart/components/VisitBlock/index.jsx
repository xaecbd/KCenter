import React from 'react';
import { Card, Box } from '@alifd/next';
import { Chart, Geom, Axis, Tooltip } from 'bizcharts';
import classNames from 'classnames';
import styles from './index.module.scss';

const DEFAULT_DATA = {
  titleItem: [
    {
      name: '总PV',
      value: '234,465789',
      des: '周同比:',
      rate: 10.1,
    },
    {
      name: '总UV',
      value: '234,465789',
      des: '周同比:',
      rate: -10.1,
    },
  ],
  chartData: [
    {
      type: 'pv',
      date: 1489593600000,
      value: 100,
    },
    {
      type: 'pv',
      date: 1489593630000,
      value: 220,
    },
    {
      type: 'pv',
      date: 1489591630000,
      value: 230,
    },
    {
      type: 'uv',
      date: 1489592600000,
      value: 140,
    },
    {
      type: 'uv',
      date: 1489592630000,
      value: 350,
    },
    {
      type: 'uv',
      date: 1489590630000,
      value: 370,
    },
  ],
  chartHeight: 300,
};

const InfoBlock = (props = DEFAULT_DATA.titleItem[0]) => {
  const { name, value, des, rate } = props;
  return (
    <Box className={styles.header} direction="column">
      <div>
        <i className={classNames(styles.rateIcon)} />
        <span className={styles.totle_font}>{name}</span>
      </div>
      <Box align="baseline" direction="row">
        <span className={styles.title}>{value}</span>
        <span className={styles.compare}>
          <span>{des}123</span>
          <span
            style={{
              color: rate > 0 ? '#36CFC9' : '#D23C26',
            }}
          >
            8.1%
            {rate > 0 ? <> ↑ </> : <>↓</>}
          </span>
          {/* <i className={classNames(styles.cocofont, styles.arrow_down)} /></span> */}
        </span>
      </Box>
    </Box>
  );
}; // 两条线pv/uv

const RenderPvChart = (props = DEFAULT_DATA) => {
  const { chartData, chartHeight } = { ...DEFAULT_DATA, ...props };
  const cols = {
    date: {
      type: 'timeCat',
      mask: 'MM/DD HH:mm',
      range: [0.05, 0.95],
    },
    value: {
      min: 0,
    },
  }; // 虚线处理

  const areaColors = [
    'l(100) 0:rgba(253,250,242) 1:rgba(255,245,205)',
    'l(100) 0:rgba(221,246,250) 1:rgba(244,252,253)',
  ];
  const lineColors = ['#FFCE03', '#00C1DE']; // 传入的height - 底部padding

  return (
    <Chart
      data={chartData}
      height={chartHeight - 30 || 230}
      width={10}
      forceFit
      scale={cols}
      padding={[20, 55, 30, 30]}
    >
      <Axis title={null} name="date" />
      <Axis title={null} name="value" />
      <Tooltip />
      <Geom type="area" position="date*value" color={['type', areaColors]} shape="smooth" />
      <Geom type="line" position="date*value" color={['type', lineColors]} shape="smooth" />
    </Chart>
  );
};

const VisitBlock = (
  props = {
    cardConfig: DEFAULT_DATA,
  },
) => {
  const { cardConfig } = props;
  const { titleItem, chartData, chartHeight } = cardConfig;
  return (
    <Card
      free
      style={{
        height: '100%',
      }}
    >
      <React.Fragment>
        <Card.Header
          title={
            <Box direction="row" spacing={50}>
              <InfoBlock {...titleItem[0]} />
              <InfoBlock {...titleItem[1]} />
            </Box>
          }
        />
      </React.Fragment>
      <Card.Content>
        <RenderPvChart chartData={chartData} chartHeight={chartHeight} />
      </Card.Content>
    </Card>
  );
};

export default VisitBlock;
