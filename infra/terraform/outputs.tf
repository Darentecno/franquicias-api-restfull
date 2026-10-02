output "r2dbc_url" {
  description = "URL R2DBC para la base local de franquicias."
  value       = "r2dbc:mysql://localhost:${var.mysql_port}/${var.database_name}"
}