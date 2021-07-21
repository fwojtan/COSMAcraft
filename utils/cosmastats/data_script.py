import subprocess
from time import sleep, strftime, localtime 
import os
import signal
import json
import datetime
import time

#nodelist = ['b1'+str(i).zfill(2) for i in range(1, 5)]
nodelist = ['m7'+str(i).zfill(3) for i in range(1, 453)]


data_directory = '/cosma/home/do009/dc-wojt1/usage_data'

def check_and_delete_excess_files(DIR):
	ls_list = os.listdir(DIR)
	filelist = [filename for filename in ls_list if os.path.isfile(os.path.join(DIR, filename))]
	print('Directory contains:', len(filelist), ' files')
	if len(filelist) > 500:
		oldest_file = min(os.listdir(DIR), key=lambda x: datetime.datetime.strptime(time.ctime(os.path.getctime(os.path.join(DIR, x))), '%a %b %d %H:%M:%S %Y'))
		print('Deleting oldest file:', oldest_file)	
		os.remove(os.path.join(DIR, oldest_file))

def convert_memory_value(memory):
	retval = 0
	if 'g' in memory:
		retval = int(memory[:-1]) * 1000000000
	elif 'm' in memory:
		retval = int(memory[:-1]) * 1000000
	elif 'k' in memory:
		retval = int(memory[:-1]) * 1000
	else:
		retval = int(memory) 
	return retval


def extract_nodes(node_string):
	retlist = []
	if '[' not in node_string:
		retlist.append(node_string)
	else:
		groups = node_string[2:-1].split(',')
		for group in groups:
			if '-' in group:
				lims = group.split('-')
				for i in range(int(lims[0]), int(lims[1])+1):
					retlist.append('m'+str(i))

			else:
				retlist.append('m'+group)
	
	return retlist
		


def poll_nodes(nodelist):
	node_dict = {}
	for node_id in nodelist:
		node_dict[node_id] = {'id':node_id, 'cpu':0.0, 'mem':0.0, 'job':'None', 'state':'idle', 'runtime':'n/a', 'updated':'failed'}	
		command = 'pmstat -t 1sec -h '+node_id
		p = subprocess.Popen(args = command, shell=True, stdout=subprocess.PIPE, universal_newlines=True, preexec_fn=os.setsid)


		sleep(1.5)
		p.send_signal(signal.SIGINT)
		
		for line in p.stdout:
			if 'loadavg' not in line and 'swpd' not in line and '@' not in line:	
				
				fields = line.split()
				percentage = 0.0
				mem_used = 0
				mem_perc = 0.0				

				if 'm7' in node_id:
					percentage = 100*float(fields[0]) / 28
					mem_used = 512000 - convert_memory_value(fields[2])/1000000
					mem_perc = 100 * mem_used / 512000

				node_dict[node_id]['cpu'] = round(percentage, 2)
				node_dict[node_id]['mem'] = mem_perc
				node_dict[node_id]['updated'] = strftime("%Y-%m-%d %H:%M:%S", localtime())


		command2 = 'squeue -h -w '+node_id
		p2 = subprocess.Popen(args = command2, shell=True, stdout=subprocess.PIPE, universal_newlines=True, preexec_fn=os.setsid)
		sleep(0.05)
		for line in p2.stdout:
			fields = line.split()
			job_nodes = fields[7]
			job_name = fields[2]
			job_duration = fields[5]
			if fields[4] == 'R':
				job_nodelist = extract_nodes(job_nodes)
				for node in job_nodelist:
					if node == node_id:
						node_dict[node]['job'] = job_name
						node_dict[node]['runtime'] = job_duration


				

		command3 = 'sinfo -h -n '+node_id
		p3 = subprocess.Popen(args=command3, shell=True, stdout=subprocess.PIPE, universal_newlines=True, preexec_fn=os.setsid)
		sleep(0.05)
		for line in p3.stdout:
			fields = line.split()
			if len(fields) >= 6:	
				queue_nodes = fields[5]
				queue_nodelist = extract_nodes(queue_nodes)
				for node in queue_nodelist:
					if node == node_id:
						node_dict[node]['state'] = fields[4]
						if node_dict[node]['state'] == 'alloc' and node_dict[node]['job'] == 'None':
							node_dict[node]['job'] = 'Unknown'
			
		print('Polled node '+node_id+'at '+strftime("%d/%m-%H:%M:%S"))
	return node_dict





check_and_delete_excess_files(data_directory)
run_timestamp = strftime("%d-%m_%H:%M:%S")
print('Starting a run at '+run_timestamp)
 
result = poll_nodes(nodelist)

with open(data_directory+'/cosma_usage'+run_timestamp+'.json', 'w+') as outfile:
	json.dump(result, outfile)

with open(data_directory+'/cosma_usage_latest.json', 'w') as latest_file:
	json.dump(result, latest_file)



