#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SUBMISSION_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
DEFAULT_ACCEPTANCE_TESTS_DIR="$(cd "${SUBMISSION_DIR}/.." && pwd)/senior-accounting-officer-acceptance-tests"

SM_PROFILE="${SM_PROFILE:-SAO_ALL}"
SM_SERVICE_TO_STOP="${SM_SERVICE_TO_STOP:-SENIOR_ACCOUNTING_OFFICER_SUBMISSION_FRONTEND}"
APP_PORT="${APP_PORT:-10058}"
APP_HEALTH_URL="${APP_HEALTH_URL:-http://localhost:${APP_PORT}/ping/ping}"
ACCEPTANCE_TESTS_DIR="${ACCEPTANCE_TESTS_DIR:-${DEFAULT_ACCEPTANCE_TESTS_DIR}}"
ACCEPTANCE_TEST_SCRIPT="${ACCEPTANCE_TEST_SCRIPT:-}"
APP_LOG="${APP_LOG:-${SUBMISSION_DIR}/logs/local-acceptance-app.log}"
STARTUP_TIMEOUT_SECONDS="${STARTUP_TIMEOUT_SECONDS:-180}"

APP_PID=""

usage() {
  cat <<EOF
Usage: $(basename "$0") [browser] [environment]

Starts service-manager dependencies, runs this submission frontend locally,
then runs the SAO acceptance tests.

Environment overrides:
  SM_PROFILE                 Default: ${SM_PROFILE}
  SM_SERVICE_TO_STOP         Default: ${SM_SERVICE_TO_STOP}
  APP_PORT                   Default: ${APP_PORT}
  APP_HEALTH_URL             Default: ${APP_HEALTH_URL}
  ACCEPTANCE_TESTS_DIR       Default: ${ACCEPTANCE_TESTS_DIR}
  ACCEPTANCE_TEST_SCRIPT     Default: run_local_tests.sh if present, otherwise run_submission_ui_tests.sh
  STARTUP_TIMEOUT_SECONDS    Default: ${STARTUP_TIMEOUT_SECONDS}

Examples:
  scripts/run_acceptance_tests.sh
  scripts/run_acceptance_tests.sh chrome local
  ACCEPTANCE_TEST_SCRIPT=run_all_tests.sh scripts/run_acceptance_tests.sh
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Required command not found: $1" >&2
    exit 1
  fi
}

is_app_healthy() {
  curl --fail --silent --show-error --max-time 2 "${APP_HEALTH_URL}" >/dev/null 2>&1
}

cleanup() {
  if [[ -n "${APP_PID}" ]] && kill -0 "${APP_PID}" >/dev/null 2>&1; then
    echo "Stopping local submission frontend (pid ${APP_PID})..."
    kill "${APP_PID}" >/dev/null 2>&1 || true
    wait "${APP_PID}" >/dev/null 2>&1 || true
  fi
}

trap cleanup EXIT

require_command sm2
require_command sbt
require_command curl

if [[ ! -d "${ACCEPTANCE_TESTS_DIR}" ]]; then
  echo "Acceptance-tests repo not found: ${ACCEPTANCE_TESTS_DIR}" >&2
  echo "Set ACCEPTANCE_TESTS_DIR to the checkout path." >&2
  exit 1
fi

if [[ -z "${ACCEPTANCE_TEST_SCRIPT}" ]]; then
  if [[ -x "${ACCEPTANCE_TESTS_DIR}/run_local_tests.sh" ]]; then
    ACCEPTANCE_TEST_SCRIPT="run_local_tests.sh"
  else
    ACCEPTANCE_TEST_SCRIPT="run_submission_ui_tests.sh"
  fi
fi

if [[ ! -x "${ACCEPTANCE_TESTS_DIR}/${ACCEPTANCE_TEST_SCRIPT}" ]]; then
  echo "Acceptance test script is not executable: ${ACCEPTANCE_TESTS_DIR}/${ACCEPTANCE_TEST_SCRIPT}" >&2
  exit 1
fi

mkdir -p "$(dirname "${APP_LOG}")"

echo "Starting service-manager profile ${SM_PROFILE}..."
sm2 --start "${SM_PROFILE}"

echo "Stopping service-manager copy of ${SM_SERVICE_TO_STOP}..."
sm2 --stop "${SM_SERVICE_TO_STOP}" || true

if is_app_healthy; then
  echo "Submission frontend is already responding at ${APP_HEALTH_URL}; leaving it running."
else
  echo "Starting local submission frontend from ${SUBMISSION_DIR}..."
  (
    cd "${SUBMISSION_DIR}"
    sbt run
  ) >"${APP_LOG}" 2>&1 &
  APP_PID="$!"

  echo "Waiting for ${APP_HEALTH_URL} (log: ${APP_LOG})..."
  deadline=$((SECONDS + STARTUP_TIMEOUT_SECONDS))
  until is_app_healthy; do
    if ! kill -0 "${APP_PID}" >/dev/null 2>&1; then
      echo "Local submission frontend exited before becoming healthy. Last log lines:" >&2
      tail -n 80 "${APP_LOG}" >&2 || true
      exit 1
    fi

    if (( SECONDS >= deadline )); then
      echo "Timed out waiting for local submission frontend. Last log lines:" >&2
      tail -n 80 "${APP_LOG}" >&2 || true
      exit 1
    fi

    sleep 2
  done
fi

echo "Running ${ACCEPTANCE_TEST_SCRIPT} from ${ACCEPTANCE_TESTS_DIR}..."
(
  cd "${ACCEPTANCE_TESTS_DIR}"
  "./${ACCEPTANCE_TEST_SCRIPT}" "$@"
)
