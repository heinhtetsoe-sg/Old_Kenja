<?php

require_once('for_php7.php');

class knjz526nForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz526nindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = CTRL_YEAR;

        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $result = $db->query(knjz526nQuery::selectQuery());
        $prevSelfDiv = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            if ($row["SELF_DIV"] != $prevSelfDiv) {
                $prevSelfDiv = $row["SELF_DIV"];
                $row["SELF_DIV_ROW_SPAN"] = $db->getOne(knjz526nQuery::checkSelfDiv($row["SELF_DIV"]));

                $row["SELF_DIV"] = View::alink("knjz526nindex.php", $row["SELF_DIV"], "target=\"right_frame\"",
                    array("cmd"            => "edit",
                        "SELF_DIV"    => $row["SELF_DIV"],
                        "ITEM_CNT"    => $row["SELF_DIV_ROW_SPAN"]));
            }

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"]  = "window.open('knjz526nindex.php?cmd=edit','right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz526nForm1.html", $arg);
    }
}
?>
