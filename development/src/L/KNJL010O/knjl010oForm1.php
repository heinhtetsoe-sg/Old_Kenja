<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl010oForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //オブジェクト作成
        $objUp = new csvfile();

        $db         = Query::dbCheckOut();
        $appliname  = $testname = array(); //入試制度、出願区分の名称
        $temp       = array(); //一覧表示

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl010oQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            $appliname[$row["NAMECD2"]] = $row["NAME1"];
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

        //出願区分
        $model->all_testdiv = array();
        $result = $db->query(knjl010oQuery::getTestdivMst($model->ObjYear));
        $namespare2 = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMESPARE2"] == 1 && $namespare2 == ""){
                $namespare2 = $row["NAMECD2"];
            }
            $testname[$row["NAMECD2"]] = $row["NAME1"];
            $arg["TOP"]["TESTABBV" .$row["NAMECD2"]] = $row["ABBV1"];
            $model->all_testdiv[$row["NAMECD2"]] = $row["ABBV1"];
        }

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
                                "出身学校地区コード",
                                "出身学校地区名",
                                "入試区分0",
                                "入試区分1",
                                "入試区分2",
                                "入試区分3",
                                "入試区分4",
                                "入試区分5",
                                "入試区分6"));

        //受験番号自(受験番号が変更されたら画面をクリア）
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
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
        $result = $db->query(knjl010oQuery::getName("Z002",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $sex_name[$row["NAMECD2"]] = $row["NAME2"];
            $arg["data2"][] = array("sex_cd" => $row["NAMECD2"], "sex_name" => $row["NAME2"]);
        }

        //出身地区コード
        $fs_area_cd = array();
        $result = $db->query(knjl010oQuery::getName("Z003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $fs_area_cd[$row["NAMECD2"]] = $row["NAME1"];
            $arg["data4"][] = array("fs_area_cd" => $row["NAMECD2"], "fs_area_name" => $row["NAME1"]);
        }

        //一覧表示(50行必ず作成する)
        //入力エラー時は入力された値を再表示）
        if ($model->cmd == "read2" || $model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")
        {
            for ($i = 0; $i < 50; $i++)
            {
                //受験番号を連番で作成
                $idx = sprintf("%05d",(int)$model->examno + $i);

                //終了座席番号以降は作成しない(99999以降に対応)
                if ($idx > $model->e_examno) break;

                $row["EXAMNO"]  = $idx;
                $row["BGCOLOR"] = "#ccffcc";

                //CSV取り込み（この4つのキー値と同じレコードのみ取り込み）
                $key = array("入試年度"       => $model->ObjYear,
                             "入試制度コード" => $model->applicantdiv,
                             "受験番号"       => $idx);

                //入力エリアとキーをセットする
                $objUp->setElementsValue("NAME[]",      "氏名", $key);
                $objUp->setElementsValue("NAME_KANA[]", "氏名かな", $key);
                $objUp->setElementsValue("SEX[]",       "性別コード", $key);
//                $objUp->setElementsValue("FS_CD[]",     "出身学校コード", $key);
                $objUp->setElementsValue("FS_NAME[]",   "出身学校名", $key);
                $objUp->setElementsValue("FS_AREA_CD[]","出身学校地区コード", $key);
                for ($code = 0; $code <= 6; $code++) {
                    $fieldname      = "TESTDIV" .$code;
                    $fieldnameObj   = $fieldname ."-" .$i;
                    $objUp->setElementsValue($fieldnameObj, "入試区分".$code, $key);
                }

                //ゼロ埋めフラグ
                $flg = array("入試年度"       => array(false,4),
                             "入試制度コード" => array(false,1),
                             "受験番号"       => array(true,5));

                $objUp->setEmbed_flg($flg);
                $objUp->setType(array(4 => 'S', 5 => 'S', 6 => 'N', 8 => 'N', 9 => 'S', 10 => 'N', 12 => 'N', 13 => 'N', 14 => 'N', 15 => 'N', 16 => 'N', 17 => 'N', 18 => 'N'));
                $objUp->setSize(array(4 => 24,  5 => 40,  6 => 1,   8 => 7,   9 => 45,  10 => 2,   12 => 1,   13 => 1,   14 => 1,   15 => 1,   16 => 1,   17 => 1,   18 => 1));

                $objForm->ae( array("type"        => "text",
                                    "name"        => "NAME",
                                    "extrahtml"   => "style=\"background-color:#ccffcc\" OnChange=\"Setflg(this);\"",
                                    "size"        => "17",
                                    "maxlength"   => "24",
                                    "value"       => ($model->isWarning() ? $model->field["NAME"][$i] : ""),
                                    "multiple"    => "1"));
                $row["NAME"] = $objForm->ge("NAME");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "NAME_KANA",
                                    "extrahtml"   => "style=\"background-color:#ccffcc\" OnChange=\"Setflg(this);\"",
                                    "size"        => "24",
                                    "maxlength"   => "40",
                                    "value"       => ($model->isWarning() ? $model->field["NAME_KANA"][$i] : ""),
                                    "multiple"    => "1"));
                $row["NAME_KANA"] = $objForm->ge("NAME_KANA");

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
                                    "extrahtml"   => " OnChange=\"Setflg(this);\" style=\"background-color:#ccffcc\" onblur=\"this.value=toInteger(this.value);\" disabled ",
                                    "multiple"    => "1"));
                $row["FS_CD"] = $objForm->ge("FS_CD");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "FS_NAME",
                                    "size"        => "32",
                                    "maxlength"   => "45",
                                    "extrahtml"   => " OnChange=\"Setflg(this);\" style=\"background-color:#ccffcc\"",
                                    "value"       => ($model->isWarning() ? $model->field["FS_NAME"][$i] : ""),
                                    "multiple"    => "1"));
                $row["FS_NAME"] = $objForm->ge("FS_NAME");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "FS_AREA_CD",
                                    "size"        => "2",
                                    "maxlength"   => "2",
                                    "extrahtml"   => "style=\"background-color:#ccffcc\" onblur=\"this.value=toInteger(this.value);setName(this,".(int)$idx.",'2');\"",
                                    "value"       => ($model->isWarning() ? $model->field["FS_AREA_CD"][$i] : ""),                                 
                                    "multiple"    => "1"));
                
                $row["FS_AREA_CD"] = $objForm->ge("FS_AREA_CD");

                $row["SEX_NAME"]        = ($model->isWarning() ? $sex_name[$model->field["SEX"][$i]] : "");
                $row["FS_AREA_NAME"] = ($model->isWarning() ? $fs_area_cd[$model->field["FS_AREA_CD"][$i]] : "");

                //入試区分--------------------------------------------------------------------------------
                for ($code = 0; $code <= 6; $code++) {
                    $fieldname      = "TESTDIV" .$code;
                    $fieldnameObj   = $fieldname ."-" .$i;
                    $fieldnameId    = $fieldname ."-" .$idx ."-0";
                    if ($model->isWarning()) {
                        $check = strlen($model->field[$fieldnameObj][$i]) ? "checked" : "";
                    } else {
                        $check = "";
                    }
                    $objForm->ae( array("type"        => "checkbox",
                                        "name"        => $fieldnameObj,
                                        "value"       => $code,
                                        "extrahtml"   => $check." OnChange=\"Setflg(this);\" style=\"background-color:#ccffcc;\" id=\"".$fieldnameId."\""));
                    $row[$fieldname] = $objForm->ge($fieldnameObj);
                    $row[$fieldname."_ID"] = $fieldnameId;
                    $row[$fieldname."_NAME"] = "第" .$code ."回";
                }
                //入試区分--------------------------------------------------------------------------------

                $temp[$idx] = $row;
            }    

            //データ取得
            $result    = $db->query(knjl010oQuery::SelectQuery($model));
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

                } else {
                    $visible = "";
                    //氏名・氏名かな・性別のいずれかが未入力の時はピンク色にする。
                    if (!strlen($row["NAME"]) || 
                        !strlen($row["NAME_KANA"]) || 
                        !strlen($row["SEX"])) 
                    {
                        $row["BGCOLOR"] = "#FF99CC";
                    } else {
                        $row["BGCOLOR"] = "#ffffff";
                    }

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
                                 $row["FS_AREA_CD"],
                                 $row["FS_AREA_NAME"],
                                 $row["TESTDIV0"],
                                 $row["TESTDIV1"],
                                 $row["TESTDIV2"],
                                 $row["TESTDIV3"],
                                 $row["TESTDIV4"],
                                 $row["TESTDIV5"],
                                 $row["TESTDIV6"]);
                    $objUp->addCsvValue($csv);
                }

                $objForm->ae( array("type"        => "text",
                                    "name"        => "NAME",
                                    "extrahtml"   => $visible." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" ",
                                    "size"        => "17",
                                    "maxlength"   => "24",
                                    "multiple"    => "1",
                                    "value"       => ($model->error_flg ? $model->field["NAME"][$key] : $row["NAME"])));
                $row["NAME"] = $objForm->ge("NAME");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "NAME_KANA",
                                    "extrahtml"   => $visible." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" ",
                                    "size"        => "24",
                                    "maxlength"   => "40",
                                    "multiple"    => "1",
                                    "value"       => ($model->error_flg ? $model->field["NAME_KANA"][$key] : $row["NAME_KANA"])));
                $row["NAME_KANA"] = $objForm->ge("NAME_KANA");

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
                                    "extrahtml"   => $visible." OnChange=\"Setflg(this);\" onblur=\"this.value=toInteger(this.value);\" id=\"".$row["EXAMNO"]."\" disabled ",
                                    "multiple"    => "1",
                                    "value"       => ($model->error_flg ? $model->field["FS_CD"][$key] : $row["FS_CD"])));
                $row["FS_CD"] = $objForm->ge("FS_CD");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "FS_NAME",
                                    "extrahtml"   => $visible." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" ",
                                    "size"        => "32",
                                    "maxlength"   => "45",
                                    "multiple"    => "1",
                                    "value"       => ($model->error_flg ? $model->field["FS_NAME"][$key] : $row["FS_NAME"])));
                $row["FS_NAME"] = $objForm->ge("FS_NAME");

                $objForm->ae( array("type"        => "text",
                                    "name"        => "FS_AREA_CD",
                                    "extrahtml"   => $visible." onblur=\"this.value=toInteger(this.value);setName(this,".(int)$row["EXAMNO"].",'2');\" id=\"".$row["EXAMNO"]."\" ",
                                    "size"        => "2",
                                    "maxlength"   => "2",
                                    "multiple"    => "1",
                                    "value"       => ($model->error_flg ? $model->field["FS_AREA_CD"][$key] : $row["FS_AREA_CD"])));
                $row["FS_AREA_CD"] = $objForm->ge("FS_AREA_CD");

                //入試区分--------------------------------------------------------------------------------
                for ($code = 0; $code <= 6; $code++) {
                    $fieldname      = "TESTDIV" .$code;
                    $fieldnameObj   = $fieldname ."-" .$key;
                    $fieldnameId    = $fieldname ."-" .$row["EXAMNO"] ."-1";
                    if ($model->isWarning()) {
                        $check = strlen($model->field[$fieldnameObj][$key]) ? "checked" : "";
                    } else {
                        $check = strlen($row[$fieldname]) ? "checked" : "";
                    }
                    $objForm->ae( array("type"        => "checkbox",
                                        "name"        => $fieldnameObj,
                                        "value"       => $code,
                                        "extrahtml"   => $visible.$check." OnChange=\"Setflg(this);\" id=\"".$fieldnameId."\""));
                    $row[$fieldname] = $objForm->ge($fieldnameObj);
                    $row[$fieldname."_ID"] = $fieldnameId;
                    $row[$fieldname."_NAME"] = "第" .$code ."回";
                }
                //入試区分--------------------------------------------------------------------------------

                //データがある受験番号の配列を上書きする
                $temp[$row["EXAMNO"]] = $row;
            }

            //HTML出力用に配置し直す
            foreach ($temp as $val)
            {
                //innerHTML用ID
                $val["SEX_ID"]        = "SEX_NAME".(int)$val["EXAMNO"];
                $val["FS_AREA_ID"] = "FS_AREA_NAME".(int)$val["EXAMNO"];
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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl010oindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl010oForm1.html", $arg); 
    }
}
?>
