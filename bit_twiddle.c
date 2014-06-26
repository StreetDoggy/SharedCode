#include "bit_twiddle.h"

int GetBit(unsigned int index, unsigned long int data){
	unsigned long int mask = 0;

	mask = 1L;
	mask = mask << index;

	return ((data & mask) != 0);
}


unsigned int GetBitRange(unsigned int low, unsigned int high, unsigned long int data){
	int lw, hg;
	unsigned long int value = 0, mask, shifted_mask;

	lw = min(low, high);
	hg = max(low, high);

	mask = 2;
	mask = (mask << (hg-lw))-1;
	shifted_mask = mask << lw;

	value = (data & shifted_mask) >> lw;

	return value;
}


signed int GetSignedBitRange(unsigned int low, unsigned int high, unsigned long int data){
	int lw, hg;

	lw = min(low, high);
	hg = max(low, high);

	if(GetBit(hg, data)) return ((GetBitRange(lw, hg, ~data))+1)*-1L;
	return GetBitRange(low, high, data);
}

void SetBit(unsigned int bitval, unsigned int index, unsigned long int* data){
	unsigned long int mask = 0, value = 0;

	mask = 1;
	mask = mask << index;

	value = (0x1L & (unsigned int)bitval) << index;

	(*data) = ((*data) & ~mask) | value;

}

void SetBitRange(unsigned long int value, unsigned int low, unsigned int high, unsigned long int* data){
	int lw, hg;
	unsigned long int mask, shifted_mask;

	lw = min(low, high);
	hg = max(low, high);

	mask = 2;
	mask = (mask << (hg-lw))-1;
	shifted_mask = mask << lw;

	(*data) = ((*data) & ~shifted_mask) | ((value << lw) & shifted_mask);
}

void SetSignedBitRange(signed long int value, unsigned int low, unsigned int high, unsigned long int* data){
	SetBitRange(value, low, high, data);
}

//Reverses the order of the bits in a value. [e.g. ReverseBits(4, 4'b1100) == 4'b0011]
void ReverseBits(unsigned int bit_length, unsigned long int* data){
	unsigned long int value = 0;
	int index;

	for(index = 0; index < bit_length; ++index){
		SetBit(GetBit(index, (*data)), (bit_length-1)-index, &value);
	}
	(*data) = value;
}

int CalcEvenParity(unsigned long int data){
	int parity = 0;
	unsigned int index;

	for(index = 0; index < sizeof(unsigned long int)*8; ++index){
		if(data & 0x1) parity = ~parity;
		data = data >> 1;
	}

	return parity != 0;
}

int CalcOddParity(unsigned long int data){
	return !CalcEvenParity(data);
}