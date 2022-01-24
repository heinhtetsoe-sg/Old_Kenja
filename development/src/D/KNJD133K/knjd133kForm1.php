<?php

require_once('for_php7.php');


class knjd133kForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        if ($model->cmd == "back") {
            $model->field["SUBCLASSCD"] = $model->subclasscd;
            $model->field["CHAIRCD"]    = $model->chaircd;
        }

        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        if (CTRL_YEAR > '2011') {
            $result = $db->query(knjd133kQuery::selectSubclassQuery($model, $model->gen_ed));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
            }
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["SUBCLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('subclasscd')\";"));
        $arg["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd133kQuery::selectChairQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"]);
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('chaircd')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");

        //ALLチェック(単位自動)
        $extra = "onClick=\"return check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra);

        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }

        //初期化
        $model->data=array();
        $counter=0;

        //一覧表示
        $colorFlg = false;
        $query = knjd133kQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //クラス-出席番(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            /*** テキストエリア ***/

            //学習内容
            $row["TOTALSTUDYACT_TYUI"] = "(全角で {$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行)";
            $model->data["TOTALSTUDYACT"."-".$counter] = $row["TOTALSTUDYACT"];
            if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
                $extra = "style=\"height:65px;background-color:#D0D0D0;\" readonly";
            } else {
                $extra = "style=\"height:65px;\" onPaste=\"return showPaste(this);\"";
                $row["TOTALSTUDYACT"] = $model->cmd != "csvInputMain" ? $row["TOTALSTUDYACT"] : $model->data_arr[$row["SCHREGNO"]]["TOTALSTUDYACT"];
            }
            $value = (!isset($model->warning)) ? $row["TOTALSTUDYACT"] : $model->fields["TOTALSTUDYACT"][$counter];

            $row["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT-".$counter, $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", $extra, $value);

            //評価
            $row["TOTALSTUDYTIME_TYUI"] = "(全角で {$model->totalstudytime_moji}文字{$model->totalstudytime_gyou}行)";
            $model->data["TOTALSTUDYTIME"."-".$counter] = $row["TOTALSTUDYTIME"];
            if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
                $extra = "style=\"height:85px;background-color:#D0D0D0;\" readonly";
            } else {
                $extra = "style=\"height:85px;\" onPaste=\"return showPaste(this);\"";
                $row["TOTALSTUDYTIME"] = $model->cmd != "csvInputMain" ? $row["TOTALSTUDYTIME"] : $model->data_arr[$row["SCHREGNO"]]["TOTALSTUDYTIME"];
            }
            $value = (!isset($model->warning)) ? $row["TOTALSTUDYTIME"] : $model->fields["TOTALSTUDYTIME"][$counter];

            $row["TOTALSTUDYTIME"] = KnjCreateTextArea($objForm, "TOTALSTUDYTIME-".$counter, $model->totalstudytime_gyou, ($model->totalstudytime_moji * 2 + 1), "soft", $extra, $value);

            /*** チェックボックス ***/

            //単位自動・・・チェックありの場合、単位マスタの単位数をセットし更新
            if (isset($model->warning) && $model->fields["CHK_CALC_CREDIT"][$counter] == "on") {
                $extra = "checked";
            }
            if ($row["GRADE_RECORD"] != "") {
                $extra = "checked";
            } else {
                $extra = "";
            }
            $row["CHK_CALC_CREDIT"] = knjCreateCheckBox($objForm, "CHK_CALC_CREDIT-".$counter, "on", $extra);

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //ファイル
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        Query::dbCheckIn($db);

        //ボタン
        $extra = "onclick=\"return btn_submit('csvInput');\"";
        $arg["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra);

        $extra = "onclick=\"return btn_submit('csvOutput');\"";
        $arg["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra);

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ));
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae(array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));
        knjCreateHidden($objForm, "totalstudyact_gyou", $model->totalstudyact_gyou);
        knjCreateHidden($objForm, "totalstudyact_moji", $model->totalstudyact_moji);
        knjCreateHidden($objForm, "totalstudytime_gyou", $model->totalstudytime_gyou);
        knjCreateHidden($objForm, "totalstudytime_moji", $model->totalstudytime_moji);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd133kindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML2($model, "knjd133kForm1.html", $arg);
    }
}
