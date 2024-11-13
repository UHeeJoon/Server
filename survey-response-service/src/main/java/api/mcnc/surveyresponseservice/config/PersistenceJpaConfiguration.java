package api.mcnc.surveyresponseservice.config;

import api.mcnc.surveyresponseservice.entity.EntityModule;
import api.mcnc.surveyresponseservice.repository.RepositoryModule;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * please explain class!
 *
 * @author :Uheejoon
 * @since :2024-11-13 오후 7:57
 */
@Configuration
@EntityScan(basePackageClasses = EntityModule.class)
@EnableJpaRepositories(basePackageClasses = RepositoryModule.class)
public class PersistenceJpaConfiguration {
  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.hikari")
  public DataSource datasource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }

  @Bean
  public TransactionTemplate writeTransactionOperations(PlatformTransactionManager transactionManager) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setReadOnly(false);
    return transactionTemplate;
  }

  @Bean
  public TransactionTemplate readTransactionOperations(PlatformTransactionManager transactionManager) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setReadOnly(true);
    return transactionTemplate;
  }
}
