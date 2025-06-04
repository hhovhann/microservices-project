resource "aws_ecs_cluster" "app_cluster" {
  name = "${var.project_name}-cluster"
}
