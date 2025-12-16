## Session: Scaffolding Ingest Service
**Date:** $(date +%Y-%m-%d)

### Issue 1: Module Not Found in Reactor
**Error:** \`[ERROR] Could not find the selected project in the reactor: :ingest-service\`
**Context:** Ran \`mvn -pl :ingest-service ...\` from the project root.
**Cause:** The new module \`ingest-service\` exists physically on disk but was not added to the \`<modules>\` section of the root \`pom.xml\`. The Maven Reactor (build manager) relies on the parent POM to discover child modules.
**Resolution:** * **Option A (Permanent):** Add \`<module>ingest-service</module>\` to the root POM.
* **Option B (Temporary/Isolated):** Run the build directly against the module's POM using the \`-f\` flag: \`mvn -f ingest-service/pom.xml ...\`.

### Issue 2: Dependency Resolution Failure (Local Repo)
**Error:** \`[ERROR] Could not find artifact com.example.realtime:common:jar:0.1.0-SNAPSHOT\`
**Context:** Building \`ingest-service\`, which depends on the \`common\` module.
**Cause:** Maven looks for dependencies in the Local Repository (\`~/.m2/repository\`), not the local file system source folders. The \`common\` module had not been \`install\`ed to the local repository yet.
**Resolution:**
1.  Run \`mvn install\` on the parent POM (to make the parent available).
2.  Run \`mvn install\` on the \`common\` module (to package it as a JAR and copy it to the local repo).
3.  Re-run the build for \`ingest-service\`.

---
EOF