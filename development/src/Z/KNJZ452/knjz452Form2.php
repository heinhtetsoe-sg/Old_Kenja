<?php

require_once('for_php7.php');

class knjz452Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz452index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg")) {
            $setRScd = ($model->div == "1") ? $model->resultCd: $model->testCd;
            $Row = $db->getRow(knjz452Query::getRow($model, $setRScd), DB_FETCHMODE_ASSOC);
            $Row["CERT_FLG"] = ($Row["CERT_FLG"] == "T") ? "1": "2";
            if ($model->div == "1") {
                $Row["KYUU_CD1"]   = substr($Row["RESULT_CD"], 0, 3);
                $Row["JOUKYOUCD1"] = substr($Row["RESULT_CD"], 3, 1);
            } else {
                $Row["KYUU_CD2"]   = substr($Row["TEST_CD"], 0, 3);
                $Row["JOUKYOUCD2"] = substr($Row["TEST_CD"], 3, 1);
            }
        } else {
            $Row =& $model->field;
        }

        if ($model->div == "1") {
            $arg["kekka"] = 1;
            $arg["kyuu"]  = "";
        } else {
            $arg["kekka"] = "";
            $arg["kyuu"]  = 1;
        }

        /******** 受験結果 ********/
        //級CD
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["KYUU_CD1"] = knjCreateTextBox($objForm, $Row["KYUU_CD1"], "KYUU_CD1", 3, 3, $extra);

        //状況CD
        $query = knjz452Query::getNameMst($model, "Z050");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "JOUKYOUCD1", $Row["JOUKYOUCD1"], $extra, 1, "");

        //結果名
        $extra = "";
        $arg["data"]["RESULT_NAME"] = knjCreateTextBox($objForm, $Row["RESULT_NAME"], "RESULT_NAME", 41, 40, $extra);

        //結果名略称
        $extra = "";
        $arg["data"]["RESULT_NAME_ABBV"] = knjCreateTextBox($objForm, $Row["RESULT_NAME_ABBV"], "RESULT_NAME_ABBV", 9, 8, $extra);

        //正式資格ラジオ（1:Ｔ, 2:Ｆ）
        $opt = array(1, 2);
        $Row["CERT_FLG"] = ($Row["CERT_FLG"] == "") ? "1" : $Row["CERT_FLG"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"CERT_FLG{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "CERT_FLG", $Row["CERT_FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //有効期間
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["LIMITED_PERIOD"] = knjCreateTextBox($objForm, $Row["LIMITED_PERIOD"], "LIMITED_PERIOD", 2, 2, $extra);

        //レベル
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["RESULT_LEVEL"] = knjCreateTextBox($objForm, $Row["RESULT_LEVEL"], "RESULT_LEVEL", 3, 3, $extra);

        /******** 受験級 ********/
        //級CD
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["KYUU_CD2"] = knjCreateTextBox($objForm, $Row["KYUU_CD2"], "KYUU_CD2", 3, 3, $extra);

        //状況CD
        $query = knjz452Query::getNameMst($model, "Z051");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "JOUKYOUCD2", $Row["JOUKYOUCD2"], $extra, 1, "");

        //受験級名称
        $extra = "";
        $arg["data"]["TEST_NAME"] = knjCreateTextBox($objForm, $Row["TEST_NAME"], "TEST_NAME", 41, 40, $extra);

        //受験級略称
        $extra = "";
        $arg["data"]["TEST_NAME_ABBV"] = knjCreateTextBox($objForm, $Row["TEST_NAME_ABBV"], "TEST_NAME_ABBV", 33, 32, $extra);

        //受験料
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["TEST_FEE"] = knjCreateTextBox($objForm, $Row["TEST_FEE"], "TEST_FEE", 5, 5, $extra);

        //前提結果CD
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["PREREQ_RESALT_CD"] = knjCreateTextBox($objForm, $Row["PREREQ_RESALT_CD"], "PREREQ_RESALT_CD", 4, 4, $extra);

        //レベル
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["TEST_LEVEL"] = knjCreateTextBox($objForm, $Row["TEST_LEVEL"], "TEST_LEVEL", 3, 3, $extra);

        /******** NOT_PRINT, SCORE ********/
        if ($model->cntNotPrintColumn > 0) {
            //出力設定
            $extra = ($Row["NOT_PRINT"] == "1") ? "checked" : "";
            $arg["data"]["NOT_PRINT"] = knjCreateCheckBox($objForm, "NOT_PRINT", "1", $extra);

            //得点
            $extra = "";
            $arg["data"]["SCORE"] = knjCreateTextBox($objForm, $Row["SCORE"], "SCORE", 4, 4, $extra);
        }

        /********/
        /*ボタン*/
        /********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->left_frame);
        knjCreateHidden($objForm, "SIKAKUCD", $model->sikakuCd);
        knjCreateHidden($objForm, "DIV", $model->div);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz452index.php?cmd=list&DIV={$model->div}';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz452Form2.html", $arg); 
    }
}

/********************************************* 以下関数 *******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($name == "BUNRIDIV") {
        $opt[] = array("label" => "", "value" => "0");
    }
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
