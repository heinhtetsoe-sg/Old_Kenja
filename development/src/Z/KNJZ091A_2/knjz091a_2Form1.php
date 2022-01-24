<?php

require_once('for_php7.php');

class knjz091a_2Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz091a_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        $query = knjz091a_2Query::selectQuery($model, "CNT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataCnt[$row["PRISCHOOLCD"]] = $row["CNT"];
        }
        $result->free();


        $query = knjz091a_2Query::selectQuery($model);
        $result = $db->query($query);
        $befPriCd = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $setRow = array();
            $setPriKana = $row["PRISCHOOL_KANA"];
            if ($befPriCd != $row["PRISCHOOLCD"]) {
                $setRow = $row;
                $setRow["PRI_ROWSPAN"] = $dataCnt[$row["PRISCHOOLCD"]];
            } else {
                $setRow["PRISCHOOL_CLASS_NAME"] = $row["PRISCHOOL_CLASS_NAME"];
            }
            $befPriCd = $row["PRISCHOOLCD"];
            $arg["data"][] = $setRow;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz091a_2Form1.html", $arg);
    }
}
