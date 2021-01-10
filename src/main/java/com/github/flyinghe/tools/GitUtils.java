package com.github.flyinghe.tools;

import com.github.flyinghe.depdcy.GitOperation;
import com.github.flyinghe.exception.GitUtilsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by FlyingHe on 2021/1/10.
 */
public class GitUtils {
    private static final Logger log = LoggerFactory.getLogger(GitUtils.class);

    private String repoPath;
    private String localPath;
    private String initBranch;
    private UsernamePasswordCredentialsProvider authProvider;
    private Git git;

    public static GitUtils getInstance(String repoPath, String localPath, String username, String password, String initBranch)
            throws GitUtilsException {
        return new GitUtils(repoPath, localPath, username, password, initBranch);
    }

    private GitUtils(String repoPath, String localPath, String username, String password, String initBranch)
            throws GitUtilsException {
        if (StringUtils.isBlank(repoPath)) {
            throw new GitUtilsException("远程仓库地址不能为空");
        }
        if (StringUtils.isBlank(localPath)) {
            throw new GitUtilsException("本地仓库地址不能为空");
        }
        if (StringUtils.isBlank(username)) {
            throw new GitUtilsException("用户名不能为空");
        }
        if (StringUtils.isBlank(password)) {
            throw new GitUtilsException("密码不能为空");
        }
        if (StringUtils.isBlank(initBranch)) {
            throw new GitUtilsException("初始操作分支不能为空");
        }
        try {
            this.authProvider = new UsernamePasswordCredentialsProvider(username, password);
            this.repoPath = repoPath;
            this.localPath = localPath;
            this.initBranch = initBranch;
            this.git = this.createGit();
            this.checkoutBranch(this.initBranch);
            log.info(String.format("创建Git对象,并切换到%s分支成功", this.initBranch));
        } catch (Exception e) {
            throw new GitUtilsException(e);
        }
    }

    /**
     * 操作Git(非一次性,可多次调用),执行完用户逻辑后,若有更改则会自动执行add,commit,push操作,反之会忽略add,commit,push操作。
     *
     * @param gitOperation 用户逻辑
     * @param commitMsg    提交信息
     * @throws GitUtilsException
     */
    public void operation(GitOperation gitOperation, String commitMsg) throws GitUtilsException {
        try {
            if (StringUtils.isBlank(commitMsg)) {
                throw new GitUtilsException("提交信息不能为空");
            }

            //用户逻辑
            gitOperation.run(this, this.git);

            /**
             * 若远程存在当前分支并且没有任何更改则不做后续任何操作
             */
            String currentBranch = this.getCurrentBranch();
            if (!this.isANewRemoteBranch(currentBranch) && this.git.status().call().isClean()) {
                log.info(String.format("本次操作(分支:%s)没有任何更改,忽略后续add,commit,push操作(提交信息:%s)", currentBranch, commitMsg));
                return;
            }
            //add All
            this.git.add().addFilepattern(".").call();
            //提交分支
            this.git.commit().setAll(true).setMessage(commitMsg).call();
            //push 分支
            this.git.push().setCredentialsProvider(this.authProvider).call();
            log.info(String.format("本次操作(分支:%s)push成功(提交信息:%s)", currentBranch, commitMsg));
        } catch (Exception e) {
            throw new GitUtilsException(e);
        }
    }

    /**
     * 一次性操作Git,执行完用户逻辑后,若有更改则会自动执行add,commit,push操作,反之会忽略add,commit,push操作。
     * 本方法执行完毕后会关闭Git对象,不能再使用Git进行其他操作
     *
     * @param gitOperation 用户逻辑
     * @param commitMsg    提交信息
     * @throws GitUtilsException
     */
    public void operationOnce(GitOperation gitOperation, String commitMsg) throws GitUtilsException {
        try (Git git = this.git) {
            this.operation(gitOperation, commitMsg);
        }
    }

    /**
     * 操作Git(非一次性,可多次调用),执行完用户逻辑后(仅支持只读操作),并不会执行add,commit,push操作
     *
     * @param gitOperation
     */
    public void operationRead(GitOperation gitOperation) {
        //用户逻辑
        gitOperation.run(this, this.git);
    }

    /**
     * 一次性操作Git,执行完用户逻辑后(仅支持只读操作),并不会执行add,commit,push操作。
     * 本方法执行完毕后会关闭Git对象,不能再使用Git进行其他操作
     *
     * @param gitOperation
     */
    public void operationReadOnce(GitOperation gitOperation) {
        try (Git git = this.git) {
            this.operationRead(gitOperation);
        }
    }

    public String getRepoPath() {
        return repoPath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getInitBranch() {
        return initBranch;
    }

    public UsernamePasswordCredentialsProvider getAuthProvider() {
        return authProvider;
    }

    public Git getGit() {
        return git;
    }


    /**
     * 关闭Git
     */
    public void closeGit() {
        this.git.close();
    }

    /**
     * 获取当前分支名称
     *
     * @return 返回当前分支名称
     * @throws Exception
     */
    public String getCurrentBranch() throws Exception {
        return this.git.getRepository().getBranch();
    }

    /**
     * 判断此分支是否是一个新的远程分支
     *
     * @param branch
     * @return 若是一个远程分支则返回true,若远程存在此分支则返回false
     */
    private boolean isANewRemoteBranch(String branch) throws GitAPIException {
        List<Ref> remoteBranches = this.git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        for (Ref ref : remoteBranches) {
            if (ref.getName().endsWith(String.format("/remotes/origin/%s", branch))) {
                return false;
            }
        }
        return true;
    }


    /**
     * 切换到指定分支,若远程不存在此分支,则本地以master重新创建此分支并切换到此分支,若远程存在此分支则切换远程分支到本地
     *
     * @param branch 指定分支
     * @throws GitAPIException
     */
    public void checkoutBranch(String branch) throws GitAPIException {
        List<String> remoteBranches = new ArrayList<>();
        List<String> localBranches = new ArrayList<>();
        this.git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().forEach(ref -> {
            if (ref.getName().contains("/remotes/origin/")) {
                //远程分支
                remoteBranches.add(ref.getName().substring(ref.getName().lastIndexOf("/") + 1));
            } else {
                //本地分支
                localBranches.add(ref.getName().substring(ref.getName().lastIndexOf("/") + 1));
            }
        });

        if (localBranches.contains(branch)) {
            //若本地存在此分支则直接切换
            this.git.checkout().setCreateBranch(false).setName(branch).call();
        } else if (remoteBranches.contains(branch)) {
            //若本地不存在此分支，远程存在此分支则切换远程分支到本地
            this.git.checkout().setStartPoint("origin/" + branch).setCreateBranch(true).setName(branch).call();
        } else {
            //若本地远程都不存在此分支则从远程master分支创建一个新的分支
            this.git.checkout().setStartPoint("origin/master").setCreateBranch(true).setName(branch).call();
        }
    }


    /**
     * 确保100%创建目标目录(空目录)
     *
     * @param clientPath 目标目录
     * @throws IOException
     */
    private void createClientPath(String clientPath) throws IOException {
        File file = new File(clientPath);
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }
        file.mkdirs();
    }


    /***
     * Clone远程仓库到本地,并返回Git对象
     * @return 返回Git对象
     * @throws Exception
     */
    private Git createGit() throws Exception {
        this.createClientPath(this.localPath);
        Git g = Git.cloneRepository().setURI(this.repoPath).setDirectory(new File(this.localPath)).setCredentialsProvider(this.authProvider).call();
        log.info(String.format("clone远程仓库(%s)到本地仓库(%s)成功", this.repoPath, this.localPath));
        return g;
    }
}
