package analyze.simulation;


import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.math.ode.DerivativeException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.math.odes.DESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.SBMLinterpreter;

import beans.simulation.Simulation_AllBeans;
import beans.simulation.Simulation_DatasetsBeans;
import beans.simulation.Simulation_XYDataBeans;
import coloring.Coloring;
import parameter.Simulation_Parameter;

public class Simulation_SBSCL {
	private Simulation_Parameter simParam;
	private Model model;
	private MultiTable solution;
	public Simulation_SBSCL( String sbmlFile , Simulation_Parameter simParam){
		this.simParam = simParam;
		try {
			this.model = SBMLReader.read( new File( sbmlFile )).getModel();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		simulation();
	}
	public void simulation(){
		double stepSize = (double) simParam.getEndTime() / (double) simParam.getNumTime();
		double timeEnd = (double) simParam.getEndTime();
		System.out.println( stepSize );
		DESSolver solver = new RosenbrockSolver();
		solver.setStepSize( stepSize);
		try {
			SBMLinterpreter interpreter = new SBMLinterpreter( model );
			solution = solver.solve( interpreter, interpreter.getInitialValues() ,0d, timeEnd);
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModelOverdeterminedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DerivativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public MultiTable getTimeSeries(){
		return( this.solution );
	}
	public Simulation_AllBeans configureSimulationBeans(Coloring colorOfVis){
		int numOfSpecies = model.getListOfSpecies().size();
		int numOfTimePoints = solution.getTimePoints().length;
		double maxCnadidate = 0.0;
		
		Simulation_AllBeans simAllBeans = new Simulation_AllBeans();
		Simulation_DatasetsBeans allDataSets[] = new Simulation_DatasetsBeans[ numOfSpecies ];
		
		for( int i = 0 ; i < numOfSpecies ; i ++){
			for( int j = 0 ; j < solution.getColumnCount() ; j ++){
				if( model.getListOfSpecies().get( i ).getId().equals( solution.getColumnName( j ))){
					allDataSets[ i ] = new Simulation_DatasetsBeans();
					allDataSets[ i ].setLabel( solution.getColumnName( j ));
					Simulation_XYDataBeans allXYDataBeans[] = new Simulation_XYDataBeans[ numOfTimePoints ];
					for( int k = 0 ; k < numOfTimePoints ; k ++){
						allXYDataBeans[ k ] = new Simulation_XYDataBeans();
						allXYDataBeans[ k ].setX( solution.getTimePoint( k ));
						allXYDataBeans[ k ].setY( solution.getValueAt( k , j ));
						if( maxCnadidate < solution.getValueAt( k , j )){
							maxCnadidate = solution.getValueAt( k , j );
						}
					}
					allDataSets[ i ].setData( allXYDataBeans );
					allDataSets[ i ].setBorderColor( colorOfVis.getColor( i ));
					allDataSets[ i ].setPointBorderColor( colorOfVis.getColor( i ));
					allDataSets[ i ].setBackgroundColor( colorOfVis.getColor( i ));
					allDataSets[ i ].setPointRadius( 0 );
				}
			}
		}
		simAllBeans.setData( allDataSets );
		simAllBeans.setXmax( solution.getTimePoint( numOfTimePoints - 1));
		simAllBeans.setYmax( maxCnadidate );
		return simAllBeans;
	}
}
