
package org.firstinspires.ftc.teamcode;

        import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
        import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
        import com.qualcomm.robotcore.hardware.ColorSensor;
        import com.qualcomm.robotcore.hardware.DcMotor;
        import com.qualcomm.robotcore.hardware.Servo;
        import com.qualcomm.robotcore.util.ElapsedTime;

        import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
        import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
        import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
        import com.qualcomm.robotcore.eventloop.opmode.Disabled;
        import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
        import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
        import com.qualcomm.robotcore.hardware.Gyroscope;
        import com.qualcomm.robotcore.hardware.IntegratingGyroscope;
        import com.qualcomm.robotcore.util.ElapsedTime;

        import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
        import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
        import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
        import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;


//@Autonomous(name="autonomous blue window side", group="Linear Opmode")
@Disabled
public class AutoBlueW extends LinearOpMode {

    //sorry about these strings, btw
    public final static String LEFTFRONT = "leftFront";
    public final static String LEFTBACK = "leftBack";
    public final static String RIGHTFRONT = "rightFront";
    public final static String RIGHTBACK = "rightBack";
    public final static String BALLARM = "ballArm";
    //intake wheels
    private DcMotor leftIntake;
    private DcMotor rightIntake;
    //servoes outtake of glyphs
    public Servo leftOut;
    public Servo rightOut;
    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftFront;
    private DcMotor leftBack;
    private DcMotor rightFront;
    private DcMotor rightBack;
    private ColorSensor color_sensor;
    private Servo ballArm;
    private Servo moveJewel;
    public final static double move = 0.5;
    public final static double slowMove = move / 2; //minor change
    public boolean detected = false;
    private ColorSensor cryptoSensor;
    //gyro stuff
    IntegratingGyroscope gyro;
    ModernRoboticsI2cGyro MRGyro;



    @Override
    public void runOpMode() {
        //gyro stuff for resetting Zheader
        boolean resetState = false;
        boolean lastResetState = false;
        //variable for how much we want the robo to turn
        int targetHeading = 180;

        color_sensor = hardwareMap.colorSensor.get("color");
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        double lfDrive;
        double lbDrive;
        double rfDrive;
        double rbDrive;

        OpenGLMatrix lastLocation = null; //from 4326

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        leftFront = hardwareMap.get(DcMotor.class, LEFTFRONT);
        leftBack = hardwareMap.get(DcMotor.class, LEFTBACK);
        rightFront = hardwareMap.get(DcMotor.class, RIGHTFRONT);
        rightBack = hardwareMap.get(DcMotor.class, RIGHTBACK);
        ballArm = hardwareMap.get(Servo.class, BALLARM);
        moveJewel = hardwareMap.servo.get("move jewel");
        
        //intake wheels, hahahahahahaha
        leftIntake = hardwareMap.dcMotor.get("left intake");
        rightIntake = hardwareMap.dcMotor.get("right intake");
        //servo for outtakes
        leftOut = hardwareMap.servo.get("left out");
        rightOut = hardwareMap.servo.get("right out");
        // run until the end of the match (driver presses STOP)

        //calirbate gyro
        MRGyro = hardwareMap.get(ModernRoboticsI2cGyro.class, "gyro");
        gyro = (IntegratingGyroscope)MRGyro;

        MRGyro.calibrate();

        while (opModeIsActive()) {

            double ballposition = 1;
            color_sensor = hardwareMap.colorSensor.get("color");

            //for storing raw data
            //dunno if needed
            int rawX = MRGyro.rawX();
            int rawY = MRGyro.rawY();
            //heading variable
            int heading = MRGyro.getHeading();

            //all of this needs to be fixe
            if (!detected) {
                ballArm.setPosition(-1.0);
                //we need to change this code so that the arm moves, not the robot
                if (color_sensor.blue() < color_sensor.red()) {
                    //no. for testing purposes only
                    //make it so arm will move forward, hitting red jewel in front
                    moveJewel.setPosition(0.1);

                    ballArm.setPosition(1.0);
                    detected = true;

                } else if (color_sensor.red() < color_sensor.blue()) {
                    //again, no. for testing purposes only
                    moveJewel.setPosition(-0.1);

                    ballArm.setPosition(1.0);
                    detected = true;
                }
//bool


            }

            //resets the z heading but only once when buttons pressed
            //don't actualy know if necessary
            resetState = (gamepad2.x && gamepad2.y);
            if (resetState && !lastResetState) {
                MRGyro.resetZAxisIntegrator();
            }
            lastResetState = resetState;

            //x is the left-right direction if the wire is at the bottom
            while (heading != targetHeading) {
                //turns, hopefully
                //also, we don't know on winterbreak which way color_sensor is facing, so we're assuming it's facing the back
                leftFront.setPower(-move);
                leftBack.setPower(-move);
                rightFront.setPower(-move);
                rightBack.setPower(-move);
            }
            if (cryptoSensor.red() > cryptoSensor.blue() && cryptoSensor.red() > cryptoSensor.green()) {
                //strafe strafe
                rightStrafe(leftFront, leftBack, rightFront, rightBack);

                if (cryptoSensor.red() > cryptoSensor.blue() && cryptoSensor.red() > cryptoSensor.green()) {
                    stopDatMovement(leftFront, leftBack, rightFront, rightBack);
                    //RELEASE YOUR GLYPH INTO THE BOX
                    leftIntake.setPower(-1.0);
                    rightIntake.setPower(-1.0);
                    //moves robot forward a little
                    // the sleep time is probs inacccurate
                    moveForward(leftFront, leftBack, rightFront, rightBack);
                    sleep(20);
                    //no. to be changed in testing. duh
                    leftOut.setPosition(0.2);
                    rightOut.setPosition(0.2);
                }
            }
            else {
                rightStrafe(leftFront, leftBack, rightFront, rightBack);
            }
        }


    }

    public static void stopDatMovement(DcMotor motor1, DcMotor motor2, DcMotor motor3, DcMotor motor4)
    {
        motor1.setPower(0);
        motor2.setPower(0);
        motor3.setPower(0);
        motor4.setPower(0);
    }

    //move forward method
    public static void moveForward(DcMotor motor1, DcMotor motor2, DcMotor motor3, DcMotor motor4)
    {
        motor1.setPower(move);
        motor2.setPower(move);
        motor3.setPower(-move);
        motor4.setPower(-move);
    }

    //move backwards method. Also, always put the left motors first, dumbo
    public static void moveBackwards(DcMotor motor1, DcMotor motor2, DcMotor motor3, DcMotor motor4) {
        motor1.setPower(-move);
        motor2.setPower(-move);
        motor3.setPower(move);
        motor4.setPower(move);
    }

    //strafe method. Once again, always put left motors first!!
    public static void leftStrafe(DcMotor motor1, DcMotor motor2, DcMotor motor3, DcMotor motor4) {
        motor1.setPower(move);
        motor2.setPower(-move);
        motor3.setPower(move);
        motor4.setPower(-move);
    }
    public static void rightStrafe(DcMotor motor1, DcMotor motor2, DcMotor motor3, DcMotor motor4) {
        motor1.setPower(-move);
        motor2.setPower(move);
        motor3.setPower(-move);
        motor4.setPower(move);
    }


}