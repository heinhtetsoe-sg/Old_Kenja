<?php

require_once('for_php7.php');

class knja032mForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja032mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["TOP"]["CTRL_YEAR"] = CTRL_YEAR;

        //コンボボックス
        $query = knja032mQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        $model->semester = $model->semester ? $model->semester : CTRL_SEMESTER;
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //最大学期
        $query = knja032mQuery::getSemester($model, "MAX");
        $maxSeme = $db->getOne($query);
        $model->isMaxSeme = $maxSeme == $model->semester ? true : false;

        $query = knja032mQuery::GradeClassQuery($model);
        $result = $db->query($query);
        //コンボボックスの中身を作成
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["HR_NAME"],
                           "value" => $row["GC"]);

            $model->schoolKind[$row["GC"]] = $row["SCHOOL_KIND"];

            if (!isset($model->gc_select)) $model->gc_select = $row["GC"];
            if ($model->gc_select == $row["GC"])
                //担任
                $arg["teacher"] = $row["STAFFNAME_SHOW"];
        }
        $result->free();
        $arg["TOP"]["gc_select"] = knjCreateCombo($objForm, "gc_select", $model->gc_select, $opt, "onChange=\"return btn_submit('main');\"", 1);

        $arg["dispCredit"] = "";
        if ($model->schoolKind[$model->gc_select] == "H") {
            $arg["dispCredit"] = "1";
        }

        $order[1] = "▲";
        $order[-1] = "▼";

        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");
        $arg["ATTENDNO"] = View::alink("knja032mindex.php", "<font color=\"white\">年-組-番</font>", "",
                        array("cmd"=>"sort", "sort"=>"ATTENDNO")) .$order[$model->sort["ATTENDNO"]];
        $arg["TARGET"] = View::alink("knja032mindex.php", "<font color=\"white\">対象</font>", "",
                        array("cmd"=>"sort", "sort"=>"TARGET")) .$order[$model->sort["TARGET"]];

        //次年度の学期マスタが存在するか
        $query = knja032mQuery::selectSemester_Mst();
        if ($model->isMaxSeme && !is_array($db->getRow($query, DB_FETCHMODE_ASSOC))){
            $arg["Closing"] = "  closing_window('MSG305', '( 学期マスタ )'); " ;
        }

        //名称マスタ「A023」略称2の存在チェック
        $setYear = $model->isMaxSeme ? $model->nextYear : $model->year;
        $model->A023Cnt = $db->getOne(knja032mQuery::selectA023($setYear));
        $A023 = $db->getOne(knja032mQuery::selectA023Abbv2($setYear));
        if ($model->A023Cnt > 1 && $A023 > 0) {
            $arg["Closing"] = " closing_window('MSG305', '( 名称マスタ「A023」 )'); " ;
        }

        //卒業可能学年か
        $model->field["GRADUATE"] = 0;
        $query = knja032mQuery::getGraduate($model, substr($model->gc_select, 0, 2));
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->field["GRADUATE"] = $row["IS_GRD"];
        }

        //年組分離
        list($grade, $hr_class) = explode(",", $model->gc_select);

        $opt = array();
        $model->sinkyuSyuuryouFlg = '';
        $setName = $model->isMaxSeme ? "1:年次進行" : "1:学期進行";
        $opt[] = array("label" => $setName, "value" => 1);
        $opt[] = array("label" => "2:卒業", "value" => 2);
        $opt[] = array("label" => "9:取消", "value" => 9);

        //処理コンボ
        $arg["TRANS"] = knjCreateCombo($objForm, "TRANS", $model->trans, $opt, "", 1);

        //----------------------以下、擬似フレーム内リスト表示----------------------
        $query = knja032mQuery::ReadQuery($model);
        $result = $db->query($query);
        $student_sum = 0; //合計人数
        $summery      = array(); //SHIFTCD別の合計
        $summery["promoted"] = $summery["graduated"] = 0;
        //取得保留単位をカンマ区切りで保持
        $g_c = $r_c = $sp = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["SCHREGNO"], "onclick=\"chkClick(this)\" tabindex=\"-1\"", "1");
            if ($row["NEXT_REGD"] == '1'){
                $row["TARGET"] = "進級";
                $summery["promoted"]++;
            }else if ($row["GRD_DIV"] == '1'){
                $row["TARGET"] = "卒業";
                $summery["graduated"]++;
            }
            if ($model->Properties["useRecordDat"] == "KIN_RECORD_DAT") {
                $row["GET_CREDITS"] = $db->getOne(knja032mQuery::getGetCredits2($row["SCHREGNO"], $model));
                $row["REM_CREDITS"] = $db->getOne(knja032mQuery::getRemCredits2($row["SCHREGNO"], $model));
            } else {
                $row["GET_CREDITS"] = $db->getOne(knja032mQuery::getGetCredits1($row["SCHREGNO"], $model));
                if ($model->school["SCHOOLDIV"] == "0") {
                    $row["REM_CREDITS"] = $db->getOne(knja032mQuery::getRemCredits0($row["SCHREGNO"], $model));
                } else {
                    $row["REM_CREDITS"] = "";
                }
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

        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model, $g_c, $r_c);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja032mForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //取り消しの場合は仮判定ボタンを押下不可とする
    if ($model->trans == 9 ) $disabled = "disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);

    //終了ボタン
    $extra = " onclick=\"return closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//Hidden作成
function makeHidden(&$objForm, $model, $g_c, $r_c)
{
    knjCreateHidden($objForm, "GET_CREDITS", $g_c);
    knjCreateHidden($objForm, "REM_CREDITS", $r_c);
    knjCreateHidden($objForm, "SCHOOLDIV", $model->school["SCHOOLDIV"]);
    knjCreateHidden($objForm, "GRADE_HVAL", $model->school["GRADE_HVAL"]);
    knjCreateHidden($objForm, "GRADE_FEARVAL", $model->school["GRADE_FEARVAL"]);
    knjCreateHidden($objForm, "GRAD_CREDITS", $model->school["GRAD_CREDITS"]);
    knjCreateHidden($objForm, "cmd");
}
?>
