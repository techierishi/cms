<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<script type="text/javascript" src="${pageContext.request.contextPath}/jsp/js/validation/category.content.validation.js"></script>

    <div class="edit-category">
        <form id="admin-content-category-form" action="javascript:void(0);" method="post" onsubmit="ajax()">
            <input type="hidden" name="action" value="edit">
            <table class="admin-input">
                <tr>
                    <input type="hidden" name="categoryId" value="${currentCategory.id}"/>
                    <td>Название категории:</td>
                    <td><input type="text" name="title" class="text-input" value="${currentCategory.name}"></td>
                    <td>Позиция:</td>
                    <td><input type="text" name="categoryPosition" class="number-input" value="${currentCategory.categoryPosition}"></td>
                </tr>
                <c:set var="categoryLength" value="${fn:length(categoryList)}"/>
                <c:choose>
                    <c:when test="${categoryLength > 0}">
                        <tr>
                            <td>Укажите родительскую категорию: </td>
                            <td></td>
                        </tr>
                        <tr>
                            <td><input type="radio" name="parentCategory" <c:if test="${empty parentCategoryId}"> checked="checked" </c:if> value="null"></td>
                            <td>Нет (категория верхнего уровня)</td>
                        </tr>
                        <c:forEach items="${categoryList}" var="category">
                            <tr>
                                <td><input type="radio"  name="parentCategory" <c:if test="${parentCategoryId eq category.id}"> checked="checked" </c:if>  value="${category.id}"></td>
                                <td>${category.name}</td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>

                    </c:otherwise>
                </c:choose>
                <tr>
                    <td>Отображать</td>
                    <td><input type="checkbox" name="isVisible" <c:if test="${currentCategory.isVisible}">checked="true"</c:if> value="visible"></td>
                </tr>
                <tr>
                    <td></td>
                    <td><input type="submit" value="Сохранить"></td>
                </tr>
            </table>
        </form>
    </div>
