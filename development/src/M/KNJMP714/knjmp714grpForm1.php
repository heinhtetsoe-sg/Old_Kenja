<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjmp714grpForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform3", "POST", "knjmp714index.php", "", "subform3");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $query = knjmp714Query::getSchregInfo($model);
        $schInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["NAME_SHOW"] = $schInfo["HR_NAME"].$schInfo["ATTENDNO"]."番 ".$schInfo["SCHREGNO"]."　".$schInfo["NAME"];

        $query = knjmp714Query::getMeisaiData($model);
        $result = $db->query($query);
        $setData = array();
        $befSlipNo = "";
        $totalMoney = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //伝票番号の最初のレコード
            if ($befSlipNo != $row["SLIP_NO"]) {
                $setCol = $row["CNT"] + 1;
                $row["SLIP_ROWSPAN"] = " rowspan=\"{$setCol}\" ";
            }
            //合計欄
            if ($row["ORDERCD"] == "2") {
                $totalMoney += $row["TMONEY"];
            }
            $row["TMONEY"] = number_format($row["TMONEY"]);
            $arg["data"][] = $row;
            $befSlipNo = $row["SLIP_NO"];
        }
        $arg["TOTAL_MONEY"] = number_format($totalMoney);

        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjmp714grpForm1.html", $arg);
    }
}
?>
