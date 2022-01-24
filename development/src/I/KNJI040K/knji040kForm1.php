<?php

require_once('for_php7.php');

class knji040kForm1 {

    function main(&$model) {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knji040kForm1", "POST", "knji040kindex.php", "", "knji040kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //学校区分抽出
        $schooldiv=="";
        if (isset($model->search_fields["graduate_year"])) {
            $result = $db->query(knji040kQuery::GetSchoolDiv($model->search_fields["graduate_year"]));
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                 $schooldiv = $row["SCHOOLDIV"];
            }
        }
        if ($schooldiv=="") $schooldiv = $model->control["学校区分"];

        //検索結果表示
        if (isset($model->search_fields)) {
            $result = $db->query(knji040kQuery::SearchStudent($model, $model->search_fields, $schooldiv));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i =0;
            list($path, $cmd) = explode("?cmd=", $model->path);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                 array_walk($row, "htmlspecialchars_array");
                 $row["GRADUATE_CLASS"] = $row["HR_NAME"].$row["ATTENDNO"]."番";

                 $row["IMAGE"]    = $image[($row["SEXNUM"]-1)];
                 $row["BIRTHDAY"] = str_replace("-","/",$row["BIRTHDAY"]);

                $objForm->add_element(array("type"      => "checkbox",
                                             "name"     => "chk",
                                             "value"    => $row["SCHREGNO"].",".$row["GRADUATEYEAR"].",".$row["SEMESTER"].",".$row["GRADE"],
                                             "extrahtml"   => "multiple" ));
                $row["CHECK"] = $objForm->ge("chk");

                 $arg["data"][]   = $row;
                 $i++;
            }
            $arg["RESULT"] = "結果　".$i."名";
            $result->free();
            if ($i == 0) {
                $arg["search_result"] = "SearchResult();";
            }
        }

        Query::dbCheckIn($db);

        $objForm->add_element(array("type"      => "checkbox",
                                    "name"      => "chk_all",
                                    "extrahtml"   => "onClick=\"return check_all();\"" ));

        $arg["CHECK_ALL"] = $objForm->ge("chk_all");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $schooldiv);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knji040kForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEKI") {
        $value = ($value && $value_flg) ? $value : STAFFCD;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //検索ボタン
    $extra = "onclick=\"wopen('knji040kindex.php?cmd=search_view', 'knji040k', 0, 0, 450, 250);\"";
    $arg["button"]["SEARCH_BTN"] = knjCreateBtn($objForm, "SEARCH_BTN", "検 索", $extra);

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model, $schooldiv) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJI040K");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GRADUATE_YEAR");
    knjCreateHidden($objForm, "GRADUATE_CLASS");
    knjCreateHidden($objForm, "LKANJI");
    knjCreateHidden($objForm, "LKANA");
    knjCreateHidden($objForm, "SCHREGNO");
    knjCreateHidden($objForm, "G_YEAR");
    knjCreateHidden($objForm, "G_SEMESTER");
    knjCreateHidden($objForm, "G_GRADE");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "schooldiv", $schooldiv);

    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "selectdata");
}
?>
