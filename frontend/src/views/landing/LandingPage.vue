<template>
  <div class="landing-page">
    <header class="topbar">
      <router-link class="brand" to="/">
        <span class="brand-mark">📚</span>
        <span>智慧图书馆</span>
      </router-link>
      <nav class="nav-links">
        <a href="#product">产品能力</a>
        <a href="#experience">体验亮点</a>
        <a href="#about">关于平台</a>
      </nav>
      <div class="top-actions">
        <router-link class="text-link" to="/login">登录</router-link>
        <router-link class="primary-btn" to="/register">立即体验</router-link>
      </div>
    </header>

    <main>
      <section class="hero">
        <div class="hero-copy">
          <div class="eyebrow">AI 驱动 · 面向未来的数字图书馆</div>
          <h1>让每一次阅读，都从这里开始。</h1>
          <p>
            以极致体验为灵感，重构校园图书馆的服务方式，让借阅、预约、发现与管理都变得轻松、顺畅且富有温度。
          </p>
          <div class="hero-actions">
            <router-link class="primary-btn large" to="/reader-login">开始使用</router-link>
            <a class="secondary-btn large" href="#product">探索功能</a>
          </div>
          <div class="hero-trust">
            <span>✓ 24/7 自助服务</span>
            <span>✓ 智能推荐引擎</span>
            <span>✓ 统一后台运营</span>
          </div>
          <ul class="hero-highlights">
            <li>智能检索与推荐</li>
            <li>预约座位与续借提醒</li>
            <li>后台统计与运营洞察</li>
          </ul>
        </div>

        <div class="hero-visual">
          <div class="visual-card main-card">
            <div class="card-label">平台概览</div>
            <div class="card-title">一站式服务中心</div>
            <div class="card-metrics">
              <div>
                <strong>98%</strong>
                <span>服务响应</span>
              </div>
              <div>
                <strong>24/7</strong>
                <span>智能运维</span>
              </div>
              <div>
                <strong>200+</strong>
                <span>书籍分类</span>
              </div>
            </div>
            <div class="mini-progress">
              <div class="progress-row">
                <span>今日活跃</span>
                <strong>1,248</strong>
              </div>
              <div class="progress-bar"><i></i></div>
            </div>
          </div>
          <div class="visual-card floating-card">
            <div class="mini-pill">今日推荐</div>
            <h3>《未来已来》</h3>
            <p>根据读者兴趣动态推荐，帮助每位读者更快找到心仪书籍。</p>
          </div>
        </div>
      </section>

      <section class="stats" id="about">
        <div v-for="item in stats" :key="item.label" class="stat-card">
          <div class="stat-icon">{{ item.icon }}</div>
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </div>
      </section>

      <section class="showcase-banner">
        <div class="banner-copy">
          <div class="eyebrow">沉浸式体验</div>
          <h3>更轻量的操作，更清晰的服务路径。</h3>
          <p>从馆藏查询到座位预约，整个流程都被重新整理为更自然、更高效的体验。</p>
        </div>
        <div class="banner-points">
          <div>📖 轻松检索</div>
          <div>🪑 便捷预约</div>
          <div>📊 数据洞察</div>
        </div>
      </section>

      <section class="feature-section" id="product">
        <div class="section-heading">
          <div class="eyebrow">产品能力</div>
          <h2>从读者入口到后台运营，所有体验都被重新定义。</h2>
        </div>
        <div class="feature-grid">
          <div v-for="feature in features" :key="feature.title" class="feature-card">
            <div class="feature-icon">{{ feature.icon }}</div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.desc }}</p>
          </div>
        </div>
      </section>

      <section class="journey-section" id="experience">
        <div class="journey-copy">
          <div class="eyebrow">沉浸式体验</div>
          <h2>像顶级产品一样，顺滑、清晰、充满未来感。</h2>
          <p>
            我们将极简的视觉语言与强大的功能能力结合，让每一位访客都能快速找到自己想要的服务。
          </p>
        </div>
        <div class="journey-panel">
          <div class="panel-item">
            <span>01</span>
            <div>
              <h4>一眼看懂</h4>
              <p>清晰的导航与信息层级，减少使用门槛。</p>
            </div>
          </div>
          <div class="panel-item">
            <span>02</span>
            <div>
              <h4>随时可达</h4>
              <p>支持多端访问，随时随地完成借阅与预约。</p>
            </div>
          </div>
          <div class="panel-item">
            <span>03</span>
            <div>
              <h4>持续进化</h4>
              <p>围绕数据反馈持续优化体验与业务流程。</p>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const stats = ref([
  { value: '10K+', label: '月活跃读者', icon: '👥' },
  { value: '300+', label: '馆藏资源', icon: '📚' },
  { value: '99.9%', label: '系统稳定性', icon: '⚡' },
  { value: '4.9/5', label: '用户满意度', icon: '⭐' }
])

const features = ref([
  {
    title: '智能检索',
    desc: '支持关键词、分类、作者与主题快速定位，提升阅读发现效率。',
    icon: '🔍'
  },
  {
    title: '便捷借阅',
    desc: '一键预约、续借提醒、逾期提示，让服务更贴近使用习惯。',
    icon: '✨'
  },
  {
    title: '后台运营',
    desc: '图书管理、读者管理、报表分析与公告发布，统一管理。',
    icon: '🛠️'
  },
  {
    title: '开放体验',
    desc: '为校园用户和管理员分别打造专属入口，使用路径清晰。',
    icon: '🌐'
  }
])
</script>

<style scoped>
:global(body) {
  background: #060816;
}

.landing-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(86, 120, 255, 0.25), transparent 28%),
    linear-gradient(135deg, #060816 0%, #0d1632 45%, #0b1023 100%);
  color: #f4f7ff;
  padding: 24px 24px 60px;
}

.topbar {
  max-width: 1280px;
  margin: 0 auto 40px;
  padding: 18px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 999px;
  background: rgba(7, 11, 24, 0.7);
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1rem;
  font-weight: 700;
  color: #f4f7ff;
  text-decoration: none;
}

.brand-mark {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #4a7dff, #7f5cff);
  font-weight: 800;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-links a,
.text-link {
  color: rgba(244,247,255,0.8);
  text-decoration: none;
  font-size: 0.95rem;
}

.top-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.primary-btn,
.secondary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  text-decoration: none;
  transition: transform 0.2s ease, background 0.2s ease;
}

.primary-btn:hover,
.secondary-btn:hover {
  transform: translateY(-1px);
}

.primary-btn {
  background: linear-gradient(135deg, #4a7dff, #6e61ff);
  color: white;
  padding: 10px 18px;
  font-weight: 600;
}

.secondary-btn {
  color: #f4f7ff;
  padding: 10px 18px;
  border: 1px solid rgba(255,255,255,0.16);
  background: rgba(255,255,255,0.06);
}

.large {
  padding: 14px 24px;
  font-size: 1rem;
}

.hero {
  max-width: 1280px;
  margin: 0 auto;
  padding: 56px 12px 36px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 40px;
  align-items: center;
}

.eyebrow {
  display: inline-block;
  padding: 7px 12px;
  margin-bottom: 18px;
  border-radius: 999px;
  background: rgba(255,255,255,0.08);
  color: #8fb2ff;
  font-size: 0.9rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero h1 {
  font-size: clamp(2.3rem, 4.6vw, 4.4rem);
  line-height: 1.08;
  margin-bottom: 16px;
  font-weight: 800;
}

.hero p {
  font-size: 1.05rem;
  line-height: 1.8;
  color: rgba(244,247,255,0.75);
  max-width: 640px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin: 24px 0 20px;
}

.hero-trust {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 14px 0 18px;
}

.hero-trust span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.08);
  color: rgba(244,247,255,0.86);
  font-size: 0.92rem;
}

.hero-highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  list-style: none;
  padding: 0;
}

.hero-highlights li {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255,255,255,0.06);
  color: rgba(244,247,255,0.85);
  font-size: 0.95rem;
}

.hero-visual {
  position: relative;
  min-height: 420px;
}

.visual-card {
  position: absolute;
  border-radius: 28px;
  border: 1px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(20px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.main-card {
  inset: 0 30px 50px 0;
  padding: 28px;
}

.card-label {
  font-size: 0.8rem;
  color: #8fb2ff;
  text-transform: uppercase;
  letter-spacing: 0.18em;
}

.card-title {
  margin-top: 8px;
  font-size: 1.6rem;
  font-weight: 700;
}

.card-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 28px;
}

.card-metrics div {
  padding: 14px;
  border-radius: 18px;
  background: rgba(6, 10, 24, 0.4);
}

.card-metrics strong {
  display: block;
  font-size: 1.1rem;
  margin-bottom: 4px;
}

.card-metrics span {
  color: rgba(244,247,255,0.7);
  font-size: 0.9rem;
}

.floating-card {
  right: 0;
  bottom: 0;
  width: 240px;
  padding: 20px;
}

.mini-pill {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(74, 125, 255, 0.2);
  color: #a9c4ff;
  font-size: 0.78rem;
  margin-bottom: 12px;
}

.floating-card h3 {
  margin-bottom: 8px;
}

.floating-card p {
  color: rgba(244,247,255,0.7);
  line-height: 1.6;
}

.mini-progress {
  margin-top: 22px;
  padding: 16px;
  border-radius: 18px;
  background: rgba(6, 10, 24, 0.45);
}

.progress-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: rgba(244,247,255,0.82);
  font-size: 0.95rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  border-radius: 999px;
  background: rgba(255,255,255,0.12);
  overflow: hidden;
}

.progress-bar i {
  display: block;
  width: 72%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #4a7dff, #7f5cff);
}

.stats,
.feature-section,
.journey-section {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 12px 0;
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.stat-card,
.feature-card,
.journey-panel {
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 24px;
  padding: 22px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.16);
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-icon,
.feature-icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(74, 125, 255, 0.25), rgba(127, 92, 255, 0.25));
  font-size: 1.1rem;
}

.stat-card strong {
  display: block;
  font-size: 1.5rem;
  margin-bottom: 2px;
}

.stat-card span {
  color: rgba(244,247,255,0.7);
}

.section-heading {
  margin-bottom: 20px;
}

.section-heading h2,
.journey-copy h2 {
  font-size: clamp(1.5rem, 2.4vw, 2.2rem);
  margin-bottom: 10px;
}

.showcase-banner {
  max-width: 1280px;
  margin: 28px auto 0;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  border-radius: 28px;
  background: linear-gradient(135deg, rgba(74,125,255,0.2), rgba(127,92,255,0.16));
  border: 1px solid rgba(255,255,255,0.12);
  box-shadow: 0 18px 45px rgba(0,0,0,0.18);
}

.banner-copy h3 {
  margin-bottom: 8px;
  font-size: 1.25rem;
}

.banner-copy p {
  color: rgba(244,247,255,0.74);
  line-height: 1.7;
  margin: 0;
}

.banner-points {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.banner-points div {
  padding: 10px 12px;
  border-radius: 999px;
  background: rgba(255,255,255,0.08);
  color: rgba(244,247,255,0.88);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.feature-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feature-card h3 {
  margin-bottom: 4px;
  font-size: 1.15rem;
}

.feature-card p,
.journey-copy p,
.panel-item p {
  color: rgba(244,247,255,0.72);
  line-height: 1.7;
}

.journey-section {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 20px;
  align-items: start;
  padding-top: 36px;
}

.panel-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.panel-item:last-child {
  border-bottom: none;
}

.panel-item span {
  color: #8fb2ff;
  font-weight: 700;
}

.panel-item h4 {
  margin-bottom: 6px;
}

@media (max-width: 960px) {
  .topbar {
    border-radius: 24px;
    flex-wrap: wrap;
    gap: 10px;
  }

  .hero,
  .journey-section,
  .feature-grid,
  .stats,
  .showcase-banner {
    grid-template-columns: 1fr;
  }

  .hero-visual {
    min-height: 340px;
  }

  .main-card {
    inset: 0 0 70px 0;
  }
}

@media (max-width: 640px) {
  .landing-page {
    padding: 16px 14px 40px;
  }

  .topbar {
    padding: 14px 16px;
  }

  .nav-links {
    display: none;
  }

  .hero {
    padding: 30px 0 0;
  }

  .showcase-banner {
    padding: 18px;
    flex-direction: column;
    align-items: flex-start;
  }

  .card-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
