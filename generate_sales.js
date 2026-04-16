const fs = require('fs');

const products = [
  { id: 1, price: 12.00 }, { id: 2, price: 10.00 }, { id: 3, price: 5.00 }, { id: 4, price: 14.00 },
  { id: 5, price: 16.00 }, { id: 6, price: 85.00 }, { id: 7, price: 65.00 }, { id: 8, price: 120.00 },
  { id: 9, price: 95.00 }, { id: 10, price: 45.00 }, { id: 11, price: 52.00 }, { id: 12, price: 38.00 }
];

let yamlText = `\n  - changeSet:\n      id: 03-insert-mock-sales-history\n      author: antigravity\n      context: homolog\n      changes:\n`;

let saleId = 6;
// Generates roughly 20 sales over the last 20 days
for (let i = 20; i >= 1; i--) {
  const customerId = Math.floor(Math.random() * 4) + 1;
  const date = new Date(2026, 3, 15 - i, 12 + Math.floor(Math.random() * 8), Math.floor(Math.random() * 59));
  const dateStr = date.toISOString().replace('T', ' ').substring(0, 19);

  const numItems = Math.floor(Math.random() * 3) + 1; // 1 to 3 items
  let saleTotal = 0;
  
  let itemsYaml = '';
  // generate items first to calculate total
  for (let j = 0; j < numItems; j++) {
    const prod = products[Math.floor(Math.random() * products.length)];
    const qty = Math.floor(Math.random() * 2) + 1;
    const totalItem = prod.price * qty;
    saleTotal += totalItem;

    itemsYaml += `        - insert:\n            tableName: tb_item_sales\n            columns:\n              - column: { name: sale_id, valueNumeric: "${saleId}" }\n              - column: { name: product_id, valueNumeric: "${prod.id}" }\n              - column: { name: quantity, valueNumeric: "${qty}" }\n              - column: { name: unit_value, valueNumeric: "${prod.price.toFixed(2)}" }\n              - column: { name: discount, valueNumeric: "0.00" }\n              - column: { name: total_value, valueNumeric: "${totalItem.toFixed(2)}" }\n              - column: { name: created_at, value: "${dateStr}" }\n              - column: { name: updated_at, value: "${dateStr}" }\n`;
  }

  const discount = (Math.random() > 0.8) ? (saleTotal * 0.1).toFixed(2) : '0.00';
  const finalValue = (saleTotal - Number(discount)).toFixed(2);

  yamlText += `        # Venda ${saleId}\n        - insert:\n            tableName: tb_sales\n            columns:\n              - column: { name: customer_id, valueNumeric: "${customerId}" }\n              - column: { name: discount, valueNumeric: "${discount}" }\n              - column: { name: total_value, valueNumeric: "${finalValue}" }\n              - column: { name: created_at, value: "${dateStr}" }\n              - column: { name: updated_at, value: "${dateStr}" }\n`;
  yamlText += itemsYaml;

  saleId++;
}

fs.appendFileSync('src/main/resources/db/changelog/changesets/02-insert-homolog-data.yaml', yamlText);
console.log('20 sales generated and appended.');
