<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010bSubForm3 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform3", "POST", "knje010bindex.php", "", "subform3");

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        //部クラブ一覧
        $query = knje010bQuery::getClub($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row = str_replace("-","/",$row);
            //和暦表示
            if ($model->Properties["useWarekiHyoji"] == "1") {
                $row["SDATE"] = common::DateConv1($row["SDATE"], 0);
                $row["EDATE"] = common::DateConv1($row["EDATE"], 0);
            }
            $row["CLUBNAME"] = "　".$row["CLUBNAME"]." ／ ".$row["SDATE"]."～".$row["EDATE"]." ／ ".$row["NAME1"]." ／ ".$row["REMARK"];
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
        View::toHTML($model, "knje010bSubForm3.html", $arg);
    }
}
?>
