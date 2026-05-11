<template>
  <div class="email-config-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">邮件配置</h2>
    </div>

    <!-- 配置表单 -->
    <el-form
      ref="formRef"
      :model="form"
      :rules="formRules"
      label-width="120px"
      label-position="right"
      v-loading="loading"
      style="max-width: 600px"
    >
      <el-form-item label="SMTP 服务器" prop="smtpHost">
        <el-input v-model="form.smtpHost" placeholder="如 smtp.qq.com" />
      </el-form-item>
      <el-form-item label="SMTP 端口" prop="smtpPort">
        <el-input-number v-model="form.smtpPort" :min="1" :max="65535" style="width: 100%" />
      </el-form-item>
      <el-form-item label="发件人邮箱" prop="username">
        <el-input v-model="form.username" placeholder="请输入发件人邮箱" />
      </el-form-item>
      <el-form-item label="授权码" prop="password">
        <el-input v-model="form.password" type="password" show-password placeholder="请输入授权码" />
      </el-form-item>
      <el-form-item label="发件人名称" prop="fromName">
        <el-input v-model="form.fromName" placeholder="请输入发件人名称" />
      </el-form-item>
      <el-form-item label="默认接收邮箱" prop="defaultToEmail">
        <el-input v-model="form.defaultToEmail" placeholder="请输入默认接收邮箱" />
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="form.enabled" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存配置</el-button>
      </el-form-item>
    </el-form>

    <!-- 测试发送区域 -->
    <el-card header="测试发送" style="max-width: 600px; margin-top: 24px;">
      <div class="test-send-area">
        <el-input
          v-model="testEmail"
          placeholder="请输入测试收件邮箱"
          style="flex: 1"
        />
        <el-button type="success" :loading="testLoading" @click="handleTestSend" style="margin-left: 12px;">
          发送测试邮件
        </el-button>
      </div>
      <div v-if="testResult" class="test-result" :class="testResult.success ? 'success' : 'error'">
        {{ testResult.message }}
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getEmailConfig, updateEmailConfig, testSendEmail } from '@/api/email'
import type { EmailConfig } from '@/api/email'

const loading = ref(false)
const saveLoading = ref(false)
const testLoading = ref(false)
const formRef = ref<FormInstance>()
const testEmail = ref('')
const testResult = ref<{ success: boolean; message: string } | null>(null)

// 表单数据
const form = reactive<EmailConfig>({
  smtpHost: '',
  smtpPort: 587,
  username: '',
  password: '',
  fromName: '',
  defaultToEmail: '',
  enabled: true,
})

// 表单验证规则
const formRules = reactive<FormRules>({
  smtpHost: [{ required: true, message: '请输入 SMTP 服务器', trigger: 'blur' }],
  smtpPort: [{ required: true, message: '请输入 SMTP 端口', trigger: 'blur' }],
  username: [
    { required: true, message: '请输入发件人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入授权码', trigger: 'blur' }],
  fromName: [{ required: true, message: '请输入发件人名称', trigger: 'blur' }],
  defaultToEmail: [
    { required: true, message: '请输入默认接收邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
})

// 加载配置
async function loadConfig() {
  loading.value = true
  try {
    const res: any = await getEmailConfig()
    if (res.data) {
      Object.assign(form, res.data)
    }
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 保存配置
async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saveLoading.value = true
    try {
      await updateEmailConfig({ ...form })
      ElMessage.success('配置保存成功')
    } catch {
      // 错误已在拦截器中处理
    } finally {
      saveLoading.value = false
    }
  })
}

// 测试发送
async function handleTestSend() {
  if (!testEmail.value) {
    ElMessage.warning('请输入测试收件邮箱')
    return
  }
  testLoading.value = true
  testResult.value = null
  try {
    await testSendEmail(testEmail.value)
    testResult.value = { success: true, message: '测试邮件发送成功！请检查收件箱。' }
  } catch {
    testResult.value = { success: false, message: '测试邮件发送失败，请检查配置是否正确。' }
  } finally {
    testLoading.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.email-config-page {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.test-send-area {
  display: flex;
  align-items: center;
}

.test-result {
  margin-top: 12px;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.test-result.success {
  background: #f0f9eb;
  color: #67c23a;
}

.test-result.error {
  background: #fef0f0;
  color: #f56c6c;
}
</style>
