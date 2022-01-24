<?php

require_once('for_php7.php');

class knjz080kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz080kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_pre_copy",
                            "value"     => "前年度からコピー",
                            "extrahtml" => "onclick=\"return btn_submit('copy');\"" ));
        $arg["pre_copy"] = $objForm->ge("btn_pre_copy");

        //リスト表示
        $result = $db->query(knjz080kQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["APPLICATIONCD"] = View::alink("knjz080kindex.php", $row["APPLICATIONCD"], "target=\"right_frame\"",
                                                  array("cmd"           => "edit",
                                                        "APPLICATIONCD" => $row["APPLICATIONCD"] ));
            //金額をカンマ区切りにする
            $row["APPLICATIONMONEY"] = (strlen($row["APPLICATIONMONEY"])) ? number_format($row["APPLICATIONMONEY"]): "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"]  = "window.open('knjz080kindex.php?cmd=edit','right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz080kForm1.html", $arg);
    }
}
?>
