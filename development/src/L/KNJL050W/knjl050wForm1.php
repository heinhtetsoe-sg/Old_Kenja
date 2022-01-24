<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl050wForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //CSVオブジェクト作成
        $objUp = new csvFile();

        //権限チェック
        $adminFlg = knjl050wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1") {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl050wQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);
        //CSV用
        $query = knjl050wQuery::getNameMst("L003", $model->ObjYear, $model->applicantdiv);
        $csvApplicant = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //入試区分コンボボックス
        $query = knjl050wQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);
        //CSV用
        $query = knjl050wQuery::getNameMst("L004", $model->ObjYear, $model->testdiv);
        $csvTestDiv = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($model->testdiv == "5" || $model->testdiv == "8") {
            //追検査の受検者のみを表示するチェックボックス
            $extra2  = " id=\"TESTDIV2\" ";
            $extra2 .= " onClick=\"return btn_submit('main');\" ";
            $extra2 .= ($model->testdiv2 == "1") ? "checked" : "";
            $arg["TOP"]["TESTDIV2"] = knjCreateCheckBox($objForm, "TESTDIV2", "1", $extra2);
        }

        //コースコンボボックス
        $query = knjl050wQuery::getCourse($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->examcoursecd, $extra, 1);
        //CSV用
        $query = knjl050wQuery::getCourse($model, $model->examcoursecd);
        $csvCourse = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //受験科目コンボボックス
        $query = knjl050wQuery::getNameMst("L009", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");
        //CSV用
        $query = knjl050wQuery::getNameMst("L009", $model->ObjYear, $model->testsubclasscd);
        $csvKamoku = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."年度入試_得点データ({$csvKamoku["CSV_NAME"]}).csv");

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
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "" && $model->examcoursecd != "") {

            list ($coursecd, $majorcd, $examcourse) = preg_split('/-/', $model->examcourse);

            //データ取得
            $result = $db->query(knjl050wQuery::SelectQuery($model, ""));
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
                $csv[] = $model->testsubclasscd;
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
                $key["試験科目"]        = $model->testsubclasscd;
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
                $objUp->setSize(array(10 => 3));

                $model->s_receptno = $count == 0 ? $row["EXAMNO"] : $model->s_receptno;
                $model->e_receptno = $row["EXAMNO"];
                $setColor = $row["JUDGEMENT"] == "4" ? "#cccccc" : "white";

                if ($row["ATTEND_FLG"] === '0' || $row["JUDGEMENT"] == "4") $row["SCORE"] = '*';

                //HIDDENに保持する用
                $arr_receptno[] = $row["EXAMNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["EXAMNO"], "perf" => (int)$row["PERFECT"]);

                //背景色
                $row["BGCOLOR"] = $setColor;
                $row["TEXT_BGCOLOR"] = ($row["JUDGEMENT"] == "4") ? "#cccccc" : "#ffffff";

                //欠席者は表示する。但し、入力不可とする。
                $disJdg = ($row["JUDGEMENT"] == "4") ? " readOnly" : "";

                //得点テキストボックス
                $value = ($model->isWarning()) ? $model->score[$row["EXAMNO"]] : $row["SCORE"];
                $extra = " onPaste=\"return showPaste(this, {$count});\" OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:right;background-color:".$row["TEXT_BGCOLOR"]."\" onblur=\"CheckScore(this);\" onKeyDown=\"keyChangeEntToTab(this)\"" .$disJdg;
                $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE[]", 3, 3, $extra);

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
        $getBacCount  = $db->getOne(knjl050wQuery::SelectQuery($model, "BAC_COUNT"));
        $getNextCount = $db->getOne(knjl050wQuery::SelectQuery($model, "NEXT_COUNT"));
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
        knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL050W");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050windex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl050wForm1.html", $arg);
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

        if ($row["NAMESPARE2"] && $default_flg){
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
