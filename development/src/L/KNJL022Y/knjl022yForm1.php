<?php
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl022yForm1
{
    function main(&$model)
    {
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

        //入試制度
        $opt = array();
        $result = $db->query(knjl022yQuery::GetName("L003", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
            if ($model->applicantdiv == $row["NAMECD2"]) $divname["APPLICANTDIV"] = $row["NAME1"];
        }
        $extra = "Onchange=\"btn_submit('main');\" tabindex=-1";
        $arg["TOP"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->applicantdiv, $opt, $extra, 1);

        //入試区分
        $opt = array();
        $model->testdiv = ($model->applicantdiv == $model->appHold) ? $model->testdiv : "";
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $result = $db->query(knjl022yQuery::GetName($namecd1, $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
            if ($model->testdiv == $row["NAMECD2"]) $divname["TESTDIV"] = $row["NAME1"];
        }
        $extra = "Onchange=\"btn_submit('main');\" tabindex=-1";
        $arg["TOP"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->testdiv, $opt, $extra, 1);

        //受験番号自
        if ($model->cmd == "main") {
            $model->examno = $db->getOne(knjl022yQuery::getMinExam($model));
            $model->e_examno = sprintf("%05d", $model->examno + 49);
        }
        $extra = "onblur=\"this.value=toInteger(this.value);\" tabindex=-1";
        $arg["TOP"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 5, 5, $extra);

        //受験番号至
        $arg["TOP"]["END_EXAMNO"] = (strlen($model->examno) ? $model->e_examno : "     ");

        //読込みボタン
        $extra = "onClick=\"btn_submit('read');\" tabindex=-1";
        $arg["TOP"]["btn_read"] = knjCreateBtn($objForm, 'btn_read', "読込み", $extra);

        //<<ボタン
        $extra = "onClick=\"btn_submit('back');\" tabindex=-1";
        $arg["TOP"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', " << ", $extra);

        //>>ボタン
        $extra = "onClick=\"btn_submit('next');\" tabindex=-1";
        $arg["TOP"]["btn_next"] = knjCreateBtn($objForm, 'btn_next', " >> ", $extra);

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."_".$divname["APPLICANTDIV"]."_活動データ.csv");

        //CSVヘッダ名
        $csvhead = array("入試年度",
                         "入試制度コード",
                         "入試制度名",
                         "受験番号",
                         "氏名",
                         "氏名かな",
                         "性別コード",
                         "性別");

        //CSVヘッダ名
        $csvhead[] = "活動";
        $csvhead[] = "所属";
        $csvhead[] = "主な実績";
        $objUp->setHeader($csvhead);

        //一覧表示
        $disBtn = "disabled";
        $tmp_examno = array();
//        if ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")
//        {
            if (!$model->isWarning()) $model->score = array();

            $query  = knjl022yQuery::SelectQuery($model);
            $result = $db->query($query);

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $disBtn = "";
                array_walk($row, "htmlspecialchars_array");

                //書き出し用CSVデータ
                $csv = array($model->ObjYear,
                            $model->applicantdiv,
                            $divname["APPLICANTDIV"],
                            $row["EXAMNO"],
                            $row["NAME"],
                            $row["NAME_KANA"],
                            $row["SEXCD"],
                            $row["SEX"]);

                //CSV取り込み（この３つのキー値と同じレコードのみ取り込み）
                $key = array("入試年度"       => $model->ObjYear,
                             "入試制度コード" => $model->applicantdiv,
                             "受験番号"       => $row["EXAMNO"]);

                //ゼロ埋めフラグ
                $flg = array("入試年度"       => array(false,4),
                             "入試制度コード" => array(false,1),
                             "受験番号"       => array(true,5));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(8=>'S',9=>'S',10=>'S'));
                $objUp->setSize(array(8=>60,9=>60,10=>240));

                //表示される受験番号を保持
                $tmp_examno[] = $row["EXAMNO"];

                //書き出し用CSVデータ
                $csv[] = $row["ACTIVITY"];
                $csv[] = $row["SECTION"];
                $csv[] = $row["RESULTS"];
                $objUp->addCsvValue($csv);

                //活動テキストボックス
                $name  = "ACTIVITY";
                $value = $model->isWarning() ? $model->score[$row["EXAMNO"]][$name] : $row[$name];
                $extra = " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"width:100%\" ";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "maxlength"   => "60",
                                    "multiple"    => "1",
                                    "value"       => $value ));
                $row[$name] = $objForm->ge($name);
                $objUp->setElementsValue($name."[]","活動", $key);

                //所属テキストボックス
                $name  = "SECTION";
                $value = $model->isWarning() ? $model->score[$row["EXAMNO"]][$name] : $row[$name];
                $extra = " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"width:100%\" ";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "maxlength"   => "60",
                                    "multiple"    => "1",
                                    "value"       => $value ));
                $row[$name] = $objForm->ge($name);
                $objUp->setElementsValue($name."[]","所属", $key);

                //主な実績テキストボックス
                $name  = "RESULTS";
                $value = $model->isWarning() ? $model->score[$row["EXAMNO"]][$name] : $row[$name];
                $extra = " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"width:100%\" ";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "maxlength"   => "240",
                                    "multiple"    => "1",
                                    "value"       => $value ));
                $row[$name] = $objForm->ge($name);
                $objUp->setElementsValue($name."[]","主な実績", $key);

                $arg["data"][] = $row;
            }
//        }

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン作成
        $extra = $disBtn ." onClick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        $extra = "onClick=\"btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //hidden作成
        makeHidden($objForm, $tmp_examno, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl022yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl022yForm1.html", $arg); 
    }
}

//hidden作成
function makeHidden(&$objForm, $tmp_examno, $model) {
    knjCreateHidden($objForm, "cmd", "");
    knjCreateHidden($objForm, "HID_APPLICANTDIV", ""); //年度データ
    knjCreateHidden($objForm, "HID_TESTDIV", "");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$tmp_examno));

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
