resource "aws_ecr_repository" "app" {
  name                 = "${var.project_name}-ecr"
  image_tag_mutability = "MUTABLE"
}
