<?php

require_once('for_php7.php');


class knjl385jForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $db           = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //クラス指定ラジオ　1：仮クラス　2：クラス
        $arrClassDiv   = array();
        $arrClassDiv[] = 1;
        $arrClassDiv[] = 2;

        if (!$model->classDiv) $model->classDiv = 1;

        $objForm->ae( array("type"       => "radio",
                            "name"       => "CLASSDIV",
                            "extrahtml"  => "onclick=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->classDiv,
                            "options"    => $arrClassDiv));

        $arg["TOP"]["CLASSDIV1"] = $objForm->ge("CLASSDIV",1);
        $arg["TOP"]["CLASSDIV2"] = $objForm->ge("CLASSDIV",2);

        //クラスコンボ
        $opt_hrclass = array();
        $opt_hrclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjl385jQuery::getHrClass($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_hrclass[] = array("label" => $row["HR_CLASS"]."：".$row["HR_NAME"], "value" => $row["HR_CLASS"]);
        }

        if (!strlen($model->pre_hr_class)) $model->pre_hr_class = $opt[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "PRE_HR_CLASS",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->pre_hr_class,
                            "options"    => $opt_hrclass));
        $arg["TOP"]["PRE_HR_CLASS"] = $objForm->ge("PRE_HR_CLASS");

        //一覧表示
        $model->data=array();
        $arr_examno = array();
        $counter=0;
        if ($model->pre_hr_class != "") {
            //データ取得
            $result    = $db->query(knjl385jQuery::getListPreSchoolInfo($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //受験番号を配列で取得
                $model->data["EXAMNO"][] = $row["EXAMNO"];

                //出欠１
                $fieldname = "ATTENDFLG1";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $checked = strlen($value) ? " checked" : "";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => $fieldname."-".$counter,
                                    "value"       => "1",
                                    "extrahtml"   => $checked." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" "));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //出欠２
                $fieldname = "ATTENDFLG2";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $checked = strlen($value) ? " checked" : "";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => $fieldname."-".$counter,
                                    "value"       => "1",
                                    "extrahtml"   => $checked." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" "));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //国語
                $fieldname = "SCORE1";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $objForm->ae( array("type"        => "text",
                                    "name"        => $fieldname."-".$counter,
                                    "extrahtml"   => " OnChange=\"Setflg(this);\" style=\"text-align:right;\" onblur=\"CheckScore(this);\" id=\"".$row["EXAMNO"]."\"",
                                    "maxlength"   => "3",
                                    "size"        => "3",
                                    "value"       => $value));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //算数
                $fieldname = "SCORE2";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $objForm->ae( array("type"        => "text",
                                    "name"        => $fieldname."-".$counter,
                                    "extrahtml"   => " OnChange=\"Setflg(this);\" style=\"text-align:right;\" onblur=\"CheckScore(this);\" id=\"".$row["EXAMNO"]."\"",
                                    "maxlength"   => "3",
                                    "size"        => "3",
                                    "value"       => $value));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //平バス１
                $fieldname = "STATIONCD1";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $checked = strlen($value) ? " checked" : "";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => $fieldname."-".$counter,
                                    "value"       => "1",
                                    "extrahtml"   => $checked." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" "));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //平バス2
                $fieldname = "STATIONCD2";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $checked = strlen($value) ? " checked" : "";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => $fieldname."-".$counter,
                                    "value"       => "1",
                                    "extrahtml"   => $checked." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" "));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //平バス3
                $fieldname = "STATIONCD3";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $checked = strlen($value) ? " checked" : "";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => $fieldname."-".$counter,
                                    "value"       => "1",
                                    "extrahtml"   => $checked." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" "));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //平バス4
                $fieldname = "STATIONCD4";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $checked = strlen($value) ? " checked" : "";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => $fieldname."-".$counter,
                                    "value"       => "1",
                                    "extrahtml"   => $checked." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" "));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //提出書類1
                $fieldname = "PRE_INFO1";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $checked = strlen($value) ? " checked" : "";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => $fieldname."-".$counter,
                                    "value"       => "1",
                                    "extrahtml"   => $checked." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" "));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //提出書類2
                $fieldname = "PRE_INFO2";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $checked = strlen($value) ? " checked" : "";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => $fieldname."-".$counter,
                                    "value"       => "1",
                                    "extrahtml"   => $checked." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" "));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //提出書類3
                $fieldname = "PRE_INFO3";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $checked = strlen($value) ? " checked" : "";
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => $fieldname."-".$counter,
                                    "value"       => "1",
                                    "extrahtml"   => $checked." OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" "));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                //備考
                $fieldname = "REMARK";
                $model->data[$fieldname."-".$counter] = $row[$fieldname];//各データを取得
                $value = ($model->isWarning()) ? $model->fields[$fieldname][$counter] : $row[$fieldname];
                $objForm->ae( array("type"        => "text",
                                    "name"        => $fieldname."-".$counter,
                                    "extrahtml"   => " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\"",
                                    "maxlength"   => "40",
                                    "size"        => "16",
                                    "value"       => $value));
                $row[$fieldname] = $objForm->ge($fieldname."-".$counter);

                $counter++;
                $arg["data"][] = $row;
            }
        }

        Query::dbCheckIn($db);

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
                            "name"      => "HID_PRE_HR_CLASS",
                            "value"     => "") );
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl385jindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl385jForm1.html", $arg); 
    }
}
?>
