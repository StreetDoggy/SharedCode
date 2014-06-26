#include <xc.h>
#include <math.h>
#include "rgb_matrix.h"
#include "shift_reg.h"
#include "bit_twiddle.h"

#define RED_INDEX 0
#define GREEN_INDEX 1
#define BLUE_INDEX 2

unsigned char rgb_buffer[RGB_MATRIX_HEIGHT][RGB_MATRIX_WIDTH][3];
unsigned char color_counter = 0;
unsigned char row = 0;

int InitRGBMatrix(void){
    ClearRGBMatrix();

    return 1;
}

unsigned long int MakeColor(unsigned long int red, unsigned long int green, unsigned long int blue){
    return ((red << 16) | (green << 8) | blue);
}

void ClearRGBMatrix(void){
    int x, y;

    for(y = 0; y < RGB_MATRIX_HEIGHT; ++y){
        for(x = 0; x < RGB_MATRIX_WIDTH; ++x){
            rgb_buffer[y][x][RED_INDEX] = 0;
            rgb_buffer[y][x][GREEN_INDEX] = 0;
            rgb_buffer[y][x][BLUE_INDEX] = 0;
        }
    }
}

void DrawPoint(unsigned int x, unsigned int y, unsigned long int color){
    int value;

    value = (int)((((color >> 16) & 0xFF)*RED_FACTOR)+0.5);
    rgb_buffer[y][x][RED_INDEX] = (unsigned char)value;
    value = (int)((((color >> 8) & 0xFF)*GREEN_FACTOR)+0.5);
    rgb_buffer[y][x][GREEN_INDEX] = (unsigned char)value;
    value = (int)(((color & 0xFF)*BLUE_FACTOR)+0.5);
    rgb_buffer[y][x][BLUE_INDEX] = (unsigned char)value;
}

void UpdateRGBMatrix(void){
    unsigned long int rgb_matrix = 0xFFFFFFFF;
    unsigned char shift_data[4];

    color_counter++;
    row++;

    row = row % 8;

    SetBit(row != 0, 6, &rgb_matrix);
    SetBit(row != 1, 7, &rgb_matrix);
    SetBit(row != 2, 8, &rgb_matrix);
    SetBit(row != 3, 9, &rgb_matrix);
    SetBit(row != 4, 22, &rgb_matrix);
    SetBit(row != 5, 23, &rgb_matrix);
    SetBit(row != 6, 24, &rgb_matrix);
    SetBit(row != 7, 25, &rgb_matrix);

    SetBit(color_counter <= rgb_buffer[row][7][RED_INDEX], 0, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][7][BLUE_INDEX], 1, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][6][GREEN_INDEX], 2, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][5][RED_INDEX], 3, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][5][BLUE_INDEX], 4, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][4][GREEN_INDEX], 5, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][3][RED_INDEX], 10, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][3][GREEN_INDEX], 11, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][2][BLUE_INDEX], 12, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][1][RED_INDEX], 13, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][1][GREEN_INDEX], 14, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][0][BLUE_INDEX], 15, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][0][GREEN_INDEX], 16, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][0][RED_INDEX], 17, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][1][BLUE_INDEX], 18, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][2][GREEN_INDEX], 19, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][2][RED_INDEX], 20, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][3][BLUE_INDEX], 21, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][4][BLUE_INDEX], 26, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][4][RED_INDEX], 27, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][5][GREEN_INDEX], 28, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][6][BLUE_INDEX], 29, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][6][RED_INDEX], 30, &rgb_matrix);
    SetBit(color_counter <= rgb_buffer[row][7][GREEN_INDEX], 31, &rgb_matrix);

    shift_data[0] = rgb_matrix;
    shift_data[1] = rgb_matrix >> 8;
    shift_data[2] = rgb_matrix >> 16;
    shift_data[3] = rgb_matrix >> 24;
    WriteShiftRegister(shift_data, 4);
}
