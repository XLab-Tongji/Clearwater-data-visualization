# frontend18v

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Run your tests
```
npm run test
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).


### Build
```
docker build -t {dockerhub-username}/frontend .
```

### Push
```
docker login -u {dockerhub-username}
docker tag {dockerhub-username}/frontend {dockerhub-username}/frontend
docker push {dockerhub-username}/frontend
```

If there is an error, see https://github.com/aws/aws-cli/issues/3264
```
Error saving credentials: error storing credentials - err: exit status 1, out: `The user name or passphrase you entered is not correct.`
```

## Pull
```
sudo docker pull {dockerhub-username}/frontend
```

### Run
```
sudo docker run -p 9998:8080 -d {dockerhub-username}/frontend
```