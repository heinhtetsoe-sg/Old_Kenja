<?php

require_once('for_php7.php');

class knja032Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja032index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $result = $db->query(knja032Query::GradeClassQuery());
        //コンボボックスの中身を作成
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["HR_NAME"],
                           "value" => $row["GC"]);

            if (!isset($model->gc_select)) $model->gc_select = $row["GC"];
            if ($model->gc_select == $row["GC"])
                //担任
                $arg["teacher"] = $row["STAFFNAME_SHOW"];
        }
        $result->free();
        $arg["gc_select"] = createCombo($objForm, "gc_select", $model->gc_select, $opt, "onChange=\"return btn_submit('main');\"", 1);

        //処理年度と処理学期
        $arg["TOP"] = array("TRANSACTION"   => CTRL_YEAR,
                            "SEMESTER"      => $model->control_data["学期名"][CTRL_SEMESTER],
                            "NEXTTRANSACTION"   => (int)CTRL_YEAR+1,
                            );
        $order[1] = "▲";
        $order[-1] = "▼";

        $arg["CHECKALL"] = createCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");
        $arg["ATTENDNO"] = View::alink("knja032index.php", "<font color=\"white\">年-組-番</font>", "",
                        array("cmd"=>"sort", "sort"=>"ATTENDNO")) .$order[$model->sort["ATTENDNO"]];
        $arg["TARGET"] = View::alink("knja032index.php", "<font color=\"white\">対象</font>", "",
                        array("cmd"=>"sort", "sort"=>"TARGET")) .$order[$model->sort["TARGET"]];

        //次年度の学期マスタが存在するか
        $query = knja032Query::selectSemester_Mst();
        if (!is_array($db->getRow($query, DB_FETCHMODE_ASSOC))){
            $arg["Closing"] = "  closing_window('MSG305'); " ;
        }
        //学校マスタ
        $query = knja032Query::selectSchool_Mst();
        $model->school = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($model->school["SEMESTERDIV"] != CTRL_SEMESTER){
            $arg["Closing"] = "  closing_window('MSG300'); " ;
        }

        //----------------------以下、擬似フレーム内リスト表示----------------------
        $query = knja032Query::ReadQuery($model);
        $result = $db->query($query);
        $student_sum = 0; //合計人数
        $summery      = array(); //SHIFTCD別の合計
        $summery["promoted"] = $summery["graduated"] = $summery["remained"] = 0;
        //取得保留単位をカンマ区切りで保持
        $g_c = $r_c = $sp = "";
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row["CHECKED"] = createCheckBox($objForm, "CHECKED", $row["SCHREGNO"], "onclick=\"chkClick(this)\" tabindex=\"-1\"", "1");
            if ($row["REMAINGRADE_FLG"] == '0'){
                $row["TARGET"] = "進級";
                $summery["promoted"]++;
            }else if ($row["REMAINGRADE_FLG"] == '1'){
                $row["TARGET"] = "留年";
                $summery["remained"]++;
            }else if ($row["GRD_DIV"] == '1'){
                $row["TARGET"] = "卒業";
                $summery["graduated"]++;
            }
            if ($model->school["SCHOOLDIV"] == "0") {
                $row["GET_CREDITS"] = $db->getOne(knja032Query::getGetCredits0($row["SCHREGNO"], $model));
                $row["REM_CREDITS"] = $db->getOne(knja032Query::getRemCredits0($row["SCHREGNO"]));
            } else {
                $row["GET_CREDITS"] = $db->getOne(knja032Query::getGetCredits1($row["SCHREGNO"]));
                $row["REM_CREDITS"] = "";
            }

            $g_c .= $sp .$row["GET_CREDITS"];
            $r_c .= $sp .$row["REM_CREDITS"];
            $sp  = ",";
            $arg["data"][] = $row;
            $student_sum++;
        }
        $arg["student_sum"] = $student_sum;             //合計人数
        $arg["promoted"]    = $summery["promoted"];     //進級
        $arg["graduated"]   = $summery["graduated"];    //卒業
        $arg["remained"]    = $summery["remained"];     //留年

        $result->free();

        //年組分離
        list($grade, $hr_class) = explode(",", $model->gc_select);
        $opt = array();
        if ((int)$model->school["GRADE_HVAL"] > (int)$grade ){
            $opt[] = array("label" => "1:進級",
                           "value" => 1 );
        }
        if (($model->school["SCHOOLDIV"] == "0" && (int)$model->school["GRADE_HVAL"] == (int)$grade) || $model->school["SCHOOLDIV"] == "1" ){
            $opt[] = array("label" => "2:卒業",
                           "value" => 2 );
        }
        $opt[] = array("label" => "3:留年",
                       "value" => 3 );
        $opt[] = array("label" => "9:取消",
                       "value" => 9 );

        //処理コンボ
        $arg["TRANS"] = createCombo($objForm, "TRANS", $model->trans, $opt, "onchange=\"chgTrans(this)\"", 1);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model, $g_c, $r_c);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja032Form1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //取り消しの場合は仮判定ボタンを押下不可とする
    if ($model->trans == 9 ) $disabled = "disabled";
    //仮判定ボタン
    $extra = "$disabled onclick=\"return btn_read();\"";
    $arg["btn_judge"] = createBtn($objForm, "btn_judge", "仮判定", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = createBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_can"] = createReset($objForm, "btn_can", "取 消", $extra);

    //終了ボタン
    $extra = " onclick=\"return closeWin();\"";
    $arg["btn_end"] = createBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $g_c, $r_c)
{
    $objForm->ae(createHiddenAe("GET_CREDITS", $g_c));
    $objForm->ae(createHiddenAe("REM_CREDITS", $r_c));
    $objForm->ae(createHiddenAe("SCHOOLDIV", $model->school["SCHOOLDIV"]));
    $objForm->ae(createHiddenAe("GRADE_HVAL", $model->school["GRADE_HVAL"]));
    $objForm->ae(createHiddenAe("GRADE_FEARVAL", $model->school["GRADE_FEARVAL"]));
    $objForm->ae(createHiddenAe("GRAD_CREDITS", $model->school["GRAD_CREDITS"]));
    $objForm->ae(createHiddenAe("cmd"));
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

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
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

//リセット作成
function createReset(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "reset",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
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
