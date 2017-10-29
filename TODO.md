# TODO

- rename rpi-daemon to buildtv-daemon

- Binaries should be pre-compiled and installed as Debian packages.
    - Ansible deploy dir could still be used, but no need for user to build project.

- Config setup and management needs to be simplified, replace Ansible with APIs for all settings and having a
  panel/interface for management instead? Not sure how firewall config etc works. If software is installed as
  binaries, perhaps have either a custom distro *or* a JAR to configure environment.

- Split down Ansible into multiple playbooks

- Add ability to encrypt Jira password

