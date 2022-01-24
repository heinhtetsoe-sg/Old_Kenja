<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja128hSubForm3
{

    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3", "POST", "knja128hindex.php", "", "subform3");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度・学期表示
        $arg["YEAR_SEMESTER"] = $model->exp_year."年度　".$model->control["学期名"][$model->exp_semester];

        //委員会リスト
        $query = knja128hQuery::getCommittee($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("-", "/", $row);
                $row["COMMITTEE"] = "　".$row["GRADE"]." ／ ".$row["SEMESTERNAME"]." ／  ".$row["COMMITTEENAME"]." ／ ".$row["CHARGENAME"]." ／ ".$row["NAME1"];
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
        View::toHTML($model, "knja128hSubForm3.html", $arg);
    }
}
