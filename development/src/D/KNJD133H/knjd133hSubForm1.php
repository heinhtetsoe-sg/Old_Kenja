<?php

require_once('for_php7.php');

class knjd133hSubForm1 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjd133hindex.php", "", "sel");

        $db = Query::dbCheckOut();

        $semester = '';
        $query = knjd133hQuery::getSemester($model->replace_get['SEMESTER']);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['VALUE'] == $model->replace_get['SEMESTER']) {
                $semester = $row["LABEL"];
            }
        }
        
        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$model->urlSchoolKind."08";
        $model->count = $db->getone(knjd133hquery::getNameMstche($model));

        $subclasscd = '';
        $query = knjd133hQuery::selectSubclassQuery($model, $model->replace_get['SEMESTER']);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['VALUE'] == $model->replace_get['SUBCLASSCD']) {
                $subclasscd = $row["LABEL"];
            }
        }
        
        $chaircd = '';
        $query = knjd133hQuery::selectChairQuery($model, $model->replace_get['SEMESTER'], $model->replace_get["SUBCLASSCD"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['VALUE'] == $model->replace_get['CHAIRCD']) {
                $chaircd = $row["LABEL"];
            }
        }
        
        
        $arg["TOP"] =  CTRL_YEAR."年度 {$semester}  {$subclasscd}  {$chaircd}";

        //生徒情報
        if(preg_match('/replace1|replace_qualifiedCd|replace_conditionDiv/', $model->cmd)) {
            $Row =& $model->field;
        } else {
            if ($model->seq == "00") {
                $query = knjd133hQuery::getSchregQualifiedTestDat($model);
            } else {
                $query = knjd133hQuery::getSchregQualifiedHobbyDat($model);
            }
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "更 新", $extra);
        //戻る
        $link = REQUESTROOT."/D/KNJD133H/knjd133hindex.php?cmd=back";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //評価定型文
        makeList($objForm, $arg, $db, $model);
        
        //生徒一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd133hSubForm1.html", $arg);
    }
}

/******************/
/* リストToリスト */
/******************/
function makeListToList(&$objForm, &$arg, $db, $model) {
    //生徒一覧
    $selectdata = explode(",", $model->replace_data["selectdata"]);
    if ($selectdata[0]=="") $selectdata[0] = $model->schregno;
    $opt_left = $opt_right = array();

    //学期開始日、終了日
    $seme = $db->getRow(knjd133hQuery::getSemester($model->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
    //学籍処理日が学期範囲外の場合、学期終了日を使用する。
    if ($seme["SDATE"] <= CTRL_DATE && CTRL_DATE <= $seme["EDATE"]) {
        $execute_date = CTRL_DATE;  //初期値
    } else {
        $execute_date = $seme["EDATE"];     //初期値
    }

    $query = knjd133hQuery::selectQuery($model, $execute_date, $model->replace_get["SEMESTER"], $model->replace_get["SUBCLASSCD"], $model->replace_get["CHAIRCD"]);
    
    $result   = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row['LABEL'] = $row['ATTENDNO'] . ' ' . $row['SCHREGNO'] . ' ' . $row['NAME_SHOW'];
        $row['VALUE'] = $row['SCHREGNO'];
        if(in_array($row["SCHREGNO"], $selectdata)){
            $opt_left[]   = array("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        }else{
            $opt_right[]  = array("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $datacnt = 0;
    
    $grade = $db->getRow(knjd133hQuery::getGrade($model,$model->replace_get['SEMESTER'],$model->replace_get['CHAIRCD']), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "GRADE", $grade["VALUE"]);
    $model->replace_get["GRADE"] = $grade["VALUE"];
            
    $query = knjd133hQuery::getHtrainRemarkTempDat($model, $model->replace_get["GRADE"]);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $check = "";
        if (isset($model->replace_data["check"])) {
            foreach($model->replace_data["check"] as $key =>$value){
                if ($value == $row["REMARK"]) {
                    $check .= " checked='checked' ";
                }
            }
        }
        $objForm->ae(array("type"       => "checkbox",
                           "name"       => "CHECK",
                           "value"      => $row["REMARK"],
                           "extrahtml"  => $check,
                           "multiple"   => "1" ));
        $row["CHECK"] = $objForm->ge("CHECK");
        $arg["list_data"][] = $row;
        $hDataDiv = $row["DATA_DIV"];
        $datacnt++;
    }
    $result->free();
    knjCreateHidden($objForm, "DATA_DIV", $hDataDiv);
    return $datacnt;
}

?>
