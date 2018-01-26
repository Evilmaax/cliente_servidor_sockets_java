package ClienteServidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class GerenciadorDeClientes extends Thread {      //Extend threads para poderem rodar mais de uma requisição somultaneamente

    private Socket cliente;
    private String nomeCliente;
    private final String info = "Aplicação Cliente Servidor v1.0 - Desenvolvida por Maximiliano Meyer";
    private static final Map<String, GerenciadorDeClientes> clientes = new HashMap<String, GerenciadorDeClientes>();
    private BufferedReader leitor;
    private PrintWriter escritor;

    public GerenciadorDeClientes(Socket cliente) {
        this.cliente = cliente;
        start();                            //Starta a thread
    }

    @Override
    public void run() {

        //Inicia o cliente e pega o nome dele, importante para algumas funções à frente
        try {
            leitor = new BufferedReader(new InputStreamReader(cliente.getInputStream()));    //Pega o que foi digitado e enviado em tempo real de pacote em pacote  
            escritor = new PrintWriter(cliente.getOutputStream(), true);                     //Por estar setado em true o flush, as mensagens do servidor serão printadas automaticamente
            escritor.println("Qual o seu nome: ");
            String msg = leitor.readLine();
            this.nomeCliente = msg;
            escritor.println("Bem-vindo, " + this.nomeCliente);
            clientes.put(this.nomeCliente, this);                               //Após identificar-se o nome ele vai para a estrutura do maps

            while (true) {
                msg = leitor.readLine();

                //Comando que fecha a conexão e exclui o usuário da lista de conectados
                if (msg.equalsIgnoreCase(Comandos.SAIR)) {
                    escritor.println("Você foi desconectado do servidor");
                    clientes.remove(this.nomeCliente);
                    this.cliente.close();
                } 

                //Comando para listar usuários online
                else if (msg.equalsIgnoreCase(Comandos.LISTA)) {
                    StringBuilder str = new StringBuilder();                    //Trabalha com manipulação de strings
                    for (String C : clientes.keySet()) {                        //Pega a chave que fora adicionada antes nos maps
                        str.append(C);
                        str.append(", ");                                       //Faz a separação dos nomes
                    }
                    str.delete(str.length() - 2, str.length());                 //Apaga a última vírgula para não ficar feio
                    escritor.println("Conectados no momento: " + str);          //Printa os usuários conectados
                } 
                
                //Comando para enviar mensagens entre os usuários conectados
                else if (msg.toLowerCase().startsWith("/msg")) {
                    String nomeDestinatario = msg.substring(4, msg.length());             //Para encontrar o usuário no maps ele desconsidera os 4 primeiros caracteres (/msg)
                    System.out.println("Mensagem enviada para " + nomeDestinatario);      //Uso do system.out porque não foi enviado ao servidor. Foi printado no próprio usuário
                    GerenciadorDeClientes destinatario = clientes.get(nomeDestinatario);  //Cria a variável com a qual irá se corresponder
                    if (destinatario == null) {						  //If para caso não encontre o nome digitado
                        escritor.println("Usuário não conectado");
                    } else {
                        escritor.println("Digite uma mensagem para " + destinatario.getNomeCliente());          //laço para envio de mensagens
                        destinatario.getEscritor().println(this.nomeCliente + " disse: " + leitor.readLine());
                        escritor.println("Mensagem enviada");
                    }
                } 
                				
		//Comando que mostra a hora atual 
                else if (msg.equalsIgnoreCase(Comandos.HORA)) {
                    Locale locale = new Locale("pt", "BR");             	//Define o locale para o padrão brasileiro
                    GregorianCalendar calendario = new GregorianCalendar();  
                    SimpleDateFormat formatador = new SimpleDateFormat("dd' de 'MMMMM' de 'yyyy' - 'HH':'mm'h'", locale); //Usa o padrão BR de data e hora a aplica a formatação especificada
                    escritor.println(formatador.format(calendario.getTime()));
                }
				
		//Comando para ver os trending topics no Brasil
                else if (msg.equalsIgnoreCase(Comandos.TWEET)) {

                    ConfigurationBuilder cf = new ConfigurationBuilder();

                    cf.setDebugEnabled(true)                                     //Aqui são setados os dados de conexão únicos e exclusivos gerados 
                            .setOAuthConsumerKey("INSIRA OS SEUS DADOS ÚNICOS DE USUÁRIO AQUI")    //pelo próprio Twitter para cada usuário que solicita o uso da API
                            .setOAuthConsumerSecret("INSIRA OS SEUS DADOS ÚNICOS DE USUÁRIO AQUI")
                            .setOAuthAccessToken("INSIRA OS SEUS DADOS ÚNICOS DE USUÁRIO AQUI")
                            .setOAuthAccessTokenSecret("INSIRA OS SEUS DADOS ÚNICOS DE USUÁRIO AQUI");

                    Twitter twitter = new TwitterFactory(cf.build()).getInstance();

                    Trends trends = twitter.getPlaceTrends(23424768);      //Define o local de busca dos trends de acordo com o índice WOEID. Aqui está configurado para o Brasil
                    escritor.println("Os seguintes tópicos são trending no Brasil:\n \n ");
                    for (int i = 0; i < trends.getTrends().length; i++) {  //Laço que printa todos os trendig topics do momento da consulta
                        escritor.println(trends.getTrends()[i].getName());
                    }
                }
		
                //Comando que recupera as informações sobre clima e tempo do local indicado
		else if (msg.equalsIgnoreCase(Comandos.TEMPO)) {

                    URL url = new URL("http://api.openweathermap.org/data/2.5/weather?id=3450269&APPID=fd1c83353bf31f3882f27f024ac5c467&units=metric"); 
                                                                                   //Na linha acima é feita a implementação da chave da API e ID do local. 
                                                                                   //O sistema usado é definido para métrico
                    Scanner scan = new Scanner(url.openStream());                  //Estabelece a conexão e faz alguns ajustes de acordo com a API utilizada
                    String str = new String();
                    while (scan.hasNext())
                        str += scan.nextLine();
                    scan.close();
                    
                    JSONObject obj = new JSONObject(str);
                                 
                    JSONObject condicao = obj.getJSONArray("weather").getJSONObject(0);      
                    escritor.println("O tempo está: " + condicao.getString("description"));
                    JSONObject mainObj = obj.getJSONObject("main");
                    escritor.println("A temperatura neste momento é de " + mainObj.get("temp") + "º");
                } 
				
                //Comando para ver as informações do software
                else if (msg.equalsIgnoreCase(Comandos.INFO)) {  //Comando simples que apenas exibe as infos do software que estão
                    escritor.println(info);                      //armazenadas em uma constante, como pedido no enunciado do trabalho
                } 

                //Comando para ver os comandos de ajuda
                else if (msg.equalsIgnoreCase(Comandos.HELP)) {
                    escritor.println(" Use /LISTA para ver todos os usuários conectados\n Use /MSG para enviar mensagens para os demais usuários conectados "
                            + "\n Use /TWEET para ver a lista de trending topics do BRASIL\n Use /HORA para ver dia e hora atual do servidor"
                            + "\n Use /TEMPO para ver as informçaões do tempo para Santa Cruz do Sul\n Use /INFO para ver as informçaões do software\n"
                            + "\n Use /SAIR para desconectar do servidor e encerrar o cliente\n Use /HELP para ver todos os comandos possíveis ");
							
                } else {
                    escritor.println("Você disse: " + msg);       //Se nenhuma mensagem de comando for identificada apenas irá ser printado o envio na tela
                }   
            }
        } catch (IOException e) {
            System.err.println("O cliente desconectou");          //Quando o cliente desconectar será printado esse aviso no servidor
        } catch (TwitterException ex) {
            System.err.println("Twitter indisponível");
        } catch (JSONException ex) {
            Logger.getLogger(GerenciadorDeClientes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PrintWriter getEscritor() {
        return escritor;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }
}