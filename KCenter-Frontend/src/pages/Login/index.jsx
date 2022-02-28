import React from 'react';
import { ResponsiveGrid } from '@alifd/next';
import LoginBlock from './components/LoginBlock';

const { Cell } = ResponsiveGrid;

const Login = () => (
  <ResponsiveGrid gap={20}>
    <Cell colSpan={12}>
      <LoginBlock />
    </Cell>
  </ResponsiveGrid>
);

export default Login;
