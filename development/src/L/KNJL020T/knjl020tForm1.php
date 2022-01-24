<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfilealp.php");

class knjl020tForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //CSVオブジェクト作成
        $objUp = new csvFileAlp();

        //DB接続
        $db = Query::dbCheckOut();

        //CSV書き出し時のコード名称をセット
        $divname = array();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl020tQuery::GetName("L003", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
            if ($model->applicantdiv == $row["NAMECD2"]) $divname["APPLICANTDIV"] = $row["NAME1"];
        }
        $extra = "Onchange=\"btn_submit('main');\" tabindex=-1";
        $arg["TOP"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->applicantdiv, $opt, $extra, 1);

        //受検番号自
        if ($model->cmd == "main") {
            $model->examno = $db->getOne(knjl020tQuery::getMinExam($model->applicantdiv));
            $model->e_examno = sprintf("%05d", $model->examno + 49);
        }
        $extra = "onblur=\"this.value=toInteger(this.value);\" tabindex=-1";
        $arg["TOP"]["EXAMNO"] = knjCreateTextBox($objForm, preg_replace('/^0*/', '', $model->examno), "EXAMNO", 5, 5, $extra);

        //受検番号至
        $arg["TOP"]["END_EXAMNO"] = (strlen($model->examno) ? preg_replace('/^0*/', '', $model->e_examno) : "     ");

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
                        "受検番号",
                        "氏名",
                        "氏名かな",
                        "性別コード",
                        "性別");

        //内申科目名称
        $result = $db->query(knjl020tQuery::GetName("L008", $model->ObjYear));
        
        $model->rpt3 = $model->rpt5 = $model->editable_rpt = array();
        
        for ($i = 1; $i < 11; $i++)
        {
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $arg["head"][] = $row["NAME1"];
            //CSVヘッダ名
            $csvhead[]     = (strlen($row["NAME1"]) ? $row["NAME1"] : "教科".sprintf("%02d",$i));
            //３教科名称
            if ($row["NAMESPARE3"] == "1"){
                $model->rpt3[] = "RPT".$row["NAMECD2"];         //予備3に1立っている教科は3教科扱いになる！
            }
            //５教科名称
            if ($row["NAMESPARE1"] == "1"){
                $model->rpt5[] = "RPT".$row["NAMECD2"];         //予備1に1立っている教科は5教科扱いになる！
            }
            //編集教科名称
            if ($row["NAMESPARE2"] == "1"){
                $model->editable_rpt[] = "RPT".$row["NAMECD2"]; //予備2に1立っている教科は編集可能(テキストボックスを表示する)
            }
        }

        //CSVヘッダ名
        $csvhead[] = "３Ｔ";
        $csvhead[] = "５Ｔ";
        $csvhead[] = "９Ｔ";
        $csvhead[] = "平均";
        $csvhead[] = "欠席日数（１年）";
        $csvhead[] = "欠席日数（２年）";
        $csvhead[] = "欠席日数（３年）";
        $objUp->setHeader($csvhead);

        //一覧表示
        $tmp_examno = array();
        if ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")
        {
            if (!$model->isWarning()) $model->score = array();

            $query  = knjl020tQuery::SelectQuery($model);
            $result = $db->query($query);

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //頭０を非表示
                $row["EXAMNO"] = (int) $row["EXAMNO"];

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
                             "受検番号"       => $row["EXAMNO"]);

                //ゼロ埋めフラグ
                $flg = array("入試年度"       => array(false,4),
                             "入試制度コード" => array(false,1),
                             "受検番号"       => array(false,5));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(8=>'N',9=>'N',10=>'N',11=>'N',12=>'N',13=>'N',14=>'N',15=>'N',16=>'N',17=>'N',22=>'N',23=>'N',24=>'N'));
                $objUp->setSize(array(8=>3,9=>3,10=>3,11=>3,12=>3,13=>3,14=>3,15=>3,16=>3,17=>3,22=>3,23=>3,24=>3));

                //表示される受検番号を保持
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
                $csv[] = $row["TOTAL3"];
                $csv[] = $row["TOTAL5"];
                $csv[] = $row["TOTAL_ALL"];
                $csv[] = $row["AVERAGE_ALL"];
                $csv[] = $row["ABSENCE_DAYS"];
                $csv[] = $row["ABSENCE_DAYS2"];
                $csv[] = $row["ABSENCE_DAYS3"];
                $objUp->addCsvValue($csv);

                //欠席日数テキストボックス
                $item = array('１年', '２年', '３年');
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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl020tindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl020tForm1.html", $arg); 
    }
}

//hidden作成
function makeHidden(&$objForm, $tmp_examno) {
    knjCreateHidden($objForm, "cmd", "");
    knjCreateHidden($objForm, "HID_APPLICANTDIV", ""); //年度データ
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$tmp_examno));
}
?>
