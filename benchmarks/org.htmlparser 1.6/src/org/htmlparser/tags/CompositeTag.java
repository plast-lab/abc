// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /cvsroot/htmlparser/htmlparser/src/org/htmlparser/tags/CompositeTag.java,v $
// $Author: derrickoswald $
// $Date: 2006/05/31 02:10:15 $
// $Revision: 1.82 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//

package org.htmlparser.tags;

import java.util.Locale;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Text;
import org.htmlparser.Tag;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.AbstractNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.scanners.CompositeTagScanner;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.htmlparser.visitors.NodeVisitor;

/**
 * The base class for tags that have an end tag.
 * Provided extra accessors for the children above and beyond what the basic
 * {@link Tag} provides. Also handles the conversion of it's children for
 * the {@link #toHtml toHtml} method.
 */
public class CompositeTag extends TagNode
{
    /**
     * The tag that causes this tag to finish.
     * May be a virtual tag generated by the scanning logic.
     */
    protected Tag mEndTag;

    /**
     * The default scanner for non-composite tags.
     */
    protected final static CompositeTagScanner mDefaultCompositeScanner = new CompositeTagScanner ();

    /**
     * Create a composite tag.
     */
    public CompositeTag ()
    {
        setThisScanner (mDefaultCompositeScanner);
    }
    
    /**
     * Get an iterator over the children of this node.
     * @return Am iterator over the children of this node.
     */
    public SimpleNodeIterator children ()
    {
        SimpleNodeIterator ret;

        if (null != getChildren ())
            ret = getChildren ().elements ();
        else
            ret = (new NodeList ()).elements ();

        return (ret);
    }

    /**
     * Get the child of this node at the given position.
     * @param index The in the node list of the child.
     * @return The child at that index.
     */
    public Node getChild (int index)
    {
        return (
            (null == getChildren ()) ? null :
            getChildren ().elementAt (index));
    }

    /**
     * Get the children as an array of <code>Node</code> objects.
     * @return The children in an array.
     */
    public Node [] getChildrenAsNodeArray ()
    {
        return (
            (null == getChildren ()) ? new Node[0] :
            getChildren ().toNodeArray ());
    }

    /**
     * Remove the child at the position given.
     * @param i The index of the child to remove.
     */
    public void removeChild (int i)
    {
        if (null != getChildren ())
            getChildren ().remove (i);
    }

    /**
     * Return the child tags as an iterator.
     * Equivalent to calling getChildren ().elements ().
     * @return An iterator over the children.
     */
    public SimpleNodeIterator elements()
    {
        return (
            (null == getChildren ()) ? new NodeList ().elements () :
            getChildren ().elements ());
    }

    /**
     * Return the textual contents of this tag and it's children.
     * @return The 'browser' text contents of this tag.
     */
    public String toPlainTextString() {
        StringBuffer stringRepresentation = new StringBuffer();
        for (SimpleNodeIterator e=children();e.hasMoreNodes();) {
            stringRepresentation.append(e.nextNode().toPlainTextString());
        }
        return stringRepresentation.toString();
    }

    /**
     * Add the textual contents of the children of this node to the buffer.
     * @param verbatim If <code>true</code> return as close to the original
     * page text as possible.
     * @param sb The buffer to append to.
     */
    protected void putChildrenInto (StringBuffer sb, boolean verbatim)
    {
        Node node;
        for (SimpleNodeIterator e = children (); e.hasMoreNodes ();)
        {
            node = e.nextNode ();
            // eliminate virtual tags
            if (!verbatim || !(node.getStartPosition () == node.getEndPosition ()))
                sb.append (node.toHtml ());
        }
    }

    /**
     * Add the textual contents of the end tag of this node to the buffer.
     * @param verbatim If <code>true</code> return as close to the original
     * page text as possible.
     * @param sb The buffer to append to.
     */
    protected void putEndTagInto (StringBuffer sb, boolean verbatim)
    {
        // eliminate virtual tags
        if (!verbatim || !(mEndTag.getStartPosition () == mEndTag.getEndPosition ()))
            sb.append (getEndTag ().toHtml());
    }

    /**
     * Return this tag as HTML code.
     * @param verbatim If <code>true</code> return as close to the original
     * page text as possible.
     * @return This tag and it's contents (children) and the end tag
     * as HTML code.
     */
    public String toHtml (boolean verbatim)
    {
        StringBuffer ret;
        
        ret = new StringBuffer ();
        ret.append (super.toHtml (verbatim));
        if (!isEmptyXmlTag ())
        {
            putChildrenInto (ret, verbatim);
            if (null != getEndTag ())
                putEndTagInto (ret, verbatim);
        }
        return (ret.toString ());
    }

    /**
     * Searches all children who for a name attribute. Returns first match.
     * @param name Attribute to match in tag
     * @return Tag Tag matching the name attribute
     */
    public Tag searchByName(String name) {
        Node node;
        Tag tag = null;
        boolean found = false;
        for (SimpleNodeIterator e = children();e.hasMoreNodes() && !found;) {
            node = e.nextNode();
            if (node instanceof Tag)
            {
                tag = (Tag)node;
                String nameAttribute = tag.getAttribute("NAME");
                if (nameAttribute!=null && nameAttribute.equals(name))
                    found=true;
            }
        }
        if (found)
            return tag;
        else
            return null;
    }

    /**
     * Searches for all nodes whose text representation contains the search string.
     * Collects all nodes containing the search string into a NodeList.
     * This search is <b>case-insensitive</b> and the search string and the
     * node text are converted to uppercase using an English locale.
     * For example, if you wish to find any textareas in a form tag containing
     * "hello world", the code would be:
     * <code>
     * NodeList nodeList = formTag.searchFor("Hello World");
     * </code>
     * @param searchString Search criterion.
     * @return A collection of nodes whose string contents or
     * representation have the <code>searchString</code> in them.
     */
    public NodeList searchFor (String searchString)
    {
        return (searchFor (searchString, false));
    }

    /**
     * Searches for all nodes whose text representation contains the search string.
     * Collects all nodes containing the search string into a NodeList.
     * For example, if you wish to find any textareas in a form tag containing
     * "hello world", the code would be:
     * <code>
     * NodeList nodeList = formTag.searchFor("Hello World");
     * </code>
     * @param searchString Search criterion.
     * @param caseSensitive If <code>true</code> this search should be case
     * sensitive. Otherwise, the search string and the node text are converted
     * to uppercase using an English locale.
     * @return A collection of nodes whose string contents or
     * representation have the <code>searchString</code> in them.
     */
    public NodeList searchFor (String searchString, boolean caseSensitive)
    {
        return (searchFor (searchString, caseSensitive, Locale.ENGLISH));
    }

    /**
     * Searches for all nodes whose text representation contains the search string.
     * Collects all nodes containing the search string into a NodeList.
     * For example, if you wish to find any textareas in a form tag containing
     * "hello world", the code would be:
     * <code>
     * NodeList nodeList = formTag.searchFor("Hello World");
     * </code>
     * @param searchString Search criterion.
     * @param caseSensitive If <code>true</code> this search should be case
     * sensitive. Otherwise, the search string and the node text are converted
     * to uppercase using the locale provided.
     * @param locale The locale for uppercase conversion.
     * @return A collection of nodes whose string contents or
     * representation have the <code>searchString</code> in them.
     */
    public NodeList searchFor (String searchString, boolean caseSensitive, Locale locale)
    {
        Node node;
        String text;
        NodeList ret;
        
        ret = new NodeList ();

        if (!caseSensitive)
            searchString = searchString.toUpperCase (locale);
        for (SimpleNodeIterator e = children (); e.hasMoreNodes (); )
        {
            node = e.nextNode ();
            text = node.toPlainTextString ();
            if (!caseSensitive)
                text = text.toUpperCase (locale);
            if (-1 != text.indexOf (searchString))
                ret.add (node);
        }

        return (ret);
    }

    /**
     * Collect all objects that are of a certain type
     * Note that this will not check for parent types, and will not
     * recurse through child tags
     * @param classType The class to search for.
     * @param recursive If true, recursively search through the children.
     * @return A list of children found.
     */
    public NodeList searchFor (Class classType, boolean recursive)
    {
        NodeList children;
        NodeList ret;

        children = getChildren ();
        if (null == children)
            ret = new NodeList ();
        else
            ret = children.extractAllNodesThatMatch (
                new NodeClassFilter (classType), recursive);

        return (ret);
    }

    /**
     * Returns the node number of the first node containing the given text.
     * This can be useful to index into the composite tag and get other children.
     * Text is compared without case sensitivity and conversion to uppercase
     * uses an English locale.
     * @param text The text to search for.
     * @return int The node index in the children list of the node containing
     * the text or -1 if not found.
     * @see #findPositionOf (String, Locale)
     */
    public int findPositionOf (String text)
    {
        return (findPositionOf (text, Locale.ENGLISH));
    }

    /**
     * Returns the node number of the first node containing the given text.
     * This can be useful to index into the composite tag and get other children.
     * Text is compared without case sensitivity and conversion to uppercase
     * uses the supplied locale.
     * @return int The node index in the children list of the node containing
     * the text or -1 if not found.
     * @param locale The locale to use in converting to uppercase.
     * @param text The text to search for.
     */
    public int findPositionOf (String text, Locale locale)
    {
        Node node;
        int loc;
        
        loc = 0;
        text = text.toUpperCase (locale);
        for (SimpleNodeIterator e = children (); e.hasMoreNodes (); )
        {
            node = e.nextNode ();
            if (-1 != node.toPlainTextString ().toUpperCase (locale).indexOf (text))
                return loc;
            loc++;
        }
        return -1;
    }

    /**
     * Returns the node number of a child node given the node object.
     * This would typically be used in conjuction with digUpStringNode,
     * after which the string node's parent can be used to find the
     * string node's position. Faster than calling findPositionOf(text)
     * again. Note that the position is at a linear level alone - there
     * is no recursion in this method.
     * @param searchNode The child node to find.
     * @return The offset of the child tag or -1 if it was not found.
     */
    public int findPositionOf(Node searchNode) {
        Node node;
        int loc = 0;
        for (SimpleNodeIterator e=children();e.hasMoreNodes();) {
            node = e.nextNode();
            if (node==searchNode) {
                return loc;
            }
            loc++;
        }
        return -1;
    }

    /**
     * Get child at given index
     * @param index The index into the child node list.
     * @return Node The child node at the given index or null if none.
     */
    public Node childAt (int index)
    {
        return (
            (null == getChildren ()) ? null :
            getChildren ().elementAt (index));
    }

    /**
     * Collect this node and its child nodes (if-applicable) into the list parameter,
     * provided the node satisfies the filtering criteria.
     * <p>This mechanism allows powerful filtering code to be written very easily,
     * without bothering about collection of embedded tags separately.
     * e.g. when we try to get all the links on a page, it is not possible to
     * get it at the top-level, as many tags (like form tags), can contain
     * links embedded in them. We could get the links out by checking if the
     * current node is a {@link CompositeTag}, and going through its children.
     * So this method provides a convenient way to do this.</p>
     * <p>Using collectInto(), programs get a lot shorter. Now, the code to
     * extract all links from a page would look like:
     * <pre>
     * NodeList list = new NodeList();
     * NodeFilter filter = new TagNameFilter ("A");
     * for (NodeIterator e = parser.elements(); e.hasMoreNodes();)
     *      e.nextNode().collectInto(list, filter);
     * </pre>
     * Thus, <code>list</code> will hold all the link nodes, irrespective of how
     * deep the links are embedded.</p>
     * <p>Another way to accomplish the same objective is:
     * <pre>
     * NodeList list = new NodeList();
     * NodeFilter filter = new TagClassFilter (LinkTag.class);
     * for (NodeIterator e = parser.elements(); e.hasMoreNodes();)
     *      e.nextNode().collectInto(list, filter);
     * </pre>
     * This is slightly less specific because the LinkTag class may be
     * registered for more than one node name, e.g. &lt;LINK&gt; tags too.</p>
     * @param list The list to add nodes to.
     * @param filter The filter to apply.
     * @see org.htmlparser.filters
     */
    public void collectInto (NodeList list, NodeFilter filter)
    {
        super.collectInto (list, filter);
        for (SimpleNodeIterator e = children(); e.hasMoreNodes ();)
            e.nextNode ().collectInto (list, filter);
        if ((null != getEndTag ()) && (this != getEndTag ())) // 2nd guard handles <tag/>
            getEndTag ().collectInto (list, filter);
    }

    /**
     * Return the HTML code for the children of this tag.
     * @return A string with the HTML code for the contents of this tag.
     */
    public String getChildrenHTML() {
        StringBuffer buff = new StringBuffer();
        for (SimpleNodeIterator e = children();e.hasMoreNodes();) {
            AbstractNode node = (AbstractNode)e.nextNode();
            buff.append(node.toHtml());
        }
        return buff.toString();
    }

    /**
     * Tag visiting code.
     * Invokes <code>accept()</code> on the start tag and then
     * walks the child list invoking <code>accept()</code> on each
     * of the children, finishing up with an <code>accept()</code>
     * call on the end tag. If <code>shouldRecurseSelf()</code>
     * returns true it then asks the visitor to visit itself.
     * @param visitor The <code>NodeVisitor</code> object to be signalled
     * for each child and possibly this tag.
     */
    public void accept (NodeVisitor visitor)
    {
        SimpleNodeIterator children;
        Node child;

        if (visitor.shouldRecurseSelf ())
            visitor.visitTag (this);
        if (visitor.shouldRecurseChildren ())
        {
            if (null != getChildren ())
            {
                children = children ();
                while (children.hasMoreNodes ())
                {
                    child = children.nextNode ();
                    child.accept (visitor);
                }
            }
            if ((null != getEndTag ()) && (this != getEndTag ())) // 2nd guard handles <tag/>
                getEndTag ().accept (visitor);
        }
    }

    /**
     * Return the number of child nodes in this tag.
     * @return The child node count.
     */
    public int getChildCount()
    {
        NodeList children;
        
        children = getChildren ();

        return ((null == children) ? 0 : children.size ());
    }

    /**
     * Get the end tag for this tag.
     * For example, if the node is {@.html <LABEL>The label</LABLE>}, then
     * this method would return the {@.html </LABLE>} end tag.
     * @return The end tag for this node.
     * <em>Note: If the start and end position of the end tag is the same,
     * then the end tag was injected (it's a virtual end tag).</em>
     */
    public Tag getEndTag()
    {
        return (mEndTag);
    }

    /**
     * Set the end tag for this tag.
     * @param tag The new end tag for this tag.
     * Note: no checking is perfromed so you can generate bad HTML by setting
     * the end tag with a name not equal to the name of the start tag,
     * i.e. {@.html <LABEL>The label</TITLE>}
     */
    public void setEndTag (Tag tag)
    {
        mEndTag = tag;
    }

    /**
     * Finds a text node, however embedded it might be, and returns
     * it. The text node will retain links to its parents, so
     * further navigation is possible.
     * @param searchText The text to search for.
     * @return The list of text nodes (recursively) found.
     */
    public Text[] digupStringNode(String searchText) {
        NodeList nodeList = searchFor(searchText);
        NodeList stringNodes = new NodeList();
        for (int i=0;i<nodeList.size();i++) {
            Node node = nodeList.elementAt(i);
            if (node instanceof Text) {
                stringNodes.add(node);
            } else {
                if (node instanceof CompositeTag) {
                    CompositeTag ctag = (CompositeTag)node;
                    Text[] nodes = ctag.digupStringNode(searchText);
                    for (int j=0;j<nodes.length;j++)
                        stringNodes.add(nodes[j]);
                }
            }
        }
        Text[] stringNode = new Text[stringNodes.size()];
        for (int i=0;i<stringNode.length;i++) {
            stringNode[i] = (Text)stringNodes.elementAt(i);
        }
        return stringNode;
    }

    /**
     * Return a string representation of the contents of this tag, it's children and it's end tag suitable for debugging.
     * @return A textual representation of the tag.
     */
    public String toString ()
    {
        StringBuffer ret;
        
        ret = new StringBuffer (1024);
        toString (0, ret);
        
        return (ret.toString ());
    }

    /**
     * Return the text contained in this tag.
     * @return The complete contents of the tag (within the angle brackets).
     */
    public String getText ()
    {
        String ret;
        
        ret = super.toHtml (true); // need TagNode.toHtml(boolean)
        ret = ret.substring (1, ret.length () - 1);
        
        return (ret);
    }

    /**
     * Return the text between the start tag and the end tag.
     * @return The contents of the CompositeTag.
     */
    public String getStringText ()
    {
        String ret;
        int start = getEndPosition ();
        int end = mEndTag.getStartPosition ();
        ret = getPage ().getText (start, end);
        
        return (ret);
    }

    /**
     * Return a string representation of the contents of this tag, it's children and it's end tag suitable for debugging.
     * @param level The indentation level to use.
     * @param buffer The buffer to append to.
     */
    public void toString (int level, StringBuffer buffer)
    {
        Node node;

        for (int i = 0; i < level; i++)
            buffer.append ("  ");
        buffer.append (super.toString ());
        buffer.append (System.getProperty ("line.separator"));
        for (SimpleNodeIterator e = children (); e.hasMoreNodes ();)
        {
            node = e.nextNode ();
            if (node instanceof CompositeTag)
                ((CompositeTag)node).toString (level + 1, buffer);
            else
            {
                for (int i = 0; i <= level; i++)
                    buffer.append ("  ");
                buffer.append (node);
                buffer.append (System.getProperty ("line.separator"));
            }
        }
        
        if ((null != getEndTag ()) && (this != getEndTag ())) // 2nd guard handles <tag/>
            // eliminate virtual tags
//            if (!(getEndTag ().getStartPosition () == getEndTag ().getEndPosition ()))
            {
                for (int i = 0; i <= level; i++)
                    buffer.append ("  ");
                buffer.append (getEndTag ().toString ());
                buffer.append (System.getProperty ("line.separator"));
            }
    }
}
