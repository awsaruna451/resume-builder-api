# resume-builder-api

Helm chart for the resume-builder backend API. Converts these raw manifests
into a templated, reusable chart:

- `api-deployment.yaml` → Deployment, Service, PVC
- `external-secret.yaml` → ExternalSecret
- `ingress.yaml` → Ingress (API paths only — `/api`, `/oauth2`)

## Not included (cluster-scoped, shared across apps)

`cluster-secret-store.yml` (the `external-secrets` ServiceAccount + IRSA role
and the `ClusterSecretStore`) is **not** part of this chart. It's a
cluster-wide dependency other apps may also rely on, so it should be applied
once, separately — e.g. its own small chart or a plain `kubectl apply` — before
installing this release. Same reasoning for the UI's `/` ingress path: it
belongs to a separate `resume-builder-ui` release, and nginx merges multiple
Ingress objects that share `ingressClassName: nginx`, so both coexist fine.

## Install

```bash
helm install resume-builder-api . \
  -n resume-builder --create-namespace \
  -f values-dev.yaml
```

## Upgrade

```bash
helm upgrade resume-builder-api . -n resume-builder -f values-dev.yaml
```

## Key values

| Key | Purpose |
|---|---|
| `image.repository` / `image.tag` | ECR image — pin `tag` to a commit SHA outside dev |
| `replicaCount` | Keep at `1` until uploads move off the RWO PVC to S3 |
| `persistence.*` | Uploads PVC (EBS gp2, ReadWriteOnce) |
| `externalSecret.data` | Which Secrets Manager keys/properties map to which env var |
| `existingSecretName` | Secret name the Deployment reads via `envFrom` (must match `externalSecret.targetSecretName`) |
| `ingress.paths` | Path prefixes routed to this API's Service |

Render locally without installing:

```bash
helm template resume-builder-api . -n resume-builder -f values-dev.yaml
```
