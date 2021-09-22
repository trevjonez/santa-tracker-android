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

package com.google.android.apps.santatracker.tracker.viewmodel

import androidx.lifecycle.Observer
import androidx.test.InstrumentationRegistry
import androidx.test.filters.MediumTest
import com.google.android.apps.santatracker.config.Config
import com.google.android.apps.santatracker.tracker.db.LiveDataTestUtil
import com.google.android.apps.santatracker.tracker.repository.SantaDataRepository
import com.google.android.apps.santatracker.tracker.time.Clock
import com.google.android.apps.santatracker.tracker.util.JsonLoader
import com.google.android.apps.santatracker.tracker.vo.Destination
import com.google.android.apps.santatracker.tracker.vo.StreamEntry
import com.google.android.apps.santatracker.tracker.vo.TrackerCard
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.Executor
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Unit tests for [TrackerViewModel]
 */
@RunWith(MockitoJUnitRunner::class)
class TrackerViewModelTest {

    companion object {

        private const val NORTH_POLE_DEPARTURE = 1514109600000L
        private const val SYDNEY_ARRIVAL = 1514120880000L
        private const val SYDNEY_DEPARTURE = 1514120940000L
        private const val NORTH_POLE_LANDING = 1545645600000L
        private const val LAST_ARRIVAL = 1514199600000L

        // The indices of the cities within the destination list in the santa.json file
        private const val NORTH_POLE_INDEX = 0
        private const val PROVIDENIYA_INDEX = 1
        private const val BRISBANE_INDEX = 35
        private const val SYDNEY_INDEX = 36
        private const val WOLLONGONG_INDEX = 37
    }

    private lateinit var viewModel: TrackerViewModel
    @Mock private lateinit var mockRepository: SantaDataRepository
    @Mock private lateinit var mockClock: Clock
    @Mock private lateinit var mockConfig: Config
    @Mock private lateinit var mockFirebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var executorService: ScheduledExecutorService
    private lateinit var destinationList: List<Destination>
    private val streamEntryList = listOf<StreamEntry>()

    @Before
    fun setUp() {
        executorService = ScheduledThreadPoolExecutor(1)

        val jsonLoader = JsonLoader()
        val jsonData = jsonLoader.parseJson(InstrumentationRegistry.getTargetContext())
        val destinations = jsonData.destinations
        val streamEntries = jsonData.streamEntries

        destinationList = destinations
        doReturn(destinations).`when`(mockRepository).loadDestinations()
        doReturn(streamEntries).`when`(mockRepository).loadStreamEntries()
        doReturn(mockFirebaseRemoteConfig).`when`(mockConfig).firebaseRemoteConfig
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @Test
    @MediumTest
    fun testTrackerCards() {
        `when`(mockClock.nowMillis()).thenReturn(2)
        @Suppress("UNCHECKED_CAST")
        val observer = mock(Observer::class.java) as Observer<List<TrackerCard>>
        viewModel.stream.observeForever(observer)
        viewModel.initializeStream(emptyList())
        verify(observer, timeout(100)).onChanged(eq(emptyList()))
        viewModel.updateStream(listOf(StreamEntry(1, StreamEntry.TYPE_DID_YOU_KNOW, false, "a")), 2)
        verify(observer, timeout(100))
                .onChanged(eq(listOf(StreamEntry(1, StreamEntry.TYPE_DID_YOU_KNOW, false, "a"))))
    }
}
