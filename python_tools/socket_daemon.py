import socket
import json
import datetime
import os

host = ''
port = 5432 

fins_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

fins_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

fins_socket.bind((host, port))

fins_socket.listen(1)

usage_filename = 'usage_data/cosma_usage_latest.json'
config_filename = 'cosma_config_data.json'


print('COSMAcraft data server is now up and running...\n')
def send_data(socket, filename):
	with open(filename, "r") as f:
		data_chunk = f.read(1024)
		while(data_chunk):
			socket.send(data_chunk.encode('utf-8'))
			data_chunk = f.read(1024)	



#data_to_send = json.dumps("{'First thing':0, 'Next thing':'hello'}")

#with open(filename, "w") as file:
#	file.write(data_to_send)



while 1:
	fsock, faddr = fins_socket.accept()
	request = fsock.recv(1024).decode('utf-8')

	print("Received request:", request)

	if "state" in request:
		
		time_of_most_recent = datetime.datetime.fromtimestamp(os.path.getmtime(usage_filename))
		parts = request.split("&")
		time_of_last_sent = datetime.datetime.strptime(parts[1], "%d/%m/%Y, %H:%M")
		if time_of_most_recent > time_of_last_sent:
			send_data(fsock, usage_filename)
		else:
			reponse = "You already have latest state!"
			fsock.send(response.encode('utf-8'))
	if "config" in request:
		send_data(fsock, config_filename)
	if "test" in request:
		response = "COSMA Receiving"
		fsock.send(response.encode('utf-8'))




	
	fsock.close()
