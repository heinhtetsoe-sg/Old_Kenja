<?php

require_once('for_php7.php');

class knjl671iForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl671iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["ENTEXAMYEAR"] = $model->year;

        //中学校
        $extra = "id=\"FINSCHOOLCD_ID\" ";
        $query = knjl671iQuery::getFinschoolName();
        makeCmb($objForm, $arg, $db, $query, $model->finschoolcd, "FINSCHOOLCD", $extra, 1, "BLANK");

        //中学校検索ボタン
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain3&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "中学校検索", $extra);

        //氏名
        $extra = "";
        $arg["TOP"]["NAME"] = knjCreateTextBox($objForm, $model->name, "NAME", $keta*2, $keta*2, $extra);

        //読込ボタン
        $extra = "onclick=\"btn_submit('listSearch')\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "読 込", $extra);

        //表示順序ラジオボタン 1:番号順 2:氏名順 2:学校順
        $opt = array(1, 2, 3);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\" onClick=\"btn_submit('chagneSort')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //一覧
        $query = knjl671iQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //並び順を保持
        $arg["HID_SORT"] = $model->sort;

        if ($model->cmd == "chagneSort") {
            $arg["reload"]  = "parent.right_frame.location.href='knjl671iindex.php?cmd=edit&HID_SORT=".$model->sort."';";
        }

        //hidden作成
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl671iForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "PRGID", "KNJL671I");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_SORT", $model->sort);
}
