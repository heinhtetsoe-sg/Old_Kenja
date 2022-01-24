<?php

require_once('for_php7.php');

class knjp747Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp747index.php", "", "edit");

        //db接続
        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = CTRL_YEAR;

        //リスト表示
        $bifKey = "";
        $query = knjp747Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $query = knjp747Query::getList($model, $row["SCHOOL_KIND"]);
            $schKindCnt = $db->getOne($query);
            if ($bifKey != $row["SCHOOL_KIND"]) {
                $row["ROWSPAN"] = $schKindCnt;
            }

            $row["FORMAT_DIV_NAME"] = $row["FORMAT_DIV"] == '1' ? '引落': '返金';
            $row["FORMAT_DIV"] = View::alink("knjp747index.php", $row["FORMAT_DIV"].'：'.$row["FORMAT_DIV_NAME"], "target=\"right_frame\"",
                                             array("cmd"            => "edit",
                                                   "SCHOOL_KIND"    => $row["SCHOOL_KIND"],
                                                   "FORMAT_DIV"     => $row["FORMAT_DIV"]));

            $bifKey = $row["SCHOOL_KIND"];
            $arg["data"][] = $row;
        }

        //hidden
        knjCreateHidden($objForm, "cmd");

        //button
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"]  = "window.open('knjp747index.php?cmd=edit','right_frame');";
        }

        //db切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp747Form1.html", $arg);
    }
}
?>
