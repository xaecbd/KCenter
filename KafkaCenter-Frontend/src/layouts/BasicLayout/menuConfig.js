import React from 'react'
import { Icon } from '@alifd/next';
import iconfont from '@/iconfont.js';

const CustomIcon = Icon.createFromIconfontCN(iconfont);

const headerMenuConfig = [];
const asideMenuConfig = [
  {
    name: 'Home',
    path: '/home/page',
    icon: <CustomIcon type="iconhome"/>,
  },
  {
    name: 'My Favorite',
    path: '/favorite',
    icon: <CustomIcon type="iconFavoritestarrate" />,
  },
  {
    name: 'Topic',
    path: '/topic',
    icon: <CustomIcon type="icontopic"/>,
    children: [
      {
        name: 'Topic List',
        path: '/topic/list',
      },
      { name: 'My Task', path: '/topic/task'},
      { name: 'Queries', path: '/topic/queries' },
    ],
  },
  {
    name: 'Monitor',
    path: '/monitor',
    icon: <CustomIcon type="iconmonitor"/>,
    children: [
      { name: 'Producer', path: '/monitor/producer' },
      { name: 'Consumer', path: '/monitor/consumer' },
      { name: 'Alert', path: '/monitor/alert' },
      { name: 'Lag', path: '/monitor/lag', authority: 'admin' },
    ],
  },
  {
    name: 'Team',
    path: '/setting/team/list',
    authority: ['member'],
    icon: <CustomIcon type="iconTeam"/>,
  },
  {
    name: 'Connect',
    path: '/connect',
    icon: <CustomIcon type="iconlink"/>,
  },
  {
    name: 'KSQL',
    path: '/ksql/list',
    icon: <CustomIcon type="iconsql"/>,
  },
  {
    name: 'Approve',
    path: '/approve',
    icon: <CustomIcon type="iconic_approve"/>,
    authority: 'admin',
  },
  {
    name: 'Kafka Manager',
    path: '/kafka-manager',
    icon: <CustomIcon type="iconall1"/>,
    authority: 'admin',
    children: [
      { name: 'Cluster', path: '/kafka-manager/cluster' },
      { name: 'Topic', path: '/kafka-manager/topic' },
      { name: 'Broker', path: '/kafka-manager/broker' },
      { name: 'Group', path: '/kafka-manager/group' },
    ],
  },
  {
    name: 'Setting',
    path: '/setting',
    icon: <CustomIcon type="iconsetting"/>,
    authority: 'admin',
    children: [
      { name: 'User', path: '/setting/user' },
      { name: 'Team', path: '/setting/team' },
    ],
  },
];
export { headerMenuConfig, asideMenuConfig };
