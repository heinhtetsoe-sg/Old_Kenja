<?php
class knjz070kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz070kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_pre_copy",
                            "value"     => "前年度からコピー",
                            "extrahtml" => "onclick=\"return btn_submit('copy');\"" ));
        $arg["pre_copy"] = $objForm->ge("btn_pre_copy");

        //リスト表示
        $result = $db->query(knjz070kQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["EXPENSE_S_CD"] = View::alink("knjz070kindex.php", $row["EXPENSE_S_CD"], "target=\"right_frame\"",
                                                  array("cmd"             => "edit",
                                                        "EXPENSE_S_CD"    => $row["EXPENSE_S_CD"] ));

            //金額をカンマ区切りにする
            $row["EXPENSE_S_MONEY"] = (strlen($row["EXPENSE_S_MONEY"])) ? number_format($row["EXPENSE_S_MONEY"]): "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"] = "parent.right_frame.location.href='knjz070kindex.php?cmd=edit"
                           . "&year=".$model->year."';";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz070kForm1.html", $arg);
    }
}
?>
