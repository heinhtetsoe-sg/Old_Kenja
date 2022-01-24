<?php

require_once('for_php7.php');

class knjl370qForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl370qindex.php", "", "edit");

        $model->mockyear = ($model->mockyear) ? $model->mockyear : CTRL_YEAR;

        //DB接続
        $db = Query::dbCheckOut();
        
        //SAT_NOが入っているとき
        if ($model->SAT_NO != "" && $model->cmd != "change" && $model->cmd != "change1" && $model->cmd != "change2") {
            $query = knjl370qQuery::getRow($model->SAT_NO);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->right_field["EXAM_NO"] = $Row["SAT_NO"];
            $model->right_field["NAME_SEI"] = $Row["LASTNAME"];
            $model->right_field["NAME_MEI"] = $Row["FIRSTNAME"];
            $model->right_field["NAME_KANA_SEI"] = $Row["KANA1"];
            $model->right_field["NAME_KANA_MEI"] = $Row["KANA2"];
            $model->right_field["SEX"] = $Row["SEX"];
            $model->right_field["BIRTHDAY"] = str_replace("-","/",$Row["BIRTHDAY"]);
            $model->right_field["GRADUATE"] = $Row["GRADUATION"];
            $model->right_field["SCHOOL_PREFCD"] = $Row["FINSCHOOL_PREF_CD"];  //中学校の県コード
            $model->right_field["SCHOOLCD"] = $Row["SCHOOLCD"];
            
            $model->right_field["ZIPCD"] = strlen($Row["ZIPCODE"]) == 8 ? $Row["ZIPCODE"] : substr($Row["ZIPCODE"],0,3)."-".substr($Row["ZIPCODE"],3,4);
            $model->right_field["ADDR1"] = $Row["ADDR1"];
            $model->right_field["ADDR2"] = $Row["ADDR2"];
            $model->right_field["TELNO"] = $Row["TELNO1"];
            $model->right_field["EXAMPLACECD"] = $Row["PLACECD"];
            $model->right_field["GROUPCD"] = $Row["GROUPCD"];
            $model->right_field["FROM_PREFCD"] = $Row["PREFCD"];  //対象者の出身県コード
            $model->right_field["IN_STUDENTNO"] = $Row["INSIDERNO"];
            $model->right_field["INPUT_DATE"] = str_replace("-","/",$Row["INPUT_DATE"]);
            
            //受験番号区分
            $arg["data"]["KUBUN"] = $Row["INOUT_KUBUN"]."　　".$Row["SEND_KUBUN"]."　　".$Row["IND_KUBUN"];
        }
        

        
        //処理
        $opt1 = array(1, 2);
        $label = array("SHORI1"=>"新規","SHORI2"=>"修正");
        $extra = array("id=\"SHORI1\" onclick=\"btn_submit('edit');\"", "id=\"SHORI2\" onclick=\"btn_submit('edit');\"");
        $radioArray = knjCreateRadio($objForm, "SHORI", $model->right_field["SHORI"], $extra, $opt1, get_count($opt1));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val."<LABEL for=".$key.">".$label[$key]."</LABEL>";
        
        //受験番号
        $extra = " onBlur=\"btn_submit('change');\"";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["EXAM_NO"] = knjCreateTextBox($objForm, $model->right_field["EXAM_NO"], "EXAM_NO", 10, 5, $extra);
        
        if(($model->cmd == "change" || $model->cmd == "change1" || $model->cmd == "change2") && $model->right_field["EXAM_NO"] != ""){
            //受験区分取得したい
            $kubunQuery = knjl370qQuery::getKubun($model->right_field["EXAM_NO"]);
            $kubunRow = $db->getRow($kubunQuery, DB_FETCHMODE_ASSOC);
            $arg["data"]["KUBUN"] = $kubunRow["INOUT_KUBUN"]."　　".$kubunRow["SEND_KUBUN"]."　　".$kubunRow["IND_KUBUN"];
            
            //受験番号入力したら会場も入力されるように
            $model->right_field["EXAMPLACECD"] = $kubunRow["PLACECD"];
        }
        
        //氏名
        $extra = "id='NAME_SEI'";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["NAME_SEI"] = knjCreateTextBox($objForm, $model->right_field["NAME_SEI"], "NAME_SEI", 20, 10, $extra);
        $extra = "id='NAME_MEI'";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["NAME_MEI"] = knjCreateTextBox($objForm, $model->right_field["NAME_MEI"], "NAME_MEI", 20, 10, $extra);

        //氏名かな
        $extra = "id='NAME_KANA_SEI'";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["NAME_KANA_SEI"] = knjCreateTextBox($objForm, $model->right_field["NAME_KANA_SEI"], "NAME_KANA_SEI", 20, 10, $extra);
        $extra = "id='NAME_KANA_MEI'";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["NAME_KANA_MEI"] = knjCreateTextBox($objForm, $model->right_field["NAME_KANA_MEI"], "NAME_KANA_MEI", 20, 10, $extra);
        
        //性別
        $opt2 = array(1, 2);
        $label = array("SEX1"=>"男","SEX2"=>"女");
        $extra = array("id=\"SEX1\"", "id=\"SEX2\"");
        $radioArray = knjCreateRadio($objForm, "SEX", $model->right_field["SEX"], $extra, $opt2, get_count($opt2));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val."<LABEL for=".$key.">".$label[$key]."</LABEL>";
        
        //生年月日
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar($objForm, "BIRTHDAY", $model->right_field["BIRTHDAY"], "");
        
        //卒業
        $opt3 = array();
        $query = knjl370qQuery::getGrade();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt3[] = array("value" => $row["NAMECD2"],
                            "label" => $row["NAME1"]);
        }
        if($model->right_field["GRADUATE"] == ""){
            $model->right_field["GRADUATE"] = "09";
        }
        $extra = "";
        $arg["data"]["GRADUATE"] = knjCreateCombo($objForm, "GRADUATE", $model->right_field["GRADUATE"], $opt3, $extra, 1);
        
        //中学コード用県コード
        $opt4 = array();
        $opt4[0] = array("value"    =>  "",
                         "label"    =>  "");
        $schprefQuery = knjl370qQuery::getSchoolPref();
        $schprefResult = $db->query($schprefQuery);
        while($schprefRow = $schprefResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt4[] = array("value"     =>  $schprefRow["FINSCHOOL_PREF_CD"],
                            "label"     =>  $schprefRow["FINSCHOOL_PREF_CD"]."：".$schprefRow["PREF_NAME"]);
        }
        $extra = " onChange=\"schoolPrefChange(this);\"";
        $arg["data"]["SCHOOL_PREFCD"] = knjCreateCombo($objForm, "SCHOOL_PREFCD", $model->right_field["SCHOOL_PREFCD"], $opt4, $extra, 1);
        
        //中学校名
        $opt5 = array();
        $opt5[0] = array("value"    =>  "",
                         "label"    =>  "");
        if($model->right_field["SCHOOL_PREFCD"] != ""){
            $schoolQuery = knjl370qQuery::getSchoolNm($model->right_field["SCHOOL_PREFCD"],$model->right_field["SEARCH_SCHOOL"]);
            $schoolResult = $db->query($schoolQuery);
            while($schoolRow = $schoolResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt5[] = array("value"     =>  $schoolRow["FINSCHOOLCD"],
                                "label"     =>  $schoolRow["FINSCHOOLCD"]."：".$schoolRow["FINSCHOOL_NAME"]);
            }
        }
        $extra = "";
        $arg["data"]["SCHOOLCD"] = knjCreateCombo($objForm, "SCHOOLCD", $model->right_field["SCHOOLCD"], $opt5, $extra, 1);
        
        //中学校検索用
        $extra = "";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["SEARCH_SCHOOL"] = knjCreateTextBox($objForm, $model->right_field["SEARCH_SCHOOL"], "SEARCH_SCHOOL", 20, 20, $extra);
        $extra = "onclick=\"return btn_submit('change1');\"";
        $arg["data"]["btn_searchS"] = knjCreateBtn($objForm, "btn_searchS", "検索", $extra);
        
        //郵便番号
        $extra = " id='ZIPCD'";
        $extra .= " onKeydown=\"toNext(this);\"";
        //ENTERで次のエリアに行きたいのでcommonのを使わないで自前にする。
        $arg["data"]["ZIPCD"] = popUpZipCode($objForm, "ZIPCD", $model->right_field["ZIPCD"], "ADDR1", "8",$extra);
        
        //住所
        $extra = "id='ADDR1'";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $model->right_field["ADDR1"], "ADDR1", 80, 50, $extra);
        $extra = "id='ADDR2'";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $model->right_field["ADDR2"], "ADDR2", 80, 50, $extra);

        
        //電話番号
        $extra = "id='TELNO'";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $model->right_field["TELNO"], "TELNO", 15, 13, $extra);
        
        //試験会場
        $extra = "";
        $placeQuery = knjl370qQuery::getPlaceNm();
        $placeResult = $db->query($placeQuery);
        $opt6 = array();
        $opt6[0] = array("value"    =>  "",
                         "label"    =>  "");
        while($placeRow = $placeResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt6[] = array("value"     =>  $placeRow["PLACECD"],
                            "label"     =>  $placeRow["PLACECD"]."：".$placeRow["PLACENAME_SHORT"]);
        }
        $arg["data"]["EXAMPLACECD"] = knjCreateCombo($objForm, "EXAMPLACECD", $model->right_field["EXAMPLACECD"], $opt6, $extra, 1);
        
        //団体コード
        $extra = "";
        $groupQuery = knjl370qQuery::getGroupNm($model->right_field["SEARCH_GROUP"]);
        $groupResult = $db->query($groupQuery);
        $opt7 = array();
        $opt7[0] = array("value"    =>  "",
                         "label"    =>  "");
        while($groupRow = $groupResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt7[] = array("value"     =>  $groupRow["GROUPCD"],
                            "label"     =>  $groupRow["GROUPCD"]."：".$groupRow["GROUPNAME"]);
        }
        $arg["data"]["GROUPCD"] = knjCreateCombo($objForm, "GROUPCD", $model->right_field["GROUPCD"], $opt7, $extra, 1);

        //団体コード検索用
        $extra = "";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["SEARCH_GROUP"] = knjCreateTextBox($objForm, $model->right_field["SEARCH_GROUP"], "SEARCH_GROUP", 20, 20, $extra);
        $extra = "onclick=\"return btn_submit('change2');\"";
        $arg["data"]["btn_searchG"] = knjCreateBtn($objForm, "btn_searchG", "検索", $extra);
        

        //出身県コード
        $prefQuery = knjl370qQuery::getPrefcdAll();
        $prefResult = $db->query($prefQuery);
        $opt8 = array();
        $opt8[0] = array("value"    => "",
                        "label"    => "");
        while($prefRow = $prefResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt8[] = array("value"  =>  $prefRow["PREF_CD"],
                            "label"  =>  $prefRow["PREF_CD"]."：".$prefRow["PREF_NAME"]);
        }
        $extra = "";
        $arg["data"]["FROM_PREFCD"] = knjCreateCombo($objForm, "FROM_PREFCD", $model->right_field["FROM_PREFCD"], $opt8, $extra, 1);

        //校内生番号
        $extra = "";
        $extra .= " onKeydown=\"toNext(this);\"";
        $arg["data"]["IN_STUDENTNO"] = knjCreateTextBox($objForm, $model->right_field["IN_STUDENTNO"], "IN_STUDENTNO", 20, 10, $extra);

        //入力日
        $arg["data"]["INPUT_DATE"] = View::popUpCalendar($objForm, "INPUT_DATE", $model->right_field["INPUT_DATE"], "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd", $model->cmd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        
        //検索パラメータ
        $arg["SEARCH"] = "&EXAM={$model->search["EXAM"]}&SEI={$model->search["SEI"]}&MEI={$model->search["MEI"]}&PLACE={$model->search["PLACE"]}";
        $arg["SEARCH"] .= "&SCHOOL={$model->search["SCHOOL"]}&GROUP={$model->search["GROUP"]}";
        if ($model->cmd == "edit2") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl370qindex.php?cmd=search&SAT_NO=".$model->SAT_NO.$arg["SEARCH"]."';";
        }else if($model->cmd == "edit3"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl370qindex.php?cmd=search&SAT_NO=".$model->SAT_NO.$arg["SEARCH"]."';";
        }

        $jsplugin = "jquery.autoKana.js|jquery-1.11.0.min.js";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML6($model, "knjl370qForm2.html", $arg, $jsplugin); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $opt = array();
    $result = $db->query($query);
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model)
{
    if($model->right_field["SHORI"] != 2){
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    }else{
        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    }
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function popUpZipCode(&$form, $zipname, $value="", $name="", $size=10, $extra="")
{
    global $sess;
    //テキストエリア
    $form->ae( array("type"        => "text",
                    "name"        => $zipname,
                    "size"        => $size,
                    "extrahtml"   => "onblur=\"isZipcd(this)\"".$extra,
                    "value"       => $value));

    //読込ボタンを作成する
    $form->ae( array("type" => "button",
                    "name"        => "btn_zip",
                    "value"       => "郵便番号入力支援",
                    "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=$name&zipname=$zipname&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );

    //確定ボタンを作成する
    $form->ae( array("type" => "button",
                    "name"        => "btn_apply",
                    "value"       => "確定",
                    "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=$name&zipname=$zipname&zip='+document.forms[0]['$zipname'].value+'&frame='+getFrameName(self))\"") );

    return  View::setIframeJs() .$form->ge($zipname) .$form->ge("btn_zip") .$form->ge("btn_apply");
}


?>
