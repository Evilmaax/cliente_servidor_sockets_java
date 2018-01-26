package ClienteServidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

//@author Maximiliano Meyer
public class ClienteSocket {

    public static void main(String[] args) {
        try {

            final Socket cliente = new Socket("127.0.0.1", 48);      // Inicia uma nova conexão na porta indicada. O IP utilizado é o endereço local da máquina

            //Lendo do servidor
            new Thread() {					     //Implementação do uso das threads para rodar quantos clientes forem solicitados
                @Override
                public void run() {
                    try {
                        BufferedReader leitor = new BufferedReader(new InputStreamReader(cliente.getInputStream())); //Variável que ficará escutando o que vem do cliente

                        //o cliente está mandando
                        while (true) {
                            String mensagem = leitor.readLine();			     //Loop que fica escutando e recebe tudo que o cliente envia
                            System.out.println("O servidor retornou: " + mensagem);	     //Retorno do servidor para a mensagem enviada pelo cliente 
                        }
                    } catch (IOException e) {
                        System.out.println("Impossível ler a mensagem do servidor");         //Exceção para caso não seja possível receber a resposta do servidor
                    }
                }
            }.start();    	//Comando que inicia a thread

            //Escrevendo para o servidor
            PrintWriter escritor = new PrintWriter(cliente.getOutputStream(), true);                 //Variável que ficará escutando o que vem do cliente. Com o autoflush setado em 
            BufferedReader leitorTerminal = new BufferedReader(new InputStreamReader(System.in));    //true ele vai printar sempre que o usuário der um enter
            String mensagemTerminal = null;

            while (true) {                                                             //Loop que executa o print das mensagens do cliente
                mensagemTerminal = leitorTerminal.readLine();
                if (mensagemTerminal == null) {
                    continue;
                }
                escritor.println(mensagemTerminal);
                if (mensagemTerminal.equalsIgnoreCase("/SAIR")) {		      //O processo de printar o que for recebido repete até que venha o comando "/sair"	
                    System.exit(0);
                }
            }

        } catch (UnknownHostException e) {
            System.out.println("Endereço inválido");                                   //Exceção para problemas de conexão com endereço do servidor
        } catch (IOException e) {
            System.out.println("Não foi possível conectar ao servidor");               //Exceção para problemas de conexão com servidor
        }

    }
}
