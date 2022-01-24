<?php

require_once('for_php7.php');

class knjz450Form1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz450index.php", "", "edit");
        $db = Query::dbCheckOut();

        //学校管理資格フラグ使用するか
        if ($model->Properties["useQualifiedManagementFlg"] == "1") {
            $arg["useQualifiedManagementFlg"] = "1";
        } else {
            $arg["useQualifiedManagementFlg"] = "";
        }

        /**********/
        /* リスト */
        /**********/
        $query = knjz450Query::getList($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($row["QUALIFIED_CD"] == $model->qualified_cd) {
                $row["QUALIFIED_NAME"] = ($row["QUALIFIED_NAME"]) ? $row["QUALIFIED_NAME"] : "　";
//                $row["QUALIFIED_NAME"] = "<a name=\"target\">{$row["QUALIFIED_NAME"]}</a><script>location.href='#target';</script>";
            }
            $row["MANAGEMENT_FLG"] = ($row["MANAGEMENT_FLG"] == "1") ? "管理": "";
            
            
            $row["link"] = View::alink(REQUESTROOT."/Z/KNJZ450_2/knjz450_2index.php", "設定" , "target=\"_parent\" ",
                                       array("QUALIFIED_CD" => $row["QUALIFIED_CD"],
                                             "QUALIFIED_NAME" => $row["QUALIFIED_NAME"]));

            $arg["data"][] = $row;
        }
        $result->free();

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz450Form1.html", $arg);
    }
}
?>
