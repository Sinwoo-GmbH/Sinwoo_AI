const UTC_ISO_DATE_TIME_PATTERN =
  /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2}(?:\.\d+)?)?Z$/i;
const CLOCK_TIME_PATTERN = /^(\d{1,2}):(\d{2})(?::\d{2}(?:\.\d+)?)?$/;
const DATE_TIME_WITH_CLOCK_PATTERN =
  /^\d{4}-\d{2}-\d{2}[T\s](\d{1,2}):(\d{2})(?::\d{2}(?:\.\d+)?)?(?:Z|[+-]\d{2}:?\d{2})?$/i;

function toClockTime(hours: number, minutes: number) {
  if (
    !Number.isInteger(hours) ||
    !Number.isInteger(minutes) ||
    hours < 0 ||
    hours > 23 ||
    minutes < 0 ||
    minutes > 59
  ) {
    return null;
  }

  return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
}

function formatLocalClockTime(value: Date) {
  return toClockTime(value.getHours(), value.getMinutes());
}

function formatMatchedClockTime(match: RegExpMatchArray | null) {
  if (!match) return null;
  return toClockTime(Number(match[1]), Number(match[2]));
}

export function formatAttendanceTimeForDisplay(value: string | null | undefined) {
  const rawValue = value?.trim();
  if (!rawValue) return "";

  if (UTC_ISO_DATE_TIME_PATTERN.test(rawValue)) {
    const date = new Date(rawValue);
    if (!Number.isNaN(date.getTime())) {
      return formatLocalClockTime(date) ?? rawValue;
    }
  }

  return (
    formatMatchedClockTime(rawValue.match(CLOCK_TIME_PATTERN)) ??
    formatMatchedClockTime(rawValue.match(DATE_TIME_WITH_CLOCK_PATTERN)) ??
    rawValue
  );
}
