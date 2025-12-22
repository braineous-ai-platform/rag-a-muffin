mvn -q package

docker build -f src/main/docker/Dockerfile.jvm -t braineous/fno .

docker run -it --rm -p 8080:8080 braineous/fno