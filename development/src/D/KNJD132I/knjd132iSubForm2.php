<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd132iSubForm2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform2", "POST", "knjd132iindex.php", "", "subform2");

        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();


        $query = knjd132iQuery::getClub($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-","/",$row);
            //和暦表示
            if ($model->Properties["useWarekiHyoji"] == 1) {
                $row["SDATE"] = common::DateConv1($row["SDATE"], 0);
                $row["EDATE"] = common::DateConv1($row["EDATE"], 0);
            }
            $row["CLUBNAME"] = "　".$row["CLUBNAME"]." ／ ".$row["SDATE"]."～".$row["EDATE"]." ／ ".$row["NAME1"];
            $arg["data"][] = $row;

        }
        Query::dbCheckIn($db);
        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻る",
                            "extrahtml" => "onclick=\"return parent.closeit()\"" ));
        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd132iSubForm2.html", $arg);
    }
}
?>
