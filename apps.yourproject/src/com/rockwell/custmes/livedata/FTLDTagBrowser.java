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
import com.rockwell.livedata.FTLDBrowser;
import com.rockwell.livedata.LiveDataServer;

/**
 * Browses FactoryTalk LiveData Tags for on a OPC Server configured in 
 * the singleton LiveDataHandler. 
 * The dialog will allow to browse for all configured OPC servers.   
 *
 * @author spunzman
 */
public class FTLDTagBrowser extends FTLDBrowserActivity implements
        FTLDInterface {

    public FTLDTagBrowser() {
        super();

        messageWhenNoNodeSelected = "noTagNodeSelected";

        tree.addCComponentEventListener(new CComponentEventListener() {
            @Override
            public Object ccomponentEventFired(CComponentEvent event) {
                if (event.getEvent().equals(TreeView.SELECTED)) {
                    ScriptArgument[] scripArgs = event.getArgs();
                    TreeViewEvent treeViewEvent = (TreeViewEvent) scripArgs[0].getArgument();
                    TreeNode eventTreeNode = treeViewEvent.node;
                    FTLDBrowser browser = (FTLDBrowser) eventTreeNode.getUserObject();

                    if (eventTreeNode.getChildCount() <= 0) {
                        try {
                            outputItem = browser.getFullName(eventTreeNode.getText());
                        } catch (FTException ex) {
                            getExceptionHandler().handleUnexpectedProblem(ex);
                        }
                    } else {
                        outputItem = null;
                    }
                }
                return null;
            }
        });
    }

    public String getSelectedPath() {
        return outputItem; 
    }        

    @Override
    public void showFTLD() {
        try {
            LiveDataHandler ldHandler = LiveDataHandler.getLiveDataHandler();

            for (LiveDataServer ls : ldHandler.getServers()) {
                TreeNode serverNode = new TreeNode();
                serverNode.setUserObject(ls.getBrowser());
                serverNode.setText(ls.toString());

                tree.addNode(new MyTreeNode(ls.getPath(), ls.getFTLDBrowser()));
            }
        } catch (FTException ex) {
            getExceptionHandler().handleUnexpectedProblem(ex);
        }
    }


    @SuppressWarnings("unchecked")
    class MyTreeNode extends TreeNode {

        MyTreeNode(String name, FTLDBrowser browser) throws FTException {
            super();
            this.setUserObject(browser);
            this.setText(name);

            String browserChild, browserItem;

            for (Iterator<String> ex = browser.getChildrenIterator(); ex
                    .hasNext();) {
                browserChild = ex.next();
                add(new MyTreeNode(browserChild, browser.getChild(browserChild)));
            }

            for (Iterator<String> e = browser.getItemIterator(); e.hasNext();) {
                browserItem = e.next();
                TreeNode leafNode = new TreeNode();
                leafNode.setUserObject(browser);
                leafNode.setText(browserItem);
                add(leafNode);
            }
        }
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
}
