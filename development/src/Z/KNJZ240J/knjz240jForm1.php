<?php

require_once('for_php7.php');
class knjz240jform1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz240jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //保健・その他ラジオボタン追加
        $opt = array(1, 2, 3, 4);
        $extra = array("id=\"DOCUMENT_DIV1\" onclick=\"return btn_submit('change');\"", "id=\"DOCUMENT_DIV2\" onclick=\"return btn_submit('change');\"", "id=\"DOCUMENT_DIV3\" onclick=\"return btn_submit('change');\"", "id=\"DOCUMENT_DIV4\" onclick=\"return btn_submit('change');\"");
        if ($model->documentMstDocumentDiv == "1") {
            $arg["TYOUSYUKIN"] = "1";
        }
        if ($model->Properties["documentMstDocumentSonota"] == "1") {
            $arg["SONOTA"] = "1";
        }
        if ($model->Properties["documentMstDocumentNyushi"] == "1") {
            $arg["NYUUSHI"] = "1";
        }
        $model->field["DOCUMENT_DIV"] = ($model->field["DOCUMENT_DIV"] == "") ? "1" : $model->field["DOCUMENT_DIV"];
        $radioArray = knjCreateRadio($objForm, "DOCUMENT_DIV", $model->field["DOCUMENT_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //文書種類
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjz240jQuery::getDocumentcd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["DOCUMENTCD"] === $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $model->field["DOCUMENTCD"] = ($model->field["DOCUMENTCD"] && $value_flg) ? $model->field["DOCUMENTCD"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('change');\"";
        $arg["data"]["DOCUMENTCD"] = knjCreateCombo($objForm, "DOCUMENTCD", $model->field["DOCUMENTCD"], $opt, $extra, 1);

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            if ($model->field["DOCUMENTCD"] == "") {
                $model->field["DOCUMENTCD"] = "01";
            }
            if ($model->cmd == 'edit' || $model->cmd == 'change' || $model->cmd == 'clear') {
                if ($model->cmd == 'clear') {
                    $model->field['PATTERN'] = 1;
                }
                $Row = knjz240jQuery::getRow($model);
                $db = Query::dbCheckOut();
                $query = knjz240jQuery::getList();
                $result = $db->query($query);
                $max = $model->field['PATTERN'];
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $model->field['LIST']['TEXT_' . ($row['SEQ'] - 2)] = $row['TEXT'];
                    if ($max < $row['SEQ']) {
                        $max = $row['SEQ'];
                    }
                }
                Query::dbCheckIn($db);
                $model->field['PATTERN'] = $max;
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        //文字数セット
        $model->setMojiSuu();

        //件名
        $extra = "";
        $arg["data"]["TITLE"] = knjCreateTextBox($objForm, $Row["TITLE"], "TITLE", 80, 120, $extra);

        //発行番号
        if ($model->schoolName == 'sakae') {
            $arg["sakae"] = "1";
            $extra = "";
            $arg["data"]["CERTIF_NO"] = knjCreateTextBox($objForm, $Row["CERTIF_NO"], "CERTIF_NO", 31, 30, $extra);
        }

        //本文
        $setCol = $model->text_moji * 2;
        $setRow = $model->text_gyou;
        $extra = "id=\"TEXT\"";
        $arg["data"]["TEXT"] = knjCreateTextArea($objForm, "TEXT", $setRow, $setCol, "hard", $extra, $Row["TEXT"]);

        //文字数コメント
        $arg["data"]["SETMOJI"] = "(全角".$model->text_moji."文字X".$model->text_gyou."行まで)";

        if ($model->isPattern && $model->field["DOCUMENT_DIV"] == '1' && $model->field["DOCUMENTCD"] == '01') {
            //SEQ表記
            $arg["data"]["TITLE_SEQ"] = "1";

            if (isset($model->field['PATTERN']) && $model->field['PATTERN']>1) {
                for ($i = 0; $i<$model->field['PATTERN'] - 1; $i++) {
                    //件名
                    $extra = "";
                    $arg["list"][$i]["TITLE"] = knjCreateTextBox($objForm, '', "TITLE", 80, 120, $extra);

                    //SEQ表記
                    $arg["list"][$i]["TITLE_SEQ"] = $i + 2;

                    //本文
                    $setCol = $model->text_moji * 2;
                    if ($model->text_moji > 40) {
                        $setCol++;
                    }
                    $setRow = $model->text_gyou;
                    $extra = "id=\"TEXT_" . $i . "\"";
                    $arg["list"][$i]["TEXT"] = knjCreateTextArea($objForm, "TEXT_" . $i, $setRow, $setCol, "hard", $extra, $model->field['LIST']['TEXT_' . $i]);
                    //文字数コメント
                    $arg["list"][$i]["SETMOJI"] = "(全角".$model->text_moji."文字X".$model->text_gyou."行まで)";
                    $arg["list"][$i]["MOJIKEY"] = "statusarea" . ($i + 2);

                    knjCreateHidden($objForm, "TEXT_" . $i . "_KETA", $model->text_moji * 2);
                    knjCreateHidden($objForm, "TEXT_" . $i . "_GYO", $model->text_gyou);
                    KnjCreateHidden($objForm, "TEXT_" . $i . "_STAT", "statusarea" . ($i + 2));
                }
            }
        }

        //修正ボタンを作成する
        $extra = "onclick=\"return btn_submit('insert');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        if ($model->isPattern && $model->field["DOCUMENT_DIV"] == '1' && $model->field["DOCUMENTCD"] == '01') {
            $arg["data"]["pattern_title"] = '&nbsp;&nbsp;&nbsp;パターン';
            //パターンボックスを作成する
            $extra = " onblur=\"Num_Check(this)\"";
            $arg["data"]["PATTERN"] = knjCreateTextBox($objForm, $model->field['PATTERN'], "PATTERN", 4, 4, $extra);

            //パターンボタンを作成する
            $extra = "onclick=\"return btn_submit('pattern');\"";
            $arg["button"]["btn_pattern"] = knjCreateBtn($objForm, "btn_pattern", "確 定", $extra);
        }
        //hiddenを作成する
        knjCreateHidden($objForm, "TEXT_KETA", $model->text_moji * 2);
        knjCreateHidden($objForm, "TEXT_GYO", $model->text_gyou);
        KnjCreateHidden($objForm, "TEXT_STAT", "statusarea1");
        KnjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjz240jForm1.html", $arg);
    }
}
