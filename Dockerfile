# NOTE: Consider not using uberjar and adding deps to their own layers:
# https://github.com/metosin/packaging-clojure-examples/tree/master/deps-docker

# NOTE: Consider building your own distroless base image and with minimal java (jlink)?

# No need for any clojure deps since the uberjar has already been built
FROM openjdk:17
WORKDIR /

# Note the very tight .dockerignore which only exposes target/app.jar to the build context.
COPY target/app.jar /

CMD java -cp app.jar clojure.main -m backend.main
