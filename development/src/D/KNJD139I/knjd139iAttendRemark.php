<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd139iAttendRemark
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("AttendRemark", "POST", "knjd139iindex.php", "", "AttendRemark");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度・学期表示
        $arg["YEAR_SEMESTER"] = $model->exp_year."年度　".$model->control["学期名"][$model->exp_semester];

        //出欠備考取得
        $counter = 0;
        $query = knjd139iQuery::getAttendAbsenceRemarkDat($model);
        $result = $db->query($query);
        $attendRemark = $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = ($row["REMARK"] == "") ? "": $sep;
            $attendRemark .= $sep.$row["REMARK"];
            $sep = "、";
        }
        $arg["data"]["remark_attend"] = knjCreateTextArea($objForm, "REMARK_ATTEND", "3", "32", "", $extra, $attendRemark);

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd139iAttendRemark.html", $arg);
    }
}
