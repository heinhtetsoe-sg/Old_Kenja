<?php

require_once('for_php7.php');


class knjz220dForm2{

    function main(&$model){

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz220dindex.php", "", "edit");

        $arg["YEAR"] = CTRL_YEAR;

        //DBオープン
        $db = Query::dbCheckOut();

        $subclasscd = $model->field1["SUBCLASSCD"];
        if($model->cmd == 'chgCmb3'){
            $subclasscd = $model->field2["SUBCLASSCD"];
        }
        $subclassname = $db->getOne(knjz220dQuery::getSubclassname($subclasscd));

        //ヘッダー設定
        $arg["head"] = array( "SEMESTERNAME"   => $model->semestername,
                              "CODE"           => $subclasscd,
                              "SUBJECT"        => $subclassname,
                              "GRADE"          => $model->grade_name[$model->grade]);
        
        //デフォルト値を作成
        $default_val = array("ASSESSLOW_ARRAY" => array()
                            ,"ASSESSHIGH_ARRAY" => array()
                            ,"ASSESSMARK_ARRAY" => array()
                            ,"PERCENT_ARRAY" => array()
                            );

        $query = knjz220dQuery::getDefaultData();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //小数点を取り除く。
            list($row["ASSESSLOW"]) = explode(".", $row["ASSESSLOW"]);
            list($row["ASSESSHIGH"]) = explode(".", $row["ASSESSHIGH"]);
            list($row["PERCENT"]) = explode(".", $row["PERCENT"]);

            $default_val["ASSESSLOW_ARRAY"][] = $row["ASSESSLOW"];
            $default_val["ASSESSHIGH_ARRAY"][] = $row["ASSESSHIGH"];
            $default_val["ASSESSMARK_ARRAY"][] = $row["ASSESSMARK"];
            $default_val["PERCENT_ARRAY"][] = $row["PERCENT"];
        }
        //最小値
        $default_val["MIN"]  = $default_val["ASSESSLOW_ARRAY"][0];
        //最大値
        $default_val["MAX"]  = $default_val["ASSESSHIGH_ARRAY"][get_count($default_val["ASSESSHIGH_ARRAY"]) - 1];

        $default_val["COUNT"] = get_count($default_val["ASSESSLOW_ARRAY"]);

        $isQuery = false;
        if (!isset($model->warning)) {
            $rows = array_map(function($row) {
                                    array_walk($row, "htmlspecialchars_array");
                                    //小数点を取り除く。
                                    list($row["ASSESSLOW"]) = explode(".", $row["ASSESSLOW"]);
                                    list($row["ASSESSHIGH"]) = explode(".", $row["ASSESSHIGH"]);
                                    list($row["PERCENT"]) = explode(".", $row["PERCENT"]);
                                    return $row;
                                }, knjz220dModel::fetchRows(knjz220dQuery::selectQuery($model), $db));
            if ($model->assesslevelcnt == '' &&get_count($rows)) {
                $model->assesslevelcnt = get_count($rows);
            }
            $isQuery = true;
        } else {
            $row    =& $model->field2;
        }
        if ($model->assesslevelcnt == '') {
            $model->assesslevelcnt = $default_val["COUNT"];
        }
        $up = "";
        if ($model->copy["FLG"] == true) {
            $up = $model->field1["UPDATED"];
        } else if ($isQuery) {
            $up = implode(",", array_map(function($row) { return $row["UPDATED"]; }, $rows));
        } else {
            $up = $model->field1["UPDATED"];
        }

        //段階数
        $extra = " style=\"text-align: right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["sepa"]["ASSESSLEVELCNT"] = knjCreateTextBox($objForm, $model->assesslevelcnt, "ASSESSLEVELCNT", 4, 3, $extra);

        //確定ボタン
        $extra = "onclick=\"return level(".$model->assesslevelcnt.");\" ";
        $arg["sepa"]["btn_level"] = knjCreateBtn($objForm, "btn_level", "確 定", $extra);

        //テーブルテンプレート作成。
        $tmp_JScript  = " STYLE=\"text-align: right\" "
                      . " onChange=\"document.getElementById('strID%s').innerHTML = (this.value - 1);\" "
                      . " onblur=\"this.value=isNumb(this.value), cleaning_val('off')\" "
                      ;
        //テーブル作成
        for ($i = 5; $i >= 2; $i--) {
            if ($isQuery) {
                $row = $rows[$i - 2];
            }

            $row["ASSESSLEVEL"] = $i;

            if ($model->cmd == 'setdef') {
                $row["ASSESSMARK"] = $default_val["ASSESSMARK_ARRAY"][$i - 1];
            } else if ($isQuery) {
                $row["ASSESSMARK"] = isset($row["ASSESSMARK"]) ? $row["ASSESSMARK"] : "" ;
            } else {
                $row["ASSESSMARK"] = $row["ASSESSMARK".$i];
            }

            //評定名称テキストボックス作成(ASSESSMARK)
            $extra = " STYLE=\"text-align: center\" onblur=\"this.value=toAlphaNumber(this.value)\"; ";
            $row["ASSESSMARKTEXT"] = knjCreateTextBox($objForm, $row["ASSESSMARK"], "ASSESSMARK".$i, 4, 2, $extra);

            //基準値テキストボックス作成(PERCENT)
            $per_textvalue = "";
            if ($model->cmd != 'new') {
                if ($isQuery) {
                    $per_textvalue  = isset($row["PERCENT"]) ? $row["PERCENT"] : "" ;
                } else {
                    $per_textvalue  = $row["PERCENT".$i];
                }
            }
            $extra = sprintf($tmp_JScript, ($i + 1));
            $row["PERCENT"] = knjCreateTextBox($objForm, $per_textvalue, "PERCENT".$i, 4, 2, $extra);

            //下限値テキストの有無設定
            if ($row["ASSESSLEVEL"] == 1) {
                $row["ASSESSLOWTEXT"] = $default_val["MIN"];
            } else {
                $textvalue = "";
                if ($model->cmd == 'setdef') {
                    $textvalue = $default_val["ASSESSLOW_ARRAY"][$i - 1];
                } else if ($model->cmd != 'new') {
                    if ($isQuery) {
                        $textvalue  = isset($row["ASSESSLOW"]) ? $row["ASSESSLOW"] : "" ;
                    } else {
                        $textvalue  = $row["ASSESSLOW".$i];
                    }
                }

                //テキストボックス作成(ASSESSLOW)
                $extra = sprintf($tmp_JScript, ($i - 1));
                $row["ASSESSLOWTEXT"] = knjCreateTextBox($objForm, $textvalue, "ASSESSLOW".$i, 4, 2, $extra);
            }

            //非text部分作成
            if ($row["ASSESSLEVEL"] == $model->assesslevelcnt) {
                $row["ASSESSHIGHTEXT"] = $default_val["MAX"];
            } else {
                $tablevalue = "";
                if ($model->cmd != 'new') {
                    if ($model->cmd == 'setdef') {
                        $tablevalue = $default_val["ASSESSHIGH_ARRAY"][$i - 1];
                    } else if ($isQuery) {
                        $tablevalue = isset($row["ASSESSHIGH"]) ? $row["ASSESSHIGH"] : "";
                    } else {
                        $tablevalue = ($row["ASSESSLOW".($i + 1)] - 1);
                    }
                }
                $row["ASSESSHIGHTEXT"] = "<span id=\"strID".$row["ASSESSLEVEL"]."\">".$tablevalue."</span>";
            }

            $arg["data"][] = $row;
        }

        //校種
        $query = knjz220dQuery::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //教科コンボ
        $query = knjz220dQuery::combo_clsQuery($model);
        $result = $db->query($query);
        $opt = array();
        $kindFlg = "";

        //教育課程対応
        $setClassDef = "00-".$model->schoolKind;
        $opt[] = array("label" => $setClassDef."：基本", "value" => $setClassDef);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => htmlspecialchars($row["LABEL"]), "value" => $row["VALUE"]);
            if ($model->Properties["useCurriculumcd"] == '1') {
                $kindFlg = $row["SCHOOL_KIND"];
            }
        }
        if ($model->classcd == "") {
            $model->classcd = $opt[0]["value"];
            if($subclasscd != ""){
                $model->classcd = substr($subclasscd, 0, 4);
            }
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $kindChk = array();
            $kindChk = explode("-", $model->classcd);
            $model->classcd = ($kindChk[1] == $kindFlg) ? $model->classcd : $opt[0]["value"];
        }
        $extra = "onChange=\"return btn_submit('chgCmb2'), iniright();\"";
        $arg["class_cmb"] = knjCreateCombo($objForm, "CLASSCD", $model->classcd, $opt, $extra, 1);

        //科目コード
        if($model->classcd == $setClassDef){
            $setClassDefcd   = $setClassDef."-00-000000";
            $setClassDefname = "基本科目";
            $opt = array();
            $opt[] = array("label" => $setClassDefcd."：".$setClassDefname, "value" => $setClassDefcd);

            $extra = "onChange=\"return btn_submit('chgCmb3');\"";
            $arg["subclass_cmb"] = knjCreateCombo($objForm, "SUBCLASSCD2", $subclasscd, $opt, $extra, 1);
        } else {
            $query = knjz220dQuery::getsubclass($model);
            $extra = "onChange=\"return btn_submit('chgCmb3');\"";
            $arg["subclass_cmb"] = knjCreateCombo($objForm, "SUBCLASSCD2", $subclasscd, knjz220dModel::createOpts($query, $db), $extra, 1);
        }


        //コピー機能コンボボックス内データ取得
        $query = knjz220dQuery::Copy_comboQuery($model->grade, $model);
        $extra = "";
        $arg["head"]["copy_cmb"] = knjCreateCombo($objForm, "COPY_SELECT", $model->copy["SELECT"], knjz220dModel::createOpts($query, $db), $extra, 1);
        $extra = "style=\"width:200px\"onclick=\"return btn_submit('copy')\"";
        $arg["head"]["BUTTON"] = knjCreateBtn($objForm, "btn_copy", "左の科目の評定をコピー", $extra);
        Query::dbCheckIn($db);
                    
        //デフォルトに戻すボタン
        $extra = "onclick=\"return btn_submit('setdef');\"";
        $arg["button"]["btn_def"] = knjCreateBtn($objForm, "btn_def", "デフォルトに戻す", $extra);

        //追加ボタン
        $extra = " onclick=\"return btn_submit('add'), cleaning_val('off');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_update", "追 加", $extra);

        //更新ボタン
        $extra = " onclick=\"return btn_submit('update'), cleaning_val('off');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = " onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"" ;
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SUBCLASSCD", $subclasscd);
        //テーブルの作成数
        knjCreateHidden($objForm, "TBL_COUNT", $default_val["COUNT"]);
        //データ処理日の保持
        knjCreateHidden($objForm, "UPDATED", $up);
        knjCreateHidden($objForm, "Cleaning", $model->Clean);
        //デフォルト値
        knjCreateHidden($objForm, "default_val_low", implode(',', $default_val["ASSESSLOW_ARRAY"]));
        knjCreateHidden($objForm, "default_val_high", implode(',', $default_val["ASSESSHIGH_ARRAY"]));
        knjCreateHidden($objForm, "default_val_mark", implode(',', $default_val["ASSESSMARK_ARRAY"]));
        knjCreateHidden($objForm, "default_val_per", implode(',', $default_val["PERCENT_ARRAY"]));

        $arg["finish"]  = $objForm->get_finish();
        if ($model->cmd == "chgCmb2") {
            $arg["reload"] = "window.open('knjz220dindex.php?cmd=edit3&SEMESTER={$model->semester}&GRADE={$model->grade}&CLASSCD={$model->classcd}','left_frame');";
        }
        if ($model->cmd == "chgCmb3") {
            $arg["reload"] = "window.open('knjz220dindex.php?cmd=edit2&SEMESTER={$model->semester}&GRADE={$model->grade}&CLASSCD={$model->classcd}&SUBCLASSCD={$model->field2["SUBCLASSCD"]}','right_frame');";
        }
        View::toHTML($model, "knjz220dForm2.html", $arg); 

    }
}
?>
