<?php

require_once('for_php7.php');

class knjz050wForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz050windex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボボックス
        $opt = array();
        $value_flg = false;
        $query  = knjz050wQuery::selectYearQuery();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        if ($model->year == "") {
            $model->year = CTRL_YEAR + 1;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_year_add"] = knjCreateBtn($objForm, "btn_year_add", "次年度作成", $extra);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz050wForm1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $result = $db->query(knjz050wQuery::getList($model));

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
         array_walk($row, "htmlspecialchars_array");
         $arg["data"][] = $row;
    }

    $result->free();
}

//権限チェック
function authCheck(&$arg) {
    $adminFlg = knjz050wQuery::getAdminFlg();
    if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1") {
        $arg["jscript"] = "OnAuthError();";
    }
}

?>
