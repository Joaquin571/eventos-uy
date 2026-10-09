const Datos = {

    categorias: [
        "Tecnología",
        "Cultura",
        "Deporte",
        "Música",
        "Negocios"
    ],

    eventos: [
        {
            id: 1,
            nombre: "Montevideo Comics",
            descripcion: "Convención de historietas y cultura geek"
        },
        {
            id: 2,
            nombre: "Maratón de Montevideo",
            descripcion: "Evento deportivo"
        }
    ],


    ediciones: [
        {
            id: 1,
            eventoId: 1,
            nombre: "Montevideo Comics 2026",
            estado: "Aceptada",
            organizadorId: 1,
            nombreOrganizador: "Organizador de prueba 1",
            sigla: "MC2026",
            ciudad: "Montevideo",
            pais: "Uruguay",
            fechaInicio: "2026-11-10",
            fechaFin: "2026-11-12",
            fechaAlta: "2026-05-15",
            imagen: ""
        },
        {
            id: 2,
            eventoId: 1,
            nombre: "Montevideo Comics 2027",
            estado: "Ingresada",
            organizadorId: 1,
            nombreOrganizador: "Organizador de prueba 1",
            sigla: "MC2027",
            ciudad: "Montevideo",
            pais: "Uruguay",
            fechaInicio: "2027-11-10",
            fechaFin: "2027-11-12",
            fechaAlta: "2026-09-20",
            imagen: ""
        },
        {
            id: 3,
            eventoId: 2,
            nombre: "Maratón de Montevideo 2026",
            estado: "Aceptada",
            organizadorId: 2,
            nombreOrganizador: "Organizador de prueba 2",
            sigla: "MM2026",
            ciudad: "Montevideo",
            pais: "Uruguay",
            fechaInicio: "2026-11-22",
            fechaFin: "2026-11-22",
            fechaAlta: "2026-04-10",
            imagen: ""
        }
    ],

    instituciones: [
        {
            id: 1,
            nombre: "UTEC",
            descripcion: "Universidad Tecnológica",
            sitioWeb: "https://www.utec.edu.uy",
            imagen: "../img/utec.png"
        },
        {
            id: 2,
            nombre: "ANTEL",
            descripcion: "Empresa de telecomunicaciones",
            sitioWeb: "https://www.antel.com.uy",
            imagen: "../img/antel.png"
        }
    ],

    tiposRegistro: [
        {
            id: 1,
            edicionId: 1,
            nombre: "General",
            costo: 1500,
            cupo: 100
        },
        {
            id: 2,
            edicionId: 1,
            nombre: "Estudiante",
            costo: 800,
            cupo: 50
        }
    ],

    patrocinios: [
        {
            id: 1,
            edicionId: 1,
            institucionId: 1,
            nivel: "Oro",
            aporte: 50000,
            tipoRegistroId: 1,
            cantidadGratuitos: 5,
            codigo: "UTEC2026",
            fechaAlta: "2026-10-01"
        }
    ]
};
window.App = window.App || {};
App.datos = Datos;