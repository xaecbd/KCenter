// 菜单配置
// headerMenuConfig：头部导航配置
// asideMenuConfig：侧边导航配置

const headerMenuConfig = [
  {
    name: 'HomePage',
    path: '/home/page',
    icon: 'home',
  },
  {
    name: 'Feedback',
    path:
      'https://github.com/xaecbd/KafkaCenter/issues/new',
    external: true,
    newWindow: true,
    icon: 'message',
  },
  {
    name: 'Help',
    path: 'https://github.com/xaecbd/KafkaCenter',
    external: true,
    newWindow: true,
    icon: 'bangzhu',
  },
];

const asideMenuConfig = [
  {
    name: 'Home',
    path: '/home/page',
    icon: 'home2',
  },
  {
    name: 'My Favorite',
    path: '/favorite',
    icon: 'customize',
  },
  {
    name: 'Topic',
    path: '/topic',
    icon: 'topic',
    children: [
      {
        name: 'Topic List',
        path: '/topic/list',
        authority: ['admin', 'member'],
      },
      { name: 'My Task', path: '/topic/task', authority: ['admin', 'member'] },
      { name: 'Queries', path: '/topic/queries' },
    ],
  },
  {
    name: 'Monitor',
    path: '/monitor',
    icon: 'chart',
    children: [
      { name: 'Producer', path: '/monitor/producer' },
      { name: 'Consumer', path: '/monitor/consumer' },
      //   { name: 'Topic', path: '/monitor/topic' },
      //   { name: 'Group', path: '/monitor/group' },
      { name: 'Alert', path: '/monitor/alert' },
      { name: 'Lag', path: '/monitor/lag', authority: 'admin' },
    ],
  },
  {
    name: 'Team',
    path: '/setting/team/list',
    authority: ['member'],
    icon: 'person',
  },
  {
    name: 'Connect',
    path: '/connect',
    icon: 'link',
  },
  {
    name: 'KSQL',
    path: '/ksql/list',
    icon: 'search',
  },
  {
    name: 'Approve',
    path: '/approve',
    icon: 'publish',
    authority: 'admin',
  },
  {
    name: 'Kafka Manager',
    path: '/kafka-manager',
    icon: 'cascades',
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
    icon: 'repair',
    authority: 'admin',
    children: [
      { name: 'User', path: '/setting/user' },
      { name: 'Team', path: '/setting/team' },
    ],
  },
];

export { headerMenuConfig, asideMenuConfig };
