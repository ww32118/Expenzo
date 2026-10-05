<script setup>
import { reactive, ref } from 'vue'
import userService from '@/services/userService'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const profileForm = reactive({
  name: authStore.user?.name || '',
  email: authStore.user?.email || '',
  username: authStore.user?.username || '',
  phoneNo: authStore.user?.phoneNumber || '',
})

const passwordForm = reactive({ oldPassword: '', newPassword: '' })
const imageUrl = ref(authStore.user?.image || '')

const savingProfile = ref(false)
const savingPassword = ref(false)
const savingImage = ref(false)
const profileMsg = ref('')
const passwordMsg = ref('')
const imageMsg = ref('')
const errorMsg = ref('')

async function handleUpdateImage() {
  savingImage.value = true
  errorMsg.value = ''
  imageMsg.value = ''
  try {
    const updated = await userService.changeProfileImage(authStore.userId, imageUrl.value)
    authStore.setUser({ ...authStore.user, image: updated.image })
    imageMsg.value = 'Profile image updated.'
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    savingImage.value = false
  }
}

async function handleUpdateProfile() {
  savingProfile.value = true
  errorMsg.value = ''
  profileMsg.value = ''
  try {
    const updated = await userService.update({ id: authStore.userId, ...profileForm })
    authStore.setUser(updated)
    profileMsg.value = 'Profile updated.'
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    savingProfile.value = false
  }
}

async function handleChangePassword() {
  savingPassword.value = true
  errorMsg.value = ''
  passwordMsg.value = ''
  try {
    await userService.changePassword({ id: authStore.userId, ...passwordForm })
    passwordMsg.value = 'Password changed.'
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    savingPassword.value = false
  }
}
</script>

<template>
  <div>
    <div class="mb-4 fade-in-up">
      <h1 class="font-brand page-title mb-1">Profile</h1>
      <p class="text-muted-soft mb-0">Manage your account details.</p>
    </div>

    <div v-if="errorMsg" class="alert alert-danger">{{ errorMsg }}</div>

    <div class="row g-3 fade-in-up stagger-2">
      <div class="col-lg-6">
        <div class="surface-card p-4">
          <h5 class="font-display fw-semibold mb-3">Account details</h5>
          <div v-if="profileMsg" class="alert alert-success py-2 small">{{ profileMsg }}</div>
          <form @submit.prevent="handleUpdateProfile">
            <div class="mb-3">
              <label class="form-label fw-semibold">Full name</label>
              <input v-model="profileForm.name" type="text" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label fw-semibold">Username</label>
              <input v-model="profileForm.username" type="text" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label fw-semibold">Email</label>
              <input v-model="profileForm.email" type="email" class="form-control" required />
            </div>
            <div class="mb-4">
              <label class="form-label fw-semibold">Phone number</label>
              <input v-model="profileForm.phoneNo" type="tel" class="form-control" />
            </div>
            <button type="submit" class="btn btn-expenzo" :disabled="savingProfile">
              <span v-if="savingProfile" class="spinner-border spinner-border-sm me-2"></span>
              Save Changes
            </button>
          </form>
        </div>

        <div class="surface-card p-4 mt-3">
          <h5 class="font-display fw-semibold mb-3">Profile image</h5>
          <div v-if="imageMsg" class="alert alert-success py-2 small">{{ imageMsg }}</div>
          <div class="d-flex align-items-center gap-3">
            <img
              v-if="imageUrl"
              :src="imageUrl"
              class="rounded-circle"
              style="width: 56px; height: 56px; object-fit: cover"
              alt="preview"
            />
            <input v-model="imageUrl" type="url" class="form-control" placeholder="https://example.com/photo.jpg" />
            <button class="btn btn-expenzo-outline" :disabled="savingImage" @click="handleUpdateImage">
              <span v-if="savingImage" class="spinner-border spinner-border-sm me-2"></span>
              Save
            </button>
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="surface-card p-4">
          <h5 class="font-display fw-semibold mb-3">Change password</h5>
          <div v-if="passwordMsg" class="alert alert-success py-2 small">{{ passwordMsg }}</div>
          <form @submit.prevent="handleChangePassword">
            <div class="mb-3">
              <label class="form-label fw-semibold">Current password</label>
              <input v-model="passwordForm.oldPassword" type="password" class="form-control" required />
            </div>
            <div class="mb-4">
              <label class="form-label fw-semibold">New password</label>
              <input v-model="passwordForm.newPassword" type="password" class="form-control" required />
            </div>
            <button type="submit" class="btn btn-expenzo-outline" :disabled="savingPassword">
              <span v-if="savingPassword" class="spinner-border spinner-border-sm me-2"></span>
              Update Password
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
