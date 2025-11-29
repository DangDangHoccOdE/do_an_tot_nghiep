import { ElMessage } from 'element-plus';

let messageInstance = null;

function handleMessage(type, msg, duration = 3000) {
  if (!msg) return;

  if (messageInstance) {
    messageInstance.close();
  }

  messageInstance = ElMessage({
    type,
    message: msg,
    duration
  });
}

export function handleError(err, i18, fallbackKey) {
  const msg = err?.response?.data?.message || i18?.(fallbackKey) || fallbackKey;
  handleMessage('error', msg);
}

export function handleSuccess(msg) {
  handleMessage('success', msg);
}

export function handleWarning(msg) {
  handleMessage('warning', msg);
}

export function handleInfo(msg) {
  handleMessage('info', msg);
}
