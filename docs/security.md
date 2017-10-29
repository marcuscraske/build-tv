# Security

Below are some quick points around how build TVs are secured, when doing a standard installation/deployment.

- `ufw` is used to control IP tables, so that only port 22 (SSH) is exposed (firewall).
- SSH authentication is key only.
- Default `pi` account is removed.
- The user automatically logged-on during startup, `wallboard` is locked down and has little to no permissions.

You are recommended to periodically update all your build TV instances to protect against vulnerabilities. Although
trial a single instance initially, in the event of breaking changes.
