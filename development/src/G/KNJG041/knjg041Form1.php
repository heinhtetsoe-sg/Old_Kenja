<?php

require_once('for_php7.php');

class knjg041Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjg041index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //職員情報表示
        $query = knjg041Query::getStaffInfo($model);
        $staff = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["SECTIONNAME"]   = $staff["SECTIONNAME"];
        $arg["JOBNAME"]       = $staff["JOBNAME"];
        $arg["STAFFNAME"]     = $staff["STAFFNAME"];

        //一覧表示
        $query = knjg041Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["PERM_NAME"] = $model->perm_data[$row["PERM_CD"]]["NAME1"];

            //権限
            $row["PERM_NAME"] = View::alink("knjg041index.php", $row["PERM_NAME"], "target=right_frame",
                                                array("cmd"            => "edit",
                                                      "applyday"       => $row["APPLYDAY"],
                                                      "APPLYCD"        => $row["APPLYCD"],
                                                      "sdate"          => $row["SDATE"],
                                                      "edate"          => $row["EDATE"],
                                                      "PERM_CD"        => $row["PERM_CD"]
                                                      )
                                           );

            $row["APPLYDAY"] = str_replace("-", "/", $row["APPLYDAY"]);
            $row["APPLYNAME"] = $db->getOne(knjg041Query::getNameMst("G100", $row["APPLYCD"]));

            $sdate = explode(" ", $row["SDATE"]);
            $edate = explode(" ", $row["EDATE"]);
            $row["KIKAN"] = str_replace("-", "/", $sdate[0]).' ～ '.str_replace("-", "/", $edate[0]);

            $arg["data"][] = $row;
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg041Form1.html", $arg);
    }
}
?>
