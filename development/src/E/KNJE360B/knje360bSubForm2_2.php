<?php

require_once('for_php7.php');

class knje360bSubForm2_2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2_2", "POST", "knje360bindex.php", "", "subform2_2");

        //DB接続
        $db = Query::dbCheckOut();

        //進路情報取得
        if ($model->cmd == "replace2" && $model->cmd != "replace2_college") {
            if ($model->cmd == "replace2") {
                $model->replace["selectdata"] = $model->grade.$model->hr_class.$model->attendno."_".$model->schregno;
                $model->replace["ghr"] = $model->grade."-".$model->hr_class;
            }
            if (isset($model->schregno) && isset($model->seq) && !isset($model->warning)) {
                $Row = $db->getRow(knje360bQuery::getSubQuery2($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->replace["field"];
            }
        } else {
            $Row =& $model->replace["field"];
        }

        //編集項目選択チェックボックス
        for ($i = 0; $i < 8; $i++) {
            $extra  = ($model->replace["data_chk"][$i] == "1") ? "checked" : "";
            $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra, "");
        }
        //全選択チェックボックス
        $extra  = ($model->replace["check_all"] == "1") ? "checked" : "";
        $extra .= " onClick=\"return check_all(this);\"";
        $arg["data"]["RCHECK_ALL"] = knjCreateCheckBox($objForm, "RCHECK_ALL", "1", $extra, "");

        //学校コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this, 'FACULTYCD')\"";
        $arg["data"]["SCHOOL_CD"] = knjCreateTextBox($objForm, $Row["SCHOOL_CD"], "SCHOOL_CD", 8, 8, $extra);

        //学部コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this, 'DEPARTMENTCD')\"";
        $arg["data"]["FACULTYCD"] = knjCreateTextBox($objForm, $Row["FACULTYCD"], "FACULTYCD", 3, 3, $extra);

        //学科コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this, 'btn_kakutei')\"";
        $arg["data"]["DEPARTMENTCD"] = knjCreateTextBox($objForm, $Row["DEPARTMENTCD"], "DEPARTMENTCD", 3, 3, $extra);

        //学校情報
        $college = $db->getRow(knje360bQuery::getCollegeInfo($Row["SCHOOL_CD"], $Row["FACULTYCD"], $Row["DEPARTMENTCD"], $Row["CAMPUS_ADDR_CD"], $model), DB_FETCHMODE_ASSOC);

        //学校名
        $arg["data"]["SCHOOL_NAME"]       = $college["SCHOOL_NAME"];

        //学部
        $arg["data"]["FACULTYNAME"]       = $college["FACULTYNAME"];

        //学科
        $arg["data"]["DEPARTMENTNAME"]    = $college["DEPARTMENTNAME"];

        //郵便番号
        $arg["data"]["ZIPCD"]             = $college["ZIPCD"];

        //住所
        $arg["data"]["ADDR1"]             = $college["ADDR1"];
        $arg["data"]["ADDR2"]             = $college["ADDR2"];

        //電話番号
        $arg["data"]["TELNO"]             = $college["TELNO"];

        //設置区分
        $arg["data"]["SCHOOL_GROUP_NAME"] = $college["SCHOOL_GROUP_NAME"];

        //東京都集計用
        knjCreateHidden($objForm, "clicBtn");

        //学校検索後の処理
        if ($model->cmd == "replace2_college") {
            //学校名
            $response  = $college["SCHOOL_NAME"];
            //学部
            $response .= "::".$college["FACULTYNAME"];
            //学科
            $response .= "::".$college["DEPARTMENTNAME"];
            //郵便番号
            $response .= "::".$college["ZIPCD"];
            //住所
            $response .= "::".$college["ADDR1"];
            $response .= "::".$college["ADDR2"];
            //電話番号
            $response .= "::".$college["TELNO"];
            //設置区分
            $response .= "::".$college["SCHOOL_GROUP_NAME"];

            //東京都集計用
            if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
                //学校区分
                $model->replace["field"]["SCHOOL_CATEGORY_CD"] = ($model->clicBtn == '1') ? $college["SCHOOL_CATEGORY_CD"]: $model->replace["field"]["SCHOOL_CATEGORY_CD"];
                if ($college["PROTECTION_FLG"] == '1') {
                    $model->replace["field"]["SCHOOL_CATEGORY_CD"] = $college["SCHOOL_CATEGORY_CD"];
                }
                $query = knje360bQuery::getSchoolCategoryCd();
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "SCHOOL_CATEGORY_CD", $model->replace["field"]["SCHOOL_CATEGORY_CD"], $extra, 1, 1);

                //大分類
                $model->replace["field"]["TOKYO_L_CD"] = ($model->clicBtn == '1') ? $college["TOKYO_L_CD"]: $model->replace["field"]["TOKYO_L_CD"];
                if ($college["PROTECTION_FLG"] == '1') {
                    $model->replace["field"]["TOKYO_L_CD"] = $college["TOKYO_L_CD"];
                }
                $query = knje360bQuery::getTokyoLcd($model->replace["field"]["SCHOOL_CATEGORY_CD"]);
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "TOKYO_L_CD", $model->replace["field"]["TOKYO_L_CD"], $extra, 1, 1);

                //中分類
                $model->replace["field"]["TOKYO_M_CD"] = ($model->clicBtn == '1') ? $college["TOKYO_M_CD"]: $model->replace["field"]["TOKYO_M_CD"];
                if ($college["PROTECTION_FLG"] == '1') {
                    $model->replace["field"]["TOKYO_M_CD"] = $college["TOKYO_M_CD"];
                }
                $query = knje360bQuery::getTokyoMcd($model->replace["field"]["SCHOOL_CATEGORY_CD"], $model->replace["field"]["TOKYO_L_CD"]);
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "TOKYO_M_CD", $model->replace["field"]["TOKYO_M_CD"], $extra, 1, 1);

                $model->clicBtn = 0;
            }
            die();
        }

        //東京都集計用
        if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
            $arg["useTokyotoShinroTyousasyo"] = "1";

            //学校区分
            $query = knje360bQuery::getSchoolCategoryCd();
            $extra = "onchange=\"collegeSelectEvent2();\"";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_CATEGORY_CD", $Row["SCHOOL_CATEGORY_CD"], $extra, 1, "BLANK");

            //大分類
            $query = knje360bQuery::getTokyoLcd($Row["SCHOOL_CATEGORY_CD"]);
            $extra = "onchange=\"collegeSelectEvent2();\"";
            makeCmb($objForm, $arg, $db, $query, "TOKYO_L_CD", $Row["TOKYO_L_CD"], $extra, 1, "BLANK");

            //中分類
            $query = knje360bQuery::getTokyoMcd($Row["SCHOOL_CATEGORY_CD"], $Row["TOKYO_L_CD"]);
            $extra = "onchange=\"collegeSelectEvent2();\"";
            makeCmb($objForm, $arg, $db, $query, "TOKYO_M_CD", $Row["TOKYO_M_CD"], $extra, 1, "BLANK");
        }
        knjCreateHidden($objForm, "useTokyotoShinroTyousasyo", $model->Properties["useTokyotoShinroTyousasyo"]);

        //本都道府県
        $mainpref = $db->getOne(knje360bQuery::getMainPref());

        //所在地
        $Row["PREF_CD"] = ($Row["PREF_CD"]) ? $Row["PREF_CD"] : '-';
        $query = knje360bQuery::getPrefList($mainpref);
        makeCmb($objForm, $arg, $db, $query, "PREF_CD", $Row["PREF_CD"], "", 1, 1);

        //登録日
        $Row["TOROKU_DATE"]         = ($Row["TOROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["TOROKU_DATE"]);
        $arg["data"]["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE", $Row["TOROKU_DATE"]);

        //受験方式
        $query = knje360bQuery::getNameMst('E002');
        $extra = "onChange=\"changeDispSh(this);\"";
        makeCmb($objForm, $arg, $db, $query, "HOWTOEXAM", $Row["HOWTOEXAM"], $extra, 1, 1);

        $setShDivArr = array();
        $query       = knje360bQuery::getNameMstNamecd2('E002');
        $result      = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setShDivArr[] = $row["NAMECD2"];
        }
        knjCreateHidden($objForm, "SH_ARR", implode(",", $setShDivArr));
        if (in_array($Row["HOWTOEXAM"], $setShDivArr)) {
            $arg["data"]["SH_DISP"] = " style=\"display:inline;\" ";
        } else {
            $arg["data"]["SH_DISP"] = " style=\"display:none;\" ";
        }

        //専併区分コンボ
        $query = knje360bQuery::getNameMst('L006');
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $Row["SHDIV"], $extra, 1, "BLANK");

        //受験結果
        $query       = knje360bQuery::getNameMst('E005');
        $setDecision = $model->seq ? $Row["DECISION"] : $model->Properties["KNJE360B_DEF_DECISION"];
        makeCmb($objForm, $arg, $db, $query, "DECISION", $setDecision, "", 1, 1);

        //合格短冊匿名希望チェックボックス
        if ($model->Properties["knje360bShowTokumeiCheck"] == "1") {
            $arg["knje360bShowTokumeiCheck"] = "1";
            $extra  = "id=\"TOKUMEI\"";
            $extra .= $Row["TOKUMEI"] == "1" ? " checked " : "";
            $arg["data"]["TOKUMEI"] = knjCreateCheckBox($objForm, "TOKUMEI", "1", $extra);
        }

        //証明書番号取得
        $certif_no = "";
        if (isset($model->seq)) {
            $query = knje360bQuery::getCertifNo($model, $model->seq);
            $certif_no = $db->getOne($query);
        }
        //調査書発行チェックボックス
        $extra = " id=\"ISSUE\" ";
        $checked = $model->Properties["KNJE360B_DEF_ISSUE"] == "1" ? " checked " : "";
        $arg["data"]["ISSUE"] = knjCreateCheckBox($objForm, "ISSUE", "1", $extra.$checked);
        if ($model->Properties["KNJE360B_DEF_ISSUE"] == 1) {
            $arg["useISSUE"] = 1;
        }

        //進路状況
        $query = knje360bQuery::getNameMst('E006');
        makeCmb($objForm, $arg, $db, $query, "PLANSTAT", $Row["PLANSTAT"], "", 1, 1);

        //入試日
        $Row["STAT_DATE1"]         = ($Row["STAT_DATE1"] == "") ? "" : str_replace("-", "/", $Row["STAT_DATE1"]);
        $arg["data"]["STAT_DATE1"] = View::popUpCalendar($objForm, "STAT_DATE1", $Row["STAT_DATE1"]);

        //合格発表日
        $Row["STAT_DATE3"]         = ($Row["STAT_DATE3"] == "") ? "" : str_replace("-", "/", $Row["STAT_DATE3"]);
        $arg["data"]["STAT_DATE3"] = View::popUpCalendar($objForm, "STAT_DATE3", $Row["STAT_DATE3"]);

        //年度学期情報
        $arg["YEAR_SEM"] = CTRL_YEAR.'年度　　'.CTRL_SEMESTERNAME;

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //生徒項目名切替
        $arg["SCH_LABEL"] = $model->sch_label;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360bSubForm2_2.html", $arg);
    }
}

//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    //置換処理選択時の生徒の情報
    $array = explode(",", $model->replace["selectdata"]);

    //年組コンボ
    $query = knje360bQuery::getGradeHrClass($model);
    $extra = "onchange=\"return btn_submit('replace2B');\"";
    makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->replace["ghr"], $extra, 1);

    //生徒一覧取得
    $opt_right = array();
    $result    = $db->query(knje360bQuery::getStudent($model, "right", array()));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $array)) {
            $opt_right[]  = array("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧リストボックス
    $extra = "multiple style=\"width:170px\" width=\"170px\" ondblclick=\"move1('left')\"";
    $arg["data"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "", $opt_right, $extra, 20);

    //対象者一覧取得
    $opt_left = array();
    $result   = $db->query(knje360bQuery::getStudent($model, "left", $array));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array("label" => $row["LABEL"],
                            "value" => $row["VALUE"]);
    }
    $result->free();

    //対象者一覧リストボックス
    $extra = "multiple style=\"width:170px\" width=\"170px\" ondblclick=\"move1('right')\"";
    $arg["data"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "", $opt_left, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('left');\"";
    $arg["button"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move1('left');\"";
    $arg["button"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move1('right');\"";
    $arg["button"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('right');\"";
    $arg["button"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1   = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"], 'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1   = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"], 'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }
    if (get_count($opt) == 2) {
        $value = ($value != "" && $value_flg) ? $value : $opt[1]["value"];
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //確定ボタン
    $extra = " onclick=\"collegeSelectEvent3();\"";
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

    //学校検索ボタンを作成する
    $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=&target_number=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "学校検索", $extra);

    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit()\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $arg["button"]["btn_back"]   = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform2A');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);

    $semes = $db->getRow(knje360bQuery::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}
