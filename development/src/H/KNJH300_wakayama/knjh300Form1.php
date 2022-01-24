<?php

require_once('for_php7.php');

class knjh300Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh300index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //生徒データ表示
        makeStudentInfo($arg, $db, $model);

        //ALLチェック
        $arg["data"]["CHECKALL"] = createCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //並び替え設定
        $sortQuery = makeSortLink($arg, $model);

        //行動の記録
        makeActionData($objForm, $arg, $db, $model, $sortQuery);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh300Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$arg, $db, $model)
{
    if (isset($model->schregno)) {
        $grad_Row = $db->getRow(knjh300Query::getStudentData($model->schregno), DB_FETCHMODE_ASSOC);
        $grad_Row["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$grad_Row["SCHREGNO"].".".$model->control_data["Extension"];
        $grad_Row["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$grad_Row["SCHREGNO"].".".$model->control_data["Extension"];
    }
    $arg["data"] = $grad_Row;
}

//並び替え設定
function makeSortLink(&$arg, $model)
{
    $sortQuery = $model->taitleSort[$model->sort]["ORDER".$model->taitleSort[$model->sort]["VALUE"]];
    foreach ($model->taitleSort as $key => $val) {

        $arg["data"][$key] = View::alink("knjh300index.php",
                                         "<font color=\"white\">".$val["NAME".$val["VALUE"]]."</font>",
                                         "",
                                         array("cmd" => $key."CLICK", $key => $val["VALUE"], "sort" => $key));
        if ($key != $model->sort) {
            $sortQuery .= $val["ORDER".$val["VALUE"]];
        }
    }
    return $sortQuery;
}

//行動の記録
function makeActionData(&$objForm, &$arg, $db, $model, $sortQuery)
{

    $time = array();
    $result = $db->query(knjh300Query::getActionDuc($model, $sortQuery));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //チェックボックス
        $disable = (AUTHORITY != DEF_UPDATABLE && $row["STAFFCD"] != STAFFCD) ? "disabled" : "";
        $checkVal = $row["SCHREGNO"].":".$row["ACTIONDATE"].":".$row["SEQ"];
        $row["DELCHK"] = createCheckBox($objForm, "DELCHK", $checkVal, $disable, "1");

        //リンク設定
        $subdata = "loadwindow('knjh300index.php?cmd=upd&cmdSub=upd&SCHREGNO={$row["SCHREGNO"]}&ACTIONDATE={$row["ACTIONDATE"]}&SEQ={$row["SEQ"]}',0,0,600,450)";
        $row["TITLE"] = View::alink("#", htmlspecialchars($row["TITLE"]),"onclick=\"$subdata\"");

        $time = preg_split("/:/", $row["ACTIONTIME"]);
        $row["ACTIONTIME"] = $time[0]."：".$time[1];

        $arg["data2"][] = $row;
    }
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $authAll  = "OFF";
    $authSome = "OFF";
    if ($model->auth["CHAIRFLG"] == "ON" || $model->auth["HRCLASSFLG"] == "ON" || $model->auth["COURSEFLG"] == "ON") {
        $authAll = "ON";
    }
    if ($model->auth["CLUBFLG"] == "ON") {
        $authSome = "ON";
    }

    //テスト情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH310/knjh310index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_TEST"] = createBtn($objForm, "BTN_TEST", "テスト情報", $extra);
    //テスト情報テスト
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJHMOSI/knjhmosiindex.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_TEST"] = createBtn($objForm, "BTN_TEST", "駿台テスト", $extra);
    //出欠情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXATTEND2/knjxattendindex.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_APPEND"] = createBtn($objForm, "BTN_APPEND", "出欠情報", $extra);
    //模試情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH320/knjh320index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_SHAM"] = createBtn($objForm, "BTN_SHAM", "模試情報", $extra);
    //指導情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH303/knjh303index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_TRAIN"] = createBtn($objForm, "BTN_TRAIN", "指導情報", $extra);
    //賞罰情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH302/knjh302index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_DETAIL"] = createBtn($objForm, "BTN_DETAIL", "賞罰情報", $extra);
    //部活情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXCLUB_COMMITTEE/index.php?SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&HYOUJI_FLG=1&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_CLUB"] = createBtn($objForm, "BTN_CLUB", "部活情報", $extra);
    //委員会情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXCLUB_COMMITTEE/index.php?SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_COMMITTEE"] = createBtn($objForm, "BTN_COMMITTEE", "委員会情報", $extra);
    //資格情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXCLUB_COMMITTEE/index.php?SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&HYOUJI_FLG=2&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_SHIKAKU"] = createBtn($objForm, "BTN_SHIKAKU", "資格情報", $extra);
    //保健情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXHOKEN/knjxhokenindex.php?cmd=&SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_HOKEN"] = createBtn($objForm, "BTN_HOKEN", "保健情報", $extra);
    //通学情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXTRAIN/knjxtrainindex.php?&SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_TSUGAKU"] = createBtn($objForm, "BTN_TSUGAKU", "通学情報", $extra);
    //学籍基礎情報
    $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
    $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXSCHREG/knjxschregindex.php?&SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["BTN_SCHREG"] = createBtn($objForm, "BTN_SCHREG", "学籍基礎情報", $extra);
    
    //追加
    $extra = "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = createBtn($objForm, "btn_insert", "追 加", $extra);
    //削除
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);
    //終了
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
    $objForm->ae(createHiddenAe("sort", $model->sort));
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

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

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
