function cargarSelect(select, elementos, textoInicial) {
    select.innerHTML = "";

    const opcionInicial = document.createElement("option");
    opcionInicial.value = "";
    opcionInicial.textContent = textoInicial;

    select.appendChild(opcionInicial);

    elementos.forEach(elemento => {
        const option = document.createElement("option");
        option.value = elemento.id;
        option.textContent = elemento.nombre;

        select.appendChild(option);
    });
}
