import React from 'react';
import { ResponsiveGrid } from '@alifd/next';
import PageHeader from '@/components/PageHeader';
import FusionCardPieChart from './components/FusionCardPieChart';

const { Cell } = ResponsiveGrid;

const Monitor = () => (
  <ResponsiveGrid gap={20}>
    <Cell colSpan={12}>
      <PageHeader
        title="监控台页面"
        breadcrumbs={[
          {
            name: 'Dashboard',
          },
          {
            name: '监控台页面',
          },
        ]}
      />
    </Cell>

    <Cell colSpan={12}>
      <FusionCardPieChart />
    </Cell>
  </ResponsiveGrid>
);

export default Monitor;
