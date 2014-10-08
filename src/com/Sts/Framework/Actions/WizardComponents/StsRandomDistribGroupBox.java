package com.Sts.Framework.Actions.WizardComponents;

import com.Sts.Framework.Interfaces.StsRandomDistribFace;
import com.Sts.Framework.Interfaces.StsRandomGaussFace;
import com.Sts.Framework.MVC.StsModel;
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
    private double avg;
    private double dev;
    private int count;
    private byte type;
    private Random random = new Random();
    static private String[] typeStrings = StsRandomDistribFace.typeStrings;

    public StsRandomDistribGroupBox(double avg, double dev, int count, byte type, String valueName)
    {
        super(valueName + " Distribution");
        this.avg = avg;
        this.dev = dev;
        this.count = count;
        this.type = type;
        buildPanel();
    }

    private void buildPanel()
    {
        StsComboBoxFieldBean typeListBean = new StsComboBoxFieldBean(this, "type", "Type: ", StsRandomDistribFace.typeStrings);
        typeListBean.setSelectedIndex(type);
        StsDoubleFieldBean avgBean = new StsDoubleFieldBean(this, "avg", true, "Average:", true);
        StsDoubleFieldBean devBean = new StsDoubleFieldBean(this, "dev", 0, Double.MAX_VALUE, "Std Dev:", true);
        StsIntFieldBean countBean = new StsIntFieldBean(this, "count", 0, 1000000, "Num Samples:", true) ;
        gbc.fill = HORIZONTAL;

        addBeanToRow(typeListBean);
        addBeanEndRow(countBean);
        addBeanToRow(avgBean);
        addBeanEndRow(devBean);
    }

    public double getSample()
    {
        if(type == StsRandomDistribFace.TYPE_GAUSS)
            return avg + dev*random.nextGaussian();
        else
            return avg + dev*random.nextDouble();
    }

    @Override
    public double getAvg()
    {
        return avg;
    }

    public void setAvg(double avg)
    {
        this.avg = avg;
    }

    @Override
    public double getDev()
    {
        return dev;
    }

    public void setDev(double dev)
    {
        this.dev = dev;
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
}
