# Faker - Simple Fake API Server
[![Build Status](https://travis-ci.org/dotronglong/faker.svg?branch=master)](https://travis-ci.org/dotronglong/faker)

## Installation

```bash
bash -c "$(curl -sSL https://raw.githubusercontent.com/dotronglong/faker/master/install.sh)"
```

## Getting Started

Create a folder `mocks` and put below content to file `mocks/users.json`

```json
{
  "plugins": {
    "cors": true
  },
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

Next, run this command to start faker

```bash
faker -s ./mocks
```

It will parse source folder and start an application on port 3030

Use `curl` to test the result

```bash
curl http://localhost:3030/v1/users
# [{"id":1,"name":"John"},{"id":2,"name":"Marry"}]
```

## Links

* [Documentation](https://github.com/dotronglong/faker/wiki)
