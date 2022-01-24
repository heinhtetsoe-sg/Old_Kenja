<?php

require_once('for_php7.php');

class knjm210_3Form1
{
    function main(&$model)
    {
        $objForm       = new form;
        $arg["start"]  = $objForm->get_start("edit", "POST", "knjm210_3index.php", "", "edit");

//        $model->schregno = "20031935";

        $arg["data"]["INFO1"] = View::alink("knjm210_3index.php",
                                           "基本情報",
                                           "target=\"right_frame\"",
                                           array("cmd" => "edit","SCHREGNO" => $model->schregno));
        $arg["data"]["INFO2"] = View::alink("knjm210_3index.php",
                                           "住所情報",
                                           "target=\"right_frame\"",
                                           array("cmd" => "edit2","SCHREGNO" => $model->schregno));
        $arg["data"]["INFO3"] = View::alink("knjm210_3index.php",
                                           "異動情報",
                                           "target=\"right_frame\"",
                                           array("cmd" => "edit3","SCHREGNO" => $model->schregno));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm210_3Form1.html", $arg);
    }
}
?>
