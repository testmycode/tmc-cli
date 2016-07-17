FROM maven:3-jdk-8

RUN apt-get update && apt-get install -y --no-install-recommends build-essential  && rm -rf /var/lib/apt/lists/*

RUN useradd -g users user && mkdir -p /home/user && chown -R user:users /home/user
WORKDIR /app

RUN chown -R user:users /app
USER user

CMD ["/bin/bash"]
