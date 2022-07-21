/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package top.gcszhn.jvision.chart;

import java.io.Serializable;

import top.gcszhn.jvision.JvisionException;

/**
 * Abstract class for drawing charts.
 */
public interface JChart extends Serializable {

    /**
     * Load data from file
     * 
     * @param file File path
     */
    public void loadData(String file) throws JvisionException;

    /**
     * Draw chart to image file
     * 
     * @param file Image file name, such as png, jpg, pdf, etc.
     */
    public void draw(String file) throws JvisionException;

    /**
     * set chart font family
     * @param fontFamily font family available in system
     */
    public void setFontFamily(String fontFamily);

    /**
     * get chart font family
     * @return font family
     */
    public String getFontFamily();

    /**
     * set chart font style
     * @param fontStyle font style Font.PLAIN, Font.BOLD, Font.ITALIC or Font.BOLD + Font.ITALIC
     */
    public void setFontStyle(int fontStyle);

    /**
     * get chart font style
     * @return font style
     */
    public int getFontStyle();

    public int getWidth();

    public int getHeight();
}
