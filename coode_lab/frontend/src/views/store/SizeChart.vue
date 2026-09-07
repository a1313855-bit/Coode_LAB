<template>
  <div class="size-chart-page">
    <h1>尺寸對照</h1>
    <div class="chart-wrap">
      <img src="/images/Size Chart.png" alt="尺寸對照表" class="chart-img" />
    </div>
  </div>
</template>

<style scoped>
.size-chart-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 48px 24px 80px;
  text-align: center;
}
h1 {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.1em;
  color: var(--ink);
  margin-bottom: 32px;
}
.chart-wrap {
  display: flex;
  justify-content: center;
}
.chart-img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}
</style>
