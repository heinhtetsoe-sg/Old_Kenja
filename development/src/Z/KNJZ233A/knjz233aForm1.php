<?php

require_once('for_php7.php');

class knjz233aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz233aindex.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //選択ラジオボタン 1:考査 2:学期成績
        $opt = array(1, 2);
        $model->leftField["FLG"] = ($model->leftField["FLG"] == "") ? "1" : $model->leftField["FLG"];
        $extra = array("id=\"FLG1\" onClick=\"btn_submit('list');\"", "id=\"FLG2\" onClick=\"btn_submit('list');\"");
        $radioArray = knjCreateRadio($objForm, "FLG", $model->leftField["FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //学年課程学科コースコンボ
        $query = knjz233aQuery::getCourseMajor($model);
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_COURSE", $model->leftField["GRADE_COURSE"], $extra, 1, "BLANK");

        if ($model->leftField["FLG"] && $model->leftField["GRADE_COURSE"]) {
            //リスト作成
            makeList($arg, $db, $model);
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        if (VARS::post("cmd") == "list"){
            $arg["reload"] = "window.open('knjz233aindex.php?cmd=edit&PROGRAMID={PROGRAMID}','right_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz233aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$arg, $db, $model) {

    $sb_cnt = $sq_cnt = 1;
    $result = $db->query(knjz233aQuery::getList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //行数取得
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_cnt = $db->getOne(knjz233aQuery::getSubclassCnt($model, $row["COMBINED_CLASSCD"].'-'.$row["COMBINED_SCHOOL_KIND"].'-'.$row["COMBINED_CURRICULUM_CD"].'-'.$row["COMBINED_SUBCLASSCD"]));
            $seq_cnt      = $db->getOne(knjz233aQuery::getSeqCnt($model, $row["COMBINED_CLASSCD"].'-'.$row["COMBINED_SCHOOL_KIND"].'-'.$row["COMBINED_CURRICULUM_CD"].'-'.$row["COMBINED_SUBCLASSCD"], $row["SEQ"]));
        } else {
            $subclass_cnt = $db->getOne(knjz233aQuery::getSubclassCnt($model, $row["COMBINED_SUBCLASSCD"]));
            $seq_cnt      = $db->getOne(knjz233aQuery::getSeqCnt($model, $row["COMBINED_SUBCLASSCD"], $row["SEQ"]));
        }

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $row["COMBINED_SUBCLASSCD_LINK"] = View::alink("knjz233aindex.php",
                                 $row["COMBINED_CLASSCD"].'-'.$row["COMBINED_SCHOOL_KIND"].'-'.$row["COMBINED_CURRICULUM_CD"].'-'.$row["COMBINED_SUBCLASSCD"],
                                "target=right_frame",
                                array("cmd" => "edit",
                                      "SEND_FLG"                    => "SUB",
                                      "COMBINED_SUBCLASSCD_SEND"    =>  $row["COMBINED_CLASSCD"].'-'.$row["COMBINED_SCHOOL_KIND"].'-'.$row["COMBINED_CURRICULUM_CD"].'-'.$row["COMBINED_SUBCLASSCD"],
                                      "FLG_SEND"                    => $model->leftField["FLG"],
                                      "GRADE_COURSE_SEND"           => $model->leftField["GRADE_COURSE"]));
    
            $row["SEQ_LINK"] = View::alink("knjz233aindex.php",
                                $row["SEQ"],
                                "target=right_frame",
                                array("cmd" => "edit",
                                      "SEND_FLG"                    => "SEQ",
                                      "COMBINED_SUBCLASSCD_SEND"    =>  $row["COMBINED_CLASSCD"].'-'.$row["COMBINED_SCHOOL_KIND"].'-'.$row["COMBINED_CURRICULUM_CD"].'-'.$row["COMBINED_SUBCLASSCD"],
                                      "FLG_SEND"                    => $model->leftField["FLG"],
                                      "GRADE_COURSE_SEND"           => $model->leftField["GRADE_COURSE"],
                                      "SEQ_SEND"                    => $row["SEQ"]));
        } else {
            $row["COMBINED_SUBCLASSCD_LINK"] = View::alink("knjz233aindex.php",
                                $row["COMBINED_SUBCLASSCD"],
                                "target=right_frame",
                                array("cmd" => "edit",
                                      "SEND_FLG"                    => "SUB",
                                      "COMBINED_SUBCLASSCD_SEND"    => $row["COMBINED_SUBCLASSCD"],
                                      "FLG_SEND"                    => $model->leftField["FLG"],
                                      "GRADE_COURSE_SEND"           => $model->leftField["GRADE_COURSE"]));
    
            $row["SEQ_LINK"] = View::alink("knjz233aindex.php",
                                $row["SEQ"],
                                "target=right_frame",
                                array("cmd" => "edit",
                                      "SEND_FLG"                    => "SEQ",
                                      "COMBINED_SUBCLASSCD_SEND"    => $row["COMBINED_SUBCLASSCD"],
                                      "FLG_SEND"                    => $model->leftField["FLG"],
                                      "GRADE_COURSE_SEND"           => $model->leftField["GRADE_COURSE"],
                                      "SEQ_SEND"                    => $row["SEQ"]));
        }

        if ($sb_cnt == 1) $row["ROWSPAN1"] = ($subclass_cnt == 0) ? 1 : $subclass_cnt;  //科目の行数
        if ($sq_cnt == 1) $row["ROWSPAN2"] = ($seq_cnt == 0) ? 1 : $seq_cnt;    //ＳＥＱの行数

        $arg["data"][] = $row;

        if (($sb_cnt == $subclass_cnt) || ($subclass_cnt == 0)) {
            $sb_cnt = 1;
        } else {
            $sb_cnt++;
        }
        if (($sq_cnt == $seq_cnt) || ($seq_cnt == 0)) {
            $sq_cnt = 1;
        } else {
            $sq_cnt++;
        }
    }
    $result->free();
}
?>
