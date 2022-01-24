<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjx_reason_collection_selectForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_reason_collection_selectindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //ALLチェック
        $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //対象項目
        $itemArray = array("ATTEND_REMARK");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //出欠備考リスト
        $counter = 0;
        $query = knjx_reason_collection_selectQuery::getReasonCollection($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //選択チェックボックス
            $value = $row["COLLECTION_CD"];
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

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_reason_collection_selectForm1.html", $arg);
    }
}
