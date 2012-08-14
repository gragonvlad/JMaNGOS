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
package org.jmangos.auth.service;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.jmangos.auth.config.Config;
import org.jmangos.auth.dao.BanIpDAO;
import org.jmangos.commons.model.BanIp;
import org.jmangos.commons.config.Compatiple;
import org.jmangos.commons.service.Service;

/**
 * The Class BanIpService.
 * 
 * @author MinimaJack
 */
public class BanIpService implements Service {
	/**
	 * Logger for this class.
	 */
	private static final Logger log = Logger.getLogger(BanIpService.class);

	@Inject
	private Config config;

	/** The ban ip dao. */
	@Inject
	private BanIpDAO banIpDAO;

	/** List of banned ip addresses. */
	private List<BanIp> banList;

	/**
	 * 
	 * @see org.jmangos.commons.service.Service#start()
	 */
	@Override
	public void start() {
		update();
		log.debug(String.format("BannedIpService loaded %d IP bans.",
				banList.size()));
	}

	/**
	 * Update.
	 */
	private void update() {
		banList = banIpDAO.getAllBans();
	}

	/**
	 * Checks if is banned.
	 * 
	 * @param ip
	 *            the ip
	 * @return true, if is banned
	 */
	public boolean isBanned(String ip) {
		// if auth server run in compatible mode...all ban info need get from
		// DB.
		if (config.COMPATIBLE == Compatiple.MANGOS) {
			BanIp result = banIpDAO.getBan(ip);
			if (result != null) {
				return true;
			}
		} else {
			for (BanIp ipBan : banList) {
				if (ipBan.isActive() && ip.equals(ipBan.getIp()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Ban ip.
	 * 
	 * @param ip
	 *            the ip
	 * @return true, if successful
	 */
	public boolean banIp(String ip) {
		return banIp(ip, null);
	}

	/**
	 * Bans ip.
	 * 
	 * @param ip
	 *            ip to ban
	 * @param expireTime
	 *            ban expiration time
	 * @return was ip banned or not
	 */
	public boolean banIp(String ip, Timestamp expireTime) {
		BanIp ipBan = new BanIp();
		ipBan.setIp(ip);
		ipBan.setTimeEnd(expireTime);
		if (banIpDAO.insert(ipBan) != null) {
			banList.add(ipBan);
			return true;
		}
		return false;
	}

	/**
	 * Adds ip ban.
	 * 
	 * @param ipBan
	 *            banned ip to add
	 * @return was it updated or not
	 */
	public boolean addBan(BanIp ipBan) {
		if (banIpDAO.insert(ipBan) != null) {
			banList.add(ipBan);
			return true;
		} else
			return false;
	}

	/**
	 * Removes ip ban.
	 * 
	 * @param ip
	 *            ip to unban
	 * @return returns true if ip was successfully unbanned
	 */
	public boolean unbanIp(String ip) {
		Iterator<BanIp> it = banList.iterator();
		while (it.hasNext()) {
			BanIp ipBan = it.next();
			if (ipBan.getIp().equals(ip)) {
				if (banIpDAO.remove(ipBan)) {
					it.remove();
					return true;
				} else
					break;
			}
		}
		return false;
	}

	/**
	 * 
	 * @see org.jmangos.commons.service.Service#stop()
	 */
	@Override
	public void stop() {
		banList.clear();
	}
}
