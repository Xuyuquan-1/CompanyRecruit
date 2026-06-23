<template>
  <div class="placeholder">
    <el-empty description="该模块正在开发中，敬请期待...">
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </el-empty>
  </div>
</template>

<style scoped>
.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}
</style>
