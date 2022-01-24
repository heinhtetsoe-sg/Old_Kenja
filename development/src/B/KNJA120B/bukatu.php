<?php
//ビュー作成用クラス
class bukatu {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform3", "POST", "knja120bindex.php", "", "subform3");

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        //部クラブ一覧
        $query = knja120bQuery::getClub($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row = str_replace("-","/",$row);
            $row["CLUBNAME"] = "　".$row["CLUBNAME"]." ／ ".$row["SDATE"]."～".$row["EDATE"]." ／ ".$row["NAME1"];
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
        View::toHTML($model, "bukatu.html", $arg);
    }
}
?>
