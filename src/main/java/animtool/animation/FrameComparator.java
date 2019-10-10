/*
 * MIT License
 *
 * Copyright (c) 2019. Austin Thompson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// Source: https://stackoverflow.com/a/23249000

package animtool.animation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FrameComparator implements Comparator<Frame> {

    private static final Pattern splitPattern = Pattern.compile("\\d+|\\.|\\s");

    @Override
    public int compare(Frame f1, Frame f2) {
        Iterator<String> i1 = splitStringPreserveDelimiter(f1.getFile().getPath()).iterator();
        Iterator<String> i2 = splitStringPreserveDelimiter(f2.getFile().getPath()).iterator();
        while (true) {
            //Til here all is equal.
            if (!i1.hasNext() && !i2.hasNext()) {
                return 0;
            }
            //first has no more parts -> comes first
            if (!i1.hasNext()) {
                return -1;
            }
            //first has more parts than i2 -> comes after
            if (!i2.hasNext()) {
                return 1;
            }

            String data1 = i1.next();
            String data2 = i2.next();
            int result;
            try {
                //If both datas are numbers, then compare numbers
                result = Long.compare(Long.valueOf(data1), Long.valueOf(data2));
                //If numbers are equal than longer comes first
                if (result == 0) {
                    result = -Integer.compare(data1.length(), data2.length());
                }
            } catch (NumberFormatException ex) {
                //compare text case insensitive
                result = data1.compareToIgnoreCase(data2);
            }

            if (result != 0) {
                return result;
            }
        }
    }

    private List<String> splitStringPreserveDelimiter(String str) {
        Matcher matcher = splitPattern.matcher(str);
        List<String> list = new ArrayList<String>();
        int pos = 0;
        while (matcher.find()) {
            list.add(str.substring(pos, matcher.start()));
            list.add(matcher.group());
            pos = matcher.end();
        }
        list.add(str.substring(pos));
        return list;
    }

}
