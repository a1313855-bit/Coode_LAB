<template>
  <div class="policy-page">
    <header class="policy-hero">
      <p class="eyebrow">RETURN &amp; EXCHANGE</p>
      <h1>退換貨政策</h1>
      <p class="hero-sub">簡單、透明的退換貨流程，<br />讓每一次購物都更加安心。</p>
    </header>

    <div class="policy-body">
      <!-- 01 -->
      <section class="policy-sec">
        <div class="sec-head">
          <span class="sec-num">01</span>
          <h2>申請期限</h2>
        </div>
        <p class="sec-text">商品送達後，如有退貨或換貨需求，請於 <strong>7 日內</strong>提出申請。</p>
        <p class="sec-text">您可以前往：</p>
        <p class="path-line">會員中心 → 我的訂單 → 訂單詳情 → 申請退換貨</p>
        <p class="sec-text">選擇需要處理的商品與數量，並填寫退換貨原因後送出申請。</p>
        <p class="sec-note">超過申請期限後，將無法透過系統提出退換貨申請。</p>
      </section>

      <!-- 02 -->
      <section class="policy-sec">
        <div class="sec-head">
          <span class="sec-num">02</span>
          <h2>退換貨條件</h2>
        </div>
        <p class="sec-text">為確保商品能順利完成退換貨，請保持商品及相關內容完整。</p>
        <p class="sec-text">符合以下情況時，可提出退貨或換貨申請：</p>
        <ul class="sec-list">
          <li>商品尺寸或款式不符合需求</li>
          <li>收到的商品與訂購內容不符</li>
          <li>商品收到時有瑕疵或損壞</li>
          <li>其他合理的退換貨需求</li>
        </ul>
        <p class="sec-text">商品若已有明顯使用、洗滌、修改、人為損壞，或其他影響商品完整性的情況，廠商可能無法受理退換貨申請。</p>
        <p class="sec-text">若同一商品一次購買多件，也可以依實際需求申請部分數量退換貨，最終核准數量將以廠商審核結果為準。</p>
      </section>

      <!-- 03 -->
      <section class="policy-sec">
        <div class="sec-head">
          <span class="sec-num">03</span>
          <h2>如何申請</h2>
        </div>
        <p class="sec-text">提出退換貨申請後，您可以隨時透過會員中心查看目前處理進度。</p>
        <div class="steps">
          <div class="step">
            <span class="step-num">STEP 01</span>
            <h3>選擇商品</h3>
            <p>進入訂單詳情，選擇需要退貨或換貨的商品。</p>
          </div>
          <div class="step">
            <span class="step-num">STEP 02</span>
            <h3>提出申請</h3>
            <p>選擇「退貨」或「換貨」，填寫申請數量、原因及相關說明。</p>
          </div>
          <div class="step">
            <span class="step-num">STEP 03</span>
            <h3>廠商審核</h3>
            <p>申請送出後，由商品所屬廠商確認申請內容與核准數量。</p>
          </div>
          <div class="step">
            <span class="step-num">STEP 04</span>
            <h3>完成處理</h3>
            <p>申請核准後，依照退貨或換貨流程完成後續處理。</p>
          </div>
        </div>
      </section>

      <!-- 04 -->
      <section class="policy-sec">
        <div class="sec-head">
          <span class="sec-num">04</span>
          <h2>退貨與退款</h2>
        </div>
        <p class="sec-text">選擇「退貨」並經廠商審核同意後，請依照退貨指示將商品寄回。</p>
        <div class="flow">
          <span v-for="(f, i) in ['提出退貨', '廠商審核', '寄回商品', '廠商確認', '辦理退款', '完成']" :key="i">
            {{ f }}<i v-if="i < 5">→</i>
          </span>
        </div>
        <p class="sec-text">廠商收到商品並確認退貨內容後，將進行退款處理。</p>
        <p class="sec-text">若一次申請多件商品，廠商可能依商品實際狀況核准全部或部分數量。未通過審核的部分，可於退換貨紀錄中查看處理結果。</p>
        <p class="sec-note">實際退款方式與完成時間，將依原付款方式及相關金流處理流程而定。</p>
      </section>

      <!-- 05 -->
      <section class="policy-sec">
        <div class="sec-head">
          <span class="sec-num">05</span>
          <h2>換貨流程</h2>
        </div>
        <p class="sec-text">如果商品尺寸、款式或其他原因需要更換，可於訂單中選擇「換貨」提出申請。</p>
        <div class="flow">
          <span v-for="(f, i) in ['提出換貨', '廠商審核', '寄回商品', '廠商確認', '寄出新品', '收到新品', '完成']" :key="i">
            {{ f }}<i v-if="i < 6">→</i>
          </span>
        </div>
        <p class="sec-text">換貨申請經廠商同意後，請依照指示將原商品寄回。</p>
        <p class="sec-text">廠商確認收到商品並完成處理後，將寄出替換商品。替換商品送達並完成收貨後，本次換貨流程即完成。</p>
        <p class="sec-text">換貨期間可以至：</p>
        <p class="path-line">會員中心 → 退換貨紀錄</p>
        <p class="sec-text">查看目前的處理進度。</p>
      </section>

      <!-- 06 -->
      <section class="policy-sec">
        <div class="sec-head">
          <span class="sec-num">06</span>
          <h2>常見問題</h2>
        </div>
        <div class="faq">
          <div class="faq-item" v-for="fa in faqs" :key="fa.q" :class="{ open: fa.open }">
            <button class="faq-q" @click="fa.open = !fa.open">
              <span class="faq-icon">{{ fa.open ? '−' : '+' }}</span>
              {{ fa.q }}
            </button>
            <transition name="faqfade">
              <p v-if="fa.open" class="faq-a">{{ fa.a }}</p>
            </transition>
          </div>
        </div>
      </section>

      <!-- 底部 CTA -->
      <section class="policy-help">
        <p class="help-eyebrow">NEED HELP?</p>
        <h2>還有其他問題嗎？</h2>
        <p class="sec-text">如果對訂單、退換貨申請或處理結果有任何疑問，<br />請透過網站提供的聯絡方式與我們聯繫。</p>
      </section>
    </div>

    <footer class="policy-brand">
      <div class="brand-logo">Coode LAB</div>
      <p>Everyday Style, Made Simple.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const faqs = ref([
  {
    q: '商品尺寸不合可以換貨嗎？',
    a: '可以。請於退換貨申請期限內進入訂單詳情，選擇需要更換的商品並提出換貨申請，實際處理結果以廠商審核為準。',
    open: false,
  },
  {
    q: '可以只退同款商品中的一件嗎？',
    a: '可以。如果同一商品購買多件，可以選擇實際需要退換貨的數量提出申請，不需要將所有商品一起退回。',
    open: false,
  },
  {
    q: '如何查看目前處理進度？',
    a: '登入會員後，前往 「會員中心 → 退換貨紀錄」，即可查看申請內容以及目前的處理狀態。',
    open: false,
  },
  {
    q: '申請送出後可以立即退回商品嗎？',
    a: '建議先等待廠商完成審核。申請核准後，再依照退換貨指示寄回商品，以避免影響後續處理。',
    open: false,
  },
  {
    q: '申請被拒絕怎麼辦？',
    a: '如果商品不符合退換貨條件，或申請內容經廠商審核後無法受理，可於退換貨紀錄中查看處理結果。',
    open: false,
  },
  {
    q: '一次申請多件商品，廠商一定會全部核准嗎？',
    a: '不一定。廠商會依實際商品狀況與申請內容進行審核，因此可能出現全部核准、部分核准或不予核准的情況。',
    open: false,
  },
])
</script>

<style scoped>
.policy-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 0 24px 80px;
}
.policy-hero {
  text-align: center;
  padding: 64px 0 48px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 48px;
}
.eyebrow {
  font-size: 12px;
  letter-spacing: 0.32em;
  color: var(--accent);
  margin-bottom: 14px;
  font-weight: 700;
}
.policy-hero h1 {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: var(--ink);
  margin: 0;
}
.hero-sub {
  margin-top: 20px;
  color: var(--muted);
  font-size: 15px;
  line-height: 1.9;
  letter-spacing: 0.04em;
}

.policy-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.policy-sec {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 36px 40px 40px;
}
.sec-head {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin-bottom: 22px;
}
.sec-num {
  font-size: 20px;
  font-weight: 800;
  color: var(--accent);
  letter-spacing: 0.06em;
}
.sec-head h2 {
  font-size: 20px;
  font-weight: 800;
  color: var(--ink);
  letter-spacing: 0.1em;
  margin: 0;
}
.sec-text {
  color: #4a4845;
  font-size: 14px;
  line-height: 1.9;
  letter-spacing: 0.03em;
  margin: 12px 0;
}
.sec-text strong {
  color: var(--ink);
  font-weight: 800;
}
.sec-note {
  color: var(--accent);
  font-size: 13px;
  line-height: 1.8;
  margin-top: 16px;
  padding: 14px 18px;
  background: #fdf3f2;
  border-radius: 8px;
}
.path-line {
  color: var(--ink);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.03em;
  padding: 12px 18px;
  background: var(--paper-soft);
  border-left: 3px solid var(--accent);
  border-radius: 0 8px 8px 0;
  margin: 10px 0;
}
.sec-list {
  list-style: none;
  margin: 16px 0;
  padding: 0;
}
.sec-list li {
  position: relative;
  padding-left: 22px;
  color: #4a4845;
  font-size: 14px;
  line-height: 2;
  letter-spacing: 0.03em;
}
.sec-list li::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 14px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
}

.steps {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 22px;
}
.step {
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 20px 18px;
  background: var(--paper-soft);
}
.step-num {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: var(--accent);
  text-transform: uppercase;
}
.step h3 {
  font-size: 15px;
  font-weight: 800;
  color: var(--ink);
  margin: 8px 0 8px;
  letter-spacing: 0.04em;
}
.step p {
  font-size: 12.5px;
  color: var(--muted);
  line-height: 1.7;
  margin: 0;
}

.flow {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin: 18px 0;
  padding: 18px 22px;
  background: var(--paper-soft);
  border-radius: 10px;
}
.flow span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  white-space: nowrap;
}
.flow i {
  font-style: normal;
  color: var(--accent);
  font-weight: 700;
}

.faq {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.faq-item {
  border: 1px solid var(--line);
  border-radius: 10px;
  overflow: hidden;
}
.faq-q {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: #fff;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink);
  text-align: left;
  letter-spacing: 0.03em;
}
.faq-q:hover {
  background: var(--paper-soft);
}
.faq-icon {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--ink);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
}
.faq-a {
  padding: 0 20px 18px 56px;
  color: var(--muted);
  font-size: 13.5px;
  line-height: 1.9;
  margin: 0;
}
.faqfade-enter-active,
.faqfade-leave-active {
  transition: opacity 0.2s ease;
}
.faqfade-enter-from,
.faqfade-leave-to {
  opacity: 0;
}

.policy-help {
  text-align: center;
  padding: 56px 0 48px;
  margin-top: 16px;
  background: var(--ink);
  color: #fff;
  border-radius: 12px;
}
.help-eyebrow {
  font-size: 11px;
  letter-spacing: 0.3em;
  color: var(--accent);
  font-weight: 700;
  text-transform: uppercase;
}
.policy-help h2 {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.1em;
  margin: 12px 0 14px;
}
.policy-help .sec-text {
  color: rgba(255, 255, 255, 0.72);
  font-size: 13.5px;
  line-height: 1.9;
}

.policy-brand {
  text-align: center;
  padding: 48px 0 0;
}
.brand-logo {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.18em;
  color: var(--ink);
}
.policy-brand p {
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.12em;
  margin: 10px 0 0;
}

@media (max-width: 680px) {
  .steps {
    grid-template-columns: 1fr;
  }
  .policy-sec {
    padding: 24px 20px;
  }
}
</style>
