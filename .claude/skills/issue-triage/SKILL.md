---
name: issue-triage
description: 'Classify, prioritize, and label incoming GitHub issues. Identifies duplicates, suggests labels, and recommends affected areas of the codebase.'
---

# Issue Triaging

## Overview

Help classify, prioritize, and label incoming GitHub issues for the Montonio Java SDK. Produces a structured triage summary that can be applied directly to the issue.

## Input

A GitHub issue number or URL. If not provided, list recent open untriaged issues.

## Step 1: Gather Context

1. **Read the issue** using `gh issue view`
2. **Check for duplicates or related issues** by searching existing open and closed issues for similar keywords
3. **Identify affected code** by searching the codebase for classes, methods, or packages mentioned in the issue

## Step 2: Classify the Issue

Assign exactly one **type**:

| Type            | When to use                                                  |
| --------------- | ------------------------------------------------------------ |
| `bug`           | Something is broken or behaves incorrectly                   |
| `feature`       | A wholly new capability that doesn't exist yet               |
| `enhancement`   | An improvement to existing functionality                     |
| `question`      | A usage question or request for clarification                |
| `documentation` | Missing or incorrect documentation                           |

## Step 3: Assess Priority

Assign a **priority** based on impact and urgency:

| Priority   | Criteria                                                        |
| ---------- | --------------------------------------------------------------- |
| `critical` | Breaks core payment flow, data loss, or security vulnerability  |
| `high`     | Significant functionality broken, no workaround                 |
| `medium`   | Functionality impaired but workaround exists                    |
| `low`      | Minor inconvenience, cosmetic, or nice-to-have                  |

## Step 4: Identify Affected Areas

Map the issue to one or more areas of the SDK:

- **api-client** - HTTP client, request/response handling
- **payment-orders** - Payment order creation, lifecycle
- **payment-methods** - Payment method discovery
- **webhooks** - JWT validation, webhook/return handling
- **auth** - Authentication, JWT signing
- **models** - Request/response DTOs
- **configuration** - SDK configuration, builder
- **testing** - Test infrastructure, test utilities
- **build** - Gradle, CI/CD, publishing

## Step 5: Present Triage Summary

Output the triage in this format:

```
## Triage Summary

**Type:** <type>
**Priority:** <priority>
**Areas:** <comma-separated areas>
**Duplicates/Related:** <issue numbers or "None found">

### Analysis

<2-3 sentence summary of the issue and its impact>

### Affected Code

- `<file path>` - <why it's relevant>
- ...

### Suggested Labels

<list of GitHub labels to apply: type/<type>, priority/<priority>, area/<area>>

### Recommended Next Steps

<1-3 actionable items for addressing the issue>
```

## Step 6: Apply Labels (if requested)

If the user confirms, apply the suggested labels to the issue using `gh issue edit`.

## Key Principles

- **Be specific** - Point to exact files and code paths, not vague areas
- **Check for duplicates first** - Avoid duplicate work by linking related issues
- **Prioritize by user impact** - Payment flow issues are always higher priority
- **Stay neutral** - Classify based on evidence in the issue, not assumptions
