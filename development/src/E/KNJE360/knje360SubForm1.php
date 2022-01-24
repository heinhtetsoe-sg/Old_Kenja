<?php

require_once('for_php7.php');

class knje360SubForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje360index.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $info = $db->getRow(knje360Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //データ一覧取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $query = knje360Query::getSubQuery1List($model);
        $cnt = get_count($db->getcol($query));
        if ($model->schregno && $cnt) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //進路種別
                $row["COURSE_KIND_NAME"] = "";
                $coursekind = array('','1：進学', '2：就職','3：その他（家事手伝い等）','4：未定');
                foreach ($coursekind as $key => $val) {
                    if ($row["COURSE_KIND"] == $key) {
                        $row["COURSE_KIND_NAME"] = $val;
                    }
                }

                $row["CONTENTS"] = "";
                if ($row["COURSE_KIND"] == "1") {
                    //学校名
                    $sch_array = $db->getAssoc(knje360Query::getSchoolName($model, $row["ENTRYDATE"], $row["SEQ"]));
                    for ($i=1; $i<=$model->kibouCnt; $i++) {
                        if ($i < 3) {
                            if (strlen($row["SCHOOL_NAME".$i]) > 0) {
                                $row["CONTENTS"] .= ($row["CONTENTS"]) ? '→'.$row["SCHOOL_NAME".$i] : $row["SCHOOL_NAME".$i];
                            }
                        } else {
                            if (strlen($sch_array[$i]) > 0) {
                                $row["CONTENTS"] .= ($row["CONTENTS"]) ? '→'.$sch_array[$i] : $sch_array[$i];
                            }
                        }
                    }
                } elseif ($row["COURSE_KIND"] == "2") {
                    //職業種別
                    for ($i=1; $i<3; $i++) {
                        if (strlen($row["JOBTYPE_LNAME".$i]) > 0) {
                            $row["CONTENTS"] .= ($row["CONTENTS"]) ? '→'.$row["JOBTYPE_LNAME".$i] : $row["JOBTYPE_LNAME".$i];
                        }
                    }
                }

                $row["ENTRYDATE"] = str_replace("-", "/", $row["ENTRYDATE"]);

                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {
                    $arg["list"][] = $setval;
                    $setval = $row;
                }
            }

            $arg["list"][] = $setval;
        }

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1A" || $model->cmd == "subform1_clear") {
            if (isset($model->schregno) && !isset($model->warning) && $model->entrydate && $model->seq) {
                $Row = $db->getRow(knje360Query::getSubQuery1($model, $model->entrydate), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //登録日
        $Row["ENTRYDATE"] = ($Row["ENTRYDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["ENTRYDATE"]);
        $arg["data"]["ENTRYDATE"] = View::popUpCalendar($objForm, "ENTRYDATE", $Row["ENTRYDATE"]);

        //進路種別
        $disable = ($Row["COURSE_KIND"] == "") ? "0" : $Row["COURSE_KIND"];
        $coursekind = array('','1：進学', '2：就職','3：その他（家事手伝い等）','4：未定');
        foreach ($coursekind as $key => $val) {
            $key = ($key == "0") ? "" : $key;
            $opt_course[] = array('label' => $val, 'value' => $key);
        }
        $extra = "onchange=\"return btn_submit('subform1');\"";
        $arg["data"]["COURSE_KIND"] = knjCreateCombo($objForm, "COURSE_KIND", $Row["COURSE_KIND"], $opt_course, $extra, 1);

        $arg["show_remark"] = 1;
        if ($disable == "1") {
            $arg["college"] = 1;
            $colspan = ' colspan="2"';
            if ($model->Properties["Show_Recommendation"] == "1") {
                $arg["show_remark"] = "";
            }
        } elseif ($disable == "2") {
            $arg["job"] = 2;
            $colspan = ' colspan="2"';
        } else {
            $colspan = '';
        }

        $arg["data"]["ITEM1"] = '<th align="center" class="no_search" nowrap width="130"'.$colspan.'>登録日</th>';
        $arg["data"]["ITEM2"] = '<th align="center" class="no_search" nowrap width="130"'.$colspan.'>進路種別</th>';
        $arg["data"]["ITEM3"] = '<th align="center" class="no_search" nowrap width="130"'.$colspan.'>調査名</th>';
        $arg["data"]["ITEM4"] = '<th align="center" class="no_search" nowrap width="130"'.$colspan.'>備考</th>';

        //調査名
        $query = knje360Query::getQuestionnaireList();
        makeCmb($objForm, $arg, $db, $query, "QUESTIONNAIRECD", $Row["QUESTIONNAIRECD"], "", 1, 1);

        //備考
        $extra = "style=\"height:75px;\"";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", 5, 41, "soft", $extra, $Row["REMARK"]);
        $Row0 = $Row;

        /*=======================進　学=======================*/
        if ($model->Properties["Show_Recommendation"] == "1") {
            $arg["Show_Recommendation"] = 1;
        } else {
            $arg["Not_Show_Recommendation"] = 1;
        }
        for ($i=1; $i<=$model->kibouCnt; $i++) {
            $tmp = array();
            if ($i > 2) {
                //警告メッセージを表示しない場合
                if ($model->cmd == "subform1A" || $model->cmd == "subform1_clear") {
                    if (isset($model->schregno) && !isset($model->warning) && $model->entrydate && $model->seq) {
                        $RowD = $db->getRow(knje360Query::getSubQuery1Detail($model, $i), DB_FETCHMODE_ASSOC);
                    } else {
                        $RowD =& $model->field;
                    }
                } else {
                    $RowD =& $model->field;
                }
                $Row = $RowD;
            }

            if ($model->Properties["Show_Recommendation"] == "1") {
                $tmp["HOPE_NUM_LABEL"] = '第'.$i.'希望';
            } else {
                $tmp["HOPE_NUM_LABEL"] = '第<br>'.common::PubFncKnjNumeral($i, 0).'<br>希<br>望';
            }

            //学校系列
            $query = knje360Query::getNameMst('E012');
            $tmp["SCHOOL_GROUP"] = makeCmb2($objForm, $arg, $db, $query, "SCHOOL_GROUP".$i, $Row["SCHOOL_GROUP".$i], "", 1, 1);

            //学部系列
            $query = knje360Query::getFacultyGroup();
            $tmp["FACULTY_GROUP"] = makeCmb2($objForm, $arg, $db, $query, "FACULTY_GROUP".$i, $Row["FACULTY_GROUP".$i], "", 1, 1);

            //学科系列
            $query = knje360Query::getDepartmentGroup();
            $tmp["DEPARTMENT_GROUP"] = makeCmb2($objForm, $arg, $db, $query, "DEPARTMENT_GROUP".$i, $Row["DEPARTMENT_GROUP".$i], "", 1, 1);

            //学校情報
            $college1 = $db->getRow(knje360Query::getCollegeInfo($Row["SCHOOL_CD".$i], $Row["FACULTYCD".$i], $Row["DEPARTMENTCD".$i]), DB_FETCHMODE_ASSOC);

            //学校コード
            $tmp["SCHOOL_CD"] = $college1["SCHOOL_CD"];

            //学校名
            $tmp["SCHOOL_NAME"] = $college1["SCHOOL_NAME"];

            //学部名
            $tmp["FACULTYNAME"] = $college1["FACULTYNAME"];

            //学科名
            $tmp["DEPARTMENTNAME"] = $college1["DEPARTMENTNAME"];

            knjCreateHidden($objForm, "SCHOOL_CD".$i, $Row["SCHOOL_CD".$i]);
            knjCreateHidden($objForm, "FACULTYCD".$i, $Row["FACULTYCD".$i]);
            knjCreateHidden($objForm, "DEPARTMENTCD".$i, $Row["DEPARTMENTCD".$i]);

            //受験区分
            $query = knje360Query::getNameMst('E002');
            $tmp["HOWTOEXAM"] = makeCmb2($objForm, $arg, $db, $query, "HOWTOEXAM".$i, $Row["HOWTOEXAM".$i], "", 1, 1);

            //学校検索ボタン
            $extra = "onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=&target_number={$i}',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
            $tmp["btn_schsearch"] = knjCreateBtn($objForm, "btn_schsearch".$i, "学校検索", $extra);

            $tmp["ID"] = $i;

            $arg["coll"][] = $tmp;
        }

        /*=======================就　職=======================*/
        /**第一希望**/

        //職業種別（大分類）
        $query = knje360Query::getJobtypeLList();
        makeCmb($objForm, $arg, $db, $query, "JOBTYPE_LCD1", $Row0["JOBTYPE_LCD1"], "", 1, 1);

        //職業種別（小分類）
        $arg["data"]["JOBTYPE_SNAME1"] = $Row0["JOBTYPE_SNAME1"];

        //就業場所
        $opt_area1[] = array('label' => "", 'value' => "");
        $opt_area1[] = array('label' => "1：県内", 'value' => "1");
        $opt_area1[] = array('label' => "2：県外", 'value' => "2");
        $arg["data"]["WORK_AREA1"] = knjCreateCombo($objForm, "WORK_AREA1", $Row0["WORK_AREA1"], $opt_area1, "", 1);

        //紹介区分ラジオボタン 1:学校紹介 2:自己・縁故 3.公務員
        $opt_intro1 = array(1, 2, 3);
        $Row0["INTRODUCTION_DIV1"] = ($Row0["INTRODUCTION_DIV1"] == "") ? "1" : $Row0["INTRODUCTION_DIV1"];
        $extra = array("id=\"INTRODUCTION_DIV11\"", "id=\"INTRODUCTION_DIV12\"", "id=\"INTRODUCTION_DIV13\"");
        $radioArray = knjCreateRadio($objForm, "INTRODUCTION_DIV1", $Row0["INTRODUCTION_DIV1"], $extra, $opt_intro1, get_count($opt_intro1));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /**第ニ希望**/

        //職業種別（大分類）
        $query = knje360Query::getJobtypeLList();
        makeCmb($objForm, $arg, $db, $query, "JOBTYPE_LCD2", $Row0["JOBTYPE_LCD2"], "", 1, 1);

        //職業種別（小分類）
        $arg["data"]["JOBTYPE_SNAME2"] = $Row0["JOBTYPE_SNAME2"];

        //就業場所
        $opt_area2[] = array('label' => "", 'value' => "");
        $opt_area2[] = array('label' => "1：県内", 'value' => "1");
        $opt_area2[] = array('label' => "2：県外", 'value' => "2");
        $arg["data"]["WORK_AREA2"] = knjCreateCombo($objForm, "WORK_AREA2", $Row0["WORK_AREA2"], $opt_area2, "", 1);

        //紹介区分ラジオボタン 1:学校紹介 2:自己・縁故 3.公務員
        $opt_intro2 = array(1, 2, 3);
        $Row0["INTRODUCTION_DIV2"] = ($Row0["INTRODUCTION_DIV2"] == "") ? "1" : $Row0["INTRODUCTION_DIV2"];
        $extra = array("id=\"INTRODUCTION_DIV21\"", "id=\"INTRODUCTION_DIV22\"", "id=\"INTRODUCTION_DIV23\"");
        $radioArray = knjCreateRadio($objForm, "INTRODUCTION_DIV2", $Row0["INTRODUCTION_DIV2"], $extra, $opt_intro2, get_count($opt_intro2));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row0);

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning)== 0 && $model->cmd !="subform1_clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="subform1_clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360SubForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //職業検索ボタンを作成する
    $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_JOB/knjxjob_searchindex.php?cmd=&target_number=1', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 380, 350)\"";
    $arg["button"]["btn_jobsearch1"] = knjCreateBtn($objForm, "btn_jobsearch1", "職業検索", $extra);
    $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_JOB/knjxjob_searchindex.php?cmd=&target_number=2', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 380, 350)\"";
    $arg["button"]["btn_jobsearch2"] = knjCreateBtn($objForm, "btn_jobsearch2", "職業検索", $extra);

    $disabled = ($model->mode == "grd") ? " disabled" : "";
    //一括更新（進学）ボタン
    $link = REQUESTROOT."/E/KNJE360/knje360index.php?cmd=replace1&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "一括更新\n（進学）", $extra.$disabled);

    //追加ボタンを作成する
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", "onclick=\"return btn_submit('subform1_insert');\"");
    //追加後前の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
    $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "追加後前の{$model->sch_label}へ", $extra.$disabled);
    //追加後次の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "追加後次の{$model->sch_label}へ", $extra.$disabled);
    //更新ボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('subform1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('subform1_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");

    //進路相談ボタン
    $extra = "style=\"height:30px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('subform4');\"";
    $arg["button"]["btn_subform4"] = KnjCreateBtn($objForm, "btn_subform4", "進路相談", $extra.$disabled);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);

    knjCreateHidden($objForm, "JOBTYPE_MCD1", $Row["JOBTYPE_MCD1"]);
    knjCreateHidden($objForm, "JOBTYPE_SCD1", $Row["JOBTYPE_SCD1"]);
    knjCreateHidden($objForm, "JOBTYPE_MCD2", $Row["JOBTYPE_MCD2"]);
    knjCreateHidden($objForm, "JOBTYPE_SCD2", $Row["JOBTYPE_SCD2"]);

    $semes = $db->getRow(knje360Query::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
