<?php

require_once('for_php7.php');

class knjb300Form1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb300Form1", "POST", "knjb300index.php", "", "knjb300Form1");

        //権限チェック
        if ($model->auth < DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //校種リスト作成
        $query = knjb300Query::getSchoolKind($model);
        $result = $db->query($query);
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field['SCHOOL_KIND'], $extra, "1");

        //クラス一覧リスト作成する
        $opt_class_left = $opt_class_right = array();
        $query = knjb300Query::getHrClass($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (in_array($row["VALUE"], $model->selectHrClass)) {
                $opt_class_left[]  = array('value' => $row["VALUE"], 'label' => $row["LABEL"]);
            } else {
                $opt_class_right[] = array('value' => $row["VALUE"], 'label' => $row["LABEL"]);
            }
        }
        $result->free();
        //出力対象クラスリストを作成する 
        $extra = "style=\"width:180px\" ondblclick=\"move('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", '', $opt_class_right, $extra, 15, "");
        //出力対象クラスリストを作成する 
        $extra = "style=\"width:180px\" ondblclick=\"move('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", '', $opt_class_left, $extra, 15, "");

        //対象選択ボタンを作成する（全部）/////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('right', 'ALL');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
        //対象取消ボタンを作成する（全部）/////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('left', 'ALL');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        //対象選択ボタンを作成する（一部）/////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取消ボタンを作成する（一部）/////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //対象日/対象期間
        $opt = array(1, 2);
        $model->field['TYEP_DATE'] = ($model->field['TYEP_DATE']) ? $model->field['TYEP_DATE'] : '1';
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TYEP_DATE{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "TYEP_DATE", $model->field['TYEP_DATE'], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //学期情報
        $query = knjb300Query::getSemesterInfo($model);
        $model->semesterInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //対象日
        $extra = "";
        $model->field['EXECUTEDATE'] = $model->field['EXECUTEDATE'] ? $model->field['EXECUTEDATE'] : CTRL_DATE;
        if (str_replace("/", "-", $model->field['EXECUTEDATE']) < $model->semesterInfo["SDATE"] || str_replace("/", "-", $model->field['EXECUTEDATE']) > $model->semesterInfo["EDATE"]) {
            $model->field['EXECUTEDATE'] = $model->semesterInfo["SDATE"];
        }
        // $extra = "extra=dateChange(f.document.forms[0][\\'EXECUTEDATE\\'].value);";
        $arg["EXECUTEDATE"] = View::popUpCalendar($objForm, "EXECUTEDATE", str_replace("-", "/", $model->field['EXECUTEDATE']), $extra);

        //開始日付
        $model->field['START_DATE'] = $model->field['START_DATE'] ? $model->field['START_DATE'] : CTRL_DATE;
        if (str_replace("/", "-", $model->field['START_DATE']) < $model->semesterInfo["SDATE"] || str_replace("/", "-", $model->field['START_DATE']) > $model->semesterInfo["EDATE"]) {
            $model->field['START_DATE'] = $model->semesterInfo["SDATE"];
        }
        // $extra = "extra=dateChange(f.document.forms[0][\\'START_DATE\\'].value);";
        $arg["START_DATE"] = View::popUpCalendar($objForm, "START_DATE", str_replace("-", "/", $model->field['START_DATE']), $extra);
        //終了日付
        $defaultTime = date('Y-m-d', strtotime($model->field['START_DATE'].' +1 week')-24*60*60);
        $model->field['END_DATE'] = $model->field['END_DATE'] ? $model->field['END_DATE'] : $defaultTime;
        if (str_replace("/", "-", $model->field['END_DATE']) < str_replace("/", "-", $model->field['START_DATE'])) {
            $model->field['END_DATE'] = $model->field['START_DATE'];
        }
        if (str_replace("/", "-", $model->field['END_DATE']) > $model->semesterInfo["EDATE"]) {
            $model->field['END_DATE'] = $model->semesterInfo["EDATE"];
        }
        $arg["END_DATE"] = View::popUpCalendar($objForm, "END_DATE", str_replace("-", "/", $model->field['END_DATE']), $extra);

        //csvボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "実行", $extra);
        //ヘッダ有チェックボックス
        $extra = " checked ";
        $arg["chk_header"] = knjCreateCheckBox($objForm, "chk_header", "1", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb300Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($name == 'GRADE') {
        $opt[] = array('label' => '全て',
                       'value' => 99);
    }

    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeHidden(&$objForm, $model)
{

    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    // 学期情報
    knjCreateHidden($objForm, "SEMESTER_SDATE", $model->semesterInfo['SDATE']);
    knjCreateHidden($objForm, "SEMESTER_EDATE", $model->semesterInfo['EDATE']);
    // 出力対象クラス
    knjCreateHidden($objForm, "SELECT_HR_CLASS", implode($model->selectHrClass, ","));
    // フレームロック
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}
?>
