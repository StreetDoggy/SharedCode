/* 
 * File:   shift_reg.h
 * Author: jecraig
 *
 * Created on December 16, 2012, 9:03 PM
 */

#ifndef SHIFT_REG_H
#define	SHIFT_REG_H

#ifdef	__cplusplus
extern "C" {
#endif

int InitShiftRegister(void);
int ClearShiftRegister(void);
int WriteShiftRegister(unsigned char  data[], int num_bytes);

#ifdef	__cplusplus
}
#endif

#endif	/* SHIFT_REG_H */

