<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl250yForm1
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
        $query = knjl250yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $query = knjl250yQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //グループコンボボックス
        $query = knjl250yQuery::getHallName($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "BLANK");

        //受験科目を配列にセット
        $model->subcdArray = array();
        $query = knjl250yQuery::getTestSubclasscd($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //４科目表示 1:言葉 2:パズル 3:生活 4:かず
            if ($row["TESTSUBCLASSCD"] > "4") continue;

            $model->subcdArray[] = array("TESTSUBCLASSNAME" => $row["TESTSUBCLASSNAME"],
                                         "TESTSUBCLASSCD"   => $row["TESTSUBCLASSCD"],
                                         "PERFECT"          => $row["PERFECT"],
                                         "RATE"             => $row["RATE"]
                                        );
            //ヘッダ受験科目を表示
            $arg["TESTSUBCLASSNAME".$row["TESTSUBCLASSCD"]] = $row["TESTSUBCLASSNAME"];
        }
        $result->free();


        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."入試_得点データ.csv");

        //CSVヘッダ名
        $csvhead = array("※入試年度",
                         "※入試制度コード",
                         "入試制度名",
                         "※入試区分コード",
                         "入試区分名",
                         "※受験番号",
                         "氏名");
        foreach ($model->subcdArray as $key => $sub) {
            $csvhead[] = $sub["TESTSUBCLASSNAME"];
        }
        $objUp->setHeader($csvhead);

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->examhallcd != "" && 0 < get_count($model->subcdArray)) {
            //データ取得
            $result    = $db->query(knjl250yQuery::SelectQuery($model));

            if ($result->numRows() == 0 ) {
//                $model->setMessage("MSG303","\\n満点マスタの設定を確認して下さい。");
                $model->setMessage("MSG303","");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                //書き出し用CSVデータ
                $csv = array($model->ObjYear,
                            $model->applicantdiv,
                            $row["APPLICANTDIV_NAME"],
                            $model->testdiv,
                            $row["TESTDIV_NAME"],
                            $row["RECEPTNO"],
                            $row["NAME"]);
                foreach ($model->subcdArray as $key => $sub) {
                    if ($row["ATTEND_FLG".$sub["TESTSUBCLASSCD"]] === '0') $row["SCORE".$sub["TESTSUBCLASSCD"]] = '*';
                    $csv[] = $row["SCORE".$sub["TESTSUBCLASSCD"]];
                }
                $objUp->addCsvValue($csv);
                //CSV取り込み（この4つのキー値と同じレコードのみ取り込み）
                $csvkey = array("※入試年度"       => $model->ObjYear,
                             "※入試制度コード" => $model->applicantdiv,
                             "※入試区分コード" => $model->testdiv,
                             "※受験番号"       => $row["RECEPTNO"]);
                //入力エリアとキーをセットする
                foreach ($model->subcdArray as $key => $sub) {
                    $objName        = "SCORE".$sub["TESTSUBCLASSCD"]."[]";
                    $csvheadName    = $sub["TESTSUBCLASSNAME"];
                    $objUp->setElementsValue($objName, $csvheadName, $csvkey);
                }
                //ゼロ埋めフラグ
                $flg = array("※入試年度"       => array(false,4),
                             "※入試制度コード" => array(false,1),
                             "※入試区分コード" => array(false,1),
                             "※受験番号"       => array(true,5));
                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(7 => 'S', 8 => 'S', 9 => 'S', 10 => 'S'));
                $objUp->setSize(array(7 => 3,   8 => 3,   9 => 3,   10 => 3));

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //得点テキストボックス
                foreach ($model->subcdArray as $key => $sub) {
                    $name  = "SCORE".$sub["TESTSUBCLASSCD"];
                    $perfect = $sub["PERFECT"];
                    $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]][$name] : $row[$name];
                    $extra = "OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this, {$perfect});\"";
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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl250yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl250yForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
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
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL250Y");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
