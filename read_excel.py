import pandas as pd
import sys

pd.set_option('display.max_columns', None)
pd.set_option('display.max_rows', None)
try:
    xl = pd.ExcelFile('document/api.xlsx')
    with open('dashboard_api.txt', 'w', encoding='utf-8') as f:
        f.write("Sheets: " + str(xl.sheet_names) + "\n")
        for sheet in xl.sheet_names:
            if "대시보드" in sheet or "Data" in sheet:
                f.write(f"--- Sheet: {sheet} ---\n")
                df = xl.parse(sheet)
                f.write(df.to_markdown() + "\n")
except Exception as e:
    print(e)
