This is a Kneser-Ney language model supports bigram and trigram.

It can decode 2000 sentences in about 200s and use a low memory (1.2G) to train 900,0000 sentences, by bit shifting and packing trick. More specifically, each word is hashed into a index within 20 bits and three words can fully utilize the 64-bit size by shifting first word left.

Actually, my hash method can still be improved by other state-of-art hash functions like FNV to improve decoding time. Java built-in hash map is very very slow since there will be too many conflicts.
