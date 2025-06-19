all:
	mkdir -p ./build 
	javac -d ./build src/*.java

clean:
	rm -rf ./build

run:
	java -cp ./build BackupTool $(input) $(output)

.PHONY: all clean run