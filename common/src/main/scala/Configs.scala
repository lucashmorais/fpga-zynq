package zynq

import chisel3._
import freechips.rocketchip.chip.{ExtMem, ExtIn, DefaultConfig, DualCoreConfig, DualCoreConfigWithRoccExample, QuadCoreConfig, HexaCoreConfig, DefaultSmallConfig, BaseConfig, WithoutTLMonitors}
import freechips.rocketchip.config.{Parameters, Config}
import freechips.rocketchip.coreplex.{RocketTilesKey, L1toL2Config, CacheBlockBytes}
import freechips.rocketchip.rocket.{RocketTileParams, RocketCoreParams, MulDivParams, DCacheParams, ICacheParams}
import freechips.rocketchip.tile.BuildCore
import testchipip._

class WithZynqAdapter extends Config((site, here, up) => {
  case SerialFIFODepth => 16
  case ResetCycles => 10
  case ZynqAdapterBase => BigInt(0x43C00000L)
  case ExtMem => up(ExtMem, site).copy(idBits = 6)
  case ExtIn => up(ExtIn, site).copy(beatBytes = 4, idBits = 12)
  case BlockDeviceKey => BlockDeviceConfig(nTrackers = 2)
  case BlockDeviceFIFODepth => 16
  case NetworkFIFODepth => 16
})

class WithNMediumCores(n: Int) extends Config((site, here, up) => {
  case RocketTilesKey => {
    val medium = RocketTileParams(
      core = RocketCoreParams(mulDiv = Some(MulDivParams(
        mulUnroll = 8,
        mulEarlyOut = true,
        divEarlyOut = true)),
        fpu = None),
      dcache = Some(DCacheParams(
        rowBits = site(L1toL2Config).beatBytes*8,
        nSets = 64,
        nWays = 1,
        nTLBEntries = 4,
        nMSHRs = 0,
        blockBytes = site(CacheBlockBytes))),
      icache = Some(ICacheParams(
        rowBits = site(L1toL2Config).beatBytes*8,
        nSets = 64,
        nWays = 1,
        nTLBEntries = 4,
        blockBytes = site(CacheBlockBytes))))
    List.fill(n)(medium) ++ up(RocketTilesKey, site)
  }
})

class DefaultMediumConfig extends Config(new WithNMediumCores(1) ++ new BaseConfig)

class ZynqConfig extends Config(new WithZynqAdapter ++ new DefaultConfig)
class ZynqDualCoreConfig extends Config(new WithZynqAdapter ++ new DualCoreConfig)
class ZynqDualCoreConfigWithRoccExample extends Config(new WithZynqAdapter ++ new DualCoreConfigWithRoccExample)
class ZynqQuadCoreConfig extends Config(new WithZynqAdapter ++ new QuadCoreConfig)
class ZynqHexaCoreConfig extends Config(new WithZynqAdapter ++ new HexaCoreConfig)
class ZynqMediumConfig extends Config(new WithZynqAdapter ++ new DefaultMediumConfig)
class ZynqSmallConfig extends Config(new WithZynqAdapter ++ new DefaultSmallConfig)

class ZynqFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqConfig)
class ZynqDualCoreFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqDualCoreConfig)
class ZynqDualCoreFPGAConfigWithRoccExample extends Config(new WithoutTLMonitors ++ new ZynqDualCoreConfigWithRoccExample)
class ZynqHexaCoreFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqHexaCoreConfig)
class ZynqMediumFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqMediumConfig)
class ZynqSmallFPGAConfig extends Config(new WithoutTLMonitors ++ new ZynqSmallConfig)
