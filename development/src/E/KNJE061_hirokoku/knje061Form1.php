<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje061Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){ $arg["jscript"] = "OnAuthError();";}

        $db = Query::dbCheckOut();
        $objForm = new form;
        
        //年度学期表示
        $arg["SEMESTERNAME"] = CTRL_YEAR ."年度　" .CTRL_SEMESTERNAME;

        //学年
        $result = $db->query(knje061Query::selectQueryAnnual($model));
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => (int)$row["ANNUAL"] ."学年",
                           "value" => $row["ANNUAL"]);
            if (!isset($model->annual)) $model->annual = $row["ANNUAL"];
        }
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual,
                            "extrahtml"  => "style=\"width:100px\" onChange=\"return btn_submit('annual');\"",
                            "options"    => $opt));
        
        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        $result = $db->query(knje061query::selectQueryHRClass($model->annual));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => (int)$row["HR_CLASS"]."組",
                           "value" => $row["HR_CLASS"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->hr_class,
                            "extrahtml"  => "style=\"width:100px\" onChange=\"return btn_submit('hr_class');\"",
                            "options"    => $opt));

        $arg["HR_CLASS"] = $objForm->ge("HR_CLASS");
        
        //コース
        $result = $db->query(knje061query::selectCourse($model));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["COURSECODE"]."：".$row["COURSECODENAME"],
                           "value" => $row["COURSECODE"]);
        }
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "COURSECODE",
                            "size"       => "1",
                            "value"      => $model->coursecode,
                            "extrahtml"  => "style=\"width:150px\" onChange=\"return btn_submit('coursecode');\"",
                            "options"    => $opt));
        
        $arg["COURSECODE"] = $objForm->ge("COURSECODE");
        
        //生徒
        $result = $db->query(knje061query::selectSchregno($model));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["SCHREGNO"]."：".$row["NAME_SHOW"],
                           "value" => $row["SCHREGNO"]);
        }
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "SCHREGNO",
                            "size"       => "1",
                            "value"      => $model->schregno,
                            "extrahtml"  => "style=\"width:300px\"",
                            "options"    => $opt));
        
        $arg["SCHREGNO"] = $objForm->ge("SCHREGNO");
        
        Query::dbCheckIn($db);

        //ファイルからの取り込み
        $objForm->ae(array("type"       => "file",
                            "name"      => "FILE",
                            "size"      => 1024000,
                            "extrahtml" => "" ));

        $arg["FILE"] = $objForm->ge("FILE");

        //CSV取込みボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('exec');\"" ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        // CSVテンプレート書出しボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_output",
                            "value"     => "テンプレート書出し",
                            "extrahtml" => "onclick=\"return btn_submit('output');\"" ));

        $arg["btn_output"] = $objForm->ge("btn_output");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
                            
        //ラジオボタン
        $arg["RADIO"] = $model->field;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje061index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje061Form1.html", $arg);
    }
}
?>
