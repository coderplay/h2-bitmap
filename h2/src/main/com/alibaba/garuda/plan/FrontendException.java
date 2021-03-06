/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.garuda.plan;

import java.io.IOException;




public class FrontendException extends IOException {

    // Change this if you modify the class.
    static final long serialVersionUID = 1L;

    /**
     * Create a new FrontendException with null as the error message.
     */
    public FrontendException() {
        super();
    }
    
    /**
     * Create a new FrontendException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     */
    public FrontendException(String message) {
        super(message);
    }
    
    /**
     * Create a new FrontendException with the specified cause.
     *
     * @param cause - The cause (which is saved for later retrieval by the <link>Throwable.getCause()</link> method) indicating the source of this exception. A null value is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public FrontendException(Throwable cause) {
        super(cause);
    }

    /**
     * Create a new FrontendException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param cause - The cause (which is saved for later retrieval by the <link>Throwable.getCause()</link> method) indicating the source of this exception. A null value is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public FrontendException(String message, Throwable cause) {
        super(message, cause);
    }

}
