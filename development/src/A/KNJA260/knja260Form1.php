<?php

require_once('for_php7.php');

class knja260Form1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knja260Form1", "POST", "knja260index.php", "", "knja260Form1");

        //権限チェック
        if ($model->auth < DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knja260Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        $opt=array();
        $opt_year=array();
        $query = knja260Query::getSelectYear();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_year[]= $row["YEAR"];
        }
        if($model->field["YEAR"]=="") $model->field["YEAR"] = $model->control["年度"];
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "YEAR",
                            "size"       => "1",
                            "value"      => $model->field["YEAR"],
                            "extrahtml"  => "onChange=\"btn_submit('init');\"",
                            "options"    => $opt_year));

        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        //学期コンボボックスを作成する/////////////////////////////////////
        $opt_sem = array();
        $query = knja260Query::getSelectSeme($model->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_sem[]= array('label' => $row["SEMESTERNAME"],
                              'value' => $row["SEMESTER"]);
        }
        $result->free();

        if($model->field["GAKKI"]=="") $model->field["GAKKI"] = $model->control["学期"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
                            "extrahtml"  => "onChange=\"btn_submit('init');\"",
                            "options"    => $opt_sem));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        //テスト種別リスト
        if(($model->tableName == 'TESTITEM_MST_COUNTFLG' && $model->field["GAKKI"] != "9") || $model->tableName == 'TESTITEM_MST_COUNTFLG_NEW'){
            $query = knja260Query::getTestItem($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_kind[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);

                if($row["VALUE"] == "0101"){
                    $opt_kind[] = array('label' => "中間成績",
                                        'value' => "010199");
                } elseif($row["VALUE"] == "0201"){
                    $opt_kind[] = array('label' => "期末成績",
                                        'value' => "020199");
                } elseif($row["VALUE"] == "0202"){
                    $opt_kind[] = array('label' => "期末2成績",
                                        'value' => "020299");
                }

            }
        }

        if($model->tableName == 'TESTITEM_MST_COUNTFLG'){
            $opt_kind[] = array('label' => '評価成績', 'value' => '9900');
        }

        //出欠集計日付
        $model->field["ATTENDDATE"] = ($model->field["ATTENDDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["ATTENDDATE"];
        $arg["data"]["ATTENDDATE"] = View::popUpCalendar2($objForm, "ATTENDDATE", $model->field["ATTENDDATE"], "", "", "");

        //初期化処理
        $test_flg = false;
        for ($i=0; $i<get_count($opt_kind); $i++) 
            if ($model->field["TESTKINDCD"] == $opt_kind[$i]["value"]) $test_flg = true;
        if (!$test_flg) $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->field["TESTKINDCD"],
                            "options"    => $opt_kind));

        $disBtnPrint = "";
        if ($opt_kind != null) {
            $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");
        } else {
            $arg["data"]["TESTKINDCD"] = "該当データなし";
            $disBtnPrint = " disabled";
        }

        //クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
        $opt_class_left = $opt_class_right = array();
        $opt_class=array();
        $query = knja260Query::getAuth($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (!in_array($row["VALUE"], $model->select_data["selectdata"])){
                $opt_class_right[]= array('label' => $row["LABEL"],
                                          'value' => $row["VALUE"]);
            } else {
                $opt_class_left[]= array('label' => $row["LABEL"],
                                         'value' => $row["VALUE"]);
            }
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => $opt_class_right));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => $opt_class_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_rights",
                            "value"     => ">>",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_lefts",
                            "value"     => "<<",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_right1",
                            "value"     => "＞",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_left1",
                            "value"     => "＜",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //csvボタンを作成する
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knja260Query::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            $printDisp = "エクセル出力";
        } else {
            $extra = "onclick=\"return btn_submit('csv');\"";
            $printDisp = "ＣＳＶ出力";
        }
        $extra .= $disBtnPrint;
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", $printDisp, $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA260");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "useVirus", $model->virus);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja260Form1.html", $arg); 
    }
}
?>
