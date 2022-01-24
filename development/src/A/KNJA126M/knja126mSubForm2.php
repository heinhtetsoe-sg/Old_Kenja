<?php

require_once('for_php7.php');

class knja126mSubForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knja126mindex.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //部クラブ情報を取得
        $query = knja126mQuery::getClub($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
            $row = str_replace("-","/",$row);
            $row["CLUBNAME"] = "　".$row["CLUBNAME"]." ／ ".$row["SDATE"]."～".$row["EDATE"]." ／ ".$row["NAME1"];
            $arg["data"][] = $row;
        }

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja126mSubForm2.html", $arg);
    }
}
?>
