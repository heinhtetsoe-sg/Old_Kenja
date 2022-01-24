<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd422SubForm3
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform3", "POST", "knjd422index.php", "", "subform3");

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度・学期表示
        $arg["YEAR_SEMESTER"] = $model->exp_year."年度　".$model->control["学期名"][$model->exp_semester];

        //委員会リスト
        $db = Query::dbCheckOut();
        $query = knjd422Query::getCommittee($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {

                $row = str_replace("-","/",$row);
                $row["COMMITTEE"] = "　".$row["GRADE"]." ／ ".$row["SEMESTERNAME"]." ／ ".$row["COMMITTEENAME"]." ／ ".$row["CHARGENAME"]." ／ ".$row["NAME1"];
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
        View::toHTML($model, "knjd422SubForm3.html", $arg);
    }
}
?>
