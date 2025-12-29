import { http } from '@/utils/request'

// 文件VO
export interface FileVO {
  id: number
  originalName: string
  url: string
  thumbnailUrl?: string
  size: number
  type: string
  mimeType: string
  width?: number
  height?: number
  createdAt: string
}

// 上传进度回调
export type UploadProgressCallback = (percent: number) => void

/**
 * 上传单个文件
 */
export const uploadFile = (
  file: File,
  businessType?: string,
  businessId?: number,
  onProgress?: UploadProgressCallback
) => {
  const formData = new FormData()
  formData.append('file', file)
  if (businessType) formData.append('businessType', businessType)
  if (businessId) formData.append('businessId', String(businessId))

  return http.post<FileVO>('/v1/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    },
  })
}

/**
 * 上传图片（带缩略图）
 */
export const uploadImage = (
  file: File,
  businessType?: string,
  businessId?: number,
  onProgress?: UploadProgressCallback
) => {
  const formData = new FormData()
  formData.append('file', file)
  if (businessType) formData.append('businessType', businessType)
  if (businessId) formData.append('businessId', String(businessId))

  return http.post<FileVO>('/v1/files/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    },
  })
}

/**
 * 批量上传文件
 */
export const uploadBatch = (
  files: File[],
  businessType?: string,
  businessId?: number,
  onProgress?: UploadProgressCallback
) => {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  if (businessType) formData.append('businessType', businessType)
  if (businessId) formData.append('businessId', String(businessId))

  return http.post<FileVO[]>('/v1/files/upload/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    },
  })
}

/**
 * 上传头像
 */
export const uploadAvatar = (file: File, onProgress?: UploadProgressCallback) => {
  const formData = new FormData()
  formData.append('file', file)

  return http.post<FileVO>('/v1/files/upload/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    },
  })
}

/**
 * 删除文件
 */
export const deleteFile = (fileId: number) => {
  return http.delete<void>(`/v1/files/${fileId}`)
}

/**
 * 获取文件信息
 */
export const getFile = (fileId: number) => {
  return http.get<FileVO>(`/v1/files/${fileId}`)
}

/**
 * 根据业务获取文件列表
 */
export const getFilesByBusiness = (businessType: string, businessId: number) => {
  return http.get<FileVO[]>('/v1/files/business', {
    params: { businessType, businessId },
  })
}

/**
 * 检查文件是否存在（用于秒传）
 */
export const checkFileExists = (md5: string) => {
  return http.get<FileVO | null>('/v1/files/check', {
    params: { md5 },
  })
}

/**
 * 计算文件MD5
 */
export const calculateMD5 = async (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = async () => {
      try {
        const arrayBuffer = reader.result as ArrayBuffer
        const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer)
        const hashArray = Array.from(new Uint8Array(hashBuffer))
        const hashHex = hashArray.map((b) => b.toString(16).padStart(2, '0')).join('')
        resolve(hashHex)
      } catch (error) {
        reject(error)
      }
    }
    reader.onerror = () => reject(reader.error)
    reader.readAsArrayBuffer(file)
  })
}
