package com.github.flyinghe.depdcy;

import com.github.flyinghe.tools.GitUtils;
import org.eclipse.jgit.api.Git;

/**
 * Created by FlyingHe on 2021/1/10.
 */
public interface GitOperation {
    public abstract void run(GitUtils gitUtils, Git git);
}
