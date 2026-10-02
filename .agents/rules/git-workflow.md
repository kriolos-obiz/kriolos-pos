# Git Workflow Rules

## NEVER commit directly on `main`

- **Never** run `git commit` while on the `main` branch.
- Always create a feature or fix branch before committing:
  ```
  git checkout -b fix/<scope>   # for fixes
  git checkout -b feat/<scope>  # for features
  ```
- If a commit was accidentally made on `main`, immediately move it to a proper branch:
  ```bash
  git checkout -b fix/<scope>       # carry the commit to a new branch
  git branch -f main origin/main    # reset local main to origin
  ```
- Branch naming convention:
  - `fix/<scope>` — bug fixes, CI fixes, doc corrections
  - `feat/<scope>` — new features
  - `chore/<scope>` — maintenance, dependency updates
