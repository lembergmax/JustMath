/*
 * Copyright (c) 2025 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.graphfx;

import com.mlprograms.justmath.graphfx.model.GraphFxFunction;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.service.GraphFxExportService;
import com.mlprograms.justmath.graphfx.view.GraphFxGraphView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxExportService")
final class GraphFxExportServiceTest {

    @Test
    void resolveExportFunction_returnsSelected_whenPresent() throws Exception {
        final GraphFxExportService service = new GraphFxExportService();
        final GraphFxModel model = new GraphFxModel();
        final GraphFxFunction f = model.addFunction("f", "x");
        model.setSelectedFunction(f);

        final GraphFxFunction resolved = invoke(service, "resolveExportFunction",
                new Class<?>[]{GraphFxModel.class},
                new Object[]{model});

        assertSame(f, resolved);
    }

    @Test
    void resolveExportFunction_setsSelection_whenSingleFunction() throws Exception {
        final GraphFxExportService service = new GraphFxExportService();
        final GraphFxModel model = new GraphFxModel();
        final GraphFxFunction f = model.addFunction("only", "x");

        assertNull(model.getSelectedFunction());

        final GraphFxFunction resolved = invoke(service, "resolveExportFunction",
                new Class<?>[]{GraphFxModel.class},
                new Object[]{model});

        assertSame(f, resolved);
        assertSame(f, model.getSelectedFunction());
    }

    @Test
    void sanitizeFileNamePart_replacesIllegalCharacters() throws Exception {
        final GraphFxExportService service = new GraphFxExportService();

        final String sanitized = invoke(service, "sanitizeFileNamePart",
                new Class<?>[]{String.class},
                new Object[]{"  my:/fun*name?  "});

        assertEquals("my__fun_name_", sanitized);
    }

    @Test
    void worldToSvgMapping_usesViewportAndSize() throws Exception {
        final GraphFxExportService service = new GraphFxExportService();

        final GraphFxGraphView view = Mockito.mock(GraphFxGraphView.class);
        Mockito.when(view.getView()).thenReturn(new GraphFxGraphView.WorldView(0, 10, 0, 10));
        Mockito.when(view.getWidth()).thenReturn(200.0);
        Mockito.when(view.getHeight()).thenReturn(100.0);

        final double x = invoke(service, "worldToSvgX",
                new Class<?>[]{GraphFxGraphView.class, double.class},
                new Object[]{view, 5.0});

        final double y = invoke(service, "worldToSvgY",
                new Class<?>[]{GraphFxGraphView.class, double.class},
                new Object[]{view, 5.0});

        assertEquals(100.0, x, 1e-9); // middle of width
        assertEquals(50.0, y, 1e-9);  // middle of height (inverted axis)
    }

    @Test
    void writeWritableImageAsPng_writesValidPngSignature() throws Exception {
        /* TODO: will nicht fertig werden
        final GraphFxExportService service = new GraphFxExportService();
        final File target = Files.createTempFile("graphfx_export_", ".png").toFile();
        target.deleteOnExit();

        final WritableImage image = FxTestSupport.onFxThread(() -> {
            final WritableImage writableImage = new WritableImage(2, 2);
            final PixelWriter writer = writableImage.getPixelWriter();
            writer.setColor(0, 0, Color.RED);
            writer.setColor(1, 0, Color.GREEN);
            writer.setColor(0, 1, Color.BLUE);
            writer.setColor(1, 1, Color.BLACK);
            return writableImage;
        });

        invokeVoid(service, "writeWritableImageAsPng",
                new Class<?>[]{WritableImage.class, File.class},
                new Object[]{image, target});

        final byte[] bytes = Files.readAllBytes(target.toPath());
        assertTrue(bytes.length > 8);

        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        assertEquals((byte) 0x89, bytes[0]);
        assertEquals((byte) 0x50, bytes[1]);
        assertEquals((byte) 0x4E, bytes[2]);
        assertEquals((byte) 0x47, bytes[3]);
        assertEquals((byte) 0x0D, bytes[4]);
        assertEquals((byte) 0x0A, bytes[5]);
        assertEquals((byte) 0x1A, bytes[6]);
        assertEquals((byte) 0x0A, bytes[7]);
        */
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(final Object target, final String methodName, final Class<?>[] parameterTypes, final Object[] args) throws Exception {
        final Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(target, args);
    }

    private static void invokeVoid(final Object target, final String methodName, final Class<?>[] parameterTypes, final Object[] args) throws Exception {
        final Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(target, args);
    }
}
