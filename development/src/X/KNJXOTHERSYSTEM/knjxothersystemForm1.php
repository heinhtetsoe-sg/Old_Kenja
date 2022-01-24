<?php

require_once('for_php7.php');

class knjxothersystemForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("detail", "POST", "knjxothersystemindex.php", "", "detail");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //学籍番号
        $arg["SCHREGNO"] = $model->schregno;

        //氏名
        $schName = $db->getOne(knjxothersystemQuery::getName($model->schregno));
        $arg["NAME"] = $schName;

        //データ表示
        $query = knjxothersystemQuery::getSchregOtherSystemUserDat($model, $model->schregno);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"][] = $row;
        }
        $result->free();

        //終了ボタン
        if ($model->buttonFlg) {
            $extra = "onclick=\"closeWin()\"";
        } else {
            $extra = "onclick=\"return parent.closeit()\"";
        }
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXOTHERSYSTEM");
        knjCreateHidden($objForm, "cmd", "");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxothersystemForm1.html", $arg);
    }
}
?>
