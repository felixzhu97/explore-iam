import type { EChartsCoreOption } from 'echarts/core';

export type SharedChartType = 'bar' | 'line' | 'pie' | 'doughnut';

export interface SharedChartItem {
  label: string;
  value: number;
}

const ACCENT = '#0051c3';
const ACCENT_SOFT = 'rgba(0, 81, 195, 0.12)';
const MUTED = '#6b6b70';
const GRID = '#e5e5e5';

export function buildSharedChartOption(
  type: SharedChartType,
  data: SharedChartItem[],
  title?: string,
): EChartsCoreOption {
  const labels = data.map((item) => item.label);
  const values = data.map((item) => item.value);

  if (type === 'pie' || type === 'doughnut') {
    return {
      title: title
        ? { text: title, left: 'center', textStyle: { fontSize: 14, fontWeight: 500 } }
        : undefined,
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: type === 'doughnut' ? ['42%', '68%'] : '65%',
          data: data.map((item) => ({ name: item.label, value: item.value })),
        },
      ],
    };
  }

  return {
    title: title
      ? { text: title, left: 'center', textStyle: { fontSize: 14, fontWeight: 500 } }
      : undefined,
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 16, top: title ? 48 : 24, bottom: 36 },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [
      {
        type,
        data: values,
        ...(type === 'line' ? { smooth: true } : {}),
      },
    ],
  };
}

/** Full-width area line chart for dashboard metric cards (Cloudflare-style). */
export function buildMetricLineOption(data: SharedChartItem[]): EChartsCoreOption {
  const labels = data.map((item) => item.label);
  const values = data.map((item) => item.value);

  return {
    animation: false,
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: GRID,
      textStyle: { color: '#1d1d1f', fontSize: 12 },
    },
    grid: { left: 8, right: 8, top: 12, bottom: 4, containLabel: false },
    xAxis: {
      type: 'category',
      data: labels,
      show: false,
      boundaryGap: false,
    },
    yAxis: {
      type: 'value',
      show: false,
      scale: true,
    },
    series: [
      {
        type: 'line',
        data: values,
        smooth: 0.35,
        showSymbol: false,
        lineStyle: { width: 2, color: ACCENT },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: ACCENT_SOFT },
              { offset: 1, color: 'rgba(0, 81, 195, 0)' },
            ],
          },
        },
      },
    ],
  };
}

/** Compact sparkline for mini metric cards. */
export function buildSparklineOption(values: number[]): EChartsCoreOption {
  return {
    animation: false,
    grid: { left: 0, right: 0, top: 4, bottom: 0 },
    xAxis: { type: 'category', show: false, data: values.map((_, i) => String(i)) },
    yAxis: { type: 'value', show: false, scale: true },
    series: [
      {
        type: 'line',
        data: values,
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 1.5, color: ACCENT },
        areaStyle: { color: ACCENT_SOFT },
      },
    ],
  };
}

export function formatCompact(n: number): string {
  if (n >= 1_000_000) {
    return `${(n / 1_000_000).toFixed(2).replace(/\.?0+$/, '')}M`;
  }
  if (n >= 1_000) {
    return `${(n / 1_000).toFixed(2).replace(/\.?0+$/, '')}k`;
  }
  return String(n);
}

export function formatDelta(pct: number): string {
  const sign = pct > 0 ? '+' : '';
  return `${sign}${pct.toFixed(1)}%`;
}

export { MUTED, ACCENT };
