<?php

require_once('for_php7.php');

class knjz095kForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz095kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)) {
            if ($model->cmd == 'prefecturescd') {
                $Row =& $model->field;
            } else {
                $Row = knjz095kQuery::getRow($model,1);
            }
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        /******************/
        /* コンボボックス */
        /******************/
        //都道府県コードコンボ
        $model->year = $model->year ? $model->year : CTRL_YEAR;
        $result    = $db->query(knjz095kQuery::getName($model->year));
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        if($Row["PREFECTURESCD"] == "") {
            $Row["PREFECTURESCD"] = $opt[0]["value"];
        }
        $result->free();
        $extra = "onChange=\"btn_submit('prefecturescd')\"";
        $arg["data"]["PREFECTURESCD"] = knjCreateCombo($objForm, "PREFECTURESCD", $Row["PREFECTURESCD"], $opt, $extra, 1);

        //学年コンボ
        $result    = $db->query(knjz095kQuery::getGrade($model->year));
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["GRADE"],
                           "value" => $row["GRADE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $Row["GRADE"], $opt, $extra, 1);

        /********************/
        /* チェックボックス */
        /********************/
        
        //入学年度使用
        $extra = "id=\"CURRICULUM_FLG\"";
        if ($Row["CURRICULUM_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["CURRICULUM_FLG"] = knjCreateCheckBox($objForm, "CURRICULUM_FLG", "1", $extra);

        //今年度使用
        $extra = "id=\"THIS_YEAR_FLG\"";
        if ($Row["THIS_YEAR_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["THIS_YEAR_FLG"] = knjCreateCheckBox($objForm, "THIS_YEAR_FLG", "1", $extra);

        //ランク使用
        $extra = "id=\"USE_RANK\"";
        if ($Row["USE_RANK"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["USE_RANK"] = knjCreateCheckBox($objForm, "USE_RANK", "1", $extra);
        
        
        /********************/
        /* テキストボックス */
        /********************/
        //前期開始年度
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ZENKI_KAISI_YEAR"] = knjCreateTextBox($objForm, $Row["ZENKI_KAISI_YEAR"], "ZENKI_KAISI_YEAR", 8, 8, $extra);
        //後期開始年度
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KOUKI_KAISI_YEAR"] = knjCreateTextBox($objForm, $Row["KOUKI_KAISI_YEAR"], "KOUKI_KAISI_YEAR", 8, 8, $extra);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz095kindex.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz095kForm2.html", $arg);
    }
}
?>
