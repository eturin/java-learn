package memDB;

import dto.EnrichmentDTO;
import dto.Message;
import dto.ResultDTO;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Log4j2
public class DBImpl implements DB {
    private ConcurrentHashMap<String,EnrichmentDTO> mpEnrichment;
    private final BlockingQueue<ResultDTO> queue;
    private final AtomicBoolean isRepeat;
    private final ConcurrentLinkedDeque<ResultDTO> results;


    public DBImpl(int bufferSize) {
        queue = new ArrayBlockingQueue<>(bufferSize);
        results = new ConcurrentLinkedDeque<>();
        mpEnrichment = new ConcurrentHashMap<>();
        isRepeat = new AtomicBoolean(true);

        new Thread(() -> {
            while (isRepeat.get()) {
                try {
                    var msg = queue.poll(3, TimeUnit.SECONDS);
                    if(msg != null) {
                        results.push(msg);
                        log.debug(String.format("Сообщение: %s", msg.toString()));
                    }
                } catch (Exception e) {
                    log.error(e.toString());
                }
            }
        }).start();

        log.error("База инициализирована");
    }

    @Override
    public void addSuccess(Message msg) throws InterruptedException {
        queue.put(new ResultDTO(msg, true));
    }

    @Override
    public void addFailed(Message msg) throws InterruptedException {
        queue.put(new ResultDTO(msg, false));
    }

    @Override
    public Optional<EnrichmentDTO> get(String msisdn) {
        var enrichment = mpEnrichment.get(msisdn);
        return enrichment != null ? Optional.of(enrichment): Optional.empty();
    }
}
