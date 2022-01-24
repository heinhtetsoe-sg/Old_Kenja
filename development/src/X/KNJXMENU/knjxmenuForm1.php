<?php

require_once('for_php7.php');
//ビュー作成用クラス
class knjxmenuForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $db = Query::dbCheckOut();
        global $auth;
        
        
        //TOPの時に選択してたメニューを開かせないようにする
        if ($model->cmd == "retree") {
            $model->menuid = "TOP";
        }
        
        //MENU表示きりかえ
        //通常  $model->menuMode = "main"(初期値)
        //サブ  $model->menuMode = "sub"
        //切り替え用リンク
        if ($model->menuMode != "SUB") {
            $mainStyle = "class=\"select\"";
            $subStyle = "class=\"notselect\"";
        } else {
            $mainStyle = "class=\"notselect \"";
            $subStyle = " class=\"select \"";
        }
        if ($model->menuEnglish != 1) {
            $arg["link"]["link_main"] = "<a href=\"index.php?cmd=tree&MENUMODE=MAIN\" target=\"_self\" {$mainStyle} id=\"leftMenuMode\">機能別メニュー</a>";
            $arg["link"]["link_sub"] = "<a href=\"index.php?cmd=tree&MENUMODE=SUB\" target=\"_self\" {$subStyle} id=\"rightMenuMode\">目的別メニュー</a>";
            $menuTitle = " ホーム";
        } else {
            $arg["link"]["link_main"] = "<a href=\"index.php?cmd=tree&MENUMODE=MAIN\" target=\"_self\" {$mainStyle} id=\"leftMenuMode\">Function Menu</a>";
            $arg["link"]["link_sub"] = "<a href=\"index.php?cmd=tree&MENUMODE=SUB\" target=\"_self\" {$subStyle} id=\"rightMenuMode\">Purpose-specific Menu</a>";
            $menuTitle = " Home";
        }

        //メニューの表示タイプで使うDBを変えるための変数！！
        //途中で上書きしない！
        if ($model->menuMode != "SUB") {
            $kind = "MENU";
        } else {
            $kind = "SUB";
        }
        
        //マイメニューを使うならループ処理にしたい
        if ($kind != "SUB") {
            if ($model->properties["useMenuStaffDat"] == "1") {
                $roopcnt = 0;
            } else {
                $roopcnt = 1;
            }
        } else {
            $roopcnt = 1;
        }
        
        $activeflg = 0;

        //TOP部分作成
        $Root["MENUNAME"] = "<a href=\"index.php?cmd=main&MENUID=TOP\" target=\"right_frame\" class=\"alefttop\" onclick=\"chengeCss('TOP', '');\"><i class=\"fa fa-home\"></i>".$menuTitle."</a>";
        $Root["MENUID"] = "TOP";
        $Root["SUBMENUID"] = "TOP";
        $Root["PID"] = "TOP";
        $Root["ID"] = "TOP";
        
        $Root["check"] = "checked";
        $Root["choice"] = " class=\"lefttopchoice\"";
        
        $arg["data"][] = $Root;
        
        //ログインした直後につけたTOPへのchoiceを後に引かないように空白に戻す
        $Root["check"] = "";
        $Root["choice"] = "";
        
        for ($roop = $roopcnt; $roop < 2; $roop++) {
            //左側メニュー作成する
            if ($kind != "SUB") {
                if ($roopcnt != 0) {
                    //$rootQuery = knjxmenuQuery::getAuth($model, "Root");
                    $rootQuery = knjxmenuQuery::getAuth($model, $auth->auth["ADMINGRP_FLG"], $model->pastyear);
                } else {
                    if ($roop == 1) {
                        $rootQuery = knjxmenuQuery::getAuthSt($model, $auth->auth["ADMINGRP_FLG"], $model->pastyear);
                    } else {
                        $rootQuery = knjxmenuQuery::getAuth($model, $auth->auth["ADMINGRP_FLG"], $model->pastyear);
                    }
                }
            } else {
                //ログインした月をSQLに渡したい
                $month = mb_substr(CTRL_DATE, 5, 2);
                $rootQuery = knjxmenuQuery::getSubmenu($model, $month, $auth->auth["ADMINGRP_FLG"], $model->pastyear);       //PARENTSUBIDにRootが入ってるもののSUBIDを取得
            }

            $rootResult = $db->query($rootQuery);
            
            //親のMENUIDを保持する用
            $pmenuid = "";
            
            //メニュー作成用
            $Root = array();
            
            while ($rootRow = $rootResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                //親のMENUIDが変わったら親を作る
                if ($pmenuid == "" || $pmenuid != $rootRow["P_".$kind."ID"]) {
                    $chcount = 0;

                    //親作る前に1つのメニューを〆る
                    if (!empty($Root)) {
                        $Root["li"] .= "</ul>";
                        
                        $arg["data"][] = $Root;
                    }
                    
                    //一番上のメニュー
                    $Root["MENUID"]    = $rootRow["P_".$kind."ID"];
                    $Root["SUBMENUID"] = $rootRow["P_SUB".$kind."ID"];
                    $Root["ID"] = $Root["MENUID"];
                    
                    //アイコン
                    if ($rootRow["P_PROGRAMMEMO"] != "") {
                        $font["FONTNAME"] = "<i class=\"fa {$rootRow["P_PROGRAMMEMO"]}\"></i> ";
                    } else {
                        $font["FONTNAME"] = "<i class=\"fa fa-bars\"></i> ";
                    }
                    
                    $Root["MENUNAME"]  = $font["FONTNAME"]." ".$rootRow["P_".$kind."NAME"];
                    $Root["PID"] = $rootRow["P_".$kind."ID"];
                    $Root["NEXTMENUID"] = $rootRow[$kind."ID"];

                    $Root["li"] = "<ul>";
                }
                
                //二番目のメニュー
                if ($rootRow["PROGRAMMEMO"] != "") {
                    $menu["FONTNAME"] = $rootRow["PROGRAMMEMO"];
                } else {
                    $menu["FONTNAME"] = "fa-paw";
                }
                $menu["MENUNAME"] = $rootRow[$kind."NAME"];

                $menu["MENUID"] = $rootRow[$kind."ID"];

                $cid = $Root["PID"].$chcount;
                
                $Root["li"] .= "<li id=\"{$cid}\"><a href=\"index.php?cmd=main&MENUID={$menu["MENUID"]}\" target=\"right_frame\"";
                $Root["li"] .= "onclick=\"chengeCss('{$Root["PID"]}', '{$cid}');\">";
                $Root["li"] .= "<i class=\"fa {$menu["FONTNAME"]}\"></i> {$menu["MENUNAME"]}</a></li>\n";
                
                $pmenuid = $rootRow["P_".$kind."ID"];
                $chcount++;
            }
            if (!empty($Root)) {
                //最後のメニュー締める
                $Root["li"] .= "</ul>";
                
                $arg["data"][] = $Root;
            }
        }
        
        //ツール部分作成
        if ($model->menuEnglish != 1) {
            $Root["MENUNAME"] = "<i class=\"fa fa-wrench\"></i> ツール";
        } else {
            $Root["MENUNAME"] = "<i class=\"fa fa-wrench\"></i> Tool";
        }
        $Root["SUBMENUID"] = "1";
        $Root["PID"] = "TOOL";
        $Root["ID"] = "TOOL";
        $Root["NEXTMENUID"] = "1PASS";

        $Root["li"] = "<ul>";

        //パスワード変更にactiveをつける
        if ($model->menuid == "1PASS") {
            $active = " class=\"active\"";
            $activeflg = 1;
        } else {
            $active = "";
        }
        $cid = $Root["PID"]."0";
        $Root["li"] .= "<li id=\"{$cid}\"><a href=\"index.php?cmd=chg_pwd&MENUID=1PASS\" target=\"right_frame\" ";
        $Root["li"] .= "onclick=\"chengeCss('{$Root["PID"]}', '{$cid}');\">";
        if ($model->menuEnglish != 1) {
            $Root["li"] .= "<i class=\"fa fa-key\"></i> パスワード変更</a></li>\n";
        } else {
            $Root["li"] .= "<i class=\"fa fa-key\"></i> Change Password</a></li>\n";
        }
        
        //ダウンロードするものを取得する
        $query = knjxmenuQuery::getDownLoad("1");
        $downloadCnt = $db->getOne($query);
        
        if ($downloadCnt > 0) {
            $query = knjxmenuQuery::getDownLoad();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Root["li"] .= "<li><a href=\"".REQUESTROOT."/download/{$row["NAME2"]}\" target=\"_blank\">";
                if ($row["ABBV1"] == "") {
                    $row["ABBV1"] = "fa-download";
                }
                if ($model->menuEnglish != 1) {
                    $Root["li"] .= "<i class=\"fa {$row["ABBV1"]}\"></i> {$row["NAME1"]}</a></li>\n";
                } else {
                    $Root["li"] .= "<i class=\"fa {$row["ABBV1"]}\"></i> {$row["NAME3"]}</a></li>\n";
                }
            }
        } else {
            $Root["li"] .= "<li><a href=\"".REQUESTROOT."/download/{$model->properties["DownLoadAdbeRdr"]}\">";
            if ($model->menuEnglish != 1) {
                $Root["li"] .= "<i class=\"fa fa-folder\"></i> AcrobatReaderのダウンロード</a></li>\n";
            } else {
                $Root["li"] .= "<i class=\"fa fa-folder\"></i> Download(AcrobatReader)</a></li>\n";
            }
            
            $Root["li"] .= "<li><a href=\"".REQUESTROOT."/download/{$model->properties["DownLoadJava"]}\">";
            if ($model->menuEnglish != 1) {
                $Root["li"] .= "<i class=\"fa fa-folder-o\"></i> JavaVMのダウンロード</a></li>\n";
            } else {
                $Root["li"] .= "<i class=\"fa fa-folder-o\"></i> Download(JavaVM)</a></li>\n";
            }
        }
        //学校切替処理
        if ($model->properties["useChgSchool"] == "1") {
            Syslog(LOG_DEBUG, "ChgSchool");
            $query = knjxmenuQuery::getChangeSchool("CNT");
            $chgSchoolCnt = $db->getOne($query);
            if ($chgSchoolCnt > 0) {
                $cid = $Root["PID"]."1";
                //$Root["li"] .= "<li id=\"{$cid}\"><a href=\"index.php?cmd=chg_school&MENUID=1SCHG\" target=\"right_frame\" "
                //$Root["li"] .= "onclick=\"chengeCss('{$Root["PID"]}', '{$cid}');\">";
                //$Root["li"] .= "<i class=\"fa fa-folder-o\"></i> 学校切替</a></li>\n";
                $Root["li"] .= "<li><a href=\"index.php?cmd=chg_school\" target=\"right_frame\">";
                $Root["li"] .= "<i class=\"fa fa-folder-o\"></i> 学校切替</a></li>\n";
            }
        }

        $Root["li"] .= "</ul>";
        $arg["data"][] = $Root;

        //hiddenを作成する
        $arg["hidden"] = "<input type=\"hidden\" name=\"cmd\" value=\"{$model->cmd}\">";
        $arg["hidden"] .= "<input type=\"hidden\" name=\"nowpid\" value=\"{$model->nowpid}\">";
        $arg["hidden"] .= "<input type=\"hidden\" name=\"nowcid\" value=\"{$model->nowcid}\">";
        $arg["hidden"] .= "<input type=\"hidden\" name=\"radiovalue\" value=\"TOP\">";
        
        $arg["REQUESTROOT"] = REQUESTROOT;
        if ($model->properties["NotUseMessage"] != "1") {
            $arg["DispMessage"] = "1";
        }

        $arg["IMAGEPATH"]  = REQUESTROOT ."/image/menu/";
        Query::dbCheckIn($db);

        if ($model->ua == "tablet") {
            $arg["zoom"] = "document.body.style.zoom = 1.5;";
        } else {
            $arg["TOOL"] = "1";
        }
        
        //fontsizeによってcssのクラス変える
        if ($model->size != 1) {
            $arg["csssize"] = "big";
        } else {
            $arg["csssize"] = "";
        }
        
        //css読み込み
        $cssplugin = "kenjaMenu{$model->cssNo}.css|kyotu.css|css/font-awesome.min.css";
        
        
        $arg["reload"]="";
        if ($model->cmd == "tree") {
            $arg["reload"]  = "window.open('index.php?cmd=remain','right_frame');";
        }


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML4($model, "knjxmenuForm1.html", $arg, "", $cssplugin);
    }
}
