import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;


class GerenciadorContatos {
    private ArrayList<Contato> contatos;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GerenciadorContatos() {
        this.contatos = new ArrayList<>();
    }

    public void adicionarContato(Contato contato) {
        // verifica se tem id repetido
        for (Contato c : contatos) {
            if (c.getId() == contato.getId()) {
                // Gera um novo ID único
                int novoId = gerarNovoId();
                System.out.println("⚠ Aviso: ID " + contato.getId() + " duplicado. Alterado para ID " + novoId);
                contato.setId(novoId);
                break;
            }
        }
        contatos.add(contato);
    }
    
    // metodo auxiliar
    private int gerarNovoId() {
        int maxId = 0;
        for (Contato c : contatos) {
            if (c.getId() > maxId) {
                maxId = c.getId();
            }
        }
        return maxId + 1;
    }

    public ArrayList<Contato> getContatos() {
        return contatos;
    }

    public void listarContatos() {
        if (contatos.isEmpty()) {
            System.out.println("Nenhum contato cadastrado.");
            return;
        }
        System.out.println("\n=== LISTA DE CONTATOS ===");
        for (Contato c : contatos) {
            System.out.println(c);
        }
        System.out.println("=========================\n");
    }

    // lê JSON
    public void lerJSON(String arquivo) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                sb.append(linha.trim());
            }
        }
        
        String json = sb.toString();
        json = json.substring(1, json.length() - 1);
        
        
        String[] objetos = json.split("\\}\\s*,\\s*\\{");
        
        for (String obj : objetos) {
            obj = obj.replace("{", "").replace("}", "").trim();
            
            int id = 0;
            String nome = "", email = "", telefone = "", dataNasc = "";
            
            // parser manual
            int pos = 0;
            while (pos < obj.length()) {
                // encontra o iniico da chave
                int chavInicio = obj.indexOf("\"", pos);
                if (chavInicio == -1) break;
                
                int chavFim = obj.indexOf("\"", chavInicio + 1);
                String chave = obj.substring(chavInicio + 1, chavFim);
                
                // pula ":"
                int valorInicio = obj.indexOf(":", chavFim) + 1;
                
                // determina o final
                String valor;
                if (obj.charAt(valorInicio) == '"' || (valorInicio < obj.length() && obj.substring(valorInicio).trim().startsWith("\""))) {
                    // valor é string
                    valorInicio = obj.indexOf("\"", valorInicio) + 1;
                    int valorFim = obj.indexOf("\"", valorInicio);
                    valor = obj.substring(valorInicio, valorFim);
                    pos = valorFim + 1;
                } else {
                    // valor é numero
                    int valorFim = obj.indexOf(",", valorInicio);
                    if (valorFim == -1) {
                        valorFim = obj.length();
                    }
                    valor = obj.substring(valorInicio, valorFim).trim();
                    pos = valorFim + 1;
                }
                
                switch (chave) {
                    case "id": id = Integer.parseInt(valor); break;
                    case "nome": nome = valor; break;
                    case "email": email = valor; break;
                    case "telefone": telefone = valor; break;
                    case "dataNascimento": dataNasc = valor; break;
                }
            }
            
            LocalDate data = LocalDate.parse(dataNasc, DATE_FORMATTER);
            
            // verifica se tem id repetido
            for (Contato c : contatos) {
                if (c.getId() == id) {
                    int idOriginal = id;
                    id = gerarNovoId();
                    System.out.println("⚠ Aviso: ID " + idOriginal + " duplicado no JSON. Alterado para ID " + id);
                    break;
                }
            }
            
            contatos.add(new Contato(id, nome, email, telefone, data));
        }
    }

    // escreve em json
    public void escreverJSON(String arquivo) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(arquivo))) {
            pw.println("[");
            for (int i = 0; i < contatos.size(); i++) {
                Contato c = contatos.get(i);
                pw.println("  {");
                pw.println("    \"id\": " + c.getId() + ",");
                pw.println("    \"nome\": \"" + c.getNome() + "\",");
                pw.println("    \"email\": \"" + c.getEmail() + "\",");
                pw.println("    \"telefone\": \"" + c.getTelefone() + "\",");
                pw.println("    \"dataNascimento\": \"" + c.getDataNascimento().format(DATE_FORMATTER) + "\"");
                pw.print("  }");
                if (i < contatos.size() - 1) {
                    pw.println(",");
                } else {
                    pw.println();
                }
            }
            pw.println("]");
        }
    }

    // le em XML
    public void lerXML(String arquivo) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(arquivo));
        
        NodeList nodeList = doc.getElementsByTagName("contato");
        
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            
            int id = Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent());
            String nome = element.getElementsByTagName("nome").item(0).getTextContent();
            String email = element.getElementsByTagName("email").item(0).getTextContent();
            String telefone = element.getElementsByTagName("telefone").item(0).getTextContent();
            LocalDate data = LocalDate.parse(
                element.getElementsByTagName("dataNascimento").item(0).getTextContent(), 
                DATE_FORMATTER
            );
            
            //  // verifica se tem id repetido
            for (Contato c : contatos) {
                if (c.getId() == id) {
                    int idOriginal = id;
                    id = gerarNovoId();
                    System.out.println("⚠ Aviso: ID " + idOriginal + " duplicado no XML. Alterado para ID " + id);
                    break;
                }
            }
            
            contatos.add(new Contato(id, nome, email, telefone, data));
        }
    }

    // escreve em XML
    public void escreverXML(String arquivo) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        Element root = doc.createElement("contatos");
        doc.appendChild(root);
        
        for (Contato c : contatos) {
            Element contato = doc.createElement("contato");
            
            Element id = doc.createElement("id");
            id.setTextContent(String.valueOf(c.getId()));
            contato.appendChild(id);
            
            Element nome = doc.createElement("nome");
            nome.setTextContent(c.getNome());
            contato.appendChild(nome);
            
            Element email = doc.createElement("email");
            email.setTextContent(c.getEmail());
            contato.appendChild(email);
            
            Element telefone = doc.createElement("telefone");
            telefone.setTextContent(c.getTelefone());
            contato.appendChild(telefone);
            
            Element data = doc.createElement("dataNascimento");
            data.setTextContent(c.getDataNascimento().format(DATE_FORMATTER));
            contato.appendChild(data);
            
            root.appendChild(contato);
        }
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(arquivo));
        transformer.transform(source, result);
    }

    // lê YAML
    public void lerYAML(String arquivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int id = 0;
            String nome = "", email = "", telefone = "", dataNasc = "";
            
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                
                if (linha.startsWith("- id:")) {
                    if (id != 0) {
                        LocalDate data = LocalDate.parse(dataNasc, DATE_FORMATTER);
                        contatos.add(new Contato(id, nome, email, telefone, data));
                    }
                    id = Integer.parseInt(linha.substring(5).trim());
                } else if (linha.startsWith("id:")) {
                    id = Integer.parseInt(linha.substring(3).trim());
                } else if (linha.startsWith("nome:")) {
                    nome = linha.substring(5).trim().replace("\"", "").replace("'", "");
                } else if (linha.startsWith("email:")) {
                    email = linha.substring(6).trim().replace("\"", "").replace("'", "");
                } else if (linha.startsWith("telefone:")) {
                    telefone = linha.substring(9).trim().replace("\"", "").replace("'", "");
                } else if (linha.startsWith("dataNascimento:")) {
                    dataNasc = linha.substring(15).trim().replace("\"", "").replace("'", "");
                }
            }
            
            if (id != 0) {
                LocalDate data = LocalDate.parse(dataNasc, DATE_FORMATTER);
                
                // verifica se tem id repetido
                for (Contato c : contatos) {
                    if (c.getId() == id) {
                        int idOriginal = id;
                        id = gerarNovoId();
                        System.out.println("⚠ Aviso: ID " + idOriginal + " duplicado no YAML. Alterado para ID " + id);
                        break;
                    }
                }
                
                contatos.add(new Contato(id, nome, email, telefone, data));
            }
        }
    }

    // escreve em YAML
    public void escreverYAML(String arquivo) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(arquivo))) {
            for (Contato c : contatos) {
                pw.println("- id: " + c.getId());
                pw.println("  nome: \"" + c.getNome() + "\"");
                pw.println("  email: \"" + c.getEmail() + "\"");
                pw.println("  telefone: \"" + c.getTelefone() + "\"");
                pw.println("  dataNascimento: \"" + c.getDataNascimento().format(DATE_FORMATTER) + "\"");
            }
        }
    }

    // lê em CSV
    public void lerCSV(String arquivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha = br.readLine(); // Pula cabeçalho
            
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(",");
                int id = Integer.parseInt(dados[0].trim());
                String nome = dados[1].trim();
                String email = dados[2].trim();
                String telefone = dados[3].trim();
                LocalDate data = LocalDate.parse(dados[4].trim(), DATE_FORMATTER);
                
                // verifica se tem id repetido
                for (Contato c : contatos) {
                    if (c.getId() == id) {
                        int idOriginal = id;
                        id = gerarNovoId();
                        System.out.println("⚠ Aviso: ID " + idOriginal + " duplicado no CSV. Alterado para ID " + id);
                        break;
                    }
                }
                
                contatos.add(new Contato(id, nome, email, telefone, data));
            }
        }
    }

    // escreve em CSV
    public void escreverCSV(String arquivo) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(arquivo))) {
            pw.println("id,nome,email,telefone,dataNascimento");
            
            for (Contato c : contatos) {
                pw.printf("%d,%s,%s,%s,%s%n",
                    c.getId(),
                    c.getNome(),
                    c.getEmail(),
                    c.getTelefone(),
                    c.getDataNascimento().format(DATE_FORMATTER)
                );
            }
        }
    }

    // lê TOON
    public void lerTOON(String arquivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int id = 0;
            String nome = "", email = "", telefone = "";
            LocalDate data = null;
            boolean dentroContato = false;
            
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                
                if (linha.startsWith("contato:")) {
                    if (dentroContato && id != 0) {
                        // Verifica se já existe um contato com o mesmo ID
                        for (Contato c : contatos) {
                            if (c.getId() == id) {
                                int idOriginal = id;
                                id = gerarNovoId();
                                System.out.println("⚠ Aviso: ID " + idOriginal + " duplicado no TOON. Alterado para ID " + id);
                                break;
                            }
                        }
                        contatos.add(new Contato(id, nome, email, telefone, data));
                    }
                    dentroContato = true;
                    id = 0;
                    nome = "";
                    email = "";
                    telefone = "";
                    data = null;
                } else if (dentroContato && linha.contains("::")) {
                    String[] partes = linha.split("::", 2);
                    String campoValor = partes[0].trim();
                    
                    if (campoValor.contains(":")) {
                        String[] cv = campoValor.split(":", 2);
                        String campo = cv[0].trim();
                        String valor = cv[1].trim().replace("\"", "");
                        
                        switch (campo) {
                            case "id":
                                id = Integer.parseInt(valor);
                                break;
                            case "nome":
                            case "name":
                                nome = valor;
                                break;
                            case "email":
                                email = valor;
                                break;
                            case "telefone":
                            case "phone":
                                telefone = valor;
                                break;
                            case "dataNascimento":
                            case "birthDate":
                                data = LocalDate.parse(valor, DATE_FORMATTER);
                                break;
                        }
                    }
                } else if (linha.isEmpty() && dentroContato && id != 0) {
                	 // verifica se tem id repetido
                    for (Contato c : contatos) {
                        if (c.getId() == id) {
                            int idOriginal = id;
                            id = gerarNovoId();
                            System.out.println("⚠ Aviso: ID " + idOriginal + " duplicado no TOON. Alterado para ID " + id);
                            break;
                        }
                    }
                    contatos.add(new Contato(id, nome, email, telefone, data));
                    dentroContato = false;
                }
            }
            
            if (dentroContato && id != 0) {
            	 // verifica se tem id repetido
                for (Contato c : contatos) {
                    if (c.getId() == id) {
                        int idOriginal = id;
                        id = gerarNovoId();
                        System.out.println("⚠ Aviso: ID " + idOriginal + " duplicado no TOON. Alterado para ID " + id);
                        break;
                    }
                }
                contatos.add(new Contato(id, nome, email, telefone, data));
            }
        }
    }

    // escreve em TOON
    public void escreverTOON(String arquivo) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(arquivo))) {
            for (Contato c : contatos) {
                pw.println("contato:");
                pw.println("  id: " + c.getId() + " :: int(required)");
                pw.println("  nome: \"" + c.getNome() + "\" :: string(required)");
                pw.println("  email: \"" + c.getEmail() + "\" :: email(required)");
                pw.println("  telefone: \"" + c.getTelefone() + "\" :: string(required)");
                pw.println("  dataNascimento: \"" + c.getDataNascimento().format(DATE_FORMATTER) + "\" :: date(required)");
                pw.println();
            }
        }
    }

    // converte pros outros formatos
    public void converterFormatos(String arquivoOrigem, String formato) throws Exception {
        System.out.println("Lendo arquivo: " + arquivoOrigem);
        
        switch (formato.toUpperCase()) {
            case "JSON": lerJSON(arquivoOrigem); break;
            case "XML": lerXML(arquivoOrigem); break;
            case "YAML": lerYAML(arquivoOrigem); break;
            case "CSV": lerCSV(arquivoOrigem); break;
            case "TOON": lerTOON(arquivoOrigem); break;
            default: throw new IllegalArgumentException("Formato não suportado: " + formato);
        }
        
        System.out.println("Contatos carregados: " + contatos.size());
        
        System.out.println("\nConvertendo para todos os formatos...");
        escreverJSON("contatos.json");
        System.out.println("✓ JSON criado");
        
        escreverXML("contatos.xml");
        System.out.println("✓ XML criado");
        
        escreverYAML("contatos.yaml");
        System.out.println("✓ YAML criado");
        
        escreverCSV("contatos.csv");
        System.out.println("✓ CSV criado");
        
        escreverTOON("contatos.toon");
        System.out.println("✓ TOON criado");
        
        System.out.println("\nConversão concluída com sucesso!");
    }
}
