# Faker - Simple Fake API Server

## How to use

Simply run below code

```bash
sh -c "$(curl -sSL https://raw.githubusercontent.com/dotronglong/faker/master/faker.sh)" -s --source /path/to/your/source
```

It will parse source folder and start an application on port 3030

## Write JSON specification

```json
{
  "request": {
    "method": "GET",
    "path": "/v1/users"
  },
  "response": {
    "body": [
      { "id": 1, "name": "John" },
      { "id": 2, "name": "Marry" }
    ]
  }
}
```