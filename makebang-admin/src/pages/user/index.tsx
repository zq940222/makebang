import { useState, useEffect, useCallback } from 'react'
import { Card, Table, Input, Select, Tag, Button, Space, message, Popconfirm, Avatar, Modal } from 'antd'
import { SearchOutlined, UserOutlined } from '@ant-design/icons'
import { getUserList, disableUser, enableUser, setUserRole, type UserVO } from '@/api/admin'
import type { ColumnsType } from 'antd/es/table'

const { Option } = Select

const UserManagement = () => {
  const [loading, setLoading] = useState(false)
  const [users, setUsers] = useState<UserVO[]>([])
  const [total, setTotal] = useState(0)
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [userType, setUserType] = useState<number | undefined>()
  const [status, setStatus] = useState<number | undefined>()

  // 设置角色弹窗
  const [roleModalOpen, setRoleModalOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState<UserVO | null>(null)
  const [newRole, setNewRole] = useState<number>(0)

  const loadUsers = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getUserList({
        keyword: keyword || undefined,
        userType,
        status,
        current,
        size: pageSize,
      })
      setUsers(res.data.records)
      setTotal(res.data.total)
    } catch (error) {
      console.error('加载用户列表失败:', error)
      message.error('加载用户列表失败')
    } finally {
      setLoading(false)
    }
  }, [keyword, userType, status, current, pageSize])

  useEffect(() => {
    loadUsers()
  }, [loadUsers])

  const handleDisable = async (userId: number) => {
    try {
      await disableUser(userId)
      message.success('用户已禁用')
      loadUsers()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const handleEnable = async (userId: number) => {
    try {
      await enableUser(userId)
      message.success('用户已启用')
      loadUsers()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const openRoleModal = (user: UserVO) => {
    setSelectedUser(user)
    setNewRole(user.role || 0)
    setRoleModalOpen(true)
  }

  const handleSetRole = async () => {
    if (!selectedUser) return
    try {
      await setUserRole(selectedUser.id, newRole)
      message.success('角色设置成功')
      setRoleModalOpen(false)
      loadUsers()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const userTypeMap: Record<number, { text: string; color: string }> = {
    0: { text: '需求方', color: 'blue' },
    1: { text: '程序员', color: 'green' },
    2: { text: '两者', color: 'purple' },
  }

  const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '禁用', color: 'red' },
    1: { text: '正常', color: 'green' },
  }

  const roleMap: Record<number, { text: string; color: string }> = {
    0: { text: '普通用户', color: 'default' },
    1: { text: '管理员', color: 'blue' },
    2: { text: '超级管理员', color: 'gold' },
  }

  const columns: ColumnsType<UserVO> = [
    {
      title: '用户',
      key: 'user',
      render: (_, record) => (
        <div className="flex items-center">
          <Avatar icon={<UserOutlined />} src={record.avatar} />
          <div className="ml-2">
            <div className="font-medium">{record.username}</div>
            <div className="text-xs text-gray-400">{record.phone || record.email}</div>
          </div>
        </div>
      ),
    },
    {
      title: '用户类型',
      dataIndex: 'userType',
      key: 'userType',
      render: (type: number) => (
        <Tag color={userTypeMap[type]?.color}>{userTypeMap[type]?.text}</Tag>
      ),
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      render: (role: number) => (
        <Tag color={roleMap[role]?.color}>{roleMap[role]?.text}</Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={statusMap[status]?.color}>{statusMap[status]?.text}</Tag>
      ),
    },
    {
      title: '注册时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => date?.split('T')[0],
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          {record.status === 1 ? (
            <Popconfirm
              title="确定禁用该用户？"
              onConfirm={() => handleDisable(record.id)}
            >
              <Button type="link" danger size="small">
                禁用
              </Button>
            </Popconfirm>
          ) : (
            <Popconfirm
              title="确定启用该用户？"
              onConfirm={() => handleEnable(record.id)}
            >
              <Button type="link" size="small">
                启用
              </Button>
            </Popconfirm>
          )}
          <Button type="link" size="small" onClick={() => openRoleModal(record)}>
            设置角色
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-bold mb-4">用户管理</h2>

      <Card>
        <div className="flex flex-wrap gap-4 mb-4">
          <Input
            placeholder="搜索用户名/手机号/邮箱"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 240 }}
            allowClear
          />
          <Select
            placeholder="用户类型"
            value={userType}
            onChange={setUserType}
            style={{ width: 120 }}
            allowClear
          >
            <Option value={0}>需求方</Option>
            <Option value={1}>程序员</Option>
            <Option value={2}>两者</Option>
          </Select>
          <Select
            placeholder="状态"
            value={status}
            onChange={setStatus}
            style={{ width: 120 }}
            allowClear
          >
            <Option value={0}>禁用</Option>
            <Option value={1}>正常</Option>
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={loadUsers}>
            搜索
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={loading}
          pagination={{
            current,
            pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, size) => {
              setCurrent(page)
              setPageSize(size)
            },
          }}
        />
      </Card>

      {/* 设置角色弹窗 */}
      <Modal
        title="设置用户角色"
        open={roleModalOpen}
        onOk={handleSetRole}
        onCancel={() => setRoleModalOpen(false)}
      >
        <div className="py-4">
          <div className="mb-4">
            用户: <span className="font-medium">{selectedUser?.username}</span>
          </div>
          <Select
            value={newRole}
            onChange={setNewRole}
            style={{ width: '100%' }}
          >
            <Option value={0}>普通用户</Option>
            <Option value={1}>管理员</Option>
            <Option value={2}>超级管理员</Option>
          </Select>
        </div>
      </Modal>
    </div>
  )
}

export default UserManagement
