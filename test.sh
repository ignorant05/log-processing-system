#!/bin/sh

echo "Testing script starts now..."

echo ""
echo "Building Docker image..."
docker compose build

echo ""
echo "Starting containers..."
docker compose down -v
docker compose up -d

echo ""
echo "Waiting for kafka to be ready..."
sleep 5

echo ""
echo "Creating topic"
docker compose exec klog-cli java -jar klog.jar topic create -n logs -p 3

echo ""
echo "Listing topic"
docker compose exec klog-cli java -jar klog.jar topic list

echo ""
echo "Describing topic"
docker compose exec klog-cli java -jar klog.jar topic describe -n logs 

echo ""
echo "Generating logs..."
docker compose exec klog-cli java -jar klog.jar generate \
  -b kafka:9092 \
  -t logs \
  -c 10 \
  -i 1000

echo ""
echo "Consuming logs..."
docker compose exec klog-cli java -jar klog.jar consume \
  -b kafka:9092 \
  -t logs

echo ""
echo "Deleting topic"
docker compose exec klog-cli java -jar klog.jar topic delete -n logs 

echo ""
echo "Listing topic"
docker compose exec klog-cli java -jar klog.jar topic list

echo ""
echo "Cleaning up..."
docker compose down -v

echo "Test completed!"
