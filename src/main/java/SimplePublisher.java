import java.util.Iterator;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class SimplePublisher implements Flow.Publisher<Integer> {
    private final Iterator<Integer> iterator;

    SimplePublisher(int count) {
        this.iterator = IntStream.rangeClosed(1, count).iterator();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
//        iterator.forEachRemaining(subscriber::onNext);
//        subscriber.onComplete();
        subscriber.onSubscribe(new SimpleSubscription(subscriber));
    }

    public static void main(String[] args) {
        new SimplePublisher(10).subscribe(new Flow.Subscriber<Integer>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {

            }

            @Override
            public void onNext(Integer item) {
                System.out.println("item = [" + item + "]");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("throwable = [" + throwable + "]");
            }

            @Override
            public void onComplete() {
                System.out.println("complete");

            }
        });
    }

    private class SimpleSubscription implements Flow.Subscription {
        private final Flow.Subscriber<? super Integer> subscriber;
        private AtomicBoolean terminated = new AtomicBoolean(false); //флаг прекращения передачи

        public SimpleSubscription(final Flow.Subscriber<? super Integer> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void request(final long n) {
            if(n <= 0) {
                subscriber.onError(new IllegalArgumentException()); //если элемент некорректный, то отправляем сообщение об ошибке
            }

            for(long demand = n; demand >0 && iterator.hasNext() && !terminated.get(); demand--) {
                subscriber.onNext(iterator.next());  // запрашиваем следующий элемент, который берем из итератора
            }
            if(!iterator.hasNext() && !terminated.getAndSet(true)) {
                subscriber.onComplete(); // если следующего элемента у итератора нет, то отсанавливаем
            }

        }

        @Override
        public void cancel() {
            terminated.set(true);
        }
    }
}

