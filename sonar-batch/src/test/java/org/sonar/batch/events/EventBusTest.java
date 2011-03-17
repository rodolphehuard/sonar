/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.batch.events;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class EventBusTest {

  @Test
  public void shouldNotifyAboutEvent() {
    FirstHandler firstHandler = mock(FirstHandler.class);
    SecondHandler secondHandler = mock(SecondHandler.class);
    EventBus eventBus = new EventBus(new EventHandler[] { firstHandler, secondHandler });

    FirstEvent firstEvent = new FirstEvent();
    eventBus.fireEvent(firstEvent);
    SecondEvent secondEvent = new SecondEvent();
    eventBus.fireEvent(secondEvent);

    verify(firstHandler).onEvent(firstEvent);
    verify(secondHandler).onEvent(secondEvent);
  }

  interface FirstHandler extends EventHandler {
    void onEvent(FirstEvent event);
  }

  static class FirstEvent extends SonarEvent<FirstHandler> {
    @Override
    protected void dispatch(FirstHandler handler) {
      handler.onEvent(this);
    }

    @Override
    public Class getType() {
      return FirstHandler.class;
    }
  }

  interface SecondHandler extends EventHandler {
    void onEvent(SecondEvent event);
  }

  static class SecondEvent extends SonarEvent<SecondHandler> {
    @Override
    protected void dispatch(SecondHandler handler) {
      handler.onEvent(this);
    }

    @Override
    public Class getType() {
      return SecondHandler.class;
    }
  }

}