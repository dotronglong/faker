# Faker - Simple Fake API Server
[![Build Status](https://travis-ci.org/dotronglong/faker.svg?branch=master)](https://travis-ci.org/dotronglong/faker)

## How to use

Simply run below code

```bash
sh -c "$(curl -sSL https://era.li/pS4p76)" -s --source /path/to/your/source
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

## Links

* [Documentation](https://github.com/dotronglong/faker/wiki)