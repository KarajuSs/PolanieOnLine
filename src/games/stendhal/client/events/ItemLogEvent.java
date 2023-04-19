/***************************************************************************
 *                 (C) Copyright 2022-2023 - PolanieOnLine                 *
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import games.stendhal.client.GameScreen;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.imageviewer.ImageViewWindow;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.gui.imageviewer.ViewPanel;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.grammar.Grammar;

public class ItemLogEvent extends Event<RPEntity> {
	// logger instance
	private static Logger logger = Logger.getLogger(ItemLogEvent.class);

	ArrayList<String> itemClassList = new ArrayList<String>();

	@Override
	public void execute() {
		if (!event.has("dropped_items")) {
			logger.warn("Could not create item log: Event does not have \"dropped_items\" attribute");
			return;
		}

		// much of this is taken from games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent
		final ViewPanel panel = new ViewPanel() {
			private static final int PAD = 5;
			@Override
			public void prepareView(Dimension maxSize) {
				Dimension screenSize = GameScreen.get().getSize();
				int maxPreferredWidth = screenSize.width - 180;

				JTabbedPane tabbedPane = new JTabbedPane();
				addItemClassToList();

				String[] categories = getItemsClassList();
				for (String category : categories) {
					List<String> itemsList = getItemsListForCategory(category);
					JTable table = createTableForCategory(itemsList);

					// Dodanie tabeli do zakładki
					JScrollPane scrollPane = new JScrollPane(table);
					tabbedPane.addTab(category, scrollPane);
				}

				// Ustawianie preferowanej wielkości dla zakładek
				tabbedPane.setPreferredSize(new Dimension(maxPreferredWidth,
					Math.min(screenSize.height - 200, tabbedPane.getPreferredSize().height + 4 * PAD)));

				add(tabbedPane, BorderLayout.CENTER);
				setVisible(true);
			}
			
			private List<String> getItemsListForCategory(String category) {
				List<String> itemsList = new ArrayList<>();
				for (String item : getItemsList()) {
					String[] itemAttrs = item.split(",");
					if (category.equals(getTranslatedClass(itemAttrs[3]))) {
						itemsList.add(item);
					}
				}
				return itemsList;
			}

			private JTable createTableForCategory(List<String> itemsList) {
				final String[] columnNames = {"#", "Nazwa przedmiotu", "Ile sztuk", "Zdobyto"};
				final Object[][] data = new Object[itemsList.size()][];

				int i = 0;
				for (final String item : itemsList) {
					String[] itemAttrs = item.split(",");
					data[i] = createDataRow(itemAttrs);
					i++;
				}

				JTable table = new JTable(data, columnNames);

				// Ustawianie właściwości tabeli
				table.setEnabled(false);
				table.setFillsViewportHeight(true);
				table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

				// Renderer komórek
				TableColumn col = table.getColumnModel().getColumn(1);
				col.setCellRenderer(new DefaultTableCellRenderer());
				DefaultTableCellRenderer r = new DefaultTableCellRenderer();
				r.setHorizontalAlignment(SwingConstants.CENTER);
				col = table.getColumnModel().getColumn(0);
				col.setCellRenderer(new SpriteCellRenderer());
				col = table.getColumnModel().getColumn(2);
				col.setCellRenderer(r);
				col = table.getColumnModel().getColumn(3);
				col.setCellRenderer(r);

				// Ustawianie nagłówków kolumn
				HeaderRenderer hr = new HeaderRenderer();
				Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
				while (cols.hasMoreElements()) {
					TableColumn c = cols.nextElement();
					c.setHeaderRenderer(hr);
				}

				// Dostosowanie szerokości kolumn i wysokości wierszy
				adjustColumnWidths(table);
				adjustRowHeights(table);

				return table;
			}

			private List<String> getItemsList() {
				return Arrays.asList(event.get("dropped_items").split(";"));
			}

			private final Map<String, String> ITEM_CLASS_MAP = new HashMap<String, String>() {{
				put("belts", "pasy");
				put("ring", "pierścionki");
				put("ranged", "zasięgowe");
				put("necklace", "naszyjniki");
				put("missile", "pociski");
				put("magia", "magia");
				put("money", "monety");
				put("misc", "różne");
				put("wand", "różdżki");
				put("resource", "zasoby");
				put("boots", "buty");
				put("club", "młoty");
				put("sword", "miecze");
				put("cloak", "płaszcze");
				put("armor", "zbroje");
				put("shield", "tarcze");
				put("glove", "rękawice");
				put("legs", "spodnie");
				put("helmet", "hełmy");
				put("jewellery", "klejnoty");
				put("drink", "napoje");
				put("dagger", "sztylety");
				put("herb", "zioła");
				put("scroll", "zwoje");
				put("food", "jedzenie");
				put("axe", "topory");
				put("ammunition", "amunicja");
			}};
			
			private String getTranslatedClass(String clazz) {
				if (ITEM_CLASS_MAP.containsKey(clazz)) {
					clazz = ITEM_CLASS_MAP.get(clazz);
				}
				return Grammar.capitalize(clazz);
			}

			private void addItemClassToList() {
				Set<String> itemClassSet = new HashSet<>();
				for (int i = 0; i < getItemsList().size(); i++) {
					String[] itemAttrs = getItemsList().get(i).split(",");
					itemClassSet.add(getTranslatedClass(itemAttrs[3]));
				}
				ArrayList<String> sortedItemClassList = new ArrayList<>(itemClassSet);
				Collections.sort(sortedItemClassList);
				itemClassList = sortedItemClassList;
			}

			private String[] getItemsClassList() {
				if (itemClassList.isEmpty()) {
					return new String[0];
				}
				return itemClassList.toArray(new String[0]);
			}

			private Object[] createDataRow(final String[] item) {
				final Object[] rval = new Object[4];

				final String name = item[0];
				final String itemDropCount = item[2];
				final String clazz = item[3];
				final String subclazz = item[4];

				boolean dropped = false;
				if (item[1].equals("true")) {
					dropped = true;
				}

				rval[0] = getItemImage(clazz, subclazz, dropped);
				rval[1] = name;
				rval[2] = "";
				rval[3] = "";

				if (dropped) {
					rval[2] = itemDropCount;
					rval[3] = "✔";
				}

				return rval;
			}

			private Sprite getItemImage(String clazz, String subclazz, boolean dropped) {
				String imagePath = "/data/sprites/items/" + clazz + "/" + subclazz + ".png";

				Sprite sprite;
				sprite = SpriteStore.get().getColoredSprite("/data/gui/bag.png", Color.LIGHT_GRAY);
				if (dropped) {
					sprite = SpriteStore.get().getSprite(imagePath);
				}

				if (sprite.getWidth() > sprite.getHeight()) {
					sprite = SpriteStore.get().getAnimatedSprite(sprite, 100);
				}

				return sprite;
			}

			/**
			 * Adjust the column widths of a table based on the table contents.
			 *
			 * @param table adjusted table
			 */
			private void adjustColumnWidths(JTable table) {
				final TableColumnModel model = table.getColumnModel();
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

			@SuppressWarnings("serial")
			class SpriteCellRenderer extends JComponent implements TableCellRenderer {
				private Sprite sprite;

				@Override
				public Component getTableCellRendererComponent(JTable table, Object color,
						boolean isSelected, boolean hasFocus, int row, int col) {
					Object obj = table.getValueAt(row, col);
					if (obj instanceof Sprite) {
						sprite = (Sprite) obj;
					} else {
						sprite = null;
					}
					return this;
				}

				@Override
				public Dimension getPreferredSize() {
					Dimension d = new Dimension();
					if (sprite != null) {
						d.width = sprite.getWidth() + 2 * PAD;
						d.height = sprite.getHeight() + 2 * PAD;
					}
					return d;
				}

				@Override
				protected void paintComponent(Graphics g) {
					if (sprite != null) {
						sprite.draw(g, (getWidth() - sprite.getWidth()) / 2, (getHeight() - sprite.getHeight()) / 2);
					}
				}
			}
		};

		new ImageViewWindow("Spis Przedmiotów", panel);
	}
}
