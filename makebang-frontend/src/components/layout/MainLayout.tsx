import { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Button, Dropdown, Avatar, Space, Badge } from 'antd'
import {
  HomeOutlined,
  ProjectOutlined,
  FileAddOutlined,
  UserOutlined,
  BellOutlined,
  MessageOutlined,
  LogoutOutlined,
  LoginOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  WalletOutlined,
  DashboardOutlined,
  TeamOutlined,
  AuditOutlined,
} from '@ant-design/icons'
import { useSelector, useDispatch } from 'react-redux'
import type { RootState } from '@/store'
import { logout } from '@/store/slices/userSlice'

const { Header, Content, Footer } = Layout

const MainLayout = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const dispatch = useDispatch()
  const { isLoggedIn, currentUser } = useSelector((state: RootState) => state.user)
  const [collapsed, setCollapsed] = useState(false)

  const menuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: '首页',
    },
    {
      key: '/projects',
      icon: <ProjectOutlined />,
      label: '浏览需求',
    },
    {
      key: '/projects/publish',
      icon: <FileAddOutlined />,
      label: '发布需求',
    },
  ]

  // 判断是否是管理员
  const isAdmin = currentUser?.role && currentUser.role >= 1

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
      onClick: () => navigate('/user/profile'),
    },
    {
      key: 'orders',
      icon: <ProjectOutlined />,
      label: '我的订单',
      onClick: () => navigate('/orders'),
    },
    {
      key: 'wallet',
      icon: <WalletOutlined />,
      label: '我的钱包',
      onClick: () => navigate('/wallet'),
    },
    {
      key: 'messages',
      icon: <MessageOutlined />,
      label: '消息中心',
      onClick: () => navigate('/messages'),
    },
    // 管理员菜单
    ...(isAdmin ? [
      {
        type: 'divider' as const,
      },
      {
        key: 'admin',
        icon: <DashboardOutlined />,
        label: '管理后台',
        onClick: () => navigate('/admin'),
      },
      {
        key: 'admin-users',
        icon: <TeamOutlined />,
        label: '用户管理',
        onClick: () => navigate('/admin/users'),
      },
      {
        key: 'admin-projects',
        icon: <AuditOutlined />,
        label: '项目审核',
        onClick: () => navigate('/admin/projects'),
      },
      {
        key: 'admin-withdrawals',
        icon: <WalletOutlined />,
        label: '提现审核',
        onClick: () => navigate('/admin/withdrawals'),
      },
    ] : []),
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        dispatch(logout())
        navigate('/login')
      },
    },
  ]

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key)
  }

  return (
    <Layout className="min-h-screen">
      <Header className="flex items-center justify-between bg-white shadow-sm px-6 sticky top-0 z-50">
        {/* Logo */}
        <div className="flex items-center">
          <div
            className="text-xl font-bold text-primary-500 cursor-pointer mr-8"
            onClick={() => navigate('/')}
          >
            码客邦
          </div>

          {/* 主导航 */}
          <Menu
            mode="horizontal"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={handleMenuClick}
            className="border-none flex-1"
            style={{ minWidth: 300 }}
          />
        </div>

        {/* 右侧操作区 */}
        <Space size="middle">
          {isLoggedIn ? (
            <>
              <Badge count={5} size="small">
                <Button
                  type="text"
                  icon={<BellOutlined />}
                  onClick={() => navigate('/messages')}
                />
              </Badge>
              <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                <Space className="cursor-pointer">
                  <Avatar
                    src={currentUser?.avatar}
                    icon={<UserOutlined />}
                    size="small"
                  />
                  <span>{currentUser?.username || '用户'}</span>
                </Space>
              </Dropdown>
            </>
          ) : (
            <>
              <Button type="link" icon={<LoginOutlined />} onClick={() => navigate('/login')}>
                登录
              </Button>
              <Button type="primary" onClick={() => navigate('/register')}>
                注册
              </Button>
            </>
          )}
        </Space>
      </Header>

      <Content className="bg-gray-50">
        <div className="max-w-7xl mx-auto py-6 px-4">
          <Outlet />
        </div>
      </Content>

      <Footer className="text-center bg-white border-t">
        <div className="text-gray-500">
          <p>码客邦 - 专业的程序员接单平台</p>
          <p className="text-sm mt-2">
            Copyright &copy; {new Date().getFullYear()} MakeBang. All rights reserved.
          </p>
        </div>
      </Footer>
    </Layout>
  )
}

export default MainLayout
