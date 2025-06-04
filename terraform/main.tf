# terraform/main.tf
provider "aws" {
  region = var.aws_region
}

resource "aws_instance" "config_service" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  # Add other configurations as needed
}

resource "aws_instance" "discovery_service" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  # Add other configurations as needed
}

resource "aws_instance" "gateway_service" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  # Add other configurations as needed
}

resource "aws_instance" "chat_service" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  depends_on = [aws_instance.config_service]
  # Add other configurations as needed
}

resource "aws_instance" "notification_service" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  # Add other configurations as needed
}

resource "aws_instance" "payment_service" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  # Add other configurations as needed
}

resource "aws_instance" "user_service" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  # Add other configurations as needed
}

