// 以下文件格式为描述路由的协议格式
// 你可以调整 routerConfig 里的内容
// 变量名 routerConfig 为 iceworks 检测关键字，请不要修改名称
import { lazy } from 'react';
import UserLogin from './pages/UserLogin';

const Empty = lazy(() => import('./pages/Exception'));
const Forbidden = lazy(() => import('./pages/Exception'));
const NotFound = lazy(() => import('./pages/Exception'));
const ServerError = lazy(() => import('./pages/Exception'));

const Logging = lazy(() => import('./pages/Logging'));
const MyProfile = lazy(() => import('./pages/UserInfo'));
// Topic page
const TopicList = lazy(() => import('./pages/Topic/TopicList'));
const TopicTask = lazy(() => import('./pages/Topic/MyTask'));
const Queries = lazy(() => import('./pages/Topic/Queries'));
// Home page
const HomeCluster = lazy(() => import('./pages/Home/ClusterInfo'));
const MonitorDetail = lazy(() => import('./pages/Home/MonitorDetail'));
const SummaryChart = lazy(() => import('./pages/Home/Summary'));
// Setting page
const Team = lazy(() => import('./pages/Setting/Team'));
const User = lazy(() => import('./pages/Setting/User'));
// Approve page
const ApproveTask = lazy(() => import('./pages/ApproveTask'));
// kafka-manager
const managerTopic = lazy(() => import('./pages/KafkaManager/Topic/TopicList'));
const managerGroup = lazy(() => import('./pages/KafkaManager/Group'));
const managerTopiDetail = lazy(() => import('./pages/KafkaManager/Topic/TopicDetail/Detail'));
const managerUpdateConfig = lazy(() => import('./pages/KafkaManager/Topic/TopicDetail/Config'));
const Cluster = lazy(() => import('./pages/KafkaManager/Cluster'));
const managerBroker = lazy(() => import('./pages/KafkaManager/Broker'));


// monitor page
const ProducerTable = lazy(() => import('./pages/Monitor/Producer/Topic'));
const ProducerMetric = lazy(() => import('./pages/Monitor/Producer/Metric'));

const consumerList = lazy(() => import('./pages/Monitor/Consumer'));
const consumerTopicDetail = lazy(() => import('./pages/Monitor/Consumer/Topic/TopicDetail'));
const consumerChart = lazy(() => import('./pages/Monitor/Consumer/Topic/ConsumerChart'));

const consumerGroupDetail = lazy(() => import('./pages/Monitor/Consumer/Group/GroupDetail'));
const AlertList = lazy(() => import('./pages/Monitor/Alert'));
const ConsumerLag = lazy(() => import('./pages/Monitor/Lag'));
// connect page
const ConnectPage = lazy(() => import('./pages/Connect/Connect'));
// favorte page
const MyFavorite = lazy(() => import('./pages/Favorite'));
// ksql
const Ksql = lazy(() => import('./pages/Ksql'));

const KsqlHome = lazy(() => import('./pages/Ksql/KsqlList/KsqlHome'));
const KsqlConsole = lazy(() => import('./pages/Ksql/KsqlList/Console/index.js'));

const routerConfig = [
  {
    path: '/home/page',
    component: HomeCluster,
  },
  {
    path: '/home/detail/:item',
    component: MonitorDetail,
  },
  {
    path: '/home/cluster',
    component: SummaryChart,
  },
  {
    path: '/favorite',
    component: MyFavorite,
    exact: true,
  },
  {
    path: '/approve',
    component: ApproveTask,
    authority: 'admin',
  },
  {
    path: '/connect',
    component: ConnectPage,
  },
  {
    path: '/logging',
    component: Logging,
  },
  {
    path: '/user/login',
    component: UserLogin,
  },
  {
    path: '/topic/list',
    component: TopicList,
  },
  {
    path: '/topic/task',
    component: TopicTask,
  },
  {
    path: '/topic/queries',
    component: Queries,
  },
  {
    path: '/setting/team',
    component: Team,
  },
  {
    path: '/setting/user',
    component: User,
  },
  {
    path: '/exception/500',
    component: ServerError,
  },
  {
    path: '/exception/403',
    component: Forbidden,
  },
  {
    path: '/exception/204',
    component: Empty,
  },
  {
    path: '/exception/404',
    component: NotFound,
  },
  {
    path: '/monitor/producer',
    component: ProducerTable,
    exact: true,
  },
  {
    path: '/monitor/producer/metric/:id/:clusterName/:topicName',
    component: ProducerMetric,
    exact: true,
  },
  {
    path: '/monitor/consumer',
    component: consumerList,
    exact: true,
  },
  {
    path: '/monitor/consumer/topic/consumer_offsets/:id/:clusterName/:topicName',
    component: consumerTopicDetail,
    exact: true,
  },
  {
    path: '/monitor/consumer/topic/consumer_offsets/chart/:id/:topic/:group/:type',
    component: consumerChart,
    exact: true,
  },
  {
    path: '/monitor/consumer/group/detail/:clusterID/:clusterName/:consummerGroup',
    component: consumerGroupDetail,
  },
  {
    path: '/users',
    component: MyProfile,
    exact: true,
  },
  {
    path: '/monitor/alert',
    component: AlertList,
  },
  {
    path: '/monitor/lag',
    component: ConsumerLag,
  },

  {
    path: '/kafka-manager/topic',
    component: managerTopic,
    exact: true,
  },
  {
    path: '/kafka-manager/broker',
    component: managerBroker,
    exact: true,
  },
  {
    path: '/kafka-manager/topic/:clusterId/:topic',
    component: managerTopiDetail,
    exact: true,
  },
  {
    path: '/kafka-manager/topic/config/:clusterId/:topic',
    component: managerUpdateConfig,
    exact: true,
  },
  {
    path: '/kafka-manager/group',
    component: managerGroup,
    exact: true,
  },
  {
    path: '/kafka-manager/cluster',
    component: Cluster,
  },
  {
    path: '/ksql/list',
    component: Ksql,
    exact: true,
  },
  {
    path: '/ksql/:clusterName/:ksqlServerId/:tab',
    component: KsqlHome,
  },
  {
    path: '/ksql/console',
    component: KsqlConsole,
  },
];

export default routerConfig;
