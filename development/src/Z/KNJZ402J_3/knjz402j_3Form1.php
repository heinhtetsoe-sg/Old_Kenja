<?php

require_once('for_php7.php');

class knjz402j_3Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz402j_3index.php", "", "edit");

        $db = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjz402j_3Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('list');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schkind, $extra, 1);
        }

        //一覧取得
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $query = knjz402j_3Query::getJViewNameList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($model->Properties["useCurriculumcd"] == '1') {
                $row["SUBCLASS_SET"] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"].':'.$row["SUBCLASSNAME"];
            } else {
                $row["SUBCLASS_SET"] = $row["SUBCLASSCD"].':'.$row["SUBCLASSNAME"];
            }
            //重みチェック  
            $dataCount = $db->getOne(knjz402j_3Query::getCountWeightData($model, $row));
            if ($dataCount > 0) {
                $row["WEIGHT_DATA"] = '有';
            } else {
                $row["WEIGHT_DATA"] = '';
            }
            $arg["data"][] = $row; 
        }
        $result->free();

        Query::dbCheckIn($db);

        $arg["year"] = $model->year_code;

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz402j_3Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
