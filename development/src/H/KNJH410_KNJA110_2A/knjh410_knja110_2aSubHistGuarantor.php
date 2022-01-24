<?php

require_once('for_php7.php');

class knjh410_knja110_2aSubHistGuarantor {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjh410_knja110_2aindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $Row = $db->getRow(knjh410_knja110_2aQuery::getSchregno_name($model),DB_FETCHMODE_ASSOC);
        $arg["NO"]   = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        //名称マスタより名称取得
        $nameM = array();
        $query = knjh410_knja110_2aQuery::get_name_mst();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nameM[$row["NAMECD1"]][$row["NAMECD2"]] = $row["NAME1"];
        }

        //履歴一覧
        $rirekiFlg = false;
        $query = knjh410_knja110_2aQuery::getGuarantorHistDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["ISSUEDATE"]   = str_replace("-","/",$row["ISSUEDATE"]);
            $row["EXPIREDATE"]  = str_replace("-","/",$row["EXPIREDATE"]);
            $row["GUARANTOR_SEX_NAME"] = $nameM["Z002"][$row["GUARANTOR_SEX"]];
            $row["GUARANTOR_RELATIONSHIP_NAME"] = $nameM["H201"][$row["GUARANTOR_RELATIONSHIP"]];

            $arg["rireki"][] = $row;
            $rirekiFlg = true;
        }
        $result->free();

        //履歴データ
        $setRow = array();
        if (!isset($model->warning) && $model->cmd != "changeCmb2") {
            if ($rirekiFlg && $model->hist_issuedate) {
                $query = knjh410_knja110_2aQuery::getGuarantorHistDat($model, $model->hist_issuedate);
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } else if (!$rirekiFlg) {
                $query = knjh410_knja110_2aQuery::getGuardianDat($model, $model->schregno);
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
            //クリア
            $model->hist_issuedate = "";
        } else {
            $setRow =& $model->histField;
        }

        //背景色
        $bgYellow = "bgcolor=\"yellow\"";
        $bgWhite  = "bgcolor=\"white\"";

        //開始日
        $arg["data"]["ISSUEDATE"] = View::popUpCalendar($objForm, "ISSUEDATE", str_replace("-", "/", $setRow["ISSUEDATE"]),"");

        //終了日
        $arg["data"]["EXPIREDATE"] = View::popUpCalendar($objForm, "EXPIREDATE", str_replace("-", "/", $setRow["EXPIREDATE"]),"");

        //保証人氏名チェックボックス
        $id = "GUARANTOR_NAME_BK";
        $extra  = $setRow["GUARANTOR_NAME_FLG"] ? "checked " : "";
        $extra .= " onclick=\"chgBGColor(this, '$id');\" ";
        $arg["data"]["GUARANTOR_NAME_FLG"] = knjCreateCheckBox($objForm, "GUARANTOR_NAME_FLG", "1", $extra);
        //保証人氏名チェックボックス（背景色）
        $arg["data"][$id]  = $setRow["GUARANTOR_NAME_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"][$id] .= " id='$id'";
        //保証人氏名
        $extra = "";
        $arg["data"]["GUARANTOR_NAME"] = knjCreateTextBox($objForm, $setRow["GUARANTOR_NAME"], "GUARANTOR_NAME", 40, 60, $extra);

        //保証人かなチェックボックス
        $id = "GUARANTOR_KANA_BK";
        $extra  = $setRow["GUARANTOR_KANA_FLG"] ? "checked " : "";
        $extra .= " onclick=\"chgBGColor(this, '$id');\" ";
        $arg["data"]["GUARANTOR_KANA_FLG"] = knjCreateCheckBox($objForm, "GUARANTOR_KANA_FLG", "1", $extra);
        //保証人かなチェックボックス（背景色）
        $arg["data"][$id]  = $setRow["GUARANTOR_KANA_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"][$id] .= " id='$id'";
        //保証人かな
        $extra = "";
        $arg["data"]["GUARANTOR_KANA"] = knjCreateTextBox($objForm, $setRow["GUARANTOR_KANA"], "GUARANTOR_KANA", 40, 120, $extra);

        //戸籍氏名チェックボックス
        $id = "GUARANTOR_REAL_NAME_BK";
        $extra  = $setRow["GUARANTOR_REAL_NAME_FLG"] ? "checked " : "";
        $extra .= " onclick=\"chgBGColor(this, '$id');\" ";
        $arg["data"]["GUARANTOR_REAL_NAME_FLG"] = knjCreateCheckBox($objForm, "GUARANTOR_REAL_NAME_FLG", "1", $extra);
        //戸籍氏名チェックボックス（背景色）
        $arg["data"][$id]  = $setRow["GUARANTOR_REAL_NAME_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"][$id] .= " id='$id'";
        //戸籍氏名
        $extra = "";
        $arg["data"]["GUARANTOR_REAL_NAME"] = knjCreateTextBox($objForm, $setRow["GUARANTOR_REAL_NAME"], "GUARANTOR_REAL_NAME", 40, 60, $extra);

        //戸籍氏名かなチェックボックス
        $id = "GUARANTOR_REAL_KANA_BK";
        $extra  = $setRow["GUARANTOR_REAL_KANA_FLG"] ? "checked " : "";
        $extra .= " onclick=\"chgBGColor(this, '$id');\" ";
        $arg["data"]["GUARANTOR_REAL_KANA_FLG"] = knjCreateCheckBox($objForm, "GUARANTOR_REAL_KANA_FLG", "1", $extra);
        //戸籍氏名かなチェックボックス（背景色）
        $arg["data"][$id]  = $setRow["GUARANTOR_REAL_KANA_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"][$id] .= " id='$id'";
        //戸籍氏名かな
        $extra = "";
        $arg["data"]["GUARANTOR_REAL_KANA"] = knjCreateTextBox($objForm, $setRow["GUARANTOR_REAL_KANA"], "GUARANTOR_REAL_KANA", 40, 120, $extra);

        //性別チェックボックス
        $id = "GUARANTOR_SEX_BK";
        $extra  = $setRow["GUARANTOR_SEX_FLG"] ? "checked " : "";
        $extra .= " onclick=\"chgBGColor(this, '$id');\" ";
        $arg["data"]["GUARANTOR_SEX_FLG"] = knjCreateCheckBox($objForm, "GUARANTOR_SEX_FLG", "1", $extra);
        //性別チェックボックス（背景色）
        $arg["data"][$id]  = $setRow["GUARANTOR_SEX_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"][$id] .= " id='$id'";
        //性別コンボボックス
        $query = knjh410_knja110_2aQuery::getNameMst("Z002");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $setRow["GUARANTOR_SEX"], "GUARANTOR_SEX", $extra, 1, "BLANK");

        //続柄チェックボックス
        $id = "GUARANTOR_RELATIONSHIP_BK";
        $extra  = $setRow["GUARANTOR_RELATIONSHIP_FLG"] ? "checked " : "";
        $extra .= " onclick=\"chgBGColor(this, '$id');\" ";
        $arg["data"]["GUARANTOR_RELATIONSHIP_FLG"] = knjCreateCheckBox($objForm, "GUARANTOR_RELATIONSHIP_FLG", "1", $extra);
        //続柄チェックボックス（背景色）
        $arg["data"][$id]  = $setRow["GUARANTOR_RELATIONSHIP_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"][$id] .= " id='$id'";
        //続柄コンボボックス
        $query = knjh410_knja110_2aQuery::getNameMst("H201");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $setRow["GUARANTOR_RELATIONSHIP"], "GUARANTOR_RELATIONSHIP", $extra, 1, "BLANK");

        //追加ボタン
        $extra = "onclick=\"return btn_submit('histAdd2')\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('histUpd2')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = "onclick=\"return btn_submit('histDel2')\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //戻るボタン
        $link = REQUESTROOT."/U/KNJH410_KNJA110_2A/knjh410_knja110_2aindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"document.forms[0].ISSUEDATE.value=''; window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjh410_knja110_2aSubHistGuarantor.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
