const fs = require('fs');
const path = require('path');

const dir = '/app/src/main/assets/tiki_dialogues';
const files = fs.readdirSync(dir);

console.log('Dialogue files in assets:', files);

files.forEach(file => {
  if (file.endsWith('.json')) {
    const filePath = path.join(dir, file);
    const content = fs.readFileSync(filePath, 'utf8');
    const data = JSON.parse(content);
    console.log(`File: ${file}`);
    console.log(`  Version: ${data.version}`);
    console.log(`  Language: ${data.language}`);
    console.log(`  Total dialogues: ${data.dialogues.length}`);
    
    const categories = {};
    data.dialogues.forEach(d => {
      categories[d.category] = (categories[d.category] || 0) + 1;
    });
    console.log('  Categories breakdown:', categories);
  }
});
