# senior-accounting-officer-submission-frontend

This frontend allows customers with an SAO (Senior Accounting Officer) enrolment to submit their SAO notification and certificate.

## How to use the service

Start dependent services using service-manager

`sm2 --start SAO_ALL`

To run the service locally, stop the service manager instance using

`sm2 --stop SENIOR_ACCOUNTING_OFFICER_SUBMISSION_FRONTEND`

Run the frontend locally using

`sbt -Dapplication.router=testOnlyDoNotUseInAppConf.Routes run`

This service is located on http://localhost:10058/senior-accounting-officer/submission/, however the user journeys begin from
`SENIOR_ACCOUNTING_OFFICER_HUB_FRONTEND` on http://localhost:10056/senior-accounting-officer/

### Running acceptance tests against this repository

From this repo, run:

`scripts/run_acceptance_tests.sh`

The script starts `SAO_ALL`, stops the service-manager copy of `SENIOR_ACCOUNTING_OFFICER_SUBMISSION_FRONTEND`, starts this branch locally on port `10058`, waits for `/ping/ping`, then runs the acceptance-tests repo script. It expects `../senior-accounting-officer-acceptance-tests` by default.

To run a different acceptance-tests script, set `ACCEPTANCE_TEST_SCRIPT`, for example:

`ACCEPTANCE_TEST_SCRIPT=run_all_tests.sh scripts/run_acceptance_tests.sh`

### Feature toggles

All feature toggles are defined in `application.conf` under `features`.
The internal model for feature toggles are kept at `models.config.FeatureToggle`.

If test-only routes are enabled, then feature toggles can be configured for a given instance at runtime via

http://localhost:10057/senior-accounting-officer/submission/test-only/feature-toggle

## Scaffolding new pages

To scaffold a new page, run `g8Scaffold <page_type_parameter>` in sbt shell. you can see the available scaffolds in the `.g8` directory.
Then execute the `migrate.sh` shell script to move the generated code into the correct place, do a `sbt clean compile` to ensure the new routes are available.

## Upload state persistence

Upscan notification upload state is stored in the main `user-answers` journey document under `data.notificationUpload`.

The upload reference is written when upload initiation succeeds, before the form is rendered back to the user.

Upscan callback processing updates the same `user-answers` document by reference, so there is no separate upload tracker collection.

The upload state inherits the same TTL policy as the journey document because it is stored in `user-answers`. A returning user can still continue and submit the journey as long as that journey document has not expired.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
