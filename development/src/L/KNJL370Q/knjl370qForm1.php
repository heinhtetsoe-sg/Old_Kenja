<?php

require_once('for_php7.php');

class knjl370qForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl370qindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();
        
        if(VARS::request("SAT_NO") != ""){
            $model->left_field["EXAMNO"] = $model->search["EXAM"];
            $model->left_field["NAME_SEI"] = $model->search["SEI"];
            $model->left_field["NAME_MEI"] = $model->search["MEI"];
            $model->left_field["PLACECD"] = $model->search["PLACE"];
            $model->left_field["SCHOOLCD"] = $model->search["SCHOOL"];
            $model->left_field["GROUPCD"] = $model->search["GROUP"];
        }
        
        //受験番号
        $extra = "";
        $arg["EXAMNO"] = knjCreateTextBox($objForm, $model->left_field["EXAMNO"], "EXAMNO", 10, 5, $extra);
        
        //氏名(姓)
        $extra = "";
        $arg["NAME_SEI"] = knjCreateTextBox($objForm, $model->left_field["NAME_SEI"], "NAMESEI", 20, 10, $extra);
        //氏名(名)
        $extra = "";
        $arg["NAME_MEI"] = knjCreateTextBox($objForm, $model->left_field["NAME_MEI"], "NAMEMEI", 20, 10, $extra);
        
        //試験会場
        $placeQuery = knjl370qQuery::getPlace();
        $placeResult = $db->query($placeQuery);
        $opt = array();
        $opt[0] = array("value"     =>  "",
                        "label"     =>  "");
        while($placeRow = $placeResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value"  => $placeRow["PLACECD"],
                           "label"  => $placeRow["PLACECD"]."：".$placeRow["PLACEAREA"]);
        }
        $extra = "";
        $arg["PLACECD"] = knjCreateCombo($objForm, "PLACECD", $model->left_field["PLACECD"], $opt, $extra, 1);

        //中学校名
        $schoolQuery = knjl370qQuery::getSchool();
        $schoolResult = $db->query($schoolQuery);
        $opt1 = array();
        $opt1[0] = array("value"     =>  "",
                         "label"     =>  "");
        while($schoolRow = $schoolResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt1[] = array("value"  => $schoolRow["SCHOOLCD"],
                            "label"  => $schoolRow["SCHOOLCD"]."：".$schoolRow["FINSCHOOL_NAME"]);
        }
        $extra = "";
        $arg["SCHOOLCD"] = knjCreateCombo($objForm, "SCHOOLCD", $model->left_field["SCHOOLCD"], $opt1, $extra, 1);

        //団体名
        $groupQuery = knjl370qQuery::getGroup();
        $groupResult = $db->query($groupQuery);
        $opt2 = array();
        $opt2[0] = array("value"     =>  "",
                         "label"     =>  "");
        while($groupRow = $groupResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt2[] = array("value"  => $groupRow["GROUPCD"],
                            "label"  => $groupRow["GROUPCD"]."：".$groupRow["GROUPNAME"]);
        }
        $extra = "";
        $arg["GROUPCD"] = knjCreateCombo($objForm, "GROUPCD", $model->left_field["GROUPCD"], $opt2, $extra, 1);

        //検索パラメータ
        $arg["SEARCH"] = "&EXAM={$model->left_field["EXAMNO"]}&SEI={$model->left_field["NAME_SEI"]}&MEI={$model->left_field["NAME_MEI"]}&PLACE={$model->left_field["PLACECD"]}";
        $arg["SEARCH"] .= "&SCHOOL={$model->left_field["SCHOOLCD"]}&GROUP={$model->left_field["GROUPCD"]}";
        
        //申込登録済み件数
        $AppCountQuery = knjl370qQuery::getAppCount();
        $AppCountResult = $db->query($AppCountQuery);
        while($AppCountRow = $AppCountResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $arg["APP_COUNT".$AppCountRow["GROUP"]] = number_format($AppCountRow["COUNT"])."件";
        }
        
        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeMockyear") {
            $arg["reload"] = "window.open('knjl370qindex.php?cmd=edit', 'right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl370qForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    if ($name == "MOCKYEAR") {
        $value = ($value) ? $value : CTRL_YEAR;
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    //何も入れなかったら全部表示したい（初期は出さないようにしておく）
    if($model->cmd == "search"){
    //if($model->left_field["EXAMNO"] != "" || 
    //   $model->left_field["NAME_SEI"] != "" || 
    //   $model->left_field["NAME_MEI"] != "" || 
    //   $model->left_field["PLACECD"] != "" || 
    //   $model->left_field["SCHOOLCD"] != "" || 
    //   $model->left_field["GROUPCD"] != "" ){
    
        $result = $db->query(knjl370qQuery::getList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             array_walk($row, "htmlspecialchars_array");
             $arg["data"][] = $row;
        }
        $result->free();
    }

}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
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

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //検索ボタン
    $extra = "onclick=\"btn_submit('search');\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);
}

?>
