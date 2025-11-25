import java.util.Scanner;

public class SistemaContatos {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== CONVERSOR DE FORMATOS DE CONTATOS ===");
        System.out.println("Este programa lê um arquivo de contatos e converte para todos os formatos suportados.\n");
        
        System.out.print("Digite o nome do arquivo de entrada: ");
        String arquivo = scanner.nextLine();
        
        System.out.println("\nFormatos suportados:");
        System.out.println("1. JSON");
        System.out.println("2. XML");
        System.out.println("3. YAML");
        System.out.println("4. CSV");
        System.out.println("5. TOON");
        System.out.print("\nEscolha o formato do arquivo de entrada (1-5): ");
        
        int opcao = scanner.nextInt();
        scanner.nextLine();
        
        String formato = "";
        switch (opcao) {
            case 1: formato = "JSON"; break;
            case 2: formato = "XML"; break;
            case 3: formato = "YAML"; break;
            case 4: formato = "CSV"; break;
            case 5: formato = "TOON"; break;
            default:
                System.out.println("Opção inválida!");
                scanner.close();
                return;
        }
        
        try {
            GerenciadorContatos gerenciador = new GerenciadorContatos();
            gerenciador.converterFormatos(arquivo, formato);
            
            System.out.println("\n=== CONTATOS CARREGADOS ===");
            gerenciador.listarContatos();
            
        } catch (Exception e) {
            System.out.println("\nErro ao processar arquivo: " + e.getMessage());
            System.out.println("\nCertifique-se de que:");
            System.out.println("- O arquivo existe na mesma pasta do programa");
            System.out.println("- O formato do arquivo está correto");
            System.out.println("- Os dados estão no formato esperado");
        }
        
        scanner.close();
    }
}
