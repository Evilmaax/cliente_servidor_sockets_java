package ClienteServidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//@author Maximiliano Meyer
public class ServidorSocket {                                          //classe que starta servidor e aceita uma nova conexão

    public static void main(String[] args) {

        ServerSocket servidor = null;

        try {
            System.out.println("Inicializando o servidor\n.\n.\n.");   //Informa a tentativa de início do servidor
            servidor = new ServerSocket(48);                           //Inicía o servidor na porta indicada
            System.out.println("Servidor inicializado com sucesso");   //Confirma o sucesso na operação

            while (true) {
                Socket cliente = servidor.accept();                    //Aceita a conexão dos clientes para que eles possam trabalhar com o servidor
                new GerenciadorDeClientes(cliente);  		       //Cria novas conexões sempre que solicitado
            }

        } catch (IOException e) {            			       //Exceção para caso não seja possível iniciar o servidor

            try {
                if (servidor != null)    //Garante que mesmo que um erro ocorra o servidor será fechado e                
                { 		         //economizará recursos e evitará vulnerabilidades de segurança
                    servidor.close();
                }
            } catch (IOException e2) {
            }

            System.err.println("Porta ocupada ou servidor fechado");   //Mensagem exibida caso a conexão não seja efetuada
        }

    }
}
