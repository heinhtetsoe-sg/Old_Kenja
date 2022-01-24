<?php

require_once('for_php7.php');


//ビュー作成用クラス
class knjd910Form1
{
    function main(&$model)
    {
        //DB
        $db = Query::dbCheckOut();

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjd910index.php", "", "main");

        //権限チェック:更新可
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //処理学期
        $opt_seme = array();
        $result = $db->query(knjd910Query::GetSemester());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_seme[] = array("label" => $row["SEMESTERNAME"], "value" => $row["SEMESTER"]);
        }
        if (!isset($model->seme)) $model->seme = CTRL_SEMESTER;
        $objForm->ae( array("type"       => "select",
                            "name"       => "SEMESTER",
                            "size"       => "1",
                            "value"      => $model->seme,
                            "extrahtml"  => "",
                            "options"    => $opt_seme));
        $arg["SEMESTER"]   = $objForm->ge("SEMESTER");

        //DB
        Query::dbCheckIn($db);

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

        View::toHTML($model, "knjd910Form1.html", $arg);
    }
}
?>
