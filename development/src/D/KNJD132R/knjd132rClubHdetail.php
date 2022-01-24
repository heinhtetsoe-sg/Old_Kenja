<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd132rClubHdetail {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("clubhdetail", "POST", "knjd132rindex.php", "", "clubhdetail");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //ALLチェック
        $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //対象項目
        $itemArray = array("CLUB", "MEET");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //記録備考リスト
        $counter = 0;
        $query = knjd132rQuery::getSchregClubHdetailDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["DETAIL_DATE"] = str_replace("-","/",$row["DETAIL_DATE"]);

            $row["MEET_SHOW"]  = $row["MEET_NAME"];
            $row["MEET_SHOW"] .= ((strlen($row["MEET_SHOW"]) > 0) ? " " : "").$row["RECORDNAME"];
            $row["MEET_SHOW"] .= ((strlen($row["MEET_SHOW"]) > 0 && substr($row["MEET_SHOW"], -1) != " " && strlen($row["DOCUMENT"]) > 0) ? " " : "").$row["DOCUMENT"];

            //選択チェックボックス
            $value = $row["CLUBCD"].":".$row["DETAIL_DATE"].":".$row["DETAIL_SEQ"];
            $extra = "onclick=\"OptionUse(this);\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $value, $extra, "1");

            foreach ($itemArray as $key) {
                knjCreateHidden($objForm, $key.":".$value, $row[$key."_SHOW"]);
            }

            $arg["data"][] = $row;
            $counter++;
        }

        foreach ($itemArray as $key) {
            //対象項目チェックボックス
            $extra  = ($counter > 0) ? "" : "disabled";
            $extra .= " checked id=\"CHECK_{$key}\" onclick=\"return OptionUse(this);\"";
            $arg["CHECK_".$key] = knjCreateCheckBox($objForm, "CHECK_".$key, $key, $extra, "");
        }

        //取込ボタン
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$model->target}');\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

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
        View::toHTML($model, "knjd132rClubHdetail.html", $arg);
    }
}
?>
