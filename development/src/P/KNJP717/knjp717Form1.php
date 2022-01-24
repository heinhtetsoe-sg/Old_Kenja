<?php

require_once('for_php7.php');

class knjp717Form1 
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp717index.php", "", "edit");

        //１レコード取得
        if (!$model->isWarning() && $model->cmd != "yuucho") {
            $Row = array();
            $seqList = array("1", "2");
            $fieldList = array("BANKCD", "BRANCHCD", "DEPOSIT_ITEM", "ACCOUNTNO", "ACCOUNTNAME", "RELATIONSHIP");
            foreach ($seqList as $seq) {
                $tmpRow = array();
                $tmpRow = knjp717Query::getRow($model, $seq);
                foreach ($fieldList as $fieldName) {
                    $Row[$fieldName.$seq] = $tmpRow[$fieldName];
                }
            }
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //生徒名
        $row = $db->getRow(knjp717Query::getStudentName($model),DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $row["SCHREGNO"];
        if ($model->search_div == "1") {
            $arg["NAME_SHOW"] = $row["NAME"];
        } else {
            $arg["NAME_SHOW"] = $row["NAME_SHOW"];
        }

        //SEQ
        $seqList = array("1", "2");
        foreach ($seqList as $seq) {
            if ($seq == "1" && !$Row["BANKCD".$seq]) {
                $query = knjp717Query::getGuardianData($model);
                $guardData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $Row["DEPOSIT_ITEM".$seq] = "1";
                $Row["ACCOUNTNAME".$seq] = str_replace("　", " ", $guardData["GUARD_KANA"]);
                $Row["RELATIONSHIP".$seq] = $guardData["RELATIONSHIP"];
            }
            //銀行コードコンボ
            $extra = "OnChange=\"SetBranch(this, '{$seq}');\"";
            makeCmbBank($objForm, $arg, $model, $model->bankList, $Row["BANKCD".$seq], "BANKCD".$seq, $extra, 1, "BLANK");

            //銀行検索
            $extra = "onkeyup=\"SetBank(this, '{$seq}');\"";
            $arg["data"]["BANKCD_SEARCH".$seq] = knjCreateTextBox($objForm, $Row["BANKCD_SEARCH".$seq], "BANKCD_SEARCH".$seq, 32, 48, $extra);

            $branchTextShow = "none";
            $branchCmbShow = "block";
            if ($Row["BANKCD".$seq] == $model->yuucho) {
                $branchTextShow = "block";
                $branchCmbShow = "none";
            }
            //支店textbox
            $extra = " style=\"display:{$branchTextShow}\" onBlur=\"this.value=toInteger(this.value);\"";
            $arg["data"]["BRANCHCD_T".$seq] = knjCreateTextBox($objForm, $Row["BRANCHCD".$seq], "BRANCHCD_T".$seq, 3, 3, $extra);

            //支店コードコンボ
            $query = knjp717Query::getBranchcd($Row["BANKCD".$seq]);
            $extra = " style=\"display:{$branchCmbShow}\" ";
            makeCmb($objForm, $arg, $db, $query, $Row["BRANCHCD".$seq], "BRANCHCD_C".$seq, $extra, 1, "");

            //預金種目コンボ
            $query = knjp717Query::getNameMst("G203");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $Row["DEPOSIT_ITEM".$seq], "DEPOSIT_ITEM".$seq, $extra, 1, "BLANK");

            //口座番号
            $extra = "onBlur=\"this.value=toInteger(this.value);\"";
            $arg["data"]["ACCOUNTNO".$seq] = knjCreateTextBox($objForm, $Row["ACCOUNTNO".$seq], "ACCOUNTNO".$seq, 7, 7, $extra);

            //口座名義
            $extra = "";
            $arg["data"]["ACCOUNTNAME".$seq] = knjCreateTextBox($objForm, $Row["ACCOUNTNAME".$seq], "ACCOUNTNAME".$seq, 32, 48, $extra);

            //続柄コンボ
            $query = knjp717Query::getNameMst("H201");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $Row["RELATIONSHIP".$seq], "RELATIONSHIP".$seq, $extra, 1, "BLANK");
        }

        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp717Form1.html", $arg);
    }
}
//makeCmbBank
function makeCmbBank(&$objForm, &$arg, &$model, $bankList, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $model->bankMst = array();
    $model->bankName = array();
    foreach ($bankList as $row) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
        $model->bankMst[] = $row;
        $model->bankName[] = $row["BANKNAME"];
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
