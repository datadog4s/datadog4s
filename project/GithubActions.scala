import sbtghactions.{UseRef, WorkflowStep}

object GithubActions {

  val envGHToken: Map[String, String] = Map[String, String]("GITHUB_TOKEN" -> "${{ secrets.GITHUB_TOKEN }}")
  val javaVersions: Seq[String]       = Seq("adopt@1.8", "adopt@1.11", "adopt@1.13")

  def postPublish: Seq[WorkflowStep] = ReleaseDrafter.releaseDrafter +: Microsite.microsite

  object ReleaseDrafter {
    val releaseDrafter: WorkflowStep =
      WorkflowStep.Use(UseRef.Public("release-drafter", "release-drafter", "v5.6.1"), env = envGHToken)
  }

  object Microsite {
    def microsite: Seq[WorkflowStep] = Ruby.jekyll :+ publishMicrosite

    private val publishMicrosite: WorkflowStep = WorkflowStep.Run(
      List(
        "export PATH=\"$PWD/vendor/bundle/bin:$PATH\"",
        "export GEM_HOME=$PWD/vendor/bundle",
        "eval \"$(ssh-agent -s)\"",
        "ssh-add - <<< \"${DEPLOY_KEY}\"",
        "git config --global user.email \"datadog4s-bot@github.com\"",
        "git config --global user.name \"Release bot\"",
        "sbt site/publishMicrosite"
      ),
      env = Map(
        "DEPLOY_KEY"      -> "${{ secrets.DEPLOY_KEY }}",
        "GIT_SSH_COMMAND" -> "\"ssh -o StrictHostKeyChecking=no\""
      )
    )
  }

  object Ruby {
    def jekyll: Seq[WorkflowStep] = Seq(rubyCache, setupRuby, installJekyll)

    private val rubyCache: WorkflowStep = WorkflowStep.Use(
      UseRef.Public("actions", "cache", "v1"),
      env = Map(
        "path"         -> "vendor/bundle",
        "key"          -> "${{ runner.os }}-gems",
        "restore-keys" -> "${{ runner.os }}-gems"
      )
    )

    private val setupRuby: WorkflowStep     =
      WorkflowStep.Use(UseRef.Public("actions", "setup-ruby", "v1"), env = Map("ruby-version" -> "2.6"))
    private val installJekyll: WorkflowStep = WorkflowStep.Run(
      List(
        "export GEM_HOME=$PWD/vendor/bundle",
        "gem install jekyll -v 4.0.0"
      )
    )

  }

}
