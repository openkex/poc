/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

import org.openkex.dto.SignedStatements;
import org.openkex.dto.StatementClaimKexId;
import org.openkex.dto.Statements;
import org.openkex.keystore.api.KeyStoreTool;
import org.openkex.serializer.SerializeService;
import org.openkex.tools.Validate;

public class StatementValidatorImpl implements StatementValidator {

    private ServerCore core;
    private SerializeService serializer;

    public StatementValidatorImpl(ServerCore core, SerializeService serializer) {
        this.core = core;
        this.serializer = serializer;
    }

    @Override
    public void validate(SignedStatements signedStatements) throws Exception {
        Validate.notNull(signedStatements);
        Statements statements = signedStatements.getStatements();

        // must iterate all statement types
        if (statements.getClaimKexId() != null) {
            validateClaimKexId(signedStatements);
        }
        else if (statements.getPublicAccountOwner() != null) {
            validatePublicAccountOwner(signedStatements);
        }
        else if (statements.getCertificateOwner() != null) {
            validateCertificateOwner(signedStatements);
        }
        else {
            throw new Exception("no valid statement found.");
        }
    }

    private void validateClaimKexId(SignedStatements signedStatements) throws Exception {
        Statements statements = signedStatements.getStatements();
        if (getStatementsCount(statements) != 1) {
            throw new StatementException("claimKexId statement is mixed with others: " + signedStatements);
        }
        StatementClaimKexId claim = statements.getClaimKexId();

        // check kexId is new
        long kexId = statements.getIssuerKexId();
        if (core.getStatementsInternal(kexId) != null) {
            throw new StatementException("kexId already exists: " + signedStatements);
        }

        // check round
        if (statements.getPreviousRound() != BlockTool.INVALID_ROUND) {
            throw new StatementException("must not have previous round: " + signedStatements);
        }

        // check hash
        if (statements.getPreviousHash() != null) {
            throw new StatementException("must not have previous hash: " + signedStatements);
        }

        // TODO: check signature date
        // statements.getSignatureDate()

        // check signature
        byte[] statementBytes = serializer.serialize(statements);
        boolean valid = KeyStoreTool.verify(claim.getAlgorithm(), claim.getKey(), statementBytes, signedStatements.getSignatureBytes());
        if (!valid) {
            throw new StatementException("signature is invalid: " + signedStatements);
        }
    }

    private int getStatementsCount(Statements statements) {
        int count = 0;
        // must iterate all statement types
        if (statements.getClaimKexId() != null) {
            count++;
        }
        if (statements.getCertificateOwner() != null) {
            count++;
        }
        if (statements.getPublicAccountOwner() != null) {
            count++;
        }
        return count;
    }

    private void validatePublicAccountOwner(SignedStatements signedStatements) throws Exception {
        throw new Exception("not implemented yet");
    }

    private void validateCertificateOwner(SignedStatements signedStatements) throws Exception {
        throw new Exception("not implemented yet");
    }
}
