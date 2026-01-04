/**
 * Cloudinary Upload Utility
 * Tải file lên Cloudinary không cần backend
 */

const CLOUDINARY_CLOUD_NAME = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME || 'dvl3k4ydi'
const CLOUDINARY_UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET || 'do_an'
const CLOUDINARY_API_URL = `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`

/**
 * Upload ảnh lên Cloudinary
 * @param {File} file - File ảnh
 * @param {Function} onProgress - Callback theo dõi tiến độ (optional)
 * @returns {Promise<{url: string, publicId: string}>}
 */
export async function uploadToCloudinary(file, onProgress) {
  try {
    if (!file) {
      throw new Error('Vui lòng chọn ảnh')
    }

    // Kiểm tra loại file
    if (!file.type.startsWith('image/')) {
      throw new Error('Vui lòng chọn file ảnh')
    }

    // Kiểm tra kích thước file (5MB)
    const maxSize = 5 * 1024 * 1024
    if (file.size > maxSize) {
      throw new Error('Kích thước ảnh không được vượt quá 5MB')
    }

    const formData = new FormData()
    formData.append('file', file)
    formData.append('upload_preset', CLOUDINARY_UPLOAD_PRESET)
    formData.append('cloud_name', CLOUDINARY_CLOUD_NAME)
    formData.append('folder', 'do_an_management/avatars')

    const xhr = new XMLHttpRequest()

    // Theo dõi tiến độ upload
    if (onProgress) {
      xhr.upload.addEventListener('progress', (e) => {
        if (e.lengthComputable) {
          const percentComplete = (e.loaded / e.total) * 100
          onProgress(percentComplete)
        }
      })
    }

    return new Promise((resolve, reject) => {
      xhr.addEventListener('load', () => {
        if (xhr.status === 200) {
          const response = JSON.parse(xhr.responseText)
          resolve({
            url: response.secure_url,
            publicId: response.public_id
          })
        } else {
          reject(new Error('Upload thất bại'))
        }
      })

      xhr.addEventListener('error', () => {
        reject(new Error('Lỗi kết nối khi upload ảnh'))
      })

      xhr.open('POST', CLOUDINARY_API_URL, true)
      xhr.send(formData)
    })
  } catch (error) {
    throw error
  }
}

/**
 * Tạo URL preview từ Cloudinary
 * @param {string} publicId - Public ID từ Cloudinary
 * @param {number} width - Chiều rộng ảnh (optional)
 * @param {number} height - Chiều cao ảnh (optional)
 * @returns {string} URL ảnh
 */
export function getCloudinaryUrl(publicId, width = 200, height = 200) {
  if (!publicId) return null
  return `https://res.cloudinary.com/${CLOUDINARY_CLOUD_NAME}/image/fetch/c_fill,w_${width},h_${height}/${publicId}`
}

/**
 * Delete ảnh từ Cloudinary (cần backend signature)
 * Nên xử lý ở backend để bảo mật API key
 */
export async function deleteFromCloudinary(publicId) {
  // Gọi API backend để xóa
  // Backend sẽ gọi Cloudinary Admin API để xóa ảnh
  console.warn('Vui lòng implement xóa ảnh ở backend')
  return Promise.reject(new Error('Chức năng xóa ảnh chưa được implement'))
}
