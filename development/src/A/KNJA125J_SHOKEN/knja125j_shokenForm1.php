<?php

require_once('for_php7.php');

class knja125j_shokenForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("knja125j_shoken", "POST", "knja125j_shokenindex.php", "", "knja125j_shoken");

        //DB接続
        $db = Query::dbCheckOut();

        //年度・学年コンボ
        $opt = array();
        $opt[] = array('label' => "",'value' => "");
        $value_flg = false;
        $query = knja125j_shokenQuery::getTrainRow($model, "year_anuual");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["YEAR_ANNUAL"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $model->field["YEAR_ANNUAL"] = ($model->field["YEAR_ANNUAL"] && $value_flg) ? $model->field["YEAR_ANNUAL"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('edit')\"";
        $arg["YEAR_ANUUAL"] = knjCreateCombo($objForm, "YEAR_ANNUAL", $model->field["YEAR_ANNUAL"], $opt, $extra, 1);

        //生徒名
        $getName = $db->getOne(knja125j_shokenQuery::getName($model));

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$getName;

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knja125j_shokenQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scd = $row["DIV"] .$row["CODE"];
            $Row["RECORD"][$scd] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knja125j_shokenQuery::getTrainRow($model, "setyear"), DB_FETCHMODE_ASSOC);
        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        }

        //行動の記録チェックボックス
        for ($i=1; $i<11; $i++) {
            $ival = "1" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"";
            $extra .= "disabled";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録チェックボックス
        for ($i=1; $i<5; $i++) {
            $ival = "2" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"";
            $extra .= "disabled";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        if ($model->Properties["Specialactremark_3disp_J"] == "1") {
            $arg["showSpecialactremark_3disp_J"] = "1";
            $moji = 17;
            $gyo = 3;
            $extra = "style=\"height:45px;\"onkeyup=\"charCount(this.value, ".$gyo.", (".$moji." * 2), true);\"";
            $arg["CLASSACT"] = knjCreateTextArea($objForm, "CLASSACT", $gyo, $moji * 2, "soft", $extra, $row["CLASSACT"]);
            $arg["STUDENTACT"] = knjCreateTextArea($objForm, "STUDENTACT", $gyo, $moji * 2, "soft", $extra, $row["STUDENTACT"]);
            $arg["SCHOOLEVENT"] = knjCreateTextArea($objForm, "SCHOOLEVENT", $gyo, $moji * 2, "soft", $extra, $row["SCHOOLEVENT"]);
        } else {
            $arg["not_showSpecialactremark_3disp_J"] = "1";
            //特別活動の記録の観点
            $extra = "style=\"height:145px;\"onkeyup=\"charCount(this.value, 10, (17 * 2), true);\"";
            $arg["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 35, "soft", $extra, $row["SPECIALACTREMARK"]);
        }

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja125j_shokenForm1.html", $arg);
    }
}