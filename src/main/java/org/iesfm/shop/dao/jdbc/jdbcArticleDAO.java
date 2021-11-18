package org.iesfm.shop.dao.jdbc;

import org.iesfm.shop.Article;
import org.iesfm.shop.dao.ArticleDAO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;

public class jdbcArticleDAO implements ArticleDAO {
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Article> ARTICLE_ROW_MAPPER = (rs, rowNum) ->
            new Article(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    getTags(rs.getInt("id"))
            );

    private final static String SELECT_ARTICLES = "SELECT * FROM Article";

    private final static String SELECT_ARTICLE_TAGS = "SELECT name FROM Tag " +
            "WHERE article_id = :articleId";

    private final static String SELECT_ARTICLE = "SELECT * FROM Article " +
            "WHERE id = :id";

    private final static String SELECT_TAG_ARTICLES_ID = "SELECT article_id FROM Tag " +
            "WHERE name = :tag";

    private final static String SELECT_TAG_ARTICLES = "SELECT * FROM Article " +
            "WHERE id = :id";

    private final static String INSERT_ARTICLE = "INSERT INTO Article (id, name, price) " +
            "VALUES (:id, :name, :price)";

    private final static String INSERT_TAGS = "INSERT INTO Tag (article_id, name) " +
            "VALUES (:articleId, :tag)";

    private final static String UPDATE_ARTICLE = "UPDATE Article " +
            "SET name = :name, price = :price " +
            "WHERE id = :id";

    private final static String DELETE_TAGS = "DELETE FROM Tag " +
            "WHERE articleId = :articleId";

    private final static String DELETE_ARTICLE = "DELETE FROM Article " +
            "WHERE id = :id";

    public jdbcArticleDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Article> list() {
        return jdbcTemplate.query(
                SELECT_ARTICLES,
                ARTICLE_ROW_MAPPER
        );
    }

    private Set<String> getTags(int articleId) {
        Map<String, Object> params = new HashMap<>();
        params.put("articleId", articleId);
        return new HashSet<String>(jdbcTemplate.query(
                SELECT_ARTICLE_TAGS,
                params,
                (rs, rowNum) ->
                        rs.getString("name")
        ));
    }

    private List<Integer> getTagArticles(String tag) {
        Map<String, Object> params = new HashMap<>();
        params.put("tag", tag);
        return jdbcTemplate.query(
                SELECT_TAG_ARTICLES_ID,
                params,
                (rs, rownum) ->
                        rs.getInt("article_id")
        );
    }

    @Override
    public List<Article> list(String tag) {
        Map<String, Object> params = new HashMap<>();
        List<Article> articles = new LinkedList<>();
        for (int id : getTagArticles(tag)) {
            params.put("id", id);
            jdbcTemplate.query(
                    SELECT_TAG_ARTICLES,
                    params,
                    ARTICLE_ROW_MAPPER
            );
        }
        return articles;
    }

    @Override
    public Article get(int id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        try {
            return jdbcTemplate.queryForObject(
                    SELECT_ARTICLE,
                    params,
                    ARTICLE_ROW_MAPPER
            );
        } catch (Exception e) {
            return null;
        }
    }

    private void insertTags(int articleId, Set<String> tags) {
        for (String tag : tags) {
            Map<String, Object> params = new HashMap<>();
            params.put("articleId", articleId);
            params.put("tag", tag);
            jdbcTemplate.update(INSERT_TAGS, params);
        }
    }

    @Override
    public boolean insert(Article article) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", article.getId());
        params.put("name", article.getName());
        params.put("price", article.getPrice());
        try {
            jdbcTemplate.update(INSERT_ARTICLE, params);
            insertTags(article.getId(), article.getTags());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateTags(int articleId, Set<String> tags) {
            Map<String, Object> params = new HashMap<>();
            params.put("articleId", articleId);

            jdbcTemplate.update(DELETE_TAGS, params);
            insertTags(articleId, tags);
    }

    @Override
    public boolean update(Article article) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", article.getId());
        params.put("name", article.getName());
        params.put("price", article.getPrice());

        updateTags(article.getId(), article.getTags());
        return jdbcTemplate.update(UPDATE_ARTICLE, params) == 1;
    }

    @Override
    public boolean delete(int id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        return jdbcTemplate.update(DELETE_ARTICLE, params) == 1;
    }
}
