// Guarda de acceso por rol. Uso en <head>:
//   <script src="../js/guard.js" data-roles="organizador"></script>
// data-silent="true": redirige sin mostrar alerta.
(function () {
    const script = document.currentScript;
    const permitidos = (script.dataset.roles || "")
        .split(",")
        .map(r => r.trim().toLowerCase())
        .filter(Boolean);

    let usuario = null;
    try {
        usuario = JSON.parse(sessionStorage.getItem("usuarioLogueado"));
    } catch (e) {
        usuario = null;
    }

    const rol = usuario && usuario.rol ? usuario.rol.toLowerCase() : "visitante";

    if (!permitidos.includes(rol)) {
        if (script.dataset.silent !== "true") {
            alert(rol === "visitante"
                ? "Debés iniciar sesión para acceder a esta página."
                : "Tu rol no tiene acceso a esta página.");
        }
        window.location.replace(rol === "visitante" ? "login.html" : "../index.html");
    }
})();