package analyze.simulation;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import org.COPASI.*;

import com.thoughtworks.xstream.alias.ClassMapper.Null;

import beans.simulation.Simulation_AllBeans;
import beans.simulation.Simulation_DatasetsBeans;
import beans.simulation.Simulation_XYDataBeans;
import coloring.Coloring;
import parameter.Simulation_Parameter;


public class Simulation_COPASI {
	private CTimeSeries simTimeSeries;
	private CCopasiDataModel dataModel;
	private Simulation_Parameter simParam;
	public Simulation_COPASI( String sbmlFile , Simulation_Parameter simParam){
		this.simParam = simParam;
		
		dataModel = CCopasiRootContainer.addDatamodel();
		try {
			dataModel.importSBML( sbmlFile );
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		simulation();
	}
	public Simulation_COPASI( CCopasiDataModel dataModel , Simulation_Parameter simParam){
		this.simParam = simParam;
		this.dataModel = dataModel;
		simulation();
	}
	private void simulation(){
		CModel simModel = dataModel.getModel();
		CReportDefinitionVector simReports = dataModel.getReportDefinitionList();
		
		CReportDefinition simrepDefinition = simReports.createReportDefinition( "Report" , "Output for timecource");
		simrepDefinition.setTaskType( CTaskEnum.timeCourse );
		simrepDefinition.setIsTable( false );
		simrepDefinition.setSeparator( "," );
		
		ReportItemVector header = simrepDefinition.getHeaderAddr();
		ReportItemVector body = simrepDefinition.getBodyAddr();
		
		body.add(new CRegisteredObjectName(simModel.getObject(new CCopasiObjectName("Reference=Time")).getCN().getString()));
        body.add(new CRegisteredObjectName(simrepDefinition.getSeparator().getCN().getString()));
        header.add(new CRegisteredObjectName(new CCopasiStaticString("time").getCN().getString()));
        header.add(new CRegisteredObjectName(simrepDefinition.getSeparator().getCN().getString()));
        
        int i , iMax = ( int ) simModel.getMetabolites().size();
        for( i = 0 ; i < iMax ; i ++){
        		CMetab metab = simModel.getMetabolite( i );
        		body.add(new CRegisteredObjectName(metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString()));
        		header.add(new CRegisteredObjectName(new CCopasiStaticString(metab.getSBMLId()).getCN().getString()));
            
            if(i!=iMax-1){
              body.add(new CRegisteredObjectName(simrepDefinition.getSeparator().getCN().getString()));
              header.add(new CRegisteredObjectName(simrepDefinition.getSeparator().getCN().getString()));
            }
        }
        
        CTrajectoryTask simTrajekTask = ( CTrajectoryTask ) dataModel.getTask( "Time-Course");
        simTrajekTask.setMethodType( CTaskEnum.deterministic );
        simTrajekTask.getProblem().setModel( dataModel.getModel() );
        simTrajekTask.setScheduled( true );
        simTrajekTask.getReport().setTarget( "SimulationResult.txt");
        simTrajekTask.getReport().setAppend( false );
        
        // Simulation Emvironment Configuration
        
        CTrajectoryProblem simProblem = ( CTrajectoryProblem )simTrajekTask.getProblem();
        
        simProblem.setStepNumber((long) simParam.getNumTime());
        dataModel.getModel().setInitialTime( 0.0 );
        
        simProblem.setDuration((long) simParam.getEndTime() );
        simProblem.setTimeSeriesRequested( true );
        
        CTrajectoryMethod simMethod = ( CTrajectoryMethod )simTrajekTask.getMethod();
        
        CCopasiParameter simParameter = simMethod.getParameter("Absolute Tolerance");
        if( simParam.getTolerance() != null){
        	simParameter.setDblValue( simParam.getTolerance() );
        }
        else{
        	simParameter.setDblValue( 1.0e-12 );
        }
        
        boolean result=true;
        try
        {
            result = simTrajekTask.processWithOutputFlags( true, (int)CCopasiTask.ONLY_TIME_SERIES);
        }
        catch ( Exception e)
        {
        		e.printStackTrace();	
        }
        
        simTimeSeries = simTrajekTask.getTimeSeries();

	}
	public CTimeSeries getTimeSeries(){
		return( this.simTimeSeries );
	}
	//Following code sum up with the Beans of JSONIC and the return value will be encoded as JSON format and responsed to Client side.
	public Simulation_AllBeans configureSimulationBeans(Coloring colorOfVis ) {
		long numOfTimePoints = simTimeSeries.getRecordedSteps();
		double maxCandidate = 0.0;
		double minCandidate = Double.MAX_VALUE;
		Simulation_AllBeans simAllBeans = new Simulation_AllBeans();
		
		// All species information is contained in listOfSpecies
		if( dataModel.getModel().getNumMetabs() != 0 ){
			long numOfSpecies = simTimeSeries.getNumVariables();
			int speciesCount = 0;
			Simulation_DatasetsBeans allDataSets[] = new Simulation_DatasetsBeans[ (int) (numOfSpecies - 1)];
			for( int i = 0 ; i < dataModel.getModel().getNumMetabs() ; i ++){
				//j == 0 means the value of time point! this is considered as the value of x axis!
				for( int j = 1 ; j < numOfSpecies ; j ++ ){
					if( dataModel.getModel().getMetabolite( i ).getSBMLId().equals( simTimeSeries.getSBMLId( j  , dataModel ))){
						allDataSets[ speciesCount ] = new Simulation_DatasetsBeans();
						if( !dataModel.getModel().getMetabolite( i ).getObjectDisplayName().equals("")){
							allDataSets[ speciesCount ].setLabel( dataModel.getModel().getMetabolite( i ).getObjectDisplayName() );
						}
						else{
							allDataSets[ speciesCount ].setLabel( simTimeSeries.getSBMLId( j , dataModel));
						}
						Simulation_XYDataBeans allXYDataBeans[] = new Simulation_XYDataBeans[ (int) numOfTimePoints ];
						for( int k = 0 ; k < numOfTimePoints ; k ++){
							allXYDataBeans[ k ] = new Simulation_XYDataBeans();
							allXYDataBeans[ k ].setX( simTimeSeries.getConcentrationData( k , 0));
							allXYDataBeans[ k ].setY( simTimeSeries.getConcentrationData( k, j ));
							if( maxCandidate < simTimeSeries.getConcentrationData( k , j)){
								maxCandidate = simTimeSeries.getConcentrationData( k , j );
							}
							if( minCandidate > simTimeSeries.getConcentrationData( k , j) && simTimeSeries.getConcentrationData( k , j) > 0.0){
								minCandidate = simTimeSeries.getConcentrationData( k , j);
							}
						}
						allDataSets[ speciesCount ].setData( allXYDataBeans );
						allDataSets[ speciesCount ].setBorderColor( colorOfVis.getColor( speciesCount ));
						allDataSets[ speciesCount ].setPointBorderColor( colorOfVis.getColor( speciesCount ));
						allDataSets[ speciesCount ].setBackgroundColor( colorOfVis.getColor( speciesCount ));
						allDataSets[ speciesCount ].setPointRadius( 1 );
						speciesCount += 1;
					}
				}
			}
			simAllBeans.setData( allDataSets );
		}
		// If the species data is contained in Model Value.
		else{
			
			ArrayList< Integer > orderODESpeceis = getODESpeciesOrder();
			int numOfSpeceis = orderODESpeceis.size();
			Simulation_DatasetsBeans allDataSets[] = new Simulation_DatasetsBeans[ numOfSpeceis ];
			
			for( int i = 0 ; i < numOfSpeceis ; i ++){
				//j == 0 means the value of time point! this is considered as the value of x axis!
				for( int j = 1 ; j < simTimeSeries.getNumVariables() ; j ++ ){
					if( dataModel.getModel().getModelValue( orderODESpeceis.get( i ) ).getSBMLId().equals( simTimeSeries.getSBMLId( j  , dataModel ))){
						allDataSets[ i ] = new Simulation_DatasetsBeans();
						if( dataModel.getModel().getModelValue( orderODESpeceis.get( i )).getObjectDisplayName().equals("")){
							allDataSets[ i ].setLabel( dataModel.getModel().getModelValue( orderODESpeceis.get( i )).getObjectDisplayName() );
						}
						else{
							allDataSets[ i ].setLabel( simTimeSeries.getSBMLId( j , dataModel));
						}
						Simulation_XYDataBeans allXYDataBeans[] = new Simulation_XYDataBeans[ (int) numOfTimePoints ];
						for( int k = 0 ; k < numOfTimePoints ; k ++){
							allXYDataBeans[ k ] = new Simulation_XYDataBeans();
							allXYDataBeans[ k ].setX( simTimeSeries.getConcentrationData( k , 0));
							allXYDataBeans[ k ].setY( simTimeSeries.getConcentrationData( k, j ));
							if( maxCandidate < simTimeSeries.getConcentrationData( k , j)){
								maxCandidate = simTimeSeries.getConcentrationData( k , j );
							}
							if( minCandidate > simTimeSeries.getConcentrationData( k , j) && simTimeSeries.getConcentrationData( k , j) > 0.0){
								minCandidate = simTimeSeries.getConcentrationData( k , j);
							}
						}
						allDataSets[ i ].setData( allXYDataBeans );
						allDataSets[ i ].setBorderColor( colorOfVis.getColor( i ));
						allDataSets[ i ].setPointBorderColor( colorOfVis.getColor( i ));
						allDataSets[ i ].setBackgroundColor( colorOfVis.getColor( i ));
						allDataSets[ i ].setPointRadius( 1 );
					}
				}
			}
			simAllBeans.setData( allDataSets );
		}

		simAllBeans.setXmax( simTimeSeries.getData( numOfTimePoints - 1 , 0));
		simAllBeans.setYmax( maxCandidate );
		simAllBeans.setYmin( minCandidate );
		return simAllBeans;
	}
	private ArrayList<Integer> getODESpeciesOrder(){
		ArrayList<Integer> orderODESpecies = new ArrayList<Integer>();
		for( int i = 0 ; i < dataModel.getModel().getNumModelValues() ; i ++){
			if( dataModel.getModel().getModelValue( i ).getStatus() == CModelEntity.ODE){
				orderODESpecies.add( new Integer( i ));
			}
		}
		return orderODESpecies;
	}
}
