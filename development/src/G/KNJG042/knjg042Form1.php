<?php

require_once('for_php7.php');

class knjg042Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjg042index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //許可区分コンボボックス
        $query = knjg042Query::getNameMst("G101");
        $extra = "onChange=\"return ChangeSelection_perm(this);\"";
        makeCmb($objForm, $arg, $db, $query, "perm_div", $model->perm_div, $extra, 1);

        //申請区分コンボボックス
        $query = knjg042Query::getNameMst("G100");
        $extra = "onChange=\"return Cleaning(this);\"";
        makeCmb($objForm, $arg, $db, $query, "apply_div", $model->apply_div, $extra, 1, "blank");

        //一覧表示
        $query = knjg042Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["PERM_NAME"] = $model->perm_data[$row["PERM_CD"]]["NAME1"];

            //権限
            $row["PERM_NAME"] = View::alink("knjg042index.php", $row["PERM_NAME"], "target=right_frame",
                                                array("cmd"            => "edit",
                                                      "applyday"       => $row["APPLYDAY"],
                                                      "APPLYCD"        => $row["APPLYCD"],
                                                      "STAFFCD"          => $row["STAFFCD"],
                                                      "sdate"          => $row["SDATE"],
                                                      "edate"          => $row["EDATE"],
                                                      "PERM_CD"        => $row["PERM_CD"]
                                                      )
                                           );

            $row["APPLYDAY"] = str_replace("-", "/", $row["APPLYDAY"]);
            $row["APPLYNAME"] = $db->getOne(knjg042Query::getNameMst("G100", $row["APPLYCD"]));

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
        View::toHTML($model, "knjg042Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
