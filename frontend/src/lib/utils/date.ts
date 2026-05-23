import { format, formatDistanceToNow } from "date-fns";

export function formatFamilyDate(value?: string | Date | null) {
  if (!value) {
    return "Unknown date";
  }
  return format(new Date(value), "MMM d, yyyy");
}

export function formatMessageTime(value?: string | Date | null) {
  if (!value) {
    return "";
  }
  return format(new Date(value), "h:mm a");
}

export function timeAgo(value?: string | Date | null) {
  if (!value) {
    return "";
  }
  return formatDistanceToNow(new Date(value), { addSuffix: true });
}
