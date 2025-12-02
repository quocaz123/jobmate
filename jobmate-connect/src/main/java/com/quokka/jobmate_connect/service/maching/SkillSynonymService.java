package com.quokka.jobmate_connect.service.maching;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import java.text.Normalizer;
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkillSynonymService {

    Map<String, List<String>> synonymMap;

    @Getter
    Map<String, List<String>> normalizedSynonyms = new HashMap<>();

    @PostConstruct
    public void loadSynonyms() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/skill_synonyms.json");

            // FIX: Null check tránh NPE
            if (is == null) {
                log.error("skill_synonyms.json NOT FOUND!");
                synonymMap = new HashMap<>();
                return;
            }

            synonymMap = mapper.readValue(is, new TypeReference<>() {});

            // FIX: Normalize đầy đủ
            synonymMap.forEach((key, value) -> {
                String k = normalize(key);
                List<String> list = new ArrayList<>();
                for (String v : value) list.add(normalize(v));
                normalizedSynonyms.put(k, list);
            });

            log.info("Loaded {} skill synonyms", normalizedSynonyms.size());

        } catch (Exception e) {
            log.error("Failed to load skill synonyms: {}", e.getMessage());
            synonymMap = new HashMap<>();
        }
    }

    public String normalize(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    // FIX: match fuzzy theo từng từ, không match cả đoạn
    public boolean fuzzyMatchInText(String text, String keyword) {
        for (String word : text.split("\\s+")) {
            if (levenshtein(word, keyword) <= 2)
                return true;
        }
        return false;
    }

    public int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;

        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(
                        1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1
                );
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    // FIX: tìm synonym theo từng từ khi key dài
    public List<String> getSynonymsSmart(String skillNorm) {
        List<String> syns = normalizedSynonyms.get(skillNorm);

        if (syns != null) return syns;

        for (String w : skillNorm.split(" ")) {
            syns = normalizedSynonyms.get(w);
            if (syns != null) return syns;
        }
        return null;
    }
}
