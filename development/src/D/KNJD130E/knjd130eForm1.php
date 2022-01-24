<?php

require_once('for_php7.php');

class knjd130eForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd130eindex.php", "", "edit");
        $db = Query::dbCheckOut();
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = knjd130eQuery::getTrainRow($model->schregno, $db);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学習内容
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYTIME",
                            "cols"        => 35,
                            "rows"        => 6,
                            "extrahtml"   => "style=\"height:90px;\"",
                            "value"       => $row["TOTALSTUDYTIME"] ));
        $arg["data"]["TOTALSTUDYTIME"] = $objForm->ge("TOTALSTUDYTIME");

        //評価
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "SPECIALACTREMARK",
                            "cols"        => 35,
                            "rows"        => 6,
                            "extrahtml"   => "style=\"height:90px;\"",
                            "value"       => $row["SPECIALACTREMARK"] ));
        $arg["data"]["SPECIALACTREMARK"] = $objForm->ge("SPECIALACTREMARK");

        //備考
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "COMMUNICATION",
                            "cols"        => 43,
                            "rows"        => 5,
                            "extrahtml"   => "style=\"height:75px;\"",
                            "value"       => $row["COMMUNICATION"] ));
        $arg["data"]["COMMUNICATION"] = $objForm->ge("COMMUNICATION");

        /**********/
        /* ボタン */
        /**********/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //一括更新ボタン
        $link  = REQUESTROOT."/D/KNJD130E/knjd130eindex.php?cmd=replace&SCHREGNO=".$model->schregno;
        $extra = "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);
        //ＣＳＶ処理
        $fieldSize  = "TOTALSTUDYTIME=316,";
        $fieldSize .= "SPECIALACTREMARK=316,";
        $fieldSize .= "COMMUNICATION=323";
        //ＣＳＶ出力ボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX157/knjx157index.php?FIELDSIZE=".$fieldSize."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);


        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd130eForm1.html", $arg);
    }
}
?>
