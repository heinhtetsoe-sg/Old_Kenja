<?php

require_once('for_php7.php');

class knjz183Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz183index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学年コンボボックス
        $opt = array();
        $value_flg = false;
        $grd_cnt = 0;
        $query = knjz183Query::getGrade();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($model->grade == $row["VALUE"]) $value_flg = true;
            $grd_cnt++;
        }
        $result->free();
        $model->grade = ($model->grade != "" && $value_flg) ? $model->grade : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('grade');\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $opt, $extra, 1);

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjz183Query::getSchoolKind($model));

        //前年度コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //項目名
        if ($model->schoolkind === 'H') {
            $arg["KOUMOKU_NAME"] = 'コースグループ';
        } else {
            $arg["KOUMOKU_NAME"] = 'コース';
        }
        
        //リスト表示
        $query  = knjz183Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            
            if ($model->schoolkind === 'H') {
                $row["GROUP"] = $row["MAJORCD"].':'.$row["GROUP_NAME"];
            } else {
                $row["GROUP"] = $row["COURSECD"].'-'.$row["MAJORCD"].'-'.$row["COURSECODE"].':'.$row["COURSENAME"].' '.$row["COURSECODENAME"];
            }
            $row["SUBCLASS"] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"].':'.$row["SUBCLASSNAME"];
            $row["RATE"] = (int) $row["RATE"];

            //リンク作成
            if ($model->schoolkind === 'H') {
                $row["SUBCLASS"] = View::alink("knjz183index.php", $row["SUBCLASS"], "target=\"right_frame\"",
                                                  array("cmd"           => "edit",
                                                        "GRADE"         => $model->grade,
                                                        "GROUP_CD"      => $row["MAJORCD"],
                                                        "SUBCLASS"      => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"],
                                                        "RATE"          => $row["RATE"]
                                                        ));
            } else {
                $row["SUBCLASS"] = View::alink("knjz183index.php", $row["SUBCLASS"], "target=\"right_frame\"",
                                                  array("cmd"           => "edit",
                                                        "GRADE"         => $model->grade,
                                                        "COURSE_MAJOR"  => $row["COURSECD"].'-'.$row["MAJORCD"].'-'.$row["COURSECODE"],
                                                        "SUBCLASS"      => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"],
                                                        "RATE"          => $row["RATE"]
                                                        ));
            }

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT) {
            $arg["Closing"] = " closing_window('0'); " ;
        }
        //事前処理チェック（学年）
        if ($grd_cnt == 0) {
            $arg["Closing"] = " closing_window('1'); " ;
        }

        if ($model->cmd == "grade" || $model->cmd == "list") {
            $arg["reload"] = "window.open('knjz183index.php?cmd=edit&GRADE={$model->grade}','right_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz183Form1.html", $arg);
    }
}    
?>
