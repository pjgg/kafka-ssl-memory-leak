#!/bin/bash
set -o nounset \
    -o errexit

echo "Cleaning secrets ..."
rm -rf secrets
mkdir secrets
mkdir -p tmp

# Generate CA key
echo "Creating CA..."
openssl req -new -x509 -keyout tmp/redhat-ca.key -out tmp/redhat-ca.crt -days 365 -subj '/CN=redhat/OU=QuarkusQE/O=redhat/L=Madrid/C=es' -passin pass:password -passout pass:password >/dev/null 2>&1
echo "CN=redhat"
echo "OU=QuarkusQE"
echo "O=redhat"
echo "L=Madrid"
echo "L=es"
echo "password: password"

for i in 'broker' 'producer' 'consumer'
do
	echo "Creating cert and keystore of $i..."
	# Create keystores
	keytool -genkey -noprompt \
				 -alias $i \
				 -dname "CN=$i, OU=QuarkusQE, O=redhat, L=Madrid, C=es" \
				 -keystore secrets/$i.keystore.jks \
				 -keyalg RSA \
				 -storepass password \
				 -keypass password  >/dev/null 2>&1

	# Create CSR, sign the key and import back into keystore
	keytool -keystore secrets/$i.keystore.jks -alias $i -certreq -file tmp/$i.csr -storepass password -keypass password >/dev/null 2>&1

	openssl x509 -req -CA tmp/redhat-ca.crt -CAkey tmp/redhat-ca.key -in tmp/$i.csr -out tmp/$i-ca-signed.crt -days 365 -CAcreateserial -passin pass:password  >/dev/null 2>&1

	keytool -keystore secrets/$i.keystore.jks -alias CARoot -import -noprompt -file tmp/redhat-ca.crt -storepass password -keypass password >/dev/null 2>&1

	keytool -keystore secrets/$i.keystore.jks -alias $i -import -file tmp/$i-ca-signed.crt -storepass password -keypass password >/dev/null 2>&1

	# Create truststore and import the CA cert.
	keytool -keystore secrets/$i.truststore.jks -alias CARoot -import -noprompt -file tmp/redhat-ca.crt -storepass password -keypass password >/dev/null 2>&1
done

echo "password" > secrets/cert_creds
rm -rf tmp

echo "SUCCEEDED"
