<?php

require_once('for_php7.php');

class knjz404jForm1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz404jindex.php", "", "edit");

        $db = Query::dbCheckOut();

        $query = knjz404jQuery::selectYearTaisyou($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->taisyouYear, "TAISYOU_YEAR", $extra, 1);

        $query = knjz404jQuery::selectYearSansyou($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->sansyouYear, "SANSYOU_YEAR", $extra, 1);

        $extra = "onClick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の年度のデータをコピー", $extra);

        if (!$model->grade) {
            $row = $db->getRow(knjz404jQuery::getGrade($model),DB_FETCHMODE_ASSOC);
            $model->grade = $row['VALUE'];
        }

        $result = $db->query(knjz404jQuery::SelectList($model));
        $i = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //一度に表示する講座一覧の上限5000件を超えたら、ワーニングメッセージ表示する。
            if ($i > 5000) {
                $i = $i-1;
                $arg["datalimit"] = "DataLimitError('". $i ."');";
                break;
            }

            $row["URL"] = View::alink("knjz404jindex.php", $row["LABEL"], "target=right_frame",
                                         array("cmd"         => "edit",
                                               "SUBCLASSCD"  => $row["VALUE"]));
            $arg["data"][] = $row;
            $i++;
        }

        $result->free();

        knjCreateHidden($objForm, "cmd");

        //学年コンボ
        $query = knjz404jQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "list" && VARS::get("ed") != "1")
            $arg["reload"] = "window.open('knjz404jindex.php?cmd=edit&init=1','right_frame');";

        View::toHTML($model, "knjz404jForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "--全て--", "value" => "999");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
