import pandas as pd
from pandas_ods_reader import read_ods

base_path = "roomLayout.ods"
sheet_index = 2
df = read_ods(base_path, sheet_index, headers=False)
df2 = read_ods(base_path, 3, headers=False)

c8_locations = [(2, 46), (7, 46), (12, 46), (17, 46), (22, 46), (2, 98), (7, 98), (12, 98), (17, 98)]
c7_locations = [(1, 45), (6, 45), (11, 45), (16, 45), (21, 45), (26, 45), (31, 45), (36, 45), (41, 45), (46, 45)]

def get_formatted_names(df, column_no, bottom_row, cosma_version):

	column_name = 'column_'+str(column_no)
	adjacent_column = 'column_'+str(column_no+1)

	print(df[column_name][bottom_row-45])

	formatted_string = '['
	inlcuded_last_time = False

	for i in range(bottom_row-1, bottom_row-43, -1):

		name_to_add = str(df[column_name][i])

		
		if inlcuded_last_time:
			inlcuded_last_time = False
			continue

		if name_to_add == 'None':
			continue


		if name_to_add[:2] == 'm'+str(cosma_version) or name_to_add[:9] == 'bluefield':
			if name_to_add[:10] != 'bluefields':
				name_to_add += '/'+str(df[column_name][i-1])+'/'+str(df[adjacent_column][i-1])+'/'+str(df[adjacent_column][i])
				inlcuded_last_time = True


		
		formatted_string += '\"'+name_to_add+'\", '


	formatted_string = formatted_string[:-2]
	formatted_string += ']'

	print(formatted_string)




for i in c8_locations:
	get_formatted_names(df, i[0], i[1], 8)

for i in c7_locations:
	get_formatted_names(df2, i[0], i[1], 7)




