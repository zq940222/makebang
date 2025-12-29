import { useState } from 'react'
import { Upload, Button, message, Progress, List } from 'antd'
import { UploadOutlined, DeleteOutlined, FileOutlined, FilePdfOutlined, FileWordOutlined, FileExcelOutlined, FileZipOutlined, FileImageOutlined } from '@ant-design/icons'
import type { UploadProps, RcFile } from 'antd/es/upload'
import { uploadFile, deleteFile, type FileVO } from '@/api/file'

interface FileUploadProps {
  value?: FileVO[]
  onChange?: (files: FileVO[]) => void
  maxCount?: number
  maxSize?: number  // MB
  accept?: string
  businessType?: string
  businessId?: number
  multiple?: boolean
}

const FileUpload = ({
  value = [],
  onChange,
  maxCount = 10,
  maxSize = 50,
  accept,
  businessType,
  businessId,
  multiple = true,
}: FileUploadProps) => {
  const [uploading, setUploading] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)

  // 获取文件图标
  const getFileIcon = (mimeType?: string) => {
    if (!mimeType) return <FileOutlined />
    if (mimeType.includes('image')) return <FileImageOutlined className="text-green-500" />
    if (mimeType.includes('pdf')) return <FilePdfOutlined className="text-red-500" />
    if (mimeType.includes('word')) return <FileWordOutlined className="text-blue-500" />
    if (mimeType.includes('excel') || mimeType.includes('spreadsheet')) return <FileExcelOutlined className="text-green-600" />
    if (mimeType.includes('zip') || mimeType.includes('rar')) return <FileZipOutlined className="text-yellow-500" />
    return <FileOutlined />
  }

  // 格式化文件大小
  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / 1024 / 1024).toFixed(1) + ' MB'
  }

  // 上传前校验
  const beforeUpload = (file: RcFile) => {
    if (value.length >= maxCount) {
      message.error(`最多只能上传 ${maxCount} 个文件`)
      return Upload.LIST_IGNORE
    }

    const isValidSize = file.size / 1024 / 1024 < maxSize
    if (!isValidSize) {
      message.error(`文件大小不能超过 ${maxSize}MB`)
      return Upload.LIST_IGNORE
    }

    return true
  }

  // 自定义上传
  const customRequest: UploadProps['customRequest'] = async (options) => {
    const { file, onSuccess, onError } = options

    setUploading(true)
    setUploadProgress(0)

    try {
      const res = await uploadFile(
        file as File,
        businessType,
        businessId,
        (percent) => setUploadProgress(percent)
      )

      onSuccess?.(res.data)
      onChange?.([...value, res.data])
      message.success('上传成功')
    } catch (error: any) {
      onError?.(error)
      message.error(error.message || '上传失败')
    } finally {
      setUploading(false)
      setUploadProgress(0)
    }
  }

  // 删除文件
  const handleDelete = async (file: FileVO) => {
    try {
      await deleteFile(file.id)
      onChange?.(value.filter((f) => f.id !== file.id))
      message.success('删除成功')
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
  }

  return (
    <div>
      <Upload
        beforeUpload={beforeUpload}
        customRequest={customRequest}
        showUploadList={false}
        accept={accept}
        multiple={multiple}
        disabled={uploading}
      >
        <Button icon={<UploadOutlined />} loading={uploading}>
          {uploading ? '上传中...' : '选择文件'}
        </Button>
      </Upload>

      {uploading && (
        <Progress percent={uploadProgress} size="small" className="mt-2" />
      )}

      {value.length > 0 && (
        <List
          className="mt-4"
          size="small"
          dataSource={value}
          renderItem={(file) => (
            <List.Item
              actions={[
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => handleDelete(file)}
                />,
              ]}
            >
              <List.Item.Meta
                avatar={getFileIcon(file.mimeType)}
                title={
                  <a href={file.url} target="_blank" rel="noopener noreferrer">
                    {file.originalName}
                  </a>
                }
                description={formatFileSize(file.size)}
              />
            </List.Item>
          )}
        />
      )}

      <div className="text-gray-400 text-xs mt-2">
        最多上传 {maxCount} 个文件，单个文件不超过 {maxSize}MB
      </div>
    </div>
  )
}

export default FileUpload
