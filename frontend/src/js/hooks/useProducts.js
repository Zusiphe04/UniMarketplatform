import { useCallback, useEffect, useState } from 'react';
import { fetchProducts } from '../api/products.js';

const EMPTY_PAGE = {
  content: [],
  number: 0,
  size: 8,
  totalElements: 0,
  totalPages: 0,
};

export function useProducts(params) {
  const [data, setData] = useState(EMPTY_PAGE);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState(null);
  const [reloadToken, setReloadToken] = useState(0);

  const retry = useCallback(() => setReloadToken((token) => token + 1), []);
  const { page, size, query, category } = params;

  useEffect(() => {
    const controller = new AbortController();
    setStatus('loading');
    setError(null);

    fetchProducts({ page, size, query, category, signal: controller.signal })
      .then((nextPage) => {
        setData(nextPage);
        setStatus('success');
      })
      .catch((requestError) => {
        if (requestError.name === 'AbortError') return;
        setError(requestError);
        setStatus('error');
      });

    return () => controller.abort();
  }, [page, size, query, category, reloadToken]);

  return {
    data,
    error,
    isLoading: status === 'loading',
    isError: status === 'error',
    isSuccess: status === 'success',
    retry,
  };
}
