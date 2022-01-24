<?php

require_once('for_php7.php');

class knjz401j_2Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz401j_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //対象学年コンボ
        $query = knjz401j_2Query::getGrade($model);
        $extra = "onchange=\"return btn_submit('grade');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1);

        //参照学年コンボ
        $query = knjz401j_2Query::getGrade($model, $model->grade);
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
        $query = knjz401j_2Query::getJViewNameList($model->grade, $model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $sep = (strlen($row["STUDYREC_CLASSCD"].$row["STUDYREC_SCHOOL_KIND"].$row["STUDYREC_CURRICULUM_CD"].$row["STUDYREC_SUBCLASSCD"]) > 0) ? '-' : '';
                $row["STUDYREC_SUBCLASSCD"] = $row["STUDYREC_CLASSCD"].$sep.$row["STUDYREC_SCHOOL_KIND"].$sep.$row["STUDYREC_CURRICULUM_CD"].$sep.$row["STUDYREC_SUBCLASSCD"];
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
            $arg["reload"] = "window.open('knjz401j_2index.php?cmd=edit','right_frame')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz401j_2Form1.html", $arg);
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
