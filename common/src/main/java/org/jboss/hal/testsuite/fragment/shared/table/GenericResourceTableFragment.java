package org.jboss.hal.testsuite.fragment.shared.table;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.fragment.PagerFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Abstraction over basic UI table
 *
 * @param <T> type of table row, it has to extend {@link ResourceTableRowFragment}
 */
public class GenericResourceTableFragment<T extends ResourceTableRowFragment> extends BaseFragment {

    private static final Logger log = LoggerFactory.getLogger(ResourceTableFragment.class);

    public static final By SELECTOR = By.className(PropUtils.get("resourcetable.class"));
    private static final By SELECTOR_PAGER
            = By.xpath("./following::table[contains(@class, '" + PagerFragment.CLASS_NAME_PAGER + "')]");
    private PagerFragment pager = null;

    /**
     * Class of generic type argument is needed to create fragments of correct class
     */
    private Class<T> type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    /**
     * Return row of this index from entire table
     *
     * @param index zero-based index of row
     * @return row of that index if exists, null otherwise
     */
    public T getRow(int index) {

        T row = null;

        if (this.hasPager()) {
            this.getPager().goToFirstPage();
            do {
                if (this.getPager().getCurrentFromNumber() <= index + 1
                        && index + 1 <= this.getPager().getCurrentToNumber()) {
                    row = this.getVisibleRow(index - this.getPager().getCurrentFromNumber());
                    break;
                }
                log.trace("Trying to move to next page");
            } while (this.getPager().goToNextPage());
        } else {
            row = this.getVisibleRow(index);
        }

        if (row != null) {
            log.debug("Row with index {} found.", index);
        } else {
            log.debug("No row with index {} at this table.", index);
        }
        return row;
    }

    /**
     * Return row of this index from current page of table
     *
     * @param index zero-based index of row
     * @return row of that index if exists, null otherwise
     */
    public T getVisibleRow(int index) {
        List<WebElement> rowElements = getRowElements();
        if (0 <= index && index < rowElements.size()) {
            return Graphene.createPageFragment(type, rowElements.get(index));
        } else {
            return null;
        }
    }

    /**
     * @return last row of entire table, null if no rows in table
     */
    public T getLastRow() {
        if (this.hasPager()) {
            this.getPager().goToLastPage();
        }
        List<WebElement> rowElements = getRowElements();
        if (!rowElements.isEmpty()) {
            WebElement rowRoot = rowElements.get(rowElements.size() - 1);
            return Graphene.createPageFragment(type, rowRoot);
        } else {
            return null;
        }
    }

    /**
     * @return all visible rows of table (listed on current table page)
     */
    public List<T> getVisibleRows() {
        List<WebElement> rowElements = getRowElements();
        List<T> rows = new ArrayList<>(rowElements.size());

        for (WebElement e : rowElements) {
            rows.add(Graphene.createPageFragment(type, e));
        }

        return rows;
    }

    /**
     * @return all rows from table (from all pages)
     */
    public List<T> getAllRows() {

        List<T> rows = new ArrayList<>();

        if (!hasPager()) {
            return this.getVisibleRows();
        } else {
            this.getPager().goToFirstPage();

            do {
                log.trace("Adding all visible rows to all rows list");
                rows.addAll(this.getVisibleRows());
            } while (this.getPager().goToNextPage());

            return rows;
        }
    }

    /**
     * @param col  zero-based column index where to search
     * @param text text to search
     * @return first row that contains given text in given column or null if no such row found
     */
    public T getRowByText(int col, String text) {

        By selector = this.getRowByTextSelector(col, text);
        T row = null;

        if (this.hasPager()) {
            this.getPager().goToFirstPage();
            do {
                List<WebElement> rowsWithText = root.findElements(selector);

                if (rowsWithText.isEmpty()) {
                    log.trace("Row with text <{}> at column {} not found on this page of table.", text, col);
                } else {
                    log.debug("Row with text <{}> at column {} found.", text, col);
                    row = Graphene.createPageFragment(type, rowsWithText.get(0));
                    break;
                }

                log.trace("Trying to move to next page");
            } while (this.getPager().goToNextPage());
        } else {
            List<WebElement> rowsWithText = root.findElements(selector);
            if (!rowsWithText.isEmpty()) {
                row = Graphene.createPageFragment(type, rowsWithText.get(0));
            }
        }

        if (row != null) {
            log.debug("Row with text <{}> at column {} found.", text, col);
        } else {
            log.debug("Row with text <{}> at column {} not found at this table.", text, col);
        }
        return row;
    }

    /**
     * Select first row (find and click on) that contains given text in given column.
     *
     * @param col  zero-based column index where to search
     * @param text text to search
     * @return selected row or null if no such row found
     */
    public T selectRowByText(int col, String text) {
        T row = this.getRowByText(col, text);

        if (row != null) {
            row.click();
            // TODO: replace timeout waiting
            Graphene.waitModel().withTimeout(1500, TimeUnit.MILLISECONDS);
        }
        return row;
    }

    /**
     * @return whether this table has pager associated with it
     */
    public boolean hasPager() {
        return !root.findElements(SELECTOR_PAGER).isEmpty();
    }

    /**
     * @return associated pager or null if no pager exists for this table
     */
    public PagerFragment getPager() {
        if (pager == null && this.hasPager()) {
            log.debug("Creating pager fragment");
            WebElement pagerElement = root.findElement(SELECTOR_PAGER);
            pager = Graphene.createPageFragment(PagerFragment.class, pagerElement);
        }
        return pager;
    }

    private List<WebElement> getRowElements(boolean fail) {
        // TODO: workaround - there is no cellTableRow class, thus odd and even row need to be selected separately
        By selector = new ByJQuery("tr.cellTableEvenRow, tr.cellTableOddRow");
        List<WebElement> rowElements = root.findElements(selector);

        if (rowElements.isEmpty()) {
            log.warn("Table is empty");
        }

        return rowElements;
    }

    private List<WebElement> getRowElements() {
        return getRowElements(true);
    }

    private By getRowByTextSelector(int col, String text) {
        // TODO: workaround - there is no cellTableRow class ...
        // TODO: find a better way than xpath
        // TODO: remove this method after resolving previous issues
        String row = "tr[contains(@class, 'cellTableEvenRow') or " +
                "contains(@class, 'cellTableOddRow')]";
        String cell = "td[contains(@class, 'cellTableCell')]";
        String containsText = "*[contains(text(), '" + text + "')]";
        By selector = By.xpath(".//" + row + "//" + cell + "[" + (col + 1) + "]" +
                "/descendant-or-self::" + containsText + "/ancestor::" + row);

        return selector;
    }

    /**
     *
     * @param col  zero-based column index where to search
     * @return list of retrieved values
     */
    public List<String> getTextInColumn(int col) {
        List<String> values = new ArrayList<>();
        List<T> rows = getAllRows();

        values.addAll(rows.stream().map(row -> row.getCellValue(col)).collect(Collectors.toList()));

        return values;
    }
}
