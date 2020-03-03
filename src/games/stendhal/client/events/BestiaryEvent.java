/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.events;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import games.stendhal.client.GameScreen;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.ScrolledViewport;
import games.stendhal.client.gui.imageviewer.ImageViewWindow;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.gui.imageviewer.ViewPanel;

public class BestiaryEvent extends Event<RPEntity> {

	// logger instance
	private static Logger logger = Logger.getLogger(BestiaryEvent.class);

	@Override
	public void execute() {
		if (event.has("enemies")) {
			// much of this is taken from games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent
			final ViewPanel panel = new ViewPanel() {
				private static final int PAD = 5;

				@Override
				public void prepareView(final Dimension maxSize) {
					Dimension screenSize = GameScreen.get().getSize();
					int maxPreferredWidth = screenSize.width - 180;
					if (event.has("caption")) {
						JLabel caption = new JLabel("<html><div width=" + (maxPreferredWidth
								- 10) + ">" + event.get("caption") + "</div></html>");
						caption.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
						add(caption, BorderLayout.NORTH);
					}

					JTable table = createTable();
					// Prevents selection
					table.setEnabled(false);
					table.setFillsViewportHeight(true);
					table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
					TableColumn col = table.getColumnModel().getColumn(0);
					//col.setCellRenderer(new SpriteCellRenderer());
					col.setCellRenderer(new DefaultTableCellRenderer());

					DefaultTableCellRenderer r = new DefaultTableCellRenderer();
					r.setHorizontalAlignment(SwingConstants.CENTER);

					col = table.getColumnModel().getColumn(1);
					col.setCellRenderer(r);

					col = table.getColumnModel().getColumn(2);
					//col.setCellRenderer(new DescriptionCellRenderer());
					col.setCellRenderer(r);

					HeaderRenderer hr = new HeaderRenderer();
					Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
					while (cols.hasMoreElements()) {
						TableColumn c = cols.nextElement();
						c.setHeaderRenderer(hr);
					}

					adjustColumnWidths(table);
					adjustRowHeights(table);

					ScrolledViewport viewPort = new ScrolledViewport(table);
					/*
					 * maxPreferredWidth is incorrect, but java does not seem to support
					 * max-width property for div's, so all the cells report the same
					 * preferred width anyway.
					 */
					viewPort.getComponent().setPreferredSize(new Dimension(maxPreferredWidth,
							Math.min(screenSize.height - 200, table.getPreferredSize().height
									+ hr.getPreferredSize().height + 4 * PAD)));
					viewPort.getComponent().setBackground(table.getBackground());
					add(viewPort.getComponent(), BorderLayout.CENTER);

					setVisible(true);
				}

				private JTable createTable() {
					String[] columnNames = { "Nazwa stworzenia", "W pojedynkę", "W grupie" };

					final List<String> enemies = Arrays.asList(event.get("enemies").split(";"));
					Collections.sort(enemies, String.CASE_INSENSITIVE_ORDER);

					Object[][] data = new Object[enemies.size()][];
					int i = 0;
					for (final String e: enemies) {
						data[i] = createDataRow(e.split(","));
						i++;
					}
					return new JTable(data, columnNames);
				}

				private Object[] createDataRow(final String[] enemy) {
					Object[] rval = new Object[4];

					final boolean solo = enemy[1].equals("true");
					final boolean shared = enemy[2].equals("true");

					rval[1] = "";
					rval[2] = "";

					if (!solo && !shared) {
						rval[0] = "???";
					} else {
						rval[0] = enemy[0];

						if (solo) {
							rval[1] = "✔";
						}
						if (shared) {
							rval[2] = "✔";
						}
					}

					return rval;
				}

				/**
				 * Adjust the column widths of a table based on the table contents.
				 *
				 * @param table adjusted table
				 */
				private void adjustColumnWidths(JTable table) {
					TableColumnModel model = table.getColumnModel();
					for (int column = 0; column < table.getColumnCount(); column++) {
						TableColumn tc = model.getColumn(column);
						int width = tc.getWidth();
						for (int row = 0; row < table.getRowCount(); row++) {
							Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
							width = Math.max(width, comp.getPreferredSize().width);
						}

						tc.setPreferredWidth(width);
					}
				}

				/**
				 * Adjust the row heights of a table based on the table contents.
				 *
				 * @param table adjusted table
				 */
				private void adjustRowHeights(JTable table) {
					for (int row = 0; row < table.getRowCount(); row++) {
						int rowHeight = table.getRowHeight();

						for (int column = 0; column < table.getColumnCount(); column++) {
							Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
							rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
						}

						table.setRowHeight(row, rowHeight);
					}
				}
			};

			new ImageViewWindow("Bestiariusz", panel);
		} else {
			logger.warn("Could not create bestiary: Event does not have \"enemies\" attribute");
		}
	}
}