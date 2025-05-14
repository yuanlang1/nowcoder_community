package com.nowcoder.community.config;

import com.nowcoder.community.quartz.postScoreRefreshJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

/**
 * @author yl
 * @date 2025-04-28 22:42
 */
@Configuration
public class QuartzConfig {

    // 任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(postScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);

        return factoryBean;
    }

    // 触发器
    @Bean
    public CronTriggerFactoryBean factoryBean(JobDetail postScoreRefreshJobDetail){
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setCronExpression("0 0/5 * * * ?");
        factoryBean.setJobDataMap(new JobDataMap());

        return factoryBean;
    }
}
