<?php

require_once('for_php7.php');

class knjz070_2aForm1 {

    function main(&$model) {

        //権限チェック
        $arg["jscript"] = "";
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz070_2aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //一覧取得
        $query = knjz070_2aQuery::getListData();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            //選択
            $row["IBELECTDIV"] = ($row["IBELECTDIV"] == "1") ? "選" : "";

            //更新後この行が画面の先頭に来るようにする
            $subclass1 = $row["IBCLASSCD"].$row["IBPRG_COURSE"].$row["IBCURRICULUM_CD"].$row["IBSUBCLASSCD"];
            $subclass2 = $model->ibclasscd.$model->ibprg_course.$model->ibcurriculum_cd.$model->ibsubclasscd;
            if ($subclass1 == $subclass2) {
                $row["IBSUBCLASSNAME"] = ($row["IBSUBCLASSNAME"]) ? $row["IBSUBCLASSNAME"] : "　";
                $row["IBSUBCLASSNAME"] = "<a name=\"target\">{$row["IBSUBCLASSNAME"]}</a><script>location.href='#target';</script>";
            }

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz070_2aForm1.html", $arg); 
    }
}
?>
