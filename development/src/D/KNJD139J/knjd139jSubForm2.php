<?php

require_once('for_php7.php');

class knjd139jSubForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;
        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjd139jindex.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //委員会リスト
        $query = knjd139jQuery::getCommittee($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-","/",$row);
            $row["COMMITTEE"] = "　".$row["GRADE"]." ／ ".$row["SEMESTERNAME"]." ／ ".$row["COMMITTEENAME"]." ／ ".$row["CHARGENAME"]." ／ ".$row["NAME1"];
            $arg["data"][] = $row;
        }

        //終了ボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd139jSubForm2.html", $arg);
    }
}
?>

