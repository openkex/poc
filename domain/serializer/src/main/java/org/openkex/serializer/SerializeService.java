/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.serializer;

/**
 * interface for serializer.
 * <p>
 * Note: this serializer must work deterministic, it is meant for objects with small memory usage
 */
public interface SerializeService {

    /**
     * convert object to byte array
     *
     * @param obj input object
     * @return byte array output
     * @throws Exception in case of serializer problem
     */
    byte[] serialize(Object obj) throws Exception;

    /**
     * convert byte array to object of type T
     *
     * @param bytes input byte array
     * @param type object type class
     * @param <T> object type
     * @return object of type T
     * @throws Exception in case of serializer problem
     */
    <T> T deserialize(byte[] bytes, Class<T> type) throws Exception;
}
