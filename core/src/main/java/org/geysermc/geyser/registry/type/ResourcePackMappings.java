/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.registry.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.*;

/**
 * @author LBuke (Teddeh)
 */
@SuppressWarnings("FieldMayBeFinal") // Jackson requires
public final class ResourcePackMappings {
    @Getter
    @JsonProperty("java_pack_format_version")
    private int javaPackFormatVersion = 6;

    @Getter
    @JsonProperty("java_pack_description")
    private String javaPackDescription = "";

    @Getter
    @JsonProperty("converted_pack_description")
    private String convertedPackDescription = "";

    @Getter
    @JsonProperty("converted_pack_uuid")
    private String convertedPackUuid = "";

    @Getter
    @JsonProperty("converted_pack_version")
    private int[] convertedPackVersion = {1, 0, 0};

    @Getter
    @JsonProperty("custom-model-data-items")
    private Set<String> customModelDataItems = new HashSet<>();

    @Getter
    @JsonProperty("mappings_data")
    private Map<String, List<CustomModelDataMapping>> mappings = new HashMap<>();
}
