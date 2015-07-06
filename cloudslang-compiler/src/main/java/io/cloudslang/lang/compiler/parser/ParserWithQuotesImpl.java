/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser;

import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.parser.Parser;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;

public class ParserWithQuotesImpl implements Parser {

    public static final char DOUBLE_QUOTES = '"';
    public static final char SINGLE_QUOTE = '\'';
    private Parser parser;

    public ParserWithQuotesImpl(StreamReader reader) {
        this.parser =  new ParserImpl(reader);

    }

    @Override
    public boolean checkEvent(Event.ID choice) {
        return parser.checkEvent(choice);
    }

    @Override
    public Event peekEvent() {
        Event event = parser.peekEvent();
        event = addDoubleQuotes(event);
        return event;
    }

    @Override
    public Event getEvent() {
        Event event = parser.getEvent();
        event = addDoubleQuotes(event);
        return event;
    }

    private Event addDoubleQuotes(Event event) {
        if (event instanceof ScalarEvent) {
            ScalarEvent scalarEvent = (ScalarEvent) event;
            switch (scalarEvent.getStyle()) {
                case DOUBLE_QUOTES:
                    event = wrapEventValue(scalarEvent, DOUBLE_QUOTES);
                    break;
                case SINGLE_QUOTE:
                    event = wrapEventValue(scalarEvent, SINGLE_QUOTE);
                    break;
                default:
                    break;
            }
        }
        return event;
    }

    private ScalarEvent wrapEventValue(ScalarEvent event, char wrapChar) {
        return new ScalarEvent(
                event.getAnchor(),
                event.getTag(),
                event.getImplicit(),
                wrapChar + event.getValue() + wrapChar,
                event.getStartMark(),
                event.getEndMark(),
                event.getStyle());
    }
}
