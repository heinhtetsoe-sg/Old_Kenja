<?php

require_once('for_php7.php');

class knjz407Form2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz407index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->field["CODE"] && $model->field["RECCNT"] == "") {
            $query = knjz407Query::getBaseRemarkMst($model->field["CODE"]);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //コード
        $extra = "onblur=\"this.value=paddingZero(this.value)\"";
        $arg["CODE"] = knjCreateTextBox($objForm, $Row["CODE"], "CODE", 2, 2, $extra);

        //名称
        $arg["NAME1"] = knjCreateTextBox($objForm, $Row["NAME1"], "NAME1", 20, 10, "");

        //項目数
        // $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra="";
        $arg["RECCNT"] = knjCreateTextBox($objForm, $Row["RECCNT"], "RECCNT", 2, 2, $extra);
        if ($model->cmd == "fixed" || $model->cmd == "edit") {
            
        }

        //確定ボタン
        $extra = "onclick=\"return btn_submit('fixed');\"";
        $arg["button"]["btn_fix"] = knjCreateBtn($objForm, "btn_fix", "確 定", $extra);

        if ($model->cmd == "fixed" || ($model->cmd == "edit" && $Row["RECCNT"] > 0)) {
            $model->field["FIX"] = "1";
            knjCreateHidden($objForm, "FIX", $model->field["FIX"]);
        } else if ($model->cmd == "edit" && $Row["RECCNT"] == 0) {
            $model->field["FIX"] = "";
        }
        if ($model->field["FIX"] == "1") {
            $arg["dispcomptype_p2"] = "1";
            if ($model->cmd == "fixed" || ($model->cmd == "edit" &&get_count($model->detailRemark) == 0)) {
                //$savedatにDB登録データを読み込む。$savedatにデータが無ければ空にする。
                $savedat = array();
                $query = knjz407Query::getBaseRemarkMst($model->field["CODE"]);
                $result = $db->query($query);
                while ($getrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $savedat[] = $getrow;
                }
            } else if ($model->cmd == "edit") {
                $savedat = array();
                $savedat = $model->detailRemark;
            }
            //入力項目を設定する
            for ($i = 1; $i <= $Row["RECCNT"]; $i++) {
                $SetWk = array();
                if($i < 10){
                    $SetWk["SEQ"] = substr("0".$i, 0,2);
                } else {
                    $SetWk["SEQ"] = substr($i, 0,2);
                }
                knjCreateHidden($objForm, "SEQ_".$i, $SetWk["SEQ"]);
                $SetWk["NAME2"] = "";
                $extra = "id =\"NAME2_".$i."\" style=\"text-align: left;\"";
                if ($model->cmd == "fixed" || $model->cmd == "edit") {
                    $SetWk["NAME2"] = knjCreateTextBox($objForm, $savedat[$i-1]["NAME2"], "NAME2_".$i, 30, 15, $extra);

                    $extra = "id =\"QUESTION_CONTENTS_".$i."\" style=\"text-align: left;\"";
                    $SetWk["QUESTION_CONTENTS"] = knjCreateTextArea($objForm, "QUESTION_CONTENTS_".$i, 10, 60, "", $extra, $savedat[$i-1]["QUESTION_CONTENTS"]);

                    $opt = array(
                                 array("label"=>"1:ラジオボタン", "value"=>"1"),
                                 array("label"=>"2:チェックボックス", "value"=>"2"),
                                 array("label"=>"3:テキスト", "value"=>"3")
                                 );
                    $extra = "id =\"ANSWER_PATTERN_".$i."\" style=\"text-align: left;\" onchange=\"chgcmbchk(this);\" ";
                    $SetWk["ANSWER_PATTERN"] = makeCmb($objForm, $arg, $db, $opt, "ANSWER_PATTERN_".$i, $savedat[$i-1]["ANSWER_PATTERN"], $extra, 1);  //knjCreateTextBox($objForm, $savedat[$i-1]["ANSWER_PATTERN"], "ANSWER_PATTERN_".$i, 30, 15, $extra);

                    $dis = $savedat[$i-1]["ANSWER_PATTERN"] == "3" ? " disabled" : "";
                    $extra = "id =\"ANSWER_SELECT_COUNT_".$i."\" style=\"text-align: left;\"".$dis;
                    $SetWk["ANSWER_SELECT_COUNT"] = knjCreateTextBox($objForm, $savedat[$i-1]["ANSWER_SELECT_COUNT"], "ANSWER_SELECT_COUNT_".$i, 2, 2, $extra);

                } else {
                    $SetWk["NAME2"] = $model->field["NAME2_".$i];
                }
                $arg["data"][] = $SetWk;
            }
        }

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
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"] = "window.open('knjz407index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz407Form2.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if (is_array($query)) {
        $opt = $query;
        $nameArray = array();
        $valArray = array_column($opt, 'value');
        if (get_count($valArray) > 0) {
            $srchidx = array_search($value, $valArray);
            if ($srchidx) $value_flg = true;
        }
    } else {
        if ($blank) $opt[] = array("label" => "", "value" => "");
        $result = $db->query($query);
        while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);

            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    // return $opt[1]["value"];
}
?>
