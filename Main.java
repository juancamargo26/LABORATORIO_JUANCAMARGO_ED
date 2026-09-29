import java.time.Instant;
import java.time.Duration;

public class Main {

    public static void exec(int size, String method, Operation operation) {
        Instant start = Instant.now();        
        
        for (int i =0; i<size; i++)
            operation.apply(i);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.printf("Se ejecutó %s de %d elementos en: %d milisegundos\n", method, size, timeElapsed);
    }

    public static void main(String[] args) {
        final int start = 100;
        final int end = 10000;
        
        for (int size = start; size <= end; size *= 10)
            for (int i =0; i<5; i++){
                ListaArrEsta lista = new ListaArrEsta(size);
                exec(size, "addFirst", lista::addFirst);
            }
    
    }
}