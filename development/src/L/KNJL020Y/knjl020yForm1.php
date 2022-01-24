<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl020yForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //CSVオブジェクト作成
        $objUp = new csvfile();

        //DB接続
        $db = Query::dbCheckOut();

        //CSV書き出し時のコード名称をセット
        $divname = array();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl020yQuery::GetName("L003", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
            if ($model->applicantdiv == $row["NAMECD2"]) $divname["APPLICANTDIV"] = $row["NAME1"];
        }
        $extra = "Onchange=\"btn_submit('main');\" tabindex=-1";
        $arg["TOP"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->applicantdiv, $opt, $extra, 1);

        //受験番号自
        if ($model->cmd == "main") {
            $model->examno = $db->getOne(knjl020yQuery::getMinExam($model));
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
        $objUp->setFileName($model->ObjYear."_".$divname["APPLICANTDIV"]."_内申データ.csv");

        //CSVヘッダ名
        $csvhead = array("入試年度",
                        "入試制度コード",
                        "入試制度名",
                        "受験番号",
                        "氏名",
                        "氏名かな",
                        "性別コード",
                        "性別");

        //内申科目名称
        $result = $db->query(knjl020yQuery::GetName("L008", $model->ObjYear));
        
        $model->editable_rpt = array();
        
        for ($i = 1; $i < 11; $i++)
        {
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $arg["head"][] = $row["NAME1"];
            //CSVヘッダ名
            $csvhead[]     = (strlen($row["NAME1"]) ? $row["NAME1"] : "教科".sprintf("%02d",$i));
            //編集教科名称
            if ($row["NAMESPARE2"] == "1"){
                $model->editable_rpt[] = "RPT".$row["NAMECD2"]; //予備2に1立っている教科は編集可能(テキストボックスを表示する)
            }
        }

        //CSVヘッダ名
        $csvhead[] = "評定合計";
        $csvhead[] = "評定平均";
        $item = ($model->applicantdiv == "2") ? array('１年', '２年', '３年') : array('１学期', '２学期', '３学期');
        for ($i = 1; $i < 4; $i++) {
            $csvhead[] = "欠席日数（".$item[$i-1]."）";
            $arg["TOP"]["ABSENCE_DAYS_NAME".$i] = $item[$i-1];
        }
        $objUp->setHeader($csvhead);

        //一覧表示
        $tmp_examno = array();
        if ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")
        {
            if (!$model->isWarning()) $model->score = array();

            $query  = knjl020yQuery::SelectQuery($model);
            $result = $db->query($query);

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
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
                $objUp->setType(array(8=>'N',9=>'N',10=>'N',11=>'N',12=>'N',13=>'N',14=>'N',15=>'N',16=>'N',17=>'N',18=>'N',19=>'N',20=>'N',21=>'N',22=>'N'));
                $objUp->setSize(array(8=>3,9=>3,10=>3,11=>3,12=>3,13=>3,14=>3,15=>3,16=>3,17=>3,18=>3,19=>3,20=>3,21=>3,22=>3));

                //表示される受験番号を保持
                $tmp_examno[] = $row["EXAMNO"];

                //内申教科テキストボックス
                for ($i = 1; $i < 11; $i++)
                {
                    $idx = sprintf("%02d",$i);

                    //編集可能科目はCSV取り込可
                    if (in_array("RPT".$idx, $model->editable_rpt)) {

                        $disabled = "";
                        //入力エリアとキーをセットする
                        $model->colname["RPT".$idx] = (strlen($arg["head"][((int)$idx-1)]) ? $arg["head"][((int)$idx-1)] : "教科".$idx);
                        $objUp->setElementsValue("RPT".$idx."[]", $model->colname["RPT".$idx], $key);

                    } else {
                        $disabled = "disabled";
                        //更新時用に先にセット
                        $model->score[$row["EXAMNO"]]["RPT".$idx] = $row["CONFIDENTIAL_RPT".$idx];
                    }

                    $objForm->ae( array("type"        => "text",
                                        "name"        => "RPT".$idx,
                                        "extrahtml"   => $disabled." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:right;\" onblur=\"this.value = toInteger(this.value);\"",
                                        "maxlength"   => "3",
                                        "size"        => "2",
                                        "multiple"    => "1",
                                        "value"       => ($model->isWarning() ? $model->score[$row["EXAMNO"]]["RPT".$idx] : $row["CONFIDENTIAL_RPT".$idx])));
                    $row["RPT".$idx] = $objForm->ge("RPT".$idx);

                    //書き出し用CSVデータ
                    $csv[] = $row["CONFIDENTIAL_RPT".$idx];
                }

                //書き出し用CSVデータ
                $csv[] = $row["AVERAGE_ALL"];
                $csv[] = $row["AVERAGE5"];
                $csv[] = $row["ABSENCE_DAYS"];
                $csv[] = $row["ABSENCE_DAYS2"];
                $csv[] = $row["ABSENCE_DAYS3"];
                $objUp->addCsvValue($csv);

                //評定合計テキストボックス
                $objForm->ae( array("type"        => "text",
                                    "name"        => "AVERAGE_ALL",
                                    "extrahtml"   => " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:right;\" onblur=\"this.value = toInteger(this.value);\"",
                                    "maxlength"   => "3",
                                    "size"        => "3",
                                    "multiple"    => "1",
                                    "value"       => ($model->isWarning() ? $model->score[$row["EXAMNO"]]["AVERAGE_ALL"] : $row["AVERAGE_ALL"])));
                $row["AVERAGE_ALL"] = $objForm->ge("AVERAGE_ALL");
                $objUp->setElementsValue("AVERAGE_ALL[]","評定合計", $key);

                //評定平均テキストボックス
                $disAvg = "disabled";
                //更新時用に先にセット
                $model->score[$row["EXAMNO"]]["AVERAGE5"] = $row["AVERAGE5"];
                $objForm->ae( array("type"        => "text",
                                    "name"        => "AVERAGE5",
                                    "extrahtml"   => $disAvg." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:right;\" onblur=\"this.value = toFloat(this.value);\"",
                                    "maxlength"   => "3",
                                    "size"        => "3",
                                    "multiple"    => "1",
                                    "value"       => ($model->isWarning() ? $model->score[$row["EXAMNO"]]["AVERAGE5"] : $row["AVERAGE5"])));
                $row["AVERAGE5"] = $objForm->ge("AVERAGE5");
                //$objUp->setElementsValue("AVERAGE5[]","評定平均", $key);

                //欠席日数テキストボックス
                for ($i = 1; $i < 4; $i++)
                {
                    $idx = ($i == 1) ? "" : $i;
                    $objForm->ae( array("type"        => "text",
                                        "name"        => "ABSENCE_DAYS".$idx,
                                        "extrahtml"   => " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:right;\" onblur=\"this.value = toInteger(this.value);\"",
                                        "maxlength"   => "3",
                                        "size"        => "3",
                                        "multiple"    => "1",
                                        "value"       => ($model->isWarning() ? $model->score[$row["EXAMNO"]]["ABSENCE_DAYS".$idx] : $row["ABSENCE_DAYS".$idx])));
                    $row["ABSENCE_DAYS".$idx] = $objForm->ge("ABSENCE_DAYS".$idx);
                    $objUp->setElementsValue("ABSENCE_DAYS".$idx."[]","欠席日数（".$item[$i-1]."）", $key);
                }
                $arg["data"][] = $row;
            }
        }

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン作成
        $extra = "onClick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        $extra = "onClick=\"btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //hidden作成
        makeHidden($objForm, $tmp_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl020yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl020yForm1.html", $arg); 
    }
}

//hidden作成
function makeHidden(&$objForm, $tmp_examno) {
    knjCreateHidden($objForm, "cmd", "");
    knjCreateHidden($objForm, "HID_APPLICANTDIV", ""); //年度データ
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$tmp_examno));
}
?>
