# create
curl -s -X POST http://127.0.0.1:8000/query \
  -H 'Content-Type: application/json' \
  -d '{"query":"What is the population of United States?"}'

echo "\n==============================================\n"

# list
#curl -s http://127.0.0.1:8000/orders

#echo "\n==============================================\n"

# get by id (example id=1)
#curl -s http://127.0.0.1:8000/orders/1

#echo "\n==============================================\n"