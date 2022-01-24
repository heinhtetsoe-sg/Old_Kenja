<?php

require_once('for_php7.php');


class knjd133mForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd133mQuery::selectSubclassQuery($model, $model->gen_ed));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
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
        $result = $db->query(knjd133mQuery::selectChairQuery($model));
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

        $extra  = " onClick=\"return check_all(this)\" ";
        $extra .= $model->ALL_FLG == "1" ? " checked " : "";
        $arg["ALL_FLG"] = knjCreateCheckBox($objForm, "ALL_FLG", "1", $extra);

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
        $query = knjd133mQuery::selectQuery($model, $execute_date);
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

            //総合的な学習の時間
            $model->data["TOTALSTUDYTIME"."-".$counter] = $row["TOTALSTUDYTIME"];
            $row["TOTALSTUDYTIME"] = $model->cmd != "csvInputMain" ? $row["TOTALSTUDYTIME"] : $model->data_arr[$row["SCHREGNO"]]["TOTALSTUDYTIME"];
            $value = (!isset($model->warning)) ? $row["TOTALSTUDYTIME"] : $model->fields["TOTALSTUDYTIME"][$counter];
            $extra = "style=\"height:64px;\" onPaste=\"return showPaste(this);\"";
            $row["TOTALSTUDYTIME"] = KnjCreateTextArea($objForm, "TOTALSTUDYTIME-".$counter, 4, 82, "soft", $extra, $value);

            //活動記録
            $model->data["TOTALSTUDYACT"."-".$counter] = $row["TOTALSTUDYACT"];
            $row["TOTALSTUDYACT"] = $model->cmd != "csvInputMain" ? $row["TOTALSTUDYACT"] : $model->data_arr[$row["SCHREGNO"]]["TOTALSTUDYACT"];
            $value = (!isset($model->warning)) ? $row["TOTALSTUDYACT"] : $model->fields["TOTALSTUDYACT"][$counter];
            $extra = "style=\"height:36px;\" onPaste=\"return showPaste(this);\"";
            $row["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT-".$counter, 2, 82, "soft", $extra, $value);

            //レコードがない場合は「checked」とする
            if (isset($model->warning)) {
                $row["TOTALSTUDYTIME_FLG"] = $model->fields["TOTALSTUDYTIME_FLG"][$counter];
            }
            $extra = ($row["TOTALSTUDYTIME_FLG"] == "1") ? "checked='checked' " : "";
            $row["TOTALSTUDYTIME_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYTIME_FLG-".$counter, "1", $extra);

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
                            
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd133mindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd133mForm1.html", $arg);
    }
}
