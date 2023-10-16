/*
 * Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.mapsmessaging.hardware.trigger;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class CronTrigger extends Trigger {

  private final Scheduler scheduler;
  private final String cronExpression;
  private final JobDetail job;

  public CronTrigger(String cronExpression) throws SchedulerException {
    super();
    this.scheduler = StdSchedulerFactory.getDefaultScheduler();
    this.cronExpression = cronExpression;
    scheduler.start();
    job = JobBuilder.newJob(CronJob.class)
        .withIdentity("cronJob", "group1")
        .build();
    job.getJobDataMap().put("trigger", this);
  }

  @Override
  public void start() {
    try {
      org.quartz.Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity("cronTrigger", "group1")
          .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
          .build();

      scheduler.scheduleJob(job, trigger);
    } catch (SchedulerException e) {
      // To Do
    }
  }

  @Override
  public void stop() {
    try {
      scheduler.shutdown();
    } catch (SchedulerException e) {
      // To Do
    }
  }
}
