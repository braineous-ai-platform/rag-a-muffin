
# read_and_validate.py
import weaviate  # type: ignore
from models import Article


def main():
    print("#########################################")
    print("____read____")

    with weaviate.connect_to_local() as c:
        col = c.collections.get("Article")
        res = col.query.fetch_objects(
            limit=10,
            return_properties=["title", "content", "tags", "published"]
        )

        articles = [Article.model_validate(o.properties) for o in res.objects]
        for a in articles:
            print(a.title, a.published, a.tags)


if __name__ == "__main__":
    main()
