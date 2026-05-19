export type LiveConnectionState = 'connecting' | 'open' | 'closed' | 'error';

type Subscription = {
  topic: string;
  onMessage: (payload: unknown) => void;
  onState?: (state: LiveConnectionState) => void;
};

class LiveBroker {
  private socket: WebSocket | null = null;
  private connected = false;
  private reconnectTimer: number | null = null;
  private buffer = '';
  private sequence = 1;
  private subscriptions = new Map<string, Subscription>();

  subscribeJson<T>(
    topic: string,
    onMessage: (payload: T) => void,
    onState?: (state: LiveConnectionState) => void,
  ) {
    const id = `sub-${this.sequence++}`;
    this.subscriptions.set(id, {
      topic,
      onMessage: onMessage as (payload: unknown) => void,
      onState,
    });
    onState?.(this.connected ? 'open' : 'connecting');
    this.ensureConnected();
    if (this.connected) {
      this.sendFrame('SUBSCRIBE', {
        id,
        destination: topic,
      });
    }

    return () => {
      this.subscriptions.delete(id);
      if (this.connected) {
        this.sendFrame('UNSUBSCRIBE', { id });
      }
      if (this.subscriptions.size === 0) {
        this.disconnect();
      }
    };
  }

  private ensureConnected() {
    if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
      return;
    }
    this.clearReconnectTimer();
    this.notifyAll('connecting');

    const socket = new WebSocket(resolveLiveUrl());
    this.socket = socket;

    socket.addEventListener('open', () => {
      this.sendFrame('CONNECT', {
        'accept-version': '1.2',
        'heart-beat': '10000,10000',
      });
    });

    socket.addEventListener('message', (event) => {
      this.buffer += String(event.data ?? '');
      this.consumeBuffer();
    });

    socket.addEventListener('close', () => {
      this.connected = false;
      this.notifyAll('closed');
      if (this.subscriptions.size > 0) {
        this.scheduleReconnect();
      }
    });

    socket.addEventListener('error', () => {
      this.connected = false;
      this.notifyAll('error');
    });
  }

  private consumeBuffer() {
    while (true) {
      const boundary = this.buffer.indexOf('\0');
      if (boundary < 0) {
        return;
      }
      const frame = this.buffer.slice(0, boundary);
      this.buffer = this.buffer.slice(boundary + 1);
      this.handleFrame(frame);
    }
  }

  private handleFrame(frame: string) {
    const trimmed = frame.trim();
    if (!trimmed) {
      return;
    }

    const [headerBlock, ...bodyParts] = frame.split('\n\n');
    const headerLines = headerBlock.split('\n').filter(Boolean);
    const command = headerLines.shift()?.trim();
    if (!command) {
      return;
    }

    const headers: Record<string, string> = {};
    for (const line of headerLines) {
      const separator = line.indexOf(':');
      if (separator < 0) {
        continue;
      }
      const key = line.slice(0, separator).trim();
      const value = line.slice(separator + 1).trim();
      headers[key] = value;
    }

    if (command === 'CONNECTED') {
      this.connected = true;
      this.notifyAll('open');
      for (const [id, subscription] of this.subscriptions.entries()) {
        this.sendFrame('SUBSCRIBE', {
          id,
          destination: subscription.topic,
        });
      }
      return;
    }

    if (command === 'MESSAGE') {
      const subscription = this.subscriptions.get(headers.subscription ?? '');
      if (!subscription) {
        return;
      }
      subscription.onMessage(parsePayload(bodyParts.join('\n\n')));
      return;
    }

    if (command === 'ERROR') {
      this.notifyAll('error');
    }
  }

  private sendFrame(command: string, headers: Record<string, string>, body = '') {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return;
    }
    const lines = [command, ...Object.entries(headers).map(([key, value]) => `${key}:${value}`), '', body];
    this.socket.send(`${lines.join('\n')}\0`);
  }

  private notifyAll(state: LiveConnectionState) {
    for (const subscription of this.subscriptions.values()) {
      subscription.onState?.(state);
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer != null) {
      return;
    }
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null;
      if (this.subscriptions.size > 0) {
        this.ensureConnected();
      }
    }, 1500);
  }

  private clearReconnectTimer() {
    if (this.reconnectTimer != null) {
      window.clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  private disconnect() {
    this.clearReconnectTimer();
    this.connected = false;
    this.buffer = '';
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }
}

function parsePayload(body: string) {
  const text = body.trim();
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text) as unknown;
  } catch {
    return text;
  }
}

function resolveLiveUrl() {
  const explicit = import.meta.env.VITE_WS_URL;
  if (explicit) {
    return explicit;
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.host}/ws`;
}

export const liveBroker = new LiveBroker();
