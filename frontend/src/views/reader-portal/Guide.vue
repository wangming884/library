<template>
  <div class="guide-page-container">
    <!-- 头部横幅 Banner -->
    <div class="guide-banner">
      <div class="banner-tag">读者指引中心</div>
      <h1>入馆服务与空间指南</h1>
      <p class="banner-desc">
        全面了解本馆借阅规则、借期政策、楼层功能规划与便民服务设施，助力高效研学。
      </p>
    </div>

    <!-- 核心导航标签页 -->
    <el-card shadow="never" class="guide-card">
      <el-tabs v-model="activeTab" class="guide-tabs">
        <!-- 标签 1: 借阅与续借规则 -->
        <el-tab-pane name="borrow">
          <template #label>
            <span class="tab-label-text">
              <el-icon><Reading /></el-icon> 借阅与续借规则
            </span>
          </template>

          <div class="tab-content-wrap">
            <div class="info-block">
              <h3 class="block-title">
                <el-icon color="#2563eb"><UserFilled /></el-icon>
                <span>读者类型与借阅额度</span>
              </h3>
              <el-table :data="readerTypeData" stripe border class="quota-table">
                <el-table-column prop="type" label="读者类型" width="160" />
                <el-table-column prop="quota" label="最大可借册数" width="140" align="center">
                  <template #default="{ row }">
                    <el-tag type="primary" size="small">{{ row.quota }} 册</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="period" label="基础借阅期限" width="140" align="center">
                  <template #default="{ row }">
                    <span>{{ row.period }} 天</span>
                  </template>
                </el-table-column>
                <el-table-column prop="renewCount" label="允许续借次数" width="140" align="center">
                  <template #default="{ row }">
                    <span>{{ row.renewCount }} 次</span>
                  </template>
                </el-table-column>
                <el-table-column prop="remark" label="说明备注" />
              </el-table>
            </div>

            <div class="info-block">
              <h3 class="block-title">
                <el-icon color="#10b981"><Clock /></el-icon>
                <span>图书线上续借细则</span>
              </h3>
              <div class="rules-grid">
                <div class="rule-box">
                  <div class="rule-num">01</div>
                  <h4>续借时间窗口</h4>
                  <p>在图书借期到期前 <strong>7 天内</strong> 方可发起线上续借，过早将无法激活续借按钮。</p>
                </div>
                <div class="rule-box">
                  <div class="rule-num">02</div>
                  <h4>续借期限与次数</h4>
                  <p>每本图书在借阅期内限续借 <strong>1 次</strong>，续借成功后借阅期限顺延 <strong>30 天</strong>。</p>
                </div>
                <div class="rule-box">
                  <div class="rule-num">03</div>
                  <h4>不可续借情形</h4>
                  <p>图书已被其他读者线上预约、读者账户存在逾期未还图书或违约金欠款时，不可办理续借。</p>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 标签 2: 违约与超期政策 -->
        <el-tab-pane name="fines">
          <template #label>
            <span class="tab-label-text">
              <el-icon><Warning /></el-icon> 违约与逾期政策
            </span>
          </template>

          <div class="tab-content-wrap">
            <div class="notice-callout">
              <el-icon size="20" color="#e6a23c"><Bell /></el-icon>
              <span>图书为公共文化资源，请广大读者自觉按期归还，保持良好信用记录。</span>
            </div>

            <div class="policy-section">
              <div class="policy-card">
                <h4>📌 逾期违约金计费规则</h4>
                <p>借出图书若超过应还日期仍未归还，系统将自超期次日起按 <strong>0.10 元 / 天 / 本</strong> 自动累计逾期违约金。</p>
              </div>

              <div class="policy-card">
                <h4>💳 违约金缴纳与权限恢复</h4>
                <p>当账户产生欠款时，读者的借书与自习选座权限将临时冻结。读者可登录【个人借阅中心】通过线上模拟支付结清，结清后权限秒级恢复。</p>
              </div>

              <div class="policy-card">
                <h4>📦 文献污损与遗失处理办法</h4>
                <p>如发生图书遗失或严重损坏涂划，读者应优先购买同版次全新图书赔偿并交纳加工费；无法购得同书时，按文献出版年限与定价的 2~5 倍予以赔偿。</p>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 标签 3: 全馆楼层空间导览 -->
        <el-tab-pane name="floors">
          <template #label>
            <span class="tab-label-text">
              <el-icon><Location /></el-icon> 楼层空间导览
            </span>
          </template>

          <div class="tab-content-wrap">
            <div class="floors-container">
              <div class="floor-item-card" v-for="fl in floorList" :key="fl.floor">
                <div class="floor-badge-col">
                  <div class="floor-number">{{ fl.floor }}</div>
                  <span class="floor-name-tag">{{ fl.name }}</span>
                </div>
                <div class="floor-info-col">
                  <div class="floor-header-row">
                    <h3>{{ fl.title }}</h3>
                    <el-tag size="small" type="success" effect="light">{{ fl.status }}</el-tag>
                  </div>
                  <p class="floor-desc">{{ fl.description }}</p>
                  <div class="floor-facilities">
                    <span class="facility-title">核心区域与设施：</span>
                    <div class="facility-tags">
                      <span v-for="tag in fl.facilities" :key="tag" class="facility-chip">{{ tag }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 标签 4: 开放时间与便民服务 -->
        <el-tab-pane name="facilities">
          <template #label>
            <span class="tab-label-text">
              <el-icon><OfficeBuilding /></el-icon> 开馆时间与便民设施
            </span>
          </template>

          <div class="tab-content-wrap">
            <div class="facilities-grid">
              <div class="facility-box">
                <div class="fac-icon icon-blue"><el-icon><Clock /></el-icon></div>
                <h4>开放时间</h4>
                <p>周一至周日：08:00 - 22:00</p>
                <p>法定节假日：09:00 - 17:00</p>
              </div>

              <div class="facility-box">
                <div class="fac-icon icon-emerald"><el-icon><Printer /></el-icon></div>
                <h4>自助打印与复印</h4>
                <p>全馆 1F 与 3F 配备自助云打印一体机，支持手机扫码快速文档打印。</p>
              </div>

              <div class="facility-box">
                <div class="fac-icon icon-amber"><el-icon><Lock /></el-icon></div>
                <h4>智能储物柜</h4>
                <p>1F 门厅西侧设智能储物柜 120 组，读者凭借书证扫码免费暂存随身书包。</p>
              </div>

              <div class="facility-box">
                <div class="fac-icon icon-purple"><el-icon><Coffee /></el-icon></div>
                <h4>温水供应与休息区</h4>
                <p>每层洗手间外侧均配备直饮水机；2F 设有静音阅读咖啡休憩吧。</p>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 标签 5: 常见问题 FAQ -->
        <el-tab-pane name="faq">
          <template #label>
            <span class="tab-label-text">
              <el-icon><QuestionFilled /></el-icon> 读者常见问题 (FAQ)
            </span>
          </template>

          <div class="tab-content-wrap">
            <el-collapse v-model="activeFaq" class="faq-collapse">
              <el-collapse-item title="Q1：借书证密码遗忘了如何找回？" name="1">
                <p>读者可通过登录页点击联系管理员重置，或携带本人有效身份证件/学生证前往 1F 总服务台人工办理密码重置手续。</p>
              </el-collapse-item>
              <el-collapse-item title="Q2：在网上预约的图书能保留多长时间？" name="2">
                <p>当您预约的图书归还到馆后，系统将自动向您发送到书通知。预约图书将在 1F 总服务台为您保留 <strong>3 天</strong>，逾期未取将自动释放给下一位读者。</p>
              </el-collapse-item>
              <el-collapse-item title="Q3：自习室座位预约后临时有事离开怎么办？" name="3">
                <p>读者可在个人中心点击「暂离」模式，系统将为您保留座位 <strong>60 分钟</strong>；如需彻底离开请及时点击「释放座位」，避免因违约记入黑名单。</p>
              </el-collapse-item>
              <el-collapse-item title="Q4：馆内没有我需要的专业图书，可以申请购买吗？" name="4">
                <p>可以！读者可直接进入【读者留言】栏目选择「好书荐购」，提交图书题名、作者与 ISBN，采编部门将定期评估并纳入图书采购计划。</p>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  Reading,
  UserFilled,
  Clock,
  Warning,
  Bell,
  Location,
  OfficeBuilding,
  Printer,
  Lock,
  Coffee,
  QuestionFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const activeTab = ref(route.query.tab || 'borrow')

watch(
  () => route.query.tab,
  (newTab) => {
    if (newTab) {
      activeTab.value = newTab
    }
  }
)
const activeFaq = ref(['1', '3'])

const readerTypeData = [
  { type: '普通读者 / 本科生', quota: 10, period: 30, renewCount: 1, remark: '可线上续借 30 天，无不良信用记录' },
  { type: '研究生 / 专硕学硕', quota: 15, period: 45, renewCount: 1, remark: '论文开题与研习保障' },
  { type: '教工 / 教学科研人员', quota: 25, period: 60, renewCount: 2, remark: '教学备课与学术课题研究支持' },
  { type: '校外访问学者 / 嘉宾', quota: 5, period: 15, renewCount: 0, remark: '仅限阅览与短期借阅' }
]

const floorList = [
  {
    floor: '1F',
    name: '综合服务层',
    title: '读者总服务大厅 · 自助借还中心 · 新书展区',
    status: '开放中',
    description: '读者入馆的第一站，提供一站式读者咨询、人工办证、自助借还书机、新书展示台与密集储物柜。',
    facilities: ['读者总台', '自助借还书机 (4台)', '新书展示专架', '还书箱', '智能储物柜', '开水间']
  },
  {
    floor: '2F',
    name: '社科人文层',
    title: '文学艺术阅览区 · 社科历史典籍 · 小组研讨室',
    status: '开放中',
    description: '涵盖文学、历史、哲学、艺术、语言类纸质文献，配备 4 间玻璃隔断学术讨论室与咖啡书吧。',
    facilities: ['中外文学图书', '历史哲学典籍', '学术研讨室 A/B/C/D', '景观阅览桌', '咖啡书吧']
  },
  {
    floor: '3F',
    name: '科技文献层',
    title: '自然科学文献 · 计算机与AI · 电子阅览大厅',
    status: '开放中',
    description: '藏有数理化、计算机科学、工程技术与经济金融图书，内设配备百兆网络的数字电子检索大厅。',
    facilities: ['计算机/软件专架', '理工工程典籍', '百人电子阅览大厅', '云打印一体机', '电源插座长桌']
  },
  {
    floor: '4F',
    name: '特藏自习层',
    title: '珍贵古籍特藏室 · 考研沉浸式静音自习区',
    status: '静音专注区',
    description: '古籍善本与珍藏文献特约阅览；全区为绝对静音自习区，配有独立护眼灯与隔板自习舱。',
    facilities: ['古籍特藏善本', '沉浸式静音自习室', '独立隔板自习舱', '个人电源接口', '严禁发声区']
  }
]
</script>

<style scoped>
.guide-page-container {
  width: 100%;
}

.guide-banner {
  background: linear-gradient(135deg, #091a32 0%, #173868 45%, #1d4ed8 100%);
  border-radius: 18px;
  padding: 44px 32px;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.15);
}

.banner-tag {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: #93c5fd;
  background: rgba(255, 255, 255, 0.14);
  padding: 3px 12px;
  border-radius: 20px;
  margin-bottom: 12px;
}

.guide-banner h1 {
  font-size: 30px;
  font-weight: 800;
  margin: 0 0 10px;
}

.banner-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  max-width: 720px;
  line-height: 1.6;
}

.guide-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  padding: 10px 16px;
}

.tab-label-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  padding: 4px 8px;
}

.tab-content-wrap {
  padding: 20px 8px 30px;
}

.info-block {
  margin-bottom: 36px;
}

.block-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 16px;
}

.quota-table {
  border-radius: 8px;
  overflow: hidden;
}

.rules-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.rule-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px 20px;
  position: relative;
}

.rule-num {
  font-size: 28px;
  font-weight: 800;
  color: #3b82f6;
  opacity: 0.35;
  margin-bottom: 8px;
  font-family: 'DIN Alternate', sans-serif;
}

.rule-box h4 {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.rule-box p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}

.notice-callout {
  background: #fffbeb;
  border: 1px solid #fef3c7;
  border-radius: 10px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  color: #92400e;
  font-size: 14px;
  font-weight: 500;
}

.policy-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.policy-card {
  background: #f8fafc;
  border-left: 4px solid #2563eb;
  border-radius: 0 12px 12px 0;
  padding: 18px 22px;
}

.policy-card h4 {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px;
}

.policy-card p {
  font-size: 13px;
  color: #475569;
  line-height: 1.7;
  margin: 0;
}

/* 楼层导览 */
.floors-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.floor-item-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 24px;
  display: flex;
  gap: 24px;
  align-items: flex-start;
  transition: all 0.2s;
}

.floor-item-card:hover {
  background: #fff;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  border-color: #cbd5e1;
}

.floor-badge-col {
  width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.floor-number {
  width: 60px;
  height: 60px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1e40af, #3b82f6);
  color: #fff;
  font-size: 24px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
}

.floor-name-tag {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
}

.floor-info-col {
  flex: 1;
}

.floor-header-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.floor-header-row h3 {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.floor-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 14px;
}

.floor-facilities {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.facility-title {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}

.facility-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.facility-chip {
  background: #eff6ff;
  color: #2563eb;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

/* 便民设施 */
.facilities-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.facility-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 24px 20px;
  text-align: center;
}

.fac-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 14px;
}

.icon-blue { background: #eff6ff; color: #2563eb; }
.icon-emerald { background: #ecfdf5; color: #059669; }
.icon-amber { background: #fffbeb; color: #d97706; }
.icon-purple { background: #faf5ff; color: #9333ea; }

.facility-box h4 {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px;
}

.facility-box p {
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 4px;
}

.faq-collapse {
  border-top: none;
}

.faq-collapse :deep(.el-collapse-item__header) {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  padding: 16px 0;
}

.faq-collapse p {
  font-size: 13px;
  color: #64748b;
  line-height: 1.7;
  margin: 0;
  padding-left: 4px;
}

@media (max-width: 900px) {
  .rules-grid, .facilities-grid {
    grid-template-columns: 1fr 1fr;
  }
  .floor-item-card {
    flex-direction: column;
  }
}

@media (max-width: 600px) {
  .rules-grid, .facilities-grid {
    grid-template-columns: 1fr;
  }
}
</style>
