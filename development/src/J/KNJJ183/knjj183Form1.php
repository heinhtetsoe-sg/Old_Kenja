<?php

require_once('for_php7.php');

class knjj183Form1 {
    function main(&$model) {
        //フォーム作成
        $objForm = new form;
        $arg["start"]  = $objForm->get_start("edit", "POST", "knjj183index.php", "", "edit");
        $arg["reload"] = "";
        $db = Query::dbCheckOut();

        $row1 = $db->getRow(knjj183Query::getSchData($model->schregno),DB_FETCHMODE_ASSOC);
        $row2 = $db->getRow(knjj183Query::getBranchMst($model->schregno, $model->schoolkind, ""),DB_FETCHMODE_ASSOC);
        $row3 = $db->getRow(knjj183Query::getNameMst("H108"),DB_FETCHMODE_ASSOC);
        $rowShow  = array_merge((array)$row1,(array)$row2,(array)$row3);
        if (!$model->isWarning()) {
            $row  = array_merge((array)$row1,(array)$row2,(array)$row3);
        } else {
            $row =& $model->field;
        }
        //生徒情報
        $arg["data"] = array("ABBV1"           =>  $rowShow["ABBV1"],
                             "GRADE"           =>  $rowShow["GRADE"],
                             "HR_CLASS"        =>  $rowShow["HR_CLASS"],
                             "ATTENDNO"        =>  $rowShow["ATTENDNO"],
                             "SCHREGNO"        =>  $rowShow["SCHREGNO"],
                             "NAME"            =>  $rowShow["NAME"],
                             "NAME_KANA"       =>  $rowShow["NAME_KANA"],
                             "SEX"             =>  $rowShow["SEX"],
                             "JFIN_NAME"       =>  $rowShow["JFIN_NAME"],
                             "HFIN_NAME"       =>  $rowShow["HFIN_NAME"],
                             "ZIPCD"           =>  $rowShow["ZIPCD"],
                             "ADDR1"           =>  $rowShow["ADDR1"],
                             "ADDR2"           =>  $rowShow["ADDR2"],
                             "TELNO"           =>  $rowShow["TELNO"],
                             "TELNO2"          =>  $rowShow["TELNO2"],
                             "BIRTHDAY"        =>  $rowShow["BIRTHDAY"]);
        //会員情報
        //会員氏名
        $extra = "";
        $arg["data"]["GUARD_NAME"] = knjCreateTextBox($objForm, $row["GUARD_NAME"], "GUARD_NAME", 40, 120, $extra);

        //会員ふりがな
        $extra = "";
        $arg["data"]["GUARD_KANA"] = knjCreateTextBox($objForm, $row["GUARD_KANA"], "GUARD_KANA", 80, 240, $extra);

        //郵便番号
        $arg["data"]["GUARD_ZIPCD"] = View::popUpZipCode($objForm, "GUARD_ZIPCD", $row["GUARD_ZIPCD"],"GUARD_ADDR1");

        //会員住所
        $extra = "";
        $arg["data"]["GUARD_ADDR1"] = knjCreateTextBox($objForm, $row["GUARD_ADDR1"], "GUARD_ADDR1", 50, 150, $extra);

        //会員住所２
        $extra = "";
        $arg["data"]["GUARD_ADDR2"] = knjCreateTextBox($objForm, $row["GUARD_ADDR2"], "GUARD_ADDR2", 50, 150, $extra);

        //電話番号
        $extra = "";
        $arg["data"]["GUARD_TELNO"] = knjCreateTextBox($objForm, $row["GUARD_TELNO"], "GUARD_TELNO", 16, 14, $extra);

        //電話番号２
        $extra = "";
        $arg["data"]["GUARD_TELNO2"] = knjCreateTextBox($objForm, $row["GUARD_TELNO2"], "GUARD_TELNO2", 16, 14, $extra);

        //支部
        $query = knjj183Query::getBranchName();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "BRANCHCD", $row["BRANCHCD"], $extra, 1, "BLANK");

        //役員
        $query = knjj183Query::getNameMst("J007");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "BRANCH_POSITION", $row["BRANCH_POSITION"], $extra, 1, "BLANK");

        //通学
        $query = knjj183Query::getNameMst("J008");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "RESIDENTCD", $row["RESIDENTCD"], $extra, 1, "BLANK");

        //送付先名
        $extra = "";
        $arg["data"]["SEND_NAME"] = knjCreateTextBox($objForm, $row["SEND_NAME"], "SEND_NAME", 40, 120, $extra);

        //保護者よりコピー用
        $result = $db->query(knjj183Query::getGuardianAddr($model->schregno));
        while ($dataHidd = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($dataHidd as $key => $value) {
                //hiddenで保持
                knjCreateHidden($objForm, "HID_".$key, $value);
            }
            $judgeHid = array_filter($dataHidd);
        }
        $daHiVal = (empty($judgeHid)) ? "" : "1";
        knjCreateHidden($objForm, "HID_NOT", $daHiVal);
        //前年度よりコピー用
        $result = $db->query(knjj183Query::getBranchMst($model->schregno, $model->schoolkind, "lastyear"));
        while ($dataHidd2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($dataHidd2 as $key => $value) {
                //hiddenで保持
                knjCreateHidden($objForm, "HID2_".$key, $value);
            }
            $judgeHid2 = array_filter($dataHidd2);
        }
        $daHiVal2 = (empty($judgeHid2)) ? "" : 1;
        knjCreateHidden($objForm, "HID2_NOT", $daHiVal2);

        /*ボタン作成*/
        //コピーボタン（保護者より）
        $extra = "style=\"width:130px\"onclick=\"copy(1);\"";
        $arg["button"]["btn_copy1"] = knjCreateBtn($objForm, "btn_copy1", "保護者よりコピー", $extra);
        //コピーボタン（前年度より）
        $extra = "style=\"width:130px\"onclick=\"copy(2);\"";
        $arg["button"]["btn_copy2"] = knjCreateBtn($objForm, "btn_copy2", "前年度よりコピー", $extra);
        //更新ボタン 
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更新", $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');
        //削除ボタン 
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削除", $extra);
        //取消ボタン 
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取消", $extra);
        //終了ボタン 
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        //家族情報登録ボタン
        //KNJJ183_FAMILYへのリンク
        $link1 = REQUESTROOT."/J/KNJJ183_FAMILY/knjj183_familyindex.php?AUTH=".AUTHORITY."&SCHREGNO=".$model->schregno."&DAMMY=DAMMY";
        $extra = "style=\"width:100px\" onclick=\" Page_jumper('$link1','".$model->schregno."');\"";
        $arg["button"]["btn_family"] = knjCreateBtn($objForm, "btn_family", "家族情報登録", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjj183Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
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
