<?php

require_once('for_php7.php');

class knjz410aSubForm2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz410aindex.php", "", "edit");
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (isset($model->school_cd) && !isset($model->warning) && $model->cmd != 'add_addr_chenge_cd') {
            $query = knjz410aQuery::getCollegeCampusAddrDat($model->school_cd, $model->campus_addr_cd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }else{
            $Row =& $model->field;
        }

        /******************/
        /* コンボボックス */
        /******************/
        //都道府県
        $opt = array();
        $value_flg = false;
        $query = knjz410aQuery::getPrefMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["PREF_CD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["PREF_CD"] = ($Row["PREF_CD"] && $value_flg) ? $Row["PREF_CD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["PREF_CD"] = knjCreateCombo($objForm, "PREF_CD", $Row["PREF_CD"], $opt, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        //学校コード
        $arg["data"]["SCHOOL_CD"] = $model->school_cd;
        //住所
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CAMPUS_ADDR_CD"] = knjCreateTextBox($objForm, $Row["CAMPUS_ADDR_CD"], "CAMPUS_ADDR_CD", 2, 2, $extra);
        //郵便番号
        $extra = "";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 8, 8, $extra);
        //住所１
        $extra = "";
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $Row["ADDR1"], "ADDR1", 60, 90, $extra);
        //住所２
        $extra = "";
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $Row["ADDR2"], "ADDR2", 60, 90, $extra);
        //電話番号
        $extra = "";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 16, 16, $extra);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add_addr_add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('add_addr_update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('add_addr_delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('add_addr_reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //戻る
        $link = REQUESTROOT."/Z/KNJZ410A/knjz410aindex.php?cmd=back";
        $extra = "onclick=\"window.open('$link','_top');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOL_CD", $model->school_cd);


        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "add_addr_edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz410aindex.php?cmd=add_addr_list';";
        }

        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz410aSubForm2.html", $arg);
    }
}
?>
