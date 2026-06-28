package memDB;

import dto.EnrichmentDTO;
import dto.Message;


import java.util.Optional;


public interface DB {
    void addSuccess(Message msg) throws InterruptedException;
    void addFailed(Message msg) throws InterruptedException;
    Optional<EnrichmentDTO> get(String msisdn);
}
