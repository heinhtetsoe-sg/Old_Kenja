<?php

require_once('for_php7.php');

class knjx_qualified_selectForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjx_qualified_selectindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

		//学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //対象項目
        if ($model->Properties["useQualifiedMst"] == '1') {
            $itemArray = array("QUALIFIED", "RANK", "REGDDATE");
            $arg["useQualifiedMst"] = 1;
        } else {
            $itemArray = array("CONTENTS", "REMARK", "REGDDATE");
            $arg["NOTuseQualifiedMst"] = 1;
        }
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

		//検定リスト
        $counter = 0;
        if ($model->schregno) {
            $result = $db->query(knjx_qualified_selectQuery::getAward($model, $db));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["REGDDATE"] = str_replace("-","/",$row["REGDDATE"]);
                $row["REGDDATE_SHOW"] = str_replace("-","/",$row["REGDDATE"]);

                $row["RANK_SHOW"]  = $row["RANK"];
                $row["RANK_SHOW"] .= ((strlen($row["RANK_SHOW"]) > 0) ? " " : "").((strlen($row["SCORE"]) > 0) ? $row["SCORE"]."点" : "");
                $row["RANK_SHOW"] .= ((strlen($row["RANK_SHOW"]) > 0) ? " " : "").$row["REMARK"];

                $row["CONTENTS_SHOW"] = $row["CONTENTS"];
                $row["REMARK_SHOW"] = $row["REMARK"];

                //選択チェックボックス
                $value = $counter;
                $extra = "onclick=\"OptionUse(this);\"";
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $value, $extra, "1");

                foreach ($itemArray as $key) {
                    knjCreateHidden($objForm, $key.":".$counter, $row[$key."_SHOW"]);
                }

                $arg["data"][] = $row;
                $counter++;
            }
        }

        foreach ($itemArray as $key) {
            //対象項目チェックボックス
            $extra  = ($counter > 0) ? "" : "disabled";
            $extra .= " checked id=\"CHECK_{$key}\" onclick=\"return OptionUse(this);\"";
            $arg["CHECK_".$key] = knjCreateCheckBox($objForm, "CHECK_".$key, $key, $extra, "");
        }

        //ALLチェック
        /* Edit by PP for empty data start 2020/01/20 */
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
        $extra = "onclick=\"return parent.closeit()\" aria-label = \"戻る\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){ 
            $arg["reload"] = "parent.edit_frame.location.href='knjx_qualified_selectindex.php?cmd=edit'";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_qualified_selectForm1.html", $arg);
    }
}

?>
