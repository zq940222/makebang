import { lazy, Suspense } from 'react'
import { RouteObject, Navigate } from 'react-router-dom'
import { Spin } from 'antd'
import MainLayout from '@/components/layout/MainLayout'

// 懒加载组件
const Home = lazy(() => import('@/pages/home'))
const Login = lazy(() => import('@/pages/auth/Login'))
const Register = lazy(() => import('@/pages/auth/Register'))
const ProjectList = lazy(() => import('@/pages/project/ProjectList'))
const ProjectDetail = lazy(() => import('@/pages/project/ProjectDetail'))
const ProjectPublish = lazy(() => import('@/pages/project/ProjectPublish'))
const UserProfile = lazy(() => import('@/pages/user/UserProfile'))
const OrderList = lazy(() => import('@/pages/order/OrderList'))
const OrderDetail = lazy(() => import('@/pages/order/OrderDetail'))
const MessageCenter = lazy(() => import('@/pages/message/MessageCenter'))
const MyBids = lazy(() => import('@/pages/bid/MyBids'))
const Wallet = lazy(() => import('@/pages/wallet/Wallet'))

// 加载中组件
const Loading = () => (
  <div className="flex items-center justify-center h-screen">
    <Spin size="large" tip="加载中..." />
  </div>
)

// 懒加载包装器
const lazyLoad = (Component: React.LazyExoticComponent<() => JSX.Element>) => (
  <Suspense fallback={<Loading />}>
    <Component />
  </Suspense>
)

const routes: RouteObject[] = [
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: lazyLoad(Home),
      },
      {
        path: 'projects',
        element: lazyLoad(ProjectList),
      },
      {
        path: 'projects/:id',
        element: lazyLoad(ProjectDetail),
      },
      {
        path: 'projects/publish',
        element: lazyLoad(ProjectPublish),
      },
      {
        path: 'user/profile',
        element: lazyLoad(UserProfile),
      },
      {
        path: 'orders',
        element: lazyLoad(OrderList),
      },
      {
        path: 'orders/:id',
        element: lazyLoad(OrderDetail),
      },
      {
        path: 'messages',
        element: lazyLoad(MessageCenter),
      },
      {
        path: 'bids',
        element: lazyLoad(MyBids),
      },
      {
        path: 'wallet',
        element: lazyLoad(Wallet),
      },
    ],
  },
  {
    path: '/login',
    element: lazyLoad(Login),
  },
  {
    path: '/register',
    element: lazyLoad(Register),
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
]

export default routes
