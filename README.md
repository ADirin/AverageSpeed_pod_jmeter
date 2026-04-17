Average Speed – JMeter Performance Testing
This repository demonstrates how to run Apache JMeter performance tests against the Average Speed REST API using IntelliJ IDEA.
The JMeter test plan (avg_speed_test.jmx) is executed in non‑GUI mode to generate a performance report suitable for analysis and CI/CD pipelines.

🔗 API Under Test
GET /api/avgSpeed?distance=100&time=2

Example response:

{
"distance": 100.0,
"time": 2.0,
"averageSpeed": 50.0
}

📁 Project Structure
AverageSpeed/
├── avg_speed_test.jmx
├── Dockerfile
├── pom.xml
├── Jenkinsfile
└── results/
⚠️ The file avg_speed_test.jmx must exist in the project root unless paths are adjusted.

🚀 Step 1: Verify the Application Is Running
Check that your Kubernetes pod is running:

kubectl get pods -n avgspeed
Expected output:
avgspeed2-app-xxxxx   1/1   Running

🔁 Step 2: Port‑Forward the Application
Expose the application locally so JMeter can access it:
kubectl port-forward -n avgspeed deploy/avgspeed2-app 8082:8082
✅ Keep this terminal open while testing.

Open a browser and visit:

http://localhost:8082/

📄 Step 3: Confirm the JMX File Exists
Navigate to the project root, i have in the jmeter folder:
c\...\Week6_Testing\AverageSpeed

Verify the JMX file:
dir avg_speed_test.jmx
You should see:
avg_speed_test.jmx

▶️ Step 6: Run JMeter (Non‑GUI Mode – Recommended)
Execute the following command from the project root:

> jmeter -t jmeter\avg_speed_test.jmx -l results\results.jtl -e -o result\report

🔍 Command explanation
 -n    Run JMeter without GUI
-t     Path to JMeter test plan   
-l     Raw results file (.jtl)
-e -o  Generate HTML performance report

📊 Step 7: View the Performance Report
Open in a browser:
results/report/index.html

The report includes:

Response time charts
Throughput (requests/second)
Error rate
Percentiles (90 / 95 / 99)
Latency distribution

🧪 Step 8: Run JMeter GUI (Debug Only)
Use GUI mode only for debugging, not performance testing.

> jmeter

Then:

File → Open → avg_speed_test.jmx
Click ▶ Start
Add listeners such as View Results Tree

🛠 Troubleshooting
❌ JMX file not found

Ensure avg_speed_test.jmx exists
Ensure you are running the command from the correct directory
Or use the full path:

> jmeter -n -t C:\full\path\avg_speed_test.jmx
⚙️ Test Configuration Summary

SettingValue
Users: 20 concurrent
Ramp‑up: 10 
secondsLoops: 10
Request: Method GET
Endpoint: /api/avgSpeed
✅ Safe for local Kubernetes clusters
✅ Suitable for load testing experiments

📝 Notes for Reports / Assignments
You may include the following statement:

Apache JMeter was executed from IntelliJ IDEA using the integrated terminal in non‑GUI mode. 
A load test was performed against the Average Speed REST API, and performance metrics such as response time, throughput, and error rate were analyzed using the generated HTML report.