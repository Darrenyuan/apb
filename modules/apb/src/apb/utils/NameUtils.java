
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

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

import static java.lang.Character.*;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Sep 9, 2008
// Time: 5:45:50 PM

//
public class NameUtils
{
    //~ Methods ..............................................................................................

    @NotNull
    public static String idFromMember(@NotNull Member member)
    {
        return idFromJavaId(member.getName());
    }

    @NotNull
    public static String idFromClass(@NotNull Class<?> clazz)
    {
        return idFromJavaId(name(clazz));
    }

    private static String idFromJavaId(String className)
    {
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < className.length(); i++) {
            final char chr = className.charAt(i);

            if (i > 0 && isUpperCase(chr) && isLowerCase(className.charAt(i - 1))) {
                result.append('-');
            }

            if (chr == '_' || chr == '$') {
                result.append('-');
            }
            else {
                result.append(toLowerCase(chr));
            }
        }

        return result.toString();
    }

    @NotNull public static String name(@NotNull Class clazz)
    {
        final String result;

        if (clazz.isArray()) {
            result = name(clazz.getComponentType()) + "[]";
        }
        else {
            //replace '$' by '.' for Inner classes
            result = clazz.getName().replace('$', '.');
        }

        return result;
    }

    @NotNull public static String dirFromId(@NotNull String id) {
        return id.replace('.', '/');
    }
}
