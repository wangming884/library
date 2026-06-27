<template>
  <div class="config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
          <el-button type="primary" :loading="saving" @click="handleSave">
            <el-icon><Check /></el-icon> 保存配置
          </el-button>
        </div>
      </template>

      <el-table :data="configList" v-loading="loading" stripe border>
        <el-table-column prop="configKey" label="配置键" width="250">
          <template #default="{ row }">
            <span class="config-key">{{ row.configKey }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" width="250" show-overflow-tooltip />
        <el-table-column label="配置值" min-width="300">
          <template #default="{ row }">
            <el-input v-model="row.configValue" size="small" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigs, updateConfigs } from '../../../api/modules/system'

const loading = ref(false)
const saving = ref(false)
const configList = ref([])

const fetchConfigs = async () => {
  loading.value = true
  try {
    const res = await getConfigs()
    configList.value = res.data || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const data = Object.fromEntries(
      configList.value.map(item => [item.configKey, String(item.configValue ?? '')])
    )
    await updateConfigs(data)
    ElMessage.success('配置保存成功')
    fetchConfigs()
  } catch {
    // handled
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.config-container {
  padding: 0;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.config-key {
  font-family: monospace;
  font-weight: 500;
  color: #409EFF;
}
</style>
