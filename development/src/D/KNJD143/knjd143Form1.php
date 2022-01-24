<?php

require_once('for_php7.php');


class knjd143Form1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        if ($model->cmd == "back") {
            $model->field["SUBCLASSCD"] = $model->subclasscd;
            $model->field["CHAIRCD"]    = $model->chaircd;
        }

        //学期コンボ
        if ($model->field["SEMESTER"] == '') {
            $model->field["SEMESTER"] = CTRL_SEMESTER;
        }
        $opt_seme = array();
        $query = knjd143Query::selectSemesterQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_seme[] = array("label" => $row["SEMESTER"].":".$row["SEMESTERNAME"],"value" => $row["SEMESTER"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEMESTER",
                            "size"        => "1",
                            "value"       => $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER,
                            "options"     => $opt_seme,
                            "extrahtml"   => "onChange=\"btn_submit('semester')\";"
                           ));
        $arg["SEMESTER"] = $objForm->ge("SEMESTER");

        $model->isLastSemester = knjd143Query::isLastSemester($db, $model->field["SEMESTER"]);
        if ($model->isLastSemester) {
            $arg["SCORE_TITLE"] = "学年<br>成績";
        } else {
            $query = knjd143Query::getTestitemMstCountflgTestitemname($model, $model->field["SEMESTER"]);
            $arg["SCORE_TITLE"] = $db->getOne($query);
        }

        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $query = knjd143Query::selectSubclassQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["SUBCLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('subclasscd')\";"));
        $arg["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");


        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $query = knjd143Query::selectChairQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('chaircd')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");

        //ALLチェック(単位自動)
        //$extra = "onClick=\"return check_all(this);\"";
        //$arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra);

        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        $sdate = str_replace("/","-",$model->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/","-",$model->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }

        //定型文格納
        $tmpArray = array();
        $convert = array("A" => "5", "B" => "4", "C" => "3", "D" => "2", "E" => "1");
        $query = knjd143Query::getHtrainremarkTempDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmpArray[$row["GRADE"].'_'.$convert[$row["PATTERN_CD"]]] = $row["REMARK"];
            knjCreateHidden($objForm, "TMP-".$row["GRADE"]."-".$convert[$row["PATTERN_CD"]], $row["REMARK"]);
        }

        $arg["conduct_contents_moji"] = $model->conduct_contents_moji;
        $arg["conduct_contents_gyo"] = $model->conduct_contents_gyo;
        $arg["conduct_eval_moji"] = $model->conduct_eval_moji;
        $arg["conduct_eval_gyo"] = $model->conduct_eval_gyo;

        //初期化
        $model->data=array();
        $counter=0;

        //一覧表示
        $colorFlg = false;
        $query = knjd143Query::selectQuery($model, $execute_date);
        $result = $db->query($query);
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
            }

            //学年格納
            knjCreateHidden($objForm, "GRADE-".$counter, $row["GRADE"]);

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            /*** テキストエリア ***/

            //学習内容
            $model->data["CONDUCT_CONTENTS"."-".$counter] = $row["CONDUCT_CONTENTS"];
            $extra = "style=\"height:150px;\" onPaste=\"return showPaste(this);\"";
            $row["CONDUCT_CONTENTS"] = $model->cmd != "csvInputMain" ? $row["CONDUCT_CONTENTS"] : $model->data_arr[$row["SCHREGNO"]]["CONDUCT_CONTENTS"];
            $value = (!isset($model->warning)) ? $row["CONDUCT_CONTENTS"] : $model->fields["CONDUCT_CONTENTS"][$counter];
            $row["CONDUCT_CONTENTS"] = KnjCreateTextArea($objForm, "CONDUCT_CONTENTS-".$counter, $model->conduct_contents_gyo, $model->conduct_contents_moji * 2 + 1, "soft", $extra, $value);

            //評価
            $model->data["CONDUCT_EVAL"."-".$counter] = $row["CONDUCT_EVAL"];
            $extra = "style=\"height:150px;\" onPaste=\"return showPaste(this);\"";
            $row["CONDUCT_EVAL"] = $model->cmd != "csvInputMain" ? $row["CONDUCT_EVAL"] : $model->data_arr[$row["SCHREGNO"]]["CONDUCT_EVAL"];
            $value = (!isset($model->warning)) ? $row["CONDUCT_EVAL"] : $model->fields["CONDUCT_EVAL"][$counter];
            $row["CONDUCT_EVAL"] = KnjCreateTextArea($objForm, "CONDUCT_EVAL-".$counter, $model->conduct_eval_gyo, $model->conduct_eval_moji * 2 + 1, "soft", $extra, $value);

            /*** チェックボックス ***/

            //学期成績
            $model->data["GRAD_VALUE"."-".$counter] = $row["GRAD_VALUE"];
            $row["GRAD_VALUE"] = $model->cmd != "csvInputMain" ? $row["GRAD_VALUE"] : $model->data_arr[$row["SCHREGNO"]]["GRAD_VALUE"];
            $value = (!isset($model->warning)) ? $row["GRAD_VALUE"] : $model->fields["GRAD_VALUE"][$counter];
            $extra = " onBlur=\"return tmpSet(this);\" STYLE=\"text-align:right;\" onPaste=\"return showPaste(this);\"";
            $row["GRAD_VALUE"] = knjCreateTextBox($objForm, $value, "GRAD_VALUE-".$counter, 3, 3, $extra);

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);

        //ボタンを作成
        //$extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        //$arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "伝 票", $extra);

        $extra = "onclick=\"return btn_submit('update');\""; 
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('reset');\""; 
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "showTemp04", $model->Properties["showTemp04"]);
        knjCreateHidden($objForm, "conduct_contents_moji", $model->conduct_contents_moji);
        knjCreateHidden($objForm, "conduct_contents_gyo", $model->conduct_contents_gyo);
        knjCreateHidden($objForm, "conduct_eval_moji", $model->conduct_eval_moji);
        knjCreateHidden($objForm, "conduct_eval_gyo", $model->conduct_eval_gyo);
        // 帳票用パラメータ
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD143");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "PRINT_SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "PRINT_SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->Properties["selectSchoolKind"]);

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd143index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd143Form1.html", $arg);
    }
}
?>
