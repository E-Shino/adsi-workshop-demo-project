#!/usr/bin/env node
import * as cdk from "aws-cdk-lib/core";
import { AttendanceStack } from "../lib/attendance-stack";

const app = new cdk.App();
const envName = app.node.tryGetContext("env") || "dev";

new AttendanceStack(app, `Attendance-${envName}`, {
  envName,
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: process.env.CDK_DEFAULT_REGION || "ap-northeast-1",
  },
});
