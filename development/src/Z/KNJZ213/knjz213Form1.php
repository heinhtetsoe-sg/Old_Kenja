<?php

require_once('for_php7.php');

class knjz213Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz213index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学年コンボ作成
        $query = knjz213Query::getGrade();
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->leftField["GRADE"], $extra, 1);

        //教科コンボ作成
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
            $query = knjz213Query::getSchoolKind($model);
            $model->schoolkind = $db->getOne($query);
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $query = knjz213Query::getClass($model, $model->schoolkind);
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->leftField["CLASSCD"], $extra, 1, "BLANK");

        //リスト作成
        makeList($arg, $db, $model);

        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz213Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $query  = knjz213Query::getList($model);
    $result = $db->query($query);

    $befSubclassCd = "";
    $dataArray = array();
    $cnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
         //レコードを連想配列のまま配列$arg[data]に追加していく。 
         array_walk($row, "htmlspecialchars_array");
         if ($befSubclassCd != "" && $befSubclassCd == $row["COMBINED_SUBCLASSCD"]) {
             $dataArray[$cnt]["COMBINED_SUBCLASSCD"] .= "<br>";
             $dataArray[$cnt]["COMBINED_SUBCLASS_NAME"] .= "<br>";
             $dataArray[$cnt]["ATTEND_SUBCLASSCD_NAME"] .= "<br>".$row["ATTEND_SUBCLASSCD_NAME"];
         } else {
             $cnt++;
             $dataArray[$cnt]["GRADE_SEND"] = $model->leftField["GRADE"];
             $dataArray[$cnt]["COMBINED_SUBCLASSCD_SEND"] = $row["COMBINED_SUBCLASSCD"];
             $dataArray[$cnt]["COMBINED_SUBCLASSCD"] = $row["COMBINED_SUBCLASSCD"];
             $dataArray[$cnt]["COMBINED_SUBCLASS_NAME"] = $row["COMBINED_SUBCLASS_NAME"];
             $dataArray[$cnt]["ATTEND_SUBCLASSCD_NAME"] = $row["ATTEND_SUBCLASSCD_NAME"];
            if ($model->Properties["useCurriculumcd"] == '1') {
                 $dataArray[$cnt]["COMBINED_CLASSCD_SEND"]       = $row["COMBINED_CLASSCD"];
                 $dataArray[$cnt]["COMBINED_SCHOOL_KIND_SEND"]   = $row["COMBINED_SCHOOL_KIND"];
                 $dataArray[$cnt]["COMBINED_CURRICULUM_CD_SEND"] = $row["COMBINED_CURRICULUM_CD"];
                 $dataArray[$cnt]["COMBINED_CLASSCD"]            = $row["COMBINED_CLASSCD"];
                 $dataArray[$cnt]["COMBINED_SCHOOL_KIND"]        = $row["COMBINED_SCHOOL_KIND"];
                 $dataArray[$cnt]["COMBINED_CURRICULUM_CD"]      = $row["COMBINED_CURRICULUM_CD"];
            }
         }
         $befSubclassCd = $row["COMBINED_SUBCLASSCD"];
    }
    $result->free();

    for ($i = 1; $i <= get_count($dataArray); $i++) {
         $arg["data"][] = $dataArray[$i];
    }
}

?>
