/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useRef, useState } from 'react';

export function useResource(loader, reloadKey = '') {
  const [state, setState] = useState({ data: null, status: 'loading', error: null });
  const loaderRef = useRef(loader);

  useEffect(() => {
    // The loader ref keeps reload stable while still calling the latest service function.
    loaderRef.current = loader;
  }, [loader]);

  const reload = useCallback(async () => {
    setState({ data: null, status: 'loading', error: null });
    try {
      const data = await loaderRef.current();
      setState({ data, status: 'success', error: null });
    } catch (error) {
      setState({ data: null, status: 'error', error });
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload, reloadKey]);

  return { ...state, reload };
}

export function useAction(action) {
  const [state, setState] = useState({ status: 'idle', error: null });

  const execute = useCallback(
    async (...args) => {
      setState({ status: 'loading', error: null });

      try {
        const result = await action(...args);
        setState({ status: 'success', error: null });
        return result;
      } catch (error) {
        setState({ status: 'error', error });
        throw error;
      }
    },
    [action],
  );

  return { ...state, execute };
}
