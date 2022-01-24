<?php

require_once('for_php7.php');

class knja134aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja134aForm1", "POST", "knja134aindex.php", "", "knja134aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期テキストボックスを作成する
        $arg["data"]["GAKKI"] = $model->control["学期名"][$model->control["学期"]];
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
        if ($model->Properties["seitoSidoYorokuUseHrClassType"] == '1') {
            $arg["useHrClassType2"] = "1";
            $opt = array(1, 2);
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"return btn_submit('clickchange');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"return btn_submit('clickchange');\"");
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //学年混合チェックボックス
        $extra = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";
        $extra .= " onclick=\"return btn_submit('clickchange');\" id=\"GAKUNEN_KONGOU\"";
        $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");

        //出力順ラジオボタンを作成
        $radioValue = array(1, 2);
        if (!$model->field["OUTPUT"]) {
            $model->field["OUTPUT"] = 1;
        }
        $extra = array("id=\"OUTPUT1\" onclick =\" return btn_submit('clickchange');\"", "id=\"OUTPUT2\" onclick =\" return btn_submit('clickchange');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $radioValue, get_count($radioValue));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //クラス選択コンボボックスを作成する
        if ($model->field["OUTPUT"] == 1) {
            $arg["student"] = 1;
            $query = knja134aQuery::getHrClass($model);
        } else {
            $arg["hr_class"] = 2;
            $query = knja134aQuery::getGrade($model);
        }
        $row1 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        if ($model->cmd == 'clickchange' ) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            $model->cmd = 'knja134a';
        }

        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1" && $model->field["OUTPUT"] == "2") {
        } elseif ($model->field["HR_CLASS_TYPE"] == "2" && $model->field["OUTPUT"] == "2") {
        } else {
            $arg["setGradeHrclass"] = "1";
            $extra = "onchange=\"return btn_submit('knja134a'),AllClearList();\"";
            $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);
        }

        //対象者リストを作成する
        makeStudentList($objForm, $arg, $db, $model);

        //出力選択ラジオボタン
        $radioValue = array(1, 2);
        if (!$model->field["CHITEKI"]) {
            if ($model->Properties["useSpecial_Support_School"] == "1") {
                $model->field["CHITEKI"] = 2;
            } else {
                $model->field["CHITEKI"] = 1;
            }
        }
        $extra = array("id=\"CHITEKI1\" onclick =\" return btn_submit('chitekichange');\"", "id=\"CHITEKI2\" onclick =\" return btn_submit('chitekichange');\"");
        $radioArray = knjCreateRadio($objForm, "CHITEKI", $model->field["CHITEKI"], $extra, $radioValue, get_count($radioValue));
        $label = array(
            "CHITEKI1" => '知的障害',
            "CHITEKI2" => '知的障害以外'
        );
        foreach ($radioArray as $key => $val) {
            if ($model->Properties["useSpecial_Support_School"] == "1") {
                if ($key == "CHITEKI1") {
                    continue;
                }
            }
            $arg["data"][$key] = "　　".$val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        }

        if ($model->field["CHITEKI"] == '1') {
            $arg["IS_CHITEKI"] = "1";
        } elseif ($model->field["CHITEKI"] == '2') {
            $arg["IS_CHITEKI_IGAI"] = "1";
        }

        //宮城県の場合、画面切り替え
        $z010name1 = "";
        $query = knja134aQuery::getNameMst("Z010", "00");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $z010name1 = $row["NAME1"];
        }
        $result->free();

        //帳票種別チェックボックス
        $name = array("KOSEKI", "SEITO", "SIMEI", "SCHZIP", "ADDR2", "SCHOOLZIP", "KATSUDO", "GAKUSHU", "TANI", "NOT_PRINT_GUARANTOR");
        if ($model->Properties["notShowOnlineSelect"] != "1") {
            $arg["showOnline"] = "1";
            $name[] = "ONLINE";
            if ($model->field["CHITEKI"] == '1') {
                $arg["data"]["NUM_ONLINE"] = "４";
            } elseif ($model->field["CHITEKI"] == '2') {
                $arg["data"]["NUM_ONLINE"] = "５";
            }
        }
        foreach ($name as $key => $val) {
            if (in_array($val, array("KOSEKI", "SCHZIP", "ADDR2", "SCHOOLZIP", "NOT_PRINT_GUARANTOR", "ONLINE"))) {
                $extra = $model->field[strtolower($val)] == "1" ? "checked" : "";
            } elseif ($val == "TANI") {
                $extra = ($model->field[strtolower($val)] == "1" || $model->cmd == "" || ($model->cmd == "chitekichange" && $model->field["seito"] && $model->field["katsudo"] && $model->field["gakushu"])) ? "checked" : "";
            } else {
                $extra = ($model->field[strtolower($val)] == "1" || $model->cmd == "") ? "checked" : "";
            }
            $extra .= " onclick=\"kubun();\" id=\"$val\"";
            if (in_array($val, array("SIMEI")) && $z010name1 == "miyagiken") {
                knjCreateHidden($objForm, "simei", "1");
            } else {
                $arg["data"][$val] = knjCreateCheckBox($objForm, strtolower($val), "1", $extra, "");
            }
        }

        if ($z010name1 == 'miyagiken') {
            $arg["not_miyagiken"] = "";
        } else {
            $arg["not_miyagiken"] = "1";
        }

/*
        $arg["SHOW_RISHU"] = "1";
        //未履修科目出力・履修のみ科目出力のデフォルト値設定
        if ($model->field["MIRISYU"] == "" || $model->field["RISYU"] == "" || $model->field["RISYUTOUROKU"] == "") {
            $query = knja134aQuery::getRisyuMirsyu($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                // 値がYなら「しない」それ以外は「する」
                $model->field["MIRISYU"] = "Y" == $row["NAMESPARE1"] ? "2" : "1";
                $model->field["RISYU"] = "Y" == $row["NAMESPARE2"] ? "2" : "1";
                $model->field["RISYUTOUROKU"] = "Y" == $row["NAMESPARE3"] ? "2" : "1";
            }
            $result->free();
            $model->field["MIRISYU"] = isset($model->field["MIRISYU"]) ? $model->field["MIRISYU"] : "2"; // デフォルトは「しない」
            $model->field["RISYU"] = isset($model->field["RISYU"]) ? $model->field["RISYU"] : "1"; // デフォルトは「する」
            $model->field["RISYUTOUROKU"] = isset($model->field["RISYUTOUROKU"]) ? $model->field["RISYUTOUROKU"] : "2"; // デフォルトは「しない」
        }

        //未履修科目出力ラジオボタンを作成する 1:する 2:しない
        $opt_mirisyu = array(1, 2);
        $extra  = array("id=\"MIRISYU1\" onclick=\"checkRisyu();\"", "id=\"MIRISYU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "MIRISYU", $model->field["MIRISYU"], $extra, $opt_mirisyu, get_count($opt_mirisyu));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //履修のみ科目出力ラジオボタン 1:する 2:しない
        $optRisyu = array(1, 2);
        $extra  = array("id=\"RISYU1\" onclick=\"checkRisyu();\"", "id=\"RISYU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "RISYU", $model->field["RISYU"], $extra, $optRisyu, get_count($optRisyu));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //履修登録のみ科目出力ラジオボタン 1:する 2:しない
        $optRisyuT = array(1, 2);
        $extra  = array("id=\"RISYUTOUROKU1\" onclick=\"checkRisyu();\"", "id=\"RISYUTOUROKU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "RISYUTOUROKU", $model->field["RISYUTOUROKU"], $extra, $optRisyuT, get_count($optRisyuT));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
 */
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        if (!isset($model->warning) && $model->cmd == 'print') {
            $model->cmd = 'knja134a';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja134aForm1.html", $arg);
    }
}
//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    $select = 0;
    if ($model->field["OUTPUT"] == 1) {
        $select = 1;
    }
    $Chiteki = empty($model->field['CHITEKI'])?'1':$model->field['CHITEKI'];
    if ($model->field["OUTPUT"] == 1) {
        if ($model->field["HR_CLASS_TYPE"] == '1') {
            $query  = "SELECT T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
            $query .= "       T2.SCHREGNO || '　' || T2.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
            $query .= "FROM SCHREG_BASE_MST T1 ";
            $query .= "INNER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T2.YEAR AND T3.GRADE = T2.GRADE AND T3.SCHOOL_KIND = 'A' ";
        } else {
            $query  = "SELECT T2.GHR_CD || T2.GHR_ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
            $query .= "       T2.SCHREGNO || '　' || T2.GHR_ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
            $query .= "FROM SCHREG_BASE_MST T1 ";
            $query .= "INNER JOIN SCHREG_REGD_GHR_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "WHERE  T2.YEAR ='" .$model->control["年度"] ."' AND ";
        $query .= "       T2.SEMESTER ='" .$model->control["学期"] ."' AND ";
        $query .= "       EXISTS( ";
        $query .= "           SELECT * FROM NAME_MST WHERE NAMECD1='A025' AND NAMESPARE3='" . $Chiteki . "' AND NAMECD2=T1.HANDICAP ";
        $query .= "       ) AND ";
        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= "       T3.SCHOOL_KIND || '-' || T2.HR_CLASS ='" .$model->field["GRADE_HR_CLASS"] ."' ";
            $query .= "ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO";
        } elseif ($model->field["HR_CLASS_TYPE"] == "2") {
            $query .= "       T2.GHR_CD = '" .$model->field["GRADE_HR_CLASS"] ."' ";
            $query .= "ORDER BY T2.GHR_CD, T2.GHR_ATTENDNO";
        } else {
            $query .= "       T2.GRADE || T2.HR_CLASS ='" .$model->field["GRADE_HR_CLASS"] ."' ";
            $query .= "ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO";
        }

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["VALUE"], $model->select_data["selectdata"])) {
                $opt2[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            } else {
                $opt1[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        }
    } else {
        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query  = " SELECT ";
            $query .= "     T2.SCHOOL_KIND || '-' || T2.HR_CLASS AS VALUE, ";
            $query .= "     T2.SCHOOL_KIND || '-' || T2.HR_CLASS || ':' || T2.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM V_STAFF_HR_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T2.YEAR ='" .$model->control["年度"] ."' AND ";
            $query .= "     T2.SEMESTER ='" .$model->control["学期"] ."' AND ";
            $query .= "     T2.SCHOOL_KIND = 'A' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T2.STAFFCD = '".STAFFCD."' ";
            }
            $query .= "     AND T2.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T2.YEAR     = M1.YEAR AND ";
            $query .= "                                     T2.SEMESTER = M1.SEMESTER AND ";
            $query .= "                                     T2.HR_CLASS = M1.HR_CLASS AND ";
            $query .= "                                     T2.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
            $query .= "                                     T2.STAFFCD  = M1.STAFFCD ";
            $query .= "                                 ) AND ";
            $query .= "     EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             * ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_DAT T3 ";
            $query .= "         INNER JOIN ";
            $query .= "             SCHREG_BASE_MST T4 ";
            $query .= "             ON T3.SCHREGNO = T4.SCHREGNO ";
            $query .= "         WHERE ";
            $query .= "             T3.HR_CLASS = T2.HR_CLASS AND ";
            $query .= "             EXISTS( ";
            $query .= "                 SELECT * FROM NAME_MST WHERE NAMECD1='A025' AND NAMESPARE3='" . $Chiteki . "' AND NAMECD2=T4.HANDICAP ";
            $query .= "             ) ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     T2.HR_CLASS, ";
            $query .= "     T2.HR_CLASS_NAME1, ";
            $query .= "     T2.SCHOOL_KIND ";
            $query .= " ORDER BY ";
            $query .= "     T2.SCHOOL_KIND DESC, ";
            $query .= "     T2.HR_CLASS, ";
            $query .= "     LABEL ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (in_array($row["VALUE"], $model->select_data["selectdata"])) {
                    $opt2[]= array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                } else {
                    $opt1[]= array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                }
            }
        } else {
            $query = knja134aQuery::getHrClassAuth($model, CTRL_YEAR, CTRL_SEMESTER);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->field["HR_CLASS_TYPE"] == '2') {
                } else {
                    if (substr($row["VALUE"], 0, 2) != $model->field["GRADE_HR_CLASS"]) {
                        continue;
                    }
                }
                if (in_array($row["VALUE"], $model->select_data["selectdata"])) {
                    $opt2[]= array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                } else {
                    $opt1[]= array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                }
            }
        }
    }
    $result->free();

    $extra = "multiple style=\"width:230px; height: 320px;\"  ondblclick=\"move1('left', $select)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1) ? $opt1: array(), $extra, 20);

    //生徒一覧リストを作成する
    $extra = "multiple style=\"width:230px; height: 320px;\"  ondblclick=\"move1('right', $select)\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", isset($opt2) ? $opt2:array(), $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $select);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $select);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $select);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left',  $select);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA134A");
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
    knjCreateHidden($objForm, "seitoSidoYorokuFormType", $model->Properties["seitoSidoYorokuFormType"]);
    knjCreateHidden($objForm, "seitoSidoYorokuFieldSize", $model->Properties["seitoSidoYorokuFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuSougouFieldSize", $model->Properties["seitoSidoYorokuSougouFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuSpecialactremarkFieldSize", $model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuKinsokuForm", $model->Properties["seitoSidoYorokuKinsokuForm"]);
    knjCreateHidden($objForm, "INEI", $model->inei);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize_disability", $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize_disability"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize_disability", $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize_disability"]);
    knjCreateHidden($objForm, "seitoSidoYorokuZaisekiMae", $model->Properties["seitoSidoYorokuZaisekiMae"]);
    knjCreateHidden($objForm, "seitoSidoYorokuKoumokuMei", $model->Properties["seitoSidoYorokuKoumokuMei"]);
    knjCreateHidden($objForm, "seitoSidoYorokuHyotei0ToBlank", $model->Properties["seitoSidoYorokuHyotei0ToBlank"]);
    knjCreateHidden($objForm, "seitoSidoYorokuYoshiki2PrintOrder", $model->Properties["seitoSidoYorokuYoshiki2PrintOrder"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherStudyrec", $model->Properties["seitoSidoYorokuNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherAttendrec", $model->Properties["seitoSidoYorokuNotPrintAnotherAttendrec"]);
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
    knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
    knjCreateHidden($objForm, "notPrintFinschooltypeName", $model->Properties["notPrintFinschooltypeName"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_Attendrec_RemarkSize", $model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_Attendrec_RemarkSize_disability", $model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize_disability"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_SpecialactremarkSize", $model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalremarkSize", $model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalremarkSize_disability", $model->Properties["seitoSidoYoroku_dat_TotalremarkSize_disability"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_H", $model->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_H"]);
    knjCreateHidden($objForm, "HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H", $model->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H"]);
    knjCreateHidden($objForm, "HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H_disability", $model->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H_disability"]);
}
