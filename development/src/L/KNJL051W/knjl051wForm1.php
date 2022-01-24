<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl051wForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //CSVオブジェクト作成
        $objUp = new csvFile();

        //権限チェック
        $adminFlg = knjl051wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1") {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $query = knjl051wQuery::getNameMst($model, "L003", $model->ObjYear);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);
        //CSV用
        $query = knjl051wQuery::getNameMst($model, "L003", $model->ObjYear, $model->applicantdiv);
        $csvApplicant = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //入試区分コンボボックス
        $query = knjl051wQuery::getNameMst($model, "L004", $model->ObjYear);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);
        //CSV用
        $query = knjl051wQuery::getNameMst($model, "L004", $model->ObjYear, $model->testdiv);
        $csvTestDiv = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($model->testdiv == "5" || $model->testdiv == "8") {
            //追検査の受検者のみを表示するチェックボックス
            $extra2  = " id=\"TESTDIV2\" ";
            $extra2 .= " onClick=\"return btn_submit('main');\" ";
            $extra2 .= ($model->testdiv2 == "1") ? "checked" : "";
            $arg["TOP"]["TESTDIV2"] = knjCreateCheckBox($objForm, "TESTDIV2", "1", $extra2);
        }

        //コースコンボボックス
        $query = knjl051wQuery::getCourse($model);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->examcoursecd, $extra, 1);
        //CSV用
        $query = knjl051wQuery::getCourse($model, $model->examcoursecd);
        $csvCourse = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //radio
        $opt = array(1, 2);
        $model->dataDiv = ($model->dataDiv == "") ? "1" : $model->dataDiv;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"DATA_DIV{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATA_DIV", $model->dataDiv, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //受験科目コンボボックス
        $query = knjl051wQuery::getNameMst($model, "L057", $model->ObjYear);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "KAIJI_SUBCLASSCD", $model->kaijiSubclasscd, $extra, 1, "BLANK");
        //CSV用
        $query = knjl051wQuery::getNameMst($model, "L057", $model->ObjYear, $model->kaijiSubclasscd);
        $csvKamoku = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //得点科目CD取得
        $query = knjl051wQuery::getTestSubclass($model);
        $testSubclass = $db->getOne($query);

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."年度入試_開示資料データ({$csvKamoku["CSV_NAME"]}).csv");

        //CSVヘッダ名
        $csvhead = array();
        $csvhead[] = "入試年度";
        $csvhead[] = "入試制度コード";
        $csvhead[] = "入試制度名";
        $csvhead[] = "入試区分コード";
        $csvhead[] = "入試区分名";
        $csvhead[] = "学科コース";
        $csvhead[] = "学科コース名";
        $csvhead[] = "試験科目";
        $csvhead[] = "試験科目名";
        $csvhead[] = "受験番号";
        $csvhead[] = "得点";

        $objUp->setHeader($csvhead);

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->kaijiSubclasscd != "" && $model->examcoursecd != "") {

            list ($coursecd, $majorcd, $examcourse) = preg_split('/-/', $model->examcourse);

            $query = knjl051wQuery::SelectQuery($model, "", $testSubclass);
            $result = $db->query($query);
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //書き出し用CSVデータ
                $csv = array();
                $csv[] = $model->ObjYear;
                $csv[] = $model->applicantdiv;
                $csv[] = $csvApplicant["CSV_NAME"];
                $csv[] = $model->testdiv;
                $csv[] = $csvTestDiv["CSV_NAME"];
                $csv[] = $model->examcoursecd;
                $csv[] = $csvCourse["CSV_NAME"];
                $csv[] = $model->kaijiSubclasscd;
                $csv[] = $csvKamoku["CSV_NAME"];
                $csv[] = $row["EXAMNO"];
                $csv[] = $row["SCORE"];

                $objUp->addCsvValue($csv);

                //CSV取り込み（この6つのキー値と同じレコードのみ取り込み）
                $key = array();
                $key["入試年度"]        = $model->ObjYear;
                $key["入試制度コード"]  = $model->applicantdiv;
                $key["入試区分コード"]  = $model->testdiv;
                $key["学科コース"]      = $model->examcoursecd;
                $key["試験科目"]        = $model->kaijiSubclasscd;
                $key["受験番号"]        = $row["EXAMNO"];

                //入力エリアとキーをセットする
                $objUp->setElementsValue("SCORE[]", "得点", $key);

                //ゼロ埋めフラグ
                $flg = array();
                $flg["入試年度"]        = array(false,4);
                $flg["入試制度コード"]  = array(false,1);
                $flg["入試区分コード"]  = array(false,1);
                $flg["学科コース"]      = array(false,10);
                $flg["試験科目"]        = array(false,1);
                $flg["受験番号"]        = array(true,5);

                $objUp->setEmbed_flg($flg);

                //取込むデータ(SCORE)
                $objUp->setType(array(10 => 'S'));
                $objUp->setSize(array(10 => 4));

                $model->s_receptno = $count == 0 ? $row["EXAMNO"] : $model->s_receptno;
                $model->e_receptno = $row["EXAMNO"];
                $setColor = $row["JUDGEMENT"] == "4" ? "#cccccc" : "white";

                //HIDDENに保持する用
                $arr_receptno[] = $row["EXAMNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["EXAMNO"], "perf" => (int)$row["PERFECT"]);

                //背景色
                $row["BGCOLOR"] = $setColor;
                $row["TEXT_BGCOLOR"] = ($row["JUDGEMENT"] == "4") ? "#cccccc" : "#ffffff";

                //得点テキストボックス
                $value = ($model->isWarning()) ? $model->score[$row["EXAMNO"]] : $row["SCORE"];
                $extra = " onPaste=\"return showPaste(this, {$count});\" OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:right;background-color:".$row["TEXT_BGCOLOR"]."\" onKeyDown=\"keyChangeEntToTab(this)\"";
                $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE[]", 4, 4, $extra);

                $arg["data"][] = $row;
                $count++;
            }
        }

        //人数
        knjCreateHidden($objForm, "all_count", $count);

        //受験番号セット
        knjCreateHidden($objForm, "s_receptno", $model->s_receptno);
        knjCreateHidden($objForm, "e_receptno", $model->e_receptno);

        //500件以上の場合、切換ボタンで切換
        $getBacCount  = $db->getOne(knjl051wQuery::SelectQuery($model, "BAC_COUNT"));
        $getNextCount = $db->getOne(knjl051wQuery::SelectQuery($model, "NEXT_COUNT"));
        if ($getBacCount > 0 && $count > 0) {
            $extra  = "onClick=\"btn_submit('back');\" tabindex=-1 ";
            $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        }
        if ($getNextCount > 0 && $count > 0) {
            $extra = "onClick=\"btn_submit('next');\" tabindex=-1 ";
            $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);
        }

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン作成
        //コピーボタン
        $disabled = $testSubclass ? "" : " disabled ";
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "得点をコピー", $extra.$disabled);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_receptno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_KAIJI_SUBCLASSCD");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL051W");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl051windex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl051wForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($name != "KAIJI_SUBCLASSCD" && $row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
