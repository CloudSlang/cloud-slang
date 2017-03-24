/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.MetadataModeller;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.modeller.result.MetadataModellingResult;
import io.cloudslang.lang.compiler.parser.MetadataParser;
import io.cloudslang.lang.compiler.parser.model.ParsedDescriptionData;
import io.cloudslang.lang.compiler.parser.utils.MetadataValidator;
import java.util.List;
import org.apache.commons.lang.Validate;

public class MetadataExtractorImpl implements MetadataExtractor {

    private MetadataModeller metadataModeller;
    private MetadataParser metadataParser;
    private MetadataValidator metadataValidator;

    @Override
    public Metadata extractMetadata(SlangSource source) {
        validateSlangSource(source);
        return getExecutableMetadata(source);
    }

    @Deprecated
    @Override
    public Metadata extractMetadata(SlangSource source, boolean shouldValidateDescription) {
        return getMetadata(source, shouldValidateDescription);
    }

    @Override
    public MetadataModellingResult extractMetadataModellingResult(SlangSource source) {
        validateSlangSource(source);
        ParsedDescriptionData parsedDescriptionData = metadataParser.parse(source);
        return metadataModeller.createModel(parsedDescriptionData);
    }

    @Deprecated
    @Override
    public MetadataModellingResult extractMetadataModellingResult(
            SlangSource source,
            boolean shouldValidateCheckstyle) {
        MetadataModellingResult metadataModellingResult = extractMetadataModellingResult(source);
        if (shouldValidateCheckstyle) {
            metadataModellingResult.getErrors().addAll(validateCheckstyle(source));
        }
        return metadataModellingResult;
    }

    @Override
    public List<RuntimeException> validateCheckstyle(SlangSource source) {
        validateSlangSource(source);
        return metadataValidator.validateCheckstyle(source);
    }

    private Metadata getMetadata(SlangSource source, boolean shouldValidateDescription) {
        MetadataModellingResult result = extractMetadataModellingResult(source);
        if (result.getErrors().size() > 0) {
            throw result.getErrors().get(0);
        }
        if (shouldValidateDescription) {
            List<RuntimeException> checkstyleErrors = validateCheckstyle(source);
            if (checkstyleErrors.size() > 0) {
                throw checkstyleErrors.get(0);
            }
        }
        return result.getMetadata();
    }

    private void validateSlangSource(SlangSource source) {
        Validate.notNull(source, "You must supply a source to extract the metadata from");
    }

    private Metadata getExecutableMetadata(SlangSource source) {
        MetadataModellingResult result = getMetadataModellingResultThrowFirstError(source);
        return result.getMetadata();
    }

    private MetadataModellingResult getMetadataModellingResultThrowFirstError(SlangSource source) {
        MetadataModellingResult result = extractMetadataModellingResult(source);
        if (result.getErrors().size() > 0) {
            throw result.getErrors().get(0);
        }
        return result;
    }

    public void setMetadataModeller(MetadataModeller metadataModeller) {
        this.metadataModeller = metadataModeller;
    }

    public void setMetadataParser(MetadataParser metadataParser) {
        this.metadataParser = metadataParser;
    }

    public void setMetadataValidator(MetadataValidator metadataValidator) {
        this.metadataValidator = metadataValidator;
    }

}
