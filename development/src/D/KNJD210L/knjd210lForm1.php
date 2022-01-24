<?php

require_once('for_php7.php');


//ビュー作成用クラス
class knjd210lForm1
{
    function main(&$model)
    {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd210lindex.php", "", "main");
        
        //権限チェック:更新可
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //処理学期
        $opt_seme = $opt_sdate = $opt_edate = array();
        $result = $db->query(knjd210lQuery::GetSemester());
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
        $seme = ($model->seme != "9") ? $model->seme : CTRL_SEMESTER ;
        $result = $db->query(knjd210lQuery::GetGrade($seme));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]      = array("label" => sprintf("%d",$row["GRADE"])."学年", "value" => $row["GRADE"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE",
                            "size"       => "1",
                            "value"      => $model->grade,
                            "options"    => $opt));
        $arg["GRADE"]   = $objForm->ge("GRADE");

        //処理種別(成績)
        $opt_exam = array();
        $result = $db->query(knjd210lQuery::GetName($model->seme));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_exam[] = array("label" => $row["LABEL"], 
                                "value" => $row["VALUE"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "EXAM",
                            "size"       => "1",
                            "value"      => $model->exam,
                            "options"    => $opt_exam));
        $arg["EXAM"]    = $objForm->ge("EXAM");

        //評価のラジオボタン 1:素点 2:評価
        $model->scoreFlg = strlen($model->scoreFlg) ? $model->scoreFlg : '1';
        $optScoreFlg = array(1, 2);
        $extra = array("id=\"SCORE_FLG1\"", "id=\"SCORE_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "SCORE_FLG", $model->scoreFlg, $extra, $optScoreFlg, get_count($optScoreFlg));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        $arg["SCORE_NAME1"] = ($model->seme != "9") ? "素点" : "評価" ;
        $arg["SCORE_NAME2"] = ($model->seme != "9") ? "評価" : "評定" ;

        Query::dbCheckIn($db);

        //講座基準日
        if ($model->chairdate == "") $model->chairdate = str_replace("-", "/", CTRL_DATE);
        $arg["CHAIRDATE"] = View::popUpCalendar($objForm, "CHAIRDATE", $model->chairdate);

        //選択科目
        $check  = ($model->electdiv == "1") ? "checked" : "" ;
        $check .= " id=\"ELECTDIV\"";
        $arg["ELECTDIV"] = knjCreateCheckBox($objForm, "ELECTDIV", "1", $check, "");

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

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd210lForm1.html", $arg);
    }
}
?>
