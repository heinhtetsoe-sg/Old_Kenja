<?php

require_once('for_php7.php');

class knjz211fForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz211findex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //観点評価段階値
        $opt = array();
        $opt[] = array("label" => "1：観点段階値", "value" =>"1");
        $opt[] = array("label" => "2：評価・評定段階値", "value" =>"2");
        if ($model->field["DIV"] == "") {
            $model->field["DIV"] = $opt[0]["value"];
        }
        if($model->field["DIV"] != "2"){
        	$arg["ISSET_DIV2"] = true;
        }
        $extra = "onChange=\"return btn_submit('change');\"";
        $arg["DIV"] = knjCreateCombo($objForm, "DIV", $model->field["DIV"], $opt, $extra, 1);

        //学年コンボ作成
        $query = knjz211fQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, 1);

        //教科コンボ作成
        $query = knjz211fQuery::getClassMst($model);
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");

        $disabled = ($model->field["CLASSCD"] == 'all')?' disabled = "disabled"':'';

        //科目コンボ作成
        $query = knjz211fQuery::getSubclassMst($model);
        $extra = "onchange=\"return btn_submit('change')\"" . $disabled;
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //観点コンボ作成
        $query = knjz211fQuery::getViewcd($model);
        $extra = "onchange=\"return btn_submit('change')\"" . $disabled;
        makeCmb($objForm, $arg, $db, $query, "VIEWCD", $model->field["VIEWCD"], $extra, 1, "blank");
        
        $query = knjz211fQuery::getViewcd($model);
        $extra = "onchange=\"return btn_submit('change')\"" . $disabled;
        makeCmb2($objForm, $arg, $db, $query, "VIEWCD2", $model->field["VIEWCD2"], $extra, 1, $model->field["VIEWCD"], "blank");

        //学期コンボ
        $query = knjz211fQuery::getSemester();
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, "");

        //段階値数取得
        $countAssess = $db->getOne(knjz211fQuery::selectCountQuery($model, $db));

        //条件が変更されたとき、初期値を取得
        if ($model->cmd == "change" || $model->cmd == "reset") {
            $model->field["MAX_ASSESSLEVEL"] = ($countAssess > 0) ? $countAssess : "";
        }
        $extra = "style=\"text-align: center\" onblur=\"this.value=NumCheck(this.value)\";";
        $arg["MAX_ASSESSLEVEL"] = knjCreateTextBox($objForm, $model->field["MAX_ASSESSLEVEL"], "MAX_ASSESSLEVEL", 1, 1, $extra);

        $model->data = array();
        if ($model->field["MAX_ASSESSLEVEL"]) {
            $counter = 0;
            //一覧表示
            $result = $db->query(knjz211fQuery::selectQuery($model,$db));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //段階値テキストボックス
                if (!isset($model->warning)) {
                    $value = ($countAssess == 0) ? "" : $row["ASSESSLEVEL"];
                } else {
                    $value = $model->fields["ASSESSLEVEL"][$counter];
                }
                $extra = "style=\"text-align: center\" onblur=\"this.value=toInteger(this.value)\";";
                $row["ASSESSLEVEL"] = knjCreateTextBox($objForm, $value, "ASSESSLEVEL-".$counter, 4, 2, $extra);

                //下限値テキストボックス
                if ($row["ROW_NUM"] != '1') {
                    $extra = "style=\"text-align: right\" onblur=\"isNumb(this, ".($row["ROW_NUM"] -1).");\"";
                    $value = (!isset($model->warning)) ? $row["ASSESSLOW"] : $model->fields["ASSESSLOW"][$counter];
                    $row["ASSESSLOW"] = knjCreateTextBox($objForm, $value, "ASSESSLOW-".$counter, 4, 2, $extra);
                } else {
                    $row["ASSESSLOW"] = '0';
                }

                //記号テキストボックス
                $extra = "style=\"text-align: center\"";
                if ($model->cmd == "change" || $model->cmd == "reset" || $model->cmd == "copy_kakutei") {
                    $value = $row["ASSESSMARK"];
                } else {
                    $value = $model->fields["ASSESSMARK"][$counter];
                }
                
                $row["ASSESSMARK"] = knjCreateTextBox($objForm, $value, "ASSESSMARK-".$counter, 4, 4, $extra);

                //上限値の表示
                if ($row["ASSESSHIGH"] == '' && $counter == 0 ) {
                    if($model->field["DIV"] == '2'){
                        $row["ASSESSHIGH"] = 0;
                        $result2 = $db->query(knjz211fQuery::getViewcd($model));
                        while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $result3 = $db->query(knjz211fQuery::selectQuery2($model,$row2['VALUE']));
                            $max = 0;
                            while ($row3 = $result3->fetchRow(DB_FETCHMODE_ASSOC)) {
                                if($row3["ASSESSLEVEL"] > $max){
                                	$max = $row3["ASSESSLEVEL"];
                                }
                            }
                            $row["ASSESSHIGH"] += $max;
                        }
                        if($row["ASSESSHIGH"] == 0){
                            $row["ASSESSHIGH"] = "100";
                        }
                    } else {
                        $row["ASSESSHIGH"] = "100";
                    }
                    $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                    $row["ASSESSHIGHTEXT"] .= $row["ROW_NUM"];
                    $row["ASSESSHIGHTEXT"] .= "\">";
                    $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    $row["ASSESSHIGHTEXT"] .= "</span>";
                } else {
                    if (isset($model->warning)) {
                        $row["ASSESSHIGH"] = $model->fields["ASSESSHIGH"][$counter];
                    }
                    $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                    $row["ASSESSHIGHTEXT"] .= $row["ROW_NUM"];
                    $row["ASSESSHIGHTEXT"] .= "\">";
                    $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    $row["ASSESSHIGHTEXT"] .= "</span>";
                }
                //段階値の上限値をhiddenで保持
                knjCreateHidden($objForm, "Assesshighvalue".$row["ROW_NUM"], $row["ASSESSHIGH"]);

                //上限値を配列で取得
                $model->data["ASSESSHIGH"][] = $row["ASSESSHIGH"];
                $model->data["ROW_NUM"][] = $row["ROW_NUM"];
                $counter++;
                $arg["data"][] = $row;
            }
            $result->free();
        }

        //1学期または前期の観点ごとのチェックボックス
        if ($model->fields["CLASSCD_ALL_CHECK"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
    	$extra .= ($model->field["CLASSCD"] == 'all')?'':' disabled="disabled"';
        $arg["CLASSCD_ALL_CHECK"] = knjCreateCheckBox($objForm, "CLASSCD_ALL_CHECK", "1", $extra);
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJZ211F");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        knjCreateHidden($objForm, "IS_HUITTI", IsHuitti($model,$db));
        knjCreateHidden($objForm, "IS_HUITTI2", IsHuitti2($model,$db));
        knjCreateHidden($objForm, "IS_HUITTI_FLAG", IsHuitti($model,$db),'0');


        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz211fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    if($name == 'CLASSCD'){
        $opt[] = array('label' => "--基本--", 'value' => "all");
        if ($value == 'all') $value_flg = true;
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size,$viewCd, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
		if($row['VALUE']!=$viewCd){
	        $opt[] = array('label' => $row["LABEL"],
	                       'value' => $row["VALUE"]);

	        if ($value == $row["VALUE"]) $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    //確定ボタン
    $extra = "onclick=\"return btn_submit('kakutei');\"";
    $arg["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);
    //更新ボタンを作成する
    $disabled = ($model->field["MAX_ASSESSLEVEL"] > 0) ? "" : " disabled";
    if($model->field["CLASSCD"] == 'all'){
    	$extra = "onclick=\"return btn_submit('update2');\"".$disabled;
    } else {
    	$extra = "onclick=\"return btn_submit('update');\"".$disabled;
    }
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $disabled = ($model->field["MAX_ASSESSLEVEL"] > 0) ? "" : ' disabled="disabled"';
    $disabled = ($model->field["CLASSCD"] == 'all')?' disabled="disabled"':$disabled;
    $extra = "onclick=\"return btn_submit('delete');\"".$disabled;
    $arg["btn_delete"] = knjCreateBtn($objForm, "btn_end", "削 除", $extra);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    
    $disabled = ($model->field["CLASSCD"] == 'all')?' disabled = "disabled"':'';
    //終了ボタンを作成する
    $extra = "onclick=\"return btn_submit('copy');\"" . $disabled;
    $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "右からコピー", $extra);
}
function IsHuitti($model,$db){
    if ($model->field["MAX_ASSESSLEVEL"]) {
        if($model->field["DIV"] == '1'){
            $model->field["DIV"] = 2;
            $row = $db->getRow(knjz211fQuery::selectQuery($model,$db),DB_FETCHMODE_ASSOC);
            if(get_count($row)==0){
            	return 0;
            }
            $model->field["DIV"] = 1;
            return $row["ASSESSHIGH"];
        }
    }
    return 0;
}
function IsHuitti2($model,$db){
    $assesshigh = 0;
    if ($model->field["MAX_ASSESSLEVEL"]) {
	    $result2 = $db->query(knjz211fQuery::getViewcd($model));
	    while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
	    	if($model->field['VIEWCD'] != $row2['VALUE']){
	            $result3 = $db->query(knjz211fQuery::selectQuery2($model,$row2['VALUE']));
	            $max = 0;
	            while ($row3 = $result3->fetchRow(DB_FETCHMODE_ASSOC)) {
	                if($row3["ASSESSLEVEL"] > $max){
	                	$max = $row3["ASSESSLEVEL"];
	                }
	            }
	            $assesshigh += $max;
	        }
	    }
    }
    return $assesshigh;
}
?>
