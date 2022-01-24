<?php

require_once('for_php7.php');

class knjz405Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjz405Form1", "POST", "knjz405index.php", "", "knjz405Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //データ表示
        $point_l_cd = "";
        $result = $db->query(knjz405Query::getList());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg["data"]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            //同じねらいコードはまとめる
            $rowspan = 0;
            if ($point_l_cd != $row["POINT_L_CD"]) {
                $rowspan = get_count($db->getCol(knjz405Query::getList($row["POINT_L_CD"])));
            }
            $row["ROWSPAN"] = $rowspan;

            $arg["data"][] = $row;

            $point_l_cd = $row["POINT_L_CD"];
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz405Form1.html", $arg); 
    }
}
?>
