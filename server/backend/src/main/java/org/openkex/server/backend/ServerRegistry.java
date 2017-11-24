/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

import org.openkex.server.api.ServerApi;

import java.util.List;

/**
 * trusted source of server information.
 * <p>
 * assume foundation signatures were validated etc.
 */
public interface ServerRegistry {

    List<ServerData> getAllServers();

    ServerData getById(String serverId);

    ServerApi getProxy(String serverId);
}
