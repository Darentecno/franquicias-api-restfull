terraform {
  required_version = ">= 1.5.0"
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
  }
}

provider "docker" {}

resource "docker_image" "mysql" {
  name         = "mysql:${var.mysql_image_tag}"
  keep_locally = true
}

resource "docker_volume" "mysql_data" {
  name = "${var.project_name}-mysql-data"
}

resource "docker_container" "mysql" {
  name  = "${var.project_name}-mysql"
  image = docker_image.mysql.image_id

  env = [
    "MYSQL_DATABASE=${var.database_name}",
    "MYSQL_ROOT_HOST=%",
    "MYSQL_ROOT_PASSWORD=${var.database_root_password}",
  ]

  ports {
    internal = 3306
    external = var.mysql_port
  }

  volumes {
    volume_name    = docker_volume.mysql_data.name
    container_path = "/var/lib/mysql"
  }
}