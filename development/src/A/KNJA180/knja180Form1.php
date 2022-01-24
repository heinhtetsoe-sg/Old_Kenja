<?php

require_once('for_php7.php');


class knja180Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成////////////////////////////////////////////////////////////////////////
        $arg["start"]   = $objForm->get_start("knja180Form1", "POST", "knja180index.php", "", "knja180Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度をhiddenで送る///////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR,
                            ) );

        //学期テキストボックスを作成する/////////////////////////////////////////////////////////////////////////////////////////

        $query = knja180Query::getSelectSeme(CTRL_YEAR);
        $result = $db->query($query);
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[]= array('label' => $row["SEMESTERNAME"],
                            'value' => $row["SEMESTER"]);
        }

        if($model->field["GAKKI"]=="") $model->field["GAKKI"] = CTRL_SEMESTER;

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
                            "extrahtml"  => "onChange=\"return btn_submit('knja180');\"",
                            "options"    => $opt));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        //クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////

        $query = knja180Query::getSql_GradeHrClass($model);
        $result = $db->query($query);
        $row1 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => $row1));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => array()));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //コンボボックスを作成する////////////////////////////////////////////////////////////////////////////

        //課程学科
        $query = knja180Query::GetSql_Course_Major(CTRL_YEAR);
        $result = $db->query($query);
        $row2 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row2[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"],
                           'cname' => $row["COURSE"],);
        }
        //コース
        $query = knja180Query::GetSql_CourseCode(CTRL_YEAR);
        $result = $db->query($query);
        $row3 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row3[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        //課程学科
        $objForm->ae( array("type"       => "select",
                            "name"       => "COURSE_MAJOR_NAME",
                            "size"       => "1",
                            "value"      => $model->field["COURSE_MAJOR_NAME"],
                            "options"    => $row2));
        $arg["data"]["COURSE_MAJOR_NAME"] = $objForm->ge("COURSE_MAJOR_NAME");
        //コース
        $objForm->ae( array("type"       => "select",
                            "name"       => "COURSECODE",
                            "size"       => "1",
                            "value"      => $model->field["COURSECODE"],
                            "options"    => $row3));
        $arg["data"]["COURSECODE"] = $objForm->ge("COURSECODE");

        //電話番号チェックボックスを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        if ($model->field["TEL"] == "1" || $model->cmd == ""){
            $check_tel = "checked";
        }else {
            $check_tel = "";
        }

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "TEL",
                            "value"     => "1",
                            "extrahtml" => $check_tel." id=\"TEL\"" ) );

        $arg["data"]["TEL"] = $objForm->ge("TEL");

        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "', '', '', '');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knja180Query::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "', 'csv');\"";
            $setBtnName = "エクセル出力";
        } else {
            $extra = "onclick=\"return btn_submit('csv');\"";
            $setBtnName = "ＣＳＶ出力";
        }

        //親画面なし
        $securityCnt = $db->getOne(knja180Query::getSecurityHigh());
        //セキュリティーチェック
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $setBtnName, $extra);
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SDAY", str_replace("/","-",$model->control["学期開始日付"][$model->field["GAKKI"]]));
        knjCreateHidden($objForm, "EDAY", str_replace("/","-",$model->control["学期終了日付"][$model->field["GAKKI"]]));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA180");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja180Form1.html", $arg); 
    }
}
?>
