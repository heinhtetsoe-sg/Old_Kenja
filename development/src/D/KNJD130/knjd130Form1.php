<?php

require_once('for_php7.php');

class knjd130Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd130index.php", "", "edit");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = knjd130Query::getTrainRow($model->schregno);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //総合的な学習の時間
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYTIME",
                            "cols"        => 43,
                            "rows"        => 4,
                            "extrahtml"   => "style=\"height:65px;\"",
                            "value"       => $row["TOTALSTUDYTIME"] ));
        $arg["data"]["TOTALSTUDYTIME"] = $objForm->ge("TOTALSTUDYTIME");

        //通信欄
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "COMMUNICATION",
                            "cols"        => 43,
                            "rows"        => 4,
                            "extrahtml"   => "style=\"height:65px;\"",
                            "value"       => $row["COMMUNICATION"] ));
        $arg["data"]["COMMUNICATION"] = $objForm->ge("COMMUNICATION");

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");

        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning) == 0 && $model->cmd !="clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd =="clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd130Form1.html", $arg);
    }
}
?>
