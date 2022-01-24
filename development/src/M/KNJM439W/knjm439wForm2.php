<?php
class knjm439wform2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm439windex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合 NO001
        if ($model->sendFlg && !isset($model->warning)) {
            $query = knjm439wQuery::getAttendData($model, $model->sendField["SEQ"]);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["SUBCLASSCD"] = $Row["CLASSCD"]."-".$Row["SCHOOL_KIND"]."-".$Row["CURRICULUM_CD"]."-".$Row["SUBCLASSCD"];
            $Row["TESTCD"] = $Row["SEMESTER"]."-".$Row["TESTKINDCD"]."-".$Row["TESTITEMCD"]."-".$Row["SCORE_DIV"];
        } else {
            $Row =& $model->field;
        }

        $dis = "";
        if ($model->schregno == '') {
            $dis = " disabled ";
        }

        //回数
        $arg["data"]["SEQ"] = $model->sendField["SEQ"];

        //試験日付
        $date_ymd = strtr($Row["TEST_DATE"], "-", "/");
        $arg["data"]["TEST_DATE"] = View::popUpCalendar($objForm, "TEST_DATE", $date_ymd);

        //出席チェックボックスを作成
        $extra = $Row["ATTEND"] == "1" ? "checked" : "";
        $extra .= " id=\"ATTEND\" ".$dis;
        $arg["data"]["ATTEND"] = knjCreateCheckBox($objForm, "ATTEND", "1", $extra, "");

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"".$dis;
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"".$dis;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"".$dis;
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "YEAR", $model->sendField["YEAR"]);
        knjCreateHidden($objForm, "SEMESTER", $model->sendField["SEMESTER"]);
        knjCreateHidden($objForm, "SUBCLASSCD", $model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]);
        knjCreateHidden($objForm, "SEQ", $model->sendField["SEQ"]);
        knjCreateHidden($objForm, "TESTCD", $model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "updEdit") {
            $arg["reload"]  = "window.open('knjm439windex.php?cmd=list&SEND_SCHREGNO=".$model->schregno."&SEND_YEAR=".$model->sendField["YEAR"]."&SEND_SEMESTER=".$model->sendField["SEMESTER"]."&SEND_TESTTYPE=".$model->sendField["TESTKINDCD"].$model->sendField["TESTITEMCD"].$model->sendField["SCORE_DIV"]."&SEND_SEQ=".$model->sendField["SEQ"]."&SEND_SUBCLASS=".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjm439wForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
