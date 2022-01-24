<?php
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl050yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //CSVオブジェクト作成
        $objUp = new csvFile();

        //DB接続
        $db           = Query::dbCheckOut();

        //CSV書き出し時のコード名称をセット
        $divname = array();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl050yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $model->testdiv = ($model->applicantdiv == $model->appHold) ? $model->testdiv : "";
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $namecd2 = ($model->applicantdiv == "1") ? "1" : "2"; //推薦入試は表示しない
        $query = knjl050yQuery::getNameMst($namecd1, $model->ObjYear, $namecd2);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //受験科目コンボボックス
        $query = knjl050yQuery::getTestSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1);

        //会場コンボボックス
        $query = knjl050yQuery::getHallName($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "BLANK");

        //計算テキストボックス表示フラグ・・・中学の「2:算数」の時、入力する
        $isKeisan = false;
        if ($model->applicantdiv == "1" && $model->testsubclasscd == "2") {
            $isKeisan = true;
            $arg["keisan"] = "on";
        }




        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."入試_得点データ.csv");

        //CSVヘッダ名
        $csvhead = array("入試年度",
                         "入試制度コード",
                         "入試制度名",
                         "入試区分コード",
                         "入試区分名",
                         "受験科目",
                         "受験科目名",
                         "座席番号",
                         "受験番号",
                         "得点");
        if ($isKeisan) $csvhead[] = "計算";

        $objUp->setHeader($csvhead);

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "" && $model->examhallcd != "")
        {
            //データ取得
            $result    = $db->query(knjl050yQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
                $model->setMessage("MSG303","\\n満点マスタの設定を確認して下さい。");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //書き出し用CSVデータ
                $csv = array($model->ObjYear,
                            $model->applicantdiv,
                            $row["APPLICANTDIV_NAME"],
                            $model->testdiv,
                            $row["TESTDIV_NAME"],
                            $model->testsubclasscd,
                            $row["TESTSUBCLASSCD_NAME"],
                            $row["RECEPTNO"],
                            $row["EXAMNO"],
                            $row["SCORE"]);
                if ($isKeisan) $csv[] = $row["SCORE3"];
                $objUp->addCsvValue($csv);

                //CSV取り込み（この6つのキー値と同じレコードのみ取り込み）
                $key = array("入試年度"       => $model->ObjYear,
                             "入試制度コード" => $model->applicantdiv,
                             "入試区分コード" => $model->testdiv,
                             "受験科目"       => $model->testsubclasscd,
                             "座席番号"       => $row["RECEPTNO"],
                             "受験番号"       => $row["EXAMNO"]);

                //入力エリアとキーをセットする
                $objUp->setElementsValue("SCORE[]", "得点", $key);
                if ($isKeisan) $objUp->setElementsValue("SCORE3[]", "計算", $key);

                //ゼロ埋めフラグ
                $flg = array("入試年度"       => array(false,4),
                             "入試制度コード" => array(false,1),
                             "入試区分コード" => array(false,1),
                             "受験科目"       => array(false,1),
                             "座席番号"       => array(true,5),
                             "受験番号"       => array(true,5));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(9 => 'N', 10 => 'N'));
                $objUp->setSize(array(9 => 3, 10 => 3));

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                //得点テキストボックス
                $name  = "SCORE";
                $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]] : $row[$name];
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\"";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "maxlength"   => "3",
                                    "size"        => "3",
                                    "multiple"    => "1",
                                    "value"       => $value));
                $row[$name] = $objForm->ge($name);

                //計算テキストボックス・・・中学の「2:算数」の時、入力する
                if ($isKeisan) {
                    $name  = "SCORE3";
                    $value = ($model->isWarning()) ? $model->score3[$row["RECEPTNO"]] : $row[$name];
                    $extra = "OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\"";
                    $objForm->ae( array("type"        => "text",
                                        "name"        => $name,
                                        "extrahtml"   => $extra,
                                        "maxlength"   => "3",
                                        "size"        => "3",
                                        "multiple"    => "1",
                                        "value"       => $value));
                    $row[$name] = $objForm->ge($name);
                }

                $arg["data"][] = $row;
            }
        }

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);





        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl050yForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL050Y");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
