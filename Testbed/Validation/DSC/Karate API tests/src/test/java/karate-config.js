function fn() {
  var env = karate.env; // get system property 'karate.env'
  karate.log('karate.env system property was:', env);
  if (!env) {
    env = 'dev';
  }
  var config = {
    env: env,
    myVarName: 'someValue'
  }
  if (env == 'dev') {
    // customize
    // e.g. config.foo = 'bar';
    var baseurl="https://localhost:8080/";
    var baseuser='{"userName":"admin","userPassword":"password"}';
  } else if (env == 'e2e') {
    // customize
    var baseurl="https://localhost:8080/";
    var baseuser='{"userName":"admin","userPassword":"password"}';
  }
  var config ={
    envurl:baseurl,
    envuser:baseuser
  }
  return config;
}