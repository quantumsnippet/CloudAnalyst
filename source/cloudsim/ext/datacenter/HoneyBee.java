package cloudsim.ext.datacenter;
import java.util.*;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;

public class HoneyBee extends VmLoadBalancer implements CloudSimEventListener 
{
	private int cutoff = 8;
	private int scoutBee = -1;
	private Map<Integer, VirtualMachineState> vmStatesList;
	Map<Integer, Integer> vmAllocationCounts = new HashMap<Integer, Integer> ();
	Map<Integer, Integer> fitness = new HashMap<Integer, Integer> ();
	
	
	public HoneyBee(DatacenterController dcb)
	{
		this.vmStatesList = dcb.getVmStatesList();
		dcb.addCloudSimEventListener(this);
	}

	/* This will return the VM Id on which CLOUDLET should be submitted.. To get VM, we are using Honey Bee */
	@Override
	public int getNextAvailableVm()
	{
		int vmId = -1;
		vmId = getScoutBee();
		scoutBee = vmId;
		allocatedVm(vmId);
		return vmId;
	}
	
	public void cloudSimEventFired(CloudSimEvent e) 
	{
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM)
		{
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			int countGunjanCloudlets;
			if(vmAllocationCounts.get(vmId)==null)
				countGunjanCloudlets = 0;
			else
				countGunjanCloudlets = vmAllocationCounts.get(vmId);
			vmAllocationCounts.put(vmId,countGunjanCloudlets+1);
			
			if(vmAllocationCounts.get(vmId)>cutoff)
				vmStatesList.put(vmId, VirtualMachineState.BUSY);
		} 
		
		else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET)
		{
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			int countGunjanCloudlets = vmAllocationCounts.get(vmId);
			vmAllocationCounts.put(vmId,countGunjanCloudlets-1);
			
			if(vmAllocationCounts.get(vmId)<cutoff)
				vmStatesList.put(vmId, VirtualMachineState.AVAILABLE);
		}
	}
		
	private boolean isSendScoutBees(int scoutBee)
	{
		if((vmAllocationCounts.get(scoutBee)==null)||(vmAllocationCounts.get(scoutBee) < cutoff))
			return false;
		else
			return true;
	}
	
	/* This will return food source */
	int getScoutBee()
	{
		if(scoutBee==-1)
		{
			if(vmStatesList.size()>0)
				return 0;
			else
				return -1;
		}
		
		else
		{
			if(isSendScoutBees(scoutBee)==false)
				return scoutBee;
			else
			{
				SendEmployedBees();
				return SendOnlookerBees();
			}
		}
	}
	
	int MemorizeBestSource() 
	{
		return waggleDance();
	}
	
	/* These are the bees which will observe Waggle Dance and give us best source */
	int SendOnlookerBees()
	{
		return MemorizeBestSource();
	}
	
	// Calculation to get the fitness value of VM
	void calculation()
	{
		  int i;
		  /*Employed Bee Phase*/
		  for (i=0;i<vmStatesList.size();i++)
		  {
			   if(vmAllocationCounts.get(i)==null)
				   fitness.put(i, 0);
			   else
				   fitness.put(i, calculateFitness(vmAllocationCounts.get(i)));
		  }
	}
	
	/* to calculate fitness.. done just to show the steps */
	int calculateFitness(int solValue)
	{
		return solValue;	
	}
	
	// Bees went in search & finding all the fitness
	void SendEmployedBees()
	{
	  fitness.clear();
	  calculation();
	}

	// By waggle Dance, we are getting best VM available
	private int waggleDance() 
	{
		int Min, i=0;
		Min = 0;
		int global = fitness.get(0);
		for(i=1;i<vmStatesList.size();i++)
		{
				if(fitness.get(i)< global)
				{
					global = fitness.get(i);
					Min = i;
				}
		}
		return Min;
	}
}
