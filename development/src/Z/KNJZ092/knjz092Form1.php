<?php

require_once('for_php7.php');

class knjz092Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form();
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz092index.php", "", "edit");

        //DB接続
        $db     = Query::dbCheckOut();

        //出身学校一覧取得
        $query  = knjz092Query::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($row["FINSCHOOLCD"] == $model->finschoolcd) {
                $row["FINSCHOOL_TYPE"] = ($row["FINSCHOOL_TYPE"]) ? $row["FINSCHOOL_TYPE"] : "　";
                $row["FINSCHOOL_TYPE"] = "<a name=\"target\">{$row["FINSCHOOL_TYPE"]}</a><script>location.href='#target';</script>";
            }
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz092Form1.html", $arg);
    }
}
