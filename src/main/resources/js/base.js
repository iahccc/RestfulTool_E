client = {
    global : {
        variables: {},
        set: function(name, value) {
            this.variables[name] = value;
        },
        get: function(name) {
            return this.variables[name];
        },
        isEmpty: function() {
            return Object.keys(this.variables).length == 0;
        },
        clear: function(name) {
            delete this.variables[name];
        },
        clearAll: function() {
            this.variables = {};
        }
    },
    test: function(testName, func) {

    },
    assert: function(condition, message) {
        // todo
    },
    log: function(text) {
        // todo
    },
    exit: function() {
        // todo
    }
}

response = {
    body: null,
    headers: {
        _data: null,
        valueOf: function(headerName) {
            if(this._data[headerName] && this._data[headerName].length > 0) {
                return this._data[headerName][0];
            }
        },
        valuesOf: function(headerName) {
            return this._data[headerName];
        }
    },
    status: 0,
    contentType: null
}

function getGlobalVariables() {
    return JSON.stringify(client.global.variables);
}
function setGlobalVariables(variables) {
    client.global.variables = variables;
}
function setResponseBody(body) {
    try{
        response.body = JSON.parse(body);
    } catch(e) {
        response.body = body;
    }
}

function setResponseHeaders(headers) {
    try{
        response.headers._data = JSON.parse(headers);
    } catch(e) {
        response.headers._data = headers;
    }
}
