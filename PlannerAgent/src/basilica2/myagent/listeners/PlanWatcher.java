package basilica2.myagent.listeners;

import java.util.ArrayList;
import java.util.HashMap;

import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.core.Event;
import basilica2.agents.components.InputCoordinator;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.ReadyEvent;
import basilica2.agents.events.priority.PriorityEvent;
import basilica2.agents.events.priority.PriorityEvent.Callback;
import basilica2.agents.listeners.BasilicaPreProcessor;
import basilica2.myagent.events.PlanEvent;
import basilica2.social.events.DormantGroupEvent;
import basilica2.tutor.events.DoTutoringEvent;


public class PlanWatcher implements BasilicaPreProcessor
{
    public PlanWatcher() 
    {
    	myConcepts.put("RECORDER_API",0);
    	myConcepts.put("AUDIO_SOURCE",0);
    	myConcepts.put("AUDIO_ENCODER",0);
    	myConcepts.put("OUTPUT_FORMAT",0);
    	myConcepts.put("PREPARE",0);
    	myConcepts.put("START",0);
    	myConcepts.put("STOP",0);
    	myConcepts.put("RELEASE",0);
    	NumConcepts = 8;
	}
    
    private Integer NumConcepts;
    
	private HashMap<String, Integer> myConcepts = new HashMap<String, Integer>();
    

	/**
	 * @param source the InputCoordinator - to push new events to. (Modified events don't need to be re-pushed).
	 * @param event an incoming event which matches one of this preprocessor's advertised classes (see getPreprocessorEventClasses)
	 * 
	 * Preprocess an incoming event, by modifying this event or creating a new event in response. 
	 * All original and new events will be passed by the InputCoordinator to the second-stage Reactors ("BasilicaListener" instances).
	 */
	@Override
	public void preProcessEvent(InputCoordinator source, Event event)
	{
		if (event instanceof MessageEvent)
		{
			MessageEvent me = (MessageEvent)event;
			String[] annotations = me.getAllAnnotations();
			
			for (String s: annotations)
		    {
				if(myConcepts.containsKey(s) && myConcepts.get(s)==0)
				{
					myConcepts.put(s,1);
					NumConcepts = NumConcepts - 1;
				}

				if(NumConcepts < 1)
				{
					//this new event will be added to the queue for second-stage processing.
					PlanEvent plan = new PlanEvent(source,  "Okay, it seems plan is ready now. You're on your own from here on. Good luck!", "NOTICE_DONE");
					source.queueNewEvent(plan);							
				}
				
				if(s.equals("HELP"))
				{           
					for (String key : myConcepts.keySet()) {
					    if(myConcepts.get(key)==0)
					    {					
					    	DoTutoringEvent toot = new DoTutoringEvent(source, key);
							source.addPreprocessedEvent(toot);
							myConcepts.put(key, 1);
							NumConcepts = NumConcepts - 1;
							break;
					    }
					}
				}			
	        }								    
	    }
		else if (event instanceof ReadyEvent)
		{
			if(NumConcepts < 1)
			{
				
				PlanEvent plan = new PlanEvent(source,  "Okay, it seems plan is ready now. You're on your own from here on. Good luck!", "NOTICE_DONE");
				
				source.queueNewEvent(plan);
			
			}
			else
			{
				for (String key : myConcepts.keySet()) {
				    if(myConcepts.get(key)==0)
				    {					
						
				    	PlanEvent plan = new PlanEvent(source,  "Plan is not yet complete. You are missing some steps. Let me help you with this.", "INCOMPLETE");

						source.queueNewEvent(plan);
					    
				    	DoTutoringEvent toot = new DoTutoringEvent(source, key);
						source.addPreprocessedEvent(toot);
				    	
						myConcepts.put(key, 1);
						NumConcepts = NumConcepts - 1;
						break;
				    }
				}
			}						
		}
		else if (event instanceof DormantGroupEvent)
		{
			for (String key : myConcepts.keySet()) {
			    if(myConcepts.get(key)==0)
			    {					
					DoTutoringEvent toot = new DoTutoringEvent(source, key);
					source.addPreprocessedEvent(toot);
					myConcepts.put(key, 1);
					NumConcepts = NumConcepts - 1;
					break;
			    }
			}
		}				
	}

	/**
	 * @return the classes of events that this Preprocessor cares about
	 */
	@Override
	public Class[] getPreprocessorEventClasses()
	{
		//only MessageEvents will be delivered to this watcher.
		return new Class[]{MessageEvent.class, ReadyEvent.class, DormantGroupEvent.class};
	}
}
