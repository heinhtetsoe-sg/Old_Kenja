<?php

require_once('for_php7.php');

class knjd105bForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd105bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報取得
        $arg["INFO"] = ($model->schregno) ? $db->getOne(knjd105bQuery::getStudentInfo($model->schregno)) : "";

        //学籍番号
        $arg["SCHREGNO"] = $model->schregno;

        //生徒氏名
        $arg["NAME"] = $model->name;

        //学期コンボ
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $query = knjd105bQuery::getSemesterMst();
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //テスト種別コンボ
        $query = knjd105bQuery::getTest($model);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1);

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knjd105bQuery::getTrainRow($model),DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //項目名
        if(substr($model->field["TESTCD"],0,2) == '01'){
            $arg["data"]["REMARK_ITEM"] = '個人評';
        } else {
            $arg["data"]["REMARK_ITEM"] = '注意事項';
        }

        //通信欄
        $arg["data"]["TOTALSTUDYTIME"] = createTextArea($objForm, "TOTALSTUDYTIME", 3, 91, "soft", "style=\"height:47px;\"", $row["TOTALSTUDYTIME"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd105bForm1.html", $arg);
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

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db)
{

    //更新
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前後の生徒へ
    if(AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT){
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = createBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = createBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }
    //取消
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);
    //csv
    $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX153/knjx153index.php?AUTH=".AUTHORITY."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", $extra);

}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//テキストエリア作成
function createTextArea(&$objForm, $name, $rows, $cols, $wrap, $extra, $value)
{
    $objForm->ae( array("type"        => "textarea",
                        "name"        => $name,
                        "rows"        => $rows,
                        "cols"        => $cols,
                        "wrap"        => $wrap,
                        "extrahtml"   => $extra,
                        "value"       => $value));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}
?>
