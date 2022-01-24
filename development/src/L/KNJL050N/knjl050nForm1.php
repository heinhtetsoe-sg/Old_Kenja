<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl050nForm1
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
        $query = knjl050nQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $query = knjl050nQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //専併区分コンボボックス
        $query = knjl050nQuery::getNameMst("L006", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->shdiv, $extra, 1);

        //志望区分コンボボックス
        $query = knjl050nQuery::getExamcourse($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSE", $model->examcourse, $extra, 1, "BLANK");

        //受験科目コンボボックス
        $query = knjl050nQuery::getTestSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");

        //CSV出力ファイル名
        //科目名
        $query = knjl050nQuery::getTestSubclasscd($model, $model->testsubclasscd);
        $titleKamoku = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //志望区分名
        $query = knjl050nQuery::getExamcourse($model, $model->examcourse);
        $titleHall = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $objUp->setFileName($model->ObjYear."入試_得点データ(".$titleKamoku["CSV_NAME"]."_".$titleHall["CSV_NAME"].").csv");

        //CSVヘッダ名
        $csvhead = array("入試年度",
                         "入試制度コード",
                         "入試制度名",
                         "入試区分コード",
                         "入試区分名",
                         "受験科目",
                         "受験科目名",
                         "受験番号",
                         "得点");

        $objUp->setHeader($csvhead);

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "" && $model->examcourse != "" && $model->shdiv != "") {
            //データ取得
            $result = $db->query(knjl050nQuery::SelectQuery($model, ""));

            if ($result->numRows() == 0 ){
                $model->setMessage("MSG303");
            }
            $count = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $model->s_receptno = $count == 0 ? $row["RECEPTNO"] : $model->s_receptno;
                $model->e_receptno = $row["RECEPTNO"];
               //書き出し用CSVデータ
                if ($row["ATTEND_FLG"] === '0') $row["SCORE"] = '*';
                $csv = array($model->ObjYear,
                            $model->applicantdiv,
                            $row["APPLICANTDIV_NAME"],
                            $model->testdiv,
                            $row["TESTDIV_NAME"],
                            $model->testsubclasscd,
                            $row["TESTSUBCLASSCD_NAME"],
                            $row["RECEPTNO"],
                            $row["SCORE"]);
                $objUp->addCsvValue($csv);

                //CSV取り込み（この6つのキー値と同じレコードのみ取り込み）
                $key = array("入試年度"       => $model->ObjYear,
                             "入試制度コード" => $model->applicantdiv,
                             "入試区分コード" => $model->testdiv,
                             "受験科目"       => $model->testsubclasscd,
                             "受験番号"       => $row["RECEPTNO"]);

                //入力エリアとキーをセットする
                $objUp->setElementsValue("SCORE[]", "得点", $key);

                //ゼロ埋めフラグ
                $flg = array("入試年度"       => array(false,4),
                             "入試制度コード" => array(false,1),
                             "入試区分コード" => array(false,1),
                             "受験科目"       => array(false,1),
                             "受験番号"       => array(false,5));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(8 => 'S'));
                $objUp->setSize(array(8 => 3));

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                //得点テキストボックス
                $name  = "SCORE";
                $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]] : $row[$name];
                if ($row["ATTEND_FLG"] == '0') $value = '*';//*の表示
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\"  onKeyDown=\"keyChangeEntToTab(this)\"";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "maxlength"   => "3",
                                    "size"        => "3",
                                    "multiple"    => "1",
                                    "value"       => $value));
                $row[$name] = $objForm->ge($name);

                $arg["data"][] = $row;
                $count++;
            }
        }
        //受験番号セット
        knjCreateHidden($objForm, "s_receptno", $model->s_receptno);
        knjCreateHidden($objForm, "e_receptno", $model->e_receptno);
        //500件以上の場合、切換ボタンで切換
        $getBacCount = $db->getOne(knjl050nQuery::SelectQuery($model, "BAC_COUNT"));
        $getNextCount = $db->getOne(knjl050nQuery::SelectQuery($model, "NEXT_COUNT"));
        if ($getBacCount > 0) {
            $extra = "onClick=\"btn_submit('back');\" tabindex=-1";
            $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        }
        if ($getNextCount > 0) {
            $extra = "onClick=\"btn_submit('next');\" tabindex=-1";
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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050nindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl050nForm1.html", $arg);
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
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
    knjCreateHidden($objForm, "HID_EXAMCOURSE");
    knjCreateHidden($objForm, "HID_SHDIV");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL050N");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
