# 2. centralised auth routes/control

Date: 2025-08-20

## Status

Accepted

## Context

Authentication and sign out are functionalities that must be maintained by all microservices, however during some users journeys they will traverse between our hub and submission services but this should be transparent to their experience.

The entry and exit points for the user sign/out in both microservices should be the same regardless whether the user executes it from the hub or submission service

## Decision

The hub service will host all the auth related routes, here we will only reference them correspondingly:
                        
   - GET        /senior-accounting-officer/account/sign-out-survey                     
   - GET        /senior-accounting-officer/account/sign-out                            
   - GET        /senior-accounting-officer/unauthorised                                

## Consequences

- If in the unlikely event that we do decide to change the respective routes then we need to remember to update both services, it would also mean that these services cannot be released in a mismatched state.
- We will need to refactor the sign out journey on the hub so there is a single route with configurable control over survey vs no survey (and updated here)
- This would avoid code duplication and centralise the maintenance efforts for unauthorised and sign out