import socket

target_host = 'localhost'
target_port = 5432

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

client.connect((target_host, target_port))

request = 'Things please'
#"GET / HTTP/1.1\r\nHost:%s\r\n\r\n" % target_host
client.send(request.encode())

#response = client.recv(1024)
with open('response_file', 'wb') as f:
	while True:
		data = client.recv(1024)
		if not data:
			break
		f.write(data)

		

