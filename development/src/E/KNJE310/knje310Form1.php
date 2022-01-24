<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje310Form1.php 56587 2017-10-22 12:54:51Z maeshiro $

class knje310Form1 {
    function main(&$model) {
        $mode = $model->mode;
        $schregno = $model->schregno;

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje310index.php", "", "edit");

        $db = Query::dbCheckOut();

        // 学籍基礎マスタより学籍番号と名前を取得
        $Row = $db->getRow(knje310Query::sqlSchregnoName($schregno, $mode), DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        // 進路情報データよりデータを取得
        if ($schregno) {
            $result = $db->query(knje310Query::sqlList($schregno));
            while($data = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data["TOROKU_DATE"] = str_replace("-", "/", $data["TOROKU_DATE"]);
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
            $arg["reload"] = "window.open('knje310index.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }

        View::toHTML($model, "knje310Form1.html", $arg);
    }
}
?>
