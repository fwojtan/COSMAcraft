import socket

target_host = 'localhost'
target_port = 80

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

client.connect((target_host, target_port))

request = 'Things please'
#"GET / HTTP/1.1\r\nHost:%s\r\n\r\n" % target_host
client.send(request.encode())

response = client.recv(4096)
print('Client here:', response.decode('utf-8'))

