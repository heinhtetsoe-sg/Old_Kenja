<?php

require_once('for_php7.php');

class knjd625fForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd625fForm1", "POST", "knjd625findex.php", "", "knjd625fForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //抽出開始年度
        $model->field["STRT_YEAR"] = $model->field["STRT_YEAR"] != "" ? $model->field["STRT_YEAR"] : intval(CTRL_YEAR)-1;
        $query = knjd625fQuery::getYearSelect();
        $extra = "onchange=\"btn_submit('changeYear');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["STRT_YEAR"], "STRT_YEAR", $extra, 1, "");

        //抽出修了年度
        $model->field["END_YEAR"] = $model->field["END_YEAR"] != "" ? $model->field["END_YEAR"] : intval(CTRL_YEAR)-1;
        $query = knjd625fQuery::getYearSelect();
        $extra = "onchange=\"btn_submit('changeYear');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["END_YEAR"], "END_YEAR", $extra, 1, "");

        //クラス・個人ラジオボタン 1:クラス選択 2:個人選択
        $opt_div = array(1, 2);
        $model->field["TESTTYPE"] = ($model->field["TESTTYPE"] == "") ? "1" : $model->field["TESTTYPE"];
        $extra = array("id=\"TESTTYPE1\" onClick=\"return btn_submit('knjd625f_2')\"", "id=\"TESTTYPE2\" onClick=\"return btn_submit('knjd625f_2')\"");
        $radioArray = knjCreateRadio($objForm, "TESTTYPE", $model->field["TESTTYPE"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        if ($model->field["TESTTYPE"] == "2") {
            $arg["testTypeStr"] = "模試";
        } else {
            $arg["testTypeStr"] = "考査";
        }

        //合格者のみ出力のチェックボックスを作成
        $model->field["PASS_ONLY"] = $model->field["PASS_ONLY"] != "" ? $model->field["PASS_ONLY"] : "";
        $extra = "id=\"PASS_ONLY\"";
        $extra .= $model->field["PASS_ONLY"] ? " checked " : "";
        $arg["data"]["PASS_ONLY"] =  knjCreateCheckBox($objForm, "PASS_ONLY", "1", $extra);

        //出力対象一覧リストを作成する
        //大学一覧
        makeListToListCollege($objForm, $arg, $db, $model);
        //考査/模試一覧
        if ($model->field["TESTTYPE"] == "2") {
            $arg["dispSelMockCd"] = "1";
            makeListToListTestInfo($objForm, $arg, $db, $model);
            //科目一覧
            makeListToListSubclass($objForm, $arg, $db, $model);
        } else {
            $arg["dispSelMockCd"] = "";
        }

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd625fForm1.html", $arg); 
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
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//出力対象一覧リストを作成する(クラス一覧)
function makeListToListCollege(&$objForm, &$arg, $db, $model)
{
    $selectdata = ($model->selectCollege != "" && $model->cmd == "knjd625f_2") ? explode(",", $model->selectCollege) : array();

    //対象者リスト
    $opt_right = array();
    $opt_left = array();
    $query = knjd625fQuery::getCollege($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $selectdata)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        } else {
            $opt_left[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    makeListToList($objForm, $arg, $opt_right, $opt_left, "COLLEGE_SELECTED", "COLLEGE_NAME", "college", "150");
}

//出力対象一覧リストを作成する(クラス一覧)
function makeListToListTestInfo(&$objForm, &$arg, $db, $model)
{
    $selectdata = ($model->selectTestInfo != "" && $model->cmd == "knjd625f_2") ? explode(",", $model->selectTestInfo) : array();

    //対象者リスト
    $opt_right = array();
    $opt_left = array();
    $query = knjd625fQuery::getTestInfo($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $selectdata)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        } else {
            $opt_left[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    makeListToList($objForm, $arg, $opt_right, $opt_left, "TESTINFO_SELECTED", "TESTINFO_NAME", "test", "150");
}

//出力対象一覧リストを作成する(クラス一覧)
function makeListToListSubclass(&$objForm, &$arg, $db, $model)
{
    $selectSubclass = ($model->selectSubclass != "" && $model->cmd == "knjd625f_2") ? explode(",", $model->selectSubclass) : array();

    //対象者リスト
    $opt_right = array();
    $opt_left = array();
    $query = knjd625fQuery::getSubclassCd($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //$label = "　".$row["SUBCLASSCD"].":".$row["SUBCLASSNAME"];
        //$value = $row["SUBCLASSCD"].":0";
        $label = $row["SUBCLASSCD"].":".$row["SUBCLASSNAME"];
        $value = $row["SUBCLASSCD"];
        if (!in_array($value, $selectSubclass)) {
            $opt_right[] = array('label' => $label,
                'value' => $value);
        } else {
            $opt_left[] = array('label' => $label,
                'value' => $value);
        }
        // 現状、元科目のみ表示で良い、となったので、先科目抽出しない。
        // もし利用する場合、次の課題に回答が無いといけない。
        // 課題：過去に先科目に切り替えた年度があった場合、過年度には元科目がある年と元科目が無い年が混ざる事になるが、指示画面に科目コードをどう表示するか。ユーザーはそれを理解できるか。
        // $query2 = knjd625fQuery::getCombinedSubclassCd($row["SUBCLASSCD"]);
        // $result2 = $db->query($query2);
        // while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
        //     $label = "●".$row2["COMBINED_SUBCLASSCD"].":".$row2["SUBCLASSNAME"];
        //     $value = $row2["COMBINED_SUBCLASSCD"].":1";
        //     if (!in_array($value, $selectSubclass)) {
        //         $opt_right[] = array('label' => $label,
        //             'value' => $value);
        //     } else {
        //         $opt_left[] = array('label' => $label,
        //             'value' => $value);
        //     }
        //}
        //$result2->free();
    }
    $result->free();

    makeListToList($objForm, $arg, $opt_right, $opt_left, "SUBCLASS_SELECTED", "SUBCLASS_NAME", "subclassButton", ($model->field["TESTTYPE"] == "2" ? "150" : "280"), "0");
}

//リストTOリスト作成 共通部分
function makeListToList(&$objForm, &$arg, $optRight, $optLeft, $categorySelected, $categoryName, $button_name, $height, $jsSort="1") {
    //一覧リスト（右）
    $extra = "multiple style=\"width:500px; height:".$height."px\" ondblclick=\"move1('left', '".$categoryName."', '".$categorySelected."', '".$jsSort."')\"";
    $arg["data"][$categoryName] = knjCreateCombo($objForm, $categoryName, "", $optRight, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:500px; height:".$height."px\" ondblclick=\"move1('right', '".$categoryName."', '".$categorySelected."', '1')\"";
    $arg["data"][$categorySelected] = knjCreateCombo($objForm, $categorySelected, "", $optLeft, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '".$categoryName."', '".$categorySelected."', '1');\"";
    $arg[$button_name]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '".$categoryName."', '".$categorySelected."', '".$jsSort."');\"";
    $arg[$button_name]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '".$categoryName."', '".$categorySelected."', '1');\"";
    $arg[$button_name]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '".$categoryName."', '".$categorySelected."', '".$jsSort."');\"";
    $arg[$button_name]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //終了ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "PRGID", "KNJD625F");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "TESTNAME");
    knjCreateHidden($objForm, "useCurriculumcd",   $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "selectCollege");
    knjCreateHidden($objForm, "selectCollegeText");
    knjCreateHidden($objForm, "selectTestInfo");
    knjCreateHidden($objForm, "selectTestInfoText");
    knjCreateHidden($objForm, "selectSubclass");
    knjCreateHidden($objForm, "selectSubclassText");
}

?>
