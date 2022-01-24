<?php
//ビュー作成用クラス
class knjl043vForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度学期表示
        $arg["TOP"]["YEAR"] = $model->examyear;

        //上位コンボを変更した際に下位コンボリセット用配列
        $cmdOrder = array();
        $cmdOrder["chgSchKind"]   = 1;
        $cmdOrder["chgAppDiv"]    = 2;
        $cmdOrder["chgCourse"]    = 3;
        $cmdOrder["chgFrequency"] = 4;

        $currentCmdOrder = $cmdOrder[$model->cmd];
        $cmdChkFlg = array_key_exists($model->cmd, $cmdOrder);

        //校種コンボ
        $extra = "onchange=\"return btn_submit('chgSchKind')\"";
        $query = knjl043vQuery::getSchoolKind($model);
        $arg["TOP"]["EXAM_SCHOOL_KIND"] = makeCmb($objForm, $arg, $db, $query, "EXAM_SCHOOL_KIND", $model->field["EXAM_SCHOOL_KIND"], $extra, 1);

        //出力対象
        $opt = array(1, 2);
        $model->field["OUTPUTDIV"] = ($model->field["OUTPUTDIV"] == "") ? "1" : $model->field["OUTPUTDIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUTDIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUTDIV", $model->field["OUTPUTDIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //入試区分
        $extra = "onchange=\"return btn_submit('chgAppDiv')\" style=\"width:120px;\" ";
        $query = knjl043vQuery::getApplicant($model);
        $model->field["APPLICANT_DIV"] = ($cmdChkFlg && $currentCmdOrder < $cmdOrder["chgAppDiv"]) ? "" : $model->field["APPLICANT_DIV"];
        $arg["TOP"]["APPLICANT_DIV"] = makeCmb($objForm, $arg, $db, $query, "APPLICANT_DIV", $model->field["APPLICANT_DIV"], $extra, 1);

        //志望コース
        $extra = "onchange=\"return btn_submit('chgCourse')\" style=\"width:240px;\" ";
        $query = knjl043vQuery::getCoursecode($model);
        $model->field["COURSE_DIV"] = ($cmdChkFlg && $currentCmdOrder < $cmdOrder["chgCourse"]) ? "" : $model->field["COURSE_DIV"];
        $arg["TOP"]["COURSE_DIV"] = makeCmb($objForm, $arg, $db, $query, "COURSE_DIV", $model->field["COURSE_DIV"], $extra, 1, "BLANK");

        //回数
        $extra = "onchange=\"return btn_submit('chgFrequency')\" style=\"width:50px;\" ";
        $query = knjl043vQuery::getFrequency($model);
        $model->field["FREQUENCY"] = ($cmdChkFlg && $currentCmdOrder < $cmdOrder["chgFrequency"]) ? "" : $model->field["FREQUENCY"];
        $arg["TOP"]["FREQUENCY"] = makeCmb($objForm, $arg, $db, $query, "FREQUENCY", $model->field["FREQUENCY"], $extra, 1, "BLANK");

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SELECTED_DATA");

        //DB切断
        Query::dbCheckIn($db);
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl043vindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjl043vForm1.html", $arg);
    }
}
/********************************************** ここから下関数 ****************************************************/

//コンボ作成関数
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"], 'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//////////////////////
//リストToリスト作成//
//////////////////////
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1[]= array('label' => '受験番号',
                   'value' => 'EXAMNO');
    $row1[]= array('label' => 'かな氏名',
                   'value' => 'NAME_KANA');
    $row1[]= array('label' => '男女',
                   'value' => 'SEX');
    $row1[]= array('label' => '★合計点',
                   'value' => 'EXAM_SCORE');

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

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
//////////////
//ボタン作成//
//////////////
function makeBtn(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}
