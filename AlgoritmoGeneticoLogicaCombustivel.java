import java.util.Random;

public class Main {
    
    static final int TAMANHO_POPULACAO = 50;
    static final int GERACOES = 100;
    static final double TAXA_MUTACAO = 0.05;
    static final int AUTONOMIA_MAXIMA = 350; 
    static final double KM_POR_LITRO = 10.0;

    // Cenário: Morrinhos (0) até Santos (9)
    static final String[] nomes_cidades = {"Morrinhos", "Itumbiara", "Uberlândia", "Uberaba", "Ribeirão Preto", "Campinas", "Jundiaí", "São Paulo", "Cubatão", "Santos"};
    static final int[] distancias_ate_aqui = {0, 100, 80, 120, 90, 110, 80, 150, 70, 100}; 
    static final double[] precos_combustivel = {5.70, 5.95, 5.50, 6.10, 5.40, 5.80, 5.30, 6.00, 5.60, 5.55}; 
    static final int NUM_POSTOS = distancias_ate_aqui.length;

    static Random random = new Random();

    public static void main(String[] args) {
        int[][] populacao = new int[TAMANHO_POPULACAO][NUM_POSTOS];
        
        // Variáveis para guardar o "campeão" de todas as gerações
        double melhorCustoGlobal = 999999;
        int[] melhorRotaGlobal = new int[NUM_POSTOS];

        // 1. Inicializa População
        for (int i = 0; i < TAMANHO_POPULACAO; i++) {
            for (int j = 0; j < NUM_POSTOS; j++) {
                populacao[i][j] = random.nextInt(2);
            }
            populacao[i][0] = 1; // Sempre sai de Morrinhos abastecido
            populacao[i][NUM_POSTOS - 1] = 1; // Sempre chega no destino
        }

        System.out.println("Iniciando Algoritmo Genético (Morrinhos -> Santos)...\n");

        // Loop das Gerações
        for (int gen = 0; gen < GERACOES; gen++) {
            double[] fitness = new double[TAMANHO_POPULACAO];
            double somaInversoFitness = 0;

            // 2. Avaliação
            for (int i = 0; i < TAMANHO_POPULACAO; i++) {
                fitness[i] = calcularFitness(populacao[i]);
                
                // Salva o melhor global encontrado até agora
                if (fitness[i] < melhorCustoGlobal) {
                    melhorCustoGlobal = fitness[i];
                    melhorRotaGlobal = populacao[i].clone(); 
                }

                if (fitness[i] < 999999) {
                    somaInversoFitness += (1.0 / fitness[i]);
                }
            }

            if (gen % 20 == 0 || gen == GERACOES - 1) {
                System.out.printf("Geração %03d | Melhor Custo da Geração: R$ %.2f\n", gen, melhorCustoGlobal);
            }

            // 3. Seleção e Reprodução (Mantida a mesma lógica)
            int[][] novaPopulacao = new int[TAMANHO_POPULACAO][NUM_POSTOS];
            for (int i = 0; i < TAMANHO_POPULACAO; i += 2) {
                int[] pai1 = selecionarRoleta(populacao, fitness, somaInversoFitness);
                int[] pai2 = selecionarRoleta(populacao, fitness, somaInversoFitness);

                int pontoCorte = NUM_POSTOS / 2;
                int[] filho1 = new int[NUM_POSTOS];
                int[] filho2 = new int[NUM_POSTOS];

                for (int j = 0; j < NUM_POSTOS; j++) {
                    if (j < pontoCorte) {
                        filho1[j] = pai1[j];  filho2[j] = pai2[j];
                    } else {
                        filho1[j] = pai2[j];  filho2[j] = pai1[j];
                    }
                }

                mutacao(filho1); mutacao(filho2);

                novaPopulacao[i] = filho1;
                if (i + 1 < TAMANHO_POPULACAO) novaPopulacao[i + 1] = filho2;
            }
            populacao = novaPopulacao;
        }

        // --- RELATÓRIO FINAL PARA A APRESENTAÇÃO ---
        System.out.println("\n OTIMIZAÇÃO CONCLUÍDA!");
        System.out.println("==================================================");
        System.out.printf("Custo Total Otimizado: R$ %.2f\n", melhorCustoGlobal);
        System.out.println("🛣️ Estratégia de Paradas (O Indivíduo Vencedor):");
        
        System.out.print("[ ");
        for(int bit : melhorRotaGlobal) System.out.print(bit + " ");
        System.out.println("]\n");

        int distPercorrida = 0;
        for (int i = 0; i < NUM_POSTOS; i++) {
            distPercorrida += distancias_ate_aqui[i];
            if (melhorRotaGlobal[i] == 1) {
                if(i == 0) {
                    System.out.println("" + nomes_cidades[i] + " -> PONTO DE PARTIDA (Tanque Cheio)");
                } else if (i == NUM_POSTOS - 1) {
                    System.out.println("🏁 " + nomes_cidades[i] + " -> DESTINO ALCANÇADO!");
                } else {
                    System.out.printf("Parada em %s (Andou %d km) | Preço: R$ %.2f/L\n", 
                                      nomes_cidades[i], distPercorrida, precos_combustivel[i]);
                }
                distPercorrida = 0; // Zera o hodômetro após abastecer
            } else {
                System.out.println("   Passou direto por " + nomes_cidades[i] + " (Preço: R$ " + precos_combustivel[i] + ")");
            }
        }
        System.out.println("==================================================");
    }

    // Função de Fitness com penalidade
    static double calcularFitness(int[] individuo) {
        double custoTotal = 0;
        int distanciaAcumulada = 0;
        
        // Força a origem e destino a serem 1 na avaliação
        individuo[0] = 1; 
        individuo[NUM_POSTOS - 1] = 1; 

        for (int i = 1; i < NUM_POSTOS; i++) {
            distanciaAcumulada += distancias_ate_aqui[i];

            if (individuo[i] == 1) { 
                if (distanciaAcumulada > AUTONOMIA_MAXIMA) {
                    return 999999; // Pane seca!
                }
                double litrosGastos = distanciaAcumulada / KM_POR_LITRO;
                custoTotal += litrosGastos * precos_combustivel[i];
                distanciaAcumulada = 0; 
            }
        }
        return custoTotal;
    }

    // Seleção Roleta
    static int[] selecionarRoleta(int[][] populacao, double[] fitness, double totalInverso) {
        if (totalInverso <= 0) return populacao[random.nextInt(TAMANHO_POPULACAO)]; // Prevenção de erro
        
        double sorteio = random.nextDouble() * totalInverso;
        double soma = 0;
        
        for (int i = 0; i < TAMANHO_POPULACAO; i++) {
            if (fitness[i] < 999999) { 
                soma += (1.0 / fitness[i]);
                if (soma >= sorteio) {
                    return populacao[i];
                }
            }
        }
        return populacao[random.nextInt(TAMANHO_POPULACAO)]; 
    }

    // Mutação
    static void mutacao(int[] individuo) {
        for (int i = 1; i < NUM_POSTOS - 1; i++) { 
            if (random.nextDouble() < TAXA_MUTACAO) {
                individuo[i] = (individuo[i] == 1) ? 0 : 1; 
            }
        }
    }
}
