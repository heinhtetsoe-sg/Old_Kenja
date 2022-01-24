<?php

require_once('for_php7.php');

class knjp747aForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp747aindex.php", "", "edit");

        //db接続
        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = CTRL_YEAR;

        //項目名セット
        for ($i=1; $i <= 12; $i++) {
            if ($model->nameArr[$i] != '') {
                $arg["ITEM_".$i] = str_replace("コード", "<br>コード", $model->nameArr[$i]);
            } else {
                $arg["ITEM_".$i] = "ヘッダー・レコード({$i})";
            }
        }

        //リスト表示
        $bifKeyKind = "";
        $bifKeyBank = "";
        $query = knjp747aQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $query = knjp747aQuery::getList($model, $row["SCHOOL_KIND"]);
            $schKindCnt = $db->getOne($query);
            if ($bifKeyKind != $row["SCHOOL_KIND"]) {
                $row["ROWSPAN_KIND"] = $schKindCnt;
            }

            $query = knjp747aQuery::getList($model, $row["SCHOOL_KIND"], $row["BANK_CD"]);
            $schBnkCnt = $db->getOne($query);
            if ($bifKeyBank != $row["SCHOOL_KIND"].$row["BANK_CD"]) {
                $row["ROWSPAN_BANK"] = $schBnkCnt;
            }

            $row["FORMAT_DIV_NAME"] = $row["FORMAT_DIV"] == '1' ? '引落': '返金';
            $row["FORMAT_DIV"] = View::alink("knjp747aindex.php", $row["FORMAT_DIV"].'：'.$row["FORMAT_DIV_NAME"], "target=\"right_frame\"",
                                             array("cmd"            => "edit",
                                                   "SCHOOL_KIND"    => $row["SCHOOL_KIND"],
                                                   "BANK_CD"        => $row["BANK_CD"],
                                                   "FORMAT_DIV"     => $row["FORMAT_DIV"]));

            $bifKeyKind = $row["SCHOOL_KIND"];
            $bifKeyBank = $row["SCHOOL_KIND"].$row["BANK_CD"];
            $arg["data"][] = $row;
        }

        //hidden
        knjCreateHidden($objForm, "cmd");

        //button
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"]  = "window.open('knjp747aindex.php?cmd=edit','right_frame');";
        }

        //db切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp747aForm1.html", $arg);
    }
}
?>
