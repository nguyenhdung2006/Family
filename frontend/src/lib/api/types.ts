export type ApiEnvelope<T> = {
  data: T;
  message?: string;
  status?: string;
};

export type ApiErrorBody = {
  errorCode?: string;
  message?: string;
  path?: string;
};

export type PageParams = {
  page?: number;
  size?: number;
};
