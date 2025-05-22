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
