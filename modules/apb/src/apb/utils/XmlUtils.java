
// Copyright 2008-2009 Emilio Lopez-Gabeiras
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License

package apb.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.Writer;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import static apb.utils.ClassUtils.invoke;
import static apb.utils.ClassUtils.newInstance;
//
// User: emilio
// Date: Dec 19, 2008
// Time: 2:37:30 PM

//
public class XmlUtils
{
    //~ Methods ..............................................................................................

    public static void writeDocument(@NotNull Document document, @NotNull File file)
    {
        try {
            Writer output = new BufferedWriter(FileUtils.createWriter(file));

            //
            //            Replace the following code by an equivalent using reflection to
            //            avoid stupid warnings from the compiler
            //
            //            final OutputFormat format = new OutputFormat(document, "UTF-8", true);
            //            final XMLSerializer serializer = new XMLSerializer(output, format);
            //            serializer.serialize(document);
            //
            final Object format = newInstance(OUTPUT_FORMAT, document, "UTF-8", true);
            final Object serializer = newInstance(XML_SERIALIZER, output, format);
            invoke(serializer, "serialize", document);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Element findChildElement(Element parent, String name)
    {
        Element  result = null;
        NodeList nodes = parent.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeName().equals(name)) {
                result = (Element) node;
                break;
            }
        }

        return result;
    }

    public static void removeAllChildren(Element deps)
    {
        NodeList childNodes = deps.getChildNodes();

        for (int i = childNodes.getLength() - 1; i >= 0; i--) {
            Node item = childNodes.item(i);
            deps.removeChild(item);
        }
    }

    public static void addValuedElement(Document document, Element parent, String elementName, String content)
    {
        Element name = document.createElement(elementName);

        Text text = document.createTextNode(content);
        name.appendChild(text);
        parent.appendChild(name);
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls public static final String OUTPUT_FORMAT =
        "com.sun.org.apache.xml.internal.serialize.OutputFormat";
    @NonNls public static final String XML_SERIALIZER =
        "com.sun.org.apache.xml.internal.serialize.XMLSerializer";
}
