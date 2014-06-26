/* 
 * File:   rgb_matrix.h
 * Author: jecraig
 *
 * Created on June 18, 2014, 6:06 PM
 */

#ifndef RGB_MATRIX_H
#define	RGB_MATRIX_H

#ifdef	__cplusplus
extern "C" {
#endif

#define RGB_MATRIX_HEIGHT 8
#define RGB_MATRIX_WIDTH 8

#define RED_FACTOR 0.35
#define BLUE_FACTOR 0.55
#define GREEN_FACTOR 1.0

#define WHITE   0xFFFFFF
#define BLACK   0x000000
#define RED     0xFF0000
#define GREEN   0x00FF00
#define BLUE    0x0000FF
#define YELLOW  0xFFFF00
#define PINK    0xFF00FF
#define CYAN    0x00FFFF
#define ORANGE  0xFF9900
#define PURPLE  0x9900FF

int InitRGBMatrix(void);
unsigned long int MakeColor(unsigned long int red, unsigned long int green, unsigned long int blue);

void ClearRGBMatrix(void);
void DrawPoint(unsigned int x, unsigned int y, unsigned long int color);

//The update function should be called at frequent regular intervals, such as the interrupt handler of a Timer.
void UpdateRGBMatrix(void);

#ifdef	__cplusplus
}
#endif

#endif	/* RGB_MATRIX_H */

