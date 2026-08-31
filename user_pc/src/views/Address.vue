<script setup>
import { ref, onMounted } from 'vue'
import { getAddresses, deleteAddress, setDefaultAddress, addAddress, updateAddress } from '../api'

const list = ref([])
const editing = ref(false)
const form = ref({ id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })

onMounted(load)

async function load() {
  list.value = await getAddresses()
}

function openEdit(addr) {
  form.value = addr ? { ...addr, isDefault: addr.isDefault } : { id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: list.value.length === 0 ? 1 : 0 }
  editing.value = true
}

async function save() {
  const body = { ...form.value }
  try {
    if (body.id) {
      await updateAddress(body)
    } else {
      await addAddress(body)
    }
    await load()
    editing.value = false
  } catch (e) {
    alert(e.message)
  }
}

async function remove(id) {
  if (!confirm('确定删除该地址？')) return
  await deleteAddress(id)
  await load()
}

async function setDefault(id) {
  await setDefaultAddress(id)
  await load()
}
</script>

<template>
  <div class="addr-page">
    <div class="card page-section">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <h3 class="section-title" style="margin:0;">收货地址</h3>
        <button class="btn-primary" @click="openEdit(null)">新增地址</button>
      </div>
      <div v-for="a in list" :key="a.id" class="addr-row">
        <div style="flex:1;">
          <div>
            <span style="font-weight:700;">{{ a.receiverName }}</span>
            <span style="margin-left:12px;color:#999;">{{ a.receiverPhone }}</span>
            <span v-if="a.isDefault === 1" class="tag">默认</span>
          </div>
          <div style="font-size:13px;color:#666;margin-top:6px;">{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }}</div>
        </div>
        <div style="display:flex;gap:12px;font-size:13px;">
          <a href="javascript:void(0)" style="color:#ff5000;" @click="openEdit(a)">编辑</a>
          <a v-if="a.isDefault !== 1" href="javascript:void(0)" @click="setDefault(a.id)">设为默认</a>
          <a href="javascript:void(0)" style="color:#999;" @click="remove(a.id)">删除</a>
        </div>
      </div>
      <div v-if="!list.length" style="color:#999;text-align:center;padding:40px;">暂无收货地址</div>
    </div>

    <div class="modal" v-if="editing">
      <div class="modal-body card">
        <h3>{{ form.id ? '编辑地址' : '新增地址' }}</h3>
        <div class="f2">
          <div class="field"><label>收货人</label><input v-model="form.receiverName" class="input" /></div>
          <div class="field"><label>手机号</label><input v-model="form.receiverPhone" class="input" /></div>
        </div>
        <div class="f3">
          <div class="field"><label>省</label><input v-model="form.province" class="input" /></div>
          <div class="field"><label>市</label><input v-model="form.city" class="input" /></div>
          <div class="field"><label>区/县</label><input v-model="form.district" class="input" /></div>
        </div>
        <div class="field"><label>详细地址</label><input v-model="form.detail" class="input" /></div>
        <label style="font-size:13px;display:flex;align-items:center;gap:6px;margin:8px 0;">
          <input type="checkbox" v-model="form.isDefault" :true-value="1" :false-value="0" /> 设为默认地址
        </label>
        <div style="display:flex;gap:10px;justify-content:flex-end;">
          <button class="btn-plain" @click="editing = false">取消</button>
          <button class="btn-primary" @click="save">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.addr-page {
  max-width: 760px;
  margin: 0 auto;
}
.addr-row {
  display: flex;
  align-items: center;
  padding: 14px;
  border-bottom: 1px solid #f5f5f5;
}
.tag {
  background: #ff5000;
  color: #fff;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  margin-left: 8px;
}
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal-body {
  width: 520px;
  padding: 24px;
}
.f2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.f3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
}
.field {
  margin-bottom: 14px;
}
.field label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}
</style>