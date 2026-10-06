# Naikeri DRA

A Diameter Routing Agent built on the [Naikeri Signaling Gateway](https://github.com/FerUy/naikeri-signaling-gateway)
and [jDiameter](https://github.com/FerUy/naikeri-jdiameter). It routes Diameter requests between peers by
rules matching origin host and realm, destination host and realm, the IMSI and the subscription ID, and
sends each to a routing host chosen by priority and load share, recording the route where configured.

It runs as a standalone JVM application over SCTP, with multi-homing: the transport it's tested on.
jDiameter's TCP transport is also available. It has been used with the location interfaces SLg, SLh and Sh.

## Requirements

- Java 11
- Maven 3.9, and Ant to build a release
- Linux with SCTP support (the `lksctp-tools` package, or the kernel's `sctp` module)
- PostgreSQL, optionally: realm and application data can be kept in a database (`conf/db.sql` creates
  the schema, and `conf/application.yaml` holds the connection). It isn't needed when the realms are
  configured in `diameter-server.xml`.

## Building

~~~
mvn clean install -Passembly
~~~

The admin and installation guides are a separate build; `-Pall` adds the PDFs to the HTML:

~~~
mvn -f docs/pom.xml clean install -Pall
~~~

## Releasing

~~~
cd release && ant
~~~

builds the DRA and packages it as `release/Naikeri-DRA-<version>.zip`:

| Directory | Contents |
|---|---|
| `bin/` | the DRA jar, its dependencies in `libs/`, the management jar, `start.sh` and `dra-cli.sh` |
| `conf/` | `naikeri-signaling-gateway.xml`, `diameter-server.xml`, `dictionary.xml`, `log4j2.xml`, `application.yaml` and `db.sql` |
| `logs/` | where the DRA writes its logs |

Jenkins builds the same zip for every commit on master, as `Naikeri-DRA-<version>-<build>.zip`.

## Configuration

- `naikeri-signaling-gateway.xml`: the routing rules, with the drop and fallback policies, and the
  Diameter layer the DRA runs on.
- `diameter-server.xml`: the jDiameter configuration. The local peer (its URI, its addresses for
  multi-homing, its realm and the applications it supports), the peers it connects to or accepts, and
  the realms.
- `dictionary.xml`: the Diameter dictionary, taken from jDiameter by the build. It's kept in `conf/` so a
  deployment can extend it.

## Running

Unpack the release anywhere and start it:

~~~
bin/start.sh
~~~

It reads its configuration from `conf/` and writes its console output to `logs/console.out`.
`bin/dra-cli.sh` opens an interactive prompt for the DRA's management commands.

## Testing

The DRA is tested end to end with the [Naikeri GMLC](https://github.com/FerUy/naikeri-gmlc) and two
jDiameter location simulators, over SLg, SLh and Sh: the GMLC and one simulator on one host, the DRA and
the other simulator on a second, with multi-homing between them.
