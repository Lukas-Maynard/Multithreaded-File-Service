import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadIssues {
    public static int x = 0;
    public static LinkedList<Integer> list = new LinkedList<>();

    static class Increment implements Runnable{
        public void run(){
            x++;
        }
    }

    static class AddToFront implements Runnable{
        public void run() {
            list.add(0,1000);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(4);

        for (int i=0; i<1000; i++){
//            Increment task = new Increment();
//            es.submit(task);

            AddToFront task2 = new AddToFront();
            es.submit(task2);
        }
        es.shutdown();
        boolean success = es.awaitTermination(2, TimeUnit.SECONDS);

        if (success){
            System.out.println("x= "+ list.size());
        } else {
            System.out.println("Some tasks are still running.");
        }
    }
}