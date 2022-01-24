<?php
class knjl032yForm2
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl032yindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //試験時間割ヘッダデータ
        if ($model->isWarning() || $model->cmd == "detail") {
            $Row =& $model->field;
        } else {
            $query = knjl032yQuery::getPtrnList($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //試験時間割名テキスト
        $name  = "PATTERN_NAME";
        $extra = "style=\"width:100%\"";
        $arg[$name] = knjCreateTextBox($objForm, $Row[$name], $name, 30, 30, $extra);

        //校時数テキスト
        $name  = "PERIOD_CNT";
        $extra = "style=\"text-align:right\" onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg[$name] = knjCreateTextBox($objForm, $Row[$name], $name, 5, 1, $extra);

        //試験科目の配列
        $arrTestSub   = array();
        $arrTestSub[] = array('label' => "", 'value' => "");
        $query  = knjl032yQuery::getTestSubclasscd($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrTestSub[] = array('label' => $row["LABEL"],
                                  'value' => $row["VALUE"]);
        }
        $result->free();

        //試験時間割科目データ
        if ($model->isWarning() || $model->cmd == "detail") {
            for ($periodcd = 1; $periodcd <= $Row["PERIOD_CNT"]; $periodcd++) {
                //初期化
                $row = array();
                //校時
                $row["PERIODCD"] = $periodcd;
                //試験科目コンボ
                $name  = "TESTSUBCLASSCD";
                $name2 = $name.$periodcd;
                $extra = "style=\"width:100%\"";
                $row[$name] = knjCreateCombo($objForm, $name2, $Row[$name2], $arrTestSub, $extra, 1);
                //時(S)テキスト
                $name  = "S_HOUR";
                $name2 = $name.$periodcd;
                $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
                $row[$name] = knjCreateTextBox($objForm, $Row[$name2], $name2, 3, 2, $extra);
                //分(S)テキスト
                $name  = "S_MINUTE";
                $name2 = $name.$periodcd;
                $row[$name] = knjCreateTextBox($objForm, $Row[$name2], $name2, 3, 2, $extra);
                //時(E)テキスト
                $name  = "E_HOUR";
                $name2 = $name.$periodcd;
                $row[$name] = knjCreateTextBox($objForm, $Row[$name2], $name2, 3, 2, $extra);
                //分(E)テキスト
                $name  = "E_MINUTE";
                $name2 = $name.$periodcd;
                $row[$name] = knjCreateTextBox($objForm, $Row[$name2], $name2, 3, 2, $extra);

                $arg["data2"][] = $row;
            }
        } else {
            $query = knjl032yQuery::getPtrnSubList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //校時
                $periodcd = $row["PERIODCD"];
                //試験科目コンボ
                $name  = "TESTSUBCLASSCD";
                $name2 = $name.$periodcd;
                $extra = "style=\"width:100%\"";
                $row[$name] = knjCreateCombo($objForm, $name2, $row[$name], $arrTestSub, $extra, 1);
                //時(S)テキスト
                $name  = "S_HOUR";
                $name2 = $name.$periodcd;
                $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name2, 3, 2, $extra);
                //分(S)テキスト
                $name  = "S_MINUTE";
                $name2 = $name.$periodcd;
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name2, 3, 2, $extra);
                //時(E)テキスト
                $name  = "E_HOUR";
                $name2 = $name.$periodcd;
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name2, 3, 2, $extra);
                //分(E)テキスト
                $name  = "E_MINUTE";
                $name2 = $name.$periodcd;
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name2, 3, 2, $extra);

                $arg["data2"][] = $row;
            }
            $result->free();
        }

        //確定ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_detail",
                            "value"       => "確 定",
                            "extrahtml"   => "onclick=\"return btn_submit('detail')\"" ) );
        $arg["btn_detail"]  = $objForm->ge("btn_detail");



        //追加・更新ボタン
        if ($model->mode == "update"){
            $value = "更 新";
        }else{
            $value = "追 加";
        }
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => $value,
                            "extrahtml"   => "onclick=\"return btn_submit('".$model->mode ."')\"" ) );
        $arg["btn_update"]  = $objForm->ge("btn_update");

        //戻るボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"top.main_frame.closeit()\"" ) );
        $arg["btn_back"]  = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl032yForm2.html", $arg); 
    }
}
?>
