def calculate_totals_and_count(file_path):
    total_tj = 0
    total_ts = 0
    line_count = 0

    with open(file_path, 'r') as file:
        for line in file:
            tj, ts = line.strip().split(',')
            tj_value = int(tj.split(':')[1])
            ts_value = int(ts.split(':')[1])

            total_tj += tj_value
            total_ts += ts_value
            line_count += 1

    return line_count, total_tj, total_ts

line_count, total_tj, total_ts = calculate_totals_and_count('timeLog.txt')

print(f"Line count: {line_count}")
print(f"Total TJ: {total_tj}")
print(f"Total TS: {total_ts}")