import { useEffect, useEffectEvent, useState } from 'react';
import { liveBroker, type LiveConnectionState } from '../lib/live';

export function useLiveTopic<T>(
  topic: string | null | undefined,
  onMessage: (payload: T) => void,
  enabled = true,
) {
  const [state, setState] = useState<LiveConnectionState>('closed');
  const onMessageEvent = useEffectEvent(onMessage);

  useEffect(() => {
    if (!enabled || !topic) {
      return;
    }

    return liveBroker.subscribeJson<T>(
      topic,
      (payload) => onMessageEvent(payload),
      setState,
    );
  }, [enabled, topic]);

  return enabled && topic ? state : 'closed';
}
