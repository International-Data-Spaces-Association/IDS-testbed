/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.service.message.handler.exception;

/**
 * Thrown to indicate that the affected resource of a ResourceUpdateMessage does not match the
 * resource in the payload.
 */
public class InvalidAffectedResourceException extends RuntimeException {

    /**
     * Default serial version uid.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an InvalidAffectedResourceException with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidAffectedResourceException(final String msg) {
        super(msg);
    }

}