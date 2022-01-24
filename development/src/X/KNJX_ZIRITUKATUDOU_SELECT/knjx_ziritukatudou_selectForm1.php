<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjx_ziritukatudou_selectForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjx_ziritukatudou_selectindex.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //対象項目
        $itemArray = array("SEMESTERNAME", "REMARK1");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //自立活動リスト
        $counter = 0;
        $query = knjx_ziritukatudou_selectQuery::getZiritukatudou($model, $sdate, $edate);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //選択チェックボックス
            $value = $row["SEMESTER"];
            $extra = "onclick=\"OptionUse(this);\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $value, $extra, "1");

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
        $extra = " id=\"CHECKALL\" $empty_data onClick=\"check_all(this); OptionUse(this)\" aria-label = \"全てを選択\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //取込ボタン
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$model->target}'); \" aria-label = \"取込\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\" aria-label = \"戻る\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_ziritukatudou_selectForm1.html", $arg);
    }
}
