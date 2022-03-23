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

package org.geysermc.geyser.util;

import com.nukkitx.nbt.NbtMap;

/**
 * @author LBuke (Teddeh)
 */
public final class ImageUtils {

    public static NbtMap getRenderOffsets(int width, int height) {
        int minSize = Math.min(width, height);
        float x = (float) (0.075/(minSize/16));
        float y = (float) (0.125/(minSize/16));
        float z = (float) (0.075/(minSize/16));

        return NbtMap.builder()
                .putCompound("main_hand", NbtMap.builder()
                        .putCompound("first_person", NbtMap.builder()
                                .putCompound("scale", NbtMap.builder()
                                        .putFloat("x", x)
                                        .putFloat("y", y)
                                        .putFloat("z", z).build()
                                ).build()
                        ).putCompound("third_person", NbtMap.builder()
                                .putCompound("scale", NbtMap.builder()
                                        .putFloat("x", x)
                                        .putFloat("y", y)
                                        .putFloat("z", z).build()
                                ).build()
                        ).build()
                ).putCompound("off_hand", NbtMap.builder()
                        .putCompound("first_person", NbtMap.builder()
                                .putCompound("scale", NbtMap.builder()
                                        .putFloat("x", x)
                                        .putFloat("y", y)
                                        .putFloat("z", z).build()
                                ).build()
                        ).putCompound("third_person", NbtMap.builder()
                                .putCompound("scale", NbtMap.builder()
                                        .putFloat("x", x)
                                        .putFloat("y", y)
                                        .putFloat("z", z).build()
                                ).build()
                        ).build()
                ).build();
    }
}
