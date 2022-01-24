<?php

require_once('for_php7.php');


class knjs551form1 {
    function main(&$model) {
        
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjs551index.php", "", "main");

        $db = Query::dbCheckOut();

        //年度コンボボックス
        $optNendo = array();
        $query = knjs551Query::selectYearQuery();
        $result = $db->query($query);
        
        //年度コンボ対象配列
        $optNendoKouho = array();
        if ($model->field["YEAR"])  {
            $optNendoKouho[$model->field["YEAR"]] = "";
        }
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optNendoKouho[$row1["VALUE"]] = "";
        }
        $result->free();
        
        //降順に並び替えし、コンボへ追加して表示する
        krsort($optNendoKouho);
        
        foreach ($optNendoKouho as $key => $val) {
            $optNendo[] = array('label' => $key,
                                'value' => $key);
        }
        
        if (get_count($optNendo) > 0) {
            foreach ($optNendo as $key => $val) {
                $ctrlFlg = ($val["value"] == CTRL_YEAR) ? true : $ctrlFlg;
            }
            if (strlen($model->field["YEAR"]) == 0 && $ctrlFlg) {
                $Data = CTRL_YEAR;
            } else if (strlen($model->field["YEAR"]) == 0 && !$ctrlFlg) {
                $Data = $optNendo[0]["value"];
            }
        }

        $model->field["YEAR"] = (strlen($model->field["YEAR"]) == 0) ? $Data : $model->field["YEAR"];
        $extra = "onchange=\"return btn_submit('main');\"";
        $setYear = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $optNendo, $extra, 1);
        
        //textbox
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $setYearAdd = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

        //button
        $extra = "onclick=\"return add('year_add');\"";
        $setYearBtn = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["YEAR"] = array( "VAL" => $setYear."&nbsp;&nbsp;".
                                       $setYearAdd."&nbsp;".$setYearBtn);
        
        //学校区分コンボ
        $query = knjs551Query::getSchoolkind($model);
        $opt2 = array();
        $value_flg2 = false;
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt2[] = array('label' => $row2["LABEL"],
                           'value' => $row2["VALUE"]);
            if ($model->field["SCHOOL_KIND"] == $row2["VALUE"]) $value_flg2 = true;               
        }
        $model->field["SCHOOL_KIND"] = ($model->field["SCHOOL_KIND"] && $value_flg2) ? $model->field["SCHOOL_KIND"] : $opt2[0]["value"];
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["info"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $opt2, $extra, 1);
                
        //対象年度の学年データ存在確認
        $query = knjs551Query::getGradeCount($model);
        $getGrade = $db->getOne($query);
        knjCreateHidden($objForm, "GETGRADE", $getGrade);
        
        //選択した学校校種の対象年度の学年データ存在確認
        $query = knjs551Query::getUnitClassDataCount($model);
        $unitclassCnt = $db->getOne($query);
        knjCreateHidden($objForm, "UNITCLASSDATA", $unitclassCnt);
        
        //表示データ
        setDispData($objForm, $arg, $db, $model);
        
        //初期値ボタン
        $extra = "onclick=\"return btn_submit('shokiti');\"";
        $arg["button"]["btn_shokiti"] = knjCreateBtn($objForm, 'btn_shokiti', '初期値', $extra);
        
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);
        
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //Hidden作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );  

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        Query::dbCheckIn($db);        
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs551Form1.html", $arg);
    }
}


//データ表示用
function setDispData(&$objForm, &$arg, $db, $model) {

    //学年と教科を取得
    $opt = array();
    
    //教科を取得
    $model->optSubclass = array();
    $querySubclass  = knjs551Query::getSubclass($model);
    $result = $db->query($querySubclass);
    while ($rowSubclass = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->optSubclass[] = array('label'  => $rowSubclass["LABEL"],
                                      'value'  => $rowSubclass["VALUE"]);
        
        $arg["data"]["SUBCLASSCD"] = $rowSubclass["LABEL"];
    }
    
    $setKekka = array();
    for ($Scount = 0; $Scount < get_count($model->optSubclass); $Scount++) {
        $setKekka[$Scount]["SUBCLASSCD"] = $model->optSubclass[$Scount]['label'];
    }
    $result->free();

    //学年を取得
    $model->optGrade = array();
    
    $queryGrade = knjs551Query::getGrade($model);
    $result = $db->query($queryGrade);
    while ($rowGrade = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->optGrade[] = array('label' => $rowGrade["LABEL"],
                                   'value' => $rowGrade["VALUE"]);
    }
    
    for ($Gcount = 0; $Gcount < get_count($model->optGrade); $Gcount++) {
        $arg["data"]["GRADE_"."$Gcount"] = $model->optGrade[$Gcount]['label'];
    }
    $result->free();

    //教科のセット用変数
    $subclasscd;
    //学年のセット用変数
    $grade;

    $gradeTotal = array();
    for ($Scount = 0; $Scount < get_count($model->optSubclass); $Scount++) {
        //教科データの確認用（各学年の教科データが存在するかを確認していく）
        $subclasscd = $model->optSubclass[$Scount]['value'];
        
        for ($Gcount = 0; $Gcount < get_count($model->optGrade); $Gcount++) {
            //学年データ確認
            $grade = $model->optGrade[$Gcount]['value'];
        
            //UNIT_CLASS_LESSON_SCHOOL_DAT学年データ確認
            $query = knjs551Query::getUnitClassGradeCount($model, $subclasscd, $grade);
            $unitclassGradeCnt = $db->getOne($query);
            //学年データがある場合のテキストボックス作成
            if ($unitclassGradeCnt != 0) {
                
                //UNIT_CLASS_LESSON_SCHOOL_DATデータ取得
                $query = knjs551Query::setUnitClassDat($model, $subclasscd, $grade);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //標準時数
                    $setName = "_".$Scount."_".$Gcount;
                    $extra = "onblur=\"this.value=toInteger(this.value);setSum('".$Scount."', '".$Gcount."')\"";
                    $setKekka[$Scount]["STANDARD_TIME_".$Gcount] = knjCreateTextBox($objForm, $row["STANDARD_TIME"], "STANDARD_TIME".$setName, 6, 9, $extra);
                    $setKekka[$Scount]["SUBCLASS_TOTAL"] += $row["STANDARD_TIME"];
                    $gradeTotal[$Gcount] += $row["STANDARD_TIME"];
                }
                $result->free();
            
            //データがない場合はテキストボックスの表示
            } else {
                $query = knjs551Query::setUnitClassNodataDat($model, $subclasscd, $grade);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row["STANDARD_TIME"] = "0";
                    
                    //標準時数
                    $setName = "_".$Scount."_".$Gcount;
                    $extra = "onblur=\"this.value=toInteger(this.value);setSum('".$Scount."', '".$Gcount."')\"";
                    $setKekka[$Scount]["STANDARD_TIME_".$Gcount] = knjCreateTextBox($objForm, "", "STANDARD_TIME".$setName, 6, 9, $extra);
                    $setKekka[$Scount]["SUBCLASS_TOTAL"] += $row["STANDARD_TIME"];
                    $gradeTotal[$Gcount] += $row["STANDARD_TIME"];
                }
                $result->free();
            }
        }
        //合計
        $setKekka[$Scount]["SUBCLASS_TOTAL_ID"] = "STANDARD_TOTAL_".$Scount;
    }
    $arg["kekka"] = $setKekka;

    //合計行
    $totalAll = 0;
    $arg["gakunen"]["SUBCLASSCD"] = "合計";
    for ($Gcount = 0; $Gcount < get_count($model->optGrade); $Gcount++) {
        $arg["gakunen"]["GAKUEN_TOTAL_ID_".$Gcount] = "GAKUEN_TOTAL_".$Gcount;
        $arg["gakunen"]["GAKUEN_TOTAL_".$Gcount] = $gradeTotal[$Gcount];
        $totalAll += $gradeTotal[$Gcount];
    }
    $arg["gakunen"]["ALL_TOTAL_ID"] = "STANDARD_TOTAL_ALL";
    $arg["gakunen"]["ALL_TOTAL"] = $totalAll;
}

?>
