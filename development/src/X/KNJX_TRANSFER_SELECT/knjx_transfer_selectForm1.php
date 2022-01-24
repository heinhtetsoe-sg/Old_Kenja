<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjx_transfer_selectForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjx_transfer_selectindex.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //対象項目
        $itemArray = array("TRANSFERNAME", "SDATE_TO_EDATE", "TRANSFERREASON", "TRANSFERPLACE");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //日付範囲
        if ($model->send_prgid == 'KNJE020') {
            $getDate = $db->getRow(knjx_transfer_selectQuery::getDate($model), DB_FETCHMODE_ASSOC);
            $sdate = $getDate["ENT_DATE"];
            $edate = $getDate["GRD_DATE"];
        } else {
            $sdate = $model->exp_year . '-04-01';
            $edate = ($model->exp_year + 1) . '-03-31';
        }

        //異動情報リスト
        $counter = 0;
        $query = knjx_transfer_selectQuery::getSchregTransferDat($model, $sdate, $edate);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["TRANSFER_SDATE"] = str_replace("-", "/", $row["TRANSFER_SDATE"]);
            $row["TRANSFER_EDATE"] = str_replace("-", "/", $row["TRANSFER_EDATE"]);

            //選択チェックボックス
            $value = $row["TRANSFERCD"].":".$row["TRANSFER_SDATE"];
            $extra = "onclick=\"OptionUse(this);\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $value, $extra, "1");

            //異動期間
            $row["SDATE_TO_EDATE"] = $row["TRANSFER_SDATE"]."～".$row["TRANSFER_EDATE"];

            foreach ($itemArray as $key) {
                knjCreateHidden($objForm, $key.":".$value, $row[$key]);
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

        //ALLチェック
        $empty_data = ($counter > 0)? "": "onfocus=\"empty_data();\"";
        $extra = " id=\"CHECKALL\" $empty_data onClick=\"check_all(this); OptionUse(this)\" aria-label=\"全てを選択\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //取込ボタン
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$model->target}'); \" aria-label=\"取込\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\" aria-label=\"戻る\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_transfer_selectForm1.html", $arg);
    }
}
