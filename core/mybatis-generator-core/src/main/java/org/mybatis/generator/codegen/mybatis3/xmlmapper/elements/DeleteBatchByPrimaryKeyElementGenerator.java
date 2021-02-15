/*
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 * 根据主键批量删除
 */
public class DeleteBatchByPrimaryKeyElementGenerator extends
        AbstractXmlElementGenerator {

    private final boolean isSimple;

    public DeleteBatchByPrimaryKeyElementGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("delete"); //$NON-NLS-1$

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getDeleteBatchByPrimaryKeyStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", "java.util.List")); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("delete from "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        boolean and = false;
        int keySize = introspectedTable.getPrimaryKeyColumns().size();
        /* 联合主键
         * <foreach collection="list" item="yourEntity" index="index" separator="or">
                ( some1=#{yourEntity.some1}
                AND some2=#{yourEntity.some2} )
            </foreach>
         */
        if (keySize > 1) {
            String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
            sb.setLength(0);
            sb.append(" where ");
            answer.addElement(new TextElement(sb.toString()));
            XmlElement foreach = new XmlElement("foreach");
            foreach.addAttribute(new Attribute("collection", "list"));
            foreach.addAttribute(new Attribute("item", tableName));
            foreach.addAttribute(new Attribute("index", "index"));
            foreach.addAttribute(new Attribute("separator", "or"));
            answer.addElement(foreach);
            sb.setLength(0);
            sb.append("(");
            for (IntrospectedColumn introspectedColumn : introspectedTable
                    .getPrimaryKeyColumns()) {
                if (and) {
                    sb.append(" and "); //$NON-NLS-1$
                } else {
                    and = true;
                }
                sb.append(MyBatis3FormattingUtilities
                        .getEscapedColumnName(introspectedColumn));
                sb.append(" = "); //$NON-NLS-1$
                sb.append(MyBatis3FormattingUtilities
                        .getParameterClause(introspectedColumn, tableName+"."));
            }
            sb.append(")");
            foreach.addElement(new TextElement(sb.toString()));
        } else if (keySize == 1) { //单个主键
            IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
            sb.setLength(0);
            sb.append("where "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" in ");
            answer.addElement(new TextElement(sb.toString()));
            XmlElement foreach = new XmlElement("foreach");
            foreach.addAttribute(new Attribute("collection", "list"));
            foreach.addAttribute(new Attribute("index", "index"));
            foreach.addAttribute(new Attribute("item", introspectedColumn.getActualColumnName()));
            foreach.addAttribute(new Attribute("open", "("));
            foreach.addAttribute(new Attribute("separator", ","));
            foreach.addAttribute(new Attribute("close", ")"));
            foreach.addElement(new TextElement(MyBatis3FormattingUtilities
                    .getParameterClause(introspectedColumn)));
            answer.addElement(foreach);
        }
        /**
         * <foreach collection="list" index="index" item="id" open="(" separator="," close=")" >
         *       #{id}
         *     </foreach>
         */

        if (context.getPlugins()
                .sqlMapDeleteByPrimaryKeyElementGenerated(answer,
                        introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
