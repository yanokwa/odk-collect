package org.odk.collect.android.projects

import androidx.core.view.children
import androidx.lifecycle.ViewModel
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.activities.AboutActivity
import org.odk.collect.android.activities.viewmodels.CurrentProjectViewModel
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.screens.AdminPreferencesActivity
import org.odk.collect.android.preferences.screens.GeneralPreferencesActivity
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.UUIDGenerator
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ProjectSettingsDialogTest {

    val currentProjectViewModel: CurrentProjectViewModel = mock {
        on { currentProject } doReturn MutableNonNullLiveData(
            Project.Saved(
                "x",
                "Project X",
                "X",
                "#ffffff"
            )
        )
    }

    val projectsRepository = InMemProjectsRepository(UUIDGenerator())

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesCurrentProjectViewModel(currentProjectProvider: CurrentProjectProvider): CurrentProjectViewModel.Factory {
                return object : CurrentProjectViewModel.Factory(currentProjectProvider) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return currentProjectViewModel as T
                    }
                }
            }

            override fun providesProjectsRepository(
                uuidGenerator: UUIDGenerator?,
                gson: Gson?,
                settingsProvider: SettingsProvider?
            ): ProjectsRepository {
                return projectsRepository
            }
        })
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'X' button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(
            ProjectSettingsDialog::class.java,
            R.style.Theme_Collect_Light
        )
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            it.binding.closeIcon.performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(
            ProjectSettingsDialog::class.java,
            R.style.Theme_Collect_Light
        )
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            onView(isRoot()).perform(pressBack())
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `General settings should be started after clicking on the 'General Settings' button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(
            ProjectSettingsDialog::class.java,
            R.style.Theme_Collect_Light
        )
        scenario.onFragment {
            Intents.init()
            assertThat(it.dialog!!.isShowing, `is`(true))
            it.binding.generalSettingsButton.performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
            assertThat(
                Intents.getIntents()[0],
                hasComponent(GeneralPreferencesActivity::class.java.name)
            )
            Intents.release()
        }
    }

    @Test
    fun `Admin settings should be started after clicking on the 'Admin Settings' button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(
            ProjectSettingsDialog::class.java,
            R.style.Theme_Collect_Light
        )
        scenario.onFragment {
            Intents.init()
            assertThat(it.dialog!!.isShowing, `is`(true))
            it.binding.adminSettingsButton.performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
            assertThat(
                Intents.getIntents()[0],
                hasComponent(AdminPreferencesActivity::class.java.name)
            )
            Intents.release()
        }
    }

    @Test
    fun `About section should be started after clicking on the 'About' button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(
            ProjectSettingsDialog::class.java,
            R.style.Theme_Collect_Light
        )
        scenario.onFragment {
            Intents.init()
            assertThat(it.dialog!!.isShowing, `is`(true))
            it.binding.aboutButton.performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
            assertThat(Intents.getIntents()[0], hasComponent(AboutActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun `currentProjectViewModel should be notified when project switched`() {
        val projectY = projectsRepository.save(Project.New("Project Y", "Y", "#ffffff"))

        val scenario = RobolectricHelpers.launchDialogFragment(
            ProjectSettingsDialog::class.java,
            R.style.Theme_Collect_Light
        )
        scenario.onFragment {
            it.binding.projectList.children.iterator().asSequence().first().performClick()
            verify(currentProjectViewModel).setCurrentProject(projectY)
        }
    }
}
