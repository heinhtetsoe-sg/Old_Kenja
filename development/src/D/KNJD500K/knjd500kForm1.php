<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd500kForm1.php 66290 2019-03-14 10:43:54Z yamashiro $
class knjd500kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd500kindex.php", "", "edit");

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

        //処理年度表示
        $arg["TOP"]["CONFIRMATION"] = $model->cntl_dt_year;

        //学年コンボ
        $query = knjd500kQuery::get_grade_data($model->cntl_dt_year);
        $opt = array();
        $opt[] = array("label" => "", "value" => "");        //先頭に空リストをセット

        if($query) {
            $result = $db->query($query);
            while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $Row["GRADE"], "value" => $Row["GRADE"]);
            }
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "gk_cmb",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"return btn_submit('');\"",
                            "value"       => $model->gk_cmb,
                            "options"     => $opt ) );

        $arg["TOP"]["GK_CMB"] = $objForm->ge("gk_cmb");

        //学期コンボ
        $query = knjd500kQuery::get_semester_data($model->cntl_dt_year);
        $opt = array();
        $opt[] = array("label" => "", "value" => "");         //先頭に空リストをセット

        if($query){
            $result = $db->query($query);
            while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $Row["SEMESTERNAME"], "value" => $Row["SEMESTER"]);
            }
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "sem_cmb",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"return btn_submit('');\"",
                            "value"       => $model->sem_cmb,
                            "options"     => $opt ) );

        $arg["TOP"]["SEM_CMB"] = $objForm->ge("sem_cmb");

        //区分コンボ
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $opt[] = array("label" => "中間", "value" => 1);
        $opt[] = array("label" => "期末", "value" => 2);
        $opt[] = array("label" => "平均", "value" => 3);

        $objForm->ae( array("type"        => "select",
                            "name"        => "div_cmb",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"return btn_submit('');\"",
                            "value"       => $model->div_cmb,
                            "options"     => $opt ) );

        $arg["TOP"]["DIV_CMB"] = $objForm->ge("div_cmb");


        //メインデータ取得
        if (strlen($model->gk_cmb) && strlen($model->sem_cmb) && strlen($model->div_cmb)) {
            if ($model->sem_cmb == "3" && $model->div_cmb != "2") {
                $query = "";
            } else {
                $query = knjd500kQuery::get_main_data($model);
                $result = $db->query($query);
            }                
        }
        //初期化
        $model->sem_div = "";
        $model->txt = array();
        $model->score = array();
        $counter = 0;          //一行ごとのカウント
        $inputableCount = 0;   //チェックボックス数
        $txt_cnt = 0;           //テキストボックス数

        $bgcolor = array("KK" => "bgcolor=\"#3399ff\"",       # 公 欠(青)
                         "KS" => "bgcolor=\"#ff0099\"" );     # 病 欠(赤)


        //指定された学期の項目名をセットする
        $cols = array();
        switch ($model->sem_cmb)
        {
            case "1":
                $cols = array("INTER" => "SEM1_INTER_REC", "TERM" => "SEM1_TERM_REC", "AVG" => "SEM1_REC"); break;
            case "2":
                $cols = array("INTER" => "SEM2_INTER_REC", "TERM" => "SEM2_TERM_REC", "AVG" => "SEM2_REC"); break;
            case "3":
                $cols = array("INTER" => "", "TERM" => "SEM3_TERM_REC", "AVG" => ""); break;
            default: break;
        }

        //指定された学期の、指定された区分のみの項目名をセットする
        switch ($model->div_cmb)
        {
            case "1":
                $model->sem_div = "SEM".$model->sem_cmb."_INTER_REC";
                break;
            case "2":
                $model->sem_div = "SEM".$model->sem_cmb."_TERM_REC";
                break;
            case "3":
                $model->sem_div = "SEM".$model->sem_cmb."_REC";
                break;
            default: break;
        }

        //データを整える
        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //背景色
            $Row["SEM1_INTER_REC_BG"] = $bgcolor[$Row["SEM1_INTER_REC_DI"]];
            $Row["SEM1_TERM_REC_BG"]  = $bgcolor[$Row["SEM1_TERM_REC_DI"]];
            $Row["SEM2_INTER_REC_BG"] = $bgcolor[$Row["SEM2_INTER_REC_DI"]];
            $Row["SEM2_TERM_REC_BG"]  = $bgcolor[$Row["SEM2_TERM_REC_DI"]];
            $Row["SEM3_TERM_REC_BG"]  = $bgcolor[$Row["SEM3_TERM_REC_DI"]];
 
            //初期化
            $ary_Trgt_Htn = array("base"   => "",      # 基準とする試験
                                  "target" => "",      # 補点する試験
                                  "term"   => false);  # 三学期の場合                
            $flg_check     = "";                       # チェックフラグ
            $flg_check_avg = "";                       # チェックフラグ(平均点用)
            $avg_menu = "";
            $base_name = "";
            $htn_I = $htn_T = $htn_A = "";             # 補点計算可能だったら値がセットされる(中間補点、期末補点、平均補点をそれぞれセット）

            if ($model->div_cmb == "1") {
                /*** 中間 ***/
                if($Row[$cols["TERM"]] != "") {
                    $flg_check = "checked";
                    $ary_Trgt_Htn["base"]   = $cols["TERM"];
                    $ary_Trgt_Htn["target"] = $cols["INTER"];
                }

                //期末の成績が入っていて中間に欠席していれば補点可能
                if($Row[$cols["INTER"]."_DI"] != "" && $ary_Trgt_Htn["base"] != "" && $ary_Trgt_Htn["target"] != "") {
                     $htn_T  = $db->getOne(knjd500kQuery::sec_avg($Row,$ary_Trgt_Htn,$model->cntl_dt_year,$model->sem_cmb, $Row[$cols["INTER"]."_DI"]));
                }
            }

            if ($model->div_cmb == "2") {
                /*** 期末 ***/
                if($Row[$cols["INTER"]] != "") {
                    $flg_check = "checked";
                    $ary_Trgt_Htn["base"]   = $cols["INTER"];
                    $ary_Trgt_Htn["target"] = $cols["TERM"];
                }

                //中間の成績が入っていて期末に欠席していれば補点可能
                if($Row[$cols["TERM"]."_DI"]!="" && $ary_Trgt_Htn["base"] != "" && $ary_Trgt_Htn["target"] != "") {
                    $htn_I  = $db->getOne(knjd500kQuery::sec_avg($Row,$ary_Trgt_Htn,$model->cntl_dt_year,$model->sem_cmb, $Row[$cols["TERM"]."_DI"]));
                }
            }

            if ($model->sem_cmb == "3") {
                /*** ３学期の場合 ***/
                if($Row["SEM1_REC"] != "" && $Row["SEM2_REC"] != "") {
                    $flg_check = "checked" ;
                    $ary_Trgt_Htn["term"] = true;
                }

                //１学期の平均と２学期の平均が入力されていて、３学期の期末に欠席していれば補点可能
                if ($Row[$cols["TERM"]."_DI"] != "" && $ary_Trgt_Htn["term"]){
                    $htn_T = $db->getOne(knjd500kQuery::sec3_avg($Row,$ary_Trgt_Htn,$model->cntl_dt_year,$Row[$cols["TERM"]."_DI"]));
                }
            }

            /*** 平均 ***/
            if ($model->div_cmb == "3") {

                if ($model->sem_cmb == "1") {
                    $base_name = "SEM2_REC";
                } elseif ($model->sem_cmb == "2") {
                    $base_name = "SEM1_REC";
                }
                
                if($Row[$cols["TERM"]] != "") {                
                    $flg_check_avg = "checked";
                    $avg_menu = 1;
                } elseif ($Row[$cols["INTER"]] == "" && $Row[$cols["TERM"]] == "" && $Row[$base_name] != "") {
                    $flg_check_avg = "checked";
                    $avg_menu = 2;
                    $ary_Trgt_Avg["base"]   = $base_name;
                    $ary_Trgt_Avg["target"] = $model->sem_div;
                }
                

                //補点クエリ実行(平均) --公欠と病欠の混在不可
                if($flg_check_avg == "" |
                    ($Row[$cols["INTER"]."_DI"] == "KK" && $Row[$cols["TERM"]."_DI"] == "KS") |
                    ($Row[$cols["INTER"]."_DI"] == "KS" && $Row[$cols["TERM"]."_DI"] == "KK") |
                    ($Row[$cols["TERM"]."_DI"] == "KK" && $Row[$cols["INTER"]."_DI"] == "KS") |
                    ($Row[$cols["TERM"]."_DI"] == "KS" && $Row[$cols["INTER"]."_DI"] == "KK")) {

                } else {
                    if ($avg_menu == 1) {

                        if($Row[$cols["INTER"]] != "") {
                            $htn_A = round(((int)$Row[$cols["INTER"]] + (int)$Row[$cols["TERM"]]) / 2);
                        }else{
                            $htn_A = $Row[$cols["TERM"]];
                        }

                    } elseif ($avg_menu == 2) {
                        if (strlen($Row[$cols["INTER"]."_DI"])) $di_cd = $Row[$cols["INTER"]."_DI"];
                        if (strlen($Row[$cols["TERM"]."_DI"]))  $di_cd = $Row[$cols["TERM"]."_DI"];

                        $htn_A = $db->getOne(knjd500kQuery::sec_rec_avg($Row,$ary_Trgt_Avg,$model->cntl_dt_year,$model->sem_cmb,$di_cd));
                    }
                }
            }

            //在籍期間(入学月日、卒業月日)の取得 2006/02/07
            $grd_cnt = $db->getOne(knjd500kQuery::getOnTheRegisterPeriod($model->cntl_dt_year,CTRL_DATE,$Row["SCHREGNO"]));
            //異動者情報の取得 2006/02/07
            $tans_cnt = $db->getOne(knjd500kQuery::getTransferData($model->cntl_dt_year,CTRL_DATE,$Row["SCHREGNO"]));
            if(0 < $grd_cnt || 0 < $tans_cnt){
                //対象者のバックカラーを黄色で表示する。 2006/02/07
                $Row["CHK_BOX_BG"]        = (strlen($Row["CHK_BOX_BG"])        ? $Row["CHK_BOX_BG"]        :"bgcolor=\"#ffff00\"");
                $Row["HR_SHOW_BG"]        = (strlen($Row["HR_SHOW_BG"])        ? $Row["HR_SHOW_BG"]        :"bgcolor=\"#ffff00\"");
                $Row["NAME_SHOW_BG"]      = (strlen($Row["NAME_SHOW_BG"])      ? $Row["NAME_SHOW_BG"]      :"bgcolor=\"#ffff00\"");
                $Row["SUBCLASSNAME_BG"]   = (strlen($Row["SUBCLASSNAME_BG"])   ? $Row["SUBCLASSNAME_BG"]   :"bgcolor=\"#ffff00\"");
                $Row["SEM1_INTER_REC_BG"] = (strlen($Row["SEM1_INTER_REC_BG"]) ? $Row["SEM1_INTER_REC_BG"] :"bgcolor=\"#ffff00\"");
                $Row["SEM1_TERM_REC_BG"]  = (strlen($Row["SEM1_TERM_REC_BG"])  ? $Row["SEM1_TERM_REC_BG"]  :"bgcolor=\"#ffff00\"");
                $Row["SEM1_REC_BG"]       = (strlen($Row["SEM1_REC_BG"])       ? $Row["SEM1_REC_BG"]       :"bgcolor=\"#ffff00\"");
                $Row["SEM2_INTER_REC_BG"] = (strlen($Row["SEM2_INTER_REC_BG"]) ? $Row["SEM2_INTER_REC_BG"] :"bgcolor=\"#ffff00\"");
                $Row["SEM2_TERM_REC_BG"]  = (strlen($Row["SEM2_TERM_REC_BG"])  ? $Row["SEM2_TERM_REC_BG"]  :"bgcolor=\"#ffff00\"");
                $Row["SEM2_REC_BG"]       = (strlen($Row["SEM2_REC_BG"])       ? $Row["SEM2_REC_BG"]       :"bgcolor=\"#ffff00\"");
                $Row["SEM3_TERM_REC_BG"]  = (strlen($Row["SEM3_TERM_REC_BG"])  ? $Row["SEM3_TERM_REC_BG"]  :"bgcolor=\"#ffff00\"");
                $Row["GRADE_RECORD_BG"]   = (strlen($Row["GRADE_RECORD_BG"])   ? $Row["GRADE_RECORD_BG"]   :"bgcolor=\"#ffff00\"");

                $flg_check_avg  ="";
                $flg_check      ="";

            }

            //補点入力可能な場合CheckBoxを作成;
            if(strlen($htn_I) | strlen($htn_T) | strlen($htn_A)) {
                //補点できる値をセット 2004/12/15 arakaki (近大-作業依頼書20041215)
                if($model->sem_cmb == "3") {
                    $htn = (strlen($htn_T) ? (int)$htn_T : "");
                }else{
                    if ($model->div_cmb == "1") {
                        $htn = (strlen($htn_T) ? (int)$htn_T : "");
                    } elseif ($model->div_cmb == "2") {
                        $htn = (strlen($htn_I) ? (int)$htn_I : "");
                    } else {
                        $htn = (strlen($htn_A) ? (int)$htn_A : "");
                    }
                }
                
                //補点値が100を超えていたら100に設定し直す
                $htn = ($htn >= 100 ? 100 : $htn);
                
                if ($model->div_cmb == "3") {
                    $chk = $flg_check_avg;
                } else {
                    $chk = $flg_check;
                }
                
                $objForm->ae( array("type"      => "checkbox",
                                    "name"      => "chk_box".$inputableCount,
                                    "value"     => $htn,
                                    "extrahtml" => "id=\"".$counter."\" ".$chk ));

                $Row["CHK_BOX"] = $objForm->ge("chk_box".$inputableCount);
                $inputableCount++;
            }

            if ($model->div_cmb != "3") {
                if ($Row[$model->sem_div."_DI"] != "") {
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "tbox".$txt_cnt,
                                        "size"      => "3",
                                        "maxlength" => "3",
                                        "value"     => $Row[$model->sem_div],
                                        "extrahtml" => "onblur=\"chk_Num(this)\" id=\"t".$counter."\""));
                    $Row[$model->sem_div] = $objForm->ge("tbox".$txt_cnt);
                  
                    $model->txt[$txt_cnt] = $Row["SCHREGNO"]."-".$Row["SUBCLASSCD"];  //データ取得用に項目名を保持
                    $txt_cnt++;
                }
            } else {
                if ($Row[$cols["INTER"]."_DI"] != "" || $Row[$cols["TERM"]."_DI"] != "") {
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "tbox".$txt_cnt,
                                        "size"      => "3",
                                        "maxlength" => "3",
                                        "value"     => $Row[$model->sem_div],
                                        "extrahtml" => "onblur=\"chk_Num(this)\" id=\"t".$counter."\""));
                    $Row[$model->sem_div] = $objForm->ge("tbox".$txt_cnt);

                    $model->txt[$txt_cnt] = $Row["SCHREGNO"]."-".$Row["SUBCLASSCD"];  //データ取得用に項目名を保持
                    $txt_cnt++;
                }
            }

            $counter++;
            $arg["data"][] = $Row;
        }

        //保存ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_prsv",
                            "value"     => " 保 存 ",
                            "extrahtml" => " onClick=\"return btn_submit('update');\"" ) );

        $arg["BUTTON"]["BTN_PRSV"] = $objForm->ge("btn_prsv");

        //取消ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_can",
                            "value"     => " 取 消 ",
                            "extrahtml" => " onClick=\"return btn_submit('cancel');\"" ));

        $arg["BUTTON"]["BTN_CAN"] = $objForm->ge("btn_can");

        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => " 終 了 ",
                            "extrahtml" => " onClick=\"return closeWin();\"" ));

        $arg["BUTTON"]["BTN_END"] = $objForm->ge("btn_end");

        //期毎補点計算ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_cal",
                            "value"     => "期毎補点計算",
                            "extrahtml" => " onClick=\"return setData();\"" ));

        $arg["BUTTON"]["BTN_CAL"] = $objForm->ge("btn_cal");

        //表示切替
        $chg_val = ($model->show_all == "on")?  "しない" : "" ;

        //補点処理済表示ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_showh",
                            "value"     => "補点処理済みを表示".$chg_val,
                            "extrahtml" => " onClick=\"return btn_submit('show_all');\"" ));

        $arg["BUTTON"]["BTN_SHOWH"] = $objForm->ge("btn_showh");

        //補点・補充後の評定フラグ設定
        $disabled = !$model->gk_cmb ? " disabled " : "";
        $link = REQUESTROOT."/D/KNJD210K/knjd210kindex.php?cmd=main&EXP_YEAR=".$model->exp_year."&SEND_GRADE=".$model->gk_cmb."&SEND_PRGID=".PROGRAMID."&SEND_AUTH=".AUTHORITY;
        $extra = "onClick=\"if (!confirm('保存されていないデータは破棄されます。処理を続行しますか？')) {return;}wopen('{$link}','SUBWIN2', 0, 0, window.outerWidth, window.outerHeight);\"";
        $arg["BUTTON"]["BTN_KNJD210K"] = KnjCreateBtn($objForm, "BTN_KNJD210K", "補点・補充後の評定フラグ設定", $disabled.$extra);

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ));

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "shw_flg",
                            "value"     => $model->show_all ));

        //hiddenを作成(補点データ数カウント)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "dataCount",
                            "value"     => $inputableCount ));
        
        //hiddenを作成(入力可能テキスト)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "txtCount",
                            "value"     => $txt_cnt ));

        Query::dbCheckIn($db);

        //処理が完了、又は権限が無ければ閉じる。
        if($model->cntl_dt_year == ""){
            $arg["Closing"] = "  closing_window('year'); " ;
        }else if($model->sec_competence == DEF_NOAUTH){
            $arg["Closing"] = "  closing_window('cm'); " ;
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd500kForm1.html", $arg);
    }
}

?>
