Using helios-solo
===

helios-solo provides a local Helios cluster running in a Docker container. This
gives you a single Helios master and agent to play around with. All Helios jobs
are run on the local Docker instance. The only prerequisite is
[Docker](https://docs.docker.com/installation/).

Install & Run
---

### OS X

Install [docker-machine](https://docs.docker.com/machine/) or
another similar solution. Configure it correctly and **start the service** so that commands like
`docker info` and `docker ps` work. Then run the below.

```bash
$ brew tap spotify/public && brew install helios-solo
$ helios-up
```

### Linux

Install Docker, configure it correctly, and **start the service** so that
commands like `docker info` and `docker ps` work. Either
[follow the detailed instructions from Docker](https://docs.docker.com/installation/),
or opt for the quick install:

```bash
$ curl -sSL https://get.docker.com/ | sudo sh -
$ sudo usermod -aG docker `whoami`
```

Then, install helios-solo:

```bash
$ curl -sSL https://spotify.github.io/helios-apt/go | sudo sh -
$ sudo apt-get install helios-solo
$ helios-up
```

If `helios-up` fails, ensure that Docker is running and your client is correctly
configured. A good test is to run `docker info` and make sure it works.

Usage
---

Here are some example commands:

```bash
# Start helios-solo
$ helios-up

# Show available versions of Helios to use for helios-solo
$ helios-use

# Upgrade to the latest version of Helios
$ helios-use latest

# Clean up all jobs & containers
$ helios-cleanup
```

Once helios-solo is up, you can use the `helios-solo` command to talk to it. The
usage is identical to the `helios` CLI. Here's an example:

```bash
# Make sure the helios-solo agent is up
$ helios-solo hosts

# Create a trivial helios job
$ helios-solo create test:1 busybox -- sh -c "while :; do sleep 1; done"

# Deploy the job on the (local) solo host
$ helios-solo deploy test:1 solo

# Check the job status
$ helios-solo status

# Undeploy job
$ helios-solo undeploy -a --yes test:1

# Remove job
$ helios-solo remove test:1
```

Commands
--------

* `helios-up`<br />
  Brings up the helios-solo container.

* `helios-down`<br />
  Destroys the helios-solo container.

* `helios-solo ...`<br />
  Wrapper around the helios CLI for talking to helios-solo.
  Essentially identical to `eval $(helios-env) && helios -z $HELIOS_URI ...`.

* `helios-restart`<br />
  Destroy and restart the helios-solo container.

* `helios-cleanup`<br />
  Remove all helios-solo jobs and containers.

* `helios-env`<br />
  Utility for setting `HELIOS_URI` environment variable in your
  shell: `eval $(helios-env)`.

* `helios-use` [version]<br />
  Lists the available versions of Helios, switches the underlying Helios version, or upgrades to the latest version.

Container Logging
-----------------

To see the logs for a container deployed by helios-solo, run:

    $ docker logs <container_id>

You can get the ID's of all running containers with `docker ps`.

Debugging
---------

The following commands can be helpful for debugging helios-solo:

```bash
$ docker logs helios-solo-container

$ helios-up && docker exec -it helios-solo-container bash
```
