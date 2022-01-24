<?php

require_once('for_php7.php');

class knjm710lForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjm710lindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = CTRL_YEAR;

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjm710lQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["COLLECT_L_CD"] = View::alink("knjm710lindex.php", $row["COLLECT_L_CD"], "target=\"right_frame\"",
                                         array("cmd"           => "edit",
                                               "COLLECT_L_CD"  => $row["COLLECT_L_CD"]));
                                               
            if ($row["LEVY_FLG"] === '1') {
                $row["LEVY_FLG"] = 'レ';
            }
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"]  = "window.open('knjm710lindex.php?cmd=edit','right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm710lForm1.html", $arg);
    }
}
?>
