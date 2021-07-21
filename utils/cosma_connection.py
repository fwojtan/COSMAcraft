import paramiko
import time
import re

def get_node_data(channel, node_name):
	command = "pmstat -h " + node_name + " -t 0.2sec\n"
	channel.send(command)
	time.sleep(0.5)
	channel.send('\x03')
	output = channel.recv(4096)
	data = output.decode('UTF-8').splitlines()
	start_index = 0
	for i, line in enumerate(data):
		if 'loadavg' in line:
			start_index = i
			#print(start_index)
			break
	#print(output.decode('UTF-8'))
	if start_index+2<=len(data) and len(data[start_index+2].split())>=2:
		return data[start_index+2].split()[0], data[start_index+2].split()[2]
	else:
		return 'failed', 'failed'

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy()) # apparently this policy isn't super great but needed if running on new machines
ssh.load_system_host_keys()
# at some point need to definitely fix the password in this code
ssh.connect(hostname='login7.cosma.dur.ac.uk', username='dc-wojt1', password='Dur4979asdf!!',
	key_filename="C:\\Users\\FINLA\\OneDrive\\DesktopHDD Docs\\UNI\\MISCADA\\Dissertation\\SSH Keys\\id_rsa")

channel = ssh.invoke_shell()
time.sleep(1)
output = channel.recv(4096)
#print(output.decode('UTF-8'))
print('Shell Initialized!!!')

nodelist = ['m70'+str(i) for i in range(10, 99)]

for node in nodelist:
	cpu, mem = get_node_data(channel, 'm7014')
	print(time.strftime('%a %H:%M:%S'), node, cpu, mem)




ssh.close()









