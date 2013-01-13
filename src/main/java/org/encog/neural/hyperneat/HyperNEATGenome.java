package org.encog.neural.hyperneat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid;
import org.encog.engine.network.activation.ActivationClippedLinear;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationGaussian;
import org.encog.engine.network.activation.ActivationSIN;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.hyperneat.substrate.SubstrateLink;
import org.encog.neural.hyperneat.substrate.SubstrateNode;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.util.obj.ChooseObject;

public class HyperNEATGenome extends NEATGenome {

	public static void buildCPPNActivationFunctions(
			ChooseObject<ActivationFunction> activationFunctions) {
		activationFunctions.add(0.25, new ActivationClippedLinear());
		activationFunctions.add(0.25, new ActivationBipolarSteepenedSigmoid());
		activationFunctions.add(0.25, new ActivationGaussian());
		activationFunctions.add(0.25, new ActivationSIN());	
	}
	
	public HyperNEATGenome(final HyperNEATGenome other) {
		super(other);
	}

	public HyperNEATGenome(final long genomeID,
			final List<NEATNeuronGene> neurons, final List<NEATLinkGene> links,
			final int inputCount, final int outputCount) {
		super(genomeID,neurons,links,inputCount,outputCount);
	}

	public HyperNEATGenome(final NEATPopulation pop, final long id,
			final int inputCount, final int outputCount) {
		super(pop,id,inputCount,outputCount);

	}
	
	public HyperNEATGenome() {
		
	}
	
	@Override
	public void decode() {
		// obtain the CPPN
		super.decode();
		NEATNetwork cppn = (NEATNetwork)getOrganism();
		
		// create the phenotype
		NEATPopulation pop = (NEATPopulation)this.getPopulation();
		Substrate substrate = pop.getSubstrate();
		
		NEATLink[] links = new NEATLink[substrate.getLinkCount()*2];
		ActivationFunction[] afs = new ActivationFunction[substrate.getNodeCount()];
		
		// all activation functions are the same
		for(int i=0;i<afs.length;i++) {
			afs[i] = pop.getActivationFunctions().pickFirst();
		}
		
		// now create the links
		int linkIndex = 0;
		for(SubstrateLink link: substrate.getLinks() ) {
			SubstrateNode source = link.getSource();
			SubstrateNode target = link.getTarget();
			MLData input = new BasicMLData(cppn.getInputCount());
			int index = 0;
			for(double d:source.getLocation()) {
				input.setData(index++, d);
			}
			for(double d:target.getLocation()) {
				input.setData(index++, d);
			}
			MLData output = cppn.compute(input);
			links[linkIndex++] = new NEATLink(source.getId(),target.getId(),output.getData(0));
			links[linkIndex++] = new NEATLink(0,target.getId(),output.getData(1)); 
		}
				
		Arrays.sort(links);
		
	    NEATNetwork network = new NEATNetwork(
	    		substrate.getInputCount(),
                substrate.getOutputCount(),
	    		links,	    		
                afs);
		
		network.setActivationCycles(pop.getActivationCycles());		
		setOrganism(network);		
		
	}

}
