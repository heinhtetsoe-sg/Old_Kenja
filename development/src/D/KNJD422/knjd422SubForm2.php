<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd422SubForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjd422index.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //部活一覧
        $query = knjd422Query::getClub($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("-","/",$row);
                $row["CLUBNAME"] = "　".$row["CLUBNAME"]." ／ ".$row["SDATE"]."～".$row["EDATE"]." ／ ".$row["NAME1"];
                $arg["data"][] = $row;
        }

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd422SubForm2.html", $arg);
    }
}
?>
