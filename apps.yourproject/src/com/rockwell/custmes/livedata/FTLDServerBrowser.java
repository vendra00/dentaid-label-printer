package com.rockwell.custmes.livedata;

import java.util.Iterator;

import javax.swing.JOptionPane;

import com.datasweep.compatibility.ui.FlatButton;
import com.datasweep.compatibility.ui.ScriptArgument;
import com.datasweep.compatibility.ui.TreeNode;
import com.datasweep.compatibility.ui.TreeView;
import com.datasweep.compatibility.ui.TreeViewEvent;
import com.rockwell.activity.CComponentEvent;
import com.rockwell.activity.CComponentEventListener;
import com.rockwell.livedata.FTException;
import com.rockwell.livedata.FTLDPath;
import com.rockwell.livedata.FTSecurityContext;

/**
 * Browses configured FactoryTalk LiveData OPC Servers.  
 *
 * @author spunzman
 */
public class FTLDServerBrowser extends FTLDBrowserActivity implements
        FTLDInterface {

    public static final String SERVER_PREFIX = "RNA://";

    public FTLDServerBrowser() {
        super();

        messageWhenNoNodeSelected = "noServerNodeSelected";

        tree.addCComponentEventListener(new CComponentEventListener() {
            @Override
            public Object ccomponentEventFired(CComponentEvent event) {
                if (event.getEvent().equals(TreeView.SELECTED)) {
                    ScriptArgument[] scripArgs = event.getArgs();
                    TreeViewEvent treeViewEvent = (TreeViewEvent) scripArgs[0]
                            .getArgument();
                    TreeNode eventTreeNode = treeViewEvent.node;

                    if (eventTreeNode.getChildCount() <= 0) {
                        outputItem = ((FTLDPath) eventTreeNode.getUserObject()).toString();
                    } else {
                        outputItem = null;
                    }
                }
                return null;
            }
        });
    }

    @Override
    protected void handleEvent(Object src, String evt) {
        if (src.equals(getOkButton()) && evt.equals(FlatButton.CLICK)) {
            if (outputItem == null) {
                getExceptionHandler().handleError(
                        getMessage(messageWhenNoNodeSelected), null, null, null,
                        JOptionPane.DEFAULT_OPTION);
            } else {
                getFunctions().closeDialog(0);
            }
        } else {
            super.handleEvent(src, evt);
        }
    }

    public String getSelectedPath() {
        return outputItem; 
    }    

    @Override
    public void showFTLD() {
        try {
            LiveDataHandler ldHandler = LiveDataHandler.getLiveDataHandler();
            FTSecurityContext fts = ldHandler.getFTSecurityContext();
            FTLDPath path = FTLDPath.getFromPath(fts, SERVER_PREFIX);
            TreeNode rootNode = parseFTLDStructure(path, new TreeNode(path.toString()));
            tree.addNode(rootNode);
            tree.repaint();
        } catch (FTException ex) {
            getExceptionHandler().handleUnexpectedProblem(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private TreeNode parseFTLDStructure(FTLDPath path, TreeNode parentTreeNode)
            throws FTException {
        for (Iterator<FTLDPath> iter = path.getSubKeys(); iter.hasNext();) {
            FTLDPath subPath = iter.next();

            String result = subPath.toString();
            if (result != null) {
                int n = result.lastIndexOf("/") + 1;
                if (0 < n && n < result.length()) {
                    result = result.substring(n);
                }
            }

            TreeNode serverSubTree = new TreeNode();
            serverSubTree.setUserObject(subPath);
            serverSubTree.setText(result);

            parseFTLDStructure(subPath, serverSubTree);
            parentTreeNode.addNode(serverSubTree);
        }
        return parentTreeNode;
    }
}
