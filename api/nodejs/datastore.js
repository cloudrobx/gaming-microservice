const Datastore = require('@google-cloud/datastore');
//var config = require ('./config.js');
// Your Google Cloud Platform project ID
const projectId = 'gaming-microservice';

// Creates a client
const datastore = new Datastore({
  projectId: projectId,
});

//[START datastore_get_entity]
function getUser(userKey, callback) {
 datastore.get(userKey)
  .then(function(data) {
    var entity = data[0];
    return callback(entity);
  })
  .catch(err => {
  	return callback(err);
  });	
}
//[END datastore_get_entity]

// [START datastore_add_entity]
function addUser(userKey, payload, callback) {

  const entity = {
    key: userKey,
    data: payload
  };

  datastore
    .save(entity)
    .then(() => {
      console.log(`User ${userKey.id} created successfully.`);
      return callback(`User created successfully.`);
    })
    .catch(err => {
      console.error('ERROR:', err);
      return callback(err);
    });
}
// [END datastore_add_entity]


function deleteUser(userKey, callback) {

  datastore
    .delete(userKey)
    .then(() => {
      return callback(`User deleted successfully.`);
    })
    .catch(err => {
      console.error('ERROR:', err);
      return callback(err);
    });
}
// [END datastore_delete_entity]

// [START datastore_retrieve_entities]
function listUsers() {
  const query = datastore.createQuery('User').order('created');

  datastore
    .runQuery(query)
    .then(results => {
      const users = results[0];

//      console.log('Users:');
      users.forEach(user => {
        const userKey = user[datastore.KEY];
//        console.log(userKey.id, user);
      });
    })
    .catch(err => {
      console.error('ERROR:', err);
      
    });
}
// [END datastore_retrieve_entities]

// [START datastore_delete_entity]

module.exports = {
  addUser: addUser,
  deleteUser: deleteUser,
  listUsers: listUsers,
  getUser: getUser
};