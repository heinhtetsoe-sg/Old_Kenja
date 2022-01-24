<?php

require_once('for_php7.php');

class knjm110dForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjm110dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //表示切替
        if ($model->Div == 2) {
            $arg["hizuke"] = 1;
        } else {
            $arg["kouza"] = 1;
        }

        if ($model->cmd == "reset") {
            unset($model->field);
        }

        //年度初期値
        if (!$model->Year) $model->Year = CTRL_YEAR;

        if ($model->Div == 2) {
            //講座
            $model->field["CHAIRCD2"] = ($model->field["CHAIRCD2"]) ? $model->field["CHAIRCD2"] : (($model->Chair) ? $model->Chair : "");
            $query = knjm110dQuery::getChairList($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "CHAIRCD2", $model->field["CHAIRCD2"], $extra, 1, "blank");

            //日付データ
            $model->field["EXEDATE2"] = ($model->field["EXEDATE2"]) ? $model->field["EXEDATE2"] : (($model->Exedate) ? $model->Exedate : CTRL_DATE);
            $arg["data"]["EXEDATE2"] = View::popUpCalendar($objForm, "EXEDATE2", str_replace("-", "/", $model->field["EXEDATE2"]));

        } else {
            //講座初期設定
            if (!$model->Chair) {
                $opt_chair = array();
                $result = $db->query(knjm110dQuery::getChairList($model));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt_chair[] = array("label" => $row["LABEL"],
                                         "value" => $row["VALUE"]);
                }
                $model->Chair = $opt_chair[0]["value"];
            }

            //講座名称
            $query = knjm110dQuery::getChairname($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["CHAIRCD_SHOW"] = $row["CHAIRNAME"];
            knjCreateHidden($objForm, "CHAIRCD2", $model->Chair);

            //日付データ
            if (VARS::get("init") == 1) $model->field["EXEDATE2"] = CTRL_DATE;
            $model->field["EXEDATE2"] = ($model->field["EXEDATE2"]) ? $model->field["EXEDATE2"] : (($model->Exedate) ? $model->Exedate : CTRL_DATE);
            $arg["data"]["EXEDATE2"] = View::popUpCalendar($objForm, "EXEDATE2", str_replace("-", "/", $model->field["EXEDATE2"]));
        }

        //校時設定
        $model->field["KOUJI2"] = ($model->field["KOUJI2"]) ? $model->field["KOUJI2"] : $model->Periodcd;
        $query = knjm110dQuery::selectPeriod($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "KOUJI2", $model->field["KOUJI2"], $extra, 1);

        //備考
        $remark = "";
        if ($model->Exedate && $model->Periodcd && $model->Chair && $model->Schooling_seq) {
            $query = knjm110dQuery::getRemark($model);
            $remark = $db->getOne($query);
        }
        $model->field["REMARK"] = ($model->field["REMARK"]) ?  $model->field["REMARK"] : $remark;
        $extra = "style=\"height:35px;\"";
        $arg["data"]["REMARK"] = KnjCreateTextArea($objForm, "REMARK", 2, 20, "soft", $extra, $model->field["REMARK"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        if ($model->warning || $model->cmd == "edit2") {
            $model->Schooling_seq = $model->field["SCHOOLING_SEQ"];
        }
        knjCreateHidden($objForm, "SCHOOLING_SEQ", $model->Schooling_seq);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //更新後データをそのまま表示させる為、edit2を使用
        if ($model->cmd == "edit2") $model->cmd = "edit";

        if (VARS::get("cmd") != "edit" && !isset($model->warning)){
            $arg["reload"] = "window.open('knjm110dindex.php?cmd=list&ed=1','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm110dForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK="") {
    $opt = array();
    $value_flg = false;
    if ($BLANK) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
