# 2. remove-unwanted-routes

Date: 2025-08-20

## Status

Under Review

## Context

All the auth related process should be managed by the SAO hub service,

## Decision

1. All the auth routes will be from the hub service:
   - GET        /account/signed-out                          controllers.auth.SignedOutController.onPageLoad()
   - GET        /account/sign-out-survey                     controllers.auth.AuthController.signOut()
   - GET        /account/sign-out                            controllers.auth.AuthController.signOutNoSurvey()
   - GET        /unauthorised                                controllers.UnauthorisedController.onPageLoad()
   - GET        /                                            controllers.IndexController.onPageLoad()

## Consequences

Reduces code redundancy and maintenance

