import { useState, useEffect } from 'react'
import { Form, Input, Button, Card, Radio, Divider, message } from 'antd'
import {
  UserOutlined,
  LockOutlined,
  MobileOutlined,
  MailOutlined,
  SafetyOutlined,
} from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { UserType } from '@/types'
import { register, sendVerifyCode } from '@/api/auth'

interface RegisterForm {
  username: string
  phone: string
  email: string
  password: string
  confirmPassword: string
  userType: UserType
  verifyCode: string
}

const Register = () => {
  const [loading, setLoading] = useState(false)
  const [sendingCode, setSendingCode] = useState(false)
  const [countdown, setCountdown] = useState(0)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  // 倒计时
  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000)
      return () => clearTimeout(timer)
    }
  }, [countdown])

  const onFinish = async (values: RegisterForm) => {
    if (values.password !== values.confirmPassword) {
      message.error('两次输入的密码不一致')
      return
    }

    setLoading(true)
    try {
      await register({
        username: values.username,
        phone: values.phone,
        email: values.email || undefined,
        password: values.password,
        verifyCode: values.verifyCode,
        userType: values.userType,
      })

      message.success('注册成功,请登录')
      navigate('/login')
    } catch (error: any) {
      // 错误已在request拦截器中处理
    } finally {
      setLoading(false)
    }
  }

  const handleSendCode = async () => {
    const phone = form.getFieldValue('phone')
    if (!phone) {
      message.error('请先输入手机号')
      return
    }

    // 验证手机号格式
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      message.error('请输入正确的手机号')
      return
    }

    setSendingCode(true)
    try {
      await sendVerifyCode(phone)
      message.success('验证码已发送')
      setCountdown(60)
    } catch (error: any) {
      // 错误已在request拦截器中处理
    } finally {
      setSendingCode(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center py-12 px-4">
      <Card className="w-full max-w-lg">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-gray-800">加入码客邦</h1>
          <p className="text-gray-500 mt-2">创建您的账号,开启接单之旅</p>
        </div>

        <Form
          form={form}
          name="register"
          onFinish={onFinish}
          size="large"
          layout="vertical"
          initialValues={{ userType: UserType.BOTH }}
        >
          <Form.Item
            name="userType"
            label="我是"
            rules={[{ required: true, message: '请选择用户类型' }]}
          >
            <Radio.Group className="w-full">
              <Radio.Button value={UserType.EMPLOYER} className="w-1/3 text-center">
                需求方
              </Radio.Button>
              <Radio.Button value={UserType.DEVELOPER} className="w-1/3 text-center">
                程序员
              </Radio.Button>
              <Radio.Button value={UserType.BOTH} className="w-1/3 text-center">
                两者都是
              </Radio.Button>
            </Radio.Group>
          </Form.Item>

          <Form.Item
            name="username"
            label="用户名"
            rules={[
              { required: true, message: '请输入用户名' },
              { min: 3, max: 20, message: '用户名长度为3-20个字符' },
              { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' },
            ]}
          >
            <Input prefix={<UserOutlined />} placeholder="请输入用户名" />
          </Form.Item>

          <Form.Item
            name="phone"
            label="手机号"
            rules={[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
            ]}
          >
            <Input prefix={<MobileOutlined />} placeholder="请输入手机号" />
          </Form.Item>

          <Form.Item
            name="verifyCode"
            label="验证码"
            rules={[{ required: true, message: '请输入验证码' }]}
          >
            <div className="flex space-x-2">
              <Input
                prefix={<SafetyOutlined />}
                placeholder="请输入验证码"
                className="flex-1"
              />
              <Button
                onClick={handleSendCode}
                loading={sendingCode}
                disabled={countdown > 0}
                style={{ width: 120 }}
              >
                {countdown > 0 ? `${countdown}秒后重试` : '获取验证码'}
              </Button>
            </div>
          </Form.Item>

          <Form.Item
            name="email"
            label="邮箱"
            rules={[
              { type: 'email', message: '请输入正确的邮箱地址' },
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="请输入邮箱(选填)" />
          </Form.Item>

          <Form.Item
            name="password"
            label="密码"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 6, max: 20, message: '密码长度为6-20个字符' },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="确认密码"
            dependencies={['password']}
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve()
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'))
                },
              }),
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="请再次输入密码" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>
              注册
            </Button>
          </Form.Item>
        </Form>

        <div className="text-center text-gray-500 text-sm">
          注册即表示同意
          <Link to="/terms" className="text-blue-500 mx-1">
            用户协议
          </Link>
          和
          <Link to="/privacy" className="text-blue-500 mx-1">
            隐私政策
          </Link>
        </div>

        <Divider />

        <div className="text-center">
          <span className="text-gray-500">已有账号? </span>
          <Link to="/login" className="text-blue-500">
            立即登录
          </Link>
        </div>
      </Card>
    </div>
  )
}

export default Register
