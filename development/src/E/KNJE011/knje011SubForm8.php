<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje011SubForm8 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform8", "POST", "knje011index.php", "", "subform8");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //記録備考リスト
        $query = knje011Query::getSchregClubHdetailDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["DETAIL_DATE"]  = str_replace("-","/",$row["DETAIL_DATE"]);
            //和暦表示
            if ($model->Properties["useWarekiHyoji"] == "1") {
                $row["DETAIL_DATE"] = common::DateConv1($row["DETAIL_DATE"], 0);
            }
            $row["KIROKU"]  = "　".$row["CLUBNAME"];
            $row["KIROKU"] .= " ／ ".$row["HOSTNAME"];
            $row["KIROKU"] .= " ／ ".$row["MEET_NAME"];
            $row["KIROKU"] .= " ／ ".$row["DETAIL_DATE"];
            $row["KIROKU"] .= " ／ ".$row["DIV_NAME"];
            $row["KIROKU"] .= " ／ ".$row["RECORDNAME"];
            $row["KIROKU"] .= " ／ ".$row["DOCUMENT"];
            if (strlen($row["DOCUMENT"]) > 0 && strlen($row["DETAIL_REMARK"]) > 0) $row["KIROKU"] .= "／";
            $row["KIROKU"] .= $row["DETAIL_REMARK"];

            $arg["data"][] = $row;
        }

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje011SubForm8.html", $arg);
    }
}
?>
