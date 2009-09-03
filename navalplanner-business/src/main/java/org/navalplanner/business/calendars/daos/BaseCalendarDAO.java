package org.navalplanner.business.calendars.daos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link BaseCalendar}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class BaseCalendarDAO extends GenericDAOHibernate<BaseCalendar, Long>
        implements IBaseCalendarDAO {

    @Override
    public List<BaseCalendar> getBaseCalendars() {
        List<BaseCalendar> list = list(BaseCalendar.class);
        removeResourceCalendarInstances(list);
        return list;
    }

    private void removeResourceCalendarInstances(List<BaseCalendar> list) {
        for (Iterator<BaseCalendar> iterator = list.iterator(); iterator
                .hasNext();) {
            BaseCalendar baseCalendar = iterator.next();
            if (baseCalendar instanceof ResourceCalendar) {
                iterator.remove();
            }
        }
    }

    @Override
    public List<BaseCalendar> findByParent(BaseCalendar baseCalendar) {
        if (baseCalendar == null) {
            return new ArrayList<BaseCalendar>();
        }

        Criteria c = getSession().createCriteria(BaseCalendar.class)
                .createCriteria("calendarDataVersions", "v");
        c.add(Restrictions.eq("v.parent", baseCalendar));

        List<BaseCalendar> list = (List<BaseCalendar>) c.list();
        removeResourceCalendarInstances(list);
        return list;
    }

    @Override
    public List<BaseCalendar> findByName(BaseCalendar baseCalendar) {
        if (baseCalendar == null) {
            return new ArrayList<BaseCalendar>();
        }

        Criteria c = getSession().createCriteria(BaseCalendar.class);
        c.add(Restrictions.eq("name", baseCalendar.getName()));

        List<BaseCalendar> list = (List<BaseCalendar>) c.list();
        removeResourceCalendarInstances(list);
        return list;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @Override
    public boolean thereIsOtherWithSameName(BaseCalendar baseCalendar) {
        List<BaseCalendar> withSameName = findByName(baseCalendar);
        if (withSameName.isEmpty())
            return false;
        if (withSameName.size() > 1)
            return true;
        return areDifferentInDB(withSameName.get(0), baseCalendar);
    }

    private boolean areDifferentInDB(BaseCalendar one, BaseCalendar other) {
        if ((one.getId() == null) || (other.getId() == null)) {
            return true;
        }
        return !one.getId().equals(other.getId());
    }

}
