package org.apache.hadoop.fs;
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.fs.FileSystem.Statistics;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.util.Progressable;

/**
 * A <code>FilterFs</code> contains some other file system, which it uses as its
 * basic file system, possibly transforming the data along the way or providing
 * additional functionality. The class <code>FilterFs</code> itself simply
 * overrides all methods of <code>AbstractFileSystem</code> with versions that
 * pass all requests to the contained file system. Subclasses of
 * <code>FilterFs</code> may further override some of these methods and may also
 * provide additional methods and fields.
 * 
 */
@InterfaceAudience.Private
@InterfaceStability.Evolving /*Evolving for a release,to be changed to Stable */
public abstract class FilterFs extends AbstractFileSystem {
  private final AbstractFileSystem myFs;
  
  protected AbstractFileSystem getMyFs() {
    return myFs;
  }
  
  protected FilterFs(AbstractFileSystem fs) throws IOException,
      URISyntaxException {
    super(fs.getUri(), fs.getUri().getScheme(),
        fs.getUri().getAuthority() != null, fs.getUriDefaultPort());
    myFs = fs;
  }

  
  protected Statistics getStatistics() {
    return myFs.getStatistics();
  }

  
  protected Path getInitialWorkingDirectory() {
    return myFs.getInitialWorkingDirectory();
  }
  
  
  protected Path getHomeDirectory() {
    return myFs.getHomeDirectory();
  }
  
  
  protected FSDataOutputStream createInternal(Path f,
    EnumSet<CreateFlag> flag, FsPermission absolutePermission, int bufferSize,
    short replication, long blockSize, Progressable progress,
    int bytesPerChecksum, boolean createParent) 
      throws IOException, UnresolvedLinkException {
    checkPath(f);
    return myFs.createInternal(f, flag, absolutePermission, bufferSize,
        replication, blockSize, progress, bytesPerChecksum, createParent);
  }

  
  protected boolean delete(Path f, boolean recursive) 
      throws IOException, UnresolvedLinkException {
    checkPath(f);
    return myFs.delete(f, recursive);
  }

  
  protected BlockLocation[] getFileBlockLocations(Path f, long start, long len)
      throws IOException, UnresolvedLinkException {
    checkPath(f);
    return myFs.getFileBlockLocations(f, start, len);
  }

  
  protected FileChecksum getFileChecksum(Path f) 
      throws IOException, UnresolvedLinkException {
    checkPath(f);
    return myFs.getFileChecksum(f);
  }

  
  protected FileStatus getFileStatus(Path f) 
      throws IOException, UnresolvedLinkException {
    checkPath(f);
    return myFs.getFileStatus(f);
  }

  
  protected FileStatus getFileLinkStatus(final Path f) 
    throws IOException, UnresolvedLinkException {
    checkPath(f);
    return myFs.getFileLinkStatus(f);
  }
  
  
  protected FsStatus getFsStatus(final Path f) throws AccessControlException,
    FileNotFoundException, UnresolvedLinkException, IOException {
    return myFs.getFsStatus(f);
  }

  
  protected FsStatus getFsStatus() throws IOException {
    return myFs.getFsStatus();
  }

  
  protected FsServerDefaults getServerDefaults() throws IOException {
    return myFs.getServerDefaults();
  }

  
  protected int getUriDefaultPort() {
    return myFs.getUriDefaultPort();
  }

  
  protected URI getUri() {
    return myFs.getUri();
  }
  
  
  protected void checkPath(Path path) {
    myFs.checkPath(path);
  }
  
  
  protected String getUriPath(final Path p) {
    return myFs.getUriPath(p);
  }
  
  
  protected FileStatus[] listStatus(Path f) 
      throws IOException, UnresolvedLinkException {
    checkPath(f);
    return myFs.listStatus(f);
  }

  
  protected void mkdir(Path dir, FsPermission permission, boolean createParent)
    throws IOException, UnresolvedLinkException {
    checkPath(dir);
    myFs.mkdir(dir, permission, createParent);
    
  }

  
  protected FSDataInputStream open(final Path f) throws AccessControlException,
    FileNotFoundException, UnresolvedLinkException, IOException {
    checkPath(f);
    return myFs.open(f);
  }

  
  protected FSDataInputStream open(Path f, int bufferSize) 
    throws IOException, UnresolvedLinkException {
    checkPath(f);
    return myFs.open(f, bufferSize);
  }

  
  protected void renameInternal(Path src, Path dst) 
    throws IOException, UnresolvedLinkException {
    checkPath(src);
    checkPath(dst);
    myFs.rename(src, dst, Options.Rename.NONE);
  }

  
  protected void renameInternal(final Path src, final Path dst,
      boolean overwrite) throws AccessControlException,
      FileAlreadyExistsException, FileNotFoundException,
      ParentNotDirectoryException, UnresolvedLinkException, IOException {
    myFs.renameInternal(src, dst, overwrite);
  }
  
  
  protected void setOwner(Path f, String username, String groupname)
    throws IOException, UnresolvedLinkException {
    checkPath(f);
    myFs.setOwner(f, username, groupname);
    
  }

  
  protected void setPermission(Path f, FsPermission permission)
    throws IOException, UnresolvedLinkException {
    checkPath(f);
    myFs.setPermission(f, permission);
  }

  
  protected boolean setReplication(Path f, short replication)
    throws IOException, UnresolvedLinkException {
    checkPath(f);
    return myFs.setReplication(f, replication);
  }

  
  protected void setTimes(Path f, long mtime, long atime) 
      throws IOException, UnresolvedLinkException {
    checkPath(f);
    myFs.setTimes(f, mtime, atime);
  }

  
  protected void setVerifyChecksum(boolean verifyChecksum) 
      throws IOException, UnresolvedLinkException {
    myFs.setVerifyChecksum(verifyChecksum);
  }

  
  protected boolean supportsSymlinks() {
    return myFs.supportsSymlinks();
  }

  
  protected void createSymlink(Path target, Path link, boolean createParent) 
    throws IOException, UnresolvedLinkException {
    myFs.createSymlink(target, link, createParent);
  }

  
  protected Path getLinkTarget(final Path f) throws IOException {
    return myFs.getLinkTarget(f);
  }
}
