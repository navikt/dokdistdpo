#!/usr/bin/env sh

if test -f "$NAV_VIRKSOMHETSSERTIFIKAT_CREDENTIALS"
then
    echo "Setting virksomhetssertifikat_alias"
    export virksomhetssertifikat_alias="$(cat $NAV_VIRKSOMHETSSERTIFIKAT_CREDENTIALS | jq -r '.alias')"
    echo "Setting virksomhetssertifikat_password"
    export virksomhetssertifikat_password="$(cat $NAV_VIRKSOMHETSSERTIFIKAT_CREDENTIALS | jq -r '.password')"
    echo "Setting virksomhetssertifikat_type"
    export virksomhetssertifikat_type="$(cat $NAV_VIRKSOMHETSSERTIFIKAT_CREDENTIALS | jq -r '.type')"
fi

if test -f "$NAV_VIRKSOMHETSSERTIFIKAT_KEY"
then
    echo "Setting virksomhetssertifikat_path"
    export virksomhetssertifikat_path="file://$NAV_VIRKSOMHETSSERTIFIKAT_KEY"
fi

if test -f /secrets/serviceuser/srvdokdistdpo/username;
then
    echo "Setting dokdistdpo_serviceuser_username"
    export  dokdistdpo_serviceuser_username=$(cat /secrets/serviceuser/srvdokdistdpo/username)
fi
if test -f /secrets/serviceuser/srvdokdistdpo/password;
then
    echo "Setting dokdistdpo_serviceuser_password"
    export  dokdistdpo_serviceuser_password=$(cat /secrets/serviceuser/srvdokdistdpo/password)
fi

if test -f /var/run/secrets/nais.io/dokdistdpo/GOOGLE_APPLICATION_CREDENTIALS
then
    echo "Setting GOOGLE_APPLICATION_CREDENTIALS"
    export GOOGLE_APPLICATION_CREDENTIALS=/var/run/secrets/nais.io/dokdistdpo/GOOGLE_APPLICATION_CREDENTIALS
fi

if test -f /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_dpo_password
then
    echo "Setting dokdistdpo_dpo_password"
    export dokdistdpo_dpo_password=$(cat /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_dpo_password)
fi

if test -f /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_dpo_username
then
    echo "Setting dokdistdpo_dpo_username"
    export dokdistdpo_dpo_username=$(cat /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_dpo_username)
fi

if test -f /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_dpo_scope
then
    echo "Setting dokdistdpo_dpo_scope"
    export dokdistdpo_dpo_scope=$(cat /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_dpo_scope)
fi

if test -f /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_dpo_clientid
then
    echo "Setting dokdistdpo_dpo_clientid"
    export dokdistdpo_dpo_clientid=$(cat /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_dpo_clientid)
fi

if test -f /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_qdist015_autostartup; then
  echo "Setting dokdistdpo_qdist015_autostartup"
  export dokdistdpo_qdist015_autostartup=$(cat /var/run/secrets/nais.io/dokdistdpo/dokdistdpo_qdist015_autostartup)
fi
