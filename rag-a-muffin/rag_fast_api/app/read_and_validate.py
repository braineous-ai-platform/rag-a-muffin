# read_and_validate.py
import weaviate  # type: ignore
from app import models


class VectorDBService:
    def get_embeddings(self, query: str):
        articles = []
        with weaviate.connect_to_local() as c:
            col = c.collections.get("Article")
            res = col.query.fetch_objects(
                limit=10,
                return_properties=["title", "content", "tags", "published"]
            )

            articles = [models.Article.model_validate(
                o.properties) for o in res.objects]
            # for a in articles:
            #    print(a.title, a.published, a.tags)
        return articles


def main():
    print("#########################################")
    print("____read_1____")

    vector_db_service = VectorDBService()

    print(vector_db_service.get_embeddings("xyz"))


if __name__ == "__main__":
    main()
