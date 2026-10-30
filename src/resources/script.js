let tempoRestante = 30;
let timerInterval = null;
let usuarioAutenticado = false;

// ==========================================================================
// 1. CONTROLE DE SESSÃO E INATIVIDADE (30 SEGUNDOS)
// ==========================================================================
function iniciarTemporizador() {
    tempoRestante = 30;
    atualizarDisplayTimer();

    if (timerInterval) clearInterval(timerInterval);

    timerInterval = setInterval(() => {
        if (!usuarioAutenticado) return;

        tempoRestante--;
        atualizarDisplayTimer();

        if (tempoRestante <= 0) {
            clearInterval(timerInterval);
            alert("Sessão expirada por inatividade (30 segundos)!"); // Expiração por inatividade[cite: 2]
            encerrarSessao();
        }
    }, 1000);
}

function reiniciarContadorAtividade() {
    if (usuarioAutenticado) {
        tempoRestante = 30; // Reinicia a contagem a cada interação do utilizador[cite: 2]
        atualizarDisplayTimer();
    }
}

function atualizarDisplayTimer() {
    document.getElementById('timer').innerText = tempoRestante + "s";
}

// Escuta movimentos de rato e pressionamento de teclas para reiniciar a sessão[cite: 2]
window.onload = () => {
    document.onmousemove = reiniciarContadorAtividade;
    document.onkeypress = reiniciarContadorAtividade;
};

// ==========================================================================
// 2. AUTENTICAÇÃO E NAVEGAÇÃO
// ==========================================================================
function realizarLogin(event) {
    event.preventDefault();
    usuarioAutenticado = true;

    document.getElementById('login-screen').classList.add('hidden');
    document.getElementById('main-panel').classList.remove('hidden');

    iniciarTemporizador();
}

function encerrarSessao() {
    usuarioAutenticado = false;
    clearInterval(timerInterval);

    document.getElementById('login-screen').classList.remove('hidden');
    document.getElementById('main-panel').classList.add('hidden');
    document.getElementById('timer').innerText = "30s";
}

function mudarAba(abaId, elementoBotao) {
    document.querySelectorAll('.section').forEach(sec => sec.classList.remove('active'));
    document.querySelectorAll('.nav-btn').forEach(btn => {
        if (!btn.classList.contains('btn-logout')) btn.classList.remove('active');
    });

    document.getElementById(abaId).classList.add('active');
    elementoBotao.classList.add('active');
}

// ==========================================================================
// 3. MANIPULAÇÃO DE DADOS DA INTERFACE
// ==========================================================================
function adicionarTutor(event) {
    event.preventDefault();

    const nome = document.getElementById('tutor-nome').value;
    const tabela = document.getElementById('tabela-tutores');
    const novaLinha = tabela.insertRow();

    novaLinha.innerHTML = `
        <td>${tabela.rows.length}</td>
        <td>${nome}</td>
        <td><span class="badge-encrypted">AES</span> [Dado Cifrado]</td>
        <td><span class="badge-encrypted">AES</span> [Dado Cifrado]</td>
    `;

    event.target.reset();
}

function adicionarAgendamento(event) {
    event.preventDefault();
    alert("Agendamento submetido com sucesso!");
    event.target.reset();
}