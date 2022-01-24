<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje340Form1.php 56587 2017-10-22 12:54:51Z maeshiro $

class knje340Form1 {
    function main(&$model) {
        $mode = $model->mode;
        $year = $model->control_data["年度"];
        $schregno = $model->schregno;

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje340index.php", "", "edit");

        $db = Query::dbCheckOut();

        // 学籍基礎マスタより学籍番号と名前を取得
        $Row = $db->getRow(knje340Query::sqlSchregnoName($schregno, $mode), DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        // 進路情報データよりデータを取得
        if ($schregno) {
            $result = $db->query(knje340Query::sqlList($schregno));
            while($data = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data["JOB_DATE1"] = str_replace("-", "/", $data["JOB_DATE1"]);
                if ($data["YEAR"] == $year) // 対象年度が今年度の場合のみリンクを表示する。
                    $data["STAT_NAME"] = "<a href=\"knje340index.php?cmd=edit&YEAR=".$data["YEAR"]."&SEQ=".$data["SEQ"]."&SCHREGNO=".$data["SCHREGNO"]."\" target=\"edit_frame\">".$data["STAT_NAME"] ."</a>";
                $arg["data"][] = $data;
            }
        }
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") == "right_list") {
            $arg["reload"] = "window.open('knje340index.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }

        View::toHTML($model, "knje340Form1.html", $arg);
    }
}
?>
