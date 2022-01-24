<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 *　2005.03.25 nakamoto データがない場合にＳＱＬエラー発生の不具合を修正---NO001
 */
class knja142Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja142Form1", "POST", "knja142index.php", "", "knja142Form1");


        //フォーム選択・・・1:仮身分証明書(新入生), 2:身分証明書(在籍), 3:仮身分証明書(在籍)
        $opt_output[0]=1;
        $opt_output[1]=2;
        $opt_output[2]=3;//---2005.06.10

        $model->field["OUTPUT"] = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "3";//---2005.06.10
        $extra = "onclick=\"return btn_submit('output');\"";
        createRadio($objForm, $arg, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));

        //年度・学期
        if ($model->field["OUTPUT"] == 1) {
            if (CTRL_SEMESTER < $model->control["学期数"]) {
                $ctrl_year      = CTRL_YEAR;
                $ctrl_semester  = CTRL_SEMESTER + 1;
            //最終学期
            } else {
                $ctrl_year      = CTRL_YEAR + 1;
                $ctrl_semester  = 1;
            }
        } else {
            $ctrl_year      = CTRL_YEAR;
            $ctrl_semester  = CTRL_SEMESTER;
        }
        //学期マスタ
        $db = Query::dbCheckOut();
        $query = knja142Query::getSemeMst($ctrl_year,$ctrl_semester);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //年度
        $objForm->ae(createHiddenAe("YEAR", $Row_Mst["YEAR"]));
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];

        //学期
        $objForm->ae(createHiddenAe("GAKKI", $Row_Mst["SEMESTER"]));
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //有効期限
        if ($model->field["OUTPUT"] == 1 || $model->field["OUTPUT"] == 3) {//---2005.06.10
            //月末日を取得
            if ($Row_Mst["SDATE"] != "") {//---NO001
                $db = Query::dbCheckOut();
                $query = knja142Query::getLastDay($Row_Mst["SDATE"]);
                $term_edate = $db->getOne($query);
                Query::dbCheckIn($db);
            }//---NO001
            if( $model->cmd=="output" || !isset($model->field["TERM_SDATE"]) ) 
                $model->field["TERM_SDATE"] = str_replace("-","/",$Row_Mst["SDATE"]);
            if( $model->cmd=="output" || !isset($model->field["TERM_EDATE"]) ) 
                $model->field["TERM_EDATE"] = str_replace("-","/",$term_edate);
        }
        if ($model->field["OUTPUT"] == 2) {
            if( $model->cmd=="output" || !isset($model->field["TERM_SDATE"]) ) 
                $model->field["TERM_SDATE"] = $model->control["学期開始日付"][9];
            if( $model->cmd=="output" || !isset($model->field["TERM_EDATE"]) ) 
                $model->field["TERM_EDATE"] = $model->control["学期終了日付"][9];
        }
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm,"TERM_SDATE",$model->field["TERM_SDATE"]);//開始
        $arg["data"]["TERM_EDATE"]=View::popUpCalendar($objForm,"TERM_EDATE",$model->field["TERM_EDATE"]);//終了

        //クラス
        $db = Query::dbCheckOut();
        $query = knja142Query::getAuth($ctrl_year,$ctrl_semester);
        $class_flg = false;
        $row1 = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$class_flg) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }
        $extra = "onchange=\"return btn_submit('knja142');\"";
        $arg["data"]["GRADE_HR_CLASS"] = createCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //生徒一覧リスト
        $db = Query::dbCheckOut();

        if ($model->field["OUTPUT"] == 1) {
            $query = knja142Query::getSchno1($model,$ctrl_year,$ctrl_semester);
        } else {
            $query = knja142Query::getSchno2($model,$ctrl_year,$ctrl_semester);
        }
        $result = $db->query($query);
        $opt_right = array();
        $opt_left  = array();

        $selectStudent = ($model->selectStudent != "") ? explode(",",$model->selectStudent) : array();
        $selectStudentLabel = ($model->selectStudentLabel != "") ? explode(",",$model->selectStudentLabel) : array();

        for ($i = 0; $i < get_count($selectStudent); $i++) {
            $opt_left[] = array('label' => $selectStudentLabel[$i],
                                'value' => $selectStudent[$i]);
        }

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($model->field["OUTPUT"] == 1) {
                $row["NAME"] = $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"];//---2005.03.31
            }
            if (!in_array($row["SCHREGNO"], $selectStudent)) {
                $opt_right[] = array('label' => $row["NAME"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = createCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = createCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);

        $objForm->ae(createHiddenAe("selectStudent"));
        $objForm->ae(createHiddenAe("selectStudentLabel"));

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja142Form1.html", $arg); 
    }
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    $objForm->ae( array("type"      => "radio",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));
    for ($i = 1; $i <= $count; $i++) {
        $arg["data"][$name.$i] = $objForm->ge($name, $i);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJA142"));
    $objForm->ae(createHiddenAe("DOCUMENTROOT", DOCUMENTROOT));
    $objForm->ae(createHiddenAe("cmd"));
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
