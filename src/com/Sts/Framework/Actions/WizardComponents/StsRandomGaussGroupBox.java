package com.Sts.Framework.Actions.WizardComponents;

import com.Sts.Framework.Interfaces.StsRandomGaussFace;
import com.Sts.Framework.MVC.StsModel;
import com.Sts.Framework.UI.Beans.StsComboBoxFieldBean;
import com.Sts.Framework.UI.Beans.StsDoubleFieldBean;
import com.Sts.Framework.UI.Beans.StsGroupBox;
import com.Sts.Framework.UI.Beans.StsIntFieldBean;
import com.Sts.Framework.Utilities.StsParameters;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Jan 22, 2010
 * Time: 9:35:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsRandomGaussGroupBox extends StsGroupBox implements StsRandomGaussFace
{
    private double avg;
    private double stdDev;
    private int count;

    public StsRandomGaussGroupBox(StsModel model)
    {
        super("Random Gauss Distribution");
        buildPanel();
    }

    private void buildPanel()
    {
        StsDoubleFieldBean avgBean = new StsDoubleFieldBean(this, "avg", true, "Average:", true);
        StsDoubleFieldBean stdDevBean = new StsDoubleFieldBean(this, "stdDev", 0, Double.MAX_VALUE, "Std Dev:", true);
        StsIntFieldBean countBean = new StsIntFieldBean(this, "count", 0, 1000000, "Num Samples:", true) ;
        gbc.fill = HORIZONTAL;
        addBeanToRow(avgBean);
        addBeanToRow(stdDevBean);
        addBeanEndRow(countBean);
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
        return stdDev;
    }

    public void setStdDev(double stdDev)
    {
        this.stdDev = stdDev;
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
}
