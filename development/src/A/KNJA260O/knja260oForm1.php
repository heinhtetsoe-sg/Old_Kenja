<?php

require_once('for_php7.php');

class knja260oForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knja260oForm1", "POST", "knja260oindex.php", "", "knja260oForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knja260oQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //年度コンボボックスを作成する/////////////////////////////////////
        $opt=array();
        $opt_year=array();
        $query = knja260oQuery::getSelectYear();
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
        $query = knja260oQuery::getSelectSeme($model->field["YEAR"]);
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

        if($model->field["GAKKI"] != 9 ){
            $opt_kind[]= array('label' => '中間試験',
                               'value' => '01');
            $opt_kind[]= array('label' => '中間成績',
                               'value' => '02');
            $opt_kind[]= array('label' => '期末試験',
                               'value' => '03');
            $opt_kind[]= array('label' => '期末成績',
                               'value' => '04');
            $opt_kind[]= array('label' => '期末2試験',
                               'value' => '05');
            $opt_kind[]= array('label' => '期末2成績',
                               'value' => '06');
            $opt_kind[]= array('label' => '学期成績',
                               'value' => '0');
        }else{
            $opt_kind[]= array('label' => '学年成績',
                               'value' => '9');
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
                            "extrahtml"  => "onChange=\"return btn_submit('knja260o');\"",
                            "options"    => $opt_kind));

        $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

        //クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
        $opt_class_left = $opt_class_right = array();
        $opt_class=array();
        $query = knja260oQuery::getAuth($model);
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
                            "extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => $opt_class_right));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => $opt_class_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");



        //csvボタンを作成する
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knja260oQuery::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            $printDisp = "エクセル出力";
        } else {
            $extra = "onclick=\"return btn_submit('csv');\"";
            $printDisp = "ＣＳＶ出力";
        }
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
        knjCreateHidden($objForm, "PRGID", "KNJA260O");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja260oForm1.html", $arg); 
    }
}
?>
