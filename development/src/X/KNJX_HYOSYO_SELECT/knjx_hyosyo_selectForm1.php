<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjx_hyosyo_selectForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;
        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_hyosyo_selectindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

		//学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        
        //対象項目
        $itemArray = array("DETAIL_SDATE", "DETAILCDNAME", "CONTENT", "REMARK");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //学校名取得
        $schoolName = $db->getOne(knjx_hyosyo_selectQuery::getSchoolName());

        //常盤木は活動の記録を出力
        if ($schoolName == "tokiwagi") {
            $detail_div = "4";
            $namecd1    = "H317";
        } else {
            $detail_div = "1";
            $namecd1    = "H303";
        }

		//賞リスト
        $counter = 0;
        $query = knjx_hyosyo_selectQuery::getHyosyo($model, $detail_div, $namecd1);
        $result = $db->query($query);
        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["DETAIL_SDATE"] = str_replace("-","/",$row["DETAIL_SDATE"]);

            //選択チェックボックス
            $value = $row["DETAIL_SDATE"];
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
        /* Edit by PP for empty data start 2020/01/20 */
        // $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\" aria-label = \"全てを選択\"";
        $empty_data = ($counter > 0)? "": "onfocus=\"empty_data();\"";
        $extra = " id=\"CHECKALL\" $empty_data onClick=\"check_all(this); OptionUse(this)\" aria-label = \"全てを選択\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");
        /* Edit by PP for empty data end 2020/01/31 */

        //取込ボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$model->target}');\" aria-label = \"取込\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "onclick=\"return parent.closeit()\" aria-label = \"戻る\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_hyosyo_selectForm1.html", $arg);
    }
}
?>
