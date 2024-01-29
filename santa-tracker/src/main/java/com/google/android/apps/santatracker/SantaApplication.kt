/*
 * Copyright 2019. Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.santatracker

import com.google.android.apps.santatracker.di.AppInjector
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import java.util.concurrent.Executor
import javax.inject.Inject

/** The [android.app.Application] for this Santa application.  */
class SantaApplication : SplitCompatApplication(), HasAndroidInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject lateinit var executor: Executor

    override fun onCreate() {
        super.onCreate()

        AppInjector.init(this)

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        SantaNotificationBuilder.createNotificationChannel(this)

        executor.execute {
            if (UpgradeDetector.hasVersionCodeChanged(this)) {
                enqueueRefreshAppIndex(this)
            }
        }
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }
}
