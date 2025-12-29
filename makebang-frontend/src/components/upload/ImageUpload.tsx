import { useState } from 'react'
import { Upload, message, Modal } from 'antd'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons'
import type { UploadProps, UploadFile, RcFile } from 'antd/es/upload'
import { uploadImage, type FileVO } from '@/api/file'

interface ImageUploadProps {
  value?: string
  onChange?: (url: string) => void
  maxSize?: number  // MB
  accept?: string
  businessType?: string
  businessId?: number
}

const ImageUpload = ({
  value,
  onChange,
  maxSize = 10,
  accept = 'image/jpeg,image/png,image/gif,image/webp',
  businessType,
  businessId,
}: ImageUploadProps) => {
  const [loading, setLoading] = useState(false)
  const [previewOpen, setPreviewOpen] = useState(false)
  const [previewImage, setPreviewImage] = useState('')

  // 上传前校验
  const beforeUpload = (file: RcFile) => {
    const isValidType = accept.split(',').some((type) => file.type === type.trim())
    if (!isValidType) {
      message.error('请上传正确格式的图片文件')
      return Upload.LIST_IGNORE
    }

    const isValidSize = file.size / 1024 / 1024 < maxSize
    if (!isValidSize) {
      message.error(`图片大小不能超过 ${maxSize}MB`)
      return Upload.LIST_IGNORE
    }

    return true
  }

  // 自定义上传
  const customRequest: UploadProps['customRequest'] = async (options) => {
    const { file, onSuccess, onError, onProgress } = options

    setLoading(true)
    try {
      const res = await uploadImage(
        file as File,
        businessType,
        businessId,
        (percent) => onProgress?.({ percent })
      )

      onSuccess?.(res.data)
      onChange?.(res.data.url)
      message.success('上传成功')
    } catch (error: any) {
      onError?.(error)
      message.error(error.message || '上传失败')
    } finally {
      setLoading(false)
    }
  }

  // 预览
  const handlePreview = () => {
    if (value) {
      setPreviewImage(value)
      setPreviewOpen(true)
    }
  }

  // 删除
  const handleRemove = () => {
    onChange?.('')
    return true
  }

  const uploadButton = (
    <div>
      {loading ? <LoadingOutlined /> : <PlusOutlined />}
      <div style={{ marginTop: 8 }}>上传</div>
    </div>
  )

  const fileList: UploadFile[] = value
    ? [
        {
          uid: '-1',
          name: 'image',
          status: 'done',
          url: value,
        },
      ]
    : []

  return (
    <>
      <Upload
        listType="picture-card"
        fileList={fileList}
        beforeUpload={beforeUpload}
        customRequest={customRequest}
        onPreview={handlePreview}
        onRemove={handleRemove}
        accept={accept}
        maxCount={1}
      >
        {!value && uploadButton}
      </Upload>
      <Modal
        open={previewOpen}
        title="图片预览"
        footer={null}
        onCancel={() => setPreviewOpen(false)}
      >
        <img alt="preview" style={{ width: '100%' }} src={previewImage} />
      </Modal>
    </>
  )
}

export default ImageUpload
