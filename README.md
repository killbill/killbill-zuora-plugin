
What is it?
-----------

The killbill-zuora-plugin is a Killbill payment plugin that allows to interact with zuora as a payment gateway. Its has been tested with
the version 27.0 of the zuora API.

Setup
-----

1. Add zuora api jar in the pom

Zuora API is not public, and they provide you with an xml configuration of their API that can be used to generate java classes.

In order to use that plugin you need to have a jar of the zuora API; you can export environment variables that the pom will read
to specify the details of the artifact:
zuora_api_groupId=XXX
zuora_api_artifactId=XXX
zuora_api_version=XXX

Another option is to edit the pom directly.


2. Custom Fields

The plugin makes use of a specific custom field (a 128 long string), called 'Killbill' for the following zuora objects:
* Subscription
* Invoice

In order to use that plugin, zuora needs to be configured with those custom fields.

How that Works?
--------------

Zuora is NOT a payment gateway so the plugin tricks the API by creating oneTime subscription, followed by a bill run to generate
the invoice, and finally a payment associated to the invoice generated. The payment will interract with the payment gateway
that zuora uses to really make the payments.


Tests
-----

In order to run the tests against the zuora sandbox, you must specify the 3 system properties:
* killbill.payment.provider.test-zuora.userName=XXX
* killbill.payment.provider.test-zuora.password=XXX
* Dkillbill.payment.provider.test-zuora.url=https://apisandbox.zuora.com/apps/services/a/27.0
