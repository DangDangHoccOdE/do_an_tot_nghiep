import { ref } from "vue";
import { handleError } from "./handleMessage";

export function useLoading(i18) {
  const loading = ref(false);

  async function withLoading(asyncFn, errorMsg, ...args) {
    loading.value = true;
    try {
      return await asyncFn(...args);
    } catch (err) {
      handleError(err, i18, errorMsg);
      return false;
    } finally {
      loading.value = false;
    }
  }

  return { loading, withLoading };
}
