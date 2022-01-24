<?php

require_once('for_php7.php');

class knjz020mForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz020mindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //リスト作成
        makeList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz020mForm1.html", $arg); 
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $bifKey = "";
    $query = knjz020mQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        $row["ROWSPAN"] = $row["NAMESPARE1"];
        //科目の満点を取ってくる
        $query = knjz020mQuery::getPerfect($row, $model);
        $Perfect = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $extra = "style=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\"";
        $row["PERFECT"] = knjCreateTextBox($objForm, $Perfect["PERFECT"], "PERFECT_{$row["NAMECD2"]}", 3, 3, $extra);
        $row["RATE"] = knjCreateTextBox($objForm, $Perfect["RATE"], "RATE_{$row["NAMECD2"]}", 3, 3, $extra);

        for ($i = 1; $i <= $row["NAMESPARE1"]; $i++) {
            //科目の答案用紙ごとのの満点を取ってくる
            $query = knjz020mQuery::getDetailPerfect($row, $model, $i);
            $detail_value = $db->getOne($query);
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\"";
            $row["DETAIL_PERFECT"] = knjCreateTextBox($objForm, $detail_value, "DETAIL_PERFECT_{$row["NAMECD2"]}_{$i}", 3, 3, $extra);
            $row["TESTPAPERCD"] = $i;
            $arg["data"][] = $row;
            unset($row["ROWSPAN"]);
        }
    }
    $result->free();
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
?>
