package br.com.devd2.helper;

import br.com.devd2.infra.ServiceException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class UtilHelper {

    private static final Random random = new Random();
    private static final int[] DIAS_MES = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final String[] MESES = {"", "Janeiro", "Fevereiro", "Março", "Abril", "Maio",
            "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[0-9a-zA-Z]([.!#$%&'*+/=?^`{|}~\\w-]*[0-9a-zA-Z])?@([0-9a-zA-Z][\\w-]*[0-9a-zA-Z]*\\.)+[a-zA-Z]{2,24}$"
    );

    private static final Pattern SENHA_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\da-zA-Z]).{8,15}$"
    );

    public static String obterNomeMes(int mes) throws ServiceException {
        if (mes < 1 || mes > 12) {
            throw new ServiceException("Mês (" + mes + ") inválido.");
        }
        return MESES[mes];
    }

    public static String obterNomeDiaSemana(LocalDate data) {
        return switch (data.getDayOfWeek()) {
            case SUNDAY -> "Domingo";
            case MONDAY -> "Segunda-feira";
            case TUESDAY -> "Terça-feira";
            case WEDNESDAY -> "Quarta-feira";
            case THURSDAY -> "Quinta-feira";
            case FRIDAY -> "Sexta-feira";
            case SATURDAY -> "Sábado";
        };
    }

    public static Map<String, String> separarEmails(String emails) {
        return separarEmails(emails, ',');
    }

    public static Map<String, String> separarEmails(String emails, char separador) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String item : emails.split(String.valueOf(separador))) {
            String nome = "";
            String email = item.replace(";", "");
            if (item.contains("(") && item.contains(")")) {
                int idxFim = item.indexOf("(");
                nome = item.substring(0, idxFim);
                email = item.substring(idxFim)
                        .replace("(", "")
                        .replace(")", "")
                        .replace(";", "");
            }
            result.put(email.trim(), nome.trim());
        }
        return result;
    }

    public static int obterDiasMes(int mes) {
        if (mes < 1 || mes > 12) return 0;
        return DIAS_MES[mes];
    }

    public static int obterDiasMes(int mes, int ano) {
        int dias = obterDiasMes(mes);
        if (dias == 0) return 0;
        if (mes == 2 && ((ano % 400 == 0) || (ano % 4 == 0 && ano % 100 != 0))) dias = 29;
        return dias;
    }

    public static String obterNomeReduzido(String nomeCompleto) {
        return converterPrimeiraLetraEmMaiusculo(nomeCompleto.split(" ")[0]);
    }

    public static String converterPrimeiraLetraEmMaiusculo(String valor) {
        return Character.toUpperCase(valor.charAt(0)) + valor.substring(1);
    }

    public static int gerarIdRandom() {
        String strId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));
        strId += random.nextInt(1001, 9999);
        return Integer.parseInt(strId);
    }

    public static LocalDate obterProximaData(LocalDate data, int qtdDias) {
        return obterProximaData(data, qtdDias, false);
    }

    public static LocalDate obterProximaData(LocalDate data, int qtdDias, boolean hasDiaUtil) {
        LocalDate novaData = data.plusDays(qtdDias);
        if (hasDiaUtil) {
            if (novaData.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
                novaData = novaData.plusDays(2);
            } else if (novaData.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                novaData = novaData.plusDays(1);
            }
        }
        return novaData;
    }

    public static boolean validarCNPJ(String numeroCnpj) {
        String cnpj = numeroCnpj.replace(".", "").replace("/", "").replace("-", "");
        String ftmt = "6543298765432";
        int[] digitos = new int[14];
        int[] soma = new int[]{0, 0};
        int[] resultado = new int[]{0, 0};
        boolean[] cnpjOk = new boolean[]{false, false};
        try {
            for (int nrDig = 0; nrDig < 14; nrDig++) {
                digitos[nrDig] = Integer.parseInt(cnpj.substring(nrDig, nrDig + 1));
                if (nrDig <= 11)
                    soma[0] += digitos[nrDig] * Integer.parseInt(ftmt.substring(nrDig + 1, nrDig + 2));
                if (nrDig <= 12)
                    soma[1] += digitos[nrDig] * Integer.parseInt(ftmt.substring(nrDig, nrDig + 1));
            }
            for (int nrDig = 0; nrDig < 2; nrDig++) {
                resultado[nrDig] = soma[nrDig] % 11;
                cnpjOk[nrDig] = (resultado[nrDig] == 0 || resultado[nrDig] == 1)
                        ? digitos[12 + nrDig] == 0
                        : digitos[12 + nrDig] == (11 - resultado[nrDig]);
            }
            return cnpjOk[0] && cnpjOk[1];
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validarCPF(String numeroCpf) {
        if (numeroCpf.equals("000.000.000-00") || numeroCpf.equals("111.111.111-11") ||
                numeroCpf.equals("222.222.222-22") || numeroCpf.equals("333.333.333-33") ||
                numeroCpf.equals("444.444.444-44") || numeroCpf.equals("555.555.555-55") ||
                numeroCpf.equals("666.666.666-66") || numeroCpf.equals("777.777.777-77") ||
                numeroCpf.equals("888.888.888-88") || numeroCpf.equals("999.999.999-99")) {
            return false;
        }
        int[] mult1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] mult2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
        numeroCpf = numeroCpf.trim().replace(".", "").replace("-", "");
        if (numeroCpf.length() != 11) return false;
        String tempCpf = numeroCpf.substring(0, 9);
        int soma = 0;
        for (int i = 0; i < 9; i++)
            soma += Character.getNumericValue(tempCpf.charAt(i)) * mult1[i];
        int resto = soma % 11;
        resto = (resto < 2) ? 0 : 11 - resto;
        String digito = String.valueOf(resto);
        tempCpf = tempCpf + digito;
        soma = 0;
        for (int i = 0; i < 10; i++)
            soma += Character.getNumericValue(tempCpf.charAt(i)) * mult2[i];
        resto = soma % 11;
        resto = (resto < 2) ? 0 : 11 - resto;
        digito = digito + resto;
        return numeroCpf.endsWith(digito);
    }

    public static boolean validarEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        try {
            return EMAIL_PATTERN.matcher(email).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validarSenha(String senha) {
        if (senha == null || senha.isEmpty()) return false;
        try {
            return SENHA_PATTERN.matcher(senha).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static String gerarHashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hsh = null;
            for (int i = 0; i <= 16; i++) {
                hsh = md.digest((password + salt).getBytes(StandardCharsets.UTF_8));
            }
            return Base64.getEncoder().encodeToString(hsh);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 not available", e);
        }
    }

    public static String gerarSaltPassword() {
        SecureRandom rng = new SecureRandom();
        int saltSize = rng.nextInt(16, 64);
        byte[] saltBytes = new byte[saltSize];
        rng.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    public static String gerarPasswordRandom() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++)
            sb.append(random.nextInt(i == 0 ? 1 : 0, 9));
        return sb.toString();
    }

    public static String criptografar(String valor) {
        if (valor == null || valor.isEmpty()) return "";
        return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_16LE));
    }

    public static String descriptografar(String valor) {
        if (valor == null || valor.isEmpty()) return null;
        try {
            return new String(Base64.getDecoder().decode(valor), StandardCharsets.UTF_16LE);
        } catch (Exception e) {
            return valor;
        }
    }
}
