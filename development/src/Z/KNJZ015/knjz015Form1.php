<?php

require_once('for_php7.php');

class knjz015Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz015index.php", "", "edit");

        $db = Query::dbCheckOut();

        if ($model->Properties["use_prg_schoolkind"] != "1"){
            $arg["Closing"] = "  closing_window('MSG300'); " ;
        }

        $order[$model->sort["SRT_S"]]="";
        $order[$model->sort["SRT_P"]]="";
        //ソート表示文字作成
        $order[1] = "▲";
        $order[-1] = "▼";

        //リストヘッダーソート作成
        $SCHOOL_KIND_SORT = "<a href=\"knjz015index.php?cmd=list&sort=SRT_S\" target=\"left_frame\" STYLE=\"color:white\">メニュー校種".$order[$model->sort["SRT_S"]]."</a>";

        $arg["SCHOOL_KIND_SORT"] = $SCHOOL_KIND_SORT;

        $PROGRAMID_SORT = "<a href=\"knjz015index.php?cmd=list&sort=SRT_P\" target=\"left_frame\" STYLE=\"color:white\">プログラムID".$order[$model->sort["SRT_P"]]."</a>";

        $arg["PROGRAMID_SORT"] = $PROGRAMID_SORT;

        //リスト表示
        $bifKey = $bifKey2 = "";
        $query = knjz015Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            if ($bifKey !== $row["PROGRAMID"]) {
                $cnt = $db->getOne(knjz015Query::getSelSchKindCnt($row["SCHOOL_KIND"], $row["PROGRAMID"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            if ($bifKey == $row["PROGRAMID"] && $bifKey2 != $row["SCHOOL_KIND_LABEL"]) {
                $cnt = $db->getOne(knjz015Query::getSelSchKindCnt($row["SCHOOL_KIND"], $row["PROGRAMID"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey  = $row["PROGRAMID"];
            $bifKey2 = $row["SCHOOL_KIND_LABEL"];
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz015Form1.html", $arg);
        }
    }
?>
