FROM fedora:20
MAINTAINER gustavonalle

RUN  (yum -y install unzip supervisor java-1.8.0-openjdk-devel; \ 
     yum -y autoremove; \
     yum clean all;) 

RUN curl "http://mirror.vorboss.net/apache/spark/spark-1.4.0/spark-1.4.0-bin-hadoop2.6.tgz" | tar -C /opt/ -xz | ln -s /opt/spark-1.4.0-bin-hadoop2.6 /opt/spark
ADD java_home.sh /etc/profile.d/java_home.sh
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
ADD start-spark.sh /opt/spark/

ADD infinispan-server-8.0.0.Beta1-bin.zip /
RUN unzip /infinispan-server-8.0.0.Beta1-bin.zip -d /opt/ && rm /infinispan-server-8.0.0.Beta1-bin.zip && ln -s /opt/infinispan-server-8.0.0.Beta1 /opt/infinispan-server
RUN sed -i 's/-Xmx512/-Xmx4096/g' /opt/infinispan-server/bin/standalone.conf
ADD start-infinispan.sh /opt/infinispan-server/bin/
EXPOSE 8080 7077 9080 9081 57600 7600 8181 9990 11211 11222
CMD ["/usr/bin/supervisord","-c","/etc/supervisor/conf.d/supervisord.conf"]
