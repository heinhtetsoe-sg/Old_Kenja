<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjx_committee_selectForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_committee_selectindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //対象項目
        $itemArray = array("SEMESTER", "COMMITTEE_FLG", "COMMITTEE", "EXECUTIVE");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //対象年度取得
        $year = array();
        if ($model->send_prgid == "KNJE020") {
            $query = knjx_committee_selectQuery::getRegdYear($model);
            $year = $db->getCol($query);
        } else {
            $year[] = $model->exp_year;
        }

        //委員会リスト
        $counter = 0;
        $query = knjx_committee_selectQuery::getCommittee($model, $year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //選択チェックボックス
            $value = $row["SEQ"];
            $names = $row["COMMITTEE_SHOW"];
            $extra = "onclick=\"OptionUse(this);\" aria-label =\"委員会の$names\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $value, $extra, "1");

            //委員会・係り
            if ($row["CHARGE_SHOW"] != "") {
                $row["COMMITTEE_SHOW"] .= "/".$row["CHARGE_SHOW"];
            }

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

        //ALLチェック
        /* Edit by HPA for empty data and for PC-talker 読み start 2020/01/20 */
        // $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        $empty_data = ($counter > 0)? "": "onfocus=\"empty_data();\"";
        $extra = " id=\"CHECKALL\" $empty_data onClick=\"check_all(this); OptionUse(this)\" aria-label = \"全てを選択\"";
        /* Edit by HPA for empty data and for PC-talker 読み end 2020/01/31 */
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //取込ボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$model->target}');\" aria-label = \"取込\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "onclick=\"return parent.closeit()\" aria-label = \"戻る\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_committee_selectForm1.html", $arg);
    }
}
