<template>
  <div class="landing-page">
    <!-- 1. 顶部全局导航栏 (磨砂玻璃悬浮) -->
    <header class="landing-header">
      <div class="header-container">
        <!-- 品牌标识 -->
        <router-link class="brand-badge" to="/">
          <div class="brand-logo-icon">
            <el-icon size="22" color="#ffffff"><Reading /></el-icon>
          </div>
          <div class="brand-text-wrap">
            <span class="brand-title">智慧图书馆</span>
            <span class="brand-subtitle">SMART DIGITAL LIBRARY</span>
          </div>
        </router-link>

        <!-- 开馆状态微标 -->
        <div class="status-ticker-badge">
          <span class="status-dot-pulse"></span>
          <span class="status-info">今日正常开馆 08:00 - 22:00</span>
          <span class="status-pipe">|</span>
          <span class="status-location">{{ currentClockTime || '四层全功能研学空间' }}</span>
        </div>

        <!-- 导航菜单 -->
        <nav class="landing-nav">
          <router-link to="/" class="nav-item active">首页</router-link>
          <router-link to="/portal/search" class="nav-item">馆藏检索</router-link>
          <router-link to="/portal/rank" class="nav-item">借阅排行</router-link>
          <router-link to="/portal/seat" class="nav-item">空间选座</router-link>
          <router-link to="/portal/news" class="nav-item">馆务资讯</router-link>
          <router-link to="/portal/guide" class="nav-item">入馆指南</router-link>
          <router-link to="/portal/feedback" class="nav-item">读者留言</router-link>
        </nav>

        <!-- 快捷登录/操作区 -->
        <div class="header-actions">
          <router-link to="/reader-login" class="action-btn-reader">
            <el-icon><User /></el-icon>
            <span>读者登录</span>
          </router-link>
          <router-link to="/register" class="action-btn-register">
            <span>办借书证</span>
          </router-link>
          <router-link to="/login" class="action-link-admin">
            <span>管理后台</span>
          </router-link>
        </div>
      </div>
    </header>

    <main class="landing-main">
      <!-- 2. 大气恢弘的文献探索中心 (Hero Section) -->
      <section class="hero-section">
        <div class="hero-backdrop">
          <div class="ambient-glow glow-blue"></div>
          <div class="ambient-glow glow-gold"></div>
          <div class="ambient-glow glow-purple"></div>
          <div class="grid-overlay"></div>
        </div>

        <div class="hero-content-wrap">
          <!-- 滚动公告跑马灯条 -->
          <div class="announcement-marquee-pill" @click="handleMarqueeClick">
            <span class="marquee-tag">动态通告</span>
            <div class="marquee-text-scroll">
              <span class="marquee-text">
                📢 {{ activeAnnouncementText }}
              </span>
            </div>
            <span class="marquee-arrow">查看详情 →</span>
          </div>

          <!-- 主标题 -->
          <h1 class="hero-main-title">
            汇聚人类典籍之美 <span class="title-gradient">赋能数字研学未来</span>
          </h1>
          <p class="hero-subtitle-desc">
            全馆 50,000+ 纸电文献毫秒级精准检索 · 4大主题空间 3D 在线选座 · 借还逾期全周期智能通知
          </p>

          <!-- 核心多维学术检索控制台 -->
          <div class="hero-search-card">
            <!-- 检索维度选项卡 -->
            <div class="search-fields-tabs">
              <div class="search-tab-list">
                <button
                  v-for="tab in searchTabs"
                  :key="tab.key"
                  type="button"
                  class="search-tab-btn"
                  :class="{ active: activeSearchType === tab.key }"
                  @click="activeSearchType = tab.key"
                >
                  <span class="tab-icon">{{ tab.icon }}</span>
                  <span>{{ tab.label }}</span>
                </button>
              </div>
              <div class="only-available-filter" @click="onlyAvailable = !onlyAvailable">
                <span class="filter-chk" :class="{ active: onlyAvailable }">
                  <span v-if="onlyAvailable">✓</span>
                </span>
                <span class="filter-text">仅看在馆可借</span>
              </div>
            </div>

            <!-- 输入框与检索按钮 -->
            <div class="search-input-group">
              <div class="input-prefix-icon">
                <el-icon size="20" color="#2563eb"><Search /></el-icon>
              </div>
              <input
                v-model="searchKeyword"
                type="text"
                class="hero-native-input"
                :placeholder="currentSearchPlaceholder"
                @keyup.enter="handleHeroSearch"
              />
              <button
                v-if="searchKeyword"
                type="button"
                class="search-clear-btn"
                @click="searchKeyword = ''"
              >
                ✕
              </button>
              <button
                type="button"
                class="hero-submit-btn"
                @click="handleHeroSearch"
              >
                <span>探索馆藏</span>
                <el-icon><ArrowRight /></el-icon>
              </button>
            </div>

            <!-- 热门搜索推荐词 -->
            <div class="hot-tags-row">
              <span class="hot-tags-label">
                <el-icon color="#f59e0b"><Trophy /></el-icon>
                <span>高频借阅：</span>
              </span>
              <div class="hot-chips-list">
                <button
                  v-for="tag in hotKeywords"
                  :key="tag"
                  type="button"
                  class="hot-chip-pill"
                  @click="quickSearchTag(tag)"
                >
                  {{ tag }}
                </button>
              </div>
            </div>
          </div>

          <!-- 实时馆情数字态势仪表盘 -->
          <div class="hero-stats-strip">
            <div class="stat-box">
              <div class="stat-visual icon-blue">📚</div>
              <div class="stat-info">
                <div class="stat-val">52,800<span class="stat-plus">+</span></div>
                <div class="stat-lbl">纸电馆藏图书 (册)</div>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-box">
              <div class="stat-visual icon-amber">🪑</div>
              <div class="stat-info">
                <div class="stat-val">356<span class="stat-unit">席</span></div>
                <div class="stat-lbl">空间自习研学席位</div>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-box">
              <div class="stat-visual icon-emerald">🔄</div>
              <div class="stat-info">
                <div class="stat-val">12,850<span class="stat-plus">+</span></div>
                <div class="stat-lbl">累计借还流通 (人次)</div>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-box">
              <div class="stat-visual icon-purple">⭐</div>
              <div class="stat-info">
                <div class="stat-val">99.2<span class="stat-unit">%</span></div>
                <div class="stat-lbl">读者综合好评率</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 3. 八大核心读者高频服务矩阵 -->
      <section class="services-section">
        <div class="section-head-center">
          <span class="section-kicker">快捷通道</span>
          <h2 class="section-title-text">全天候数字化读者服务大厅</h2>
          <p class="section-subtitle-text">打通全流程借阅与空间研习通道，各功能独立成页、深度体验</p>
        </div>

        <div class="services-matrix-grid">
          <div
            v-for="service in serviceCards"
            :key="service.title"
            class="service-metro-card"
            :class="service.themeClass"
            @click="router.push(service.path)"
          >
            <div class="service-card-top">
              <div class="service-badge-icon">
                <component :is="service.icon" />
              </div>
              <span class="service-code-tag">{{ service.tag }}</span>
            </div>
            <h3 class="service-card-title">{{ service.title }}</h3>
            <p class="service-card-desc">{{ service.desc }}</p>
            <div class="service-card-footer">
              <span class="service-action-hint">立即进入</span>
              <span class="service-action-arrow">→</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 4. 典籍流芳 · 馆藏好书与最新入藏画廊 -->
      <section class="books-showcase-section">
        <div class="showcase-header">
          <div class="showcase-header-left">
            <span class="section-kicker">典籍精选</span>
            <h2 class="section-title-text">馆藏好书与新书入藏</h2>
            <p class="section-subtitle-text">精选馆藏高借阅榜单与最新编目入库图书，支持查看在馆状态与索书号</p>
          </div>
          <div class="showcase-tabs">
            <button
              v-for="tab in bookTabs"
              :key="tab.key"
              type="button"
              class="book-tab-pill"
              :class="{ active: activeBookTab === tab.key }"
              @click="switchBookTab(tab.key)"
            >
              {{ tab.label }}
            </button>
            <router-link to="/portal/rank" class="view-more-books-link">
              查看完整榜单专页 →
            </router-link>
          </div>
        </div>

        <!-- 图书卡片网格 -->
        <div class="books-gallery-grid">
          <div
            v-for="book in currentDisplayBooks"
            :key="book.id"
            class="book-showcase-card"
            @click="handleBookClick(book)"
          >
            <!-- 3D 书脊与封皮效果 -->
            <div class="book-3d-wrap">
              <div class="book-cover-container">
                <el-image
                  :src="book.cover"
                  fit="cover"
                  class="book-cover-img"
                  lazy
                >
                  <template #error>
                    <div class="book-cover-fallback" :style="{ background: book.themeColor || '#1e3a8a' }">
                      <el-icon size="32" color="rgba(255,255,255,0.7)"><Reading /></el-icon>
                      <span class="fallback-text">{{ book.title }}</span>
                    </div>
                  </template>
                </el-image>
                <div class="book-status-badge" :class="book.availableCount > 0 ? 'badge-in' : 'badge-out'">
                  {{ book.availableCount > 0 ? ('在馆可借 (' + book.availableCount + ')') : '已被借出' }}
                </div>
                <div class="book-hover-actions">
                  <button type="button" class="hover-btn hover-btn-primary" @click.stop="handleQuickBorrow(book)">
                    {{ book.availableCount > 0 ? '立即借阅' : '预约排队' }}
                  </button>
                  <button type="button" class="hover-btn hover-btn-secondary" @click.stop="handleBookClick(book)">
                    查看详情
                  </button>
                </div>
              </div>
            </div>

            <!-- 图书文字元信息 -->
            <div class="book-meta-info">
              <div class="book-category-tag">
                {{ book.categoryName || '精品文献' }}
              </div>
              <h4 class="book-title-heading" :title="book.title">{{ book.title }}</h4>
              <p class="book-author-text" :title="book.author">著者：{{ book.author || '未知作者' }}</p>
              <div class="book-callno-row">
                <span class="callno-label">索书号：</span>
                <code class="callno-code">{{ book.callNumber || 'TP312/2026' }}</code>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 5. 馆务动态与学术活动双列专区 -->
      <section class="news-events-section">
        <el-row :gutter="32">
          <!-- 左列：馆务动态公告 -->
          <el-col :xs="24" :lg="14">
            <div class="news-panel-card">
              <div class="panel-card-head">
                <div class="panel-head-title">
                  <div class="icon-chip blue-chip">
                    <el-icon><Bell /></el-icon>
                  </div>
                  <div>
                    <h3 class="panel-main-heading">馆务要闻与通告</h3>
                    <p class="panel-sub-heading">馆务决策、闭馆通告及借阅服务政策速递</p>
                  </div>
                </div>
                <router-link to="/portal/news" class="panel-more-link">
                  全部资讯 →
                </router-link>
              </div>

              <div class="news-items-list">
                <div
                  v-for="item in announcementList"
                  :key="item.id"
                  class="news-item-row"
                  @click="openAnnouncement(item)"
                >
                  <span class="news-type-pill" :class="getNoticeClass(item.type)">
                    {{ getNoticeTypeLabel(item.type) }}
                  </span>
                  <div class="news-title-desc">
                    <h5 class="news-item-title" :title="item.title">{{ item.title }}</h5>
                    <p class="news-item-preview">{{ item.summary || (item.content ? item.content.slice(0, 48) : '请点击查看详情与政策全文...') }}</p>
                  </div>
                  <div class="news-item-meta">
                    <span class="news-date">{{ formatDate(item.publishTime || item.createTime) }}</span>
                    <span class="news-read-arrow">阅读 →</span>
                  </div>
                </div>
              </div>
            </div>
          </el-col>

          <!-- 右列：学术讲座与文化沙龙 -->
          <el-col :xs="24" :lg="10">
            <div class="events-panel-card">
              <div class="panel-card-head">
                <div class="panel-head-title">
                  <div class="icon-chip amber-chip">
                    <el-icon><Calendar /></el-icon>
                  </div>
                  <div>
                    <h3 class="panel-main-heading">学术讲座与文化活动</h3>
                    <p class="panel-sub-heading">启迪思想，共赴书香精神盛宴</p>
                  </div>
                </div>
                <span class="status-live-chip">火热进行中</span>
              </div>

              <div class="events-cards-list">
                <div
                  v-for="evt in culturalEvents"
                  :key="evt.title"
                  class="event-card-item"
                  @click="openEventModal(evt)"
                >
                  <div class="event-date-badge">
                    <span class="evt-month">{{ evt.month }}</span>
                    <span class="evt-day">{{ evt.day }}</span>
                  </div>
                  <div class="event-detail">
                    <div class="event-tag-status">
                      <span class="evt-category">{{ evt.category }}</span>
                      <span class="evt-status-pill" :class="evt.statusClass">{{ evt.status }}</span>
                    </div>
                    <h4 class="event-title-text">{{ evt.title }}</h4>
                    <p class="event-speaker">主讲人：{{ evt.speaker }}</p>
                    <p class="event-location"><el-icon><Location /></el-icon> {{ evt.location }}</p>
                    <div class="event-arrow-hint">
                      <span>详情与席位预约 →</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </section>

      <!-- 6. 空间自习与楼层导览交互 (Floor Navigator) -->
      <section class="space-navigator-section">
        <div class="section-head-center">
          <span class="section-kicker">空间赋能</span>
          <h2 class="section-title-text">全馆楼层研学空间平面导览</h2>
          <p class="section-subtitle-text">356 席沉浸式自习空间，实时掌握各楼层余座、网络、电源配置</p>
        </div>

        <div class="floor-navigator-box">
          <!-- 楼层选择器标签页 -->
          <div class="floor-tabs-bar">
            <button
              v-for="floor in floorList"
              :key="floor.id"
              type="button"
              class="floor-tab-btn"
              :class="{ active: activeFloorId === floor.id }"
              @click="activeFloorId = floor.id"
            >
              <span class="floor-badge-number">{{ floor.level }}</span>
              <div class="floor-tab-text">
                <span class="floor-name">{{ floor.name }}</span>
                <span class="floor-seats-status">余 {{ floor.availableSeats }} 席</span>
              </div>
            </button>
          </div>

          <!-- 当前选中楼层的详细信息卡片 -->
          <div class="floor-content-display">
            <div class="floor-info-left">
              <div class="floor-highlight-badge">{{ currentFloor.level }} · {{ currentFloor.name }}</div>
              <h3 class="floor-display-title">{{ currentFloor.tagline }}</h3>
              <p class="floor-display-desc">{{ currentFloor.description }}</p>

              <!-- 席位占用率进度条 -->
              <div class="seats-meter-container">
                <div class="seats-meter-label">
                  <span>实时座位占用率</span>
                  <strong>{{ currentFloor.occupancy }}% ({{ currentFloor.totalSeats - currentFloor.availableSeats }} / {{ currentFloor.totalSeats }} 席在用)</strong>
                </div>
                <div class="meter-bar-track">
                  <div
                    class="meter-bar-fill"
                    :style="{ width: currentFloor.occupancy + '%' }"
                    :class="currentFloor.occupancy > 80 ? 'fill-red' : 'fill-blue'"
                  ></div>
                </div>
              </div>

              <!-- 空间便民设施清单 -->
              <div class="amenities-grid">
                <div v-for="item in currentFloor.amenities" :key="item.name" class="amenity-chip">
                  <span class="amenity-icon">{{ item.icon }}</span>
                  <span>{{ item.name }}</span>
                </div>
              </div>

              <!-- 选座行动入口 -->
              <div class="floor-actions-row">
                <router-link :to="{ path: '/portal/seat', query: { floor: currentFloor.level } }" class="btn-go-seat">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>在线预约该楼层自习席位 (余 {{ currentFloor.availableSeats }} 席) →</span>
                </router-link>
                <router-link to="/portal/guide" class="btn-floor-guide">
                  <span>查看详细楼层排架规则</span>
                </router-link>
              </div>
            </div>

            <div class="floor-preview-right">
              <div class="zone-badges-list">
                <h4>🎯 核心功能分区</h4>
                <div class="zones-flow">
                  <div v-for="zone in currentFloor.zones" :key="zone.title" class="zone-item-card">
                    <div class="zone-bullet"></div>
                    <div>
                      <strong>{{ zone.title }}</strong>
                      <p>{{ zone.detail }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 7. 高校学术资源与权威数据库矩阵 -->
      <section class="databases-section">
        <div class="section-head-center">
          <span class="section-kicker">学术资源</span>
          <h2 class="section-title-text">中外权威学术数据库与科研资源</h2>
          <p class="section-subtitle-text">校内 IP 免登录直达，全学科期刊、会议、学位论文及电子书库深度覆盖</p>
        </div>

        <div class="databases-grid">
          <div
            v-for="db in academicDatabases"
            :key="db.name"
            class="db-card-box"
          >
            <div class="db-icon-wrap" :style="{ color: db.color, background: db.bgColor }">
              <span class="db-abbr">{{ db.abbr }}</span>
            </div>
            <div class="db-content">
              <h4 class="db-name">{{ db.name }}</h4>
              <p class="db-desc">{{ db.desc }}</p>
              <div class="db-tags-row">
                <span v-for="t in db.tags" :key="t" class="db-tag">{{ t }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 8. 读者借阅配额规范与入馆指引 -->
      <section class="policies-section" id="guide">
        <div class="policy-card-container">
          <div class="policy-head">
            <div>
              <span class="section-kicker">读者服务规范</span>
              <h2 class="section-title-text">各类读者借阅权限与借期标准</h2>
              <p class="section-subtitle-text">秉持公平、高效原则，保障全体师生与读者的充分借阅权益</p>
            </div>
            <router-link to="/portal/guide" class="policy-more-btn">
              查看完整借阅与赔偿细则 →
            </router-link>
          </div>

          <div class="policy-table-responsive">
            <table class="policy-quota-table">
              <thead>
                <tr>
                  <th>读者类型</th>
                  <th>最大借阅册数</th>
                  <th>基础借阅期限</th>
                  <th>线上续借次数</th>
                  <th>最长续借期</th>
                  <th>预约图书额度</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in quotaPolicies" :key="p.type">
                  <td>
                    <span class="reader-type-tag" :class="p.tagClass">{{ p.type }}</span>
                  </td>
                  <td><strong>{{ p.maxBooks }}</strong> 册</td>
                  <td>{{ p.term }} 天</td>
                  <td>{{ p.renewTimes }} 次</td>
                  <td>+{{ p.renewDays }} 天</td>
                  <td>{{ p.reserveLimit }} 册</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 便民规则小贴士条 -->
          <div class="policy-tips-row">
            <div class="tip-item">
              <span class="tip-symbol">⏱️</span>
              <span><strong>还书提醒：</strong>到期日前 3 天将通过系统公告与弹窗提醒读者及时归还或线上续借。</span>
            </div>
            <div class="tip-item">
              <span class="tip-symbol">💰</span>
              <span><strong>逾期政策：</strong>逾期产生违约金 0.1 元/天/本，结清违约金后恢复正常借阅功能。</span>
            </div>
            <div class="tip-item">
              <span class="tip-symbol">📦</span>
              <span><strong>预约到馆：</strong>预约图书到馆后为读者免费保留 7 天，超时自动释放给下一位读者。</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 9. 常见问题 (FAQ 手风琴) -->
      <section class="faq-section">
        <div class="section-head-center">
          <span class="section-kicker">答疑解惑</span>
          <h2 class="section-title-text">读者高频疑问解答</h2>
          <p class="section-subtitle-text">了解借阅卡办理、密码重置、自习室签到与失物招领流程</p>
        </div>

        <div class="faq-list-wrap">
          <div
            v-for="(faq, idx) in faqList"
            :key="faq.q"
            class="faq-accordion-item"
            :class="{ open: activeFaqIndex === idx }"
            @click="toggleFaq(idx)"
          >
            <div class="faq-question-row">
              <div class="faq-q-left">
                <span class="faq-q-badge">Q{{ idx + 1 }}</span>
                <span class="faq-q-text">{{ faq.q }}</span>
              </div>
              <span class="faq-arrow">{{ activeFaqIndex === idx ? '▲' : '▼' }}</span>
            </div>
            <div v-show="activeFaqIndex === idx" class="faq-answer-box">
              <p>{{ faq.a }}</p>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 11. 公告详情弹窗 -->
    <el-dialog
      v-model="noticeDialogVisible"
      :title="currentNotice.title || '公告详情'"
      width="640px"
      align-center
    >
      <div class="notice-modal-body">
        <div class="notice-modal-meta">
          <el-tag :type="getNoticeTagType(currentNotice.type)">
            {{ getNoticeTypeLabel(currentNotice.type) }}
          </el-tag>
          <span class="notice-modal-date">发布时间：{{ formatDate(currentNotice.publishTime || currentNotice.createTime) }}</span>
        </div>
        <div class="notice-modal-text">
          {{ currentNotice.content || '暂无详细通告内容。' }}
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="noticeDialogVisible = false">我已知晓</el-button>
      </template>
    </el-dialog>


    <!-- 13. 学术活动与讲座详情/预约弹窗 -->
    <el-dialog
      v-model="eventDialogVisible"
      :title="selectedEvent ? selectedEvent.title : '活动详情'"
      width="600px"
      align-center
    >
      <div v-if="selectedEvent" class="event-modal-content">
        <div class="event-modal-tags">
          <el-tag type="warning" effect="dark">{{ selectedEvent.category }}</el-tag>
          <span class="event-modal-status" :class="selectedEvent.statusClass">{{ selectedEvent.status }}</span>
        </div>
        <div class="event-info-table">
          <div class="info-row">
            <span class="info-label">🎙️ 主讲嘉宾：</span>
            <span class="info-val">{{ selectedEvent.speaker }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">🕒 活动时间：</span>
            <span class="info-val">{{ selectedEvent.time || '2026-03-28 14:30 - 16:30' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">📍 活动地点：</span>
            <span class="info-val">{{ selectedEvent.location }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">👥 席位规模：</span>
            <span class="info-val">{{ selectedEvent.capacity || '150人 (名额有限，报满即止)' }}</span>
          </div>
        </div>
        <div class="event-modal-desc">
          <h4>活动简介</h4>
          <p>{{ selectedEvent.desc }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="eventDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleRegisterEvent(selectedEvent)">
          立即预约讲座席位
        </el-button>
      </template>
    </el-dialog>

    <!-- 14. 侧边快捷悬浮工具栏 -->
    <div class="floating-quick-bar">
      <button class="quick-fab-btn" title="空间选座" @click="router.push('/portal/seat')">
        <span class="fab-icon">🪑</span>
        <span class="fab-label">选座</span>
      </button>
      <button class="quick-fab-btn" title="文献检索" @click="scrollToSearch">
        <span class="fab-icon">🔍</span>
        <span class="fab-label">查书</span>
      </button>
      <button v-show="showBackToTop" class="quick-fab-btn fab-top" title="回到顶部" @click="scrollToTop">
        <span class="fab-icon">▲</span>
        <span class="fab-label">置顶</span>
      </button>
    </div>

    <!-- 12. 典雅庄重的学术页脚 -->
    <footer class="landing-footer">
      <div class="footer-inner">
        <div class="footer-columns-grid">
          <!-- 品牌介绍 -->
          <div class="footer-col footer-col-brand">
            <div class="footer-brand-header">
              <div class="footer-logo-box">
                <el-icon size="22" color="#ffffff"><Reading /></el-icon>
              </div>
              <div class="footer-brand-title">
                <h3>智慧图书馆服务平台</h3>
                <span>SMART DIGITAL LIBRARY</span>
              </div>
            </div>
            <p class="footer-desc-text">
              基于 Spring Boot 2.7 与 Vue 3 构建的高性能数字文献与智能自习空间管理体系，赋能现代高校与科研机构文献资源高效流通。
            </p>
            <div class="footer-system-badges">
              <span class="sys-badge">Spring Boot 2.7</span>
              <span class="sys-badge">Vue 3 SFC</span>
              <span class="sys-badge">Element Plus</span>
              <span class="sys-badge">MySQL 8.0</span>
            </div>
          </div>

          <!-- 快速通道 -->
          <div class="footer-col">
            <h4 class="footer-col-title">读者服务通道</h4>
            <ul class="footer-nav-list">
              <li><router-link to="/portal/search">全馆文献检索</router-link></li>
              <li><router-link to="/portal/seat">空间选座预约</router-link></li>
              <li><router-link to="/portal/rank">文献借阅榜单</router-link></li>
              <li><router-link to="/portal/center">个人在借中心</router-link></li>
              <li><router-link to="/portal/feedback">读者好书荐购</router-link></li>
            </ul>
          </div>

          <!-- 规章与指南 -->
          <div class="footer-col">
            <h4 class="footer-col-title">入馆规范与支持</h4>
            <ul class="footer-nav-list">
              <li><router-link to="/portal/guide">借还与续借规则</router-link></li>
              <li><router-link to="/portal/guide">违约金与赔偿政策</router-link></li>
              <li><router-link to="/portal/guide">1F~4F 空间导览</router-link></li>
              <li><router-link to="/portal/news">馆务活动与通报</router-link></li>
              <li><router-link to="/register">免费办理电子借书证</router-link></li>
            </ul>
          </div>

          <!-- 联系信息与开馆时间 -->
          <div class="footer-col">
            <h4 class="footer-col-title">开馆时间与联系</h4>
            <div class="footer-contact-info">
              <p>🕒 <strong>服务时间：</strong>周一至周日 08:00 - 22:00</p>
              <p>📍 <strong>馆舍地址：</strong>高校图书馆文献研学总馆</p>
              <p>📞 <strong>咨询服务台：</strong>010-8888-6666</p>
              <p>✉️ <strong>馆长信箱：</strong>library@oneais.dev</p>
            </div>
          </div>
        </div>

        <div class="footer-bottom-bar">
          <p>© 2024-2026 智慧图书馆管理系统 · 保留所有权利</p>
          <div class="footer-legal-links">
            <router-link to="/reader-login">读者门户入口</router-link>
            <span class="divider">/</span>
            <router-link to="/login">图书管理控制台</router-link>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Reading,
  Search,
  User,
  Setting,
  Trophy,
  OfficeBuilding,
  ChatDotRound,
  Bell,
  Calendar,
  Location,
  Guide,
  ArrowRight
} from '@element-plus/icons-vue'
import { getNewBooks, getBorrowRank } from '../../api/modules/book'
import { getAnnouncements } from '../../api/modules/system'

const router = useRouter()


// 动态时钟与跑马灯
const currentClockTime = ref('')
let clockTimer = null
const updateClock = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const date = String(now.getDate()).padStart(2, '0')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const dayStr = weekDays[now.getDay()]
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentClockTime.value = `${year}-${month}-${date} ${hours}:${minutes}:${seconds} ${dayStr}`
}

const activeAnnouncementIndex = ref(0)
let marqueeTimer = null

const activeAnnouncementText = computed(() => {
  if (announcementList.value && announcementList.value.length > 0) {
    const item = announcementList.value[activeAnnouncementIndex.value % announcementList.value.length]
    return `【${getNoticeTypeLabel(item.type)}】${item.title} (${formatDate(item.publishTime || item.createTime)})`
  }
  return '2026年春季新书 1,500 册已完成编目上架 · 4F 沉浸式静音自习室全天候开放 · 欢迎使用智慧服务大厅！'
})

const handleMarqueeClick = () => {
  if (announcementList.value && announcementList.value.length > 0) {
    const item = announcementList.value[activeAnnouncementIndex.value % announcementList.value.length]
    openAnnouncement(item)
  } else {
    router.push('/portal/news')
  }
}

const onlyAvailable = ref(false)

// 检索控制
const searchKeyword = ref('')
const activeSearchType = ref('all')

const searchTabs = [
  { key: 'all', label: '全部馆藏', icon: '📚' },
  { key: 'title', label: '图书书名', icon: '📖' },
  { key: 'author', label: '著作作者', icon: '✍️' },
  { key: 'isbn', label: '标准 ISBN', icon: '🏷️' },
  { key: 'category', label: '学科门类', icon: '📂' },
  { key: 'callno', label: '索书号', icon: '🔢' }
]

const currentSearchPlaceholder = computed(() => {
  const map = {
    all: '输入书名、著者、ISBN 或学科关键词快速检索馆藏...',
    title: '请输入图书全称或书名关键字 (如: 人工智能、三体)...',
    author: '请输入著者、编者、译者姓名 (如: 刘慈欣、周志华)...',
    isbn: '请输入 10 位或 13 位标准 ISBN (如: 9787111544937)...',
    category: '输入学科分类 (如: 计算机、经济学、自然科学)...',
    callno: '请输入图书索书号 (如: TP312/B915、I247.5/L765)...'
  }
  return map[activeSearchType.value] || '探索馆藏图书...'
})

const hotKeywords = [
  '人工智能',
  '深入理解计算机系统',
  '三体',
  '宏观经济学',
  '心理学与生活',
  '考研数学',
  '红楼梦',
  '数据结构'
]

const handleHeroSearch = () => {
  const query = {}
  const kw = searchKeyword.value.trim()
  if (kw) {
    if (activeSearchType.value === 'author') {
      query.author = kw
    } else {
      query.keyword = kw
    }
  }
  if (activeSearchType.value && activeSearchType.value !== 'all') {
    query.type = activeSearchType.value
  }
  if (onlyAvailable.value) {
    query.onlyAvailable = 'true'
  }
  router.push({ path: '/portal/search', query })
}

const handleQuickBorrow = (book) => {
  if (book && book.id) {
    router.push({
      path: '/portal/book/' + book.id,
      query: { action: book.availableCount > 0 ? 'borrow' : 'reserve' }
    })
  } else {
    router.push({ path: '/portal/search', query: { keyword: book.title } })
  }
}

const quickSearchTag = (tag) => {
  searchKeyword.value = tag
  activeSearchType.value = 'all'
  handleHeroSearch()
}

// 八大读者核心服务矩阵
const serviceCards = [
  {
    title: '馆藏图书检索',
    tag: 'SEARCH',
    desc: '全馆纸质文献与电子书分类查阅，实时掌握排架索书号与可借副本。',
    path: '/portal/search',
    icon: Search,
    themeClass: 'metro-blue'
  },
  {
    title: '空间自习选座',
    tag: 'SEAT 3D',
    desc: '全馆四层考研与静音阅览室，平面图实时选座，避免占座高效研习。',
    path: '/portal/seat',
    icon: OfficeBuilding,
    themeClass: 'metro-emerald'
  },
  {
    title: '文献借阅风云榜',
    tag: 'TOP 20',
    desc: '全馆读者借阅量前三甲金银铜领奖台，探索大家都在读的热门佳作。',
    path: '/portal/rank',
    icon: Trophy,
    themeClass: 'metro-amber'
  },
  {
    title: '馆务动态与公告',
    tag: 'NEWS',
    desc: '开闭馆通知、新书到馆通报与重要借阅规则调整第一线速递。',
    path: '/portal/news',
    icon: Bell,
    themeClass: 'metro-purple'
  },
  {
    title: '个人借阅中心',
    tag: 'MY LOANS',
    desc: '在借文献归还期限预警，支持到期前一键线上快速续借与借阅历史查询。',
    path: '/portal/center',
    icon: Reading,
    themeClass: 'metro-indigo'
  },
  {
    title: '入馆服务指南',
    tag: 'GUIDE',
    desc: '读者类型配额表格、借期规则、超期政策与1F至4F空间设施导览。',
    path: '/portal/guide',
    icon: Guide,
    themeClass: 'metro-cyan'
  },
  {
    title: '读者好书荐购',
    tag: 'ACQUISITION',
    desc: '读者荐书心声直达文献资源采编部，采纳采购进度全流程透明公开。',
    path: '/portal/feedback',
    icon: ChatDotRound,
    themeClass: 'metro-rose'
  },
  {
    title: '图书采编管理后台',
    tag: 'ADMIN CONSOLE',
    desc: '图书编目、读者借书证审核、副本流通办理与全馆综合运营数据大屏。',
    path: '/login',
    icon: Setting,
    themeClass: 'metro-slate'
  }
]

// 馆藏好书展示与 API 联动
const activeBookTab = ref('rank')
const bookTabs = [
  { key: 'rank', label: '🌟 借阅热门风云榜' },
  { key: 'new', label: '🆕 最新入藏典籍' },
  { key: 'classics', label: '💡 经典学术推荐' }
]

// 预设优质经典书籍（网络异常或空数据时高质兜底）
const fallbackRankBooks = [
  {
    id: 1,
    title: '深入理解计算机系统 (原书第3版)',
    author: 'Randal E. Bryant / David R. OHallaron',
    categoryName: '计算机科学',
    callNumber: 'TP312/B915',
    availableCount: 4,
    themeColor: '#1e3a8a',
    cover: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300&q=80'
  },
  {
    id: 2,
    title: '三体全集 (地球往事三部曲)',
    author: '刘慈欣',
    categoryName: '当代科幻',
    callNumber: 'I247.5/L765',
    availableCount: 2,
    themeColor: '#0f172a',
    cover: 'https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?w=300&q=80'
  },
  {
    id: 3,
    title: '算法导论 (原书第3版)',
    author: 'Thomas H. Cormen 等',
    categoryName: '计算机科学',
    callNumber: 'TP301.6/C811',
    availableCount: 1,
    themeColor: '#047857',
    cover: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300&q=80'
  },
  {
    id: 4,
    title: '经济学原理 (微观+宏观分册)',
    author: 'N. 格里高利·曼昆',
    categoryName: '经济管理',
    callNumber: 'F015/M282',
    availableCount: 5,
    themeColor: '#b45309',
    cover: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=300&q=80'
  },
  {
    id: 5,
    title: '机器学习 (西瓜书)',
    author: '周志华',
    categoryName: '人工智能',
    callNumber: 'TP181/Z741',
    availableCount: 0,
    themeColor: '#4338ca',
    cover: 'https://images.unsplash.com/photo-1509228468518-180dd4864904?w=300&q=80'
  },
  {
    id: 6,
    title: '人类简史：从动物到上帝',
    author: '尤瓦尔·赫拉利',
    categoryName: '人文历史',
    callNumber: 'K105/H281',
    availableCount: 3,
    themeColor: '#9333ea',
    cover: 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=300&q=80'
  },
  {
    id: 7,
    title: '红楼梦 (全二册)',
    author: '曹雪芹 / 高鹗',
    categoryName: '古典文学',
    callNumber: 'I242.4/C182',
    availableCount: 6,
    themeColor: '#be123c',
    cover: 'https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=300&q=80'
  },
  {
    id: 8,
    title: '心理学与生活 (第19版)',
    author: '理查德·格里格 / 菲利普·津巴多',
    categoryName: '心理科学',
    callNumber: 'B84/G857',
    availableCount: 3,
    themeColor: '#0e7490',
    cover: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=300&q=80'
  }
]

const fallbackNewBooks = [
  {
    id: 11,
    title: '大模型应用开发实战：LangChain与Agent',
    author: '李明 / 张智慧',
    categoryName: '人工智能',
    callNumber: 'TP18/L382',
    availableCount: 4,
    themeColor: '#2563eb',
    cover: 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=300&q=80'
  },
  {
    id: 12,
    title: '现代数字信号处理教程 (第2版)',
    author: '陈建国',
    categoryName: '信息工程',
    callNumber: 'TN911.7/C391',
    availableCount: 2,
    themeColor: '#059669',
    cover: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=300&q=80'
  },
  {
    id: 13,
    title: '认知觉醒：开启学习的底层逻辑',
    author: '周岭',
    categoryName: '个人成长',
    callNumber: 'B848.4/Z781',
    availableCount: 5,
    themeColor: '#d97706',
    cover: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=300&q=80'
  },
  {
    id: 14,
    title: '量子计算原理与前沿算法',
    author: '王量子',
    categoryName: '数理科学',
    callNumber: 'O413/W219',
    availableCount: 3,
    themeColor: '#4f46e5',
    cover: 'https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=300&q=80'
  }
]

const fallbackClassics = [
  {
    id: 21,
    title: '国富论 (微观与国际贸易典藏版)',
    author: '亚当·斯密',
    categoryName: '西方名著',
    callNumber: 'F091.33/S642',
    availableCount: 3,
    themeColor: '#334155',
    cover: 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=300&q=80'
  },
  {
    id: 22,
    title: '百年孤独 (范晔译本)',
    author: '加西亚·马尔克斯',
    categoryName: '世界文学',
    callNumber: 'I775.45/M291',
    availableCount: 4,
    themeColor: '#7c2d12',
    cover: 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=300&q=80'
  },
  {
    id: 23,
    title: '论法的精神 (全二册)',
    author: '孟德斯鸠',
    categoryName: '法学政治',
    callNumber: 'D909/M531',
    availableCount: 2,
    themeColor: '#1e293b',
    cover: 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=300&q=80'
  },
  {
    id: 24,
    title: '史记 (文白对照精注版)',
    author: '司马迁',
    categoryName: '中华史学',
    callNumber: 'K204.2/S581',
    availableCount: 5,
    themeColor: '#991b1b',
    cover: 'https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=300&q=80'
  }
]

const rankBooksList = ref(fallbackRankBooks)
const newBooksList = ref(fallbackNewBooks)

const currentDisplayBooks = computed(() => {
  if (activeBookTab.value === 'rank') return rankBooksList.value
  if (activeBookTab.value === 'new') return newBooksList.value
  return fallbackClassics
})

const switchBookTab = (key) => {
  activeBookTab.value = key
}

const handleBookClick = (book) => {
  if (book && book.id) {
    router.push('/portal/book/' + book.id)
  } else {
    router.push({ path: '/portal/search', query: { keyword: book.title } })
  }
}

// 动态通知公告与 API 联动
const noticeDialogVisible = ref(false)
const currentNotice = ref({})

const fallbackAnnouncements = [
  {
    id: 101,
    title: '关于2026年春季学期图书馆开放时间及借还服务安排的通告',
    type: 1,
    publishTime: '2026-03-20',
    summary: '总馆周一至周日 08:00 - 22:00 正常开放，沉浸式静音自习区全天候开放，欢迎各位师生借阅研学。',
    content: '各位读者：\n为保障广大师生科研与学业需求，本学期图书馆全面恢复常规开放作息。1F至4F阅览区域每日 08:00 至 22:00 开放，4F 考研静音自习区支持晚间预约。馆内全区域已完成千兆 Wi-Fi 6 升级与智能直饮水系统巡检，请读者凭电子借书证扫码入馆。'
  },
  {
    id: 102,
    title: '【新书上架】2026年第一批人文社科与人工智能外文图书已编目入库',
    type: 2,
    publishTime: '2026-03-18',
    summary: '包含深度学习、量子计算、国际经济学等新书共计 1,500 余册，索书号与排架信息已同步上线。',
    content: '采编部近期完成 2026 年度首批图书采选与编目入库工作。新书现已全部上架至 2F 社科文学阅览区与 3F 科技文献借阅区，读者可通过官网检索系统查询索书号前往书架借阅。'
  },
  {
    id: 103,
    title: '关于期末自习室考研选座系统防占座签到规则的调整通知',
    type: 3,
    publishTime: '2026-03-15',
    summary: '预约座位后请于 30 分钟内到馆扫码签到，离开超 45 分钟未归席位将自动释放给候补读者。',
    content: '为维护公平和谐的研学秩序，自即日起严格执行防占座签到机制。线上选座成功后，请读者在 30 分钟内到馆刷卡/扫码完成签到。若有中途离开需求，请在自习预约系统内选择“暂离模式”。'
  },
  {
    id: 104,
    title: '关于万方数据知识服务平台及 Web of Science 数据库更新的通知',
    type: 1,
    publishTime: '2026-03-10',
    summary: '校内 IP 段已完成全新引文索引与学位论文全库授权续订，校外读者可通过 CARSI 账号访问。',
    content: '我馆已完成 2026 年度重点学术资源订购工作。万方数据硕博学位论文库、Web of Science SCI 核心数据库已全面升级，校外师生可通过校园统一身份认证系统直接访问全文。'
  }
]

const announcementList = ref(fallbackAnnouncements)

const getNoticeTypeLabel = (type) => {
  const map = { 1: '通告', 2: '新书', 3: '规则', 4: '活动' }
  return map[type] || '通知'
}

const getNoticeClass = (type) => {
  const map = { 1: 'type-notice', 2: 'type-book', 3: 'type-rule', 4: 'type-activity' }
  return map[type] || 'type-notice'
}

const getNoticeTagType = (type) => {
  const map = { 1: '', 2: 'success', 3: 'warning', 4: 'danger' }
  return map[type] || ''
}

const openAnnouncement = (item) => {
  currentNotice.value = item
  noticeDialogVisible.value = true
}

const formatDate = (dateStr) => {
  if (!dateStr) return '2026-03-24'
  return String(dateStr).slice(0, 10)
}

// 学术讲座与读者沙龙活动
const culturalEvents = [
  {
    month: '03月',
    day: '28',
    category: '前沿学术',
    status: '报名中',
    statusClass: 'status-open',
    title: '大模型与生成式 AI 时代的科研文献检索变革',
    speaker: '李明哲 教授 (博士生导师)',
    location: '1F 学术报告厅 (可容纳 200 人)',
    time: '2026-03-28 14:30 - 16:30',
    capacity: '200 人 (余座 48 席)',
    desc: '探讨基于大语言模型、向量数据库与 RAG 检索增强生成的学术文献智慧挖掘。系统讲解如何借助智能 Agent 提炼高价值文献，提升高校师生科研攻关与论文开题效率。'
  },
  {
    month: '04月',
    day: '02',
    category: '书香沙龙',
    status: '进行中',
    statusClass: 'status-active',
    title: '重读《百年孤独》：魔幻现实主义的现代精神困境',
    speaker: '张文澜 作家 / 文学学者',
    location: '2F 多功能研讨室 A-201',
    time: '2026-04-02 19:00 - 21:00',
    capacity: '60 人 (余座 12 席)',
    desc: '从马尔克斯笔下的布恩迪亚家族兴衰出发，深度解析魔幻现实主义的叙事结构、宿命论与拉丁美洲近现代历史隐喻，带领读者共赴一场文学的精神沉浸之旅。'
  },
  {
    month: '04月',
    day: '15',
    category: '特藏展览',
    status: '筹备中',
    statusClass: 'status-upcoming',
    title: '文渊流芳——馆藏明清善本古籍装帧与修复艺术特展',
    speaker: '图书馆古籍保护研究所',
    location: '4F 特藏文库展览厅',
    time: '2026-04-15 09:30 - 11:30',
    capacity: '100 人 (余座 80 席)',
    desc: '集中亮相馆藏明代刻本、清乾隆殿版古籍善本共 60 余部，现场由资深古籍修复师进行传统手工纸张修补、古籍线装、经折装装帧工艺实操演示，欢迎师生预约观摩。'
  }
]

const eventDialogVisible = ref(false)
const selectedEvent = ref(null)

const openEventModal = (evt) => {
  selectedEvent.value = evt
  eventDialogVisible.value = true
}

const handleRegisterEvent = (evt) => {
  ElMessage.success(`恭喜！您已成功预约《${evt.title}》，电子凭证已发送至您的个人中心`)
  eventDialogVisible.value = false
}

const showBackToTop = ref(false)
const handleScroll = () => {
  showBackToTop.value = window.scrollY > 400
}
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
const scrollToSearch = () => {
  const searchEl = document.querySelector('.hero-search-card')
  if (searchEl) {
    searchEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

// 空间与楼层导览数据
const activeFloorId = ref('3f')

const floorList = [
  {
    id: '1f',
    level: '1F',
    name: '综合服务与新书借还',
    totalSeats: 60,
    availableSeats: 26,
    occupancy: 56,
    tagline: '便捷高效 · 一站式读者业务办理枢纽',
    description: '汇聚读者办证借还总台、新书上架展示区、数字化检索触控屏与书香咖啡休憩吧，提供热情周到的读者咨询服务。',
    amenities: [
      { name: '读者服务总台', icon: '💁' },
      { name: '自助借还书机', icon: '🖥️' },
      { name: '立式智能检索终端', icon: '🔍' },
      { name: '书香咖啡吧', icon: '☕' },
      { name: '失物招领柜', icon: '🧳' }
    ],
    zones: [
      { title: '读者服务大厅', detail: '借书证办理、借还疑难咨询与馆长接待' },
      { title: '当季新书速递展架', detail: '近期入藏 1,500 册实体新书推荐' },
      { title: '报刊现刊阅览区', detail: '180 种主流学术期刊与当日报刊' }
    ]
  },
  {
    id: '2f',
    level: '2F',
    name: '人文社科与文学艺术',
    totalSeats: 96,
    availableSeats: 32,
    occupancy: 66,
    tagline: '墨香沉淀 · 感受经典人文与艺术思想的碰撞',
    description: '涵盖哲学、历史、文学、艺术、政治经济等社科类典藏文献，配备宽敞实木研读长桌与多媒体小组研讨包间。',
    amenities: [
      { name: '实木护眼研习桌', icon: '🪑' },
      { name: '全席 220V 电源', icon: '⚡' },
      { name: '多媒体研讨室 (需预约)', icon: '👥' },
      { name: '自助云打印机', icon: '🖨️' },
      { name: '温热直饮水机', icon: '💧' }
    ],
    zones: [
      { title: '中国文学与世界名著库', detail: 'I 类文学典籍开架借阅' },
      { title: '经济学与管理学专区', detail: 'F 类经管学术典藏图书' },
      { title: '学术小组研讨室 (4间)', detail: '支持 4-8 人学术项目研讨交流' }
    ]
  },
  {
    id: '3f',
    level: '3F',
    name: '科技文献与理工计算机',
    totalSeats: 110,
    availableSeats: 48,
    occupancy: 56,
    tagline: '探索前沿 · 理工计算机与自然科学文献高地',
    description: '聚焦数理科学、计算机科学、电子工程、人工智能与生命科学文献。全席配备千兆有线网络与极速 Wi-Fi 6，支持笔记本大功率充电。',
    amenities: [
      { name: '极速 Wi-Fi 6 全覆盖', icon: '📶' },
      { name: 'Type-C 快充接口', icon: '🔌' },
      { name: '数字文献微缩检索区', icon: '💻' },
      { name: '高清静音扫描仪', icon: '📑' },
      { name: '恒温恒湿新风系统', icon: '🌿' }
    ],
    zones: [
      { title: '计算机与人工智能专架', detail: 'TP 类算法、架构、大模型相关文献' },
      { title: '数理化前沿科学库', detail: 'O 类基础理论与学术专著' },
      { title: '科研文献检索中心', detail: '配备 20 台高性能学术查阅终端' }
    ]
  },
  {
    id: '4f',
    level: '4F',
    name: '沉浸静音与考研自习',
    totalSeats: 90,
    availableSeats: 36,
    occupancy: 60,
    tagline: '格物致知 · 专为深度阅读与专注备考打造',
    description: '全馆绝对静音保护区，严禁键盘敲击声与手机铃声。独立格栅私密隔断席位，配备智能阅读台灯与静音储物柜。',
    amenities: [
      { name: '独立私密自习格栅', icon: '🛡️' },
      { name: '智能三档调光台灯', icon: '💡' },
      { name: '全静音降噪地毯', icon: '🔇' },
      { name: '智能电子储物柜', icon: '🗄️' },
      { name: '沉浸专注倒计时牌', icon: '⏳' }
    ],
    zones: [
      { title: '考研专注封闭区', detail: '60 席长租与日约自习席位' },
      { title: '无声电子书阅览区', detail: '严禁有声键盘的静音研读空间' },
      { title: '特藏文献善本室', detail: '明清古籍与地方志保护查阅' }
    ]
  }
]

const currentFloor = computed(() => {
  return floorList.find(f => f.id === activeFloorId.value) || floorList[2]
})

// 权威学术数据库
const academicDatabases = [
  {
    abbr: 'CNKI',
    name: '中国知网 (CNKI)',
    desc: '国内收录最全的学术期刊、博硕士学位论文、重要报纸与会议论文库。',
    color: '#1d4ed8',
    bgColor: '#eff6ff',
    tags: ['中文核心', '学位论文', '全文下载']
  },
  {
    abbr: 'WOS',
    name: 'Web of Science',
    desc: '全球最具权威的自然科学、社会科学、艺术与人文领域的引文索引库。',
    color: '#b45309',
    bgColor: '#fffbeb',
    tags: ['SCI', 'SSCI', '高被引论文']
  },
  {
    abbr: 'WF',
    name: '万方数据知识服务平台',
    desc: '整合中文科技期刊、学位论文、科技成果及国内外专利的知识服务平台。',
    color: '#059669',
    bgColor: '#ecfdf5',
    tags: ['学术期刊', '专利成果', '科技报告']
  },
  {
    abbr: 'IEEE',
    name: 'IEEE Xplore Digital',
    desc: '电气电子工程师学会权威数据库，计算机科学与电子通信顶级国际文献。',
    color: '#0891b2',
    bgColor: '#ecfeff',
    tags: ['IEEE 期刊', '国际会议', '电子工程']
  },
  {
    abbr: 'SP',
    name: 'SpringerLink 电子期刊',
    desc: '世界知名学术出版商 Springer 出版的高水平同行评审全文文献数据库。',
    color: '#7c3aed',
    bgColor: '#f5f3ff',
    tags: ['英文专著', '同行评审', '前沿科学']
  },
  {
    abbr: 'SS',
    name: '超星移动图书馆与电子书',
    desc: '收录海量中文电子图书、名师学术讲座视频与移动端随身研学阅读平台。',
    color: '#e11d48',
    bgColor: '#fff1f2',
    tags: ['百万电子书', '视频讲座', '多端阅读']
  }
]

// 借阅权限配额标准
const quotaPolicies = [
  {
    type: '本科生',
    maxBooks: 15,
    term: 30,
    renewTimes: 1,
    renewDays: 30,
    reserveLimit: 3,
    tagClass: 'badge-ug'
  },
  {
    type: '硕士 / 博士研究生',
    maxBooks: 30,
    term: 60,
    renewTimes: 2,
    renewDays: 60,
    reserveLimit: 5,
    tagClass: 'badge-pg'
  },
  {
    type: '教职员工 / 访问学者',
    maxBooks: 50,
    term: 90,
    renewTimes: 3,
    renewDays: 90,
    reserveLimit: 10,
    tagClass: 'badge-faculty'
  },
  {
    type: '校外读者 (持证)',
    maxBooks: 5,
    term: 30,
    renewTimes: 0,
    renewDays: 0,
    reserveLimit: 1,
    tagClass: 'badge-external'
  }
]

// FAQ 手风琴
const activeFaqIndex = ref(0)
const toggleFaq = (idx) => {
  activeFaqIndex.value = activeFaqIndex.value === idx ? -1 : idx
}

const faqList = [
  {
    q: '读者借书证如何办理与开通？',
    a: '在校师生凭学工号在线点击页面右上角“办借书证”即可快速注册并自动核验开通电子读者卡；校外读者可携带有效身份证件至 1F 读者服务总台人工核验办理。'
  },
  {
    q: '在借图书即将到期，如何在线办理续借？',
    a: '读者登录个人中心后，进入“当前在借”列表，在图书到期日前 5 天内点击“一键续借”按钮即可顺利延长借阅期。每本图书可续借次数视读者类型而定。'
  },
  {
    q: '自习室在线选座有哪些防占座规则？',
    a: '通过“空间选座”预约成功后，读者需在 30 分钟内到馆扫码签到。若中途需要离开自习区，请在移动端选择“暂离”（最长保留45分钟），超时未归系统将自动释放座位。'
  },
  {
    q: '借阅图书发生损坏或遗失，如何赔偿？',
    a: '若图书遗失，读者可购买同版或新版同名正版图书充抵并补缴 5 元加工费；无法购得原书的，将按照原书定价的 2~3 倍折价赔偿。'
  },
  {
    q: '在校外如何免费使用图书馆购买的学术数据库？',
    a: '我馆已全量接入中国教育科研计算机网 CARSI 联盟认证，师生在校外访问知网、Web of Science 等数据库时，选择“高校登录”输入我校统一身份账号密码即可直接免费下载全文。'
  }
]

// 生命周期：加载实时数据
onMounted(async () => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  marqueeTimer = setInterval(() => {
    if (announcementList.value && announcementList.value.length > 0) {
      activeAnnouncementIndex.value = (activeAnnouncementIndex.value + 1) % announcementList.value.length
    }
  }, 4500)
  window.addEventListener('scroll', handleScroll)

  try {
    const resRank = await getBorrowRank(8)
    if (resRank && resRank.data && resRank.data.length > 0) {
      rankBooksList.value = resRank.data
    }
  } catch (err) {
    // 使用 fallback 数据
  }

  try {
    const resNew = await getNewBooks(8)
    if (resNew && resNew.data && resNew.data.length > 0) {
      newBooksList.value = resNew.data
    }
  } catch (err) {
    // 使用 fallback 数据
  }

  try {
    const resNotice = await getAnnouncements({ page: 1, size: 5 })
    if (resNotice && resNotice.data) {
      const records = resNotice.data.records || resNotice.data.list || (Array.isArray(resNotice.data) ? resNotice.data : null)
      if (records && records.length > 0) {
        announcementList.value = records
      }
    }
  } catch (err) {
    // 使用 fallback 数据
  }
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (marqueeTimer) clearInterval(marqueeTimer)
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* ================= 全局容器与重置 ================= */
.landing-page {
  min-height: 100vh;
  background-color: #f8fafc;
  color: #1e293b;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  overflow-x: hidden;
}

/* ================= 1. 顶部全局导航栏 ================= */
.landing-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(15, 23, 42, 0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.brand-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  flex-shrink: 0;
}

.brand-logo-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.4);
}

.brand-text-wrap {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 18px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 0.5px;
}

.brand-subtitle {
  font-size: 10px;
  color: #94a3b8;
  letter-spacing: 1px;
  font-weight: 600;
}

.status-ticker-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: #e2e8f0;
}

.status-dot-pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 10px #10b981;
  animation: pulse-glow 2s infinite;
}

@keyframes pulse-glow {
  0% { transform: scale(0.9); opacity: 0.7; }
  50% { transform: scale(1.2); opacity: 1; }
  100% { transform: scale(0.9); opacity: 0.7; }
}

.status-pipe {
  color: rgba(255, 255, 255, 0.2);
}

.status-location {
  color: #94a3b8;
}

.landing-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  color: #cbd5e1;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 14px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.nav-item:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.08);
}

.nav-item.active {
  color: #60a5fa;
  background: rgba(37, 99, 235, 0.15);
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}



.action-btn-reader {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #2563eb;
  color: #ffffff;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);
  transition: all 0.2s ease;
}

.action-btn-reader:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.action-btn-register {
  color: #93c5fd;
  border: 1px solid rgba(147, 197, 253, 0.3);
  text-decoration: none;
  padding: 7px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}

.action-btn-register:hover {
  background: rgba(147, 197, 253, 0.1);
  color: #ffffff;
}

.action-link-admin {
  color: #94a3b8;
  text-decoration: none;
  font-size: 13px;
  padding: 6px 10px;
  transition: color 0.2s;
}

.action-link-admin:hover {
  color: #ffffff;
}

/* ================= 2. Hero Section ================= */
.hero-section {
  position: relative;
  background: #0b132b;
  color: #ffffff;
  padding: 64px 24px 80px;
  overflow: hidden;
}

.hero-backdrop {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.ambient-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.45;
}

.glow-blue {
  width: 500px;
  height: 500px;
  background: #2563eb;
  top: -150px;
  left: 10%;
}

.glow-gold {
  width: 400px;
  height: 400px;
  background: #d97706;
  bottom: -100px;
  right: 15%;
  opacity: 0.25;
}

.glow-purple {
  width: 450px;
  height: 450px;
  background: #7c3aed;
  top: 20%;
  right: -5%;
  opacity: 0.3;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
}

.hero-content-wrap {
  position: relative;
  max-width: 1100px;
  margin: 0 auto;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 跑马灯微条 */
.announcement-marquee-pill {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 30px;
  padding: 6px 18px 6px 8px;
  margin-bottom: 28px;
  cursor: pointer;
  max-width: 90%;
  transition: all 0.25s ease;
}

.announcement-marquee-pill:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(96, 165, 250, 0.5);
  transform: translateY(-1px);
}

.marquee-tag {
  background: linear-gradient(135deg, #ef4444, #f97316);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 20px;
  flex-shrink: 0;
}

.marquee-text-scroll {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 13px;
  color: #e2e8f0;
}

.marquee-arrow {
  font-size: 12px;
  color: #60a5fa;
  font-weight: 600;
  flex-shrink: 0;
}

/* 主标题 */
.hero-main-title {
  font-size: 46px;
  font-weight: 900;
  line-height: 1.25;
  margin: 0 0 16px;
  letter-spacing: -0.5px;
}

.title-gradient {
  background: linear-gradient(135deg, #60a5fa 20%, #38bdf8 50%, #facc15 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  display: inline-block;
}

.hero-subtitle-desc {
  font-size: 17px;
  color: #94a3b8;
  max-width: 780px;
  line-height: 1.6;
  margin: 0 0 36px;
}

/* 核心检索卡片 */
.hero-search-card {
  width: 100%;
  max-width: 920px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 22px;
  padding: 16px 20px 20px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4);
}

.search-fields-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.search-tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: none;
  color: #94a3b8;
  font-size: 13px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.search-tab-btn:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.08);
}

.search-tab-btn.active {
  color: #ffffff;
  background: #2563eb;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.4);
}

.tab-icon {
  font-size: 14px;
}

.search-input-group {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 14px;
  padding: 6px 8px 6px 18px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  gap: 10px;
}

.input-prefix-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.hero-native-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  color: #0f172a;
  background: transparent;
  padding: 10px 0;
}

.hero-native-input::placeholder {
  color: #94a3b8;
  font-size: 15px;
}

.search-clear-btn {
  background: #f1f5f9;
  border: none;
  color: #64748b;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-clear-btn:hover {
  background: #e2e8f0;
  color: #0f172a;
}

.hero-submit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border: none;
  color: #ffffff;
  padding: 12px 26px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.4);
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.hero-submit-btn:hover {
  background: linear-gradient(135deg, #1d4ed8, #1e40af);
  transform: translateX(2px);
}

/* 热门搜索词 */
.hot-tags-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
  text-align: left;
  flex-wrap: wrap;
}

.hot-tags-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #cbd5e1;
  font-weight: 600;
  flex-shrink: 0;
}

.hot-chips-list {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.hot-chip-pill {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #e2e8f0;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.hot-chip-pill:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: #60a5fa;
  color: #ffffff;
}

/* 馆情态势仪表盘 */
.hero-stats-strip {
  display: flex;
  align-items: center;
  justify-content: space-around;
  width: 100%;
  max-width: 960px;
  margin-top: 50px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  padding: 20px 24px;
}

.stat-box {
  display: flex;
  align-items: center;
  gap: 14px;
  text-align: left;
}

.stat-visual {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.icon-blue { background: rgba(37, 99, 235, 0.2); border: 1px solid rgba(37, 99, 235, 0.3); }
.icon-amber { background: rgba(245, 158, 11, 0.2); border: 1px solid rgba(245, 158, 11, 0.3); }
.icon-emerald { background: rgba(16, 185, 129, 0.2); border: 1px solid rgba(16, 185, 129, 0.3); }
.icon-purple { background: rgba(139, 92, 246, 0.2); border: 1px solid rgba(139, 92, 246, 0.3); }

.stat-val {
  font-size: 24px;
  font-weight: 900;
  color: #ffffff;
  font-family: 'DIN Alternate', sans-serif;
  line-height: 1.1;
}

.stat-plus, .stat-unit {
  font-size: 16px;
  color: #60a5fa;
  margin-left: 2px;
}

.stat-lbl {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 38px;
  background: rgba(255, 255, 255, 0.1);
}

/* ================= 3. 八大核心读者服务矩阵 ================= */
.services-section {
  max-width: 1360px;
  margin: -36px auto 0;
  padding: 0 24px;
  position: relative;
  z-index: 10;
}

.section-head-center {
  text-align: center;
  margin-bottom: 40px;
}

.section-kicker {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  color: #2563eb;
  background: #eff6ff;
  padding: 4px 14px;
  border-radius: 20px;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
}

.section-title-text {
  font-size: 32px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 10px;
}

.section-subtitle-text {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

.services-matrix-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.service-metro-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.03);
}

.service-metro-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.service-metro-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}

.service-metro-card:hover::before {
  opacity: 1;
}

.metro-blue::before { background: #2563eb; }
.metro-emerald::before { background: #059669; }
.metro-amber::before { background: #d97706; }
.metro-purple::before { background: #7c3aed; }
.metro-indigo::before { background: #4f46e5; }
.metro-cyan::before { background: #0891b2; }
.metro-rose::before { background: #e11d48; }
.metro-slate::before { background: #475569; }

.service-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.service-badge-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.metro-blue .service-badge-icon { background: #eff6ff; color: #2563eb; }
.metro-emerald .service-badge-icon { background: #ecfdf5; color: #059669; }
.metro-amber .service-badge-icon { background: #fffbeb; color: #d97706; }
.metro-purple .service-badge-icon { background: #faf5ff; color: #7c3aed; }
.metro-indigo .service-badge-icon { background: #eef2ff; color: #4f46e5; }
.metro-cyan .service-badge-icon { background: #ecfeff; color: #0891b2; }
.metro-rose .service-badge-icon { background: #fff1f2; color: #e11d48; }
.metro-slate .service-badge-icon { background: #f1f5f9; color: #475569; }

.service-code-tag {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.8px;
  color: #94a3b8;
  background: #f8fafc;
  padding: 3px 8px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.service-card-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px;
}

.service-card-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 20px;
  flex: 1;
}

.service-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 600;
  color: #2563eb;
  border-top: 1px solid #f1f5f9;
  padding-top: 14px;
}

.service-action-arrow {
  transition: transform 0.2s;
  font-size: 15px;
}

.service-metro-card:hover .service-action-arrow {
  transform: translateX(4px);
}

/* ================= 4. 典籍流芳 · 馆藏好书展示 ================= */
.books-showcase-section {
  max-width: 1360px;
  margin: 80px auto 0;
  padding: 0 24px;
}

.showcase-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 20px;
}

.showcase-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.book-tab-pill {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  color: #475569;
  font-size: 14px;
  font-weight: 600;
  padding: 8px 18px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.book-tab-pill:hover {
  border-color: #93c5fd;
  color: #2563eb;
}

.book-tab-pill.active {
  background: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.view-more-books-link {
  font-size: 14px;
  font-weight: 600;
  color: #2563eb;
  text-decoration: none;
  margin-left: 8px;
}

.view-more-books-link:hover {
  text-decoration: underline;
}

.books-gallery-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.book-showcase-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
}

.book-showcase-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}

/* 3D 封皮与书脊 */
.book-3d-wrap {
  position: relative;
  width: 100%;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
}

.book-cover-container {
  position: relative;
  width: 140px;
  height: 190px;
  border-radius: 4px 8px 8px 4px;
  box-shadow: -4px 6px 14px rgba(0, 0, 0, 0.25), 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.25s;
}

.book-showcase-card:hover .book-cover-container {
  transform: scale(1.04) rotate(1deg);
}

.book-cover-img {
  width: 100%;
  height: 100%;
}

.book-cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  text-align: center;
  color: #ffffff;
}

.fallback-text {
  font-size: 13px;
  font-weight: 700;
  margin-top: 10px;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.book-status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.badge-in {
  background: #10b981;
  color: #ffffff;
}

.badge-out {
  background: #f59e0b;
  color: #ffffff;
}

.book-meta-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.book-category-tag {
  align-self: flex-start;
  font-size: 11px;
  font-weight: 600;
  color: #2563eb;
  background: #eff6ff;
  padding: 2px 8px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.book-title-heading {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.book-author-text {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-callno-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #94a3b8;
  margin-top: auto;
  border-top: 1px dashed #e2e8f0;
  padding-top: 10px;
}

.callno-code {
  font-family: monospace;
  background: #f8fafc;
  color: #475569;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

/* ================= 5. 馆务动态与学术活动 ================= */
.news-events-section {
  max-width: 1360px;
  margin: 80px auto 0;
  padding: 0 24px;
}

.news-panel-card, .events-panel-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 30px;
  height: 100%;
  box-sizing: border-box;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.02);
}

.panel-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.panel-head-title {
  display: flex;
  align-items: center;
  gap: 14px;
}

.icon-chip {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.blue-chip { background: #eff6ff; color: #2563eb; }
.amber-chip { background: #fffbeb; color: #d97706; }

.panel-main-heading {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 4px;
}

.panel-sub-heading {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.panel-more-link {
  font-size: 13px;
  font-weight: 600;
  color: #2563eb;
  text-decoration: none;
}

.status-live-chip {
  background: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 20px;
}

/* 动态列表项 */
.news-items-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.news-item-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
}

.news-item-row:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
  transform: translateX(4px);
}

.news-type-pill {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 6px;
  flex-shrink: 0;
}

.type-notice { background: #eff6ff; color: #2563eb; }
.type-book { background: #ecfdf5; color: #059669; }
.type-rule { background: #fffbeb; color: #d97706; }
.type-activity { background: #fdf2f8; color: #db2777; }

.news-title-desc {
  flex: 1;
  overflow: hidden;
}

.news-item-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-item-preview {
  font-size: 12px;
  color: #64748b;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-item-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.news-date {
  font-size: 11px;
  color: #94a3b8;
}

.news-read-arrow {
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
}

/* 活动卡片 */
.events-cards-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.event-card-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 14px;
  transition: all 0.2s;
}

.event-card-item:hover {
  background: #ffffff;
  border-color: #cbd5e1;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.04);
}

.event-date-badge {
  width: 52px;
  height: 58px;
  background: linear-gradient(135deg, #1e3a8a, #2563eb);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;
}

.evt-month {
  font-size: 10px;
  opacity: 0.9;
}

.evt-day {
  font-size: 20px;
  font-weight: 800;
  line-height: 1.1;
}

.event-detail {
  flex: 1;
}

.event-tag-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.evt-category {
  font-size: 11px;
  color: #2563eb;
  font-weight: 600;
}

.evt-status-pill {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
}

.status-open { background: #eff6ff; color: #2563eb; }
.status-active { background: #ecfdf5; color: #059669; }
.status-upcoming { background: #f1f5f9; color: #64748b; }

.event-title-text {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px;
}

.event-speaker {
  font-size: 12px;
  color: #475569;
  margin: 0 0 4px;
}

.event-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
  margin: 0;
}

/* ================= 6. 空间选座与楼层导览 ================= */
.space-navigator-section {
  max-width: 1360px;
  margin: 80px auto 0;
  padding: 0 24px;
}

.floor-navigator-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 22px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
}

.floor-tabs-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 24px;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 16px;
}

.floor-tab-btn {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px 18px;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease;
}

.floor-tab-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.floor-tab-btn.active {
  background: #eff6ff;
  border-color: #2563eb;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.15);
}

.floor-badge-number {
  font-size: 20px;
  font-weight: 900;
  color: #2563eb;
  font-family: 'DIN Alternate', sans-serif;
}

.floor-tab-text {
  display: flex;
  flex-direction: column;
}

.floor-name {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.floor-seats-status {
  font-size: 12px;
  color: #059669;
  font-weight: 600;
}

.floor-content-display {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 36px;
  padding: 10px;
}

.floor-highlight-badge {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  color: #2563eb;
  background: #eff6ff;
  padding: 4px 12px;
  border-radius: 6px;
  margin-bottom: 10px;
}

.floor-display-title {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 10px;
}

.floor-display-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.7;
  margin: 0 0 24px;
}

/* 占用率计量条 */
.seats-meter-container {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
}

.seats-meter-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #475569;
  margin-bottom: 8px;
}

.meter-bar-track {
  width: 100%;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.meter-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.4s ease;
}

.fill-blue { background: linear-gradient(90deg, #3b82f6, #2563eb); }
.fill-red { background: linear-gradient(90deg, #f59e0b, #ef4444); }

.amenities-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 28px;
}

.amenity-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  padding: 6px 12px;
  border-radius: 8px;
}

.floor-actions-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.btn-go-seat {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #2563eb;
  color: #ffffff;
  text-decoration: none;
  font-size: 14px;
  font-weight: 700;
  padding: 12px 24px;
  border-radius: 10px;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35);
  transition: all 0.2s;
}

.btn-go-seat:hover {
  background: #1d4ed8;
  transform: translateY(-2px);
}

.btn-floor-guide {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  text-decoration: none;
}

.btn-floor-guide:hover {
  color: #2563eb;
}

/* 分区卡片 */
.zone-badges-list {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 24px;
  height: 100%;
  box-sizing: border-box;
}

.zone-badges-list h4 {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 16px;
}

.zones-flow {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.zone-item-card {
  display: flex;
  gap: 12px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px 14px;
}

.zone-bullet {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2563eb;
  margin-top: 6px;
  flex-shrink: 0;
}

.zone-item-card strong {
  font-size: 14px;
  color: #0f172a;
}

.zone-item-card p {
  font-size: 12px;
  color: #64748b;
  margin: 4px 0 0;
}

/* ================= 7. 学术权威数据库矩阵 ================= */
.databases-section {
  max-width: 1360px;
  margin: 80px auto 0;
  padding: 0 24px;
}

.databases-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.db-card-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  gap: 16px;
  transition: all 0.2s;
}

.db-card-box:hover {
  transform: translateY(-3px);
  border-color: #cbd5e1;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.05);
}

.db-icon-wrap {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 14px;
  flex-shrink: 0;
}

.db-content {
  flex: 1;
}

.db-name {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px;
}

.db-desc {
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
  margin: 0 0 10px;
}

.db-tags-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.db-tag {
  font-size: 10px;
  color: #475569;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
}

/* ================= 8. 借阅规范与配额 ================= */
.policies-section {
  max-width: 1360px;
  margin: 80px auto 0;
  padding: 0 24px;
}

.policy-card-container {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 22px;
  padding: 36px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
}

.policy-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}

.policy-more-btn {
  font-size: 14px;
  font-weight: 600;
  color: #2563eb;
  text-decoration: none;
}

.policy-table-responsive {
  overflow-x: auto;
}

.policy-quota-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 14px;
}

.policy-quota-table th {
  background: #f8fafc;
  color: #475569;
  font-weight: 700;
  padding: 14px 18px;
  border-bottom: 1px solid #e2e8f0;
}

.policy-quota-table td {
  padding: 16px 18px;
  border-bottom: 1px solid #f1f5f9;
  color: #1e293b;
}

.policy-quota-table tr:last-child td {
  border-bottom: none;
}

.reader-type-tag {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 6px;
}

.badge-ug { background: #eff6ff; color: #2563eb; }
.badge-pg { background: #ecfdf5; color: #059669; }
.badge-faculty { background: #fffbeb; color: #d97706; }
.badge-external { background: #f1f5f9; color: #64748b; }

.policy-tips-row {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px dashed #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  color: #475569;
}

.tip-symbol {
  font-size: 16px;
}

/* ================= 9. FAQ 手风琴 ================= */
.faq-section {
  max-width: 960px;
  margin: 80px auto 0;
  padding: 0 24px;
}

.faq-list-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-accordion-item {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 18px 22px;
  cursor: pointer;
  transition: all 0.2s;
}

.faq-accordion-item:hover {
  border-color: #cbd5e1;
}

.faq-accordion-item.open {
  border-color: #93c5fd;
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.06);
}

.faq-question-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.faq-q-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.faq-q-badge {
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  padding: 4px 8px;
  border-radius: 6px;
}

.faq-q-text {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.faq-arrow {
  font-size: 11px;
  color: #94a3b8;
}

.faq-answer-box {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
  font-size: 14px;
  color: #475569;
  line-height: 1.7;
}

/* ================= 10. 公告详情弹窗 ================= */

.notice-modal-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.notice-modal-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.notice-modal-date {
  font-size: 13px;
  color: #94a3b8;
}

.notice-modal-text {
  font-size: 14px;
  color: #334155;
  line-height: 1.8;
  white-space: pre-line;
}

/* ================= 12. 典雅庄重的学术页脚 ================= */
.landing-footer {
  background: #0b132b;
  color: #94a3b8;
  padding: 72px 24px 32px;
  margin-top: 100px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.footer-inner {
  max-width: 1360px;
  margin: 0 auto;
}

.footer-columns-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr 1fr 1.2fr;
  gap: 48px;
  padding-bottom: 48px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.footer-brand-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.footer-logo-box {
  width: 40px;
  height: 40px;
  background: #2563eb;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-brand-title h3 {
  font-size: 17px;
  font-weight: 800;
  color: #ffffff;
  margin: 0;
}

.footer-brand-title span {
  font-size: 10px;
  color: #64748b;
  letter-spacing: 0.8px;
}

.footer-desc-text {
  font-size: 13px;
  line-height: 1.7;
  color: #94a3b8;
  margin: 0 0 20px;
}

.footer-system-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.sys-badge {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #cbd5e1;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 4px;
}

.footer-col-title {
  font-size: 15px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 20px;
}

.footer-nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-nav-list a {
  color: #94a3b8;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s;
}

.footer-nav-list a:hover {
  color: #60a5fa;
}

.footer-contact-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
}

.footer-contact-info p {
  margin: 0;
}

.footer-contact-info strong {
  color: #e2e8f0;
}

.footer-bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 28px;
  font-size: 13px;
  color: #64748b;
  flex-wrap: wrap;
  gap: 16px;
}

.footer-bottom-bar p {
  margin: 0;
}

.footer-legal-links {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-legal-links a {
  color: #94a3b8;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s;
}

.footer-legal-links a:hover {
  color: #60a5fa;
}

.footer-legal-links .divider {
  color: #334155;
}

/* ================= 响应式断点 ================= */
@media (max-width: 1200px) {
  .services-matrix-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .books-gallery-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .floor-tabs-bar {
    grid-template-columns: repeat(2, 1fr);
  }
  .floor-content-display {
    grid-template-columns: 1fr;
  }
  .databases-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .footer-columns-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 860px) {
  .landing-nav, .status-ticker-badge {
    display: none;
  }
  .hero-main-title {
    font-size: 32px;
  }
  .search-fields-tabs {
    justify-content: flex-start;
  }
  .search-input-group {
    flex-wrap: wrap;
  }
  .hero-submit-btn {
    width: 100%;
    justify-content: center;
  }
  .hero-stats-strip {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  .stat-divider {
    display: none;
  }
  .services-matrix-grid {
    grid-template-columns: 1fr;
  }
  .books-gallery-grid {
    grid-template-columns: 1fr;
  }
  .databases-grid {
    grid-template-columns: 1fr;
  }
  .footer-columns-grid {
    grid-template-columns: 1fr;
  }
}

/* 仅看在馆开关 */
.search-tab-list {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.only-available-filter {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  font-size: 13px;
  color: #94a3b8;
  padding: 6px 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.2s ease;
  margin-left: auto;
}

.only-available-filter:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.12);
}

.filter-chk {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease;
}

.filter-chk.active {
  background: #2563eb;
  border-color: #3b82f6;
}

.filter-text {
  font-weight: 500;
}

/* 书籍卡片悬停操作 */
.book-cover-container {
  position: relative;
  overflow: hidden;
}

.book-hover-actions {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.78);
  backdrop-filter: blur(2px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.25s ease;
  border-radius: 8px;
  z-index: 5;
}

.book-cover-container:hover .book-hover-actions {
  opacity: 1;
}

.hover-btn {
  padding: 6px 16px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.hover-btn-primary {
  background: #2563eb;
  color: #ffffff;
  border: none;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.4);
}

.hover-btn-primary:hover {
  background: #1d4ed8;
  transform: scale(1.05);
}

.hover-btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.hover-btn-secondary:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

/* 活动卡片点击手势与微标 */
.event-card-item {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.event-card-item:hover {
  transform: translateX(4px);
  border-color: #cbd5e1;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.event-arrow-hint {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #d97706;
  text-align: right;
  transition: color 0.2s;
}

.event-card-item:hover .event-arrow-hint {
  color: #b45309;
}

/* 学术讲座弹窗内容 */
.event-modal-content {
  padding: 8px 0;
}

.event-modal-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.event-modal-status {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.event-info-table {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px 18px;
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
}

.info-label {
  color: #64748b;
  min-width: 90px;
  font-weight: 500;
}

.info-val {
  color: #0f172a;
  font-weight: 600;
}

.event-modal-desc h4 {
  font-size: 14px;
  color: #0f172a;
  margin-bottom: 8px;
}

.event-modal-desc p {
  font-size: 13px;
  color: #475569;
  line-height: 1.7;
}

/* 侧边快速悬浮导航栏 */
.floating-quick-bar {
  position: fixed;
  right: 24px;
  bottom: 32px;
  z-index: 999;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-fab-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #0f172a;
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  outline: none;
}

.quick-fab-btn:hover {
  background: #2563eb;
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.45);
}

.fab-icon {
  font-size: 16px;
  line-height: 1;
}

.fab-label {
  font-size: 9px;
  font-weight: 600;
  margin-top: 2px;
  letter-spacing: 0.5px;
}

.fab-top {
  background: #2563eb;
}

.fab-top:hover {
  background: #1d4ed8;
}

</style>
