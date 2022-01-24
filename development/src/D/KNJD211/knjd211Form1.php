<?php

// kanji=漢字
// $Id: knjd211Form1.php 56580 2017-10-22 12:35:29Z maeshiro $

//ビュー作成用クラス
class knjd211Form1
{
    function main(&$model)
    {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd211index.php", "", "main");
        
        //権限チェック:更新可
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //処理学年
        $opt = array();
        $result = $db->query(knjd211Query::GetGrade());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]      = array("label" => sprintf("%d",$row["GRADE"])."学年", "value" => $row["GRADE"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE",
                            "size"       => "1",
                            "value"      => $model->grade,
                            "extrahtml" => "onChange=\"btn_submit('chg_grade');\"",
                            "options"    => $opt));
        $arg["GRADE"]   = $objForm->ge("GRADE");

        //処理種別(成績)
        $opt_exam = array();
        $result = $db->query(knjd211Query::GetName($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_exam[] = array("label" => $row["NAME1"], "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "EXAM",
                            "size"       => "1",
                            "value"      => $model->exam,
                            "options"    => $opt_exam));
        $arg["EXAM"]    = $objForm->ge("EXAM");

        Query::dbCheckIn($db);

        //異動対象日付---2005.10.12Add
        if ($model->date == "") $model->date = str_replace("-","/",CTRL_DATE);
        $arg["DATE"] = View::popUpCalendar($objForm ,"DATE" ,$model->date);


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

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd211Form1.html", $arg);
    }
}
?>
