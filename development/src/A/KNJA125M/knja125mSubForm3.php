<?php

require_once('for_php7.php');

class knja125mSubForm3 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3", "POST", "knja125mindex.php", "", "subform3");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

		//年度・学期表示
		$arg["YEAR_SEMESTER"] = $model->exp_year."年度　".$model->control["学期名"][$model->exp_semester];

		//委員会リスト
        $query = knja125mQuery::getCommittee($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
            $row = str_replace("-","/",$row);
            $row["COMMITTEE"] = "　".$row["GRADE"]." ／ ".$row["SEMESTERNAME"]." ／ ".$row["COMMITTEENAME"]." ／ ".$row["CHARGENAME"]." ／ ".$row["NAME1"];
            $arg["data"][] = $row;
        }

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja125mSubForm3.html", $arg);
    }
}
?>
