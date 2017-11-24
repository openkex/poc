/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.api;

/**
 * enum that describes how keystore is locked
 */
public enum LockType {

    /** key store is not locked */
    NOT_LOCKED,
    /** key store need to be unlocked only once */
    UNLOCK_ONCE,
    /** key store need to be unlocked for each critical operation */
    UNLOCK_EACH,
    /** unlock happens on external device, e.g. external keypad or fingerprint sensor */
    UNLOCK_EXTERNAL
}
