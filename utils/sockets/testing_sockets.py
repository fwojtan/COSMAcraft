import socket
import json

host = ''
port = 80

fins_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

fins_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

fins_socket.bind((host, port))

fins_socket.listen(1)

print('Server is now up and running')

filename = 'test_send_file.json'

data_to_send = json.dumps("{'First thing':0, 'Next thing':'hello'}")

with open(filename, "w") as file:
	file.write(data_to_send)


while 1:
	fsock, faddr = fins_socket.accept()
	request = fsock.recv(1024).decode('utf-8')
	with open(filename, "r") as file:
		send_data = file.readline()
		fsock.send(send_data.encode('utf-8'))
	print('Server here:', request)
	fsock.close()



