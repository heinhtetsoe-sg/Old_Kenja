<?php

require_once('for_php7.php');

class knjd106aForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjd106aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //プログラムＩＤ
        $arg["PROGRAMID"] = PROGRAMID;

        //テスト種別コンボボックスを作成する
        $query = knjd106aQuery::getTest($model);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->testcd, "onchange=\"return btn_submit('coursename');\"", 1);

        //学年コンボボックスを作成する
        $query = knjd106aQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, "onchange=\"return btn_submit('coursename');\"", 1);

        //コース一覧取得
        $result = $db->query(knjd106aQuery::getList($model)); 
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["FOOTNOTE"] = str_replace("\r\n", "<BR>", $row["FOOTNOTE"]);
            $arg["data"][] = $row;
        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "coursename"){
            $arg["reload"] = "window.open('knjd106aindex.php?cmd=edit','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd106aForm1.html", $arg);
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = KnjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //コピーボタンを作成
    $arg["btn_copy"] = KnjCreateBtn($objForm, "btn_copy", "前年度からコピー", "onclick=\"return btn_submit('copy');\"");
}

//Hidden作成
function makeHidden(&$objForm)
{
    KnjCreateHidden($objForm, "cmd");
}

?>
