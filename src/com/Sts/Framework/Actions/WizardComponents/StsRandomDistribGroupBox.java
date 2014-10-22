package com.Sts.Framework.Actions.WizardComponents;

import com.Sts.Framework.Interfaces.StsRandomDistribFace;
import com.Sts.Framework.UI.Beans.StsComboBoxFieldBean;
import com.Sts.Framework.UI.Beans.StsDoubleFieldBean;
import com.Sts.Framework.UI.Beans.StsGroupBox;
import com.Sts.Framework.UI.Beans.StsIntFieldBean;
import com.Sts.Framework.Utilities.StsParameters;

import java.util.Random;


/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Jan 22, 2010
 * Time: 9:35:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsRandomDistribGroupBox extends StsGroupBox implements StsRandomDistribFace
{
    protected double avg;
    protected double dev;
    protected int count;
    protected byte type;
    protected double logAvg;
    protected double logDev;

    public double lastSample;

    private StsComboBoxFieldBean typeBean;
    private StsIntFieldBean countBean = null;
    private StsDoubleFieldBean avgBean, devBean;

    private Random random = createRandom();

    static public final long randomSeed = 1;
    static private String[] typeStrings = StsRandomDistribFace.typeStrings;

    public StsRandomDistribGroupBox(double avg, double dev, byte type, String valueName)
    {
        super(valueName + " Distribution");
        this.avg = avg;
        this.dev = dev;
        logAvg = Math.log(avg);
        logDev = computeLogDev(avg, dev);
        this.count = -1;
        this.type = type;
        constructBeans();
        buildPanel();
    }

    static public Random createRandom()
    {
        if(randomSeed != 0)
            return new Random(randomSeed);
        else
            return new Random();
    }

    private static double  computeLogDev(double avg, double dev)
    {
        if(dev <avg)
            return Math.log(avg) - Math.log(avg - dev);
        else
            return Math.log(avg + dev) - Math.log(avg);
    }

    private void constructBeans()
    {
        typeBean = new StsComboBoxFieldBean(this, "type", "Type: ", StsRandomDistribFace.typeStrings);
        if (count != -1)
            countBean = new StsIntFieldBean(this, "count", 0, 1000000, "Num Samples:", true);
        avgBean = new StsDoubleFieldBean(this, "avg", true, "Average:", true);
        devBean = new StsDoubleFieldBean(this, "dev", 0, Double.MAX_VALUE, "Std Dev:", true);
    }

    private void buildPanel()
    {
        gbc.fill = HORIZONTAL;

        addBeanToRow(typeBean);
        if (countBean != null)
            addBeanEndRow(countBean);
        addBeanToRow(avgBean);
        addBeanEndRow(devBean);
    }

    public double getSample()
    {
        if (type == StsRandomDistribFace.TYPE_GAUSS)
            lastSample = avg + dev * random.nextGaussian();
        else if (type == StsRandomDistribFace.TYPE_LINEAR)
        {
            double value = random.nextDouble();
            lastSample = avg + 2 * dev * (value - 0.5);
        }
        else if (type == StsRandomDistribFace.TYPE_LOGNORM)
        {
            lastSample = staticLogNormal(random, logAvg, logDev);
        }
        else // TYPE_POISSON
        {
            lastSample = staticPoisson(random, avg);
        }
        return lastSample;
    }

    static double staticLogNormal(Random random, double logAvg, double logDev)
    {
        double logValue =  logAvg + logDev * random.nextGaussian();
        return Math.exp(logValue);
    }

    static double staticPoisson(Random random, double avg)
    {
        int k = 0;
        double p = 1.0;
        double L = Math.exp(-avg);
        do
        {
            k++;
            p *= random.nextDouble();
        }
        while (p >= L);
        return k - 1;
    }

    @Override
    public double getAvg()
    {
        return avg;
    }

    public void setAvg(double avg)
    {
        this.avg = avg;
        logAvg = Math.log(avg);
    }

    @Override
    public double getDev()
    {
        return dev;
    }

    public void setDev(double dev)
    {
        this.dev = dev;
        logDev = computeLogDev(avg, dev);
    }

    @Override
    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public String getType()
    {
        return typeStrings[type];
    }

    public void setType(String typeString)
    {
        this.type = StsParameters.getByteIndexFromString(typeString, typeStrings);
    }

    static public void main(String[] args)
    {
        // test poisson distribution
        int nValues = 1000;
        double avg = 100;
        double dev = 100;
        int[] frequency = new int[1000];
        Random random = createRandom();

        double logAvg = Math.log(avg);
        double logDev = computeLogDev(avg, dev);

        for(int n = 0; n < nValues; n++)
        {
            int value = (int)staticLogNormal(random, logAvg, logDev);
            value = Math.min(value, 999);
            frequency[value]++;
        }
        System.out.println("Results: 1 - " +  frequency[1] + " 10 - " + frequency[10] + " 50 - " + frequency[50] + " 100 - " +
                frequency[100] + " 200 - " + frequency[200] + " 500 - " + frequency[500]);

     /* poisson
        for(int n = 0; n < nValues; n++)
        {
            int value = (int)staticPoisson(random, avg);
            frequency[value]++;
        }
        System.out.println("Results: 1 - " +  frequency[1] + " 10 - " + frequency[10] + " 50 - " + frequency[50] + " 100 - " +
                frequency[100] + " 200 - " + frequency[200] + " 500 - " + frequency[500]);
     */
    }
}
