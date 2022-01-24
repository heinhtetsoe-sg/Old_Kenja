<?php

require_once('for_php7.php');
class knjm431wform2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm431windex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合 NO001
        if ($model->sendFlg && !isset($model->warning)) {
            $query = knjm431wQuery::getRecordScoreHist($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["SUBCLASSCD"] = $Row["CLASSCD"]."-".$Row["SCHOOL_KIND"]."-".$Row["CURRICULUM_CD"]."-".$Row["SUBCLASSCD"];
            $Row["TESTCD"] = $Row["SEMESTER"]."-".$Row["TESTKINDCD"]."-".$Row["TESTITEMCD"]."-".$Row["SCORE_DIV"];
            if ($model->Properties["knjm431wUseGakkiHyouka"] == "1" && $model->sendField["TESTKINDCD"] == "99" && $model->sendField["TESTITEMCD"] == "00" && $model->sendField["SCORE_DIV"] == "08") {
                $Row["SCORE"] = $Row["VALUE"];
            }
        } else {
            $Row =& $model->field;
        }

        //makeCmb2
//        $query = knjm431wQuery::getSubclassCd($model);
//        $extra = "onChange=\"btn_submit('edit2')\";";
//        makeCmb($objForm, $arg, $db, $query, $Row["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //makeCmb2
//        $query = knjm431wQuery::getTest($model, $Row["SUBCLASSCD"]);
//        $extra = "onChange=\"btn_submit('edit2')\";";
//        makeCmb($objForm, $arg, $db, $query, $Row["TESTCD"], "TESTCD", $extra, 1, "BLANK");

        //試験日付
        $date_ymd = strtr($Row["TEST_DATE"], "-", "/");
        $arg["data"]["TEST_DATE"] = View::popUpCalendar($objForm, "TEST_DATE", $date_ymd);

        //textbox
        $extra = " style=\"text-align: right;\"";
        $arg["data"]["SCORE"] = knjCreateTextBox($objForm, $Row["SCORE"], "SCORE", 3, 3, $extra);

        //textbox2
        $extra = "id=\"COMMENT\"";
        $displen = 50;
        $disprow = 3;
        $arg["data"]["COMMENT"] = knjCreateTextArea($objForm, "COMMENT", $disprow, $displen, "", $extra, $Row["COMMENT"]);
            knjCreateHidden($objForm, "COMMENT_KETA", $displen);
            knjCreateHidden($objForm, "COMMENT_GYO", $disprow);
            KnjCreateHidden($objForm, "COMMENT_STAT", "statusarea1");
// sample                                  KnjCreateTextArea($objForm, "TOTALSTUDYTIME", 3, 91, "soft", "style=\"height:47px;\"", $row["TOTALSTUDYTIME"]);

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "結果通知書 印刷", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJM431W");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "YEAR", $model->sendField["YEAR"]);
        knjCreateHidden($objForm, "SEMESTER", $model->sendField["SEMESTER"]);
        knjCreateHidden($objForm, "SUBCLASSCD", $model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]);
        knjCreateHidden($objForm, "SEQ", $model->sendField["SEQ"]);
        knjCreateHidden($objForm, "TESTCD", $model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]);
        knjCreateHidden($objForm, "USE_CURRICULUMCD", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "updEdit") {
            $arg["reload"]  = "window.open('knjm431windex.php?cmd=list&SEND_SCHREGNO=".$model->schregno."&SEND_YEAR=".$model->sendField["YEAR"]."&SEND_SEMESTER=".$model->sendField["SEMESTER"]."&SEND_TESTTYPE=".$model->sendField["TESTKINDCD"].$model->sendField["TESTITEMCD"].$model->sendField["SCORE_DIV"]."&SEND_SEQ=".$model->sendField["SEQ"]."&SEND_SUBCLASS=".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjm431wForm2.html", $arg);
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
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
