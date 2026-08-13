{{/*
Fully qualified name
*/}}
{{- define "resume-builder-db.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-db" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "resume-builder-db.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "resume-builder-db.labels" -}}
helm.sh/chart: {{ include "resume-builder-db.chart" . }}
{{ include "resume-builder-db.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "resume-builder-db.selectorLabels" -}}
app.kubernetes.io/name: resume-builder-db
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
