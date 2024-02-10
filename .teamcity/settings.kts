import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.11"

project {

    val sequentials = sequential {
        buildType(Maven(name = "Build", goals = "clean compile"))
        parallel {
            buildType(Maven(name = "FastTest", goals = "clean test", runnerArgs = "-Dmaven.test.failure.ignore=true -Dtest=*.unit.*Test"))
            buildType(Maven(name = "SlowTest", goals = "clean test", runnerArgs = "-Dmaven.test.failure.ignore=true -Dtest=*.integration.*Test"))
        }
        buildType(Maven(name = "Package", goals = "clean package", runnerArgs = "-DskipTests"))
    }

    val buildTypes = sequentials.buildTypes()
    buildTypes.forEach { buildType(it) }
    buildTypes.last().triggers {
        vcs {

        }
    }
}

/**
 * Blueprint for Maven build configurations (BuildType)
 *
 * @param name - name of build configuration
 * @param goals - specific commands for maven to execute
 * @param runnerArgs - optional arguments for [goals] command
 */
class Maven(name: String, goals: String, runnerArgs: String? = null) : BuildType({
    this.name = name

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            this.goals = goals
            this.runnerArgs = runnerArgs
        }
    }

    features {
        perfmon {
        }
    }
})