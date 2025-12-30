import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useDispatch } from 'react-redux'
import { setUser, setToken } from '@/store/slices/userSlice'
import { login, getCurrentUser } from '@/api/auth'
import { UserRole } from '@/types'

interface LoginForm {
  account: string
  password: string
}

const Login = () => {
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const [loading, setLoading] = useState(false)

  const onFinish = async (values: LoginForm) => {
    setLoading(true)
    try {
      const res = await login(values)
      const { accessToken, user } = res.data

      // 验证是否是管理员
      if (!user.role || user.role < UserRole.ADMIN) {
        message.error('您没有管理员权限，无法登录管理后台')
        return
      }

      // 保存 token 和用户信息
      dispatch(setToken(accessToken))
      dispatch(setUser(user))

      message.success('登录成功')
      navigate('/')
    } catch (error: any) {
      message.error(error.message || '登录失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-500 to-purple-600">
      <Card
        className="w-full max-w-md shadow-2xl"
        bordered={false}
      >
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-gray-800 mb-2">码客邦管理后台</h1>
          <p className="text-gray-500">请使用管理员账号登录</p>
        </div>

        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="account"
            rules={[{ required: true, message: '请输入用户名/手机号/邮箱' }]}
          >
            <Input
              prefix={<UserOutlined className="text-gray-400" />}
              placeholder="用户名/手机号/邮箱"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined className="text-gray-400" />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>

        <div className="text-center text-gray-400 text-sm">
          <p>仅限管理员登录</p>
        </div>
      </Card>
    </div>
  )
}

export default Login
