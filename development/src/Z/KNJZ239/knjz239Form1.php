<?php

require_once('for_php7.php');

class knjz239Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz239index.php", "", "edit");
        if ($model->cmd == "list_from_right") {
            $model->field["GRADE"] = $model->grade;
            if ($model->groupcd < 600) {
                $model->field["GROUP_SELECT"] = "1";
            } else {
                $model->field["GROUP_SELECT"] = "2";
            }
        }

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $query = knjz239Query::getExeYear();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //学年
        $query = knjz239Query::getSchregRegdGdat($model);
        $opt = array();
        $opt[] = array('label' => '', 'value' => 'all');
        $value = $model->field["GRADE"];
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('list');\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $value, $opt, $extra, 1);

        //履修選択、選択以外の切替
        $opt = array(1, 2);
        $model->field["GROUP_SELECT"] = ($model->field["GROUP_SELECT"] == "") ? "1" : $model->field["GROUP_SELECT"];
        $extra = array("id=\"GROUP_SELECT1\" onClick=\"return btn_submit('list');\"", "id=\"GROUP_SELECT2\" onClick=\"return btn_submit('list');\"");
        $radioArray = knjCreateRadio($objForm, "GROUP_SELECT", $model->field["GROUP_SELECT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        
        //リスト作成
        makeList($arg, $db, $model);

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からデータをコピー", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        if ($model->cmd == "combo") {
            $arg["reload"] = "window.open('knjz239index.php?cmd=edit','right_frame')";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz239Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $bifKey = "";
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        //画面上の表示用
        $arg["CURRICULUM_CD"] = "1";
    } else {
        //画面上の表示用
        $arg["NO_CURRICULUM_CD"] = "1";
    }
    $query = knjz239Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        if ($bifKey !== $row["GROUPCD"] . $row["GRADE"] . $row["COURSEMAJOR"] . $row["COURSECODE"]) {
            $query = knjz239Query::getGroupCnt($model, $row["GROUPCD"], $row["GRADE"], $row["COURSEMAJOR"], $row["COURSECODE"]);
            $cnt = $db->getOne($query);
            $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
        }
        $bifKey = $row["GROUPCD"] . $row["GRADE"] . $row["COURSEMAJOR"] . $row["COURSECODE"];
        $arg["data"][] = $row;
    }
    $result->free();
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
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

?>
