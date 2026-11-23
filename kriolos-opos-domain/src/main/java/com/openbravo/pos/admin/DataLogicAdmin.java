//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
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

import com.openbravo.data.loader.*;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.format.Formats;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.container.BeanFactoryDataSingle;

/**
 *
 * @author adrianromero
 */
public class DataLogicAdmin extends BeanFactoryDataSingle implements PeopleService {

    private Session s;
    private TableDefinition<PeopleInfo> m_tpeople;
    private TableDefinition<RoleInfo> m_troles;

    public DataLogicAdmin() {}

    @Override
    public void init(Session s) {
        this.s = s;

        m_tpeople = new TableDefinition<>(s,
                "people",
                 new String[]{"ID", "NAME", "APPPASSWORD", "ROLE", "VISIBLE", "CARD", "IMAGE"},
                 new String[]{"ID", AppLocal.getIntString("label.peoplename"), AppLocal.getIntString("label.Password"), AppLocal.getIntString("label.role"), AppLocal.getIntString("label.peoplevisible"), AppLocal.getIntString("label.card"), AppLocal.getIntString("label.peopleimage")},
                 new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.BOOLEAN, Datas.STRING, Datas.IMAGE},
                 new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.BOOLEAN, Formats.STRING, Formats.NULL},
                 new int[]{0},
                 PeopleInfo.getSerializerRead(),
                 PeopleInfo.getSerializerInsert(),
                 PeopleInfo.getSerializerUpdate(),
                 PeopleInfo.getSerializerDelete()
        );

        m_troles = new TableDefinition<>(s,
                "roles",
                 new String[]{"ID", "NAME", "PERMISSIONS"},
                 new String[]{"ID", AppLocal.getIntString("label.name"), "PERMISSIONS"},
                 new Datas[]{Datas.STRING, Datas.STRING, Datas.BYTES},
                 new Formats[]{Formats.STRING, Formats.STRING, Formats.NULL},
                 new int[]{0},
                 RoleInfo.getSerializerRead(),
                 RoleInfo.getSerializerInsert(),
                 RoleInfo.getSerializerUpdate(),
                 RoleInfo.getSerializerDelete()
        );
    }

    public final TableDefinition<PeopleInfo> getTablePeople() {
        return m_tpeople;
    }

    public final TableDefinition<RoleInfo> getTableRoles() {
        return m_troles;
    }

    

    @Override
    public SaveProvider<PeopleInfo> getPeopleSaveProvider(){
        return new DefaultSaveProvider<>(m_tpeople);
    }
            

    public final SentenceList<PeopleInfo> getPeopleList() {
        return new StaticSentence(s,
                 "SELECT ID, NAME FROM people ORDER BY NAME",
                 null,
                 new SerializerReadClass(PeopleInfo.class));
    }
    
    public final SentenceList<RoleInfo> getRolesList() {
        return new StaticSentence(s,
                 "SELECT ID, NAME FROM roles ORDER BY NAME",
                 null,
                 new SerializerReadClass(RoleInfo.class));
    }
    
    public final SentenceList<ResourceInfo> getResourceList() {
        return new StaticSentence(s,
                 "SELECT ID, NAME, RESTYPE, CONTENT FROM resources ORDER BY NAME",
                 null,
                 new SerializerReadClass(ResourceInfo.class));
    }
}
