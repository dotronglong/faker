# Faker - Simple Fake API Server
[![Run tests on PRs](https://github.com/dotronglong/faker/actions/workflows/main.yml/badge.svg)](https://github.com/dotronglong/faker/actions/workflows/main.yml)

## Installation

```bash
bash -c "$(curl -sSL https://raw.githubusercontent.com/dotronglong/faker/master/install.sh)"
```

## Getting Started

Create a folder `mocks` and put below content to file `mocks/users.json`

```json
{
  "plugins": [
    {
      "name": "list",
      "args": {
        "count": 5,
        "item": {
          "id": "#random:int:min=1&max=1000#",
          "name": "#random:name#",
          "created_at": "#timestamp#",
          "updated_at": "#timestamp#"
        }
      }
    },
    {"name": "random"},
    {"name": "timestamp"}
  ],
  "request": {
    "method": "GET",
    "path": "/v1/users"
  },
  "response": {
    "body": []
  }
}
```

Next, run this command to start faker

```bash
fakerio -s ./mocks
```

It will parse source folder and start an application on port 3030

Use `curl` to test the result

```bash
curl http://localhost:3030/v1/users
```

```json
[
  {
    "id": 105,
    "name": "Jesse Ortega",
    "created_at": 1581691318567,
    "updated_at": 1581691318567
  },
  {
    "id": 561,
    "name": "Steven Peterson",
    "created_at": 1581691318567,
    "updated_at": 1581691318567
  },
  {
    "id": 779,
    "name": "John Adams",
    "created_at": 1581691318567,
    "updated_at": 1581691318567
  },
  {
    "id": 768,
    "name": "Joseph Watson",
    "created_at": 1581691318567,
    "updated_at": 1581691318567
  },
  {
    "id": 196,
    "name": "David Howell",
    "created_at": 1581691318567,
    "updated_at": 1581691318567
  }
]
```

## Links

* [Run with NodeJS](https://github.com/dotronglong/faker/wiki/Getting-Started-%5BNodeJS%5D)
* [Run with Docker](https://github.com/dotronglong/faker/wiki/Getting-Started-%5BDocker%5D)
* [Run with Vagrant](https://github.com/dotronglong/faker/wiki/Getting-Started-%5BVagrant%5D)
* [Documentation](https://github.com/dotronglong/faker/wiki)
