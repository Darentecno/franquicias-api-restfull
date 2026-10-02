variable "project_name" {
  description = "Prefijo para los recursos locales de Docker."
  type        = string
  default     = "franquicias"
}

variable "mysql_image_tag" {
  description = "Tag de la imagen oficial de MySQL."
  type        = string
  default     = "8.4"
}

variable "mysql_port" {
  description = "Puerto de MySQL publicado en el host."
  type        = number
  default     = 3307
}

variable "database_name" {
  description = "Nombre de la base local."
  type        = string
  default     = "franquicias_db"
}

variable "database_root_password" {
  description = "Contraseña root de MySQL local. Cambiar antes de desplegar."
  type        = string
  sensitive   = true
  default     = "test"
}