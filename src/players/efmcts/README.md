This is the two effective action version of our featured EMCTS agent.

To run it initialise using
EFMCTSParams efmctsParams = new EFMCTSParams();
player = new EFMCTSPlayer(seed, playerID++, efmctsParams);

The parameters that can be tuned are in the class EFMCTSParams.
Note that stop_type should not be changed as this may cause the agent to overtime.