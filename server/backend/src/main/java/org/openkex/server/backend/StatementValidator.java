/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

import org.openkex.dto.SignedStatements;

public interface StatementValidator {

    /**
     * check if statements are valid
     *
     * @param statements statements to check
     * @throws StatementException if statement is invalid
     * @throws Exception in case of other problems
     */
    void validate(SignedStatements statements) throws Exception;
}
