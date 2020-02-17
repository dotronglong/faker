#!/usr/bin/env node
const fs = require("fs");
const https = require("https");
const { execFile } = require("child_process");
const homedir = require('os').homedir();
const chalk = require('chalk');
const cliProgress = require('cli-progress');

const info = (msg) => console.log(chalk.cyan(`[INFO] ${msg}`));
const warn = (msg) => console.log(chalk.yellow(`[WARN] ${msg}`));
const exit = (msg) => process.exit(console.log(chalk.red(`[EXIT] ${msg}`)));

const requestHeaders = {
  "User-Agent": `NodeJS/${process.version}`
};

const options = {
  dir: `${homedir}/.faker`,
  bin: "",
  install_dir: "",

  /* Faker Options */
  version: "latest",
  source: null,
  watch: false,
  port: 3030
};

async function validate() {
  await isJavaInstalled();
  if (options.source === null) {
    warn("source is not specified. Use -s or --source to specify source folder");
    warn("current directory will be used as source");
    options.source = __dirname;
  }
}

const isJavaInstalled = async () => {
  return new Promise((resolve, reject) => {
    execFile('java', ['-version'], (error, stdout, stderr) => {
      if (error) {
        return reject(new Error("Java is not installed"));
      }
      const version = stderr.split("\n")[0].replace("java version ", "").replace(/"/g, "");
      info(`java: ${version}`);
      resolve();
    });
  });
};

const help = () => {
  console.log("usage: fakerio [-options] [args...]");
  console.log("  -v | --version    specify faker version");
  console.log("  -s | --source     path to source folder");
  console.log("  -p | --port       specify port");
  console.log("  -w | --watch      enable source watching");
  console.log("  -h | --help       show usage");
  console.log("\n\nExample: fakerio -s ./mocks -p 3030 --watch");
  process.exit(0);
};

const parse = () => {
  const [, , ...args] = process.argv;
  for (let i = 0; i < args.length; i++) {
    switch (args[i]) {
      case "-v":
      case "--version":
        options.version = args[++i];
        break;

      case "-s":
      case "--source":
        options.source = args[++i];
        break;

      case "-p":
      case "--port":
        options.port = args[++i];
        break;

      case "-w":
      case "--watch":
        options.watch = true;
        break;

      case "-h":
      case "--help":
        help();
        return;   

      default:
        warn(`${args[i]} is not supported`);
        break;
    }
  }
};

const readResponseBody = (response, parse) => {
  return new Promise((resolve, reject) => {
    response.setEncoding('utf8');
    let rawData = '';
    response.on('data', (chunk) => { rawData += chunk; });
    response.on('end', () => {
      try {
        resolve(parse === undefined || parse ? JSON.parse(rawData) : rawData);
      } catch (e) {
        reject(e);
      }
    });
  });
};

const getLatestVersion = () => {
  return new Promise((resolve, reject) => {
    https.get({
      hostname: "api.github.com",
      path: "/repos/dotronglong/faker/releases/latest",
      headers: requestHeaders
    }, async (response) => {
      const { statusCode } = response;
      const contentType = response.headers['content-type'];

      let error;
      if (statusCode !== 200) {
        error = new Error(`Request Failed. Status Code: ${statusCode}`);
      } else if (!/^application\/json/.test(contentType)) {
        error = new Error(`Invalid content-type. Expected application/json but received ${contentType}`);
      }

      if (error) {
        return reject(error);
      }

      try {
        const body = await readResponseBody(response);
        resolve(body.tag_name.replace('v', ''));
      } catch (e) {
        reject(e);
      }
    }).on('error', reject).end();
  });
};

const downloadFaker = () => {
  return new Promise((resolve, reject) => {
    info("downloading faker ...");
    const file = fs.createWriteStream(`${options.install_dir}/faker.jar`);
    https.get({
      host: 'github.com',
      path: `/dotronglong/faker/releases/download/v${options.version}/faker.jar`,
      headers: requestHeaders
    }, async (response) => {
      if (response.statusCode === 302 && typeof response.headers.location === 'string') {
        downloadFile(response.headers.location, file).then(resolve).catch(reject);
      } else {
        reject(new Error(`Request Failed. Status Code: ${response.statusCode}`));
      }
    });
  });
};

const downloadFile = (url, file) => {
  return new Promise((resolve, reject) => {
    https.get(url, (response) => {
      if (response.statusCode !== 200) {
        reject(new Error(`Request Failed. Status Code: ${response.statusCode}`));
      } else {
        const size = parseInt(response.headers['content-length'], 10);
        let downloaded = 0;
        const progress = new cliProgress.SingleBar({}, cliProgress.Presets.shades_classic);
        progress.start(100, 0);
        response.on('data', function (chunk) {
          file.write(chunk);
          downloaded += chunk.length;
          progress.update((100.0 * downloaded / size).toFixed(0));
        }).on('end', () => {
          file.end();
          progress.stop();
          resolve();
        });
      }
    });
  });
};

const install = () => {
  return new Promise(async (resolve, reject) => {
    try {
      if (options.version === 'latest') {
        options.version = await getLatestVersion();
      }
      if (options.version === undefined) {
        exit('version must be specified');
      }

      info(`faker: ${options.version}`);
      if (!fs.existsSync(options.dir)) {
        fs.mkdirSync(options.dir, 0744);
      }

      options.install_dir = `${options.dir}/${options.version}`;
      if (!fs.existsSync(options.install_dir)) {
        fs.mkdirSync(options.install_dir, 0744);
      }

      options.bin = `${options.install_dir}/faker.jar`;
      if (!fs.existsSync(options.bin)) {
        await downloadFaker();
      }
      resolve();
    } catch (e) {
      warn(`error is occurred, trying to delete ${options.bin}`);
      fs.unlinkSync(options.bin);
      reject(e);
    }
  });
};

const run = () => {
  return new Promise(async (resolve, reject) => {
    const cmd = await execFile('java', [
      `-Dfaker.source=${options.source}`,
      `-Dfaker.watch=${options.watch}`,
      `-Dserver.port=${options.port}`,
      '-jar', options.bin
    ]);
    if (cmd.error) {
      return reject(cmd.error);
    }
    cmd.stdout.on('data', function (output) {
      console.log(output.toString());
    });
    resolve();
  });
};

const main = async () => {
  try {
    parse();
    await validate();
    await install();
    await run();
  } catch (e) {
    exit(e.message);
  }
};

main();