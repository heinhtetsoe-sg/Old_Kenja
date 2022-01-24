<?php

require_once('for_php7.php');

class knjg045bForm3 {

    function main(&$model) {

        //DB接続
        $db = Query::dbCheckOut();

        //オブジェクト作成
        $objForm = new form;

        //年度コンボ作成
        $query = knjg045bQuery::getYear($model);
        $extra = "onchange=\"return btn_submit('year');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);
        
        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjg045bQuery::getSchkind($model);
        $extra = "onchange=\"return btn_submit('year');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schoolkind, $extra, 1);

        if($model->sort =='ASC') {
            $arg['sort'] = '<a href="knjg045bindex.php?cmd=list&SORT=DESC" target="left_frame" style="color:#FFFFFF">▲</a>';
        } else {
            $arg['sort'] = '<a href="knjg045bindex.php?cmd=list&SORT=ASC" target="left_frame" style="color:#FFFFFF">▼</a>';
        }
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjg045bindex.php", "", "edit");
        $db = Query::dbCheckOut();
        $query  = knjg045bQuery::getList($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["URL"] = View::alink("knjg045bindex.php", str_replace('-','/',$row["DIARY_DATE"]), "target=right_frame",
                                        array("cmd"         => "from_list",
                                              "SCHOOLCD"    => $row["SCHOOLCD"],
                                              "SCHOOL_KIND"    => $row["SCHOOL_KIND"],
                                              "DIARY_DATE"    => $row["DIARY_DATE"]
                                              ));
            $arg["data"][] = $row;
        }

        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg045bForm3.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
