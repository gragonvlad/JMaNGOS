/*******************************************************************************
 * Copyright (C) 2012 JMaNGOS <http://jmangos.org/>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.jmangos.commons.network.handlers;

import org.jmangos.commons.dataholder.XmlDataLoader;
import org.jmangos.commons.network.model.NetworkChannel;
import org.jmangos.commons.network.model.ReceivablePacket;
import org.jmangos.commons.network.model.SendablePacket;
import org.jmangos.commons.network.model.State;
import org.jmangos.commons.network.netty.model.PacketData;
import org.jmangos.commons.network.netty.model.PacketList;
import org.jmangos.commons.network.netty.model.PacketTemplate;
import org.jmangos.commons.service.ServiceContent;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating AbstractPacketHandler objects.
 *
 * @author minimajack
 */
public abstract class AbstractPacketHandlerFactory extends
		XmlDataLoader implements PacketHandlerFactory {

	/** The c handler. */
	ClientPacketHandler cHandler = new ClientPacketHandler();
	
	/** The s handler. */
	ServerPacketHandler sHandler = new ServerPacketHandler();

	/**
	 * Instantiates a new abstract packet handler factory.
	 */
	public AbstractPacketHandlerFactory() {
		super();
	}

	/**
	 * Adds the list.
	 *
	 * @param pl the pl
	 */
	public void addList(PacketData pl) {
		ClassLoader cl = AbstractPacketHandlerFactory.class.getClassLoader();
		for (PacketList plist : pl.templates) {
			switch (plist.getDirection()) {
			case DOWNSTREAM:
				for (Object pt : plist.data.values()) {
					try {
						ReceivablePacket packet = (ReceivablePacket) cl
								.loadClass(
										plist.getPackageName()
												+ ((PacketTemplate) pt)
														.getName())
								.newInstance();
						packet.setOpCode(((PacketTemplate) pt).getTemplateId());
						ServiceContent.getInjector().injectMembers(packet);
						addPacket(packet, ((PacketTemplate) pt).getState());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case UPSTREAM:
				for (Object pt : plist.data.values()) {
					try {
						addPacket(((SendablePacket) (cl.loadClass((plist
								.getPackageName() + ((PacketTemplate) pt)
								.getName()))).newInstance()).getClass(),
								((PacketTemplate) pt).getTemplateId());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.wowemu.common.network.handlers.PacketHandlerFactory#addPacket(org.wowemu.common.network.model.ReceivablePacket, org.wowemu.common.network.model.State[])
	 */
	public void addPacket(ReceivablePacket packetPrototype, State... states) {
		cHandler.addPacketOpcode(packetPrototype, states);

	}

	/* (non-Javadoc)
	 * @see org.wowemu.common.network.handlers.PacketHandlerFactory#getServerPacketopCode(org.wowemu.common.network.model.SendablePacket)
	 */
	public int getServerPacketopCode(SendablePacket packetClass) {
		return sHandler.getOpCode(packetClass);
	}

	/* (non-Javadoc)
	 * @see org.wowemu.common.network.handlers.PacketHandlerFactory#handleClientPacket(int, org.wowemu.common.network.model.NetworkChannel)
	 */
	public ReceivablePacket handleClientPacket(int id, NetworkChannel ch) {
		return cHandler.getPacket(id, ch);
	}

	/* (non-Javadoc)
	 * @see org.wowemu.common.network.handlers.PacketHandlerFactory#addPacket(java.lang.Class, int)
	 */
	public void addPacket(Class<? extends SendablePacket> packetPrototype,
			int opcode) {
		sHandler.addPacketOpcode(packetPrototype, opcode);

	}

}