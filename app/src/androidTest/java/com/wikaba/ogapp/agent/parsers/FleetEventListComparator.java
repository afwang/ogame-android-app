/*
	Copyright 2014 Alexander Wang

	This file is part of Ogame on Android.

	Ogame on Android is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Ogame on Android is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.models.FleetEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FleetEventListComparator implements Comparator<List<FleetEvent>> {
    private static final Logger logger = LoggerFactory.getLogger(FleetEventListComparator.class);

    @Override
    public int compare(List<FleetEvent> l1, List<FleetEvent> l2) {
        if (l1 == null || l2 == null) {
            if (l1 != l2) {
                //One, and only one, of the two list references are not null
                //Treat null as the "lesser" value
                return l1 == null ? -1 : 1;
            } else {
                return 0;
            }
        }

        int size1 = l1.size();
        int size2 = l2.size();
        if (size1 != size2) {
            int diff = size2 - size1;
            logger.info("FleetEvent lists are not equal. Second list is longer by {}", diff);
            return size2 - size1;
        }

        long difference = 0;
        Iterator<FleetEvent> iter1 = l1.iterator();
        Iterator<FleetEvent> iter2 = l2.iterator();
        while (iter1.hasNext()) {
            FleetEvent f1 = iter1.next();
            FleetEvent f2 = iter2.next();

            difference = f1.compareTo(f2);
            if(difference != 0) {
                logger.info("Element f1 is not equal to element f2:");
                logger.info("f1: {}", f1);
                logger.info("f2: {}", f2);
                break;
            }
        }

        return (int) difference;
    }

    private int compareFleetResources(Map<String, Long> m1, Map<String, Long> m2) {
        if (m1 == null || m2 == null) {
            if (m1 != m2) {
                return m1 == null ? -1 : 1;
            } else {
                return 0;
            }
        }

        int size1 = m1.size();
        int size2 = m2.size();
        if (size1 != size2) {
            return size2 - size1;
        }

        long difference = 0;
        Iterator<String> keyIter = m1.keySet().iterator();
        while (keyIter.hasNext()) {
            String key1 = keyIter.next();
            long v1 = m1.get(key1);
            Long val2 = m2.get(key1);
            if (val2 == null) {
                return 1;
            }
            long v2 = val2;
            if (v1 != v2) {
                difference = v2 - v1;
                break;
            }
            m2.remove(key1);
        }
        return (int) difference;
    }
}
