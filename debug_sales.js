const axios = require('axios');

async function test() {
  try {
    // Login
    const res = await axios.post('http://localhost:8080/auth/login', {
      email: 'master@saborexpress.com',
      password: 'masterpassword'
    });
    
    // Fetch sales
    const salesRes = await axios.get('http://localhost:8080/api/sales', {
      headers: { Authorization: `Bearer ${res.data.accessToken}` }
    });
    
    if (salesRes.data.length > 0) {
       console.log('First Sale:');
       console.log(JSON.stringify(salesRes.data[0], null, 2));
    } else {
       console.log('0 sales returned by API');
    }
  } catch (e) {
    console.error(e.message);
  }
}

test();
