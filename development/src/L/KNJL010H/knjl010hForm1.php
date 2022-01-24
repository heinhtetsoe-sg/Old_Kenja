<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl010hForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //オブジェクト作成
        $objUp = new csvFile();

        $db         = Query::dbCheckOut();
        $appliname  = $testname = array(); //入試制度、入試区分の名称
        $temp       = array(); //一覧表示

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl010hQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            $appliname[$row["NAMECD2"]] = $row["NAME1"];
            if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
        }

        $disabled = $model->isWarning() ? "disabled" : "";
        $arg["change_flg"] = $model->isWarning() ? "true" : "false";

        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLICANTDIV",
                            "size"       => "1",
                            "extrahtml"  => $disabled." Onchange=\"btn_submit('main');\" tabindex=-1",
                            "value"      => $model->applicantdiv,
                            "options"    => $opt));
                            
        $arg["TOP"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        //CSV出力ファイル名
        $objUp->setFileName($model->ObjYear."入試_".$appliname[$model->applicantdiv].".csv");

        //CSVヘッダ名
        $objUp->setHeader(array("入試年度",
                                "入試制度コード",
                                "入試制度名",
                                "受験番号",
                                "氏名",
                                "氏名かな",
                                "性別コード",
                                "性別",
                                "出身学校コード",
                                "出身学校名",
                                "国公私立コード",
                                "国公私立名",
                                "入試区分1",
                                "入試区分2",
                                "入試区分3",
                                "推薦受験番号"));

        //受験番号自(受験番号が変更されたら画面をクリア）
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\" tabindex=-1",
                            "value"       => $model->examno));

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
        
        //性別、出願コース名
        //（JAVASCRIPTで変更時にラベル表示する用。読込み時の出願コースおよび名称は初期値は先頭レコード
        $sex_name = array();
        $result = $db->query(knjl010hQuery::getName("Z002",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $sex_name[$row["NAMECD2"]] = $row["NAME2"];
            $arg["data2"][] = array("sex_cd" => $row["NAMECD2"], "sex_name" => $row["NAME2"]);
        }

        //国公私立
        $fs_natpubpridiv = array();
        $result = $db->query(knjl010hQuery::getName("L015",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $fs_natpubpridiv[$row["NAMECD2"]] = $row["NAME1"];
            $arg["data4"][] = array("fs_natpubpridiv" => $row["NAMECD2"], "fs_natpub_name" => $row["NAME1"]);
        }

        //出身学校
        $cd2 = ($model->applicantdiv == "1") ? "01" : "02";
        $fs_cd = array();
        $result = $db->query(knjl010hQuery::getFsName($model->ObjYear, $cd2));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $fs_cd[$row["FINSCHOOLCD"]] = $row["FINSCHOOL_NAME"];
            $arg["data5"][] = array("fs_cd" => $row["FINSCHOOLCD"], "fs_name" => $row["FINSCHOOL_NAME"]);
        }

        //一覧表示(50行必ず作成する)
        //入力エラー時は入力された値を再表示）
        if ($model->cmd == "read2" || $model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")
        {
            for ($i = 0; $i < 50; $i++)
            {
                //受験番号を連番で作成
                $idx = sprintf("%04d",(int)$model->examno + $i);

                //終了座席番号以降は作成しない(9999以降に対応)
                if ($idx > $model->e_examno) break;

                $row["EXAMNO"]  = $idx;
                $row["BGCOLOR"] = "#ccffcc";

                //CSV取り込み（この4つのキー値と同じレコードのみ取り込み）
                $key = array("入試年度"       => $model->ObjYear,
                             "入試制度コード" => $model->applicantdiv,
                             "受験番号"       => $idx);

                $name     = "NAME" .$i;
                $nameKana = "NAME_KANA" .$i;
                //入力エリアとキーをセットする
                $objUp->setElementsValue($name,      "氏名", $key);
                $objUp->setElementsValue($nameKana, "氏名かな", $key);
                $objUp->setElementsValue("SEX[]",       "性別コード", $key);
                $objUp->setElementsValue("FS_CD[]",     "出身学校コード", $key);
                $objUp->setElementsValue("FS_NATPUBPRIDIV[]",     "国公私立コード", $key);
                $objUp->setElementsValue("TESTDIV1"."-".$i, "入試区分1", $key);
                $objUp->setElementsValue("TESTDIV2"."-".$i, "入試区分2", $key);
                $objUp->setElementsValue("TESTDIV3"."-".$i, "入試区分3", $key);
                $objUp->setElementsValue("RECOM_EXAMNO[]", "推薦受験番号", $key);

                //ゼロ埋めフラグ
                $flg = array("入試年度"       => array(false,4),
                             "入試制度コード" => array(false,1),
                             "受験番号"       => array(true,4));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(4 => 'S', 5 => 'S', 6 => 'N', 8 => 'N', 10 => 'N', 12 => 'N', 13 => 'N', 14 => 'N', 15 => 'N'));
                $objUp->setSize(array(4 => 60, 5 => 120, 6=>1, 8=>7, 10 => 1, 12 => 1, 13 => 1, 14 => 1, 15 => 4));

                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => "style=\"background-color:#ccffcc\" OnChange=\"Setflg(this)\" onkeyup=\" keySet('".$name."', '".$nameKana."', 'H')\"",
                                    "size"        => "15",
                                    "maxlength"   => "24",
                                    "value"       => ($model->isWarning() ? $model->field["NAME"][$i] : "")));
                $row["NAME"] = $objForm->ge($name);

                $objForm->ae( array("type"        => "text",
                                    "name"        => $nameKana,
                                    "extrahtml"   => "style=\"background-color:#ccffcc\" OnChange=\"Setflg(this);\"",
                                    "size"        => "20",
                                    "maxlength"   => "40",
                                    "value"       => ($model->isWarning() ? $model->field["NAME_KANA"][$i] : "")));
                $row["NAME_KANA"] = $objForm->ge($nameKana);

                $objForm->ae( array("type"        => "text",
                                    "name"        => "SEX",
                                    "extrahtml"   => "style=\"background-color:#ccffcc\" onblur=\"this.value=toInteger(this.value);setName(this,".(int)$idx.",'0');\"",
                                    "size"        => "1",
                                    "maxlength"   => "1",
                                    "value"       => ($model->isWarning() ? $model->field["SEX"][$i] : ""),
                                    "multiple"    => "1"));
                $row["SEX"] = $objForm->ge("SEX");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "FS_CD",
                                    "size"        => "7",
                                    "maxlength"   => "7",
                                    "value"       => ($model->isWarning() ? $model->field["FS_CD"][$i] : ""),
                                    "extrahtml"   => " OnChange=\"Setflg(this);\" style=\"background-color:#ccffcc\" onblur=\"this.value=toInteger(this.value);setName(this,".(int)$idx.",'5');\"",
                                    "multiple"    => "1"));
                $row["FS_CD"] = $objForm->ge("FS_CD");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "FS_NATPUBPRIDIV",
                                    "size"        => "1",
                                    "maxlength"   => "1",
                                    "extrahtml"   => "style=\"background-color:#ccffcc\" onblur=\"this.value=toInteger(this.value);setName(this,".(int)$idx.",'2');\"",
                                    "value"       => ($model->isWarning() ? $model->field["FS_NATPUBPRIDIV"][$i] : ""),                                 
                                    "multiple"    => "1"));
                
                $row["FS_NATPUBPRIDIV"] = $objForm->ge("FS_NATPUBPRIDIV");


                if ($model->isWarning()) {
                    $checked1 = strlen($model->field["TESTDIV1"."-".$i][$i]) ? "checked" : "";
                    $checked2 = strlen($model->field["TESTDIV2"."-".$i][$i]) ? "checked" : "";
                    $checked3 = strlen($model->field["TESTDIV3"."-".$i][$i]) ? "checked" : "";
                } else {
                    $checked1 = "";
                    $checked2 = "";
                    $checked3 = "";
                }
                if ($model->applicantdiv == "2" || $model->applicantdiv == "3" || $model->applicantdiv == "4") $checked1 = "checked";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => "TESTDIV1"."-".$i,
                                    "value"       => "1",
                                    "extrahtml"   => $checked1." OnChange=\"Setflg(this);\" style=\"background-color:#ccffcc;\" id=\""."TESTDIV1"."-".$idx."-0"."\""));
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => "TESTDIV2"."-".$i,
                                    "value"       => "1",
                                    "extrahtml"   => $checked2." OnChange=\"Setflg(this);\" style=\"background-color:#ccffcc;\" id=\""."TESTDIV2"."-".$idx."-0"."\""));
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => "TESTDIV3"."-".$i,
                                    "value"       => "1",
                                    "extrahtml"   => $checked3." OnChange=\"Setflg(this);\" style=\"background-color:#ccffcc;\" id=\""."TESTDIV3"."-".$idx."-0"."\""));
                $row["TESTDIV1"] = $objForm->ge("TESTDIV1"."-".$i);
                $row["TESTDIV2"] = $objForm->ge("TESTDIV2"."-".$i);
                $row["TESTDIV3"] = $objForm->ge("TESTDIV3"."-".$i);
                $row["TESTDIV1_ID"] = "TESTDIV1"."-".$idx."-0";
                $row["TESTDIV2_ID"] = "TESTDIV2"."-".$idx."-0";
                $row["TESTDIV3_ID"] = "TESTDIV3"."-".$idx."-0";

                $row["SEX_NAME"]        = ($model->isWarning() ? $sex_name[$model->field["SEX"][$i]] : "");
                $row["FS_NATPUB_NAME"] = ($model->isWarning() ? $fs_natpubpridiv[$model->field["FS_NATPUBPRIDIV"][$i]] : "");
                $row["FS_NAME"] = ($model->isWarning() ? $fs_cd[$model->field["FS_CD"][$i]] : "");

                //推薦受験番号
                $objForm->ae( array("type"        => "text",
                                    "name"        => "RECOM_EXAMNO",
                                    "extrahtml"   => "style=\"background-color:#ccffcc\" onblur=\"this.value=toInteger(this.value);\"",
                                    "size"        => "4",
                                    "maxlength"   => "4",
                                    "value"       => ($model->isWarning() ? $model->field["RECOM_EXAMNO"][$i] : ""),
                                    "multiple"    => "1"));
                $row["RECOM_EXAMNO"] = $objForm->ge("RECOM_EXAMNO");

                $temp[$idx] = $row;
            }    

            //データ取得
            $result    = $db->query(knjl010hQuery::SelectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $key = (int)$row["EXAMNO"] - (int)$model->examno;

                //入試制度が違う場合は一行非表示
                if ($row["APPLICANTDIV"] != $model->applicantdiv) 
                {
                    $visible = "STYLE=\"Display: None\";";
                    $row["BGCOLOR"]         = "#FF99CC";
                    $row["NAME"]            = "";
                    $row["NAME_KANA"]       = "";
                    $row["SEX"]             = "";
                    $row["SEX_NAME"]        = "";
                    $model->setMessage("\\n指定した受験番号は既に違う入試制度で登録されています。\\n入試制度および受験番号を確認して下さい");

                } else {
                    $visible = "";
                    $row["BGCOLOR"] = "#ffffff";

                    //書き出し用CSVデータ
                    $csv = array($model->ObjYear,
                                 $model->applicantdiv,
                                 $appliname[$model->applicantdiv],
                                 $row["EXAMNO"],
                                 $row["NAME"],
                                 $row["NAME_KANA"],
                                 $row["SEX"],
                                 $row["SEX_NAME"],
                                 $row["FS_CD"],
                                 $row["FS_NAME"],
                                 $row["FS_NATPUBPRIDIV"],
                                 $row["FS_NATPUB_NAME"],
                                 $row["TESTDIV1"],
                                 $row["TESTDIV2"],
                                 $row["TESTDIV3"],
                                 $row["RECOM_EXAMNO"]);
                    $objUp->addCsvValue($csv);

                }

                $name     = "NAME" .$key;
                $nameKana = "NAME_KANA" .$key;
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $visible." OnChange=\"Setflg(this)\" id=\"".$row["EXAMNO"]."\" onkeyup=\" keySet('".$name."', '".$nameKana."', 'H')\"",
                                    "size"        => "15",
                                    "maxlength"   => "24",
                                    "value"       => ($model->error_flg ? $model->field["NAME"][$key] : $row["NAME"])));
                $row["NAME"] = $objForm->ge($name);

                $objForm->ae( array("type"        => "text",
                                    "name"        => $nameKana,
                                    "extrahtml"   => $visible." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" ",
                                    "size"        => "20",
                                    "maxlength"   => "40",
                                    "value"       => ($model->error_flg ? $model->field["NAME_KANA"][$key] : $row["NAME_KANA"])));
                $row["NAME_KANA"] = $objForm->ge($nameKana);

                $objForm->ae( array("type"        => "text",
                                    "name"        => "SEX",
                                    "extrahtml"   => $visible." onblur=\"this.value=toInteger(this.value);setName(this,".(int)$row["EXAMNO"].",'0');\" id=\"".$row["EXAMNO"]."\" ",
                                    "size"        => "1",
                                    "maxlength"   => "1",
                                    "multiple"    => "1",
                                    "value"       => ($model->error_flg ? $model->field["SEX"][$key] : $row["SEX"])));
                $row["SEX"] = $objForm->ge("SEX");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "FS_CD",
                                    "size"        => "7",
                                    "maxlength"   => "7",
                                    "extrahtml"   => $visible." OnChange=\"Setflg(this);\" onblur=\"this.value=toInteger(this.value);setName(this,".(int)$row["EXAMNO"].",'5');\" id=\"".$row["EXAMNO"]."\" ",
                                    "multiple"    => "1",
                                    "value"       => ($model->error_flg ? $model->field["FS_CD"][$key] : $row["FS_CD"])));
                $row["FS_CD"] = $objForm->ge("FS_CD");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "FS_NATPUBPRIDIV",
                                    "extrahtml"   => $visible." onblur=\"this.value=toInteger(this.value);setName(this,".(int)$row["EXAMNO"].",'2');\" id=\"".$row["EXAMNO"]."\" ",
                                    "size"        => "1",
                                    "maxlength"   => "1",
                                    "multiple"    => "1",
                                    "value"       => ($model->error_flg ? $model->field["FS_NATPUBPRIDIV"][$key] : $row["FS_NATPUBPRIDIV"])));
                $row["FS_NATPUBPRIDIV"] = $objForm->ge("FS_NATPUBPRIDIV");


                if ($model->isWarning()) {
                    $checked1 = strlen($model->field["TESTDIV1"."-".$key][$key]) ? " checked" : "";
                    $checked2 = strlen($model->field["TESTDIV2"."-".$key][$key]) ? " checked" : "";
                    $checked3 = strlen($model->field["TESTDIV3"."-".$key][$key]) ? " checked" : "";
                } else {
                    $checked1 = strlen($row["TESTDIV1"]) ? " checked" : "";
                    $checked2 = strlen($row["TESTDIV2"]) ? " checked" : "";
                    $checked3 = strlen($row["TESTDIV3"]) ? " checked" : "";
                }
                if ($model->applicantdiv == "2" || $model->applicantdiv == "3" || $model->applicantdiv == "4") $checked1 = "checked";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => "TESTDIV1"."-".$key,
                                    "value"       => "1",
                                    "extrahtml"   => $visible.$checked1." OnChange=\"Setflg(this);\" id=\""."TESTDIV1"."-".$row["EXAMNO"]."-1"."\" "));
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => "TESTDIV2"."-".$key,
                                    "value"       => "1",
                                    "extrahtml"   => $visible.$checked2." OnChange=\"Setflg(this);\" id=\""."TESTDIV2"."-".$row["EXAMNO"]."-1"."\" "));
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => "TESTDIV3"."-".$key,
                                    "value"       => "1",
                                    "extrahtml"   => $visible.$checked3." OnChange=\"Setflg(this);\" id=\""."TESTDIV3"."-".$row["EXAMNO"]."-1"."\" "));
                $row["TESTDIV1"] = $objForm->ge("TESTDIV1"."-".$key);
                $row["TESTDIV2"] = $objForm->ge("TESTDIV2"."-".$key);
                $row["TESTDIV3"] = $objForm->ge("TESTDIV3"."-".$key);
                $row["TESTDIV1_ID"] = "TESTDIV1"."-".$row["EXAMNO"]."-1";
                $row["TESTDIV2_ID"] = "TESTDIV2"."-".$row["EXAMNO"]."-1";
                $row["TESTDIV3_ID"] = "TESTDIV3"."-".$row["EXAMNO"]."-1";

                //推薦受験番号
                $objForm->ae( array("type"        => "text",
                                    "name"        => "RECOM_EXAMNO",
                                    "extrahtml"   => $visible." onblur=\"this.value=toInteger(this.value);\" id=\"".$row["EXAMNO"]."\" ",
                                    "size"        => "4",
                                    "maxlength"   => "4",
                                    "multiple"    => "1",
                                    "value"       => ($model->error_flg ? $model->field["RECOM_EXAMNO"][$key] : $row["RECOM_EXAMNO"])));
                $row["RECOM_EXAMNO"] = $objForm->ge("RECOM_EXAMNO");

                //データがある受験番号の配列を上書きする
                $temp[$row["EXAMNO"]] = $row;
            }

            //HTML出力用に配置し直す
            foreach ($temp as $val)
            {
                //innerHTML用ID
                $val["SEX_ID"]        = "SEX_NAME".(int)$val["EXAMNO"];
                $val["FS_NATPUB_ID"] = "FS_NATPUB_NAME".(int)$val["EXAMNO"];
                $val["FS_NAME_ID"] = "FS_NAME".(int)$val["EXAMNO"];
                $arg["data"][] = $val;
            }
        } 

        //受験番号至
        $arg["TOP"]["END_EXAMNO"] = (strlen($model->examno) ? $model->e_examno : "     ");

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
                            "name"      => "cmd",
                            "value"     => $model->cmd) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_APPLICANTDIV",
                            "value"     => $model->applicantdiv) );
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl010hindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl010hForm1.html", $arg); 
    }
}
?>
