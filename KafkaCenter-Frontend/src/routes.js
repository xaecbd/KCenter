import { lazy } from 'ice';
import UserLayout from '@/layouts/UserLayout';
import Login from '@/pages/Login';

import BasicLayout from '@/layouts/BasicLayout';
import Queries from '@/pages/Topic/Queries';
// Approve page
import ApproveTask from '@/pages/ApproveTask';
import managerUpdateConfig from '@/pages/KafkaManager/Topic/TopicDetail/Config';
import consumerTopicDetail from '@/pages/Monitor/Consumer/Topic/TopicDetail';

import consumerGroupDetail from '@/pages/Monitor/Consumer/Group/GroupDetail';

import Connect from '@/pages/Connector';
import ConnectJob from '@/pages/Connector/ConnectorJob';





const Empty = lazy(() => import('@/pages/Exception'));
const Forbidden = lazy(() => import('@/pages/Exception'));
const NotFound = lazy(() => import('@/pages/Exception'));
const ServerError = lazy(() => import('@/pages/Exception'));

const Logging = lazy(() => import('@/pages/Logging'));
const MyProfile = lazy(() => import('@/pages/UserInfo'));
// Topic page
const TopicList = lazy(() => import('@/pages/Topic/TopicList'));
const TopicTask = lazy(() => import('@/pages/Topic/MyTask'));
// Home page
const HomeCluster = lazy(() => import('./pages/Home/ClusterInfo'));
const MonitorDetail = lazy(() => import('@/pages/Home/MonitorDetail'));
const SummaryChart = lazy(() => import('@/pages/Home/Summary'));
// Setting page
const Team = lazy(() => import('@/pages/Setting/Team'));
const User = lazy(() => import('@/pages/Setting/User'));
// kafka-manager
const managerTopiDetail = lazy(() => import('@/pages/KafkaManager/Topic/TopicDetail/Detail'));
const Cluster = lazy(() => import('@/pages/KafkaManager/Cluster'));

const clusterTab = lazy(() => import('@/pages/KafkaManager/ClusterTab'));

// monitor page
const ProducerTable = lazy(() => import('@/pages/Monitor/Producer/Topic'));
const ProducerMetric = lazy(() => import('@/pages/Monitor/Producer/Metric'));

const consumerList = lazy(() => import('@/pages/Monitor/Consumer'));
const consumerChart = lazy(() => import('@/pages/Monitor/Consumer/Topic/ConsumerChart'));
const AlertList = lazy(() => import('@/pages/Monitor/Alert'));
const ConsumerLag = lazy(() => import('@/pages/Monitor/Lag'));
// connect page
const ConnectPage = lazy(() => import('@/pages/Connect/Connect'));
// favorte page
const MyFavorite = lazy(() => import('@/pages/Favorite'));
// ksql
const Ksql = lazy(() => import('@/pages/Ksql'));

const KsqlHome = lazy(() => import('@/pages/Ksql/KsqlList/KsqlHome'));
const KsqlConsole = lazy(() => import('@/pages/Ksql/KsqlList/Console/index.js'));
const Loading = lazy(() => import('@/pages/Loading'));


const routerConfig = [
  {
    path: '/loading',
    component: Loading,
  },
  {
    path: '/user',
    component: UserLayout,
    children: [
      {
        path: '/login',
        component: Login,
      },
      {
        path: '/',
        redirect: '/user/login',
      },
    ],
  },
  {
    path: '/logging',
    component: Logging,
  },
  {
    path: '/',
    component: BasicLayout,
    children: [
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
        path: '/connector/list',
        component: Connect,
        exact: true,
      },
      {
        path: '/connector/job/:clusterId/:clusterName/:name/:id',
        component: ConnectJob,
        exact: true,
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
        path: '/cluster/topic/:clusterId/:clusterName/:topic',
        component: managerTopiDetail,
        exact: true,
      },
      {
        path: '/cluster/:clusterID/:clusterName/:tab',
        component: clusterTab,
        exact: true,
      },
      {
        path: '/cluster/topic/config/:clusterId/:clusterName/:topic',
        component: managerUpdateConfig,
        exact: true,
      },
      {
        path: '/cluster',
        component: Cluster,
        exact: true,
      },
      {
        path: '/ksql/list',
        component: Ksql,
        exact: true,
      },
      {
        path: '/ksql/:id/:clusterName/:ksqlServerId/:tab',
        component: KsqlHome,
      },
      {
        path: '/ksql/console',
        component: KsqlConsole,
      },
      {
        path: '/',
        redirect: '/user/login',
      },
    ],
  },
];
export default routerConfig;
