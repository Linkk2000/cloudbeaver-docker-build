/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
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
package org.jkiss.dbeaver.ui.editors.sql.plan.simple;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchSite;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.exec.plan.DBCPlan;
import org.jkiss.dbeaver.model.exec.plan.DBCPlanNode;
import org.jkiss.dbeaver.model.exec.plan.DBCPlanNodeKind;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.ui.LoadingJob;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.ObjectViewerRenderer;
import org.jkiss.dbeaver.ui.navigator.actions.NavigatorHandlerObjectOpen;
import org.jkiss.dbeaver.ui.navigator.itemlist.DatabaseObjectListControl;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Plan nodes tree
 */
public class PlanNodesTree extends DatabaseObjectListControl<DBCPlanNode> {

    private String query;
    private DBPDataSource dataSource;

    private static final String CONFIG_COLOR_INDEXSCAN_BACKGROUND = "org.jkiss.dbeaver.sql.plan.view.color.indexscan.background";
    private static final String CONFIG_COLOR_INDEXSCAN_FOREGROUND = "org.jkiss.dbeaver.sql.plan.view.color.indexscan.foreground";
    private static final String CONFIG_COLOR_TABLESCAN_BACKGROUND = "org.jkiss.dbeaver.sql.plan.view.color.tablescan.background";
    private static final String CONFIG_COLOR_TABLESCAN_FOREGROUND = "org.jkiss.dbeaver.sql.plan.view.color.tablescan.foreground";

    public PlanNodesTree(Composite parent, int style, IWorkbenchSite site)
    {
        super(parent, style, site, CONTENT_PROVIDER);
        setFitWidth(true);
    }

    @Override
    protected ObjectViewerRenderer createRenderer()
    {
        return new PlanTreeRenderer();
    }

    @NotNull
    @Override
    protected String getListConfigId(List<Class<?>> classList) {
        return "ExecutionPlan/" + dataSource.getContainer().getDriver().getId();
    }

    @Override
    protected LoadingJob<Collection<DBCPlanNode>> createLoadService(boolean forUpdate)
    {
        return null;
    }

    public void showPlan(DBPDataSource dataSource, DBCPlan plan) {
        this.dataSource = dataSource;

        List<DBCPlanNode> nodes = new ArrayList<>(plan.getPlanNodes(
            Collections.singletonMap(DBCPlan.OPTION_KEEP_ORIGINAL, true)));

        final TreeViewer itemsViewer = (TreeViewer) PlanNodesTree.this.getItemsViewer();
        itemsViewer.getControl().setRedraw(false);
        try {
            clearListData();
            setListData(nodes, false, false);
            itemsViewer.expandToLevel(10);
        } finally {
            itemsViewer.getControl().setRedraw(true);
        }

    }

    @Override
    public boolean supportsDataGrouping() {
        return false;
    }

    private static ITreeContentProvider CONTENT_PROVIDER = new ITreeContentProvider() {
        @Override
        public Object[] getElements(Object inputElement)
        {
            if (inputElement instanceof Collection) {
                return ((Collection<?>)inputElement).toArray();
            }
            return null;
        }

        @Override
        public Object[] getChildren(Object parentElement)
        {
            if (parentElement instanceof DBCPlanNode) {
                Collection<? extends DBCPlanNode> nestedNodes = ((DBCPlanNode) parentElement).getNested();
                return CommonUtils.isEmpty(nestedNodes) ? new Object[0] : nestedNodes.toArray();
            }
            return null;
        }

        @Override
        public Object getParent(Object element)
        {
            if (element instanceof DBCPlanNode) {
                return ((DBCPlanNode)element).getParent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element)
        {
            return element instanceof DBCPlanNode && !CommonUtils.isEmpty(((DBCPlanNode) element).getNested());
        }

        @Override
        public void dispose()
        {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
        }

    };

    private class PlanTreeRenderer extends ViewerRenderer {
        @Override
        public boolean isHyperlink(Object element, Object cellValue)
        {
            return cellValue instanceof DBSObject;
        }

        @Override
        public void navigateHyperlink(Object cellValue)
        {
            if (cellValue instanceof DBSObject) {
                NavigatorHandlerObjectOpen.openEntityEditor((DBSObject) cellValue);
            }
        }

    }

    @Override
    protected Color getObjectBackground(DBCPlanNode item) {
        if (item.getNodeKind() == DBCPlanNodeKind.TABLE_SCAN) {
            return UIUtils.getColorRegistry().get(CONFIG_COLOR_TABLESCAN_BACKGROUND);
        } else if (item.getNodeKind() == DBCPlanNodeKind.INDEX_SCAN) {
            return UIUtils.getColorRegistry().get(CONFIG_COLOR_INDEXSCAN_BACKGROUND);
        }
        return super.getObjectBackground(item);
    }

    @Override
    protected Color getObjectForeground(DBCPlanNode item) {
        if (item.getNodeKind() == DBCPlanNodeKind.TABLE_SCAN) {
            return UIUtils.getColorRegistry().get(CONFIG_COLOR_TABLESCAN_FOREGROUND);
        } else if (item.getNodeKind() == DBCPlanNodeKind.INDEX_SCAN) {
            return UIUtils.getColorRegistry().get(CONFIG_COLOR_INDEXSCAN_FOREGROUND);
        }
        return super.getObjectForeground(item);
    }

}
