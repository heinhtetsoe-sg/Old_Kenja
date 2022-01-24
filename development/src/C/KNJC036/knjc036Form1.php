<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjc036Form1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc036index.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = CTRL_YEAR;

        //対象学期
        $query = knjc036Query::selectSemester();
        $extra = "Onchange=\"btn_submit('change');\"";
        makeCombo($objForm, $arg, $db, $query, $model->semester, "TOP", "SEMESTER", $extra, 1, "BLANK");

        /* 対象学級 */
        $query = knjc036Query::selectHrClass($model);
        $extra = "Onchange=\"btn_submit('change');\"";
        makeCombo($objForm, $arg, $db, $query, $model->hr_class, "TOP", "HR_CLASS", $extra, 1, "BLANK");

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model);

        /* ボタン作成 */
        makeButton($objForm, $arg);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        makeHidden($objForm);

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjc036Form1.html", $arg);
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $argName, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($name == "SEMESTER") {
            $arg[$argName][$name ."NAME" .$row["VALUE"]] = $row["LABEL"];
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
//      $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model)
{
    //SCHOOL_MSTの情報を取得。
    $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, CTRL_YEAR);
    //取得データを配列にセット
    $tmp_data = array();
    $setArray = array("LATE"        => array("SIZE" => 2, "MAXLEN" => 3),
                      "LATE_COR"    => array("SIZE" => 2, "MAXLEN" => 3),
                      "LATE_FIX"    => array("SIZE" => 2, "MAXLEN" => 3),
                      "JISUU"       => array("SIZE" => 2, "MAXLEN" => 3),
                      "KEKKA"       => array("SIZE" => 2, "MAXLEN" => 3),
                      "KEKKA_COR"   => array("SIZE" => 2, "MAXLEN" => 3),
                      "KEKKA_FIX"   => array("SIZE" => 2, "MAXLEN" => 3)
                      );
    $result = $db->query(knjc036Query::selectAttendQuery($model,$knjSchoolMst));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $tmp_data[$row["ATTENDNO"]]["ATTENDNO"] = $row["ATTENDNO"];
        $tmp_data[$row["ATTENDNO"]]["NAME_SHOW"] = $row["NAME_SHOW"];
        //各学期
        $sem = $row["SEMESTER"];
        foreach ($setArray as $key => $val) {
            $name = $key .$sem;
            $value = ($row[$key] != 0) ? $row[$key] : "";
            $tmp_data[$row["ATTENDNO"]]["SUM_".$key] += $value; //年間（累計）
            $tmp_data[$row["ATTENDNO"]]["SUM_".$key] = ($tmp_data[$row["ATTENDNO"]]["SUM_".$key] != 0) ? $tmp_data[$row["ATTENDNO"]]["SUM_".$key] : "";
            if (strstr($key, "_COR") && $sem == $model->semester) {
                $extra = " STYLE=\"text-align: right\"; onblur=\"checkText(this)\"; ";
                $row[$key] = knjCreateTextBox($objForm, $value, $name."[]", $val["SIZE"], $val["MAXLEN"], $extra);
            } else {
                $row[$key] = $value;
            }
            $tmp_data[$row["ATTENDNO"]][$name] = $row[$key]; //1～3学期
        }
        //hidden(学籍番号)
        $row["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$row["SCHREGNO"]."\">";
        $tmp_data[$row["ATTENDNO"]]["SCHREGNO"] = $row["SCHREGNO"];
    }
    //HTML表示用にセット
    $data = array();
    foreach ($tmp_data as $key => $val) {
        $data[] = $val;
    }
    $arg["attend_data"] = $data;
}
//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //更新ボタン
    $disabled = "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", " 更 新 ", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", " 取 消 ", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", " 終 了 ", "onclick=\"closeWin();\"");
    //印刷ボタン
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", " 印 刷 ", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
}
//Hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJC036");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
}
?>
