[ ![Download](https://api.bintray.com/packages/hiyainc-oss/maven/sbt-git-versioning/images/download.svg) ](https://bintray.com/hiyainc-oss/maven/sbt-git-versioning/_latestVersion)

This is a fork of [rallyhealth/sbt-git-versioning](https://github.com/rallyhealth/sbt-git-versioning). 
SemVer plugin was removed to make it compatible with newer MiMa plugins.

**sbt-git-versioning** is a suite of sbt plugins designed to make maintaining a simple, consistent, and accurate
 [semantic versioning](http://semver.org/spec/v2.0.0.html) scheme with as little manual labor as possible.

There are two sbt plugins in this one plugin library:
* [GitVersioningPlugin](#GitVersioningPlugin)

# Install

1. Remove the `version := ...` directive from the project's `build.sbt` file
2. Add the following code to `project/plugins.sbt` file (if you want to use Ivy style resolution):
```scala
  resolvers += Resolver.bintrayRepo("hiyainc-oss", "maven")
```
In either case, you should now be able to add the plugin dependency to `project/plugins.sbt`:
```scala
  addSbtPlugin("com.hiya" % "sbt-git-versioning" % "x.y.z")
```
3. Add a `gitVersioningSnapshotLowerBound` placeholder in build.sbt.
```sbt
// Uncomment when you're ready to start building 1.0.0-...-SNAPSHOT versions.
// gitVersioningSnapshotLowerBound in ThisBuild := "1.0.0"
```

# GitVersioningPlugin

[GitVersioningPlugin](blob/master/src/main/scala/com/rallyhealth/versioning/GitVersioningPlugin.scala)
focuses on automatically determining the value of the `version` setting. The `version` is determined by looking at git history (for
previous tags) and the state of the working directly. Read on for the exact details.

## Usage

This plugin will automatically determine and set the `version` setting on startup. There is nothing developers need to
explicitly do. You can `publishLocal` and PRs and use the created snapshot artifacts anywhere you would a release artifact.

You will see additional log statements like:
```
[info] Skipping fetching tags from git remotes; to enable, set the system property version.autoFetch=true
[info] GitVersioningPlugin set versionFromGit=2.0.0-dirty-SNAPSHOT
[info] GitVersioningPlugin set version=2.0.0-dirty-SNAPSHOT
[info] GitVersioningPlugin set isCleanRelease=false
```

## Information

The version format is:

* Leading `v`
* Major.Minor.Patch, e.g `1.2.3`
* (Optional) Commits since last tag
* (Optional) Short hash of the current commit
* (Optional) `-dirty-SNAPSHOT` if uncommitted changes

Putting it all together (including all optionals) gives you `v1.2.3-<commit_count>-<hash>-dirty-SNAPSHOT`.

| If HEAD is...         | ...and commits have a tag (ex. `v1.0.3`) ... | ...and is "clean"  | Then the version is                |
|-----------------------|----------------------------------------------|--------------------|------------------------------------|
| tagged (ex. `v1.1.0`) | :heavy_minus_sign:                           | :white_check_mark: | `v1.1.0`                           |
| tagged (ex. `v1.1.0`) | :heavy_minus_sign:                           | :x:                | `v1.1.0-dirty-SNAPSHOT`            |
| not tagged            | :white_check_mark:                           | :white_check_mark: | `v1.0.4-1-0123abc-SNAPSHOT`        |
| not tagged            | :white_check_mark:                           | :x:                | `v1.0.4-1-0123abc-dirty-SNAPSHOT`  |
| not tagged            | :x:                                          | :white_check_mark: | `v0.0.1-1-0123abc-SNAPSHOT`        |
| not tagged            | :x:                                          | :x:                | `v0.0.1-1-0123abc-dirty-SNAPSHOT`  |
| non-existent          | :heavy_minus_sign:                           | :white_check_mark: | `v0.0.1-dirty-SNAPSHOT`            |
| non-existent          | :heavy_minus_sign:                           | :x:                | `v0.0.1-dirty-SNAPSHOT`            |

## Nudging the version

The version is generally derived from git, though there are a couple ways to change that.

### Version Override

The version.override arg sets the version and overrides all other sources.

```
sbt -Dversion.override=1.2.3 ...
```

### gitVersioningSnapshotLowerBound

The `gitVersioningSnapshotLowerBound` settingKey can push the version to a higher version snapshot than the current git state.

This is useful for preparing major releases with breaking changes (esp. when combined with shading -- stay tuned for more features here).

| versionFromGit           | gitVersioningSnapshotLowerBound | Final Version            |
| --------------           | --------------------------------- | -------------            |
| 1.0.0                    |                                   | 1.0.0                    |
| 1.0.0-n-0123abc-SNAPSHOT |                                   | 1.0.0-n-0123abc-SNAPSHOT |
| 1.0.0                    | 2.0.0                             | 2.0.0-n-0123abc-SNAPSHOT |

### Release arg Property

The release arg bumps the version up by a major, minor, or patch increment.
```
sbt -Drelease=<major|minor|patch> ...
```

The release arg alters the value of `versionFromGit`, but is still bounded by `gitVersioningSnapshotLowerBound`. For example:

| versionFromGit | -Drelease | gitVersioningSnapshotLowerBound | Final Version |
| -------------- | --------- | ------------------------------- | ------------- |
| 1.0.0          | patch     |                                 | 1.0.1         |
| 1.0.0          | minor     |                                 | 1.1.0         |
| 1.0.0          | major     |                                 | 2.0.0         |
| 1.0.0          | patch     | 2.0.0                           | 2.0.0-n-0123abc-SNAPSHOT |
| 1.0.0          | minor     | 2.0.0                           | 2.0.0-n-0123abc-SNAPSHOT |
| 1.0.0          | major     | 2.0.0                           | 2.0.0         |

#### Example
With most recent git tag at `v1.4.2` and a `gitVersioningSnapshotLowerBound` setting of:
```
gitVersioningSnapshotLowerBound in ThisBuild := "2.0.0"
```

```
$ sbt
[info] GitVersioningPlugin set versionFromGit=1.4.3-1-400b9ac-SNAPSHOT
[info] GitVersioningPlugin set version=2.0.0-1-400b9ac-SNAPSHOT
>
```

### Notes

* The patch version is incremented if there are commits, dirty or not. But it is not incremented if there are no
commits. (I'm not clear on why this is, but it is legacy behavior.)
* The hash does **not** have a 'g' prefix like the output of `git describe`
* A build will be flagged as not clean (and will have a `-dirty-SNAPSHOT` identifier applied) if
`git status --porcelain` returns a non-empty result.
* If there is no tag in the commit history, the number appended after the version number will reflect the number of commits
since the creation of the repository.
* This plugin is intentionally different than something like [sbt-release](https://github.com/sbt/sbt-release) which stores
the version in a `version.sbt` file. Those types of plugins require more manual effort on the part of the developer.

*Warning:* Git Versioning may misbehave with shallow clones. If the incorrect version is being returned and tags are
accessible, you may be using a shallow clone, in which case `git fetch --unshallow` will fix the issue. On CI systems,
ensure that any git plugins are configured to not use shallow clones.

## Creating a Release

### Recommended: -Drelease

Creating a release is done by passing a [release arg](#release-arg-property).
```
sbt -Drelease=patch publish[Local]
```

You can extract the version for other purposes (e.g. git tagging after successful publish) using the `writeVersion <file>` input task.

```bash
export VERSIONFILE=$(mktemp) # avoid creating untracked files so version doesn't become -dirty.
sbt "writeVersion $VERSIONFILE"
export VERSION=$(cat $VERSIONFILE)
git tag "v${VERSION}""
git push origin "v${VERSION}"
# ...
```

### Possible: tag + sbt-git-versioning

...or by tagging and then publishing a release...
 ```
git tag v1.2.3
sbt publish[Local]
```

### Not recommended (unless have good reasons): version.override

...or by overriding the version that will be applied to a specific build, using the
`version.override` setting. Typically this is done by at the command line to avoid changing `build.sbt`.
```
sbt -Dversion.override=1.2.3 publish[Local]
```

You shouldn't do this without good reason. Version determination can be complicated, because git can be complicated.

## Extra Identifiers

To add an extra identifier like "-alpha" or "-rc1" or "-rally" it must be included it in the version directly
by overriding the "version" setting directly. (There was a feature to add those separately but it has been
removed because it was never used. Feel free to re-add it.)
