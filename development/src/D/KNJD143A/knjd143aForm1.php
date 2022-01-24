<?php

require_once('for_php7.php');


class knjd143aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        /* Add by PP for Title 2020-01-20 start */
        $arg["TITLE"]   = "小学部通知表所見入力学習のようす画面";
        
         if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJA143aForm1_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJA143aForm1_CurrentCursor915\", x);</script>";
            $model->message915 = "";
        }
         /* Add by PP for Title 2020-01-31 end */

        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        if ($model->cmd == "back") {
            $model->field["SUBCLASSCD"] = $model->subclasscd;
            $model->field["GRADE_HR_CLASS"]    = $model->gradehrclass;
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種一覧取得
            $schoolkind = $db->getCol(knjd143aQuery::getSchoolKindList($model, "cnt"));
            if (get_count($schoolkind) > 1) {
                //校種コンボ
                $query = knjd143aQuery::getSchoolKindList($model, "list");
                // Add by PP for CurrentCursor 2020-01-20 start
                $extra = "id=\"schoolkind\" onChange=\"current_cursor('schoolkind'); btn_submit('schoolkind')\"; aria-label=\"校種\"";
                // Add by PP for CurrentCursor 2020-01-31 end
                makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
                $arg["useSchoolKindCmb"] = 1;
            } else {
                $model->field["SCHOOL_KIND"] = $schoolkind[0];
                knjCreateHidden($objForm, "SCHOOL_KIND", $schoolkind[0]);
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $model->field["SCHOOL_KIND"] = SCHOOLKIND;
            knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        } else {
            $model->field["SCHOOL_KIND"] = SCHOOLKIND;
            knjCreateHidden($objForm, "SCHOOL_KIND");
        }

        //学期コンボ
        if ($model->field["SEMESTER"] == '') {
            $model->field["SEMESTER"] = CTRL_SEMESTER;
        }
        $query = knjd143aQuery::selectSemesterQuery($model);
        // Add by PP for CurrentCursor 2020-01-20 start
        $extra = "id=\"semester\" onChange=\"current_cursor('semester'); btn_submit('semester')\";  aria-label=\"学期\"";
        // Add by PP for CurrentCursor 2020-01-31 end
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        $model->isLastSemester = knjd143aQuery::isLastSemester($db, $model->field["SEMESTER"]);
        // Add by PP for PC-Talker 2020-01-20 start
        $grade_result = "";
        if ($model->isLastSemester) {
            $arg["SCORE_TITLE"] = "学年<br>成績";
            $grade_result = "学年成績";
        } else {
            $query = knjd143aQuery::getTestitemMstCountflgTestitemname($model, $model->field["SEMESTER"]);
            $arg["SCORE_TITLE"] = $db->getOne($query);
            $grade_result = $db->getOne($query);
        }
        // Add by PP for PC-Talker 2020-01-31 end

        //年組コンボ作成
        $query = knjd143aQuery::getHrClass($model);
        // Add by PP for CurrentCursor 2020-01-20 start
        $extra = "id=\"grade_hr_class\" onchange=\"current_cursor('grade_hr_class'); return btn_submit('grade_hr_class')\"  aria-label=\"年組\"";
        // Add by PP for CurrentCursor 2020-01-31 end
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        //科目コンボ
        $query = knjd143aQuery::selectSubclassQuery($model);
        // Add by PP for CurrentCursor 2020-01-20 start
        $extra = "id=\"subclasscd\" onChange=\"current_cursor('subclasscd'); btn_submit('subclasscd')\";  aria-label=\"科目\"";
        // Add by PP for CurrentCursor 2020-01-31 end
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1);

        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        $sdate = str_replace("/","-",$model->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/","-",$model->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }

        $arg["conduct_eval_moji"] = $model->conduct_eval_moji;
        $arg["conduct_eval_gyo"] = $model->conduct_eval_gyo;

        //初期化
        $model->data=array();
        $counter=0;

        //一覧表示
        $colorFlg = false;
        $query = knjd143aQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        // Add by PP for PC-Talker 2020-01-20 start
        $attendno = "";
        $name_show = "";
        // Add by PP for PC-Talker 2020-01-31 end

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //氏名欄に学籍番号表記
            if ($model->Properties["use_SchregNo_hyoji"] == 1) {
                $row["SCHREGNO_SHOW"] = $row["SCHREGNO"] . "　";
            }
            
            //クラス-出席番(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
                // Add by PP for PC-Talker 2020-01-20 start
                $attendno = $row["ATTENDNO"];
                // Add by PP for PC-Talker 2020-01-31 end
            }

            //学年格納
            knjCreateHidden($objForm, "GRADE-".$counter, $row["GRADE"]);

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            /*** テキストエリア ***/

            //評価
            $model->data["CONDUCT_EVAL"."-".$counter] = $row["CONDUCT_EVAL"];
            // Add by PP for PC-Talker 2020-01-20 start
            $name_show = $row["NAME_SHOW"];
            $comment = "全角で".$model->conduct_eval_moji."文字X".$model->conduct_eval_gyo."行";
            $extra = " onPaste=\"return showPaste(this);\" aria-label=\"".$attendno."".$name_show."の実施・評価$comment\"";
            // Add by PP for PC-Talker 2020-01-31 end
            $row["CONDUCT_EVAL"] = $row["CONDUCT_EVAL"];
            $value = (!isset($model->warning)) ? $row["CONDUCT_EVAL"] : $model->fields["CONDUCT_EVAL"][$counter];
            $row["CONDUCT_EVAL"] = KnjCreateTextArea($objForm, "CONDUCT_EVAL-".$counter, $model->conduct_eval_gyo, $model->conduct_eval_moji * 2 + 1, "soft", $extra, $value);

            //学期成績
            $model->data["GRAD_VALUE"."-".$counter] = $row["GRAD_VALUE"];
            $row["GRAD_VALUE"] = $row["GRAD_VALUE"];
    
            $value = (!isset($model->warning)) ? $row["GRAD_VALUE"] : $model->fields["GRAD_VALUE"][$counter];
            if ($model->Properties["useSpecial_Support_School"] == "1" && $model->field["SCHOOL_KIND"] == "P") {
                // Add by PP for PC-Talker 2020-01-20 start
                $extra = "id=\"GRADE-$counter\" onBlur=\"return tmpSet(this,'true', 'GRADE-$counter');\" STYLE=\"text-align:right;\" onPaste=\"return showPaste(this);\"  aria-label=\"".$attendno."".$name_show."の".$grade_result."\"";
            } else {
                $extra = "id=\"GRADE-$counter\" onBlur=\"return tmpSet(this,'false', 'GRADE-$counter');\" STYLE=\"text-align:right;\" onPaste=\"return showPaste(this);\"  aria-label=\"".$attendno."".$name_show."の".$grade_result."\"";
                // Add by PP for PC-Talker 2020-01-31 end
            }
            $row["GRAD_VALUE"] = knjCreateTextBox($objForm, $value, "GRAD_VALUE-".$counter, 3, 3, $extra);

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //レコード件数によりスライドバー表示分のタイトル幅を調整
        if ($counter > 2) {
            $arg["SCORE_WIDTH"] = "116";
        } else {
            $arg["SCORE_WIDTH"] = "100";
        }

        Query::dbCheckIn($db);

        // Add by PP for CurrentCursor 2020-01-20 start
        $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('update');\" aria-label=\"更新\""; 
        // Add by PP for CurrentCursor 2020-01-31 end
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        // Add by PP for CurrentCursor 2020-01-20 start
        $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('reset');\"  aria-label=\"取消\""; 
        // Add by PP for CurrentCursor 2020-01-31 end
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        // Add by PP for CurrentCursor 2020-01-20 start
        $extra = "id=\"btn_end\" onclick=\"closeWin();\"  aria-label=\"終了\"";
        // Add by PP for CurrentCursor 2020-01-31 end
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "conduct_eval_moji", $model->conduct_eval_moji);
        knjCreateHidden($objForm, "conduct_eval_gyo", $model->conduct_eval_gyo);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD143A");

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd143aindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd143aForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if($name == "SEMESTER"){
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
