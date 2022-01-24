<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl040jForm1
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

        //入試区分
        $opt = array();
        $result = $db->query(knjl040jQuery::GetName("L004",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv == "" && $row["NAMESPARE2"] == "1") $model->testdiv = $row["NAMECD2"];
            if ($model->testdiv == $row["NAMECD2"]) $divname["TESTDIV"] = $row["NAME1"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\" tabindex=-1",
                            "value"      => $model->testdiv,
                            "options"    => $opt));
        $arg["TOP"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //受験型
        $testdiv = (int)$model->testdiv;
        $opt_type = array();
        $result = $db->query(knjl040jQuery::GetName("L005",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (($row["NAMECD2"] == "1" && in_array($testdiv, array(1,2,3,4,6))) || 
                ($row["NAMECD2"] == "2" && in_array($testdiv, array(1,3,4,6))) || 
                ($row["NAMECD2"] == "3" && in_array($testdiv, array(5))))
            {
                $opt_type[] = array("label" => $row["NAMECD2"].":".$row["NAME1"], "value" => $row["NAMECD2"]);
                if ($model->exam_type == "") $model->exam_type = $row["NAMECD2"];
                if ($model->exam_type == $row["NAMECD2"]) $divname["EXAM_TYPE"] = $row["NAME1"];
            }
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "EXAM_TYPE",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\" tabindex=-1",
                            "value"      => $model->exam_type,
                            "options"    => $opt_type));
        $arg["TOP"]["EXAM_TYPE"] = $objForm->ge("EXAM_TYPE");

        //受験番号自
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\" tabindex=-1",
                            "value"       => $model->examno));

        //受験番号至
        $arg["TOP"]["END_EXAMNO"] = (strlen($model->examno) ? $model->e_examno : "     ");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_read",
                            "value"       => "読込み",
                            "extrahtml"   => "onClick=\"btn_submit('read');\" tabindex=-1" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => "onClick=\"btn_submit('back');\" tabindex=-1" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => "onClick=\"btn_submit('next');\" tabindex=-1" ) );

        $arg["TOP"]["EXAMNO"] = $objForm->ge("EXAMNO");
        $arg["TOP"]["button"] = $objForm->ge("btn_read")."　　".$objForm->ge("btn_back").$objForm->ge("btn_next");

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."入試_受付データ.csv");

        //一覧表示
        $tmp_examno = array();
        if ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")
        {
            if (!$model->isWarning()) $model->score = array();

            $result    = $db->query(knjl040jQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303");
            }

            $model->data=array();
            $cnt=0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //表示される受験番号を保持
                $tmp_examno[] = $row["EXAMNO"];

                $model->data["EXAMNO"][] = $row["EXAMNO"];
                $model->data["RECEPTNO"][] = $row["RECEPTNO"];
                $model->data["EXAM_TYPE"][] = $row["EXAM_TYPE"];
                $model->data["APPLICANTDIV"][] = $row["APPLICANTDIV"];

                //書き出し用CSVデータ
                $csv = array($model->ObjYear,
                             $model->testdiv,
                             $divname["TESTDIV"],
                             $row["EXAMNO"],
                             $row["RECEPTNO"],
                             $row["NAME"],
                             $row["NAME_KANA"],
                             $row["SEX"],
                             $row["SEX_NAME"],
                             $row["EXAM_TYPE"],
                             $row["EXAM_TYPE_NAME"],
                             $row["APPLICANTDIV"],
                             $row["APPLICANTDIV_NAME"]);

                //書き出し用CSVデータ
                $objUp->addCsvValue($csv);

                //CSV取り込み（この３つのキー値と同じレコードのみ取り込み）
                $key = array("※入試年度"       => $model->ObjYear,
                             "※入試区分コード" => $model->testdiv,
                             "※受験番号"       => $row["EXAMNO"]);

                //入力エリアとキーをセットする
                $objUp->setElementsValue("RECEPTNO[]", "座席番号", $key);

                //ゼロ埋めフラグ
                $flg = array("※入試年度"       => array(false,4),
                             "※入試区分コード" => array(false,1),
                             "※受験番号"       => array(true,5),
                             "座席番号"       => array(true,4));

                $objUp->setEmbed_flg($flg);
                $objUp->setHeader(array("※入試年度",
                                        "※入試区分コード",
                                        "入試区分名",
                                        "※受験番号",
                                        "座席番号",
                                        "氏名",
                                        "氏名かな",
                                        "性別コード",
                                        "性別",
                                        "受験型",
                                        "受験型名",
                                        "入試制度コード",
                                        "入試制度名"));
                $objUp->setType(array(0 => 'N',1 => 'N',3 => 'N',4 => 'N'));
                $objUp->setSize(array(0 => 4,1 => 1,3 => 5,4 => 4));

                $objForm->ae( array("type"        => "text",
                                    "name"        => "RECEPTNO",
                                    "extrahtml"   => " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" onblur=\"this.value = toInteger(this.value);\"",
                                    "maxlength"   => "4",
                                    "size"        => "4",
                                    "multiple"    => "1",
                                    "value"       => ($model->isWarning() ? $model->receptno[$cnt] : $row["RECEPTNO"])));
                $row["RECEPTNO"] = $objForm->ge("RECEPTNO");

                $arg["data"][] = $row;
                $cnt++;
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
                            "name"      => "HID_TESTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_EXAM_TYPE",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_EXAMNO",
                            "value"     => implode(",",$tmp_examno)) );
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl040jindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl040jForm1.html", $arg); 
    }
}
?>
