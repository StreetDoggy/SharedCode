/* 
 * File:   bit_twiddle.h
 * Author: jecraig
 *
 * Created on May 5, 2014, 12:52 PM
 */

#ifndef BIT_TWIDDLE_H
#define	BIT_TWIDDLE_H

#ifdef	__cplusplus
extern "C" {
#endif

#ifndef min
#define min(a,b) ((a)>(b)?(b):(a))
#endif

#ifndef max
#define max(a,b) ((a)<(b)?(b):(a))
#endif

void SetBit(unsigned int bitval, unsigned int index, unsigned long int* data);
int GetBit(unsigned int index, unsigned long int data);
unsigned int GetBitRange(unsigned int low, unsigned int high, unsigned long int data);
signed int GetSignedBitRange(unsigned int low, unsigned int high, unsigned long int data);
void SetBitRange(unsigned long int value, unsigned int low, unsigned int high, unsigned long int* data);
void SetSignedBitRange(signed long int value, unsigned int low, unsigned int high, unsigned long int* data);
void ReverseBits(unsigned int bit_length, unsigned long int* data);
int CalcEvenParity(unsigned long int data);
int CalcOddParity(unsigned long int data);


#ifdef	__cplusplus
}
#endif

#endif	/* BIT_TWIDDLE_H */

