import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Base64;
import java.util.Scanner;

public class PetShopApp {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/petshop";
    private static final String DB_USER = "root";
    private static final String DB_PASS = " ";

    // Recupera a chave AES da variável de ambiente (deve ter 16 caracteres)
    private static final String AES_KEY = System.getenv("AES_KEY") != null ? System.getenv("AES_KEY") : "Chave16BytesAtend!";
    private static long ultimaAtividade = 0;

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("--- PETSHOP SYSTEM ---");
            System.out.print("Login: ");
            String login = scanner.nextLine();
            System.out.print("Senha: ");
            String senha = scanner.nextLine();

            if (autenticar(conn, login, senha)) {
                renovarSessao();
                System.out.println("Login realizado com sucesso!");

                // Exemplo de operação protegida por sessão
                executarOperacaoComSessao(conn, scanner);
            } else {
                System.out.println("Credenciais inválidas.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- CONTROLE DE SESSÃO (30 SEGUNDOS DE INATIVIDADE) ---
    private static void renovarSessao() {
        ultimaAtividade = System.currentTimeMillis();
    }

    private static boolean validarSessao() {
        if (System.currentTimeMillis() - ultimaAtividade > 30000) { // 30 segundos
            System.out.println("\n[Sessão Expirada por inatividade. Faça login novamente.]");
            return false;
        }
        renovarSessao();
        return true;
    }

    private static void executarOperacaoComSessao(Connection conn, Scanner scanner) throws Exception {
        if (!validarSessao()) return;

        System.out.println("\n1. Cadastrar Tutor");
        System.out.println("2. Agendar Serviço");
        System.out.print("Opção: ");
        String op = scanner.nextLine();

        if (!validarSessao()) return;

        if ("1".equals(op)) {
            System.out.print("Nome: "); String nome = scanner.nextLine();
            System.out.print("CPF: "); String cpf = scanner.nextLine();
            System.out.print("Telefone: "); String tel = scanner.nextLine();
            cadastrarTutor(conn, nome, cpf, tel);
        } else if ("2".equals(op)) {
            System.out.print("Data/Hora (YYYY-MM-DD HH:MM:SS): "); String dh = scanner.nextLine();
            System.out.print("ID Pet: "); int petId = Integer.parseInt(scanner.nextLine());
            agendarServico(conn, dh, "banho", petId);
        }
    }

    // --- SEGURANÇA: AES E HASH PBKDF2 ---
    public static String criptografarAES(String dados) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return Base64.getEncoder().encodeToString(cipher.doFinal(dados.getBytes(StandardCharsets.UTF_8)));
    }

    public static String gerharHashSenha(String senha, String salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt.getBytes(), 65536, 128);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    // --- REGRAS DE NEGÓCIO E CRUD ---
    private static boolean autenticar(Connection conn, String login, String senha) throws Exception {
        String sql = "SELECT senha_hash FROM usuario WHERE login = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashBanco = rs.getString("senha_hash");
                return hashBanco.equals(gerharHashSenha(senha, login)); // Usa o login como salt simplificado
            }
        }
        return false;
    }

    private static void cadastrarTutor(Connection conn, String nome, String cpf, String telefone) throws Exception {
        String sql = "INSERT INTO tutor (nome, cpf_enc, telefone_enc) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, criptografarAES(cpf));       // Grava CPF Criptografado
            stmt.setString(3, criptografarAES(telefone));  // Grava Telefone Criptografado
            stmt.executeUpdate();
            System.out.println("Tutor cadastrado com sucesso (dados sensíveis criptografados).");
        }
    }

    private static void agendarServico(Connection conn, String dataHora, String servico, int petId) {
        String sql = "INSERT INTO agendamento (data_hora, servico, pet_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dataHora);
            stmt.setString(2, servico);
            stmt.setInt(3, petId);
            stmt.executeUpdate();
            System.out.println("Agendamento realizado!");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Código de erro do MySQL para UNIQUE duplicado
                System.out.println("Erro: Já existe um agendamento para este horário!");
            } else {
                e.printStackTrace();
            }
        }
    }
}