<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjx_club_selectForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjx_club_selectindex.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //ALLチェック
        //$extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        //$arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //対象項目
        $itemArray = array("CLUB", "EXECUTIVE");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //日付範囲
        if ($model->send_prgid == 'KNJE020') {
            $getDate = $db->getRow(knjx_club_selectQuery::getDate($model), DB_FETCHMODE_ASSOC);
            $sdate = $getDate["ENT_DATE"];
            $edate = $getDate["GRD_DATE"];
        } else {
            $sdate = $model->exp_year . '-04-01';
            $edate = ($model->exp_year + 1) . '-03-31';
        }

        //部活動リスト
        $counter = 0;
        $query = knjx_club_selectQuery::getClub($model, $sdate, $edate);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['SDATE'] = str_replace("-", "/", $row['SDATE']);
            $row['EDATE'] = str_replace("-", "/", $row['EDATE']);

            //選択チェックボックス
            $value = $row["CLUBCD"].":".$row["SDATE"];
            $extra = "onclick=\"OptionUse(this);\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $value, $extra, "1");

            foreach ($itemArray as $key) {
                knjCreateHidden($objForm, $key.":".$value, $row[$key."_SHOW"]);
            }

            //期間
            $row["SDATE_TO_EDATE"] = $row["SDATE"]."～".$row["EDATE"];

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
        // $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        $empty_data = ($counter > 0)? "": "onfocus=\"empty_data();\"";
        $extra = " id=\"CHECKALL\" $empty_data onClick=\"check_all(this); OptionUse(this)\" aria-label = \"全てを選択\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");
        /* Edit by PP for empty data end 2020/01/31 */

        //取込ボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$model->target}'); \" aria-label = \"取込\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "onclick=\"return parent.closeit()\" aria-label = \"戻る\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_club_selectForm1.html", $arg);
    }
}
