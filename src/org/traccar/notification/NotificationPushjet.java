/*
 * Copyright 2018 Anton Tananaev (anton@traccar.org)
 * Copyright 2018 Andrey Kunitsyn (andrey@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.notification;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.traccar.Context;
import org.traccar.helper.Log;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.model.User;

public class NotificationPushjet extends Notificator {

    private String url;

    public NotificationPushjet() {
        url = Context.getConfig().getString("notificator.pushjet.url", "http://localhost:8080/");
    }

    @Override
    public void sendSync(long userId, Event event, Position position) throws MessageException, InterruptedException {
        final User user = Context.getPermissionsManager().getUser(userId);
        if (user.getAttributes().containsKey("pushjet.service")) {
            Form form = new Form();
            form.param("secret", user.getString("pushjet.service"));
            form.param("message", NotificationFormatter.formatShortMessage(userId, event, position));
            form.param("level", String.valueOf(event.getType().equals(Event.TYPE_ALARM) ? 5 : 3));
            Context.getClient().target(url + "/message").request().async().post(Entity.form(form));
        }
    }

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        try {
            sendSync(userId, event, position);
        } catch (MessageException | InterruptedException error) {
            Log.warning(error);
        }
    }

}
