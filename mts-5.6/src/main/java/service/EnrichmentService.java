package service;


import dto.Message;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.MessageDTO;
import util.FabricDB;

public class EnrichmentService {
    // возвращается обогащенный (или необогащенный content сообщения)
    public String enrich(Message message) {
        var db = FabricDB.getDB();
        var json = message.getContent();

        try {
            var mapper = new ObjectMapper();
            var msg = mapper.readValue(json, MessageDTO.class);

            var enrichment = db.get(msg.getMsisdn());
            if(enrichment.isEmpty()) {
                db.addFailed(message);
            } else {
                msg.setEnrichment(enrichment.get());
                json = mapper.writeValueAsString(msg);
                db.addSuccess(message);
            }
        } catch (Exception e) {
            db.addFailed(message);
        }

        return json;
    }
}
