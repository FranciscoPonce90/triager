package org.ssv.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.ssv.model.*;

@Data
@SuperBuilder
@NoArgsConstructor
public class TriageService {

    private static final UrgencyCode[][] URGENCY_MATRIX = {
            {UrgencyCode.Ø, UrgencyCode.l, UrgencyCode.L, UrgencyCode.m},
            {UrgencyCode.l, UrgencyCode.L, UrgencyCode.m, UrgencyCode.M},
            {UrgencyCode.L, UrgencyCode.m, UrgencyCode.M, UrgencyCode.h},
            {UrgencyCode.m, UrgencyCode.M, UrgencyCode.h, UrgencyCode.H}
    };
    public UrgencyCode urgencyCodeCalculator(Microservice microservice, Smell smell){
        Relevance highestRelevance = Relevance.NONE;
        for (QualityAttribute smellAffectedAttribute : smell.getPropertiesAffected()) {
            for (QualityAttribute microserviceAttribute : microservice.getQualityAttributes()) {
                if (microserviceAttribute instanceof QualityAttributeMS) {
                    QualityAttributeMS castedAttribute = (QualityAttributeMS) microserviceAttribute;
                    if (smellAffectedAttribute.getName().equalsIgnoreCase(castedAttribute.getName()) &&
                            castedAttribute.getRelevance().ordinal() > highestRelevance.ordinal()) {
                        highestRelevance = castedAttribute.getRelevance();
                    }
                }
            }
        }
        return URGENCY_MATRIX[highestRelevance.ordinal()][microservice.getRelevance().ordinal()];
    }

}
