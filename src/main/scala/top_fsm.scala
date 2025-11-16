import chisel3._
import chisel3.util._


class top_fsm extends Module  {
  val io = IO(new Bundle {
    val in_start = Input(Bool())
    val in_random_data = Input(UInt(3.W))     // tie to fpga buttons

    //val out_ciphered_text = Output(UInt(512.W))
    val out_led = Output(UInt(8.W))
    val out_ready = Output(Bool())

  })

  val state_number = RegInit(0.U(4.W)) // for state transition control

  val sReady :: sAddEventToPool :: sClrSha :: sHashToPool :: sReseedFromPools :: sHashToKey :: sResetPools :: sGenerateBlock :: sOutputBlock :: sRegen_1 :: sResetAes :: sRegen_2 :: sRegenKey:: Nil = Enum(13)
  val state = RegInit(sReady)
  
  //default values
  io.out_ready := false.B
  io.out_led := 0.U(8.W)

  switch(state) {
    
    
    is(sReady) {
      when(io.in_start) {
        when(state_number === 1.U){
          state := sAddEventToPool
        }
          .elsewhen(state_number === 4.U){ // weather button is pressed or not, if 4 then go ahead
            state := sReseedFromPools
          }
          .elsewhen(state_number === 7.U){ // weather button is pressed or not, if 4 then go ahead
            state := sGenerateBlock
          }
          .otherwise {
          state := sReady
        }
      }
        .elsewhen(state_number === 4.U){  // weather button is pressed or not, if 4 then go ahead
          state := sReseedFromPools
        }
        .elsewhen(state_number === 7.U){ // weather button is pressed or not, if 4 then go ahead
          state := sGenerateBlock
        }
        .otherwise {
          state := sReady
        }
    }
    
    
    is(sAddEventToPool){
      when(state_number === 2.U){
        state := sClrSha
      }
        .otherwise{
          state := sAddEventToPool
        }
    }


    is(sClrSha) {
      when(state_number === 3.U) {
        state := sHashToPool
      }
        .elsewhen(state_number === 5.U) {
          state := sHashToKey
        }
        .otherwise {
          state := sClrSha
        }
    }
    
    
    is(sHashToPool) {
      when(state_number === 0.U) {
        state := sReady
      }
        .otherwise {
          state := sHashToPool
        }
    }

    
    is(sReseedFromPools) {
      when(state_number === 2.U) {  // jump back to clear sha state
        state := sClrSha
      }
        .otherwise {
          state := sReseedFromPools
        }
    }
    
    
    is(sHashToKey) {
      when(state_number === 6.U) {
        state := sResetPools
      }
        .otherwise {
          state := sHashToKey
        }
    }

    
    is(sResetPools) {
      when(state_number === 7.U) {
        state := sGenerateBlock
      }
        .otherwise {
          state := sResetPools
        }
    }
    
    
    is(sGenerateBlock) {
      when(state_number === 8.U) {
        state := sOutputBlock
       }
        .otherwise {
          state := sGenerateBlock
        }
    }
    
    
    is(sOutputBlock) {

      //io.out_led := 
      //io.out_ready := 
      
      when(state_number === 7.U) {
          state := sGenerateBlock
        }
        .elsewhen(state_number === 0.U ) {
          state := sReady
        }
       .elsewhen(state_number === 9.U) {
         state := sRegen_1
       }
      .otherwise {
          state := sOutputBlock
        }
    }
    
    
    is(sRegen_1) {
      when(state_number === 10.U) {
        state := sResetAes
      }
        .otherwise {
          state := sRegen_1
        }
    }

    
    is(sResetAes) {
      when(state_number === 11.U) {
        state := sRegen_2
      }
        .otherwise {
          state := sResetAes
        }
    }

    
    is(sRegen_2) {
      when(state_number === 12.U) {
        state := sRegenKey
      }
        .otherwise {
          state := sRegen_2
        }
    }

    
    is(sRegenKey) {
      when(state_number === 7.U) {
        state := sGenerateBlock
      }
        .otherwise {
          state := sRegenKey
        }
    }
    
    
    
  }
}
