`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 01.12.2025 09:37:04
// Design Name: 
// Module Name: Top
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module Top (
    input  logic   clk_100mhz,
    input  logic   rst,
    input          add_data,	
    input          generate_data,
    input  [7:0]   in_random_data,
    output [7:0]   out_rng_data,	
    output         busy,
    output         valid_data	
);
    // Parameters
    localparam int CLK_DIVISION_FACTOR = 8;

    // Internal signals
    logic clk;
    logic add_data_db, generate_data_db;
    
    logic [511:0] rng_data;
    
    assign out_rng_data = rng_data[7:0];

    // Clock divider instance
    clock_divider #(.DIVIDE(CLK_DIVISION_FACTOR)) clock_divider_inst_0 (
        .clk_in(clk_100mhz),
        .clk_out(clk)
    );

    // Debounce instance
    debounce debounce_inst_0 (
        .clk(clk),
        .reset(rst),
        .sw(add_data),
        .db_level(add_data_db),
        .db_tick()
    );
    
    debounce debounce_inst_1 (
        .clk(clk),
        .reset(rst),
        .sw(generate_data),
        .db_level(generate_data_db),
        .db_tick()
    );

    // Accelerator instance
    FortunaTop fortuna_inst_0 (
        .clock(clk),
        .reset(rst),
        .io_add_data(add_data_db),
        .io_generate_data(generate_data_db),
        .io_in_random_data(in_random_data),
        .io_out_rng_data(rng_data),
        .io_busy(busy),
        .io_valid_data(valid_data)
    );
    
endmodule
