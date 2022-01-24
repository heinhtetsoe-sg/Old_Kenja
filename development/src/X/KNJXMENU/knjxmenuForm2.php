<?php

require_once('for_php7.php');
class knjxmenuForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "index.php", "", "main");

        //メニューの表示タイプで使うテーブルを変えるための変数！！
        //途中で上書きしない！
        if ($model->menuMode != "SUB") {
            $kind = "MENU";
        } else {
            $kind = "SUB";
        }
        
        //マニュアルを開くのに使う！
        $count = 1;

        $db = Query::dbCheckOut();
        //マニュアルアップロード画面のリンクを作るのはシステム管理者のときだけ！
        $userQuery = knjxmenuQuery::getUserGroup($model->properties);
        $userResult = $db->query($userQuery);
        $kanri = 0;
        while ($userRow = $userResult->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($userRow["GROUPCD"] == "9999") {
                $kanri = 1;
                break;
            }
        }
        
        
        $arg["maintenance_kenja"] = "<a href=\"".REQUESTROOT."/maintenance/maintenance.html\" target=\"_blank\" accesskey=?></a>";

        $query = knjxmenuQuery::getUseDb2();
        $model->useDb2 = $db->getOne($query);
        if ($model->useDb2 == "1") {
            $db2 = Query::dbCheckOut2();
        }

        $Z026 = $db->getRow(knjxmenuQuery::getDateCtrl(), DB_FETCHMODE_ASSOC);

        list($year, $month, $day) = preg_split("/-/", CTRL_DATE);
        $lastDay = date("Y-m-d", mktime(0, 0, 0, $month, intval($day) - 1, intval($year)));

        $toDate = $Z026["NAMESPARE1"] == "1" ? CTRL_DATE : $lastDay;

        if ($model->properties["useCheckAttendUnInput"] == "ON") {
            $syukketuDiv = $db->getOne(knjxmenuQuery::getSchool());
            list($model->syukketuSdate, $model->syukketuEdate) = $db->getRow(knjxmenuQuery::getSemeDate(9));
            if ($Z026["NAMESPARE2"] == "1") {
                list($model->syukketuSdate, $dummy) = $db->getRow(knjxmenuQuery::getSemeDate(CTRL_SEMESTER));
            }
            if ($syukketuDiv == 0) {
                $misyukketu = $db->getOne(knjxmenuQuery::getMisyukketuPrt1($model, $toDate, "CNT"));
            } else {
                $misyukketu = $db->getOne(knjxmenuQuery::getMisyukketuPrt2($model, $toDate, "CNT"));
            }
        }

        $bunsyoKanri = $db->getOne(knjxmenuQuery::getBunsyoKanri());
        $model->edboardSchoolcd = $db->getOne(knjxmenuQuery::getEdboardSchoolcd());
        if ($model->properties["useTuutatu"] == "ON" && $bunsyoKanri > 0) {
            $tuutatu = $db2->getOne(knjxmenuQuery::getTuutatu($model, "CNT"));
        }

        //右側の固定文字も英語対応にする
        if ($model->menuEnglish != 1) {
            $arg["LOGOUT"] = "ログアウト";
            $arg["THSHORI"] = "処理名";
            $arg["THMANUAL"] = "マニュアル";
            //TOP
            $arg["THNOT_DISP"] = "非表示";
            $arg["THHYOUZI"] = "表示期間";
            $arg["THNAIYO"] = "お知らせ";
            $arg["THTEMP_FILE"] = "添付";
            $arg["THSEND"] = "送信者";
        } else {
            $arg["LOGOUT"] = "Logout";
            $arg["THSHORI"] = "Processing Name";
            $arg["THMANUAL"] = "Manual";
            //TOP
            $arg["THNOT_DISP"] = "Not Display";
            $arg["THHYOUZI"] = "Display Period";
            $arg["THNAIYO"] = "Announcements";
            $arg["THTEMP_FILE"] = "Attached File";
            $arg["THSEND"] = "Sender";
        }


        if ($model->menuid != "TOP") {
            if ($kind != "SUB") {
                if ($model->properties["useMenuStaffDat"] != "1") {
                    $MenuQuery = " SELECT MENUNAME{$model->menuField} AS MENUNAME FROM MENU_MST WHERE MENUID = '".$model->menuid."'";
                    if ($model->properties["useSchool_KindMenu"] == "1") {
                        $MenuQuery .= "         AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                        $MenuQuery .= "         AND SCHOOLCD = '".SCHOOLCD."' ";
                    }
                } else {      //教員マイメニュー用
                    $MenuQuery  = " WITH MAIN_MENU_T AS( ";
                    $MenuQuery .= " SELECT MENUID, SUBMENUID, PARENTMENUID, MENUNAME{$model->menuField} AS MENUNAME, PROGRAMID, PROGRAMPATH, PROCESSCD, INVALID_FLG, PROGRAMMEMO, SHOWORDER, SSL ";
                    $MenuQuery .= " FROM ";
                    $MenuQuery .= "     MENU_MST ";
                    if ($model->properties["useSchool_KindMenu"] == "1") {
                        $MenuQuery .= " WHERE ";
                        $MenuQuery .= "         SCHOOL_KIND = '".SCHOOLKIND."' AND ";
                        $MenuQuery .= "         SCHOOLCD = '".SCHOOLCD."' ";
                    }
                    $MenuQuery .= " UNION ";
                    $MenuQuery .= " SELECT MENUID, SUBMENUID, PARENTMENUID, MENUNAME, PROGRAMID, PROGRAMPATH, PROCESSCD, INVALID_FLG, PROGRAMMEMO, SHOWORDER, SSL ";
                    $MenuQuery .= " FROM ";
                    $MenuQuery .= "     MENU_STAFF_MST ";
                    $MenuQuery .= " WHERE ";
                    $MenuQuery .= "     STAFFCD = '".STAFFCD."' ";
                    $MenuQuery .= " ) ";
                    $MenuQuery .= " SELECT MENUNAME FROM MAIN_MENU_T WHERE MENUID = '".$model->menuid."'";
                }
            } else {
                $MenuQuery = " SELECT SUBNAME{$model->menuField} AS SUBNAME FROM MENU_SUB_MST WHERE SUBID = '".$model->menuid."'";
            }
            $MenuResult = $db->getOne($MenuQuery);

            $urla = "'".REQUESTROOT."/X/KNJXUPDATE/knjxupdateindex.php?PROGRAMID={$model->menuid}&MENUID={$model->menuid}'";
            $java = "wopen({$urla},'{$model->menuid}',200,200,400,300);";
            
            if ($MenuResult == "") {
                $MenuResult = "　　";
            }
            //システム管理の人だけリンク
            if ($kanri != 0) {
                $arg["MENUNAME"] = View::alink("#", $MenuResult, "onClick=\"$java\"");
            } else {
                $arg["MENUNAME"] = $MenuResult;
            }

            global $auth;
            //学校名
            $gakuQuery = knjxmenuQuery::getGakuname();
            $gakuname = $db->getOne($gakuQuery);
            
            //おおもとのマニュアル
            $MANUAL = getManual($model->menuid, $gakuname, "sample2", $count);
            if ($MANUAL[1] != "") {
                $arg["MANUAL"] = $MANUAL[1]."　".$MANUAL[0];
            } else {
                $arg["MANUAL"] = $MANUAL[0];
            }
            
        
            //教科、科目、クラス取得
            $result = $db->query(knjxmenuQuery::selectQuery($model->menuid, $auth->auth["ADMINGRP_FLG"], 2, $model->pastyear, "", $kind, $model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($titleflg == 1 && $row["MAIN"] != "TITLE" && $row["MAIN"] != "ENGTITLE") {
                    $arg["table"][] = $titlerow;
                }

                // $url = urlencode(REQUESTROOT.$row["PROGRAMPATH"]."/".strtolower($row["PROGRAMID"])."index.php?PROGRAMID={$row["PROGRAMID"]}&MN_ID={$row["MENUID"]}&URL_SCHOOLCD=".SCHOOLCD."&URL_SCHOOLKIND=".SCHOOLKIND);
                $url = urlencode(REQUESTROOT.$row["PROGRAMPATH"]."/".strtolower($row["PROGRAMID"])."index.php?PROGRAMID={$row["PROGRAMID"]}&MN_ID={$row["MENUID"]}");
                if ($kind != "SUB") {
                    $title = $MenuResult ."-[" .$row["MENUNAME"] ."]　".STAFFNAME_SHOW;
                } else {
                    if ($row["SUBNAME"] != "") {
                        $title = $MenuResult ."-[" .$row["SUBNAME"] ."]　".STAFFNAME_SHOW;
                    } else {
                        $title = $MenuResult ."-[" .$row["MENUNAME"] ."]　".STAFFNAME_SHOW;
                    }
                }
                $title = urlencode($title);

                
                //メニューごとのマニュアル
                if ($row["PROGRAMID"] != "") {
                    $manual = getManual($row["PROGRAMID"], $gakuname, "sample3", $count);
                    if ($manual[1] != "") {
                        $row["manual"] = $manual[1]."　".$manual[0];
                    } else {
                        $row["manual"] = $manual[0];
                    }
                }

                //-------------------------------------

                //リンク設定
                if ($row["PROGRAMMEMO"] != "") {
                    $img = "<i class=\"fa {$row["PROGRAMMEMO"]}\"></i>";
                } else {
                    if ($row["MAIN"] =="TITLE" || $row["MAIN"] == "ENGTITLE") {
                        $img = "&nbsp;";
                    } else {
                        $img = "<i class=\"fa fa-star\"></i>";
                    }
                }
                 
                $urla = "'".REQUESTROOT."/X/KNJXUPDATE/knjxupdateindex.php?PROGRAMID={$row["PROGRAMID"]}&MENUID={$row["MENUID"]}'";
                $java = "wopen({$urla},'{$row["PROGRAMID"]}',200,200,400,300);";
                if ($kanri != 0) {
                    $row["Image"] = View::alink("#", $img, "onClick=\"$java\"");
                } else {
                    $row["Image"] = $img;
                }

                if ($row["MAIN"] != "TITLE" && $row["MAIN"] != "ENGTITLE") {       //タイトル部分じゃなかったら
                    $row["class"] = "";
                    
                    $js = "wopen('index.php?cmd=top_frame&TITLE=$title&main_src=$url&PROGRAMID={$row["PROGRAMID"]}','SUBWIN',0,0,screen.availWidth,screen.availHeight);";
                    if (!is_null($row["PROGRAMID"]) && $row["PROGRAMID"] !== "") {
                        $row["NAME"] = View::alink("#", htmlspecialchars($row["MENUNAME"]), "style=\"display:block;\" onClick=\"$js\"");
                    } else {
                        $row["NAME"] = htmlspecialchars($row["MENUNAME"]);
                    }
                    
                    $row["PROGRAMID"] = $row["PROGRAMID"];
                    $row["MENUID"] = $row["MENUID"];
                    
                    $arg["table"][] = $row;
                    
                    $titleflg = 0;
                } else {          //タイトル部分
                    $titlerow["class"] = "class=\"title\"";
                    
                    $titlerow["Image"] = $row["Image"];
                    if ($kind != "SUB") {
                        $titlerow["NAME"] = $row["MENUNAME"];
                    } else {
                        $titlerow["NAME"] = $row["SUBNAME"];
                    }
                    $titlerow["PROGRAMID"] = "&nbsp;";
                    $titlerow["MENUID"] = "&nbsp;";
                    $titlerow["user"] = "&nbsp;";
                    $titlerow["manual"] = "&nbsp;";
                    
                    $titleflg = 1;
                }
            }
        } else {
            if ($model->menuEnglish != 1) {
                $arg["MENUNAME"] = "ホーム";
            } else {
                $arg{"MENUNAME"} = "Home";
            }
            $arg["menu"] = "1";
            
            //OSHIRASE_TBLからデータ取得
            //当日の日付
            $today = date("Y-m-d");
            
            //所属するGROUPCD取得
            $grpQuery = knjxmenuQuery::getUserGroup($model->properties);
            $grpResult = $db->query($grpQuery);
            
            $cnm = "";
            $grp = "";
            
            while ($grpRow = $grpResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $grp .= $cnm.$grpRow["GROUPCD"];
                $cnm = "','";
            }
            
            //OSHIRASE_TBLからデータ取得
            $osrsQuery = knjxmenuQuery::getOshirase($model, $model->properties, $today, $grp);
            $osrsResult = $db->query($osrsQuery);

            while ($osrsRow = $osrsResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $oshi = array();
                //チェックボックス
                $setKey = $osrsRow["DATA_DIV"]."_".$osrsRow["OSHIRASE_NO"];
                $setCheckName = "NOT_DISP".$setKey;
                $extra = " id=\"{$setCheckName}\" ";
                $extra .= "onClick=\"updNotDisp(this, 'checkNotDisp', '{$model->menuid}', '{$setKey}');\"";
                $checked = $osrsRow["NOT_DISP_FLG"] == '1' ? " checked " : "";
                $oshi["NOT_DISP"] = knjCreateCheckBox($objForm, $setCheckName, $setKey, $checked.$extra);
                $showSDate = str_replace("-", "/", $osrsRow["START_DATE"]);
                $showEDate = str_replace("-", "/", $osrsRow["END_DATE"]);
                $oshi["DATE"] = $showSDate." ～ ".$showEDate;

                
                //STAFF_MSTを取得
                $stfQuery = knjxmenuQuery::getStaffNameJpEng($osrsRow["STAFFCD"]);
                $stfNameArr = $db->getRow($stfQuery, DB_FETCHMODE_ASSOC);
                $oshi["STAFF"] = $model->menuEnglish != 1 ? $stfNameArr["STAFFNAME"] : $stfNameArr["STAFFNAME_ENG"];

                //リリースノートの場合の対応　添付ファイル
                if ($osrsRow["DATA_DIV"] == '98') {
                    $dir = "/download/";
                    if ($model->menuEnglish != 1) {
                        $showMessage = "&nbsp;&nbsp;&nbsp;&nbsp;操作マニュアル";
                        $oshi["STAFF"] = "システム";
                    } else {
                        $showMessage = "&nbsp;&nbsp;&nbsp;&nbsp;Manual";
                        $oshi["STAFF"] = "System";
                    }
                    $oshi["ANNOUNCE"] = $showMessage;
                    $oshi["DATE"] = '';
                } elseif ($osrsRow["DATA_DIV"] == '99') {
                    $dir = "/releaseNote/";
                    if ($model->menuEnglish != 1) {
                        $showMessage = "&nbsp;リリース<br>&nbsp;&nbsp;&nbsp;&nbsp;詳細は添付ファイルを参照ください。";
                        $oshi["STAFF"] = "システム";
                    } else {
                        $showMessage = "&nbsp;Release Note<br>&nbsp;&nbsp;&nbsp;&nbsp;For detail information, please check attached file.";
                        $oshi["STAFF"] = "System";
                    }
                    $oshi["ANNOUNCE"] = "<i class=\"fa fa-info-circle fa-lg\"></i> ".$showSDate.$showMessage;
                    $oshi["DATE"] = '';
                } else {
                    $dir = "/oshiraseFile/" . $osrsRow["OSHIRASE_NO"] . "/";
                    if ($model->menuEnglish != 1) {
                        $osrsRow["ANNOUNCE"] = str_replace("\n", "<br>", str_replace("\r\n", "\n", $osrsRow["ANNOUNCE"]));
                        $oshi["ANNOUNCE"] = $osrsRow["ANNOUNCE"];
                    } else {
                        $osrsRow["ANNOUNCE_ENG"] = strlen($osrsRow["ANNOUNCE_ENG"]) > 0 ? $osrsRow["ANNOUNCE_ENG"] : $osrsRow["ANNOUNCE"];
                        $osrsRow["ANNOUNCE_ENG"] = str_replace("\n", "<br>", str_replace("\r\n", "\n", $osrsRow["ANNOUNCE_ENG"]));
                        $oshi["ANNOUNCE"] = $osrsRow["ANNOUNCE_ENG"];
                    }
                }
                $dataDir = DOCUMENTROOT . $dir;
                $userlink = "";
                $linkSep = "";
                if (!is_dir($dataDir)) {
                    //フォルダなし
                } elseif ($readFileName = opendir($dataDir)) {
                    $fileCnt = 1;
                    while (false !== ($filename = readdir($readFileName))) {
                        //拡張子
                        if ($filename != "." && $filename != "..") {
                            $setFileName = mb_convert_encoding($filename, "UTF-8", "SJIS-win");
                            $filenameArray = explode(".", $filename);
                            $extension = $filenameArray[get_count($filenameArray) - 1];

                            if ($extension != 'pdf' && $extension != 'PDF') {
                                continue;
                            }
                            if ($osrsRow["DATA_DIV"] == '98' && $setFileName != "menu.pdf") {
                                continue;
                            }
                            if ($osrsRow["DATA_DIV"] == '99' && !strpos($setFileName, "ReleaseNote_".$osrsRow["OSHIRASE_NO"].".")) {
                                continue;
                            }

                            //ファイルURL
                            $urlFilename = urlencode($filename);
                            $url2 = REQUESTROOT . $dir . $urlFilename;
                            $js = "wopen('{$url2}', '{$model->menuid}{$fileCnt}', 0, 0, screen.availWidth, screen.availHeight);";
                            $img = "<i class=\"fa fa-file-pdf-o\" title=\"{$setFileName}\"></i>";
                            $userlink .= $linkSep."<a onclick=\"{$js}\" href=\"#\">{$img}</a>";
                            $linkSep = "&nbsp;";
                            $fileCnt++;
                        }
                    }
                    closedir($readFileName);
                    $oshi["TEMP_FILE"] = $userlink;
                }

                $arg["anno"][] = $oshi;
            }
            
            if ($model->menuEnglish != 1) {
                $buttontitle = "追加";
            } else {
                $buttontitle = "Add";
            }
            if (!is_array($arg["anno"])) {
                if ($model->menuEnglish != 1) {
                    $arg["not"] = "現在お知らせはありません";
                } else {
                    $arg["not"] = "There are currently no announcement.";
                }
            } else {
                $arg["not"] = "";
            }

            //お知らせ入力画面へのリンク
            $url = urlencode(REQUESTROOT."/Z/KNJZ334/knjz334index.php?PROGRAMID=KNJZ334");
            $js = "wopen('index.php?cmd=top_frame&TITLE=$title&main_src=$url','SUBWIN',0,0,screen.availWidth,screen.availHeight);";
            $name = "<i class=\"fa fa-comment-o\"></i> メッセージ送信</a></li>";
            $arg["osirase"] = View::alink("#", htmlspecialchars($buttontitle), "class=\"size1\" onClick=\"$js\"");

            //表示/非表示
            if ($model->menuEnglish != 1) {
                if ($model->notDisp == 1) {
                    $notDispName = "全件表示";
                    $setDisp = "0";
                } else {
                    $notDispName = "選択非表示";
                    $setDisp = "1";
                }
            } else {
                if ($model->notDisp == 1) {
                    $notDispName = "Show Hidden";
                    $setDisp = "0";
                } else {
                    $notDispName = "Hide Selected";
                    $setDisp = "1";
                }
            }
            $arg["hihyouji"] = "<a href=\"index.php?cmd=change&DISP={$setDisp}&MENUID={$model->menuid}\" target=\"right_frame\" class=\"size{$setDisp}\">{$notDispName}</a>";
        }

        if ($model->cssNo == "") {
            $model->getProperties();
            $model->cssNo= $model->properties["cssIRO"];
        }
        
        //学期変更はTOPに限らず。ただし変更したらTOPに戻る
        if ($model->gakkichange != 0) {      //学期変更画面を見た人だけ
            if ($model->menuEnglish != 1) {
                $gakkihenkou = "学期変更";
            } else {
                $gakkihenkou = "Change Semester";
            }
            $arg["gakkichange"] = "<a href=\"#\" onClick=\"logout3();\"><i class=\"fa fa-cogs\"></i> ".$gakkihenkou."</a>";
        }
        
        
        //CSS選択用リンク
        $space = "";
        for ($i = 0; $i < 8; $i++) {
            $lyric = array("＿","＿","＿","＿","＿","＿","＿","＿","＿");
            if ($model->cssNo == $i) {
                if ($i == 5) {
                    $arg["CSS"] .= " △ <span class=\"css{$i} csschoice\">".$lyric[$i]."</span>";
                } else {
                    $arg["CSS"] .= $space."<span class=\"css{$i} csschoice\">".$lyric[$i]."</span>";
                }
            } elseif ($i == 5) {
                $arg["CSS"] .= " △ <a href=\"index.php?cmd=change&CSSNO={$i}&MENUID={$model->menuid}\" target=\"right_frame\" class=\"css{$i}\">{$lyric[$i]}</a>";
            } else {
                $arg["CSS"] .= $space."<a href=\"index.php?cmd=change&CSSNO={$i}&MENUID={$model->menuid}\" target=\"right_frame\" class=\"css{$i}\">{$lyric[$i]}</a>";
            }
            $space = " ";
        }

        //fontsize選択用リンク
        for ($i = 1; $i < 3; $i++) {
            $lyric2 = array("", "<i class=\"fa fa-search-minus\"></i>","<i class=\"fa fa-search-plus\"></i>");
            if ($model->size == $i) {
                $arg["SIZE"] .= "<span class=\"sizechoice\"><i class=\"fa fa-search\"></i></span>";
            } else {
                $arg["SIZE"] .= "<a href=\"index.php?cmd=change&SIZE={$i}&MENUID={$model->menuid}\" target=\"right_frame\" class=\"size\">{$lyric2[$i]}</a>";
            }
        }
        
        //fontsizeによってcssのクラス変える
        if ($model->size != 1) {
            $arg["csssize"] = "big";
        } else {
            $arg["csssize"] = "";
        }

        //言語変換ボタン
        if ($model->menuEnglish != 1) {
            $changeLanguageName = "English";
            $setLang = "1";
        } else {
            $changeLanguageName = "日本語";
            $setLang = "0";
        }
        $arg["CHANGE_LANGUAGE"] = "<a href=\"index.php?cmd=change&LANG={$setLang}&MENUID={$model->menuid}\" target=\"right_frame\" class=\"size\">{$changeLanguageName}</a>";
        if ($model->properties["useLanguageChange"] == "1") {
            $arg["useLanguageChange"] = "1";
        }


        //入力変換チェック
        for ($i = 1; $i < 3; $i++) {
            $lyric3 = array("","IME","ATOK");
            if ($model->input == $i) {
                $arg["INPUT"] .= "<span class=\"sizechoice\">{$lyric3[$i]}</span>";
            } else {
                $arg["INPUT"] .= "<a href=\"index.php?cmd=change&INPUT={$i}&MENUID={$model->menuid}\" target=\"right_frame\" class=\"size\">{$lyric3[$i]}</a>";
            }
        }
        if ($model->properties["useImeOrAtok"] == "1") {
            $arg["useImeOrAtok"] = "1";
        }

        //学校名表示
        $para = "X002";
        $gakuQuery = knjxmenuQuery::getGakuname($para);
        $gakuRow = $db->getOne($gakuQuery);
        if ($gakuRow == "") {
            $gakuQuery = knjxmenuQuery::getGaku($model);
            $gakuRow = $db->getOne($gakuQuery);
        }
        $arg["GAKU_NAME"] = $gakuRow;

        $showDate = str_replace("-", "/", CTRL_DATE);
        $gakuQuery = knjxmenuQuery::getStaffNameJpEng(STAFFCD);
        $gakuRow = $db->getRow($gakuQuery, DB_FETCHMODE_ASSOC);
        if ($model->menuEnglish != 1) {
            $arg["CTRL_YEAR"] = CTRL_YEAR ."年度&nbsp;" .CTRL_SEMESTERNAME ."　処理日付：" .$showDate;
            $arg["STAFFNAME_SHOW"] = $gakuRow["STAFFNAME"];
        } else {
            $englishSeme = array("1" => "st", "2" => "nd", "3" => "rd");
            $showSeme = CTRL_SEMESTER."th";
            if ($englishSeme[CTRL_SEMESTER]) {
                $showSeme = CTRL_SEMESTER.$englishSeme[CTRL_SEMESTER];
            }
            $arg["CTRL_YEAR"] = CTRL_YEAR ."&nbsp;" .$showSeme ." Semester　Login date：" .$showDate;
            $arg["STAFFNAME_SHOW"] = $gakuRow["STAFFNAME_ENG"];
        }
        
        $arg["REQUESTROOT"] = REQUESTROOT;

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        
        //hiddenを作成する
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "PROGRAMID");
        knjCreateHidden($objForm, "HIDMENUID", $model->menuid);

        if ($model->properties["useCheckAttendUnInput"] == "ON" || $model->properties["useTuutatu"] == "ON") {
            if ($misyukketu > 0) {
                $arg["misyukketu"] = "loadwindow('index.php?cmd=misyukketu&syukketuDiv={$syukketuDiv}', 290, 320, 400, 230)";
            } elseif ($tuutatu > 0) {
                $arg["tuutatu"] = "loadwindow('index.php?cmd=tuutatu&syukketuDiv={$syukketuDiv}', 0, 100, 550, 400)";
            }
        }

        Query::dbCheckIn($db);
        if ($model->useDb2 == "1") {
            Query::dbCheckIn($db2);
        }

        if ($model->ua == "tablet") {
            $arg["zoom"] = "document.body.style.zoom = 1.5;";
        }
        
        //css読み込み
        $cssplugin = "kenjaMenu{$model->cssNo}.css|kyotu.css|css/font-awesome.min.css";

        $arg["schoolWareMenu"] = '';
        $setParam = '';
        if (($model->cmd == 'main' || $model->cmd == 'remain') && $model->getSchoolWareMenuId) {
            list($prgId, $atDate, $peri, $chair) = explode("_", $model->getSchoolWareMenuId);
            $openPrg = $model->schoolWareMenu;
            if ($prgId == 'KNJC020A' || $prgId == 'KNJC030A') {
                $openPrg["PROGRAMPATH"] = "/C/KNJC010A";
                $openPrg["PROGRAMID"] = "KNJC010A";
                $setParam = "&syoribi={$atDate}&periodcd={$peri}&chaircd={$chair}&SEND_PRG={$prgId}";
            }

            $model->getSchoolWareMenuId = '';
            $model->schoolWareMenu = array();

            $url = urlencode(REQUESTROOT.$openPrg["PROGRAMPATH"]."/".strtolower($openPrg["PROGRAMID"])."index.php?{$setParam}");
            $js = "wopen('index.php?cmd=top_frame&TITLE={$openPrg["MENUNAME"]}&main_src=$url&PROGRAMID={$openPrg["PROGRAMID"]}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
            $arg["schoolWareMenu"] = $js;
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML4($model, "knjxmenuForm2.html", $arg, "", $cssplugin);
    }
}

    
/*****マニュアル表示を外出ししてみる*******/
function getManual($nowMenuid, $gakuName, $tooltip, &$count)
{
    //返す値↓
    $returnLink = "";
    $userLink = "";

    for ($j=0; $j<2; $j++) {
        //ディレクトリに接続
        if ($j == 0) {
            $dataDir = DOCUMENTROOT."/sousaManual/";
        } else {
            $dataDir = DOCUMENTROOT."/kyotuManual/";
        }
        $aa = opendir($dataDir);

        $i = 0;
        $space = "";
        $name = array();
        $gakko = array();
        $fileName = array();
        $readFile = array();

        while (false !== ($filename = readdir($aa))) {                          //MENUIDが合致するものだけ_で分けて配列に入れる
            $motoname = $filename;
            $filename = mb_convert_encoding($filename, "UTF-8", "EUC-JP");
            
            //時間取得
            $dirName = $dataDir.$motoname;
            $fileDate = View::echo_filedate($dirName);
            
            $fileName = explode("_", $filename);
            if ($fileName[1] == $nowMenuid) {
                $readFile[$i]["kind"] = $fileName[0];
                $readFile[$i]["menu"] = $fileName[1];
                $readFile[$i]["gakko"] = $fileName[2];
                $readFile[$i]["name"] = $fileName[3];
                $readFile[$i]["motoname"] = $motoname;
                $readFile[$i]["date"] = $fileDate;
                $i++;
            }
        }
        
        closedir($aa);

        //配列が空白じゃなかったときだけ
        if ($i > 0) {
            if ($i > 1) {     //配列に複数入ってたときだけ並べ替え
                //配列を並び替え
                foreach ($readFile as $key => $row) {
                    //並べ替えたいところを配列に入れる
                    $name[$key] = $row["name"];
                    $gakko[$key] = $row["gakko"];
                }
                array_multisort($name, SORT_STRING, $gakko, SORT_STRING, $readFile);
            }
            
            $link = "";
            $userlink = "";
            $bfrname = "";
            
            //配列を1つずつ確認
            foreach ($readFile as $Rfile) {
                //ファイルの名前が前のと違ったら
                if ($bfrname != "" && $bfrname != $Rfile["name"] && ($link != ""||$userlink != "")) {
                    if ($userlink != "") {
                        $userLink .= $spaceU.$userlink;
                        $spaceU = "　";
                    } else {
                        $returnLink .= $spaceM.$link;
                        $spaceM = "　";
                    }
                    $link = "";
                    $userlink = "";
                    $count++;
                }
                    
                if ($Rfile["gakko"] == "zzzz") {
                    if ($link == "") {
                        $filename = urlencode($Rfile["motoname"]);
                        
                        $url2 = "'".REQUESTROOT."/kyotuManual/{$filename}?date={$Rfile["date"]}'";
                        $js = "wopen({$url2},'{$nowMenuid}{$count}',0,0,screen.availWidth,screen.availHeight);";

                        //表示名のエンコードの種類を変える
                        if (mb_detect_encoding($Rfile["motoname"], "UTF-8,SJIS") == "UTF-8") {
                            $hyouzi = $Rfile["motoname"];
                        } else {
                            $hyouzi = mb_convert_encoding($Rfile["motoname"], "UTF-8", "SJIS-win");
                        }
                        $hyouziA = explode("_", $hyouzi);
                        $hyouziAA = explode(".", $hyouziA[3]);
                        $FileNo = $hyouziAA[0];
                        
                        $img = "<i class=\"fa fa-question-circle\"></i>";
                        
                        $link = "<div class=\"{$tooltip}\">".View::alink("#", $img, "onClick=\"$js\"")."</a><span>{$FileNo}</span></div>";

                    
                        $bfrname = $Rfile["name"];
                    }
                } elseif ($Rfile["gakko"] == $gakuName) {
                    $filename = urlencode($Rfile["motoname"]);

                    if ($j != 0) {
                        $url2 = "'".REQUESTROOT."/kyotuManual/{$filename}?date={$Rfile["date"]}'";
                        $js = "wopen({$url2},'{$nowMenuid}{$count}',0,0,screen.availWidth,screen.availHeight);";
                        //表示名のエンコードの種類を変える
                        if (mb_detect_encoding($Rfile["motoname"], "UTF-8,SJIS") == "UTF-8") {
                            $hyouzi = $Rfile["motoname"];
                        } else {
                            $hyouzi = mb_convert_encoding($Rfile["motoname"], "UTF-8", "SJIS-win");
                        }
                        $hyouziA = explode("_", $hyouzi);
                        $hyouziAA = explode(".", $hyouziA[3]);
                        $FileNo = $hyouziAA[0];
                        
                        $img = "<i class=\"fa fa-question-circle\"></i>";
                        $link = "<div class=\"{$tooltip}\">".View::alink("#", $img, "onClick=\"$js\"")."</a><span>{$FileNo}</span></div>";
                    } else {
                        $url2 = "'".REQUESTROOT."/sousaManual/{$filename}?date={$Rfile["date"]}'";
                        $js = "wopen({$url2},'{$nowMenuid}{$count}',0,0,screen.availWidth,screen.availHeight);";
                        $filenameA = explode(".", $Rfile["name"]);
                        $FileNo = $filenameA[0];
                        
                        $img = "<i class=\"fa fa-file-pdf-o\"></i>";
                        $userlink = "<div class=\"{$tooltip}\">".View::alink("#", $img, "onClick=\"$js\"")."</a><span>{$FileNo}</span></div>";
                    }
                    
                    $bfrname = $Rfile["name"];
                }
            }
            if ($link != "" || $userlink != "") {
                if ($userlink != "") {
                    $userLink .= $spaceU.$userlink;
                    $spaceU = "";
                } else {
                    $returnLink .= $spaceM.$link;
                    $spaceM = "";
                }
                $count++;
            }
        }
    }
    return array($returnLink, $userLink);
}

?>
