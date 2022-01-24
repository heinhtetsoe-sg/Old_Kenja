<?php

require_once('for_php7.php');

class knjf012Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf012index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        /* ヘッダ */
        if(isset($model->schregno)){
            //生徒学籍データを取得
            $result = $db->query(knjf012Query::getSchregBaseMst($model));
            $RowB = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $result->free();
            //学籍番号
            $arg["header"]["SCHREGNO"] = $model->schregno;
            //氏名
            $arg["header"]["NAME_SHOW"] = $model->name;
            //生年月日
            $birth_day = explode("-",$RowB["BIRTHDAY"]);
            $arg["header"]["BIRTHDAY"] = $birth_day[0]."年".$birth_day[1]."月".$birth_day[2]."日";
        } else {
            //学籍番号
            $arg["header"]["SCHREGNO"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            //氏名
            $arg["header"]["NAME_SHOW"] = "&nbsp;&nbsp;&nbsp;&nbsp;";
            //生年月日
            $arg["header"]["BIRTHDAY"] = "&nbsp;&nbsp;&nbsp;&nbsp;年&nbsp;&nbsp;&nbsp;&nbsp;月&nbsp;&nbsp;&nbsp;&nbsp;日";
        }

        //レイアウトの切り替え
        if ($model->Properties["printKenkouSindanIppan"] == "1" || $model->Properties["printKenkouSindanIppan"] == "2") {
            $arg["new"] = 1;
        } else {
            $arg["base"] = 1;
        }

        //回数コンボ
        $opt = array();
        for ($i = 1; $i <= $model->Properties["KenkouSindanMaxNo"]; $i++) {
            $opt[] = array('label' => $i.'回目', 'value' => $i);
        }
        $model->field["NO"] = ($model->field["NO"]) ? $model->field["NO"] : $opt[0]["value"];
        $extra = ($model->schregno) ? "onchange=\"return btn_submit('edit');\"" : "disabled";
        $arg["data"]["NO"] = knjCreateCombo($objForm, "NO", $model->field["NO"], $opt, $extra, 1);

        //警告メッセージを表示しない場合
        if(!isset($model->warning) && isset($model->schregno) && isset($model->field["NO"])){
            $query = knjf012Query::getMedexamDetNoDat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row =& $model->field;
        }

        //extra
        $extraNum   = "style=\"text-align: right\"; onblur=\"return Num_Check(this);\"";
        $extraMark  = "style=\"text-align: center\"; onblur=\"return Mark_Check(this);\"";

        //健康診断実施日付
        $Row["DATE"] = str_replace("-","/",$Row["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE" ,$Row["DATE"]);

        //身長
        $arg["data"]["HEIGHT"] = knjCreateTextBox($objForm, $Row["HEIGHT"], "HEIGHT", 5, 5, $extraNum);

        //体重
        $arg["data"]["WEIGHT"] = knjCreateTextBox($objForm, $Row["WEIGHT"], "WEIGHT", 5, 5, $extraNum);

        //座高
        $arg["data"]["SITHEIGHT"] = knjCreateTextBox($objForm, $Row["SITHEIGHT"], "SITHEIGHT", 5, 5, $extraNum);

        //視力・右裸眼(数字)
        $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $Row["R_BAREVISION"], "R_BAREVISION", 4, 4, $extraNum);

        //視力・右矯正(数字)
        $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $Row["R_VISION"], "R_VISION", 4, 4, $extraNum);

        //視力・左裸眼(数字)
        $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $Row["L_BAREVISION"], "L_BAREVISION", 4, 4, $extraNum);

        //視力・左矯正(数字)
        $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $Row["L_VISION"], "L_VISION", 4, 4, $extraNum);

        //視力・右裸眼(文字)
        $arg["data"]["R_BAREVISION_MARK"] = knjCreateTextBox($objForm, $Row["R_BAREVISION_MARK"], "R_BAREVISION_MARK", 1, 1, $extraMark);

        //視力・右矯正(文字)
        $arg["data"]["R_VISION_MARK"] = knjCreateTextBox($objForm, $Row["R_VISION_MARK"], "R_VISION_MARK", 1, 1, $extraMark);

        //視力・左矯正(文字)
        $arg["data"]["L_BAREVISION_MARK"] = knjCreateTextBox($objForm, $Row["L_BAREVISION_MARK"], "L_BAREVISION_MARK", 1, 1, $extraMark);

        //視力・左裸眼(文字)
        $arg["data"]["L_VISION_MARK"] = knjCreateTextBox($objForm, $Row["L_VISION_MARK"], "L_VISION_MARK", 1, 1, $extraMark);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "HIDDENDATE", $Row["DATE"]);

        if ($model->Properties["printKenkouSindanIppan"] == "1" || $model->Properties["printKenkouSindanIppan"] == "2") {
            $query = knjf012Query::getMedexamDetNoDat($model);
            $tmp = $db->getRow($query, DB_FETCHMODE_ASSOC);
            knjCreateHidden($objForm, "R_BAREVISION",  $tmp["R_BAREVISION"]);
            knjCreateHidden($objForm, "R_VISION",      $tmp["R_VISION"]);
            knjCreateHidden($objForm, "L_BAREVISION",  $tmp["L_BAREVISION"]);
            knjCreateHidden($objForm, "L_VISION",      $tmp["L_VISION"]);
        }

        //DB切断
        Query::dbCheckIn($db);

        if(get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent(0);";
        } else if ($model->cmd == "reset") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjf012Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    if ($model->field["NO"] == "1" && $model->Properties["isMedexamDetNoDatWrite"] == "1") {
        $extra .= " disabled ";
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //更新後前の生徒へボタン
    if ($model->field["NO"] == "1" && $model->Properties["isMedexamDetNoDatWrite"] == "1") {
        $setNext = View::updateNext($model, $objForm, 'btn_update');
        $arg["button"]["btn_up_next"] = str_replace("style=", " disabled style=", $setNext);
    } else {
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');
    }

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    if ($model->field["NO"] == "1" && $model->Properties["isMedexamDetNoDatWrite"] == "1") {
        $extra .= " disabled ";
    }
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
