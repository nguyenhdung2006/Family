"use client";

import { Client, type IMessage, type StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAccessToken } from "@/lib/auth/token";

const WS_URL = process.env.NEXT_PUBLIC_WS_URL ?? "/ws";
const activeClients = new Set<Client>();

export function createStompClient() {
  const token = getAccessToken();
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    reconnectDelay: 4000,
    heartbeatIncoming: 10_000,
    heartbeatOutgoing: 10_000,
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {}
  });
  activeClients.add(client);
  return client;
}

export async function disconnectAllStompClients() {
  const clients = Array.from(activeClients);
  activeClients.clear();
  await Promise.allSettled(clients.map((client) => client.deactivate()));
}

export type StompMessageHandler<T> = (payload: T, raw: IMessage) => void;

export function subscribeJson<T>(client: Client, destination: string, handler: StompMessageHandler<T>): StompSubscription {
  return client.subscribe(destination, (message) => {
    handler(JSON.parse(message.body) as T, message);
  });
}
