<?php
//ビュー作成用クラス
class knja125jClubKiroku {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("clubKiroku", "POST", "knja125jindex.php", "", "clubKiroku");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //記録備考リスト
        $query = knja125jQuery::getSchregClubHdetailDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["KIROKU"]  = "　".$row["CLUBNAME"];
            $row["KIROKU"] .= " ／ ".$row["HOSTNAME"];
            $row["KIROKU"] .= " ／ ".$row["MEET_NAME"];
            $row["KIROKU"] .= " ／ ".str_replace("-","/",$row["DETAIL_DATE"]);
            $row["KIROKU"] .= " ／ ".$row["DIV_NAME"];
            $row["KIROKU"] .= " ／ ".$row["RECORDNAME"];
            $row["KIROKU"] .= " ／ ".$row["DOCUMENT"];
            if (strlen($row["DOCUMENT"]) > 0 && strlen($row["DETAIL_REMARK"]) > 0) $row["KIROKU"] .= "／";
            $row["KIROKU"] .= $row["DETAIL_REMARK"];

            $arg["data"][] = $row;
        }

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja125jClubKiroku.html", $arg);
    }
}
?>
