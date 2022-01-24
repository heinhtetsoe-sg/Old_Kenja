<?php

require_once('for_php7.php');
class knjz334Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        $db = Query::dbCheckOut();
        if($model->cmd == "edit" || $model->cmd == "comp"){
            $model->seldata = "";
        }

        //OSHIRASE_NOが送られてきたらモードを修正モードにする
        if($model->seldata != ""){
            $model->mode = "update";
        }else{
            $model->mode = "add";
        }
        if($model->selmode == "" && $model->cmd != "grp"){
            if($model->seldata != ""){
                //選択されたデータ内容を取得
                $selQuery = knjz334Query::getSelect($model, $model->seldata);
                $selResult = $db->query($selQuery);
                $count = 0;

                while($selRow = $selResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    if($count == 0){
                        $announce = $selRow["ANNOUNCE"];
                        $announceEng = $selRow["ANNOUNCE_ENG"];
                        if($selRow["GROUP_CD"] != ""){
                            $group = $selRow["GROUP_CD"];
                        }else{
                            $model->selectdata = $selRow["IND"];
                            $model->selectdataLabel = $selRow["STAFFNAME_SHOW"];
                            
                            $model->selmode = "indiv";
                        }
                        $from = $selRow["START_DATE"];
                        $to = $selRow["END_DATE"];
                    }else{
                        if($model->selmode != ""){
                            $model->selectdata = $model->selectdata.",".$selRow["IND"];
                            $model->selectdataLabel = $model->selectdataLabel.",".$selRow["STAFFNAME_SHOW"];
                        }else{
                            $group = $group.",".$selRow["GROUP_CD"];
                        }
                    }
                    $count++;
                }
            }else if($model->error != ""){
                $announce = VARS::post("NAIYO");
                $announceEng = VARS::post("NAIYO_ENG");
                $group = VARS::post("GROUP");
                $from = VARS::post("FROM");
                $to = VARS::post("TO");
                
                $model->selectdata       = VARS::post("selectdata");
                $model->selectdataLabel  = VARS::post("selectdataLabel");
                
            }else{
                $announce = "";
                $announceEng = "";
                $group = "";
                $from = date("Y-m-d");
                $to = "";
                
                $model->selectdata       = VARS::post("selectdata");
                $model->selectdataLabel  = VARS::post("selectdataLabel");
            }
        }else{
            $announce = VARS::post("NAIYO");
            $announceEng = VARS::post("NAIYO_ENG");
            $group = VARS::post("GROUP");
            $from = VARS::post("FROM");
            $to = VARS::post("TO");
            
            $model->selectdata       = VARS::post("selectdata");
            $model->selectdataLabel  = VARS::post("selectdataLabel");
        }

        //内容入力欄
        $extra = " style=\"width: 100%;\" maxlength=\"200\"";
        $arg["data"]["NAIYO"] = knjCreateTextArea($objForm, "NAIYO", "5", "80", "soft", $extra, $announce);

        //内容入力欄英語
        $extra = " style=\"width: 100%;\" maxlength=\"200\"";
        $arg["data"]["NAIYO_ENG"] = knjCreateTextArea($objForm, "NAIYO_ENG", "5", "80", "soft", $extra, $announceEng);

        //グループ名
        if(!is_array($group)){
            $sel_group = explode(",", $group);
        }else{
            $sel_group = $group;
        }
        $grpQuery = knjz334Query::getGroup($model);
        $grpResult = $db->query($grpQuery);
        
        $i = 0;
        $GROUP = "<table bgcolor=\"#FFFFFF\"><tr>";
        while($grpRow = $grpResult->fetchRow(DB_FETCHMODE_ASSOC)){
            
                $extra = "id=".$i;
                if(in_array($grpRow["GROUPCD"], $sel_group)){
                    $extra .= " checked ";
                }
                //個人選択のときは使えなくする
                if($model->selmode != ""){
                    $extra .= " disabled ";
                }
                $objForm->ae( array("type"      => "checkbox",
                                    "name"      => "GROUP[]",
                                    "value"     => $grpRow["GROUPCD"],
                                    "extrahtml" => $extra,
                                    "multiple"  => ""));
                $GROUP .= "<td>".$objForm->ge("GROUP[]")."<LABEL for=".$i.">".$grpRow["GROUPNAME"]."</LABEL></td>";
                if(($i+1)%5==0){
                    $GROUP .= "</tr><tr>";
                }else{
                    $GROUP .= "";
                }
                $i++;
        }
        //最後にログイン画面用のチェックボックス
        $extra = "id=".$i;
        if(in_array("log", $sel_group)){
            $extra .= " checked ";
        }
        //個人選択のときは使えなくする
        if($model->selmode != ""){
            $extra .= " disabled ";
        }
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "GROUP[]",
                            "value"     => "log",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $GROUP .= "<td>".$objForm->ge("GROUP[]")."<LABEL for=".$i.">ログイン画面</LABEL></td>";
        
        $arg["data"]["GROUP"] = $GROUP."</tr></table>";

        //日付
        $arg["data"]["FROM"] =  View::popUpCalendar($objForm, "FROM", str_replace("-","/",$from),"");
        $arg["data"]["TO"] =  View::popUpCalendar($objForm, "TO", str_replace("-","/",$to),"");

        //添付ファイル
        updownFile($objForm, $arg, $model);

        if($model->selmode != ""){
            //右側
            //基本メニューコンボ
            $query = knjz334Query::getBaseGrp($model);
            $extra = "onchange=\"return btn_submit('change_grp')\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SELECT_INDNAME"], "SELECT_INDNAME", $extra, 1, "");

            //生徒リストToリスト作成
            makeStudentList($objForm, $arg, $db, $model);
        }
        if($model->mode != "update"){
            if($model->selmode != ""){
                //追加ボタンを作成する
                $objForm->ae( array("type" => "button",
                                    "name"        => "btn_add",
                                    "value"       => "追 加",
                                    "extrahtml"   => "onclick=\"return btn_submit('indadd');\"" ) );

                $arg["button"]["btn_add"] = $objForm->ge("btn_add");
            }else{
                //追加ボタンを作成する
                $objForm->ae( array("type" => "button",
                                    "name"        => "btn_add",
                                    "value"       => "追 加",
                                    "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

                $arg["button"]["btn_add"] = $objForm->ge("btn_add");
            }
        }else{
            //新規ボタンを作成する
            $objForm->ae( array("type" => "button",
                                "name"        => "btn_edit",
                                "value"       => "新 規",
                                "extrahtml"   => "onclick=\"return btn_submit('edit');\"" ) );

            $arg["button"]["btn_edit"] = $objForm->ge("btn_edit")."&nbsp;";
            
            if($model->selmode != ""){
                //更新ボタンを作成する
                $objForm->ae( array("type" => "button",
                                    "name"        => "btn_update",
                                    "value"       => "更 新",
                                    "extrahtml"   => "onclick=\"return btn_submit('indupdate');\"" ) );

                $arg["button"]["btn_update"] = $objForm->ge("btn_update");
            }else{
                //更新ボタンを作成する
                $objForm->ae( array("type" => "button",
                                    "name"        => "btn_update",
                                    "value"       => "更 新",
                                    "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

                $arg["button"]["btn_update"] = $objForm->ge("btn_update");
            }
            //削除ボタンを作成する
            $objForm->ae( array("type" => "button",
                                "name"        => "btn_del",
                                "value"       => "削 除",
                                "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

            $arg["button"]["btn_del"] = "&nbsp;".$objForm->ge("btn_del");
        }

        if($model->selmode != "indiv"){
            //個人対象ボタンを作成する
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_indgrp",
                                "value"       => "個人を選択",
                                "extrahtml"   => "onclick=\"return btn_submit('indiv');\"" ) );

            $arg["button"]["btn_indgrp"] = $objForm->ge("btn_indgrp");
        }else{
            //グループ対象ボタンを作成する
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_indgrp",
                                "value"       => "グループを選択",
                                "extrahtml"   => "onclick=\"return btn_submit('grp');\"" ) );

            $arg["button"]["btn_indgrp"] = $objForm->ge("btn_indgrp");
        }

        //クリアボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) ); 
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata",
                            "value"     => $model->selectdata
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdataLabel",
                            "value"     => $model->selectdataLabel
                            ) );

        knjCreateHidden($objForm, "fileName", "");

        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz334index.php", "", "edit");

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && ($cd_change==true || $model->isload != "1")){
            $arg["reload"]  = "parent.left_frame.location.href='knjz334index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz334Form2.html", $arg);
    }
} 
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg["data2"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model) {
    //左リスト(溜める式)
    $selectdata      = ($model->selectdata != "")       ? explode(",", $model->selectdata)      : array();
    $selectdataLabel = ($model->selectdataLabel != "")  ? explode(",", $model->selectdataLabel) : array();

    //左リスト
    $opt_left = array();
    if ($model->cmd == 'change_grp' ) {
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[] = array("label" => $selectdataLabel[$i],
                                "value" => $selectdata[$i]);
        }
    } else {
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[] = array("label" => $selectdataLabel[$i],
                                "value" => $selectdata[$i]);
        }
    }

    //右リスト
    $opt_right = array();
    $result = $db->query(knjz334Query::getStaff($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if(!in_array($row["VALUE"], $selectdata)){
            $opt_right[]= array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧リスト(右)
    $extra = "multiple style=\"width:100%;\" ondblclick=\"moveStudent('left', 0)\"";
    $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_PART", "", $opt_right, $extra, 20);

    //対象者一覧リスト(左)
    $setSort = $model->field["SELECT_INDNAME"] ? "1" : "0";
    $extra = "multiple style=\"width:100%\" ondblclick=\"moveStudent('right', $setSort)\"";
    $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_PART", "", $opt_left, $extra, 20);

    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('sel_add_all', 0);\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('left', 0);\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('right', $setSort);\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('sel_del_all', $setSort);\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}

//ファイル作成
function updownFile(&$objForm, &$arg, &$model) {
    if ($model->seldata) {
        $dir = "/oshiraseFile/" . $model->seldata . "/";
        $dataDir = DOCUMENTROOT . $dir;

        if (!is_dir($dataDir) || $model->allDelFlg) {
            //フォルダなし
        } else if ($readFileName = opendir($dataDir)) {
            while (false !== ($filename = readdir($readFileName))) {
                //拡張子
                if ($filename != "." && $filename != "..") {
                    $setFiles = array();
                    $setFilename = mb_convert_encoding($filename,"UTF-8", "SJIS-win");
                    //削除ボタン
                    $extra = "onclick=\"return btn_executeDel('executeDel', '{$setFilename}');\"";
                    $setFiles["PDF_FILE_DEL"] = knjCreateBtn($objForm, "PDF_FILE_DEL{$cnt}", "削除", $extra);
                    //ファイル名
                    $setFiles["PDF_FILE_NAME"] = $setFilename;
                    //ファイルURL
                    $setFilename = urlencode($filename);
                    $setFiles["PDF_URL"] = REQUESTROOT . $dir . $setFilename;
                    $arg["down"][] = $setFiles;
                }
            }
            closedir($readFileName);
        }
    }
    //ファイルからの取り込み
    $arg["data"]["FILE"] = knjCreateFile($objForm, "FILE", "", 10240000);
    //実行
    $extra = ($model->seldata) ? "onclick=\"return btn_submit('execute');\"" : "disabled";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);
}
?>
