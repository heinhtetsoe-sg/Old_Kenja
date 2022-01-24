<?php

require_once('for_php7.php');
class knja031Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja031index.php", "", "edit");

        //コントロールマスタの学籍処理年度で学年進行データを検索。
        $db     = Query::dbCheckOut();

        $result = $db->query(knja031Query::gradeClassQuery());
        //コンボボックスの中身を作成
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["HR_NAME"],
                           "value" => $row["GC"]);
            if (!isset($model->gc_select)) {
                $model->gc_select = $row["GC"];
            }
            if ($model->gc_select == $row["GC"]) {
                //担任
                $arg["teacher"] = $row["STAFFNAME_SHOW"];
            }
        }

        $result->free();

        $objForm->ae(array("type"          => "select",
                            "name"          => "gc_select",
                            "size"          => "1",
                            "value"         => $model->gc_select,
                            "extrahtml"     => "onChange=\"return btn_submit('main');\"",
                            "options"       => $opt));

        $arg["gc_select"] = $objForm->ge("gc_select");

        //処理年度と処理学期
        $arg["TOP"] = array("TRANSACTION"   => CTRL_YEAR,
                            "SEMESTER"      => $model->control_data["学期名"][CTRL_SEMESTER],
                            "NEXTTRANSACTION"   => (int) CTRL_YEAR+1,
                            );

        $objForm->ae(array("type"   => "checkbox",
                        "name"      => "CHECKALL",
                        "extrahtml" => "onClick=\"return check_all(this);\"" ));

        $order[1] = "▲";
        $order[-1] = "▼";

        $arg["CHECKALL"] = $objForm->ge("CHECKALL");
        $arg["ATTENDNO"] = View::alink(
            "knja031index.php",
            "<font color=\"white\">年-組-番</font>",
            "",
            array("cmd"=>"sort", "sort"=>"ATTENDNO")
        ) .$order[$model->sort["ATTENDNO"]];
        $arg["TARGET"] = View::alink(
            "knja031index.php",
            "<font color=\"white\">対象</font>",
            "",
            array("cmd"=>"sort", "sort"=>"TARGET")
        ) .$order[$model->sort["TARGET"]];

        //次年度の学期マスタが存在するか
        $query = knja031Query::selectSemesterMst();
        if (!is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $arg["Closing"] = "  closing_window('MSG305'); " ;
        }
        //学校マスタ
        $query = knja031Query::selectSchoolMst();
        $model->school = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($model->school["SEMESTERDIV"] != CTRL_SEMESTER) {
            $arg["Closing"] = "  closing_window('MSG311'); " ;
        }
        //----------------------以下、擬似フレーム内リスト表示----------------------
        $query = knja031Query::readQuery($model);
        $result = $db->query($query);
        $student_sum = 0; //合計人数
        $summery      = array(); //SHIFTCD別の合計
        $summery["promoted"] = $summery["graduated"] = $summery["remained"] = 0;
        //取得保留単位をカンマ区切りで保持
        $g_c = $r_c = $sp = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $objForm->ae(array("type"        => "checkbox",
                                "name"        => "CHECKED",
                                "value"       => $row["SCHREGNO"],
                                "extrahtml"   => "onclick=\"chkClick(this)\" tabindex=\"-1\"",
                                "multiple"    => "1" ));

            $row["CHECKED"] = $objForm->ge("CHECKED");
            if ($row["REMAINGRADE_FLG"] == '0') {
                $row["TARGET"] = "進級";
                $summery["promoted"]++;
            } elseif ($row["REMAINGRADE_FLG"] == '1') {
                $row["TARGET"] = "留年";
                $summery["remained"]++;
            } elseif ($row["GRD_DIV"] == '1') {
                $row["TARGET"] = "卒業";
                $summery["graduated"]++;
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

        //年組分離
        list($grade, $hr_class) = explode(",", $model->gc_select);
        $opt = array();
        if ((int) $model->school["GRADE_HVAL"] > (int) $grade) {
            $opt[] = array("label" => "1:進級",
                           "value" => 1 );
        }
        if (($model->school["SCHOOLDIV"] == "0" && (int) $model->school["GRADE_HVAL"] == (int) $grade) || $model->school["SCHOOLDIV"] == "1") {
            $opt[] = array("label" => "2:卒業",
                           "value" => 2 );
        }
        $opt[] = array("label" => "3:留年",
                       "value" => 3 );
        $opt[] = array("label" => "9:取消",
                       "value" => 9 );

        //処理コンボ
        $objForm->ae(array("type"          => "select",
                            "name"          => "TRANS",
                            "size"          => "1",
                            "value"         => $model->trans,
                            "extrahtml"     => "onchange=\"chgTrans(this)\"",
                            "options"       => $opt));

        $arg["TRANS"] = $objForm->ge("TRANS");
        //hidden
        $objForm->ae(array("type"          => "hidden",
                            "name"          => "GET_CREDITS",
                            "value"         => $g_c
                            ));
        //hidden
        $objForm->ae(array("type"          => "hidden",
                            "name"          => "REM_CREDITS",
                            "value"         => $r_c
                            ));
        //hidden
        $objForm->ae(array("type"          => "hidden",
                            "name"          => "SCHOOLDIV",
                            "value"         => $model->school["SCHOOLDIV"]
                            ));
        //hidden
        $objForm->ae(array("type"          => "hidden",
                            "name"          => "GRADE_HVAL",
                            "value"         => $model->school["GRADE_HVAL"]
                            ));

        //hidden
        $objForm->ae(array("type"          => "hidden",
                            "name"          => "GRADE_FEARVAL",
                            "value"         => $model->school["GRADE_FEARVAL"]
                            ));

        //hidden
        $objForm->ae(array("type"          => "hidden",
                            "name"          => "GRAD_CREDITS",
                            "value"         => $model->school["GRAD_CREDITS"]
                            ));

        $result->free();

        Query::dbCheckIn($db);
        //取り消しの場合は仮判定ボタンを押下不可とする
        if ($model->trans == 9) {
            $disabled = "disabled";
        }
        //仮判定ボタン
        $objForm->ae(array("type"          => "button",
                            "name"          => "btn_judge",
                            "value"         => "仮判定",
                            "extrahtml"     => "$disabled onclick=\"return btn_read();\"" ));
        $arg["btn_judge"] = $objForm->ge("btn_judge");

        //更新ボタン
        $objForm->ae(array("type"          => "button",
                            "name"          => "btn_update",
                            "value"         => "更 新",
                            "extrahtml"     => "onclick=\"return btn_submit('update');\"" ));
        $arg["btn_update"] = $objForm->ge("btn_update");

        //取消ボタン
        $objForm->ae(array("type"          => "reset",
                            "name"          => "btn_can",
                            "value"         => "取 消",
                            "extrahtml"     => "onclick=\"return btn_submit('reset');\"" ));
        $arg["btn_can"] = $objForm->ge("btn_can");

        //終了ボタン
        $objForm->ae(array("type"          => "button",
                            "name"          => "btn_end",
                            "value"         => "終 了",
                            "extrahtml"     => " onclick=\"return closeWin();\"" ));
        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae(array("type"          => "hidden",
                            "name"          => "cmd"));


        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja031Form1.html", $arg);
    }
}
