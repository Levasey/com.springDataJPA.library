(function () {
  function syncToggle(input, btn, visible) {
    input.type = visible ? "text" : "password";
    btn.setAttribute("aria-pressed", visible ? "true" : "false");
    btn.setAttribute("aria-label", visible ? "Скрыть пароль" : "Показать пароль");
    btn.textContent = visible ? "Скрыть" : "Показать";
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".password-toggle").forEach(function (btn) {
      var id = btn.getAttribute("aria-controls");
      if (!id) return;
      var input = document.getElementById(id);
      if (!input) return;
      btn.addEventListener("click", function () {
        syncToggle(input, btn, input.type === "password");
      });
    });
  });
})();
