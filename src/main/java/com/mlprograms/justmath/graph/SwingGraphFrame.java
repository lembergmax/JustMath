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
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Simple Swing frame to plot expressions using CalculatorEngine.
 */
public class SwingGraphFrame extends JFrame {

    private final SwingGraphPanel graphPanel;
    private final JTextField expressionField;
    private final JTextField xMinField;
    private final JTextField xMaxField;
    private final JSpinner samplesSpinner;
    private final JButton plotButton;

    private final CalculatorEngine calculatorEngine;

    public SwingGraphFrame(@NonNull final CalculatorEngine calculatorEngine) {
        super("JustMath - Graph");

        this.calculatorEngine = calculatorEngine;
        this.graphPanel = new SwingGraphPanel();

        this.expressionField = new JTextField("sin(x)+x^2");
        this.xMinField = new JTextField("-10");
        this.xMaxField = new JTextField("10");

        this.samplesSpinner = new JSpinner(new SpinnerNumberModel(800, 50, 20_000, 50));
        this.plotButton = new JButton("Plot");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(createControls(), BorderLayout.NORTH);
        add(graphPanel, BorderLayout.CENTER);

        plotButton.addActionListener(e -> plot());

        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    private JComponent createControls() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        int x = 0;

        c.gridx = x++;
        c.weightx = 0;
        panel.add(new JLabel("f(x) ="), c);

        c.gridx = x++;
        c.weightx = 1;
        panel.add(expressionField, c);

        c.gridx = x++;
        c.weightx = 0;
        panel.add(new JLabel("xMin"), c);

        c.gridx = x++;
        c.weightx = 0.25;
        panel.add(xMinField, c);

        c.gridx = x++;
        c.weightx = 0;
        panel.add(new JLabel("xMax"), c);

        c.gridx = x++;
        c.weightx = 0.25;
        panel.add(xMaxField, c);

        c.gridx = x++;
        c.weightx = 0;
        panel.add(new JLabel("Samples"), c);

        c.gridx = x++;
        c.weightx = 0.2;
        panel.add(samplesSpinner, c);

        c.gridx = x;
        c.weightx = 0;
        panel.add(plotButton, c);

        return panel;
    }

    private void plot() {
        final String expression = expressionField.getText().trim();
        if (expression.isBlank()) {
            return;
        }

        final BigDecimal xMin;
        final BigDecimal xMax;
        try {
            xMin = new BigDecimal(xMinField.getText().trim());
            xMax = new BigDecimal(xMaxField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid xMin/xMax. Use '.' as decimal separator.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int samples = (Integer) samplesSpinner.getValue();

        plotButton.setEnabled(false);

        final SwingWorker<GraphData, Void> worker = new SwingWorker<>() {
            @Override
            protected GraphData doInBackground() {
                final GraphSamplingConfig config = GraphSamplingConfig.builder().build();
                final GraphFunction function = new CalculatorEngineGraphFunction(calculatorEngine, expression);
                return GraphSampler.sample(function, xMin, xMax, samples, config);
            }

            @Override
            protected void done() {
                try {
                    final GraphData data = get();
                    graphPanel.setGraphData(data);
                    graphPanel.fitToData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SwingGraphFrame.this, ex.getMessage(), "Plot Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    plotButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

}
