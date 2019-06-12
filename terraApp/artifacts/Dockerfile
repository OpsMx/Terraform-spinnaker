FROM opsmx11/java:14.04-openjdk-8-jdk
ENV TERRAFORM_VERSION=0.11.11
RUN apt-get update && apt-get install git -y && apt-get install curl -y && apt-get install openssh-client -y && apt-get install gnupg -y && apt-get install unzip -y
RUN mkdir /home/terraform && mkdir /home/terraform/provider && mkdir /home/terraform/logs
#COPY sample.tf /home/terraform
COPY terraApp.jar /home/terraform
COPY exeTerraformCmd.sh /home/terraform
COPY terraform_service.sh /home/terraform
WORKDIR /home/terraform
RUN echo Building image for Terraform ${TERRAFORM_VERSION} && curl https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip > terraform_${TERRAFORM_VERSION}_linux_amd64.zip && unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip -d /bin && rm -f terraform_${TERRAFORM_VERSION}_linux_amd64.zip
CMD ["/home/terraform/terraform_service.sh"]
