#!/bin/bash
#
# Travis ci script for releasing gradle project
# Adapted from http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

# Change this
REPO="travis-ci-config-test"

# Change if necessary
USER="shiraji"
JDK="oraclejdk7"
BRANCH="master"

# Not handling errors
set -e

if [ "$TRAVIS_REPO_SLUG" != "$USER/$REPO" ]; then
  # Check repo
  echo "TRAVIS_REPO_SLUG: '$TRAVIS_REPO_SLUG' USER/REPO: '$USER/$REPO'"
elif [ "$TRAVIS_JDK_VERSION" != "$JDK" ]; then
  # Check JDK
  echo "TRAVIS_JDK_VERSION: '$TRAVIS_JDK_VERSION' JDK: '$JDK"
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  # Check how to run this script
  echo "It's pull request!"
elif [ "$TRAVIS_BRANCH" != "$BRANCH" ]; then
  # Check branch
  echo "TRAVIS_BRANCH: '$TRAVIS_BRANCH' BRANCH: '$BRANCH'"
else
  echo "Start releasing..."
  git checkout master
  git config user.name "Travis CI"
  git config user.email "isogai.shiraji@gmail.com"
  git tag `cat VERSION`
  git push git@github.com:${USER}/${REPO}.git `cat VERSION`
  git rm .travis/release
  git commit -m "[skip ci] prepare next development"
  git push git@github.com:${USER}/${REPO}.git $BRANCH
fi
