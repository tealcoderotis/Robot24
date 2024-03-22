package Team4450.Robot24.commands;


import Team4450.Lib.Util;
import Team4450.Robot24.subsystems.DriveBase;
import Team4450.Robot24.subsystems.ElevatedShooter;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;

public class SpinUpShooter extends Command {
    private final ElevatedShooter elevatedShooter;
    private double startTime;
    private  enum State {NONE, MOVING, BACKFEED, DONE};
    private State state = State.NONE;

    private double angle;
    private boolean delay = false;

    public SpinUpShooter(ElevatedShooter elevatedShooter, DriveBase robotDrive, double angle, boolean delay) {
        SmartDashboard.putString("ShootSpeaker Status", state.name());
        this.elevatedShooter = elevatedShooter;
        this.angle = angle;
        this.delay = delay;
        addRequirements(elevatedShooter);
    }


    @Override
    public void initialize() {
        Util.consoleLog();
        elevatedShooter.shooter.hasShot = false;
        elevatedShooter.shooter.enableClosedLoopFeedStop(false);
        
        if (Double.isNaN(angle)) {
            state = State.BACKFEED;
            startTime = Util.timeStamp();
        }
        else
            state = State.MOVING;
        
        elevatedShooter.shootDoesTheSpeakerInsteadOfTheAmp = true;
    }

    @Override
    public void execute() {
        SmartDashboard.putString("ShootSpeaker Status", state.name());
        switch (state) {
            case NONE:
                break;
            case MOVING:
                double angleSetpoint = angle;

                if (elevatedShooter.executeSetPosition(angleSetpoint, 0, 0.15, false)) {
                    state = State.BACKFEED;
                    startTime = Util.timeStamp();
                }
                break;
            case BACKFEED:
                if (Util.getElaspedTime(startTime) < 0.05) {
                    elevatedShooter.shooter.startFeeding(-0.3); // start by feeding the note backwards a bit (30% speed for 0.2 seconds see down below)
                    // elevatedShooter.shooter.startShooting();
                }
                else {
                    elevatedShooter.shooter.startShooting();
                    state = State.DONE;
                }
                break;
            case DONE:
                elevatedShooter.shooter.stopFeeding();
                elevatedShooter.shooter.startShooting();
                SmartDashboard.putBoolean("Spun Up", true);
                break;
        }
    }

    @Override
    public void end(boolean interrupted) {
        Util.consoleLog("interrupted=%b", interrupted);
        elevatedShooter.shooter.stopFeeding();
        if (!delay)
            elevatedShooter.shooter.stopShooting();
    }

    @Override
    public boolean isFinished() {
        SmartDashboard.putString("ShootSpeaker Status", state.name());
        if (delay)
            return state == State.DONE;
        else
            return elevatedShooter.shooter.hasShot;
    }
}
