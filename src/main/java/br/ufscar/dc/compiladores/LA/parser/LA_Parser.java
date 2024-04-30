package br.ufscar.dc.compiladores.LA.parser;

// Importações
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.antlr.v4.runtime.*;

public class LA_Parser {

    public static void main(String[] args) throws IOException {
        // Recebe o arquivo de entrada e saída obrigatoriamente
        if (args.length != 2) {
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        // Configura o lexer e o parser com base no arquivo de entrada
        CharStream cs = CharStreams.fromFileName(inputFile);
        LALexer lexer = new LALexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LAParser parser = new LAParser(tokens);

        // Escreve os resultados no arquivo de saída
        PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(outputFile)));
        try {
            // Percorre os tokens do arquivo para a análise léxica, adicionando o problema e encerrando o programa
            // se for necessário
            Token t = lexer.nextToken();
            while (t.getType() != Token.EOF) {
                switch (t.getType()) {
                    case LALexer.ERRO_SIMBOLO:
                        writer.printf("Linha %d: %s - simbolo nao identificado%n", t.getLine(), t.getText());
                        writer.println("Fim da compilacao");
                        writer.flush();
                        System.exit(1);
                        break;
                    case LALexer.COMENTARIO_NAO_FECHADO:
                        writer.printf("Linha %d: comentario nao fechado%n", t.getLine());
                        writer.println("Fim da compilacao");
                        writer.flush();
                        System.exit(1);
                        break;
                    case LALexer.CADEIA_NAO_FECHADA:
                        writer.printf("Linha %d: cadeia literal nao fechada%n", t.getLine());
                        writer.println("Fim da compilacao");
                        writer.flush();
                        System.exit(1);
                        break;
                }
                t = lexer.nextToken();
            }

            // Reinicia o fluxo de entrada para que o parser funcione corretamente
            lexer.setInputStream(CharStreams.fromFileName(inputFile));

            // Configuração do tratamento de erros sintáticos
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                    String tokenText = ((Token) offendingSymbol).getText();
                    // Sobrescreve o token EOF para o padrão especificado nos casos de teste
                    if (tokenText.equals("<EOF>")) {
                        tokenText = "EOF";
                    }
                    // Escreve a mensagem de erro sintático antes de finalizar o programa
                    writer.printf("Linha %d: erro sintatico proximo a %s%n", line, tokenText);
                    writer.println("Fim da compilacao");
                    writer.flush();
                    System.exit(1);
                }
            });

            // Chamada da regra inicial do parser
            parser.programa();

            // Finalização da manipulação do arquivo
        } finally {
            writer.close();
        }
    }
}


