{{/*
Expand the name of the chart.
*/}}
{{- define "log-processing-system.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "log-processing-system.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Chart label — name + version.
*/}}
{{- define "log-processing-system.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels applied to every resource.
*/}}
{{- define "log-processing-system.labels" -}}
helm.sh/chart: {{ include "log-processing-system.chart" . }}
{{ include "log-processing-system.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels (used in matchLabels and pod template labels).
*/}}
{{- define "log-processing-system.selectorLabels" -}}
app.kubernetes.io/name: {{ include "log-processing-system.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Service account name
*/}}
{{- define "log-processing-system.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "log-processing-system.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Kafka bootstrap servers 
*/}}
{{- define "log-processing-system.kafkaBootstrapServers" -}}
{{- if .Values.kafka.enabled }}
{{- printf "%s-kafka:9092" .Release.Name }}
{{- else }}
{{- .Values.externalKafka.bootstrapServers }}
{{- end }}
{{- end }}
