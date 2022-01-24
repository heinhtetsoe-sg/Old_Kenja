<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd120oForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $objUp = new csvFile();

        //CSVヘッダ名
        $header = array("0" => "科目コード",
                        "1" => "科目名",
                        "2" => "講座コード",
                        "3" => "講座名",
                        "4" => "学籍番号",
                        "5" => "クラス－出席番",
                        "6" => "氏名",
                        "0111" => "前期－中間－素点",
                        "0121" => "前期－期末－素点",
                        "0211" => "後期－中間－素点",
                        "0221" => "後期－期末－素点",
                        "0882" => "学年評定");

        $objUp->setHeader(array_values($header));
        
        $arg["start"]    = $objForm->get_start("main", "POST", "knjd120oindex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $arg["useProvFlg"] = "1";
        } else {
            $arg["not_useProvFlg"] = "1";
        }

        //新規作成
        if ($model->cmd != "subclasscd") {
            $db->query(knjd120oQuery::insertExStdRecQuery($model));
        }

        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd120oQuery::selectSubclassQuery($model->gen_ed, $model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],
                                        "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]);
                if ($row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"] == $model->field["SUBCLASSCD"]) {
                    $subclassname = $row["SUBCLASSNAME"];
                }
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
                if ($row["SUBCLASSCD"] == $model->field["SUBCLASSCD"]) {
                    $subclassname = $row["SUBCLASSNAME"];
                }
            }
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["SUBCLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('subclasscd')\";"));
        $arg["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd120oQuery::selectChairQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"]);
            if ($row["CHAIRCD"] == $model->field["CHAIRCD"]) {
                $chairname = $row["CHAIRNAME"];
            }
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('chaircd')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");

        //CSV出力ファイル名
        $objUp->setFileName(CTRL_YEAR."年度_".$subclassname."_".$chairname."_"."成績入力(前後期制).csv");

        $backcolor = array( "1"  => "#3399ff",
                            "2"  => "#66cc33",
                            "3"  => "#66cc33",
                            "4"  => "#ff0099",
                            "5"  => "#ff0099",
                            "6"  => "#ff0099",
                            "8"  => "#3399ff",
                            "9"  => "#66cc33",
                            "10" => "#66cc33",
                            "11" => "#ff0099",
                            "12" => "#ff0099",
                            "13" => "#ff0099",
                            "14" => "#ff0099");
                            
        //試験名称NAMECD2
        $ctrl_name = array("0111" => "SEM1_INTR_SCORE"
                          ,"0121" => "SEM1_TERM_SCORE"
                          ,"0182" => "SEM1_VALUE"
                          ,"0211" => "SEM2_INTR_SCORE"
                          ,"0221" => "SEM2_TERM_SCORE"
                          ,"0282" => "SEM2_VALUE"
                          ,"0882" => "GRAD_VALUE");

        //考査満点マスタのコード
        $perfectcd = array("0111" => "10101"
                          ,"0121" => "10201"
                          ,"0182" => "19900"
                          ,"0211" => "20101"
                          ,"0221" => "20201"
                          ,"0282" => "29900"
                          ,"0882" => "99900");

        //管理者コントロール
        $admin_key = array();
        $result = $db->query(knjd120oQuery::selectContolCodeQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];

            //NO001
            $admin_key2[$row["CONTROL_CODE"]]=$row["CONTROL_CODE"];
            //NO001
        }
        //初期化
        $model->data=array();
        $counter=0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd120oQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号をHiddenで保持
            knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]."-".$row["TAKESEMES"]);

            //クラス-出席番(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }
            //名前
            $row["INOUTCD"]     = ($row["INOUTCD"] == '1')? "☆": "　";
            $row["NAME_SHOW"]   = $row["SCHREGNO"]." ".htmlspecialchars($row["NAME_SHOW"]);

            //書き出し用CSVデータ
            $csv = array($model->field["SUBCLASSCD"],
                        $subclassname,
                        $model->field["CHAIRCD"],
                        $chairname,
                        $row["SCHREGNO"],
                        $row["ATTENDNO"],
                        $row["NAME_SHOW"]);

            //キー値をセット
            $key = array("科目コード" => $model->field["SUBCLASSCD"],
                        "講座コード" => $model->field["CHAIRCD"],
                        "学籍番号"   => $row["SCHREGNO"]);

            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                //ゼロ埋めフラグ
                $flg = array("科目コード" => array(false,13),
                             "講座コード" => array(true,7),
                             "学籍番号"   => array(true,8));
            } else {
                //ゼロ埋めフラグ
                $flg = array("科目コード" => array(true,6),
                             "講座コード" => array(true,7),
                             "学籍番号"   => array(true,8));
            }

            $objUp->setEmbed_flg($flg);
            $objUp->setType(array(7=>'S',8=>'S',9=>'S',10=>'S',11=>'S',12=>'S'));
            $objUp->setSize(array(7=>3,8=>3,9=>3,10=>3,11=>3,12=>3));

            $tmp = array();
            //平均値
            if ($row["SEM1_INTR_SCORE"] != "" && $row["SEM1_TERM_SCORE"] != "") {
                $tmp[] = (int)$row["SEM1_INTR_SCORE"];
                $tmp[] = (int)$row["SEM1_TERM_SCORE"];
            } elseif ($row["SEM1_INTR_SCORE"] == "" && $row["SEM1_TERM_SCORE"] != "") {
                $tmp[] = (int)$row["SEM1_TERM_SCORE"];
            }
            $row["SEM1_AVG"] = (get_count($tmp) > 0)? array_sum($tmp)/get_count($tmp):"";

            $tmp2 = array();
            //平均値
            if ($row["SEM2_INTR_SCORE"] != "" && $row["SEM2_TERM_SCORE"] != "") {
                $tmp2[] = (int)$row["SEM2_INTR_SCORE"];
                $tmp2[] = (int)$row["SEM2_TERM_SCORE"];
            } elseif ($row["SEM2_INTR_SCORE"] == "" && $row["SEM2_TERM_SCORE"] != "") {
                $tmp2[] = (int)$row["SEM2_TERM_SCORE"];
            }
            $row["SEM2_AVG"] = (get_count($tmp2) > 0)? array_sum($tmp2)/get_count($tmp2):"";

            $row["ALL_AVG"] = (get_count($tmp1)+get_count($tmp2) > 0)? round((array_sum($tmp)+array_sum($tmp2))/(get_count($tmp)+get_count($tmp2)), 1):"";

            //平均集計
            if (is_numeric($row["SEM1_AVG"])) {
                $term_data["SEM1_AVG"][] = (float)$row["SEM1_AVG"];
                $term_data["SEM2_AVG"][] = (float)$row["SEM2_AVG"];
                $term_data["ALL_AVG"][]  = (float)$row["ALL_AVG"];
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //入力可能なテキストの名前を取得する
            $setTextField = "";
            $textSep = "";
            foreach ($ctrl_name as $code => $col) {
                $edit_flg = true;  //テキストボックス表示フラグ
                //在籍情報がない場合
                if (!isset($row["CHAIR_STD".$sem]) || $row["CHAIR_STD".$sem] == "") {
                    $edit_flg = false;
                }
                //ラベルのみ
                if ((!$edit_flg && (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT))) {
                    $setTextField .= "";
                } elseif (in_array($code, $admin_key)) {
                    $setTextField .= $textSep.$col."-";
                    $textSep = ",";
                }
            }
            
            //各項目を作成
            foreach ($ctrl_name as $code => $col) {
                //学期成績集計項目
                if (is_numeric($row[$col])) {
                    $term_data[$col][] = (int)$row[$col];
                }

                $edit_flg = true;  //テキストボックス表示フラグ
                $sem = substr($code, 1, 1);
                $row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

                if (strlen($row[$col."_ATTEND"]) && $row[$col."_ATTEND"] > 0) {
                    $row[$col."_COLOR"] = $backcolor[$row[$col."_ATTEND"]];
                }
                //異動期間がテスト実施の範囲内
                if ($row["TRANSFER_SDATE".$sem]) {
                    $row[$col."_COLOR"] = "#ffff00";
                }
                //除籍日付がテスト実施の範囲内
                if ($row["GRD_DATE".$sem]) {
                    $row[$col."_COLOR"] = "#ffff00";
                }
                //在籍情報がない場合
                if (!isset($row["CHAIR_STD".$sem]) || $row["CHAIR_STD".$sem] == "") {
                    $edit_flg = false;
                    if ($sem <= CTRL_SEMESTER) {
                        $row[$col."_COLOR"]="#ffff00";
                    }
                }
                if ($col != "SEM1_VALUE" && $col != "SEM2_VALUE") {
                    //CSV書き出し
                    $csv[] = $row[$col];
                }
                
                $controlFlg = '';
                //ラベルのみ
                if ((!$edit_flg && (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT))) {
                    //hidden
                    knjCreateHidden($objForm, $col."-".$counter, $row[$col]);
                    $row[$col] = "<font color=\"#000000\">".$row[$col]."</font>";
                } elseif (in_array($code, $admin_key)) {
                    $controlFlg = '1';
                    //入力エリアとキーをセットする
                    $objUp->setElementsValue($col."-".$counter, $header[$code], $key);
                    $value = $row[$col];

                    $id = $col .":".$counter;
                    //テキストボックスを作成
                    $objForm->ae(array("type"      => "text",
                                        "name"      => $col."-".$counter,
                                        "size"      => "3",
                                        "maxlength" => "3",
                                        "value"     => $value,
                                        "extrahtml" => " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" id=\"$id\""));
                    $row[$col] = $objForm->ge($col."-".$counter);

                    //考査満点マスタ
                    $query = knjd120oQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $perfectcd[$code], $row["GRADE"], $row["COURSE"], $model);
                    $perfect = ($model->usePerfect == 'true') ? $db->getOne($query) : 200;
                    if ($perfect == "") {
                        $perfect = 200;
                    }
                    //テキストボックスを作成
                    $objForm->ae(array("type"      => "hidden",
                                        "name"      => $col."_PERFECT"."-".$counter,
                                        "value"     => $perfect ));
                } else {
                    //hidden
                    knjCreateHidden($objForm, $col."-".$counter, $row[$col]);
                }
                //仮評定フラグ対応
                if ($model->Properties["useProvFlg"] == '1') {
                    if ($col == "GRAD_VALUE") {
                        $chk = $row["PROV_FLG"] == '1' ? ' checked="checked" ' : '';
                        $dis = $controlFlg == '1' ? '' : ' disabled="disabled" ';
                        $row["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG"."-".$counter, "1", $chk.$dis);
                    }
                }
            }
            $objUp->addCsvValue($csv);
            $row["SEM1_AVG_ID"] = "SEM1_AVG:". $counter;
            $row["SEM2_AVG_ID"] = "SEM2_AVG:". $counter;
            $row["ALL_AVG_ID"] = "ALL_AVG:". $counter;
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            
            $counter++;
            $arg["data"][] = $row;
        }

        knjCreateHidden($objForm, "COUNT", $counter);

        if (is_array($term_data)) {
            //学期ごとの集計
            foreach ($term_data as $key => $v) {
                //合計
                $arg[$key."_SUM"]=array_sum($v);
                //平均
                $arg[$key."_AVG"]=round((array_sum($v)/get_count($v)), 1);

                //最高点と最低点を求める
                array_multisort($v, SORT_NUMERIC);
                $max = get_count($v)-1;
                //最高点
                $arg[$key."_MAX"]=$v[$max];
                //最低点
                $arg[$key."_MIN"]=$v[0];
            }
        }
        
        // NO001 "成績入力完了"処理 追加修正 //

        /*****
        checkbox(成績入力完了)の要不要の判断基準が無い為
        1学期の中間期末、2学期の中間期末を固定で表示する
        ******/

        //初期化
        $j = 1;
        $chkDbValue = array();
        $chngChk = array( "0" => "",
                          "1" => "checked",
                          "" => "" );

        if ($model->field["CHK_FLG"] == "reset") {
            #echo "Do Reset !<BR>";

            $chkDbValue = array( "10101" => null,
                                 "10201" => null,
                                 "20101" => null,
                                 "20201" => null  );

            $ableflg["10101"] = (isset($admin_key2["0111"]))? "" : "disabled" ;
            $ableflg["10201"] = (isset($admin_key2["0121"]))? "" : "disabled" ;
            $ableflg["20101"] = (isset($admin_key2["0211"]))? "" : "disabled" ;
            $ableflg["20201"] = (isset($admin_key2["0221"]))? "" : "disabled" ;

            $model->field["CHK_FLG"] = "";

            //データ取得
            $query = knjd120oquery::getSchChrTestData($model->field["CHAIRCD"]);
            #var_dump($query);
            $result3 = $db->query($query);
            while ($row = $result3->fetchRow(DB_FETCHMODE_ASSOC)) {
                $ExecutedValue = ($row["EXECUTED"] == "1")? 1 : 0;
                $StartchkDbValue = (is_null($chkDbValue[$row["TESTKIND"]]))? 1 : $chkDbValue[$row["TESTKIND"]];
                $chkDbValue[$row["TESTKIND"]] = $StartchkDbValue * $ExecutedValue;
            }
            $result3->free();
        } else {
            #echo "Default <BR>";

            for ($k=1; $k<=4; $k++) {
                $chkDbValue[$k] = ($model->field["CHK_COMP".$i] == "on")? "1" : "0";
            }
        }

        //チェックボックス作成
        foreach ($chkDbValue as $key => $value) {
            $objForm->ae(
                array("type"     => "checkbox",
                                "name"     => "CHK_COMP".$j,
                                "value"    => "on",
                                "extrahtml"=> $chngChk["$value"]." ".$ableflg[$key]." " )
            );

            $arg["CHK_COMP".$j] = $objForm->ge("CHK_COMP".$j);
            $j++;
        }

        /********************************************/
        /* 評定・評価の成績入力完了チェックボックス */
        /********************************************/
        $testcdArray = array("0182" => "19900"
                            ,"0282" => "29900"
                            ,"0882" => "99900");

        //初期化
        $j = 1;
        foreach ($testcdArray as $testkey => $testval) {
            $chk = '';
            $dis = '';
            $query = knjd120oQuery::getRecordChkfinDat($model, $testval);

            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow)) {
                if ($resultRow["EXECUTED"] == '1') {
                    $chk = ' checked="checked" ';
                } else {
                    $chk = '';
                }
            }
            $dis = isset($admin_key2[$testkey]) ? '' : ' disabled="disabled" ';
            $objForm->ae(
                array("type"      => "checkbox",
                               "name"      => "CHK_COMP_VALUE".$j,
                               "value"     => "on",
                               "extrahtml" => $chk.$dis)
            );

            $arg["CHK_COMP_VALUE".$j] = $objForm->ge("CHK_COMP_VALUE".$j);
            $j++;
        }


        //変更保存用hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CHK_FLG",
                            "value"     => $model->field["CHK_FLG"]
                            ));
                            
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        // NO001 "成績入力完了"処理 おわり //


        Query::dbCheckIn($db);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ));
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae(array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "印 刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ));
        $arg["btn_print"] = $objForm->ge("btn_print");

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "gen_ed",
                            "value"     => ($model->field["SUBCLASSCD"] == $model->gen_ed ? $model->gen_ed : "") ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD124" ));//2005.05.31Modify---alp nakamoto
//                            "value"     => "KNJD122" )  );

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => CTRL_SEMESTER ));
        //2005.05.31Add---alp nakamoto
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "STAFF",
                            "value"     => STAFFCD ));

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd120oForm1.html", $arg);
    }
}
