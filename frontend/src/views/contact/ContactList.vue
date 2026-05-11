<template>
  <div class="contact-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">联系人管理</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索联系人姓名"
          :prefix-icon="Search"
          clearable
          style="width: 240px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增联系人</el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="name" label="姓名" min-width="100" />
      <el-table-column prop="relationship" label="关系" min-width="80" />
      <el-table-column label="生日类型" min-width="90">
        <template #default="{ row }">
          <el-tag :type="row.birthdayType === 'SOLAR' ? 'primary' : 'warning'" size="small">
            {{ row.birthdayType === 'SOLAR' ? '公历' : '农历' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="生日日期" min-width="100">
        <template #default="{ row }">
          <span v-if="row.birthdayType === 'SOLAR'">{{ row.birthdayDate }}</span>
          <span v-else>{{ formatLunarDate(row.lunarMonth, row.lunarDay, row.isLeapMonth) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="notifyEmail" label="接收邮箱" min-width="180" />
      <el-table-column label="状态" min-width="80">
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            @change="handleToggleEnabled(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑联系人' : '新增联系人'"
      width="550px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="关系" prop="relationship">
          <el-select v-model="form.relationship" placeholder="请选择关系" style="width: 100%">
            <el-option v-for="item in relationshipOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日类型" prop="birthdayType">
          <el-radio-group v-model="form.birthdayType" @change="handleBirthdayTypeChange">
            <el-radio value="SOLAR">公历</el-radio>
            <el-radio value="LUNAR">农历</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.birthdayType === 'SOLAR'" label="公历日期" prop="birthdayDate">
          <el-date-picker
            v-model="solarDateValue"
            type="date"
            placeholder="选择日期"
            format="MM-DD"
            value-format="MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <template v-if="form.birthdayType === 'LUNAR'">
          <el-form-item label="农历月份" prop="lunarMonth">
            <el-select v-model="form.lunarMonth" placeholder="请选择月份" style="width: 100%">
              <el-option
                v-for="item in lunarMonthOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="农历日" prop="lunarDay">
            <el-select v-model="form.lunarDay" placeholder="请选择日" style="width: 100%">
              <el-option
                v-for="item in lunarDayOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="闰月">
            <el-checkbox v-model="form.isLeapMonth">是闰月</el-checkbox>
          </el-form-item>
        </template>
        <el-form-item label="接收邮箱" prop="notifyEmail">
          <el-input v-model="form.notifyEmail" placeholder="请输入接收邮箱" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getContacts, createContact, updateContact, deleteContact } from '@/api/contact'
import type { Contact } from '@/api/contact'

// 农历月份名称
const lunarMonthNames = ['正月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '腊月']

// 农历日名称
const lunarDayNames = [
  '初一', '初二', '初三', '初四', '初五', '初六', '初七', '初八', '初九', '初十',
  '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十',
  '廿一', '廿二', '廿三', '廿四', '廿五', '廿六', '廿七', '廿八', '廿九', '三十',
]

// 格式化农历日期显示
function formatLunarDate(month?: number, day?: number, isLeapMonth?: boolean): string {
  if (!month || !day) return ''
  const prefix = isLeapMonth ? '闰' : ''
  return `${prefix}${lunarMonthNames[month - 1]}${lunarDayNames[day - 1]}`
}

// 关系选项
const relationshipOptions = ['父母', '朋友', '同学', '同事', '恋人', '亲戚', '其他']

// 农历月份选项
const lunarMonthOptions = lunarMonthNames.map((label, index) => ({
  label,
  value: index + 1,
}))

// 农历日选项
const lunarDayOptions = lunarDayNames.map((label, index) => ({
  label,
  value: index + 1,
}))

// 列表相关
const tableData = ref<Contact[]>([])
const loading = ref(false)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | undefined>()

// 公历日期值（中间变量）
const solarDateValue = computed({
  get: () => form.birthdayDate || '',
  set: (val: string) => {
    form.birthdayDate = val
  },
})

// 表单数据
const form = reactive<Contact>({
  name: '',
  relationship: '',
  birthdayType: 'SOLAR',
  birthdayDate: '',
  lunarMonth: undefined,
  lunarDay: undefined,
  isLeapMonth: false,
  enabled: true,
  notifyEmail: '',
  remark: '',
})

// 表单验证规则
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  relationship: [{ required: true, message: '请选择关系', trigger: 'change' }],
  birthdayType: [{ required: true, message: '请选择生日类型', trigger: 'change' }],
  birthdayDate: [{ required: true, message: '请选择公历日期', trigger: 'change' }],
  lunarMonth: [{ required: true, message: '请选择农历月份', trigger: 'change' }],
  lunarDay: [{ required: true, message: '请选择农历日', trigger: 'change' }],
  notifyEmail: [
    { required: true, message: '请输入接收邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
})

// 加载列表数据
async function loadData() {
  loading.value = true
  try {
    const res: any = await getContacts({
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: keyword.value || undefined,
    })
    const data = res.data
    tableData.value = data.content || []
    total.value = data.totalElements || 0
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  loadData()
}

// 分页
function handleSizeChange() {
  currentPage.value = 1
  loadData()
}

function handleCurrentChange() {
  loadData()
}

// 新增
function handleAdd() {
  isEdit.value = false
  editingId.value = undefined
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: Contact) {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    relationship: row.relationship,
    birthdayType: row.birthdayType,
    birthdayDate: row.birthdayDate || '',
    lunarMonth: row.lunarMonth,
    lunarDay: row.lunarDay,
    isLeapMonth: row.isLeapMonth || false,
    enabled: row.enabled,
    notifyEmail: row.notifyEmail,
    remark: row.remark || '',
  })
  dialogVisible.value = true
}

// 切换生日类型时清除另一种类型的数据
function handleBirthdayTypeChange(val: string | number | boolean) {
  if (val === 'SOLAR') {
    form.lunarMonth = undefined
    form.lunarDay = undefined
    form.isLeapMonth = false
  } else {
    form.birthdayDate = ''
  }
}

// 切换启用状态
async function handleToggleEnabled(row: Contact) {
  try {
    await updateContact(row.id!, { ...row })
    ElMessage.success(row.enabled ? '已启用' : '已禁用')
  } catch {
    row.enabled = !row.enabled
  }
}

// 删除
function handleDelete(row: Contact) {
  ElMessageBox.confirm(`确定要删除联系人"${row.name}"吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteContact(row.id!)
      ElMessage.success('删除成功')
      loadData()
    } catch {
      // 错误已在拦截器中处理
    }
  }).catch(() => {
    // 用户取消
  })
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const submitData: Contact = {
        name: form.name,
        relationship: form.relationship,
        birthdayType: form.birthdayType,
        birthdayDate: form.birthdayType === 'SOLAR' ? form.birthdayDate : undefined,
        lunarMonth: form.birthdayType === 'LUNAR' ? form.lunarMonth : undefined,
        lunarDay: form.birthdayType === 'LUNAR' ? form.lunarDay : undefined,
        isLeapMonth: form.birthdayType === 'LUNAR' ? form.isLeapMonth : false,
        enabled: form.enabled,
        notifyEmail: form.notifyEmail,
        remark: form.remark,
      }
      if (isEdit.value && editingId.value) {
        await updateContact(editingId.value, submitData)
        ElMessage.success('更新成功')
      } else {
        await createContact(submitData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch {
      // 错误已在拦截器中处理
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    name: '',
    relationship: '',
    birthdayType: 'SOLAR',
    birthdayDate: '',
    lunarMonth: undefined,
    lunarDay: undefined,
    isLeapMonth: false,
    enabled: true,
    notifyEmail: '',
    remark: '',
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.contact-page {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
