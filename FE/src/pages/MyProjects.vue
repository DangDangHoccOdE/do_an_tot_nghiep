<template>
    <div class="min-h-screen bg-gray-50">
        <!-- Main Content -->
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <!-- Page Title -->
            <div class="mb-8">
                <h1 class="text-3xl font-bold text-gray-900">{{ $t('myProjects.title') }}</h1>
                <p class="mt-2 text-gray-600">{{ $t('myProjects.subtitle') }}</p>
            </div>

            <!-- Loading State -->
            <div v-if="loading" class="flex justify-center items-center py-20">
                <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-[#CE181E]"></div>
            </div>

            <!-- Empty State -->
            <div v-else-if="!projects.length" class="text-center py-20">
                <svg class="mx-auto h-16 w-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                        d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                </svg>
                <h3 class="mt-4 text-lg font-medium text-gray-900">{{ $t('myProjects.noProjects') }}</h3>
            </div>

            <!-- Projects Grid -->
            <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div v-for="project in projects" :key="project.id"
                    class="bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-shadow duration-200 overflow-hidden">
                    <!-- Project Header -->
                    <div class="p-6 border-b border-gray-100">
                        <div class="flex items-start justify-between">
                            <h3 class="text-lg font-semibold text-gray-900 line-clamp-2">{{ project.name }}</h3>
                            <span :class="getStatusClass(project.status)"
                                class="ml-2 px-2.5 py-0.5 text-xs font-medium rounded-full whitespace-nowrap">
                                {{ getStatusLabel(project.status) }}
                            </span>
                        </div>
                        <p class="mt-2 text-sm text-gray-600 line-clamp-3">{{ project.description || 'Không có mô tả' }}
                        </p>
                    </div>

                    <!-- Project Info -->
                    <div class="px-6 py-4 space-y-3">
                        <div class="flex items-center text-sm text-gray-500">
                            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                            <span>{{ formatDate(project.startDate) }} - {{ formatDate(project.endDate) }}</span>
                        </div>

                        <div v-if="project.teamName" class="flex items-center text-sm text-gray-500">
                            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                            </svg>
                            <span>{{ project.teamName }}</span>
                        </div>

                        <div v-if="project.budgetEstimated" class="flex items-center text-sm text-gray-500">
                            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            <span>{{ formatCurrency(project.budgetEstimated) }}</span>
                        </div>
                    </div>

                    <!-- Project Actions -->
                    <div class="px-6 py-4 bg-gray-50 border-t border-gray-100">
                        <router-link :to="`/admin/projects/current`"
                            class="inline-flex items-center text-sm font-medium text-[#CE181E] hover:text-[#f93535] transition-colors no-underline">
                            {{ $t('myProjects.viewDetails') }}
                            <svg class="ml-1 w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M9 5l7 7-7 7" />
                            </svg>
                        </router-link>
                    </div>
                </div>
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="mt-8 flex justify-center">
                <nav class="flex items-center gap-2">
                    <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                        class="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed">
                        Trước
                    </button>
                    <span class="px-4 py-2 text-sm text-gray-700">
                        Trang {{ currentPage + 1 }} / {{ totalPages }}
                    </span>
                    <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages - 1"
                        class="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed">
                        Sau
                    </button>
                </nav>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { apiProjects } from '@/services/apiProjects';

const { t } = useI18n();

const loading = ref(true);
const projects = ref([]);
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = 9;

const fetchMyProjects = async () => {
    loading.value = true;
    try {
        const response = await apiProjects.myProjects({
            page: currentPage.value,
            size: pageSize
        });

        if (response.content) {
            projects.value = response.content;
            totalPages.value = response.totalPages || 1;
        } else if (Array.isArray(response)) {
            projects.value = response;
        }
    } catch (error) {
        console.error('Error fetching projects:', error);
        projects.value = [];
    } finally {
        loading.value = false;
    }
};

const changePage = (page) => {
    if (page >= 0 && page < totalPages.value) {
        currentPage.value = page;
        fetchMyProjects();
    }
};

const getStatusClass = (status) => {
    const classes = {
        current: 'bg-green-100 text-green-800',
        future: 'bg-blue-100 text-blue-800',
        completed: 'bg-gray-100 text-gray-800'
    };
    return classes[status] || 'bg-gray-100 text-gray-800';
};

const getStatusLabel = (status) => {
    return t(`myProjects.status.${status}`) || status;
};

const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
};

const formatCurrency = (amount) => {
    if (!amount) return 'N/A';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
};

onMounted(() => {
    fetchMyProjects();
});
</script>

<style scoped>
.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.line-clamp-3 {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
</style>
