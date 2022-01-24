<?php

require_once('for_php7.php');

class knjl332qForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl332qForm1", "POST", "knjl332qindex.php", "", "knjl332qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl332q');\"";
        $query = knjl332qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl332q');\"";
        if (SCHOOLKIND == "J") {
            $query = knjl332qQuery::getNameMst($model->ObjYear, "L024");
        } else {
            $query = knjl332qQuery::getNameMstL004($model->ObjYear);
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //海外入試選択コンボボックス(高校のみ)
        if ($model->field["APPLICANTDIV"] === "2") {
            if (is_null($model->field["TESTDIV"]) || $model->field["TESTDIV"] === "1") {
                $extra = "";
                $query = knjl332qQuery::getNameMstL004Detail($model->ObjYear);
                makeCmb($objForm, $arg, $db, $query, $model->field["DIVOVERSEA"], "DIVOVERSEA", $extra, 1);
            } else {
                $model->field["DIVOVERSEA"] = "";
            }
        }

        //通知日付
        $model->field["NOTICEDATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["NOTICEDATE"] = View::popUpCalendarAlp($objForm, "NOTICEDATE", $model->field["NOTICEDATE"], "", "");

        if ($model->field["TESTDIV"] == '2') {
            $arg["teishutuKigen"] = "1";
            //提出日付
            if ($model->field["TEISHUTSUDATE"] == '') {
                $model->field["TEISHUTSUDATE"] = str_replace("-", "/", CTRL_DATE);
            }
            $arg["data"]["TEISHUTSUDATE"] = View::popUpCalendarAlp($objForm, "TEISHUTSUDATE", $model->field["TEISHUTSUDATE"], "", "");
        }

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL332Q");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl332qForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
