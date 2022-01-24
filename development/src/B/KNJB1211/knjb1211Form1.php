<?php

require_once('for_php7.php');

class knjb1211Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjb1211index.php", "", "edit");

        //db接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjb1211Query::getYearData();
        $extra = " onChange=\"return btn_submit('change');\"";
        makeCombo($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1);

        if ($model->Properties["useSpecial_Support_School"] == "1") {
            //校種を表示
            $arg["school_kind"] = '1';
            //校種コンボ
            $query = knjb1211Query::getSchkind($model);
            $extra = "onChange=\"return btn_submit('change');\"";
            makeCombo($objForm, $arg, $db, $query, $model->school_kind, "SCHOOL_KIND", $extra, 1);
        }

        //次年度作成ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('copy');\"" : "disabled";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //科目教科書一覧作成
        $cnt = 0;
        $result = $db->query(knjb1211Query::getList($model)); 
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");

            $a = array("cmd"            => "edit",
                       "YEAR"           => $row["YEAR"],
                       "CLASSCD"        => $row["CLASSCD"],
                       "SCHOOL_KIND"    => $row["SCHOOL_KIND"],
                       "CURRICULUM_CD"  => $row["CURRICULUM_CD"],
                       "SUBCLASSCD"     => $row["SUBCLASSCD"]
                       );

            $row["SUBCLASSNAME"] = View::alink(REQUESTROOT ."/B/KNJB1211/knjb1211index.php", 
                                    htmlspecialchars($row["SUBCLASSNAME"]), 
                                    "target=\"right_frame\" ",
                                    $a);

            $arg["data"][] = $row; 
            $cnt++;
        }
        $model->cnt = $cnt;

        //hidden作成
        makeHidden($objForm);

        //db切断
        Query::dbCheckIn($db);

        if ($model->cmd == "change") {
            $arg["jscript"] = "parent.right_frame.location.href='knjb1211index.php?cmd=edit';";
        }
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb1211Form1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array ("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

}

//Hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
