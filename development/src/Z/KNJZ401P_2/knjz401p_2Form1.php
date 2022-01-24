<?php

require_once('for_php7.php');

class knjz401p_2Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz401p_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期数取得
        $model->getSemesterCount = $db->getOne(knjz401p_2Query::getSemester());
        $arg["SET_NAME1"] = $db->getOne(knjz401p_2Query::getSemester("1"));
        $arg["SET_NAME2"] = $db->getOne(knjz401p_2Query::getSemester("2"));
        if ($model->getSemesterCount > 2) {
            $arg["SEMESTER3"] = "1";
            $arg["SET_NAME3"] = $db->getOne(knjz401p_2Query::getSemester("3"));
        }
        //対象学年コンボ
        $query = knjz401p_2Query::getGrade();
        $extra = "onchange=\"return btn_submit('grade');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1);

        //参照学年コンボ
        $query = knjz401p_2Query::getGrade($model->grade);
        makeCmb($objForm, $arg, $db, $query, "R_GRADE", $model->field["R_GRADE"], "", 1);

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の学年のデータをコピー", $extra);

        //一覧取得
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $query = knjz401p_2Query::getJViewNameList($model->grade, $model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $sep1 = (strlen($row["STUDYREC_CLASSCD1"].$row["STUDYREC_SCHOOL_KIND1"].$row["STUDYREC_CURRICULUM_CD1"].$row["STUDYREC_SUBCLASSCD1"]) > 0) ? '-' : '';
                $row["STUDYREC_SUBCLASSCD1"] = $row["STUDYREC_CLASSCD1"].$sep1.$row["STUDYREC_SCHOOL_KIND1"].$sep1.$row["STUDYREC_CURRICULUM_CD1"].$sep1.$row["STUDYREC_SUBCLASSCD1"];
                $sep2 = (strlen($row["STUDYREC_CLASSCD2"].$row["STUDYREC_SCHOOL_KIND2"].$row["STUDYREC_CURRICULUM_CD2"].$row["STUDYREC_SUBCLASSCD2"]) > 0) ? '-' : '';
                $row["STUDYREC_SUBCLASSCD2"] = $row["STUDYREC_CLASSCD2"].$sep2.$row["STUDYREC_SCHOOL_KIND2"].$sep2.$row["STUDYREC_CURRICULUM_CD2"].$sep2.$row["STUDYREC_SUBCLASSCD2"];
                if ($model->getSemesterCount > 2) {
                    $sep3 = (strlen($row["STUDYREC_CLASSCD3"].$row["STUDYREC_SCHOOL_KIND3"].$row["STUDYREC_CURRICULUM_CD3"].$row["STUDYREC_SUBCLASSCD3"]) > 0) ? '-' : '';
                    $row["STUDYREC_SUBCLASSCD3"] = $row["STUDYREC_CLASSCD3"].$sep3.$row["STUDYREC_SCHOOL_KIND3"].$sep3.$row["STUDYREC_CURRICULUM_CD3"].$sep3.$row["STUDYREC_SUBCLASSCD3"];
                }
            }

            if ($row["RECORD_DIV"] == "1") {
                $row["RECORD_DIV_NAME"] = "健常者";
            } else {
                $row["RECORD_DIV_NAME"] = "その他";
            }

            $arg["data"][] = $row;
        }
        $result->free();

        $arg["year"] = $model->year_code;

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "grade") {
            $model->subclasscd = $model->viewcd = "";
            $arg["reload"] = "window.open('knjz401p_2index.php?cmd=edit','right_frame')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz401p_2Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }
    $result = $db->query($query);
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
