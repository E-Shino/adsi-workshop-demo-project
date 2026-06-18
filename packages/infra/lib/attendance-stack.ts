import * as cdk from "aws-cdk-lib/core";
import { Construct } from "constructs";

interface AttendanceStackProps extends cdk.StackProps {
  envName: string;
}

export class AttendanceStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: AttendanceStackProps) {
    super(scope, id, props);

    cdk.Tags.of(this).add("Project", "attendance");
    cdk.Tags.of(this).add("Environment", props.envName);

    // TODO: VPC, ECS, RDS, ALB などのリソースを要件定義後に追加
  }
}
