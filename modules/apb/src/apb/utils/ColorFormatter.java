

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
//


package apb.utils;

import apb.Environment;
//
// User: emilio
// Date: Dec 4, 2008
// Time: 4:40:10 PM

public class ColorFormatter
    extends SimpleFormatter
{
    //~ Constructors .........................................................................................

    public ColorFormatter(Environment env)
    {
        super(env);
    }

    //~ Methods ..............................................................................................

    @Override protected void appendHeader(StringBuilder result)
    {
        result.append(GREEN_COLOR);
        super.appendHeader(result);
        result.append(RESET_COLOR);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String GREEN_COLOR = "\033[32m";
    private static final String RESET_COLOR = "\033[0m";
}
