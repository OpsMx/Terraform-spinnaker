# GitHub Action for Container Scanning

### Failing the build on policy and/or vulnerabilities
The above example will run the action but not cause the build to fail based on results. If you want to have the build step fail in cases where
there are vulnerabilities that violate the [default policy](dist/critical_security_policy.json) (fail only if vulnerabilities exist with severity >= HIGH and which have a fix available), then set the `fail-build` input = `true`.
That will make the job step fail of the policy evaluations detects a policy violation.

For example: 
```yaml
 - name: Scan image
   uses: anchore/scan-action@v1
   with:
     image-reference: "localbuild/testimage:latest"
     fail-build: true
```
For now, we are passing the image build.