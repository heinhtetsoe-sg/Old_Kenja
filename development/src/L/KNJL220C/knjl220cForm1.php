<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl220cForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //オブジェクト作成
        $objUp = new csvFile();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボ
        $query = knjl220cQuery::getApplicantdiv($model);
        $extra = "onchange=\"btn_submit('main');\"  tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //プレテスト区分コンボ
        $query = knjl220cQuery::getPreTestdiv($model);
        $extra = "onchange=\"btn_submit('main');\"  tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "PRE_TESTDIV", $model->preTestdiv, $extra, 1, "");

        //受験科目コンボ
        $query = knjl220cQuery::getTestsubclasscd($model);
        $extra = "onchange=\"btn_submit('main');\"  tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."プレテスト_得点データ.csv");

        //CSVヘッダ名
        $objUp->setHeader(array("入試年度",
                                "入試制度コード",
                                "入試制度名",
                                "プレテスト区分コード",
                                "プレテスト区分名",
                                "受験科目",
                                "受験科目名",
                                "受験番号",
                                "得点"));

        //一覧表示
        $arr_receptno = array();
        $dataCnt = 0;
        $colorFlg = false;
        if ($model->testsubclasscd != "")
        {
            //データ取得
            $result    = $db->query(knjl220cQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303","");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //color
                if ($dataCnt % 5 == 0) {
                    $colorFlg = !$colorFlg;
                }

                //書き出し用CSVデータ
                $csv = array($model->ObjYear,
                             $model->applicantdiv,
                             $row["APPLICANT_NAME"],
                             $model->preTestdiv,
                             $row["PRE_TESTDIV_NAME"],
                             $model->testsubclasscd,
                             $row["TESTSUBCLASS_NAME"],
                             $row["PRE_RECEPTNO"],
                             $row["SCORE"]);
                $objUp->addCsvValue($csv);

                //CSV取り込み（この6つのキー値と同じレコードのみ取り込み）
                $key = array("入試年度"             => $model->ObjYear,
                             "入試制度コード"       => $model->applicantdiv,
                             "プレテスト区分コード" => $model->preTestdiv,
                             "受験科目"             => $model->testsubclasscd,
                             "受験番号"             => $row["PRE_RECEPTNO"]);

                //入力エリアとキーをセットする
                $objUp->setElementsValue("SCORE[]", "得点", $key);

                //ゼロ埋めフラグ
                $flg = array("入試年度"             => array(false,4),
                             "入試制度コード"       => array(false,1),
                             "プレテスト区分コード" => array(false,1),
                             "受験科目"             => array(false,1),
                             "受験番号"             => array(true,4));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(8 => 'N'));
                $objUp->setSize(array(8 => 3));

                //HIDDENに保持する用
                $arr_receptno[] = $row["PRE_RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["PRE_RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                $objForm->ae( array("type"        => "text",
                                    "name"        => "SCORE",
                                    "extrahtml"   => "onchange=\"Setflg(this);\" id=\"".$row["PRE_RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\"",
                                    "maxlength"   => "3",
                                    "size"        => "3",
                                    "multiple"    => "1",
                                    "value"       => ($model->isWarning() ? $model->score[$row["PRE_RECEPTNO"]] : $row["SCORE"])));
                $row["SCORE"] = $objForm->ge("SCORE");

                //bgcolor
                $row["BGCOLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

                $arg["data"][] = $row;
                $dataCnt++;
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onClick=\"btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onClick=\"btn_submit('reset');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_update");
        $arg["btn_reset"]  = $objForm->ge("btn_reset");
        $arg["btn_end"]    = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_RECEPTNO",
                            "value"     => implode(",",$arr_receptno)) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_APPLICANTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_PRE_TESTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTSUBCLASSCD",
                            "value"     => "") );
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl220cindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl220cForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $value_flg = false;
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
