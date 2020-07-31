import React from 'react';
import { ResponsiveGrid } from '@alifd/next';
import FusionCardAreaChart from './components/CardAreaChart';
import FusionCardPieChart from './components/CardPieChart';
import WebInfoBlock from './components/WebInfoBlock';
import VisitBlock from './components/VisitBlock';
import TrendChart from './components/TrendChart';
import mock from './mock.js';

const { Cell } = ResponsiveGrid;

const MonitorBlock = () => (
  <ResponsiveGrid gap={20}>
    <Cell colSpan={3}>
      <WebInfoBlock cardConfig={mock.JSErrorRate} />
    </Cell>
    <Cell colSpan={6} rowSpan={2}>
      <VisitBlock />
    </Cell>
    <Cell colSpan={3}>
      <WebInfoBlock cardConfig={mock.APISuccessRate} />
    </Cell>
    <Cell colSpan={3}>
      <WebInfoBlock cardConfig={mock.FirstRenderTime} />
    </Cell>
    <Cell colSpan={3}>
      <WebInfoBlock cardConfig={mock.ResourceError} />
    </Cell>

    <Cell colSpan={12}>
      <TrendChart />
    </Cell>

    <Cell colSpan={8}>
      <FusionCardPieChart />
    </Cell>

    <Cell colSpan={4}>
      <FusionCardAreaChart />
    </Cell>
  </ResponsiveGrid>
);

export default MonitorBlock;
