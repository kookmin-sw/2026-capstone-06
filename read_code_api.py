import pandas as pd
import sys

pd.set_option('display.max_columns', None)
pd.set_option('display.max_rows', None)
try:
    xl = pd.ExcelFile('document/api.xlsx')
    with open('code_api.txt', 'w', encoding='utf-8') as f:
        f.write("Sheets: " + str(xl.sheet_names) + "\n")
        sheet = '코드'
        f.write(f"--- Sheet: {sheet} ---\n")
        df = xl.parse(sheet)
        f.write(df.to_markdown() + "\n")
except Exception as e:
    print(e)
