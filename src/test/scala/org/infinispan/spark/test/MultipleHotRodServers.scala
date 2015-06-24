package org.infinispan.spark.test

import org.infinispan.client.hotrod.test.HotRodClientTestingUtil._
import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.lifecycle.ComponentStatus
import org.infinispan.server.hotrod.HotRodServer
import org.infinispan.test.TestingUtil._
import org.infinispan.test.fwk.{TestCacheManagerFactory, TransportFlags}
import org.scalatest.{BeforeAndAfterAll, Suite}


trait MultipleHotRodServers extends BeforeAndAfterAll with RemoteTest {
   this: Suite =>

   protected def numServers: Int

   protected def getConfigurationBuilder: ConfigurationBuilder

   protected val servers: List[HotRodServer]

   override protected def beforeAll(): Unit = {
      val servers = (0 to numServers).map { _ =>
         val clusteredCacheManager = TestCacheManagerFactory.createClusteredCacheManager(getConfigurationBuilder, new TransportFlags)
         startHotRodServer(clusteredCacheManager)
      }
//      blockUntilViewReceived(servers.head.getCacheManager.getCache, numServers)
//      servers.foreach(s => blockUntilCacheStatusAchieved(s.getCacheManager.getCache, ComponentStatus.RUNNING, 1000L))
      super.beforeAll()
   }


   override protected def afterAll(): Unit = {

      super.afterAll()
   }


   /**
    *
    *
   protected HotRodServer addHotRodServer(ConfigurationBuilder builder) {
      EmbeddedCacheManager cm = this.addClusterEnabledCacheManager(builder);
      HotRodServer server = HotRodClientTestingUtil.startHotRodServer(cm);
      this.servers.add(server);
      return server;
   }

    *
    *
    * protected List<HotRodServer> servers = new ArrayList();
   protected List<RemoteCacheManager> clients = new ArrayList();


   protected void createHotRodServers(int num, ConfigurationBuilder defaultBuilder) {
      int i;
      for(i = 0; i < num; ++i) {
         this.addHotRodServer(defaultBuilder);
      }

      for(i = 0; i < num; ++i) {
         assert this.manager(i).getCache() != null;
      }

      TestingUtil.blockUntilViewReceived(this.manager(0).getCache(), num);

      for(i = 0; i < num; ++i) {
         TestingUtil.blockUntilCacheStatusAchieved(this.manager(i).getCache(), ComponentStatus.RUNNING, 10000L);
      }

      for(i = 0; i < num; ++i) {
         this.clients.add(this.createClient(i));
      }

   }

   protected RemoteCacheManager createClient(int i) {
      return new InternalRemoteCacheManager(this.createHotRodClientConfigurationBuilder(this.server(i).getPort()).build());
   }

   protected org.infinispan.client.hotrod.configuration.ConfigurationBuilder createHotRodClientConfigurationBuilder(int serverPort) {
      org.infinispan.client.hotrod.configuration.ConfigurationBuilder clientBuilder = new org.infinispan.client.hotrod.configuration.ConfigurationBuilder();
      clientBuilder.addServer().host("localhost").port(serverPort).maxRetries(this.maxRetries()).pingOnStartup(false);
      return clientBuilder;
   }

   protected int maxRetries() {
      return 0;
   }

   @AfterMethod(
      alwaysRun = true
   )
   protected void clearContent() throws Throwable {
   }

   @AfterClass(
      alwaysRun = true
   )
   protected void destroy() {
      try {
         Iterator var1 = this.servers.iterator();

         while(var1.hasNext()) {
            HotRodServer server = (HotRodServer)var1.next();
            HotRodClientTestingUtil.killServers(new HotRodServer[]{server});
         }
      } finally {
         super.destroy();
      }

   }

   protected HotRodServer addHotRodServer(ConfigurationBuilder builder) {
      EmbeddedCacheManager cm = this.addClusterEnabledCacheManager(builder);
      HotRodServer server = HotRodClientTestingUtil.startHotRodServer(cm);
      this.servers.add(server);
      return server;
   }

   protected HotRodServer addHotRodServer(ConfigurationBuilder builder, int port) {
      EmbeddedCacheManager cm = this.addClusterEnabledCacheManager(builder);
      HotRodServer server = HotRodTestingUtil.startHotRodServer(cm, port, new HotRodServerConfigurationBuilder());
      this.servers.add(server);
      return server;
   }

   protected HotRodServer server(int i) {
      return (HotRodServer)this.servers.get(i);
   }

   protected void killServer(int i) {
      HotRodServer server = (HotRodServer)this.servers.get(i);
      HotRodClientTestingUtil.killServers(new HotRodServer[]{server});
      this.servers.remove(i);
      TestingUtil.killCacheManagers(new EmbeddedCacheManager[]{(EmbeddedCacheManager)this.cacheManagers.get(i)});
      this.cacheManagers.remove(i);
   }

   protected RemoteCacheManager client(int i) {
      return (RemoteCacheManager)this.clients.get(i);
   }

   protected void defineInAll(String cacheName, ConfigurationBuilder builder) {
      Iterator var3 = this.servers.iterator();

      while(var3.hasNext()) {
         HotRodServer server = (HotRodServer)var3.next();
         server.getCacheManager().defineConfiguration(cacheName, builder.build());
      }

   }
    */

}
