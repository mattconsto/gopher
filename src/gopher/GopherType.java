package gopher;

public enum GopherType {
	PlainText('0'),
	DirectoryListing('1'),
	SearchQuery('2'),
	ErrorMessage('3'),
	BinHexFile('4'),
	BinaryArchive('5'),
	UUEncodedText('6'),
	SearchEngineQuery('7'),
	TelnetSessionPointer('8'),
	BinaryFile('9'),
	GIFImage('g'),
	HTMLFile('h'),
	Information('i'),
	JPEGImage('I'),
	WAVAudio('s'),
	TN3270SessionPointer('s'),
	Unknown('z');
	
	private GopherType(char code) {}
}
