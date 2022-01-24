<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl550fForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

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
        $query = knjl550fQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //レイアウト切替
        if ($model->applicantdiv == "1") {
            $arg["applicantdiv1"] = 1;
        } else {
            $arg["applicantdiv2"] = 1;
        }

        //入試区分コンボボックス
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjl550fQuery::getNameMst($namecd1, $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //中学のみ(IEE判定)
        $model->ieeFlg = false;
        if ($model->applicantdiv == "1") {
            $query = knjl550fQuery::getNameMstL024($model->ObjYear, $model->testdiv);
            $abbv2 = $db->getOne($query);
            $model->ieeFlg = ($abbv2 == '2') ? true: false;
        }
        $arg["BIKOU"] = ($model->ieeFlg) ? "備考<font style=\"font-size:10pt\">(全角で25文字)</font>": "";

        //中学の入試区分「9:コース別思考力」の場合
        if ($model->applicantdiv == "1" && (in_array($model->testdiv, array("9", "10", "11")))) {
            //受験型コンボ「6:グローバル」「7:サイエンス」「8:スポーツ」
            $query = knjl550fQuery::getExamType($model);
            makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->exam_type, $extra, 1);
        }

        //受験科目コンボボックス
        $query = knjl550fQuery::getTestSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");

        //管理番号FromToテキストボックス
        $extra = " onBlur=\"return ExamNoVisible(this); \"";
        $arg["TOP"]["RECEPTNO_FROM"] = knjCreateTextBox($objForm, $model->receptNoFrom, "RECEPTNO_FROM", 10, 10, $extra);
        $arg["TOP"]["RECEPTNO_TO"] = knjCreateTextBox($objForm, $model->receptNoFrom, "RECEPTNO_TO", 10, 10, $extra);

        //CSV出力ファイル名
        //科目名
        $query = knjl550fQuery::getTestSubclasscd($model, $model->testsubclasscd);
        $titleKamoku = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $objUp->setFileName($model->ObjYear."年度入試_得点データ(".$titleKamoku["CSV_NAME"].").csv");

        //CSVヘッダ名
        $csvhead = array();
        $csvhead[] = "入試年度";
        $csvhead[] = "入試制度コード";
        $csvhead[] = "入試制度名";
        $csvhead[] = "入試区分コード";
        $csvhead[] = "入試区分名";
        $csvhead[] = "受験科目";
        $csvhead[] = "受験科目名";
        $csvhead[] = "受験番号";
        $csvhead[] = "得点";

        $objUp->setHeader($csvhead);

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "") {

            //データ取得
            $result = $db->query(knjl550fQuery::SelectQuery($model, ""));

            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $model->s_receptno = $count == 0 ? $row["RECEPTNO"] : $model->s_receptno;
                $model->e_receptno = $row["RECEPTNO"];

                if ($row["ATTEND_FLG"] === '0') $row["SCORE"] = '*';

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
                $key["受験科目"]        = $model->testsubclasscd;
                $key["受験番号"]        = $row["RECEPTNO"];

                //入力エリアとキーをセットする
                $objUp->setElementsValue("SCORE[]", "得点", $key);

                //ゼロ埋めフラグ
                $flg = array();
                $flg["入試年度"]        = array(false,4);
                $flg["入試制度コード"]  = array(false,1);
                $flg["入試区分コード"]  = array(false,1);
                $flg["受験科目"] = array(false,1);
                $flg["受験番号"] = array(false,4);

                $objUp->setEmbed_flg($flg);

                //得点の位置
                $score_row = 8;

                $objUp->setType(array($score_row => 'S'));
                $objUp->setSize(array($score_row => 3));

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"].'-'.$row["EXAMNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                //得点テキストボックス
                $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]] : $row["SCORE"];
                if ($row["ATTEND_FLG"] == '0') $value = '*';    //*の表示
                $extra = " OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\" onKeyDown=\"keyChangeEntToTab(this)\"";
                $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE[]", 3, 3, $extra);

                if ($model->applicantdiv == "1" && $model->ieeFlg) {
                    //備考
                    $value = ($model->isWarning()) ? $model->remark2[$row["RECEPTNO"].'-'.$row["EXAMNO"]] : $row["INTERVIEW_REMARK2"];
                    $extra = " OnChange=\"Setflg2(this, '{$row["RECEPTNO"]}');\"";
                    $row["EXAM_TYPE_NAME"] = knjCreateTextBox($objForm, $value, "INTERVIEW_REMARK2[]", 51, 75, $extra);
                }

                $arg["data"][] = $row;
                $count++;
            }
        }

        //管理番号セット
        knjCreateHidden($objForm, "s_receptno", $model->s_receptno);
        knjCreateHidden($objForm, "e_receptno", $model->e_receptno);

        //500件以上の場合、切換ボタンで切換
        $getBacCount  = $db->getOne(knjl550fQuery::SelectQuery($model, "BAC_COUNT"));
        $getNextCount = $db->getOne(knjl550fQuery::SelectQuery($model, "NEXT_COUNT"));
        if ($getBacCount > 0) {
            $extra  = "onClick=\"btn_submit('back');\" tabindex=-1 ";
            $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        }
        if ($getNextCount > 0) {
            $extra = "onClick=\"btn_submit('next');\" tabindex=-1 ";
            $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);
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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl550findex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl550fForm1.html", $arg);
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV0");
    knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
    knjCreateHidden($objForm, "HID_RECEPTNO_FROM");
    knjCreateHidden($objForm, "HID_RECEPTNO_TO");
    knjCreateHidden($objForm, "HID_EXAMCOURSE");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL550F");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
