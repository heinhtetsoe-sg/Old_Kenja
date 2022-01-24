<?php

require_once('for_php7.php');

class knjmp980Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjmp980index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;
        
        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjmp980Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["LEVY_L_CD"] = View::alink("knjmp980index.php", $row["LEVY_L_CD"], "target=\"right_frame\"",
                                         array("cmd"        => "edit",
                                               "YEAR"       => $row["YEAR"],
                                               "LEVY_L_CD"  => $row["LEVY_L_CD"]));
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"]  = "window.open('knjmp980index.php?cmd=edit','right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjmp980Form1.html", $arg);
    }
}
?>
