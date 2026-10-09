//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.admin;

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.forms.AppLocal;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link JPeopleFinderPanel}.
 *
 * @deprecated Use {@link JPeopleFinderPanel#show(Component, DataLogicAdmin)} instead.
 */
@Deprecated
public class JPeopleFinder {

    private final Component parent;
    private final JPeopleFinderPanel panel;

    private JPeopleFinder(Component parent, PeopleService dlPeople) {
        this.parent = parent;
        this.panel = new JPeopleFinderPanel(dlPeople);
    }

    public static JPeopleFinder getPeopleFinder(Component parent, PeopleService dlPeople) {
        return new JPeopleFinder(parent, dlPeople);
    }

    @Deprecated
    public static JPeopleFinder getPeopleFinder(Component parent, DataLogicAdmin dlPeople) {
        return new JPeopleFinder(parent, (PeopleService) dlPeople);
    }

    public static PeopleInfo show(Component parent, PeopleService dlPeople) {
        return JPeopleFinderPanel.show(parent, dlPeople);
    }

    public static PeopleInfo show(Component parent, PeopleService dlPeople, PeopleInfo people) {
        return JPeopleFinderPanel.show(parent, dlPeople, people);
    }

    @Deprecated
    public static PeopleInfo show(Component parent, DataLogicAdmin dlPeople) {
        return JPeopleFinderPanel.show(parent, (PeopleService) dlPeople);
    }

    @Deprecated
    public static PeopleInfo show(Component parent, DataLogicAdmin dlPeople, PeopleInfo people) {
        return JPeopleFinderPanel.show(parent, (PeopleService) dlPeople, people);
    }

    public void search(PeopleInfo people) {
        panel.search(people);
    }

    public void executeSearch() {
        panel.executeSearch();
    }

    public void setVisible(boolean b) {
        if (b) {
            PosUIModal modal = PosUIModal.create(parent, panel)
                    .setTitle(AppLocal.getIntString("form.usertitle"))
                    .setModal(true)
                    .setResizable(true);
            panel.setModalContext(modal);
            modal.show();
        }
    }

    public PeopleInfo getSelectedPeople() {
        return panel.getSelectedPeople();
    }
}
