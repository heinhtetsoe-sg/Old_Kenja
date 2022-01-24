<?php

require_once('for_php7.php');

class knja430sForm3
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knja430sindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $query = knja430sQuery::getStaffYear();
        $extra = "onchange=\"return btn_submit('left_list')\"";
        makeCmb($objForm, $arg, $db, $query, "STAFFYEAR", $model->staffYear, $extra, 1);

        //検索
        $extra = "onclick=\"wopen('knja430sindex.php?cmd=search_view','knja430sSearch',0,0,450,250);\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "かな氏名検索", $extra);

        //リスト
//      if ($model->cmd == "search") {
            $result = $db->query(knja430sQuery::getStaffList($model->staffYear, $model->searchCode, $model->searchName, $model->searchKana));
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //レコードを連想配列のまま配列$arg[data]に追加していく。
                array_walk($row, "htmlspecialchars_array");

                $row["URL"] = View::alink("knja430sindex.php", $row["STAFFCD"], "target=right_frame",
                                            array("cmd"         => "right_list",
                                                  "STAFFCD"     => $row["STAFFCD"]
                                                  ));
                $arg["data"][] = $row;
            }
            $result->free();
//      }

        //DB切断
        Query::dbCheckIn($db);

        //hiddenを作成する
        makeHidden($objForm);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja430sForm3.html", $arg);
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

    $value = ($value && $value_flg) ? $value : CTRL_YEAR;

    $arg["header"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SEARCH_CODE");
    knjCreateHidden($objForm, "SEARCH_NAME");
    knjCreateHidden($objForm, "SEARCH_KANA");
}

?>
