<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl050cForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //オブジェクト作成
        $objUp = new csvFile();
        $db           = Query::dbCheckOut();
        $divname = array();  //CSV書き出し時のコード名称をセット
        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl050cQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
            if ($model->applicantdiv == $row["NAMECD2"]) $divname["APPLICANTDIV"] = $row["NAME1"];
        }

        if (!strlen($model->applicantdiv)) {
            $model->applicantdiv = $opt[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLICANTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->applicantdiv,
                            "options"    => $opt));
        $arg["TOP"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        //入試区分
        $opt = array();
        $result = $db->query(knjl050cQuery::GetName("L004",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]    = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
            if ($model->testdiv == $row["NAMECD2"]) $divname["TESTDIV"] = $row["NAME1"];
        }

        if (!strlen($model->testdiv)) {
            $model->testdiv = $opt[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->testdiv,
                            "options"    => $opt));
        $arg["TOP"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //受験科目コードを配列にセット
        $testsubclass = array();
        $result = $db->query(knjl050cQuery::getTestSubclasscd($model->ObjYear, $model->applicantdiv, $model->testdiv));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $testsubclass[] = $row["TESTSUBCLASSCD"];
        }

        //受験科目
        $opt = $opt_select_subclass_div = array();
        $result = $db->query(knjl050cQuery::GetName("L009",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (in_array($row["NAMECD2"], $testsubclass)) {
                $opt_select_subclass_div[$row["NAMECD2"]] = $row["NAMESPARE1"];
                $opt[]    = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
                if (!strlen($model->testsubclasscd) || !in_array($model->testsubclasscd, $testsubclass)) {
                    $model->testsubclasscd = $row["NAMECD2"];
                }
                if ($model->testsubclasscd == $row["NAMECD2"]) $divname["TESTSUBCLASSCD"] = $row["NAME1"];
            }
        }
        $model->select_subclass_div = $opt_select_subclass_div[$model->testsubclasscd];

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTSUBCLASSCD",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->testsubclasscd,
                            "options"    => $opt));
        $arg["TOP"]["TESTSUBCLASSCD"] = $objForm->ge("TESTSUBCLASSCD");

        //会場コンボ
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $result = $db->query(knjl050cQuery::getHallName($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["EXAMHALLCD"]."：".$row["EXAMHALL_NAME"]."（".$row["S_RECEPTNO"]."～".$row["E_RECEPTNO"]."）",
                           "value" => $row["EXAMHALLCD"]);
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "EXAMHALLCD",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->examhallcd,
                            "options"    => $opt));
        $arg["TOP"]["EXAMHALLCD"] = $objForm->ge("EXAMHALLCD");

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."入試_得点データ.csv");

        //CSVヘッダ名
        $objUp->setHeader(array("入試年度",
                                "入試制度コード",
                                "入試制度名",
                                "入試区分コード",
                                "入試区分名",
                                "受験科目",
                                "受験科目名",
                                "座席番号",
                                "受験番号",
                                "得点"));


        //一覧表示
        $arr_receptno = array();
        if ($model->examhallcd != "")
        {
            //データ取得
            $result    = $db->query(knjl050cQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303","\\n座席番号登録が行われていないか、志願者数確定処理が行われていません。");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                 array_walk($row, "htmlspecialchars_array");

                //書き出し用CSVデータ
                $csv = array($model->ObjYear,
                            $model->applicantdiv,
                            $divname["APPLICANTDIV"],
                            $model->testdiv,
                            $divname["TESTDIV"],
                            $model->testsubclasscd,
                            $divname["TESTSUBCLASSCD"],
                            $row["RECEPTNO"],
                            $row["EXAMNO"],
                            $row["SCORE"]);
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

                //ゼロ埋めフラグ
               $flg = array("入試年度"       => array(false,4),
                            "入試制度コード" => array(false,1),
                            "入試区分コード" => array(false,1),
                            "受験科目"       => array(false,1),
                            "座席番号"       => array(true,4),
                            "受験番号"       => array(true,4));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(9 => 'N'));
                $objUp->setSize(array(9 => 3));

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                $objForm->ae( array("type"        => "text",
                                    "name"        => "SCORE",
                                    "extrahtml"   => "  OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\"",
                                    "maxlength"   => "3",
                                    "size"        => "3",
                                    "multiple"    => "1",
                                    "value"       => ($model->isWarning() ? $model->score[$row["RECEPTNO"]] : $row["SCORE"])));
                $row["SCORE"] = $objForm->ge("SCORE");

                $arg["data"][] = $row;
            }
        }

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
                            "name"      => "HID_TESTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTSUBCLASSCD",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_EXAMHALLCD",
                            "value"     => "") );
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050cindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl050cForm1.html", $arg); 
    }
}
?>
