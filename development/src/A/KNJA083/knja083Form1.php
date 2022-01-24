<?php

require_once('for_php7.php');
class knja083Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja083Form1", "POST", "knja083index.php", "", "knja083Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["authError"] = "OnAuthError();";
        }

        //最終学期チェック
        $maxYS = $db->getRow(knja083Query::getYearSemester($model, "max"), DB_FETCHMODE_ASSOC);
        if (CTRL_SEMESTER != $maxYS["SEMESTER"]) {
            $arg["maxsemError"] = "OnMaxSemError(); ";
        }

        //初期化（学年変更）
        if ($model->cmd == "change") {
            $model->fields = array();
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //新年度・新学期
        $newYS = $db->getRow(knja083Query::getYearSemester($model, "new"), DB_FETCHMODE_ASSOC);
        $arg["NEW_YS"] = $newYS["YEAR"].'年度';
        if (!strlen($model->field["YEAR"])) {
            $model->field["YEAR"] = $newYS["YEAR"];
        }
        if (!strlen($model->field["SEMESTER"])) {
            $model->field["SEMESTER"] = $newYS["SEMESTER"];
        }
        if (!strlen($model->field["SEMESTERNAME"])) {
            $model->field["SEMESTERNAME"] = $newYS["SEMESTERNAME"];
        }

        knjCreateHidden($objForm, "YEAR", $model->field["YEAR"]);
        knjCreateHidden($objForm, "SEMESTER", $model->field["SEMESTER"]);
        knjCreateHidden($objForm, "SEMESTERNAME", $model->field["SEMESTERNAME"]);

        //学年コンボ
        $query = knja083Query::getGrade($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //開始学年判定
        $startGradeArray = $db->getCol(knja083Query::getStartGrade($model));
        $s_grade = in_array($model->field["GRADE"], $startGradeArray);
        knjCreateHidden($objForm, "S_GRADE", $s_grade);

        //クラス一覧取得
        $hrArray = array();
        $hrList = $sep = "";
        $query = knja083Query::getHrClass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hrArray[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);

            $hrList .= $sep.$row["VALUE"];
            $sep = ",";
        }
        $result->free();

        knjCreateHidden($objForm, "HR_CLASS_LIST", $hrList);

        //コース（または履修パターン）
        $counter = 0;
        $courseList = $courseNameList = $sep = "";
        if ($s_grade) {
            $query = knja083Query::getCourseList($model);
        } else {
            $query = knja083Query::getPatternList($model);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //課程学科コースを配列で取得
            $model->data["COURSE"][$counter] = $row["COURSE"];
            $courseList .= $sep.$row["COURSE"];
            $courseNameList .= $sep.$row["COURSENAME"];
            $sep = ",";

            //開始クラス
            $opt = array();
            $value_flg = false;
            $value = $model->fields["START_CLASS"][$counter];
            foreach ($hrArray as $key => $val) {
                $opt[] = array('label' => $val['label'],
                               'value' => $val['value']);
                if ($value == $val['value']) {
                    $value_flg = true;
                }
            }
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
            $row["START_CLASS"] = knjCreateCombo($objForm, "START_CLASS:".$row["COURSE"], $value, $opt, "", 1);

            //クラス数
            $extra = "onBlur=\"this.value=toInteger(this.value);\" style=\"text-align:right;\"";
            $row["CLASS_CNT"] = knjCreateTextBox($objForm, $model->fields["CLASS_CNT"][$counter], "CLASS_CNT:".$row["COURSE"], 4, 2, $extra);

            $arg["data"][] = $row;
            $counter++;

            knjCreateHidden($objForm, "TARGET_CLASS:".$row["COURSE"]);
        }
        $result->free();

        knjCreateHidden($objForm, "COURSE_LIST", $courseList);
        knjCreateHidden($objForm, "COURSE_NAME_LIST", $courseNameList);

        //処理結果一覧
        if ($s_grade) {
            $query = knja083Query::getList($model);
        } else {
            $query = knja083Query::getList2($model);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["list"][] = $row;
        }
        $result->free();

        //使用不可
        $disable = ($model->field["GRADE"]) ? "" : " disabled";

        //実行ボタン
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["button"]["btn_execute"] = knjCreateBtn($objForm, "btn_execute", "割り振り処理実行", $extra.$disable);

        //CSV出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra.$disable);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja083Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
