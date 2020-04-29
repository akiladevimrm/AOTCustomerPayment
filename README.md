# Checkout and Build

To checkout the project and build from source, provide the following command
```
git clone https://github.com/akiladevimrm/AOTCustomerPayment.git
```

# Pre-requisites:
```install docker-compose https://docs.docker.com/compose/install/```
```Java```

# How to start Customer Payment application
Pre-built docker image is already available in docker hub registry. 
```
$docker run -i -t --name payment_app --network=docker_my-kafka-network-1 akiladevi26/paymentapp:1.0
```
This starts the application in command line which processes the invoice data received from kafka and send the payment details to the vendor.

# Other Dependencies
Place the keystore and truststore files under resources folder.