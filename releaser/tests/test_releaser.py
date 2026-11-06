#!/usr/bin/env python3
"""Unit tests for the `releaser` package."""

from pathlib import Path
import sys
import tempfile
import unittest

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from releaser.core import AndroidReleaser, MarkdownVersionTable, VersionRow


class ReleaserTest(unittest.TestCase):
    def test_markdown_table_roundtrip(self) -> None:
        rows = [
            VersionRow(
                project_dir="chordwheel",
                app_id="com.eggwall.chordwheel",
                display_name="ChordWheel",
                version_name="1.4",
                version_code=4,
                tag_pattern="{app}-v{version}",
            ),
            VersionRow(
                project_dir="nested/my-app",
                app_id="com.example.myapp",
                display_name="My App",
                version_name="2.1.0",
                version_code=12,
                tag_pattern="v{version}",
            ),
        ]
        md = MarkdownVersionTable.dumps(rows, include_header=True)
        parsed = MarkdownVersionTable.loads(md)
        self.assertEqual(parsed, rows)
        self.assertEqual(parsed[0].render_tag(), "chordwheel-v1.4")
        self.assertEqual(parsed[1].render_tag(), "v2.1.0")

    def test_markdown_table_reordered_columns(self) -> None:
        reordered_md = (
            "| Tag Pattern      | Version Code | Version Name | Display Name       | App ID                 | Project Dir |\n"
            "| ---------------- | ------------ | ------------ | ------------------ | ---------------------- | ----------- |\n"
            "| {app}-v{version} |            4 |          1.4 | ChordWheel         | com.eggwall.chordwheel | chordwheel  |\n"
            "| v{version}       |           12 |        2.1.0 | Fretboard Explorer | com.eggwall.fretboard  | fretboard   |\n"
        )
        parsed = MarkdownVersionTable.loads(reordered_md)
        self.assertEqual(len(parsed), 2)
        self.assertEqual(parsed[0].project_dir, "chordwheel")
        self.assertEqual(parsed[0].version_name, "1.4")
        self.assertEqual(parsed[0].version_code, 4)
        self.assertEqual(parsed[0].app_id, "com.eggwall.chordwheel")
        self.assertEqual(parsed[0].display_name, "ChordWheel")
        self.assertEqual(parsed[0].tag_pattern, "{app}-v{version}")
        self.assertEqual(parsed[1].project_dir, "fretboard")
        self.assertEqual(parsed[1].version_name, "2.1.0")
        self.assertEqual(parsed[1].version_code, 12)
        self.assertEqual(parsed[1].render_tag(), "v2.1.0")

    def test_recursive_discovery_flat_and_multimodule_and_update(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)

            # 1. Flat single-module Groovy project
            flat_dir = root / "alpha"
            flat_dir.mkdir(parents=True)
            (flat_dir / "gradlew").write_text("#!/bin/sh\n", encoding="utf-8")
            (flat_dir / "build.gradle").write_text(
                "plugins {\n"
                "    id 'com.android.application' version '8.13.2'\n"
                "}\n"
                "android {\n"
                "    namespace = 'com.example.alpha'\n"
                "    defaultConfig {\n"
                "        applicationId = 'com.example.alpha'\n"
                "        versionCode = 3\n"
                "        versionName = '1.3'\n"
                "    }\n"
                "}\n",
                encoding="utf-8",
            )
            (flat_dir / "AndroidManifest.xml").write_text(
                '<?xml version="1.0" encoding="utf-8"?>\n'
                '<manifest xmlns:android="http://schemas.android.com/apk/res/android"\n'
                '          android:versionCode="3"\n'
                '          android:versionName="1.3">\n'
                "</manifest>\n",
                encoding="utf-8",
            )
            res_values = flat_dir / "res" / "values"
            res_values.mkdir(parents=True)
            (res_values / "strings.xml").write_text(
                '<resources><string name="app_name">Alpha Synth</string></resources>\n',
                encoding="utf-8",
            )

            # 2. Nested multi-module Kotlin DSL project (`suite/beta/app/build.gradle.kts`)
            beta_proj = root / "suite" / "beta"
            beta_app = beta_proj / "app"
            beta_app.mkdir(parents=True)
            (beta_proj / "gradlew").write_text("#!/bin/sh\n", encoding="utf-8")
            (beta_proj / "settings.gradle.kts").write_text(
                'rootProject.name = "BetaApp"\ninclude(":app")\n',
                encoding="utf-8",
            )
            (beta_app / "build.gradle.kts").write_text(
                'plugins {\n'
                '    id("com.android.application")\n'
                '}\n'
                'android {\n'
                '    namespace = "com.example.beta"\n'
                '    defaultConfig {\n'
                '        applicationId = "com.example.beta"\n'
                '        versionCode = 7\n'
                '        versionName = "2.0.1"\n'
                '    }\n'
                '}\n',
                encoding="utf-8",
            )

            releaser = AndroidReleaser(root_dir=root)
            projects = releaser.discover_projects()
            self.assertEqual(len(projects), 2)

            p_alpha, p_beta = projects[0], projects[1]
            self.assertEqual(p_alpha.rel_project_dir, "alpha")
            self.assertEqual(p_alpha.app_id, "com.example.alpha")
            self.assertEqual(p_alpha.display_name, "Alpha Synth")
            self.assertEqual(p_alpha.version_name, "1.3")
            self.assertEqual(p_alpha.version_code, 3)

            self.assertEqual(p_beta.rel_project_dir, "suite/beta")
            self.assertEqual(p_beta.project_slug, "suite-beta")
            self.assertEqual(p_beta.app_id, "com.example.beta")
            self.assertEqual(p_beta.display_name, "BetaApp")
            self.assertEqual(p_beta.version_name, "2.0.1")
            self.assertEqual(p_beta.version_code, 7)

            # Test updating version on both Groovy and Kotlin DSL projects
            releaser.update_project_version(p_alpha, "1.4", 4)
            releaser.update_project_version(p_beta, "2.1.0", 8)

            re_projects = releaser.discover_projects()
            self.assertEqual(re_projects[0].version_name, "1.4")
            self.assertEqual(re_projects[0].version_code, 4)
            self.assertEqual(
                releaser.read_manifest_version(re_projects[0]), ("1.4", 4)
            )
            self.assertEqual(re_projects[1].version_name, "2.1.0")
            self.assertEqual(re_projects[1].version_code, 8)

    def test_build_environment_and_fdroid_readiness_checks(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            app_dir = root / "synth"
            wrapper_dir = app_dir / "gradle" / "wrapper"
            wrapper_dir.mkdir(parents=True)
            (app_dir / "gradlew").write_text("#!/bin/sh\n", encoding="utf-8")
            (wrapper_dir / "gradle-wrapper.properties").write_text(
                "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.13-bin.zip\n",
                encoding="utf-8",
            )
            # First write a build.gradle with outdated AGP 8.9.0 for compileSdk 36,
            # missing dependenciesInfo, and a hardcoded org.gradle.java.home
            (app_dir / "gradle.properties").write_text(
                "org.gradle.java.home=/custom/jdk\n",
                encoding="utf-8",
            )
            (app_dir / "build.gradle").write_text(
                "plugins {\n"
                "    id 'com.android.application' version '8.9.0'\n"
                "}\n"
                "android {\n"
                "    namespace = 'com.example.synth'\n"
                "    compileSdk = 36\n"
                "    defaultConfig {\n"
                "        applicationId = 'com.example.synth'\n"
                "        minSdk = 19\n"
                "        targetSdk = 33\n"
                "        versionCode = 2\n"
                "        versionName = '1.1'\n"
                "    }\n"
                "    compileOptions {\n"
                "        sourceCompatibility = JavaVersion.VERSION_1_8\n"
                "        targetCompatibility = JavaVersion.VERSION_1_8\n"
                "    }\n"
                "}\n",
                encoding="utf-8",
            )
            # Create F-Droid metadata file so F-Droid readiness is active
            fdroid_dir = root / "fdroid" / "metadata"
            fdroid_dir.mkdir(parents=True)
            (fdroid_dir / "com.example.synth.yml").write_text(
                "Categories:\n"
                "  - Multimedia\n"
                "Builds:\n"
                "  - versionName: '1.0'\n"
                "    versionCode: 1\n"
                "    commit: synth-v1.0\n"
                "    subdir: synth\n"
                "CurrentVersion: '1.0'\n"
                "CurrentVersionCode: 1\n",
                encoding="utf-8",
            )

            releaser = AndroidReleaser(root_dir=root)
            projects = releaser.discover_projects()
            self.assertEqual(len(projects), 1)
            proj = projects[0]
            self.assertTrue(releaser.has_any_fdroid_metadata(projects))

            # Check build environment findings
            env_findings = releaser.check_build_environment(projects)
            categories = {(f.category, f.severity) for f in env_findings}
            # Should flag AGP 8.9.0 < 8.10.0 for compileSdk 36
            self.assertIn(("AGP", "WARNING"), categories)
            # Should warn that targetSdk 33 is below Play Store floor 35
            self.assertIn(("Android SDK", "WARNING"), categories)
            # Should suggest upgrading Java 1.8 sourceCompatibility
            self.assertIn(("Java Bytecode", "SUGGESTION"), categories)

            # Check F-Droid readiness findings (should catch missing LICENSE,
            # hardcoded org.gradle.java.home, missing dependenciesInfo, missing Fastlane,
            # and out-of-sync YAML version)
            fd_findings = releaser.check_fdroid_readiness(proj)
            fd_errors = [f for f in fd_findings if f.severity == "ERROR"]
            self.assertGreaterEqual(len(fd_errors), 4)
            summaries = " ".join(f.summary for f in fd_errors)
            self.assertIn("LICENSE", summaries)
            self.assertIn("org.gradle.java.home", summaries)
            self.assertIn("dependenciesInfo", summaries)
            self.assertIn("title.txt", summaries)

            # Now fix all issues and verify F-Droid readiness passes with 0 errors
            (root / "LICENSE").write_text("Apache 2.0\n", encoding="utf-8")
            (app_dir / "gradle.properties").write_text(
                "org.gradle.jvmargs=-Xmx2048m\n",
                encoding="utf-8",
            )
            (app_dir / "build.gradle").write_text(
                "plugins {\n"
                "    id 'com.android.application' version '8.13.2'\n"
                "}\n"
                "android {\n"
                "    namespace = 'com.example.synth'\n"
                "    compileSdk = 36\n"
                "    defaultConfig {\n"
                "        applicationId = 'com.example.synth'\n"
                "        minSdk = 19\n"
                "        targetSdk = 35\n"
                "        versionCode = 2\n"
                "        versionName = '1.1'\n"
                "    }\n"
                "    dependenciesInfo {\n"
                "        includeInApk = false\n"
                "        includeInBundle = false\n"
                "    }\n"
                "}\n",
                encoding="utf-8",
            )
            fl_dir = app_dir / "fastlane" / "metadata" / "android" / "en-US"
            (fl_dir / "images" / "phoneScreenshots").mkdir(parents=True)
            (fl_dir / "changelogs").mkdir(parents=True)
            (fl_dir / "title.txt").write_text("Synth\n", encoding="utf-8")
            (fl_dir / "short_description.txt").write_text("Short synth desc\n", encoding="utf-8")
            (fl_dir / "full_description.txt").write_text("Full synth description\n", encoding="utf-8")
            (fl_dir / "images" / "icon.png").write_bytes(b"png")
            (fl_dir / "images" / "phoneScreenshots" / "01.png").write_bytes(b"png")

            projects = releaser.discover_projects()
            proj = projects[0]
            releaser.update_project_version(proj, "1.1", 2, tag_name="synth-v1.1")

            fd_findings_after = releaser.check_fdroid_readiness(proj)
            fd_errors_after = [f for f in fd_findings_after if f.severity == "ERROR"]
            self.assertEqual(fd_errors_after, [])


if __name__ == "__main__":
    unittest.main()
