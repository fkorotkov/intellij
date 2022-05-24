/*
 * Copyright 2019 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.command.buildresult;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.idea.blaze.base.command.buildresult.BlazeArtifact.LocalFileArtifact;
import com.google.idea.blaze.base.filecache.RemoteOutputsCache;
import com.google.idea.blaze.base.ideinfo.ArtifactLocation;
import com.google.idea.blaze.base.sync.workspace.ArtifactLocationDecoder;
import com.intellij.openapi.project.Project;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.toImmutableList;

/** Helper class for resolving {@link BlazeArtifact}s to local files. */
public final class OutputArtifactResolver {
  /**
   * Resolve a collection of {@link ArtifactLocation} to local files, skipping those for which a
   * local file can't be found.
   */
  public static ImmutableList<File> resolveAll(
      Project project, ArtifactLocationDecoder decoder, Collection<ArtifactLocation> artifacts) {
    return artifacts.stream()
        .map(a -> resolve(project, decoder, a))
        .filter(Objects::nonNull)
        .collect(toImmutableList());
  }

  /**
   * Resolve a single {@link ArtifactLocation} to a local file, returning null if no local file can
   * be found.
   */
  @Nullable
  public static File resolve(
      Project project, ArtifactLocationDecoder decoder, ArtifactLocation artifact) {
    return resolve(project, decoder.resolveOutput(artifact));
  }

  @Nullable
  public static File resolve(Project project, BlazeArtifact output) {
    if (output instanceof LocalFileArtifact) {
      return patchExternalFilePath(((LocalFileArtifact) output).getFile());
    }
    Preconditions.checkState(output instanceof RemoteOutputArtifact);
    return RemoteOutputsCache.getInstance(project).resolveOutput((RemoteOutputArtifact) output);
  }


  /**
   * Point external workspace symlinks to the corresponding fixed location so IntelliJ doesn't go crazy.
   */
  private static File patchExternalFilePath(@Nullable File maybeExternal) {
    if (maybeExternal == null) {
      return null;
    }
    String externalString = maybeExternal.toString();
    if (externalString.contains("/external/")
            && !externalString.contains("/bazel-out/")
            && !externalString.contains("/blaze-out/")) {
      return new File(externalString.replaceAll("/execroot.*/external/", "/external/"));
    }
    return maybeExternal;
  }
}
