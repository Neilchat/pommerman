This is the two effective action version of our featured EMCTS agent.

To run it initialise using
EFMCTSParams efmctsParams = new EFMCTSParams();
player = new EFMCTSPlayer(seed, playerID++, efmctsParams);

The parameters that can be tuned are in the class EFMCTSParams.
Note that stop_type should not be changed as this may cause the agent to overtime.


The link to the codebase for the other versions can be found at:
https://github.com/Neilchat/pommerman

The code here is in several branches. 
The unfeatured agent's code can be found in the master branch, under src/playes/emcts.
The 3 effective action agent's code can be found in the 3actions branch, under src/playes/efmcts.
The 2 effective action agent's code can be found in the 2actions branch, under src/playes/efmcts.
The master branch has the initial codebase for 6 actions agent.