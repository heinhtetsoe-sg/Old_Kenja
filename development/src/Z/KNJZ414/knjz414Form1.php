<?php

require_once('for_php7.php');

class knjz414Form1 {

    function main(&$model) {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz414index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //リスト作成
        $key = "";
        $query = knjz414Query::getList();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($key !== $row["INDUSTRY_LCD"]) {
                $cnt = get_count($db->getCol(knjz414Query::chkIndustryM($row["INDUSTRY_LCD"])));
                $row["ROWSPAN"] = ($cnt) > 0 ? $cnt : 1;
            }
            $key = $row["INDUSTRY_LCD"];

            if ($row["INDUSTRY_LCD"] == $model->industry_lcd) {
                $row["INDUSTRY_LNAME"] = ($row["INDUSTRY_LNAME"]) ? $row["INDUSTRY_LNAME"] : "　";
                $row["INDUSTRY_LNAME"] = "<a name=\"target\">{$row["INDUSTRY_LNAME"]}</a><script>location.href='#target';</script>";
            }

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz414Form1.html", $arg);
    }
}
?>
