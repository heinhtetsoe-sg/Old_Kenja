<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje011aSubForm4 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform4", "POST", "knje011aindex.php", "", "subform4");

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        //委員会リスト
        $query = knje011aQuery::getCommittee($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
                $row = str_replace("-","/",$row);
                $row["COMMITTEE"] = "　".$row["GRADE"]." ／ ".$row["SEMESTERNAME"]." ／ ".$row["COMMITTEENAME"]." ／ ".$row["CHARGENAME"]." ／ ".$row["NAME1"];
                $arg["data"][] = $row;
        }

        //DB切断
        Query::dbCheckIn($db);

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return parent.closeit()\"" ));
        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje011aSubForm4.html", $arg);
    }
}
?>
