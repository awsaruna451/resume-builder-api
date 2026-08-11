# resume-builder-ui

Helm chart for the resume-builder frontend. Converts `ui-deployment.yaml`
into a templated chart:

- Deployment (image, probes, resources — all templated)
- Service (ClusterIP, port 80)
- Ingress — the catch-all `"/"` path only

## Scope note

The API's `/api` and `/oauth2` Ingress paths live in the separate
`resume-builder-api` chart/release, not here. nginx merges multiple Ingress
objects that share `ingressClassName: nginx`, so installing both this chart
and `resume-builder-api` into the same namespace/host works fine — you don't
need to combine them into one Ingress object.

## Install

```bash
helm install resume-builder-ui . \
  -n resume-builder --create-namespace \
  -f values-dev.yaml
```

For local minikube testing:

```bash
minikube addons enable ingress
helm install resume-builder-ui . -n resume-builder --create-namespace -f values-minikube.yaml
```

## Upgrade

```bash
helm upgrade resume-builder-ui . -n resume-builder -f values-dev.yaml
```

## Key values

| Key | Purpose |
|---|---|
| `image.repository` / `image.tag` | ECR image — pin `tag` to `$CI_COMMIT_SHORT_SHA` outside dev |
| `replicaCount` | Stateless static/SPA server — safe to scale beyond 1 |
| `ingress.paths` | Path prefixes routed to this UI's Service (default: `/`) |

Render locally without installing:

```bash
helm template resume-builder-ui . -n resume-builder -f values-dev.yaml
```
