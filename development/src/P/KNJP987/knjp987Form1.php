<?php

require_once('for_php7.php');

class knjp987Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp987index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjp987Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["TAX_CD"] = View::alink("knjp987index.php", $row["TAX_CD"], "target=\"right_frame\"",
                                                 array("cmd"            => "edit",
                                                       "TAX_CD"         => $row["TAX_CD"],
                                                       "DATE_FROM"      => $row["DATE_FROM"] ));

            $row["DATE_FROM"] = str_replace("-", "/", $row["DATE_FROM"]);
            $row["DATE_TO"]   = str_replace("-", "/", $row["DATE_TO"]);

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        if (!isset($model->warning) && $model->cmd == "change") {
            $arg["reload"] = "parent.right_frame.location.href='knjp987index.php?cmd=edit"
                           . "&year=".$model->year."&SCHOOL_KIND=".$model->schoolKind."';";

            if ($model->cmd == "change") {
                unset($model->exp_lcd);
                unset($model->exp_mcd);
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp987Form1.html", $arg);
    }
}
?>
