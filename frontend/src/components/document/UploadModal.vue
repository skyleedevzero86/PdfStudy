<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="$emit('close')">
    <div class="card mx-4 w-full max-w-lg p-6">
      <div class="mb-6 flex items-center justify-between">
        <h2 class="text-lg font-bold text-slate-800">Upload PDF</h2>
        <button type="button" class="rounded p-1 hover:bg-surface-100" @click="$emit('close')">
          <X class="h-5 w-5 text-slate-400" />
        </button>
      </div>

      <form class="space-y-4" @submit.prevent="handleUpload">
        <div
          @dragover.prevent
          @drop.prevent="handleDrop"
          @click="fileInput?.click()"
          :class="[
            'cursor-pointer rounded-xl border-2 border-dashed p-8 text-center transition-colors',
            file ? 'border-primary-300 bg-primary-50' : 'border-slate-200 hover:border-primary-300 hover:bg-surface-50'
          ]"
        >
          <input ref="fileInput" type="file" accept=".pdf" class="hidden" @change="handleFileChange" />
          <FileText class="mx-auto mb-2 h-10 w-10 text-slate-300" />
          <p v-if="file" class="text-sm font-medium text-primary-700">{{ file.name }}</p>
          <p v-else class="text-sm text-slate-500">Drag a PDF here or click to choose a file.</p>
          <p class="mt-1 text-xs text-slate-400">Up to 100MB</p>
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">
            Title <span class="text-red-500">*</span>
          </label>
          <input v-model="title" type="text" class="input" placeholder="Document title" required />
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Description</label>
          <textarea
            v-model="description"
            class="input resize-none"
            rows="2"
            placeholder="Document description (optional)"
          />
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Tags</label>
          <input
            v-model="tagInput"
            type="text"
            class="input"
            placeholder="Example: HR, policy, onboarding"
            @blur="commitTagInput"
            @keydown="handleTagKeydown"
          />
          <p class="mt-1 text-xs text-slate-400">
            Press Enter or type a comma to add a tag. # and commas are removed automatically.
          </p>
          <div v-if="tags.length > 0" class="mt-2 flex flex-wrap gap-1">
            <span
              v-for="tag in tags"
              :key="tag"
              class="inline-flex items-center gap-1 rounded-full bg-primary-50 px-2 py-0.5 text-xs text-primary-700"
            >
              {{ tag }}
              <button type="button" class="rounded hover:bg-primary-100" @click="removeTag(tag)">
                <X class="h-3 w-3" />
              </button>
            </span>
          </div>
        </div>

        <div v-if="uploading" class="space-y-1">
          <div class="flex justify-between text-xs text-slate-500">
            <span>Uploading...</span>
            <span>{{ progress }}%</span>
          </div>
          <div class="h-1.5 overflow-hidden rounded-full bg-surface-200">
            <div class="h-full bg-primary-500 transition-all" :style="{ width: `${progress}%` }" />
          </div>
        </div>

        <div v-if="error" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{{ error }}</div>

        <div class="flex justify-end gap-3">
          <button type="button" class="btn-secondary" @click="$emit('close')">Cancel</button>
          <button type="submit" class="btn-primary" :disabled="!file || !title || uploading">
            <Loader2 v-if="uploading" class="h-4 w-4 animate-spin" />
            {{ uploading ? 'Uploading...' : 'Upload' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { FileText, Loader2, X } from 'lucide-vue-next'
import { useDocumentStore } from '@/stores/document'

const emit = defineEmits<{ close: []; success: [] }>()
const store = useDocumentStore()

const fileInput = ref<HTMLInputElement>()
const file = ref<File | null>(null)
const title = ref('')
const description = ref('')
const tagInput = ref('')
const tags = ref<string[]>([])
const uploading = ref(false)
const progress = ref(0)
const error = ref('')

const maxTags = 8

const canAddMoreTags = computed(() => tags.value.length < maxTags)

function sanitizeTagParts(value: string): string[] {
  return value
    .split(/[,\n\r，]+/)
    .map((entry) => entry.replace(/#/g, ' ').replace(/\s+/g, ' ').trim())
    .filter(Boolean)
}

function appendTags(values: string[]) {
  if (values.length === 0 || !canAddMoreTags.value) return

  const seen = new Set(tags.value.map((entry) => entry.toLowerCase()))
  const nextTags = [...tags.value]

  for (const value of values) {
    const normalizedKey = value.toLowerCase()
    if (seen.has(normalizedKey)) continue
    nextTags.push(value)
    seen.add(normalizedKey)
    if (nextTags.length >= maxTags) break
  }

  tags.value = nextTags
}

function commitTagInput() {
  appendTags(sanitizeTagParts(tagInput.value))
  tagInput.value = ''
}

function removeTag(tag: string) {
  tags.value = tags.value.filter((entry) => entry !== tag)
}

function handleTagKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' || event.key === ',') {
    event.preventDefault()
    commitTagInput()
  }
}

function handleFileChange(e: Event) {
  const selectedFile = (e.target as HTMLInputElement).files?.[0]
  if (!selectedFile) return

  file.value = selectedFile
  if (!title.value) {
    title.value = selectedFile.name.replace(/\.pdf$/i, '')
  }
}

function handleDrop(e: DragEvent) {
  const droppedFile = e.dataTransfer?.files[0]
  if (!droppedFile || droppedFile.type !== 'application/pdf') return

  file.value = droppedFile
  if (!title.value) {
    title.value = droppedFile.name.replace(/\.pdf$/i, '')
  }
}

async function handleUpload() {
  if (!file.value) return

  uploading.value = true
  error.value = ''
  progress.value = 10

  try {
    commitTagInput()

    const formData = new FormData()
    formData.append('file', file.value)
    formData.append('title', title.value)
    if (description.value.trim()) formData.append('description', description.value.trim())
    tags.value.forEach((tag) => formData.append('tags', tag))

    progress.value = 50
    await store.uploadDocument(formData)
    progress.value = 100
    emit('success')
  } catch {
    error.value = 'Upload failed. Please try again.'
  } finally {
    uploading.value = false
  }
}
</script>
