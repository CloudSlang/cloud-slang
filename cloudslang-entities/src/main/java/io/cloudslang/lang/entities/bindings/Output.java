/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.cloudslang.lang.entities.bindings.values.Value;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;

/**
 * @author Bonczidai Levente
 * @version $Id$
 * @since 11/7/2014
 */
public class Output extends InOutParam {

    private static final long serialVersionUID = -5390581034091916685L;

    private Boolean robot;

    public Output(String name, Value value) {
        super(name, value);
    }

    public Output(
            String name,
            Value value,
            Set<ScriptFunction> scriptFunctions,
            Set<String> systemPropertyDependencies) {
        super(name, value, scriptFunctions, systemPropertyDependencies);
    }

    public boolean hasRobotProperty() {
        return robot != null;
    }

    @JsonIgnore
    public boolean isRobot() {
        return robot != null && robot;
    }

    public void setRobot(Boolean robot) {
        this.robot = robot;
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private Output() {
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("robot", robot)
                .toString();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Output that = (Output) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(robot, that.robot)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 47)
                .appendSuper(super.hashCode())
                .append(robot)
                .toHashCode();
    }
}
