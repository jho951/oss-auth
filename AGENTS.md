# AGENTS.md instructions for /Users/jhons/Downloads/BE/module/auth

<INSTRUCTIONS>
# Architecture boundary: auth vs authorization

## Core principle
This repository implements authentication infrastructure, not authorization policy.

## What this project owns
- Authentication only
- Identity verification for JWT / session / hybrid auth flows
- Principal creation and propagation
- Token/session validation
- Delivery of authorization-related metadata as claims only
    - examples: roles, scopes, tenantId, authSource

## What this project does NOT own
- Final authorization decisions
- Resource-level permission evaluation
- Domain policy such as:
    - canEditDocument(userId, documentId)
    - canManageWorkspace(userId, workspaceId)
    - owner/editor/viewer resolution
    - tenant-specific policy interpretation
- Permission service logic
- RBAC/ABAC policy engine
- Domain-specific access rules

## Allowed behavior
- Read roles/scopes/claims from token or session
- Expose them through AuthPrincipal or equivalent model
- Provide simple authenticated/unauthenticated guards
- Provide extension points for downstream services to consume auth metadata

## Forbidden behavior
- Do not implement resource authorization logic in auth modules
- Do not add permission evaluators tied to document/workspace/block domains
- Do not make gateway/service-specific authorization decisions here
- Do not couple auth-core/auth-jwt/auth-session/auth-spring to domain permission models

## Design rule
Authentication modules supply identity and security metadata.
Authorization is evaluated in a separate permission layer or domain service.
</INSTRUCTIONS>
