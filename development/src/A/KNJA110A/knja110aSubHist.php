<?php

require_once('for_php7.php');

class knja110aSubHist
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knja110aindex.php", "", "sel");

        $db = Query::dbCheckOut();

        $bgYellow = "bgcolor=\"yellow\"";
        $bgWhite = "bgcolor=\"white\"";

        $rirekiFlg = false;
        $query = knja110aQuery::getSchregHist($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setDate = str_replace("-", "/", $row["ISSUEDATE"]);
            $row["EXPIREDATE"] = str_replace("-", "/", $row["EXPIREDATE"]);
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if (!$model->selectSchoolKind || $model->selectSchoolKind && in_array($row["SCHOOL_KIND"], explode(':', $model->selectSchoolKind))) {
                    $row["ISSUEDATE"] = "<a href=\"knja110aindex.php?cmd=histEdit&HIST_ISSUEDATE={$row["ISSUEDATE"]}\">{$setDate}</a>";
                } else {
                    $row["ISSUEDATE"] = $setDate;
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                if ($row["SCHOOL_KIND"] == SCHOOLKIND) {
                    $row["ISSUEDATE"] = "<a href=\"knja110aindex.php?cmd=histEdit&HIST_ISSUEDATE={$row["ISSUEDATE"]}\">{$setDate}</a>";
                } else {
                    $row["ISSUEDATE"] = $setDate;
                }
            } else {
                $row["ISSUEDATE"] = "<a href=\"knja110aindex.php?cmd=histEdit&HIST_ISSUEDATE={$row["ISSUEDATE"]}\">{$setDate}</a>";
            }
            if ($model->Properties["useSchool_KindField"] == "1") {
                $row["GRADE_HR_ATTE"] = $row["GRADE_HR_ATTE"]."({$row["SCHOOL_KIND"]})";
            }
            $arg["rireki"][] = $row;
            $rirekiFlg = true;
        }
        $result->free();

        $setRow = array();
        if (!isset($model->warning) && $model->cmd != "changeCmb") {
            if ($rirekiFlg && $model->hist_issuedate) {
                $query = knja110aQuery::getSchregHistData($model->schregno, $model->hist_issuedate);
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } elseif (!$rirekiFlg) {
                $query = knja110aQuery::getSchregData($model->schregno);
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

        //年度
        $query = knja110aQuery::getYear($model);
        $extra = "onChange=\"return btn_submit('changeCmb')\"";
        makeCmb($objForm, $arg, $db, $query, $setRow["YEAR"], "YEAR", $extra, 1, "BLANK");

        //学期
        $query = knja110aQuery::getSeme($setRow);
        $extra = "onChange=\"return btn_submit('changeCmb')\"";
        makeCmb($objForm, $arg, $db, $query, $setRow["SEMESTER"], "SEMESTER", $extra, 1, "BLANK");

        //出席番号
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["ATTENDNO"] = knjCreateTextBox($objForm, $setRow["ATTENDNO"], "ATTENDNO", 3, 3, $extra);

        //年組コンボボックス
        $query = knja110aQuery::getGrdClasHist($setRow["YEAR"], $setRow["SEMESTER"], $model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $setRow["GRADE_CLASS"], "GRADE_CLASS", $extra, 1, "BLANK");

        //年組番チェックボックス
        $extra = $setRow["GRADE_FLG"] || $setRow["HR_CLASS_FLG"] || $setRow["ATTENDNO_FLG"] ? "checked " : "";
        $arg["data"]["GRADE_CLASS_BK"] = $setRow["GRADE_FLG"] || $setRow["HR_CLASS_FLG"] || $setRow["ATTENDNO_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["GRADE_HR_ATTE_FLG"] = knjCreateCheckBox($objForm, "GRADE_HR_ATTE_FLG", "1", $extra);

        //年次
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["ANNUAL"] = knjCreateTextBox($objForm, $setRow["ANNUAL"], "ANNUAL", 2, 2, $extra);

        //年次チェックボックス
        $extra = $setRow["ANNUAL_FLG"] ? "checked " : "";
        $arg["data"]["ANNUAL_BK"] = $setRow["ANNUAL_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["ANNUAL_FLG"] = knjCreateCheckBox($objForm, "ANNUAL_FLG", "1", $extra);

        //課程学科
        $value_flg = false;
        $opt_coursecd = array();
        $opt_coursecd[] = array('label' => "", 'value' => "");
        $value = $setRow["COURSEMAJORCD"];
        $result = $db->query(knja110aQuery::getCourseSubjectHist($setRow["YEAR"]));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_coursecd[] = array('label' => str_replace(",", "", $row["COURSEMAJORCD"])."  ".htmlspecialchars($row["COURSE_SUBJECT"]),
                                    'value' => $row["COURSEMAJORCD"]);

            if ($value == $row["COURSEMAJORCD"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $value = ($value != "" && $value_flg) ? $value : $opt_coursecd[0]["value"];
        $extra = "";
        $arg["data"]["COURSEMAJORCD"] = knjCreateCombo($objForm, "COURSEMAJORCD", $value, $opt_coursecd, $extra, 1);

        //課程チェックボックス
        $extra = $setRow["COURSECD_FLG"] ? "checked " : "";
        $arg["data"]["COURSEMAJORCD_BK"] = $setRow["COURSECD_FLG"] || $setRow["MAJORCD_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["COURSECD_FLG"] = knjCreateCheckBox($objForm, "COURSECD_FLG", "1", $extra);

        //学科チェックボックス
        $extra = $setRow["MAJORCD_FLG"] ? "checked " : "";
        $arg["data"]["MAJORCD_FLG"] = knjCreateCheckBox($objForm, "MAJORCD_FLG", "1", $extra);

        //コース
        $value_flg = false;
        $opt_course3 = array();
        $opt_course3[] = array('label' => "", 'value' => "");
        $value = $setRow["COURSECODE"];
        $result = $db->query(knja110aQuery::getCourseCodeHist($setRow["YEAR"]));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_course3[] = array('label'  => $row["COURSECODE"]."  ".htmlspecialchars($row["COURSECODENAME"]),
                                    'value' => $row["COURSECODE"]);

            if ($value == $row["COURSECODE"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $value = ($value != "" && $value_flg) ? $value : $opt_course3[0]["value"];
        $extra = "";
        $arg["data"]["COURSECODE"] = knjCreateCombo($objForm, "COURSECODE", $value, $opt_course3, $extra, 1);

        //コースチェックボックス
        $extra = $setRow["COURSECODE_FLG"] ? "checked " : "";
        $arg["data"]["COURSECODE_BK"] = $setRow["COURSECODE_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["COURSECODE_FLG"] = knjCreateCheckBox($objForm, "COURSECODE_FLG", "1", $extra);

        //学籍番号
        $arg["data"]["SCHREGNO"] = $setRow["SCHREGNO"];

        //氏名
        $extra = "onBlur=\" Name_Clip(this);\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $setRow["NAME"], "NAME", 50, 80, $extra);
        $arg["data"]["NAMESLEN"] = $model->nameSLen;

        //氏名チェックボックス
        $extra = $setRow["NAME_FLG"] ? "checked " : "";
        $arg["data"]["NAME_BK"] = $setRow["NAME_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NAME_FLG"] = knjCreateCheckBox($objForm, "NAME_FLG", "1", $extra);

        //表示用氏名
        $extra = "";
        $arg["data"]["NAME_SHOW"] = knjCreateTextBox($objForm, $setRow["NAME_SHOW"], "NAME_SHOW", 20, 30, $extra);

        //表示用氏名チェックボックス
        $extra = $setRow["NAME_SHOW_FLG"] ? "checked " : "";
        $arg["data"]["NAME_SHOW_BK"] = $setRow["NAME_SHOW_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NAME_SHOW_FLG"] = knjCreateCheckBox($objForm, "NAME_SHOW_FLG", "1", $extra);

        //氏名かな
        $extra = "";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $setRow["NAME_KANA"], "NAME_KANA", 80, 160, $extra);

        //氏名かなチェックボックス
        $extra = $setRow["NAME_KANA_FLG"] ? "checked " : "";
        $arg["data"]["NAME_KANA_BK"] = $setRow["NAME_KANA_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NAME_KANA_FLG"] = knjCreateCheckBox($objForm, "NAME_KANA_FLG", "1", $extra);

        //英字氏名
        $extra = "";
        $arg["data"]["NAME_ENG"] = knjCreateTextBox($objForm, $setRow["NAME_ENG"], "NAME_ENG", 40, 40, $extra);

        //英字氏名チェックボックス
        $extra = $setRow["NAME_ENG_FLG"] ? "checked " : "";
        $arg["data"]["NAME_ENG_BK"] = $setRow["NAME_ENG_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NAME_ENG_FLG"] = knjCreateCheckBox($objForm, "NAME_ENG_FLG", "1", $extra);

        //戸籍氏名
        $extra = "onBlur=\" Name_Clip(this);\"";
        $arg["data"]["REAL_NAME"] = knjCreateTextBox($objForm, $setRow["REAL_NAME"], "REAL_NAME", 50, 80, $extra);

        //戸籍氏名チェックボックス
        $extra = $setRow["REAL_NAME_FLG"] ? "checked " : "";
        $arg["data"]["REAL_NAME_BK"] = $setRow["REAL_NAME_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["REAL_NAME_FLG"] = knjCreateCheckBox($objForm, "REAL_NAME_FLG", "1", $extra);

        //戸籍氏名かな
        $extra = "";
        $arg["data"]["REAL_NAME_KANA"] = knjCreateTextBox($objForm, $setRow["REAL_NAME_KANA"], "REAL_NAME_KANA", 60, 160, $extra);

        //戸籍氏名かなチェックボックス
        $extra = $setRow["REAL_NAME_KANA_FLG"] ? "checked " : "";
        $arg["data"]["REAL_NAME_KANA_BK"] = $setRow["REAL_NAME_KANA_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["REAL_NAME_KANA_FLG"] = knjCreateCheckBox($objForm, "REAL_NAME_KANA_FLG", "1", $extra);

        //その他
        $arg["data"]["HANDICAP"] = $model->createCombo($objForm, $db, "A025", "HANDICAP", $setRow["HANDICAP"], 1);

        //その他チェックボックス
        $extra = $setRow["HANDICAP_FLG"] ? "checked " : "";
        $arg["data"]["HANDICAP_BK"] = $setRow["HANDICAP_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["HANDICAP_FLG"] = knjCreateCheckBox($objForm, "HANDICAP_FLG", "1", $extra);

        //その他 項目名
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $handicapTitle = "教育区分";
        } else {
            $handicapTitle = "その他";
        }
        $arg["data"]["HANDICAP_TITLE"] = $handicapTitle;

        //第二国籍
        $query = knja110aQuery::getNameMst("A024");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $setRow["NATIONALITY2"], "NATIONALITY2", $extra, 1, "BLANK");

        //第二国籍チェックボックス
        $extra = $setRow["NATIONALITY2_FLG"] ? "checked " : "";
        $arg["data"]["NATIONALITY2_BK"] = $setRow["NATIONALITY2_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NATIONALITY2_FLG"] = knjCreateCheckBox($objForm, "NATIONALITY2_FLG", "1", $extra);

        //氏名
        $extra = "onBlur=\" Name_Clip(this);\"";
        $arg["data"]["NATIONALITY_NAME"] = knjCreateTextBox($objForm, $setRow["NATIONALITY_NAME"], "NATIONALITY_NAME", 50, 80, $extra);

        //氏名チェックボックス
        $extra = $setRow["NATIONALITY_NAME_FLG"] ? "checked " : "";
        $arg["data"]["NATIONALITY_NAME_BK"] = $setRow["NATIONALITY_NAME_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NATIONALITY_NAME_FLG"] = knjCreateCheckBox($objForm, "NATIONALITY_NAME_FLG", "1", $extra);

        //氏名かな
        $extra = "";
        $arg["data"]["NATIONALITY_NAME_KANA"] = knjCreateTextBox($objForm, $setRow["NATIONALITY_NAME_KANA"], "NATIONALITY_NAME_KANA", 80, 160, $extra);

        //氏名かなチェックボックス
        $extra = $setRow["NATIONALITY_NAME_KANA_FLG"] ? "checked " : "";
        $arg["data"]["NATIONALITY_NAME_KANA_BK"] = $setRow["NATIONALITY_NAME_KANA_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NATIONALITY_NAME_KANA_FLG"] = knjCreateCheckBox($objForm, "NATIONALITY_NAME_KANA_FLG", "1", $extra);

        //英字氏名
        $extra = "";
        $arg["data"]["NATIONALITY_NAME_ENG"] = knjCreateTextBox($objForm, $setRow["NATIONALITY_NAME_ENG"], "NATIONALITY_NAME_ENG", 40, 40, $extra);

        //英字氏名チェックボックス
        $extra = $setRow["NATIONALITY_NAME_ENG_FLG"] ? "checked " : "";
        $arg["data"]["NATIONALITY_NAME_ENG_BK"] = $setRow["NATIONALITY_NAME_ENG_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NATIONALITY_NAME_ENG_FLG"] = knjCreateCheckBox($objForm, "NATIONALITY_NAME_ENG_FLG", "1", $extra);

        //戸籍氏名
        $extra = "onBlur=\" Name_Clip(this);\"";
        $arg["data"]["NATIONALITY_REAL_NAME"] = knjCreateTextBox($objForm, $setRow["NATIONALITY_REAL_NAME"], "NATIONALITY_REAL_NAME", 50, 80, $extra);

        //戸籍氏名チェックボックス
        $extra = $setRow["NATIONALITY_REAL_NAME_FLG"] ? "checked " : "";
        $arg["data"]["NATIONALITY_REAL_NAME_BK"] = $setRow["NATIONALITY_REAL_NAME_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NATIONALITY_REAL_NAME_FLG"] = knjCreateCheckBox($objForm, "NATIONALITY_REAL_NAME_FLG", "1", $extra);

        //戸籍氏名かな
        $extra = "";
        $arg["data"]["NATIONALITY_REAL_NAME_KANA"] = knjCreateTextBox($objForm, $setRow["NATIONALITY_REAL_NAME_KANA"], "NATIONALITY_REAL_NAME_KANA", 60, 160, $extra);

        //戸籍氏名かなチェックボックス
        $extra = $setRow["NATIONALITY_REAL_NAME_KANA_FLG"] ? "checked " : "";
        $arg["data"]["NATIONALITY_REAL_NAME_KANA_BK"] = $setRow["NATIONALITY_REAL_NAME_KANA_FLG"] ? $bgYellow : $bgWhite;
        $arg["data"]["NATIONALITY_REAL_NAME_KANA_FLG"] = knjCreateCheckBox($objForm, "NATIONALITY_REAL_NAME_KANA_FLG", "1", $extra);

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
        $link = REQUESTROOT."/A/KNJA110A/knja110aindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"document.forms[0].ISSUEDATE.value=''; window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja110aSubHist.html", $arg);
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
