<?php

require_once('for_php7.php');

class knjd520kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd520kindex.php", "", "main");

        //権限チェック
        switch($model->sec_competence)
        {
            case DEF_UPDATABLE:       //更新可
                $error_mode["cp"] = false;
                break;
            case DEF_UPDATE_RESTRICT: //制限付更新可
            case DEF_REFER_RESTRICT:  //制限付参照
            case DEF_REFERABLE:       //参照のみ
            case DEF_NOAUTH:          //権限無し
            default:
                $error_mode["cp"] = true;
                break;
        }

        $db = Query::dbCheckOut();

        //成績反映ボタンを押し不可にする
        $disabled = "disabled";

        //処理年度表示
        $arg["TOP"]["CONFIRMATION"] = $model->cntl_dt_year;

        //学年コンボ
        $query = knjd520kQuery::get_grade_data($model->cntl_dt_year);
        $opt = array();

        if($query) {
            $result = $db->query($query);
            while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $Row["GRADE"], "value" => $Row["GRADE"]);
            }
        }

         //コンボの初期設定(起動時)
        if($model->gk_cmb == ""){
           $model->gk_cmb = $opt[0]["value"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "gk_cmb",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"return btn_submit('main');\"",
                            "value"       => $model->gk_cmb,
                            "options"     => $opt ) );

        $arg["TOP"]["GK_CMB"] = $objForm->ge("gk_cmb");

        //科目コンボ
        $query = knjd520kQuery::get_subject_data($model->cntl_dt_year, $model->cntl_semester, $model->gk_cmb, $model);
        $opt = array();
        $opt[] = array("label" => "　　　　　　　　　", "value" => "");         //先頭に空リストをセット

        if($query){
            $exist = $db->getOne($query);

            //科目が存在するならデータを追加
            if (isset($exist)) {
                $result = $db->query($query);
                $opt[] = array("label" => "全科目表示", "value" => "0");
                while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $opt[] = array("label" => $Row["SUBCLASSCD"] . " " . $Row["SUBCLASSABBV"],
                                   "value" => $Row["SUBCLASSCD"]);
                }
            }
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "sub_cmb",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"return btn_submit('main');\"",
                            "value"       => $model->sub_cmb,
                            "options"     => $opt ) );

        $arg["TOP"]["SUB_CMB"] = $objForm->ge("sub_cmb");


        //表示画面作成
        if ($model->sub_cmb == "0") {
            //成績反映ボタンのdisabled解除
            $disabled = "";

            //追試対象の科目を取得
            $sub_code = array();
            $result = $db->query(knjd520kQuery::get_subject_data($model->cntl_dt_year, $model->cntl_semester, $model->gk_cmb, $model));
            while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $sub_code[] = array("SUBCLASSCD" => $Row["SUBCLASSCD"],
                                    "SUB_NAME"   => $Row["SUBCLASSABBV"],
                                    "ELECTDIV"   => $Row["ELECTDIV"] );
            }

            //表示するデータを変えるために添え字を計算する
            if ($model->flg == 1) {
                $model->num = $model->num - 12;
            } elseif ($model->flg == 2) {
                $model->num = $model->num + 12;
            }

            //表示する科目名の設定
            $sp_flg = 0;
            $disp = array();
            $model->range_cd = array();
            for($j=$model->num; $j<get_count($sub_code); $j++)
            {
                //データが12件超えたらストップ(１画面に表示できる科目数 = 12)
                if ($sp_flg == 12) {
                    break;
                }

                //選択科目かどうかを判断
                if ($sub_code[$j]["ELECTDIV"] != "1") {
                    $elect_color = "bgcolor=\"#316f9b\"";
                } else {
                    $elect_color = "bgcolor=\"#008080\"";
                }

                $arg["disp"][] = array("NAME"         => $sub_code[$j]["SUB_NAME"],
                                       "ELECT_COLOR"  => $elect_color );

                $model->range_cd[] = $sub_code[$j]["SUBCLASSCD"];
                $sp_flg++;
            }

            //追試対象者のデータを取得
            $supp_name = array();
            $result = $db->query(knjd520kQuery::get_supp_name($model->cntl_dt_year, $model->cntl_semester, $model->gk_cmb));

            $i = 0;
            while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $arg["schregno"][$i] = array("SCHREGNO"  => $Row["SCHREGNO"],
                                             "NAME_SHOW" => $Row["NAME_SHOW"],
                                             "CLASSNO"   => $Row["HR_NAMEABBV"] ."-". $Row["ATTENDNO"],
                                             "SUBCLASSCD" => array());

                //生徒毎の追試験対象の全科目数を取得
                $arg["schregno"][$i]["SUB_CNT"] = $db->getOne(knjd520kQuery::get_sub_get_count($model->cntl_dt_year, $Row["SCHREGNO"], 1, $model));

                //生徒毎の追試験対象の選択科目数を取得
                $ele_cnt = $db->getOne(knjd520kQuery::get_sub_get_count($model->cntl_dt_year, $Row["SCHREGNO"], 2, $model));
                $arg["schregno"][$i]["ELE_CNT"] = "";

                if ($ele_cnt != "") {
                    $arg["schregno"][$i]["ELE_CNT"] = "(" .$ele_cnt. ")";
                }

                //科目数分の配列を初期化
                $sp_flg = 0;
                for($j=$model->num; $j<get_count($sub_code); $j++)
                {
                    if ($sp_flg == 12) {
                        break 1;
                    }
                    $arg["schregno"][$i]["SUBCLASSCD"][$sp_flg]["CODE"] = $sub_code[$j]["SUBCLASSCD"];
                    $arg["schregno"][$i]["SUBCLASSCD"][$sp_flg]["VALUE"] = "";
                    $arg["schregno"][$i]["SUBCLASSCD"][$sp_flg]["BGCOLOR"] = "";
                    $sp_flg++;
                }
                $i++;
            }

            //表示を整えるためにwidthとcolspanを計算
            if ($sp_flg == 1) {
                $arg["data"]["width"]  = "width=\"391\"";
                $arg["data"]["width2"] = "width=\"65\"";
                $arg["data"]["width3"] = "width=\"65\"";
                $arg["data"]["width4"] = "width=\"59\"";
            } else {
                $arg["data"]["width"]  = "width=\"".($sp_flg * 53 + 326). "\"";
                $arg["data"]["width2"] = "width=\"" .($sp_flg * 53). "\"";
                $arg["data"]["width3"] = "width=\"53\"";
                $arg["data"]["width4"] = "width=\"47\"";
            }
            $arg["data"]["colspan"] = "colspan=\"" .($sp_flg + 3). "\"";
            $arg["data"]["colspan2"] = "colspan=\"" .$sp_flg. "\"";

            //メインデータ取得
            for($i=0; $i<get_count($arg["schregno"]); $i++)
            {

                $result = $db->query(knjd520kQuery::get_main_data($model->cntl_dt_year, $arg["schregno"][$i]["SCHREGNO"], $model->range_cd, $model));
                while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {

                    //if (!is_null($Row["SCORE"])) {                //2005/01/17 arakaki
                    if ($Row["SCORE"]!="") {
                        $score = $Row["SCORE"];
                    //} elseif (!is_null($Row["DI_MARK"])) {        //2005/01/17 arakaki
                    } elseif ($Row["DI_MARK"]!="") {
                        $score = $Row["DI_MARK"];
                    } else {
                        $score = "";
                    }
                    $sp_flg = 0;
                    for($j=$model->num; $j<get_count($sub_code); $j++)
                    {
                        //データが12件超えたらストップ
                        if ($sp_flg == 12) {
                            break 1;
                        }
                        //取得したデータが表示対象の科目と一致するか調べる
                        if ($arg["schregno"][$i]["SUBCLASSCD"][$sp_flg]["CODE"] == $Row["SUBCLASSCD"]) {
                            $arg["schregno"][$i]["SUBCLASSCD"][$sp_flg]["VALUE"] = $score;

                            //背景色の設定
                            if ($score == "KK") {
                                $bgcolor = "bgcolor=\"#3399ff\"";
                            } elseif ($score == "KS") {
                                $bgcolor = "bgcolor=\"#ff0099\"";
                            //} elseif ((!is_null($Row["SCORE"])) && $Row["SCORE"] < $Row["TYPE_ASSES_LOW"]) {  //2005/01/17 arakaki
                            } elseif (($Row["SCORE"]!="") && $Row["SCORE"] < $Row["TYPE_ASSES_LOW"]) {
                                $bgcolor = "bgcolor=\"#ff0000\""; 
                            } elseif ($Row["JUDGE_PATTERN"] == "A") {
                                $bgcolor = "bgcolor=\"#ffff00\""; 
                            } elseif($Row["JUDGE_PATTERN"] == "B") {
                                $bgcolor = "bgcolor=\"#00ff00\"";
                            } elseif($Row["JUDGE_PATTERN"] == "C") {
                                $bgcolor = "bgcolor=\"#ff00ff\"";
                            } else {
                                $bgcolor = "";
                            }

                            $arg["schregno"][$i]["SUBCLASSCD"][$sp_flg]["BGCOLOR"] = $bgcolor;
                        }

                        $sp_flg++;
                    }
                }
            }

           //前ページにデータがある場合にリンク設定
           if ($model->num != 0) {
               //共通関数View::alinkを使うとリンクの色を指定できないので使わずに設定
               $hash = "knjd520kindex.php?cmd=main&FLG=1&sub_cmb=0&gk_cmb=" .$model->gk_cmb;

               $arg["hash1"] = "<a href=\"" .$hash. "\" target=\"_self\" >";
               $arg["end_link1"] = "</a>";
           }
           //次ページにデータがある場合にリンク設定
           if (isset($sub_code[$j]["SUBCLASSCD"])) {
               $hash = "knjd520kindex.php?cmd=main&FLG=2&sub_cmb=0&gk_cmb=" .$model->gk_cmb;

               $arg["hash2"] = "<a href=\"" .$hash. "\" target=\"_self\" >";
               $arg["end_link2"] = "</a>";
           }

 
        //編集画面作成
        } elseif ($model->sub_cmb != "") {
            //編集対象の科目名を取得
            $query = knjd520kQuery::get_edit_data($model->cntl_dt_year, $model->sub_cmb, $model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["EDIT_NAME"] = $Row["SUBCLASSABBV"];

            //選択科目かどうかを判断
            if ($Row["ELECTDIV"] != "1") {
                $elect_color = "bgcolor=\"#316f9b\"";
            } else {
                $elect_color = "bgcolor=\"#008080\"";
            }

            $arg["ELECT_COLOR"] = $elect_color;

            //編集用メインデータ取得
            $query = knjd520kQuery::edit_main_data($model->cntl_dt_year, $model->cntl_semester, $model->gk_cmb, $model->sub_cmb, $model);

            $result = $db->query($query);

            $cnt = 0;
            $model->schregno_cnt = array();
            while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {

                //if (!is_null($Row["SCORE"])) {                //2005/01/17 arakaki
                if ($Row["SCORE"]!="") {
                    $score = $Row["SCORE"];
                //} elseif (!is_null($Row["DI_MARK"])) {        //2005/01/17 arakaki
                } elseif ($Row["DI_MARK"]!="") {
                    $score = $Row["DI_MARK"];
                } else {
                    $score = "";
                }

                //背景色の設定
                if ($score == "KK") {
                    $bgcolor = "bgcolor=\"#3399ff\"";
                } elseif ($score == "KS") {
                    $bgcolor = "bgcolor=\"#ff0099\"";
                //} elseif ((!is_null($Row["SCORE"])) && $Row["SCORE"] < $Row["TYPE_ASSES_LOW"]) {
                } elseif (($Row["SCORE"]!="") && $Row["SCORE"] < $Row["TYPE_ASSES_LOW"]) {
                    $bgcolor = "bgcolor=\"#ff0000\""; 
                } elseif ($Row["JUDGE_PATTERN"] == "A") {
                    $bgcolor = "bgcolor=\"#ffff00\"";
                } elseif($Row["JUDGE_PATTERN"] == "B") {
                    $bgcolor = "bgcolor=\"#00ff00\"";
                } elseif($Row["JUDGE_PATTERN"] == "C") {
                    $bgcolor = "bgcolor=\"#ff00ff\"";
                } else {
                    $bgcolor = "";
                }

                $schregno = $Row["SCHREGNO"];
                $classno = $Row["HR_NAMEABBV"] ."-". $Row["ATTENDNO"];

                //生徒毎の追試験対象の全科目数を取得
                $count = $db->getOne(knjd520kQuery::get_sub_get_count($model->cntl_dt_year, $schregno, 1, $model));

                //生徒毎の追試験対象の選択科目数を取得
                $ele_cnt = $db->getOne(knjd520kQuery::get_sub_get_count($model->cntl_dt_year, $schregno, 2, $model));

                if ($ele_cnt != "") {
                    $ele_cnt = "(" .$ele_cnt. ")";
                }

                //編集用テキストの作成
                $objForm->ae( array("type"      => "text",
                                    "name"      => "edit_text".$cnt,
                                    "value"     => $score,
                                    "size"      => "5",
                                    "maxlength" => "3",
                                    "extrahtml" => "STYLE=\"text-align: right\" onblur=\"calc(this);\" "));

                $arg["edit"][$cnt] = array("CLASSNO"    => $classno,
                                           "NAME_SHOW"  => $Row["NAME_SHOW"],
                                           "EDIT_TEXT"  => $objForm->ge("edit_text".$cnt),
                                           "SUB_CNT"    => $count,
                                           "ELE_CNT"    => $ele_cnt,
                                           "BGCOLOR"    => $bgcolor );

                $model->schregno_cnt[$cnt] = $Row["SCHREGNO"];
                $cnt++;
            }

            //保存ボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_pre",
                                "value"     => "保存",
                                "extrahtml" => " onClick=\"return btn_submit('update');\"" ));

            $arg["BUTTON2"]["BTN_PRE"] = $objForm->ge("btn_pre");

            //取消ボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_cancel",
                                "value"     => "取消",
                                "extrahtml" => " onClick=\"return btn_submit('cancel');\"" ));

            $arg["BUTTON2"]["BTN_CANCEL"] = $objForm->ge("btn_cancel");

            //戻るボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_back",
                                "value"     => "戻る",
                                "extrahtml" => " onClick=\"location.href='knjd520kindex.php?sub_cmb=0'\"" ));
            $arg["BUTTON2"]["BTN_BACK"] = $objForm->ge("btn_back");


        } else {
            $arg["virtual"] = true;
        }

        //編集画面以外の時に作成
        if ($model->sub_cmb == "" || $model->sub_cmb == "0") {
            //終了ボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_end",
                                "value"     => " 終了 ",
                                "extrahtml" => " onClick=\"return closeWin();\"" ));

            $arg["BUTTON1"]["BTN_END"] = $objForm->ge("btn_end");

            //成績反映ボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_reflect",
                                "value"     => " 成績反映 ",
                                "extrahtml" => $disabled." onClick=\"return btn_submit('reflect');\"" ));

            $arg["BUTTON1"]["BTN_REFLECT"] = $objForm->ge("btn_reflect");
        }

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "num",
                            "value"     => $model->num ));

        Query::dbCheckIn($db);

        //処理が完了、又は権限が無ければ閉じる。
        if($model->cntl_dt_year == ""){
            $arg["Closing"] = "  closing_window('year'); " ;
        }else if($model->sec_competence == DEF_NOAUTH){
            $arg["Closing"] = "  closing_window('cm'); " ;
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd520kForm1.html", $arg);
    }
}

?>
