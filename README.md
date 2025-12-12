
>>  **Current sbt issue**:
> [12/12/25] "Terminal weirdness" with sbt and giter8 (g8) scaffolds refers to several interactive shell issues where user input is not handled or displayed correctly during template generation. This often manifests when using sbt new or g8Scaffold and is frequently caused by sbt's Super Shell interfering with interactive prompts
>
>Current workaround is to run the following command:
>> `sbt --supershell=false`

# senior-accounting-officer-submission-frontend

This frontend allows customers with SAO (Senior Accounting Officer) enrolment to submit their SAO notification and certificate.

## How to use the service

Start dependent services using service-manager

`sm2 --start SAO_ALL`

To run the service locally, stop the service manager instance using

`sm2 --stop SENIOR_ACCOUNTING_OFFICER_SUBMISSION_FRONTEND`

Run the frontend locally using

`sbt run`

This service is localed on http://localhost:10058/senior-accounting-officer/submission/, however the user journeys begin from
`SENIOR_ACCOUNTING_OFFICER_HUB_FRONTEND` on http://localhost:10056/senior-accounting-officer/

# ADR
This project uses ADR to [adr-tools](https://github.com/npryce/adr-tools) to record architecture decisions.
After installing the tool ensure to execute

`adr init doc/architecture/decisions`

but delete the new `*-record-architecture-decisions.md`

This is so it would sync the output directory so that subsequent docs are placed under `doc/architecture` otherwise the default will go into `doc/adr/`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
