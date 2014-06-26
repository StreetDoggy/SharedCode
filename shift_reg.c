#include <xc.h>
#include <plib.h>
#include <HardwareProfile.h>
#include <shift_reg.h>

int InitShiftRegister(void){
    SHIFTREG_SER_TRIS = 0;
    SHIFTREG_SER = 0;
    SHIFTREG_RCLK_TRIS = 0;
    SHIFTREG_RCLK = 0;
    SHIFTREG_SRCLK_TRIS = 0;
    SHIFTREG_SRCLK = 0;
    SHIFTREG_nSRCLR_TRIS = 0;
    SHIFTREG_nSRCLR = 1;
    SHIFTREG_nOE_TRIS = 0;
    SHIFTREG_nOE = 0;

    return 1;
}
int ClearShiftRegister(void){
    SHIFTREG_nSRCLR = 0;
    SHIFTREG_RCLK = 0;
    SHIFTREG_SRCLK = 0;
    SHIFTREG_SER = 0;
    SHIFTREG_nOE = 0;
    Nop(); //Nop(); Nop(); Nop();
    SHIFTREG_nSRCLR = 1;
    Nop(); //Nop(); Nop(); Nop();

    return 1;
}
int WriteShiftRegister(unsigned char data[], int num_bytes){
    int index, byte_index;

    //SHIFTREG_RCLK = 0;
    //SHIFTREG_SRCLK = 0;
    //SHIFTREG_SER = 0;
    //SHIFTREG_nSRCLR = 1;
    
    for(byte_index = num_bytes-1; byte_index >= 0; --byte_index){
        for(index = 7; index >= 0; --index){
            SHIFTREG_SRCLK = 0;
            SHIFTREG_SER = (data[byte_index] >> index);
            Nop(); //Nop(); Nop(); Nop();
            SHIFTREG_SRCLK = 1;
            Nop(); //Nop(); Nop(); Nop();
        }
    }
    SHIFTREG_SRCLK = 0;
    //SHIFTREG_SER = 0;
    Nop(); //Nop(); Nop(); Nop();
    SHIFTREG_RCLK = 1;
    Nop(); //Nop(); Nop(); Nop();
    SHIFTREG_RCLK = 0;

    return 1;
}
