package com.fredplugins.giantsfoundry;

import com.fredplugins.giantsfoundry.enums.SStage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * A state-machine that keeps track of heating/cooling actions.
 */
@Slf4j
@Data
public class HeatActionStateMachine
{
	/**
	 * Tick counter for heating, -1 means not currently heating.
	 */
	int HeatingTicks = -1;

	/**
	 * Tick counter for cooling, -1 means not currently cooling.
	 */
	int CoolingTicks = -1;

	/**
	 * The velocity of the heating/cooling action.
	 */
	int Velocity;

	/**
	 * The acceleration bonus of the heating/cooling action.
	 */
	int AccelerationBonus;

	/**
	 * The starting heat amount of the heating/cooling action.
	 */
	int StartingHeat;

	/**
	 * The estimated tick duration of the heating/cooling action.
	 */
	int EstimatedDuration;

	/**
	 * The goal heat amount of the heating/cooling action.
	 */
	int GoalHeat = 0;

	/**
	 * The last action the player clicked on. Used for ui overlay to display.
	 * When null, the state-machine will stop() and reset.
	 */
	String ActionName = null;

	private FredsGiantsFoundryState State;
	private FredsGiantsFoundryConfig Config;

	/**
	 * Start the state-machine with the given parameters.
	 * <p>
	 * These parameters have to be set-up manually before start().
	 * Velocity, AccelerationBonus, ActionName
	 *
	 * @param state        the current state of the foundry
	 * @param config       the current configuration of the plugin
	 * @param startingHeat the starting heat amount
	 * @see HeatActionStateMachine#setup(int, int, String)
	 */
	public void start(FredsGiantsFoundryState state, FredsGiantsFoundryConfig config, int startingHeat)
	{
		// use Velocity to determine if heating or cooling
		if (Velocity > 0)
		{
			HeatingTicks = 0;
			CoolingTicks = -1;
		}
		else
		{
			CoolingTicks = 0;
			HeatingTicks = -1;
		}
		StartingHeat = startingHeat - Velocity;
		State = state;
		Config = config;

		calculateEstimates();
	}

	/**
	 * Get the estimated remaining duration of the heating/cooling action.
	 *
	 * @return the estimated remaining duration in ticks
	 */
	public int getRemainingDuration()
	{
		if (isHeating())
		{
			return Math.max(0, EstimatedDuration - HeatingTicks);
		}
		else if (isCooling())
		{
			return Math.max(0, EstimatedDuration - CoolingTicks);
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Core logic. Runs once on {@link HeatActionStateMachine#start} and assumes synchronization with the game.
	 * Calculate the estimated duration and goal heat amount of the heating/cooling action.
	 */
	public void calculateEstimates()
	{
		int[] range = State.getCurrentHeatRange();
		SStage stage = State.getCurrentStage();
		int actionsLeft = State.getActionsLeftInStage();
		int actionsLeft_DeltaHeat = actionsLeft * stage.heatChange();
		if (isHeating())
		{
			if (stage.isHeating())
			{
				GoalHeat = Math.max(range[0] + Config.heatingCoolingBuffer(), range[1] - actionsLeft_DeltaHeat);
				if (StartingHeat < GoalHeat)
				{
					EstimatedDuration = HeatActionSolver.findDx0Index(
						GoalHeat - StartingHeat,
						Velocity, AccelerationBonus);

					GoalHeat += EstimatedDuration / 2; // compensate for decay during heating

					EstimatedDuration = HeatActionSolver.findDx0Index(
						GoalHeat - StartingHeat,
						Velocity, AccelerationBonus);
				}
				else // overheating
				{
					EstimatedDuration = 0;
				}
			}
			else // is cooling
			{
				GoalHeat = Math.min(range[1] - Config.heatingCoolingBuffer(), range[0] - actionsLeft_DeltaHeat);
				if (StartingHeat < GoalHeat)
				{
					EstimatedDuration = HeatActionSolver.findDx0Index(
						GoalHeat - StartingHeat,
						Velocity, AccelerationBonus
					);
				}
				else // cold enough
				{
					EstimatedDuration = 0;
				}
			}
		}
		else if (isCooling())
		{
			if (stage.isCooling())
			{
				GoalHeat = Math.max(range[1] - Config.heatingCoolingBuffer(), range[0] + actionsLeft_DeltaHeat);
				if (StartingHeat > GoalHeat) // too hot
				{
					EstimatedDuration = HeatActionSolver.findDx0Index(
						StartingHeat - GoalHeat,
						Math.abs(Velocity), Math.abs(AccelerationBonus)
					);
				}
				else // hot enough
				{
					EstimatedDuration = 0;
				}
			}
			else // Heating Stage
			{
				GoalHeat = Math.max(range[0] + Config.heatingCoolingBuffer(), range[1] - actionsLeft_DeltaHeat);
				if (StartingHeat > GoalHeat)
				{
					EstimatedDuration = HeatActionSolver.findDx0Index(
						(StartingHeat - GoalHeat),
						Math.abs(Velocity), Math.abs(AccelerationBonus)
					);
				}
				else
				{
					EstimatedDuration = 0;
				}
			}
		}
	}

	/**
	 * Helper to remind the neccessary parameters to start the state-machine.
	 *
	 * @param velocity          the velocity of the heating/cooling action, 7 for slow, 27 for fast.
	 * @param accelerationBonus the acceleration bonus of the heating/cooling action. Usually 0 for slow, 2 for fast.
	 * @param actionName        the name of the action to display in the ui overlay
	 */
	public void setup(int velocity, int accelerationBonus, String actionName)
	{
		Velocity = velocity;
		AccelerationBonus = accelerationBonus;
		ActionName = actionName;
	}

	/**
	 * Stop the state-machine.
	 */
	public void stop()
	{
		HeatingTicks = -1;
		CoolingTicks = -1;
		ActionName = null;
	}

	/**
	 * Check if the state is currently heating.
	 *
	 * @return true if heating, false otherwise
	 */
	public boolean isHeating()
	{
		return HeatingTicks >= 0;
	}

	/**
	 * Check if the state is currently cooling.
	 *
	 * @return true if cooling, false otherwise
	 */
	public boolean isCooling()
	{
		return CoolingTicks >= 0;
	}

	/**
	 * Check if the heating/cooling state is currently idle. Neither heating nor cooling.
	 *
	 * @return
	 */
	public boolean isIdle()
	{
		return !(isHeating() || isCooling());
	}

	/**
	 * Tick the state-machine. This has to be called onVarbitChanged in order to sync with the game.
	 */
	public void onTick()
	{
		if (isHeating())
		{
			HeatingTicks++;
			if (HeatingTicks >= EstimatedDuration)
			{
				stop();
			}
		}
		if (isCooling())
		{
			CoolingTicks++;
			if (CoolingTicks >= EstimatedDuration)
			{
				stop();
			}
		}
//		log.info("\nReal Heat: " + State.getHeatAmount()
//		+ "\nGoal Heat - StartingHeat: " + (GoalHeat - StartingHeat)
//		+ "\nDuration: " + EstimatedDuration);
	}

}