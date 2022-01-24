<?php

require_once('for_php7.php');


//ビュー作成用クラス
class knjd210rForm1
{
    function main(&$model)
    {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd210rindex.php", "", "main");
        
        //権限チェック:更新可能および更新可能(制限付)以外は「権限がありません。」メッセージ表示し画面閉じる
        if (AUTHORITY < DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //処理学期
        $opt_seme = $opt_sdate = $opt_edate = array();
        $result = $db->query(knjd210rQuery::GetSemester());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_seme[] = array("label" => $row["SEMESTERNAME"], "value" => $row["SEMESTER"]);
            $opt_sdate[$row["SEMESTER"]] = str_replace("-", "/", $row["SDATE"]);
            $opt_edate[$row["SEMESTER"]] = str_replace("-", "/", $row["EDATE"]);
        }
        if (!isset($model->seme)) $model->seme = CTRL_SEMESTER;

        $objForm->ae( array("type"       => "select",
                            "name"       => "SEMESTER",
                            "size"       => "1",
                            "value"      => $model->seme,
                            "extrahtml" => "onChange=\"btn_submit('');\"",
                            "options"    => $opt_seme));
        $arg["SEMESTER"]   = $objForm->ge("SEMESTER");

        //処理学年
        $opt = array();
        $value_flg = false;
        $seme = ($model->seme != "9") ? $model->seme : CTRL_SEMESTER ;
        $result = $db->query(knjd210rQuery::GetGrade($seme));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[]      = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
            if ($model->grade == $row["VALUE"]) $value_flg = true;
        }
        $model->grade = ($model->grade && $value_flg) ? $model->grade : $opt[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE",
                            "size"       => "1",
                            "value"      => $model->grade,
                            "extrahtml" => "onChange=\"btn_submit('');\"",
                            "options"    => $opt));
        $arg["GRADE"]   = $objForm->ge("GRADE");

        //処理種別(成績)
        $opt_exam = array();
        $value_flg = false;
        $result = $db->query(knjd210rQuery::GetName($model->seme));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_exam[] = array("label" => $row["LABEL"], 
                                "value" => $row["VALUE"]);
            if ($model->exam == $row["VALUE"]) $value_flg = true;
        }
        $model->exam = ($model->exam && $value_flg) ? $model->exam : $opt_exam[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "EXAM",
                            "size"       => "1",
                            "value"      => $model->exam,
                            "extrahtml" => "onChange=\"btn_submit('');\"",
                            "options"    => $opt_exam));
        $arg["EXAM"]    = $objForm->ge("EXAM");

        //講座基準日
        if ($model->chairdate == "") $model->chairdate = str_replace("-", "/", CTRL_DATE);
        $arg["CHAIRDATE"] = View::popUpCalendar($objForm, "CHAIRDATE", $model->chairdate);

        //科目コンボ
        $extra = "onChange=\"disElectdiv();\" ";
        $query = knjd210rQuery::getSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1, "");

        //選択科目
        $check  = ($model->electdiv == "1") ? "checked" : "" ;
        $check .= " id=\"ELECTDIV\"";
        $check .= ($model->subclasscd == "999999" || $model->subclasscd == "") ? "" : " disabled" ;
        $arg["ELECTDIV"] = knjCreateCheckBox($objForm, "ELECTDIV", "1", $check, "");

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //実行ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('execute');\"" ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        //学期開始日付
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SDATE",
                            "value"     => $opt_sdate[$model->seme] ) );
        //学期終了日付
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "EDATE",
                            "value"     => $opt_edate[$model->seme] ) );

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd210rForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();

    if ($name == "SUBCLASSCD" && AUTHORITY == DEF_UPDATABLE) {
        $opt[] = array("label" => "全て", "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SUBCLASSCD" && AUTHORITY == DEF_UPDATABLE) {
        $opt[] = array("label" => "総合計（３・５・９）", "value" => "999999");
        if ($value == "999999") $value_flg = true;
    }

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model) {
    //教育課程対応
    $dummycd = "";
    if ($model->Properties["useCurriculumcd"] == '1') {
        $dummycd = "00-00-00-";
    }
    //履歴一覧
    $query = knjd210rQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        $row["CHAIRDATE"] = str_replace("-", "/", $row["CHAIRDATE"]);
        $row["ELECTDIV_FLG"] = $row["ELECTDIV_FLG"] == "1" ? "除く" : "";
        if ($row["SUBCLASSCD"] == $dummycd."ALL") {
            $row["SUBCLASSNAME"] = "全て";
        } else if ($row["SUBCLASSCD"] == $dummycd."999999") {
            $row["SUBCLASSNAME"] = "総合計（３・５・９）";
        } else {
            $row["SUBCLASSNAME"] = $row["SUBCLASSCD"].":".$row["SUBCLASSNAME"];
        }
        $arg['data2'][] = $row;
    }
    $result->free();
}
?>
