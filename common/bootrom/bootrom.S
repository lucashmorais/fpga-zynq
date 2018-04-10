#define DRAM_BASE 0x80000000

.section .text.start, "ax", @progbits
.globl _start
_start:
  li a1, 0x2000000          // base address of clint // pseudo-instruction for loading arbitrary value 0x2000000 to gp register a1
  csrr a0, mhartid          // mhartid is a read-only register that contains the integer ID of the HW thread now running // csrr is an instruction for reading from a CSR into a GP reg
  bnez a0, boot_core        // if a0 != 0, then go to boot_core

  addi a2, a1, 4            // a2 = a1 + 4
  li a3, 1                  // a3 = 1
interrupt_loop:
  sw a3, 0(a2)              // *(a2 + 0) = a3
  addi a2, a2, 4            // a2 = a2 + 4
  lw a3, -4(a2)             // a3 = *(a2 - 4)
  bnez a3, interrupt_loop   // if a3 != 0, then go to interrupt_loop
  j boot_core               // go to boot_core

.section .text.hang, "ax", @progbits
.globl _hang
_hang:
  // This boot ROM doesn't know about any boot devices, so it just spins,
  // waiting for the serial interface to load the program and interrupt it
  la a0, _start             // a0 = PC + _start                                 // TODO: confirm
  csrw mtvec, a0            // mtvec = a0
  li a0, 8                  // a0 = 8 (b'1000)                                  // MIE or MSIP bit
  csrw mie, a0              // mie = a0 (b'1000)                                // set only MSIP in mie CSR
  csrw mideleg, zero        // mideleg = 0                                      // no delegation
  csrs mstatus, a0          // mstatus = mstatus | a0 = mstatus | b'1000        // set MIE in mstatus CSR
wfi_loop:
  wfi                       // Wait for interrupt. It may be implemented as a 'nop'
  j wfi_loop                // Loops to another wfi in case this HW thread wasn't moved to somewhere else

boot_core:
  sll a0, a0, 2             // a0 = a0 << 2 & b'11111 = a0 << 2                 // offset for hart msip
  add a0, a0, a1            // a0 = a0 + a1
  sw zero, 0(a0)            // *(a0 + 0) = 0                                    // clear the interrupt
  li a0, DRAM_BASE          // a0 = DRAM_BASE                                   // program reset vector
  csrw mepc, a0             // mpec = a0                                        // return from interrupt to start of user program
  csrr a0, mhartid          // a0 = mhartid                                     // hartid for next level bootloader
  la a1, _dtb               // a1 = PC + _dtb                                   // TODO: confirm                                    // dtb address for next level bootloader
  li a2, 0x80               // a2 = b'10000000                                  // set mstatus MPIE to 0
  csrc mstatus, a2          // mstatus = mstatus & ~a2 = mstatus & b'01111111
  mret                      // instruction for returning from M-Mode trap

_dtb:
  .incbin DEVICE_TREE
