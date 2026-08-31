<script setup>
import { ref, onMounted } from 'vue'
import { myReviews, publishReview } from '../api'
import ReviewImages from '../components/ReviewImages.vue'

const list = ref([])
const showModal = ref(false)
const current = ref(null)
const followContent = ref('')
const followImages = ref([])
const popMsg = ref('')

const showImg = url => window.open(url, '_blank')

const hasFollowUp = r => (r.content || '').includes('【追评】')

onMounted(async () => {
  list.value = await myReviews()
})

function openFollow(r) {
  current.value = r
  followContent.value = ''
  followImages.value = []
  showModal.value = true
}

async function submitFollow() {
  const text = followContent.value.trim()
  if (!text) {
    popMsg.value = '请输入追评内容'
    setTimeout(() => popMsg.value = '', 1500)
    return
  }
  await publishReview({
    orderItemId: current.value.orderItemId,
    rating: current.value.rating,
    content: text,
    images: followImages.value.join(','),
    followUp: true
  })
  showModal.value = false
  popMsg.value = '追评成功'
  setTimeout(() => popMsg.value = '', 1500)
  list.value = await myReviews()
}
</script>

<template>
  <div class="card">
    <h3 class="section-title">我的评价（{{ list.length }}）</h3>
    <div v-for="r in list" :key="r.id" style="padding:12px 0;border-bottom:1px solid #f5f5f5;">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <div>
          <span class="stars" style="color:#ff8000;">{{ '★'.repeat(r.rating) }}</span>
          <span style="color:#999;font-size:12px;margin-left:10px;">{{ r.createTime }}</span>
        </div>
        <button v-if="!hasFollowUp(r)" class="follow-btn" @click="openFollow(r)">追评</button>
        <span v-else class="followed-tag">已追评</span>
      </div>
      <p style="margin-top:6px;white-space:pre-line;">{{ r.content }}</p>
      <div v-if="r.images" class="review-imgs">
        <img v-for="u in r.images.split(',')" :key="u" :src="u" class="review-img" @click="showImg(u)" />
      </div>
      <p v-if="r.reply" style="color:#999;font-size:13px;margin-top:4px;">商家回复：{{ r.reply }}</p>
    </div>
    <div v-if="!list.length" style="text-align:center;color:#999;padding:60px;">还没有发表过评价</div>
  </div>

  <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
    <div class="modal">
      <h3 style="margin-bottom:12px;">追加评价</h3>
      <textarea v-model="followContent" class="textarea" rows="4" maxlength="500" placeholder="说说使用后的感受…"></textarea>
      <div style="margin-top:10px;">
        <ReviewImages v-model="followImages" />
      </div>
      <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:14px;">
        <button class="btn-plain" @click="showModal = false">取消</button>
        <button class="btn-primary" @click="submitFollow">提交追评</button>
      </div>
    </div>
  </div>

  <div class="pop-msg" v-if="popMsg">{{ popMsg }}</div>
</template>

<style scoped>
.follow-btn {
  padding: 5px 14px;
  border: 1px solid #ff5000;
  color: #ff5000;
  background: #fff;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.follow-btn:hover {
  background: #fff7f3;
}
.followed-tag {
  color: #999;
  font-size: 12px;
}
.review-imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.review-img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
  cursor: zoom-in;
}
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  width: 420px;
  max-width: 90vw;
}
.textarea {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 10px;
  font-size: 14px;
  resize: vertical;
  box-sizing: border-box;
}
.pop-msg {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  padding: 10px 20px;
  border-radius: 6px;
  z-index: 99;
}
</style>
