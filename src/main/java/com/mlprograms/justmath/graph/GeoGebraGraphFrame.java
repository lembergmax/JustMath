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

package com.mlprograms.justmath.graph;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graph.GeoGebraGraphPanel;
import com.mlprograms.justmath.graph.GraphScene;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * GeoGebra-like frame with sidebar (functions + variables) and a graph panel.
 */
public class GeoGebraGraphFrame extends JFrame {

    private final GraphScene scene;
    private final CalculatorEngine engine;
    private final GeoGebraGraphPanel panel;

    private final JLabel statusLabel = new JLabel("x=0, y=0");

    private final FunctionsTableModel functionsModel;
    private final JTable functionsTable;

    public GeoGebraGraphFrame(@NonNull final CalculatorEngine engine) {
        super("JustMath - Graph (GeoGebra-like)");

        this.scene = new GraphScene();
        this.engine = engine;
        this.panel = new GeoGebraGraphPanel(scene, engine);

        this.functionsModel = new FunctionsTableModel(scene);
        this.functionsTable = new JTable(functionsModel);

        scene.addFunction("f", "sin(x)+x^2");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        panel.setStatusListener((x, y) -> statusLabel.setText("x=" + format(x) + "   y=" + format(y)));

        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        SwingUtilities.invokeLater(() -> {
            functionsModel.fireTableDataChanged();
            panel.fitToData();
        });
    }

    private JComponent buildToolbar() {
        final JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        tb.add(toolButton("Move", GeoGebraGraphPanel.ToolMode.MOVE));
        tb.add(toolButton("Zoom Box", GeoGebraGraphPanel.ToolMode.ZOOM_BOX));
        tb.addSeparator();
        tb.add(toolButton("Point", GeoGebraGraphPanel.ToolMode.POINT_ON_FUNCTION));
        tb.add(toolButton("Tangent", GeoGebraGraphPanel.ToolMode.TANGENT));
        tb.add(toolButton("Normal", GeoGebraGraphPanel.ToolMode.NORMAL));
        tb.add(toolButton("Root", GeoGebraGraphPanel.ToolMode.ROOT));
        tb.add(toolButton("Intersect", GeoGebraGraphPanel.ToolMode.INTERSECTION));
        tb.add(toolButton("Integral", GeoGebraGraphPanel.ToolMode.INTEGRAL));
        tb.addSeparator();

        final JButton fit = new JButton("Fit");
        fit.addActionListener(e -> panel.fitToData());
        tb.add(fit);

        final JCheckBox grid = new JCheckBox("Grid", scene.getSettings().isShowGrid());
        grid.addActionListener(e -> {
            scene.getSettings().setShowGrid(grid.isSelected());
            panel.repaint();
        });
        tb.add(grid);

        final JCheckBox axes = new JCheckBox("Axes", scene.getSettings().isShowAxes());
        axes.addActionListener(e -> {
            scene.getSettings().setShowAxes(axes.isSelected());
            panel.repaint();
        });
        tb.add(axes);

        return tb;
    }

    private JButton toolButton(@NonNull final String text, @NonNull final GeoGebraGraphPanel.ToolMode mode) {
        final JButton b = new JButton(text);
        b.addActionListener(e -> panel.setToolMode(mode));
        return b;
    }

    private JComponent buildContent() {
        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0);
        split.setLeftComponent(buildSidebar());
        split.setRightComponent(panel);
        split.setDividerLocation(380);
        return split;
    }

    private JComponent buildSidebar() {
        final JPanel root = new JPanel(new BorderLayout(8, 8));

        root.add(buildFunctionsPanel(), BorderLayout.CENTER);
        root.add(buildVariablesPanel(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildFunctionsPanel() {
        final JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Functions"));

        functionsTable.setFillsViewportHeight(true);
        functionsTable.setRowHeight(22);

        p.add(new JScrollPane(functionsTable), BorderLayout.CENTER);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JButton add = new JButton("Add");
        add.addActionListener(e -> {
            scene.addFunction("f" + (scene.getFunctions().size() + 1), "x");
            functionsModel.fireTableDataChanged();
        });

        final JButton remove = new JButton("Remove");
        remove.addActionListener(e -> {
            final int row = functionsTable.getSelectedRow();
            if (row < 0 || row >= scene.getFunctions().size()) {
                return;
            }
            final UUID id = scene.getFunctions().get(row).getId();
            scene.removeFunction(id);
            functionsModel.fireTableDataChanged();
        });

        buttons.add(add);
        buttons.add(remove);

        p.add(buttons, BorderLayout.SOUTH);

        return p;
    }

    private JComponent buildVariablesPanel() {
        final JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Variables / Sliders"));

        final JPanel sliders = new JPanel();
        sliders.setLayout(new BoxLayout(sliders, BoxLayout.Y_AXIS));

        rebuildSliders(sliders);

        scene.addListener(ch -> SwingUtilities.invokeLater(() -> rebuildSliders(sliders)));

        final JScrollPane sc = new JScrollPane(sliders);
        sc.setPreferredSize(new Dimension(360, 220));

        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    private void rebuildSliders(@NonNull final JPanel container) {
        container.removeAll();

        for (final GraphScene.VariableEntry v : scene.getVariables().values()) {
            final JPanel row = new JPanel(new BorderLayout(6, 0));
            final JCheckBox enabled = new JCheckBox(v.getName(), v.isSliderEnabled());
            enabled.addActionListener(e -> scene.updateVariable(v.toBuilder().sliderEnabled(enabled.isSelected()).build()));

            row.add(enabled, BorderLayout.WEST);

            if (v.isSliderEnabled()) {
                final SliderAdapter adapter = new SliderAdapter(v.getSliderMin(), v.getSliderMax(), v.getSliderStep(), v.getValue());
                final JSlider slider = new JSlider(0, adapter.getMaxIndex(), adapter.toIndex(v.getValue()));
                slider.addChangeListener(e -> {
                    final BigDecimal newVal = adapter.fromIndex(slider.getValue());
                    scene.setVariableValue(v.getName(), newVal);
                });
                row.add(slider, BorderLayout.CENTER);

                final JLabel valueLabel = new JLabel(v.getValue().stripTrailingZeros().toPlainString());
                row.add(valueLabel, BorderLayout.EAST);

                scene.addListener(ch -> SwingUtilities.invokeLater(() -> valueLabel.setText(scene.getVariables().get(v.getName()).getValue().stripTrailingZeros().toPlainString())));
            }

            container.add(row);
        }

        container.revalidate();
        container.repaint();
    }

    private JComponent buildStatusBar() {
        final JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        p.add(statusLabel, BorderLayout.WEST);
        return p;
    }

    private static String format(final double v) {
        return String.format(java.util.Locale.ROOT, "%.6f", v);
    }

    private static final class FunctionsTableModel extends AbstractTableModel {

        private final GraphScene scene;
        private final String[] cols = {"Visible", "Name", "Expression"};

        private FunctionsTableModel(@NonNull final GraphScene scene) {
            this.scene = scene;
            scene.addListener(ch -> SwingUtilities.invokeLater(this::fireTableDataChanged));
        }

        @Override
        public int getRowCount() {
            return scene.getFunctions().size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(final int column) {
            return cols[column];
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return switch (columnIndex) {
                case 0 -> Boolean.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            final GraphScene.FunctionEntry f = scene.getFunctions().get(rowIndex);
            return switch (columnIndex) {
                case 0 -> f.isVisible();
                case 1 -> f.getName();
                case 2 -> f.getExpression();
                default -> "";
            };
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
            final GraphScene.FunctionEntry f = scene.getFunctions().get(rowIndex);

            if (columnIndex == 0) {
                scene.updateFunction(f.toBuilder().visible(Boolean.TRUE.equals(aValue)).build());
            } else if (columnIndex == 1) {
                scene.updateFunction(f.toBuilder().name(String.valueOf(aValue)).build());
            } else if (columnIndex == 2) {
                scene.updateFunction(f.toBuilder().expression(String.valueOf(aValue)).build());
            }
        }
    }

    private static final class SliderAdapter {

        private final BigDecimal min;
        private final BigDecimal step;
        private final int maxIndex;

        private SliderAdapter(@NonNull final BigDecimal min, @NonNull final BigDecimal max, @NonNull final BigDecimal step, @NonNull final BigDecimal current) {
            this.min = min;
            this.step = step;

            final BigDecimal range = max.subtract(min);
            final BigDecimal idx = range.divide(step, 0, java.math.RoundingMode.HALF_UP);
            this.maxIndex = Math.max(1, idx.intValueExact());
        }

        private int getMaxIndex() {
            return maxIndex;
        }

        private int toIndex(@NonNull final BigDecimal value) {
            final BigDecimal d = value.subtract(min);
            return d.divide(step, 0, java.math.RoundingMode.HALF_UP).intValue();
        }

        private BigDecimal fromIndex(final int index) {
            return min.add(step.multiply(BigDecimal.valueOf(index)));
        }
    }
}
