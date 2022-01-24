<?php

require_once('for_php7.php');

class knjxcommi_detailForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjxcommi_detailindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR.'年度';

        //日付一覧取得
        $result = $db->query(knjxcommi_detailQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["COMMITTEENAME"] = ($row["COMMITTEENAME"] || $row["CHARGENAME"]) ? $row["COMMITTEENAME"].$row["CHARGENAME"] : "";
            $row["DETAIL_DATE"] = str_replace("-", "/", $row["DETAIL_DATE"]);

            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxcommi_detailForm1.html", $arg);
    }
}
