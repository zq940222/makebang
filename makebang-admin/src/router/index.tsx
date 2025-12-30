import { lazy, Suspense } from 'react'
import { RouteObject, Navigate } from 'react-router-dom'
import { Spin } from 'antd'
import AdminLayout from '@/layouts/AdminLayout'

// 懒加载组件
const Login = lazy(() => import('@/pages/login'))
const Dashboard = lazy(() => import('@/pages/dashboard'))
const UserManagement = lazy(() => import('@/pages/user'))
const ProjectReview = lazy(() => import('@/pages/project'))
const WithdrawalReview = lazy(() => import('@/pages/withdrawal'))

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

// 路由守卫组件
const AuthGuard = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem('admin_token')
  if (!token) {
    return <Navigate to="/login" replace />
  }
  return <>{children}</>
}

const routes: RouteObject[] = [
  {
    path: '/login',
    element: lazyLoad(Login),
  },
  {
    path: '/',
    element: (
      <AuthGuard>
        <AdminLayout />
      </AuthGuard>
    ),
    children: [
      {
        index: true,
        element: lazyLoad(Dashboard),
      },
      {
        path: 'users',
        element: lazyLoad(UserManagement),
      },
      {
        path: 'projects',
        element: lazyLoad(ProjectReview),
      },
      {
        path: 'withdrawals',
        element: lazyLoad(WithdrawalReview),
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
]

export default routes
