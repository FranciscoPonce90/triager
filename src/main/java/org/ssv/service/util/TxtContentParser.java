package org.ssv.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssv.exception.InvalidContentException;
import org.ssv.model.Analysis;
import org.ssv.model.Refactoring;
import org.ssv.model.Smell;
import org.ssv.model.SmellStatus;
import org.ssv.service.FactoryAnalysis;
import org.ssv.service.SmellDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TxtContentParser extends ContentParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TxtContentParser.class);

    @Override
    public List<Smell> parseContent(String content, Analysis analysis) throws InvalidContentException {
        if (!Pattern.compile("^Analysis results:\\s*\n", Pattern.MULTILINE).matcher(content).find())
            throw new InvalidContentException("Invalid content");

        content = content.replaceAll("\r\n", "\n").replaceAll("\r", "\n");

        content = content.replaceAll("^Analysis results:\\s*\n", "");
        List<Smell> smells = new ArrayList<>();

        Pattern pattern = Pattern.compile("^(.*?) - detected smells \\{(.*?)\\}\\n([\\s\\S]*?)(?=\\n\\w|\\Z)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        int i = 0;
        while (matcher.find()) {
            String analysisValue = matcher.group(1).trim();
            String codes = matcher.group(2).trim();
            String description = matcher.group(3).trim();

            // Gestire i codici smell concatenati
            for (String code : codes.split(",")) {
                code = code.trim();
                SmellDetail detail = FactoryAnalysis.getInstance().findSmellDetailByCode(code);
                Refactoring refactoring = assignTemplateValues(code, description, detail.getRefactoring());

                Smell newSmell = Smell.builder()
                        .code(code)
                        .id(++i)
                        .description(description)
                        .extendedName(detail.getExtendedName())
                        .propertiesAffected(detail.getPropertiesAffected())
                        .refactoring(refactoring)
                        .status(SmellStatus.UNFIXED)
                        .analysis(analysis)
                        .analysisId(analysis.getId())
                        .outputAnalysis(analysisValue)
                        .build();
                smells.add(newSmell);
            }
        }
        return smells;
    }
}
