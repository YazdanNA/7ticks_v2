import os
import re

def scan_kotlin_files():
    results = []
    
    # We target packages that render user-facing UI, typically directories containing "presentation" or components
    for root, dirs, files in os.walk("app/src/main/java/com/example"):
        # Skip technical system modules that have internal identifiers
        if any(term in root for term in ["tts", "database", "feedback", "fsrs", "theme", "localization"]):
            continue
            
        for file in files:
            if file.endswith(".kt"):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    lines = f.readlines()
                
                for idx, line in enumerate(lines):
                    stripped = line.strip()
                    # Skip comments, imports, packages, annotations, and system logs
                    if (stripped.startswith("//") or stripped.startswith("import") or 
                        stripped.startswith("package") or stripped.startswith("Log.") or 
                        stripped.startswith("@")):
                        continue
                    
                    # Find potential hardcoded string literals inside double quotes
                    quotes = re.findall(r'\"([^\"]+)\"', line)
                    for q in quotes:
                        # Filter out common non-UI string patterns
                        if len(q) <= 1:
                            continue
                        if q.isupper() and len(q) > 2: # e.g., constants like "TAG"
                            continue
                        if q.startswith("ROUTE_") or q.startswith("route_") or '/' in q or '_' in q or '\\' in q:
                            continue
                        if any(k in q for k in ["com.example", "id", "name", "desc", "url", "http", "HH:mm", "yyyy-MM-dd", "db", "FSRS", "M3", "UTC", "applicationId", "namespace", "float", "int"]):
                            continue
                        
                        # Find if it is NOT followed by .localize()
                        pattern = re.escape(f'"{q}"') + r'(?!\s*\.\s*localize\()'
                        if re.search(pattern, line):
                            # Ignore transition maps, custom box default parameters, or non-UI assignments
                            if "to" in stripped and ("mapOf" in stripped or stripped.endswith(",")):
                                continue
                            results.append(f"{path}:{idx+1}: {stripped}  --> Found: \"{q}\"")
                            
    # Write all findings to a text file
    with open("all_unlocalized_results.txt", "w", encoding="utf-8") as out:
        out.write(f"Total potential hardcoded strings found: {len(results)}\n\n")
        for res in results:
            out.write(res + "\n")

if __name__ == "__main__":
    scan_kotlin_files()
