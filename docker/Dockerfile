FROM fedora:20
MAINTAINER gustavonalle

RUN  (yum -y install unzip supervisor java-1.8.0-openjdk-devel; \ 
     yum -y autoremove; \
     yum clean all;) 

RUN curl "http://mirror.vorboss.net/apache/spark/spark-1.4.0/spark-1.4.0-bin-hadoop2.6.tgz" | tar -C /usr/local/ -xz | ln -s /usr/local/spark-1.4.0-bin-hadoop2.6/ /usr/local/spark
ADD java_home.sh /etc/profile.d/java_home.sh
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
ADD start-spark.sh /usr/local/spark/

ADD infinispan-server-*-bin.zip /
RUN unzip /infinispan-server-*-bin.zip -d /usr/local && rm /infinispan-server-*-bin.zip && ln -s /usr/local/infinispan-server-* /usr/local/infinispan-server
RUN sed -i 's/-Xmx512/-Xmx4096/g' /usr/local/infinispan-server/bin/standalone.conf
ADD start-infinispan.sh /usr/local/infinispan-server/bin/
EXPOSE 8080 7077 9080 9081 57600 7600 8181 9990 11211 11222
CMD ["/usr/bin/supervisord","-c","/etc/supervisor/conf.d/supervisord.conf"]
