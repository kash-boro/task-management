// Simple client-side confirmation before deleting something. Reused for both
// task deletion and user deletion, with a message appropriate to each.
function confirmDelete(message) {
    return confirm(message || "Are you sure you want to delete this task?");
}
function confirmDeleteUser() {
    return confirmDelete("Are you sure you want to delete this user? This will also delete all of their tasks.");
}
function confirmDeleteAccount() {
    return confirm("Are you sure you want to permanently delete your account? This cannot be undone and you will be logged out immediately.");
}

// Generic open/close for any modal dialog box, identified by its element id.
// Used by both the Add Task modal and the Add User modal.
function openModal(modalId) {
    document.getElementById(modalId).classList.add('show');
}
function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('show');
}

// Kept as thin wrappers so existing onclick="openAddModal()" calls on the
// tasks page still work without needing to touch that template again.
function openAddModal() {
    openModal('addTaskModal');
}
function closeAddModal() {
    closeModal('addTaskModal');
}

// Shows a loading spinner overlay while a form submits (add/edit task).
function showSpinner() {
    const overlay = document.getElementById('spinnerOverlay');
    if (overlay) overlay.classList.add('show');
}

// Toggles dark mode and remembers the choice in localStorage.
function toggleDarkMode() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    if (isDark) {
        document.documentElement.removeAttribute('data-theme');
        localStorage.setItem('theme', 'light');
    } else {
        document.documentElement.setAttribute('data-theme', 'dark');
        localStorage.setItem('theme', 'dark');
    }
}
