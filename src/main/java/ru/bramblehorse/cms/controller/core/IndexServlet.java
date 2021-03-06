package ru.bramblehorse.cms.controller.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import ru.bramblehorse.cms.facade.CatalogFilterFacade;
import ru.bramblehorse.cms.model.commerce.*;
import ru.bramblehorse.cms.model.content.Category;
import ru.bramblehorse.cms.model.content.Content;
import ru.bramblehorse.cms.model.content.LinkContent;
import ru.bramblehorse.cms.model.security.Account;
import ru.bramblehorse.cms.service.AbstractService;
import ru.bramblehorse.cms.service.CategoryService;
import ru.bramblehorse.cms.service.SecurityService;


import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bramblehorse
 * Date: 29.08.13
 * Time: 23:54
 * To change this template use File | Settings | File Templates.
 */
public class IndexServlet extends HttpServlet {

    private Logger logger;
    private WebApplicationContext context;
    private CatalogFilterFacade catalogFilterFacade;
    private CategoryService categoryService;
    private AbstractService<LinkContent> linkContentService;

    private AbstractService<CatalogCategory> catalogCategoryService;
    private AbstractService<CatalogCategoryFilter> catalogCategoryFilterService;
    private AbstractService<FilterCriterion> filterCriterionService;
    private AbstractService<Brand> brandService;

    private SecurityService<Account> accountService;
//    private SecurityService<TomcatRole> tomcatRoleService;
    private Properties settings;

    private static final int DEFAULT_NUMBER_OF_RECORDS = 5;

    @Override
    public void init() throws ServletException {

        context = ContextLoaderListener.getCurrentWebApplicationContext();

        logger = LoggerFactory.getLogger(IndexServlet.class);
        catalogFilterFacade = (CatalogFilterFacade) context.getBean("catalogFilterFacade");
        categoryService = (CategoryService) context.getBean("categoryService");
        linkContentService = (AbstractService<LinkContent>) context.getBean("linkContentService");

        catalogCategoryService = (AbstractService<CatalogCategory>) context.getBean("catalogCategoryService");
        catalogCategoryFilterService = (AbstractService<CatalogCategoryFilter>) context.getBean("catalogCategoryFilterService");
        filterCriterionService = (AbstractService<FilterCriterion>) context.getBean("filterCriterionService");
        brandService = (AbstractService<Brand>) context.getBean("brandService");

        accountService = (SecurityService<Account>) context.getBean("accountService");
//        tomcatRoleService = (SecurityService<TomcatRole>) context.getBean("tomcatRoleService");

        settings = new Properties();
        try {
            settings.load(getServletContext().getResourceAsStream("/WEB-INF/classes/settings.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        processGetIndexPage(req, resp);
        return;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        doGet(req, resp);
    }

    private void processGetIndexPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Boolean isSettingsChanged = (Boolean) getServletContext().getAttribute("isSettingsChanged");
        if (isSettingsChanged != null) {
            if (isSettingsChanged) {
                try {
                    settings.load(getServletContext().getResourceAsStream("/WEB-INF/classes/settings.properties"));
                    getServletContext().setAttribute("isSettingsChanged", false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String categoryId = req.getParameter("category");
        List<Category> categoryList = categoryService.getVisibleRootCategories();
        Collections.sort(categoryList);
        for (Category c : categoryList) {
            c.sortChildren();
        }
        req.setAttribute("categoryList", categoryList);
        if ("true".equalsIgnoreCase(settings.getProperty("show_footer_links"))) {
            List<LinkContent> tempLinkList = linkContentService.getAll();
            List<LinkContent> linkList = new ArrayList<LinkContent>();
            for (LinkContent l : tempLinkList) {
                if (l.getIsVisible())
                    linkList.add(l);
            }
            Collections.sort(linkList);
            req.setAttribute("linkList", linkList);
            String footerLinksSize = settings.getProperty("footer_links_size");
            req.setAttribute("footerLinksSize", footerLinksSize);
        }

        if (categoryList != null) {
            if ((!categoryList.isEmpty()) && (categoryId == null || categoryId.isEmpty())) {
                categoryId = String.valueOf(categoryList.get(0).getId());
            }
        }
        if (categoryId != null && !categoryId.isEmpty()) {
            List<Content> tempList = categoryService.getById(Integer.parseInt(categoryId)).getContent();
            List<Content> contentList = new ArrayList<Content>();
            for (Content c : tempList) {
                if (c.getIsVisible())
                    contentList.add(c);
            }
            Collections.sort(contentList);
            req.setAttribute("contentList", contentList);
        }


        processGetShowCatalog(req, resp);


        RequestDispatcher rd = getServletContext().getRequestDispatcher("/jsp/index.jsp");
        rd.forward(req, resp);
    }

    private void processGetShowCatalog(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String catalogCategoryId = req.getParameter("catalogCategoryId");

        CatalogCategory currentCatalogCategory = null;
        List<CatalogCategory> catalogCategoriesList = catalogCategoryService.getAll();
        Set<Item> itemsSet = new TreeSet<Item>();
        List<CatalogCategoryFilter> filtersList = null;
        Collections.sort(catalogCategoriesList);
        req.setAttribute("catalogCategoriesList", catalogCategoriesList);
        req.setAttribute("catalogCategoryId", catalogCategoryId);
        if (catalogCategoryId != null) {

            try {
                currentCatalogCategory = catalogCategoryService.getById(Integer.parseInt(catalogCategoryId));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            filtersList = currentCatalogCategory.getCatalogCategoryFilters();
            Integer offset = null;
            Integer numberOfRecords = null;
            try {
                offset = Integer.parseInt(req.getParameter("offset"));
            } catch (Exception e) {
                offset = 1;
                logger.error(e.getMessage());
            }
            try {
                numberOfRecords = Integer.parseInt(req.getParameter("numberOfRecords"));
                if(numberOfRecords < 1) numberOfRecords = DEFAULT_NUMBER_OF_RECORDS;
            } catch (Exception e) {
                numberOfRecords = DEFAULT_NUMBER_OF_RECORDS;
                logger.error(e.getMessage());
            }
            List<Item> totalItems = catalogFilterFacade.processItemsList(req, resp, currentCatalogCategory);
            Integer pageCount = totalItems.size()/numberOfRecords + 1;
            List<Integer> catalogPagesList = new ArrayList<Integer>();
            for(int i = 1; i <= pageCount; i++){
                catalogPagesList.add(i);
            }
            if(offset > 0) offset -= 1;
            for(int i = offset * numberOfRecords, j = 0; i < totalItems.size() && j < numberOfRecords; j++){
                itemsSet.add(totalItems.get(i++));
            }
            req.setAttribute("currentCatalogPage", offset);
            if(pageCount > 1){
                req.setAttribute("catalogPagesList", catalogPagesList);
            }
            req.setAttribute("itemsSet", itemsSet);
            req.setAttribute("filtersList", filtersList);
            req.setAttribute("contentValue", "catalog");
        } else {

            req.setAttribute("contentValue", "content");
        }
    }
}
