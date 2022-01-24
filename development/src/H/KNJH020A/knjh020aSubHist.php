<?php

require_once('for_php7.php');

class knjh020aSubHist
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh020aindex.php", "", "sel");

        $db = Query::dbCheckOut();

        //履歴入力ボタン
        $link = REQUESTROOT."/H/KNJH020A/knjh020aindex.php?cmd=rireki&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["btn_rireki"] = knjCreateBtn($objForm, "btn_rireki", "保護者履歴修正", $extra);

        $bgYellow = "bgcolor=\"yellow\"";
        $bgWhite = "bgcolor=\"white\"";

        $rirekiFlg = false;
        $query = knjh020aQuery::getGuardianHist($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["ISSUEDATE"] = str_replace("-", "/", $row["ISSUEDATE"]);
            $row["EXPIREDATE"] = str_replace("-", "/", $row["EXPIREDATE"]);
            $arg["rireki"][] = $row;
            $rirekiFlg = true;
        }
        $result->free();

        $setRow = array();
        if (isset($model->hist_issuedate) && !isset($model->warning) && $model->cmd != "changeCmb") {
            if ($rirekiFlg && $model->hist_issuedate) {
                $query = knjh020aQuery::getGuardianHistData($model->schregno, $model->hist_issuedate);
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } elseif (!$rirekiFlg) {
                $query = knjh020aQuery::getGuardianAddr($model->schregno, "");
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
            //クリア
            $model->hist_issuedate = "";
        } else {
            $setRow =& $model->histField;
        }

        //開始日
        $arg["data"]["ISSUEDATE"] = View::popUpCalendar($objForm, "ISSUEDATE", str_replace("-", "/", $setRow["ISSUEDATE"]), "");

        //終了日
        $arg["data"]["EXPIREDATE"] = View::popUpCalendar($objForm, "EXPIREDATE", str_replace("-", "/", $setRow["EXPIREDATE"]), "");

        //保護者氏名
        $extra = "";
        $arg["data"]["GUARD_NAME"] = knjCreateTextBox($objForm, $setRow["GUARD_NAME"], "GUARD_NAME", 40, 60, $extra);

        //保護者氏名チェックボックス
        $extra = $setRow["GUARD_NAME_FLG"] ? "checked " : "";
        $arg["data"]["GUARD_NAME_BK"] = $setRow["GUARD_NAME_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["GUARD_NAME_FLG"] = knjCreateCheckBox($objForm, "GUARD_NAME_FLG", "1", $extra);

        //保護者かな
        $extra = "";
        $arg["data"]["GUARD_KANA"] = knjCreateTextBox($objForm, $setRow["GUARD_KANA"], "GUARD_KANA", 80, 120, $extra);

        //保護者かなチェックボックス
        $extra = $setRow["GUARD_KANA_FLG"] ? "checked " : "";
        $arg["data"]["GUARD_KANA_BK"] = $setRow["GUARD_KANA_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["GUARD_KANA_FLG"] = knjCreateCheckBox($objForm, "GUARD_KANA_FLG", "1", $extra);

        //戸籍氏名
        $extra = "";
        $arg["data"]["GUARD_REAL_NAME"] = knjCreateTextBox($objForm, $setRow["GUARD_REAL_NAME"], "GUARD_REAL_NAME", 40, 60, $extra);

        //戸籍氏名チェックボックス
        $extra = $setRow["GUARD_REAL_NAME_FLG"] ? "checked " : "";
        $arg["data"]["GUARD_REAL_NAME_BK"] = $setRow["GUARD_REAL_NAME_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["GUARD_REAL_NAME_FLG"] = knjCreateCheckBox($objForm, "GUARD_REAL_NAME_FLG", "1", $extra);

        //戸籍氏名かな
        $extra = "";
        $arg["data"]["GUARD_REAL_KANA"] = knjCreateTextBox($objForm, $setRow["GUARD_REAL_KANA"], "GUARD_REAL_KANA", 80, 120, $extra);

        //戸籍氏名かなチェックボックス
        $extra = $setRow["GUARD_REAL_KANA_FLG"] ? "checked " : "";
        $arg["data"]["GUARD_REAL_KANA_BK"] = $setRow["GUARD_REAL_KANA_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["GUARD_REAL_KANA_FLG"] = knjCreateCheckBox($objForm, "GUARD_REAL_KANA_FLG", "1", $extra);

        //性別コンボボックス
        $query = knjh020aQuery::getNameMst("Z002");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $setRow["GUARD_SEX"], "GUARD_SEX", $extra, 1, "BLANK");

        //性別チェックボックス
        $extra = $setRow["GUARD_SEX_FLG"] ? "checked " : "";
        $arg["data"]["GUARD_SEX_BK"] = $setRow["GUARD_SEX_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["GUARD_SEX_FLG"] = knjCreateCheckBox($objForm, "GUARD_SEX_FLG", "1", $extra);

        //誕生日
        $arg["data"]["GUARD_BIRTHDAY"] = View::popUpCalendar($objForm, "GUARD_BIRTHDAY", str_replace("-", "/", $setRow["GUARD_BIRTHDAY"]), "");

        //誕生日チェックボックス
        $extra = $setRow["GUARD_BIRTHDAY_FLG"] ? "checked " : "";
        $arg["data"]["GUARD_BIRTHDAY_BK"] = $setRow["GUARD_BIRTHDAY_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["GUARD_BIRTHDAY_FLG"] = knjCreateCheckBox($objForm, "GUARD_BIRTHDAY_FLG", "1", $extra);

        //続柄コンボボックス
        $query = knjh020aQuery::getNameMst("H201");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $setRow["RELATIONSHIP"], "RELATIONSHIP", $extra, 1, "BLANK");

        //続柄チェックボックス
        $extra = $setRow["RELATIONSHIP_FLG"] ? "checked " : "";
        $arg["data"]["RELATIONSHIP_BK"] = $setRow["RELATIONSHIP_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["RELATIONSHIP_FLG"] = knjCreateCheckBox($objForm, "RELATIONSHIP_FLG", "1", $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('histAdd')\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('histUpd')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('histDel')\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //戻るボタン
        $link = REQUESTROOT."/H/KNJH020A/knjh020aindex.php?cmd=edit";
        $extra = "onclick=\"document.forms[0].ISSUEDATE.value=''; window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh020aSubHist.html", $arg);
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
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } elseif ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
