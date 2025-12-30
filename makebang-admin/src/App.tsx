import { useRoutes } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import routes from '@/router'

function App() {
  const element = useRoutes(routes)

  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#1890ff',
        },
      }}
    >
      {element}
    </ConfigProvider>
  )
}

export default App
