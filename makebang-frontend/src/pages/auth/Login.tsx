import { useState } from 'react'
import { Form, Input, Button, Checkbox, Card, Divider, message } from 'antd'
import { UserOutlined, LockOutlined, WechatOutlined, AlipayOutlined } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { setToken, setUser } from '@/store/slices/userSlice'
import { login } from '@/api/auth'

interface LoginForm {
  account: string
  password: string
  rememberMe: boolean
}

const Login = () => {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const dispatch = useDispatch()

  const onFinish = async (values: LoginForm) => {
    setLoading(true)
    try {
      const res = await login({
        account: values.account,
        password: values.password,
        rememberMe: values.rememberMe,
      })

      // 保存token和用户信息
      dispatch(setToken(res.data.accessToken))
      dispatch(setUser(res.data.user))

      // 保存refreshToken
      if (values.rememberMe) {
        localStorage.setItem('refreshToken', res.data.refreshToken)
      } else {
        sessionStorage.setItem('refreshToken', res.data.refreshToken)
      }

      message.success('登录成功')
      navigate('/')
    } catch (error: any) {
      // 错误已在request拦截器中处理
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center py-12 px-4">
      <Card className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-gray-800">欢迎回到码客邦</h1>
          <p className="text-gray-500 mt-2">登录您的账号</p>
        </div>

        <Form
          name="login"
          initialValues={{ rememberMe: true }}
          onFinish={onFinish}
          size="large"
        >
          <Form.Item
            name="account"
            rules={[{ required: true, message: '请输入用户名/手机号/邮箱' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名/手机号/邮箱"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <div className="flex justify-between items-center">
              <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                <Checkbox>记住我</Checkbox>
              </Form.Item>
              <Link to="/forgot-password" className="text-blue-500">
                忘记密码?
              </Link>
            </div>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>
              登录
            </Button>
          </Form.Item>
        </Form>

        <Divider plain>其他登录方式</Divider>

        <div className="flex justify-center space-x-6">
          <Button
            shape="circle"
            size="large"
            icon={<WechatOutlined className="text-green-500" />}
            onClick={() => message.info('微信登录功能开发中')}
          />
          <Button
            shape="circle"
            size="large"
            icon={<AlipayOutlined className="text-blue-500" />}
            onClick={() => message.info('支付宝登录功能开发中')}
          />
        </div>

        <div className="text-center mt-6">
          <span className="text-gray-500">还没有账号? </span>
          <Link to="/register" className="text-blue-500">
            立即注册
          </Link>
        </div>
      </Card>
    </div>
  )
}

export default Login
