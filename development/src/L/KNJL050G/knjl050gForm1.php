<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl050gForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //CSVオブジェクト作成
        $objUp = new csvFile();

        //DB接続
        $db = Query::dbCheckOut();

        //CSV書き出し時のコード名称をセット
        $divname = array();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl050gQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl050gQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //専併区分
        $arg["isKeiai"] = $model->isKeiai;
        knjCreateHidden($objForm, "isKeiai", $model->isKeiai);
        $arg["isKasiwara"] = $model->isKasiwara;
        if ($model->isKasiwara == "1") {
            $model->exam_type = "1";
        } else {
            $query = knjl050gQuery::getNameMst($model->ObjYear, "L006");
            $extra = "onChange=\"return btn_submit('main')\" tabindex=-1";
            makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->exam_type, $extra, 1);
        }

        //試験科目コンボボックス
        $extra = "onchange=\"return btn_submit('test');\" tabindex=-1";
        $query = knjl050gQuery::getTestSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");

        //初期化
        if ($model->cmd == "main") {
            $model->examhallcd = "";
        } elseif ($model->cmd == "hall") {
        }

        //会場コンボボックス
        $extra = "onchange=\"return btn_submit('hall');\" tabindex=-1";
        $query = knjl050gQuery::getEntexamHallYdat($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "BLANK");

        //会場の開始受付番号・終了受付番号取得・・・帳票パラメータ
        $query = knjl050gQuery::getEntexamHallYdat($model, $model->examhallcd);
        $hall = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "S_RECEPTNO", $hall["S_RECEPTNO"]);
        knjCreateHidden($objForm, "E_RECEPTNO", $hall["E_RECEPTNO"]);

        //特別措置者(インフルエンザ)
        $extra  = "id=\"SPECIAL_REASON_DIV\" ";
        $extra .= strlen($model->special_reason_div) ? "checked" : "";
        $arg["TOP"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //CSV出力ファイル名
        //試験科目名
        $query = knjl050gQuery::getTestSubclasscd($model, $model->testsubclasscd);
        $titleKamoku = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $objUp->setFileName($model->ObjYear."年度入試_得点データ(".$titleKamoku["CSV_NAME"]."_".$hall["CSV_NAME"].").csv");

        //CSVヘッダ名
        $csvhead = array();
        $csvhead[] = "入試年度";
        $csvhead[] = "入試制度コード";
        $csvhead[] = "入試制度名";
        $csvhead[] = "入試区分コード";
        $csvhead[] = "入試区分名";
        $csvhead[] = "試験科目";
        $csvhead[] = "試験科目名";
        $csvhead[] = "受験番号";
        $csvhead[] = "得点";

        $objUp->setHeader($csvhead);

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "" && $model->examhallcd != "") {
            //データ取得
            $query = knjl050gQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                if ($row["ATTEND_FLG"] === '0') {
                    $row["SCORE"] = '*';
                }

                //書き出し用CSVデータ
                $csv = array();
                $csv[] = $model->ObjYear;
                $csv[] = $model->applicantdiv;
                $csv[] = $row["APPLICANTDIV_NAME"];
                $csv[] = $model->testdiv;
                $csv[] = $row["TESTDIV_NAME"];
                $csv[] = $model->testsubclasscd;
                $csv[] = $row["TESTSUBCLASSCD_NAME"];
                $csv[] = $row["RECEPTNO"];
                $csv[] = $row["SCORE"];

                $objUp->addCsvValue($csv);

                //CSV取り込み（この6つのキー値と同じレコードのみ取り込み）
                $key = array();
                $key["入試年度"]        = $model->ObjYear;
                $key["入試制度コード"]  = $model->applicantdiv;
                $key["入試区分コード"]  = $model->testdiv;
                $key["試験科目"]        = $model->testsubclasscd;
                $key["受験番号"]        = $row["RECEPTNO"];

                //入力エリアとキーをセットする
                $objUp->setElementsValue("SCORE[]", "得点", $key);

                //ゼロ埋めフラグ
                $flg = array();
                $flg["入試年度"]        = array(false,4);
                $flg["入試制度コード"]  = array(false,1);
                $flg["入試区分コード"]  = array(false,1);
                $flg["試験科目"]        = array(false,1);
                $flg["受験番号"]        = array(true,4);

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(8 => 'S'));
                $objUp->setSize(array(8 => 3));

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                //得点テキストボックス
                $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]] : $row["SCORE"];
                if ($row["ATTEND_FLG"] == '0') {
                    $value = '*';
                }    //*の表示
                $row["BGCOLOR"] = $row["JUDGEMENT"] === "4" ? "pink" : "#ffffff";
                $extra = " OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;background-color:".$row["BGCOLOR"]."\" onblur=\"CheckScore(this);\" onKeyDown=\"goEnter(this);\"";
                $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE[]", 3, 3, $extra);

                $arg["data"][] = $row;
            }
        }

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $arr_receptno);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050gindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl050gForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $arr_receptno)
{
    $disable  = (0 < get_count($arr_receptno)) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //csvボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "全件ＣＳＶ出力", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAM_TYPE");
    knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");
    knjCreateHidden($objForm, "HID_ISCALL050G", "TRUE");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL312G");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
}
