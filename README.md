# Product mapping :
Multi-fields for Search & Analytics: Using text for search and keyword sub-fields (like in title and brand) is best practice. It allows you to perform full-text searches while still being able to run aggregations (facets) and sorting.

Nested Objects: You correctly used type: nested for attributes, variants, and store_locations. Standard objects flatten arrays, which would lose the relationship between an attribute name and its specific value.

Rank Feature: Implementing search_boost_score as a rank_feature is much more performant than using script-based boosting during query time.

Path Hierarchy: Using path_hierarchy_analyzer for category_path is the gold standard for "Breadcrumb" style navigation (e.g., searching for "Electronics" and finding "Electronics > Audio").

1. The "Nested" Performance Tax

You have several nested fields (attributes, variants, collection_points, store_locations).

The Risk: Every nested object is stored as a separate hidden document. If a product has 50 variants and 20 attributes, one "product" is actually 71 documents in the index.

Recommendation: Keep an eye on your Mapping Explosion. If you expect thousands of variants per product, you might see slow queries. Ensure you set a reasonable index.mapping.nested_objects.limit.

2. Scaled Float vs. Double

You used scaled_float with a scaling_factor of 100 for prices.

Why this is good: It saves disk space and is faster for range queries because it stores the data as a long (e.g., $19.99 becomes $1999).

Note: Just ensure your application logic always handles the conversion correctly when indexing.

3. Completion Suggester Constraints

Your suggest field uses the completion type.

The Limitation: This is an in-memory data structure. While very fast, it is strictly prefix-based. If a user types "phone" but your title is "iPhone," the completion suggester won't find it.

Alternative: Since you already have an autocomplete sub-field with an analyzer on title, you might find that a standard match_phrase_prefix query on that field is more flexible than the suggest field.

4. enabled: false for Objects

You used "enabled": false for ratings.distribution and opening_hours.

The Benefit: This is an excellent way to store "blobs" of data that you want to retrieve in search results but never search on. It keeps the index lean.

💡 Pro-Tip: The "In-Stock" Filter
In your suggest field, you have a context for in_stock. Since inventory changes frequently, updating the entire document just to toggle a stock status for a suggester can be heavy on I/O. If you have high-velocity inventory changes, consider using Doc Values for filtering rather than building it into the completion context.

If are using a rollover alias (products) and an ILM policy (products_policy), the best practice is to create the index with a date-math or numeric pattern (like products-000001) and define the alias immediately.

First create ilm policy:
PUT _ilm/policy/products_policy
{}

Then create index:
PUT products-000001
{
"settings":settings,
mappings:mappings
aliases:aliases
}