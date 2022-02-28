import React, { useState } from 'react';
import { Search } from '@alifd/next';

const mockData = [
  {
    label: '搜索条件一',
    value: '搜索条件一',
  },
  {
    label: '搜索条件二',
    value: '搜索条件二',
  },
  {
    label: '搜索条件三',
    value: '搜索条件三',
  },
  {
    label: '搜索条件四',
    value: '搜索条件四',
  },
];
export default function GlobalSearch() {
  const initData = [];
  const [dataSource, setDataSource] = useState(initData);

  function onChange() {
    setDataSource(mockData);
  }

  return <Search dataSource={dataSource} shape="simple" type="dark" onChange={onChange} />;
}
