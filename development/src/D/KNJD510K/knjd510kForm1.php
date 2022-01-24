<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd510kForm1.php 66290 2019-03-14 10:43:54Z yamashiro $
class knjd510kForm1
{
    function main(&$model)
    {
        /* debug------ */
            #error_reporting(E_ALL);
            #echo $model->cntl_dt_year;
            #echo $model->sem_cmb;
            #echo $model->show_all;
        /* ----debug end */

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd510kindex.php", "", "edit");

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

        //学年コンボボックスデータ取得
        $query = knjd510kQuery::get_grade_data($model->cntl_dt_year);
        $opt = array();
        if($query)
        {
            //初期値は空
            $opt[] = array("label" => "","value" => "");
            
            //学年コンボボックスデータ作成
            $result = $db->query($query);
            while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $Row["GRADE"], 
                               "value" => $Row["GRADE"]);
            }
        }

        //学年コンボボックス作成
        $objForm->ae( array("type"        => "select",
                            "name"        => "gk_cmb",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"return btn_submit('');\"",
                            "value"       => $model->gk_cmb,
                            "options"     => $opt ) );

        $arg["TOP"]["GK_CMB"] = $objForm->ge("gk_cmb");

        //学期コンボ
        $query = knjd510kQuery::get_semester_data($model->cntl_dt_year);
        $opt = array();
        if($query){
            //初期値は空
            $opt[] = array("label" => "","value" => "");
            //学年コンボボックスデータ作成
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

        //メインデータ取得
        if (strlen($model->gk_cmb) && strlen($model->sem_cmb)) {
            $query  = knjd510kQuery::get_main_data($model->gk_cmb, $model->cntl_dt_year, $model->show_all);
            $result = $db->query($query);
        }

        $data_count  = 0;      #レコード数
        $tx_count    = 0;      #平均用テキストボックス数

        $bgcolor = array("KK" => "bgcolor=\"#3399ff\"",       # 公 欠(青)
                         "KS" => "bgcolor=\"#ff0099\"" );     # 病 欠(赤)

        //項目名
        $term = array("SEM1_INTER_REC","SEM1_TERM_REC","SEM1_REC","SEM2_INTER_REC","SEM2_TERM_REC","SEM2_REC","SEM3_TERM_REC");

        $model->txt_names = $model->off_chk_box = array();

        //テキストボックス
        $txt_term = array();
        if ($model->sem_cmb == "1") {
            $txt_term = array("SEM1_INTER_REC","SEM1_TERM_REC","SEM1_REC");
        } elseif ($model->sem_cmb == "2") {
            $txt_term = array("SEM1_INTER_REC","SEM1_TERM_REC","SEM1_REC","SEM2_INTER_REC","SEM2_TERM_REC","SEM2_REC");
        } elseif ($model->sem_cmb == "3") {
            $txt_term = array("SEM1_INTER_REC","SEM1_TERM_REC","SEM1_REC","SEM2_INTER_REC","SEM2_TERM_REC","SEM2_REC","SEM3_TERM_REC");
        }

        //データを整える
        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $txt_flg = array();
            
            foreach ($term as $col)
            {
                //更新時エラーの場合前回入力した値を戻す
                if($model->warning)
                {

                //各学期のテストに出欠情報があればテキストボックスにする
                } elseif ($Row[$col."_DI"] == "KK" | $Row[$col."_DI"] == "KS") {

                    //補点済みならテキストボックスにしない 
                    if($Row[$col."_FLG"] != "1" && in_array($col, $txt_term)) {
                        //出欠情報がある学期のみ平均用テキストボックスにする
                        $txt_flg["SEM1"] = (preg_match("/1/",$col) ? true : false);
                        $txt_flg["SEM2"] = (preg_match("/2/",$col) ? true : false); 
                    
                        $checked = ($Row[$col."_FLG"] == "2") ? "checked" : "";
                    
                        //チェックボックスを作成
                        $objForm->ae( array("type"      => "checkbox",
                                            "name"      => "chk_box",
                                            "value"     => ($Row["SUBCLASSCD"].",".$Row["SCHREGNO"]),
                                            "extrahtml" => $checked,
                                            "multiple"  => true ));

                        $Row["CHK_BOX"] = $objForm->ge("chk_box");
                                
                        //テキストボックスを作成
                        $objForm->ae( array("type"      => "text",
                                            "name"      => "tbox".$tx_count,
                                            "size"      => "3",
                                            "maxlength" => "3",
                                            "value"     => $Row[$col],
                                            "extrahtml" => "onblur=\"chk_Num(this)\""));
                        $Row[$col] = $objForm->ge("tbox".$tx_count);
                    
                        //データ名判別用
                        $model->txt_names[$tx_count] = $col."-".$Row["SUBCLASSCD"].",".$Row["SCHREGNO"];
                        $tx_count++;
                    }

                    //背景色を決定
                    $Row[$col."_BG"] = $bgcolor[$Row[$col."_DI"]];

                //平均用テキストボックスを作成
                } elseif ($col == "SEM1_REC" && $txt_flg["SEM1"] && $Row[$col."_FLG"] != "1") {
                
                    //中間テストがあるか？
                    $testCnt = $db->getOne(knjd510kQuery::getSchTest($model->cntl_dt_year, "1", "01", "01", $Row["SCHREGNO"], $Row["SUBCLASSCD"]));
                    $testFlg = (0 < $testCnt) ? "testOn" : "testOff";

                    $objForm->ae( array("type"       => "text",
                                        "name"       => "tbox".$tx_count,
                                        "size"       => "3",
                                        "maxlength"  => "3",
                                        "value"      => $Row[$col],
                                        "extrahtml"  => "onblur=\"chk_Num(this)\" id=\"a".$tx_count."e\" class=\"".$testFlg."\""));
                    $Row[$col] = $objForm->ge("tbox".$tx_count);

                    //データ名判別用
                    $model->txt_names[$tx_count] = $col."-".$Row["SUBCLASSCD"].",".$Row["SCHREGNO"];
                    $tx_count++;
    
                } elseif ($col == "SEM2_REC" && $txt_flg["SEM2"] && $Row[$col."_FLG"] != "1") {
                
                    //中間テストがあるか？
                    $testCnt = $db->getOne(knjd510kQuery::getSchTest($model->cntl_dt_year, "2", "01", "01", $Row["SCHREGNO"], $Row["SUBCLASSCD"]));
                    $testFlg = (0 < $testCnt) ? "testOn" : "testOff";

                    $objForm->ae( array("type"       => "text",
                                        "name"       => "tbox".$tx_count,
                                        "size"       => "3",
                                        "maxlength"  => "3",
                                        "value"      => $Row[$col],
                                        "extrahtml"  => "onblur=\"chk_Num(this)\" id=\"a".$tx_count."e\" class=\"".$testFlg."\""));
                    $Row[$col] = $objForm->ge("tbox".$tx_count);

                    //データ名判別用
                    $model->txt_names[$tx_count] = $col."-".$Row["SUBCLASSCD"].",".$Row["SCHREGNO"];
                    $tx_count++;

                } else {
                    $Row[$col] = $Row[$col];
                }
                $data_count++;
            }

            //在籍期間(入学月日、卒業月日)の取得 2006/02/07
            $grd_cnt = $db->getOne(knjd510kQuery::getOnTheRegisterPeriod($model->cntl_dt_year,CTRL_DATE,$Row["SCHREGNO"]));
            //異動者情報の取得 2006/02/07
            $tans_cnt = $db->getOne(knjd510kQuery::getTransferData($model->cntl_dt_year,CTRL_DATE,$Row["SCHREGNO"]));
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
            }


            $arg["data"][] = $Row;
        }

        //DbRecodeSetを開放
        //$result->free();

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

        //仮計算
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_cal",
                            "value"     => "仮計算",
                            "extrahtml" => " onClick=\"return setData();\"" ));

        $arg["BUTTON"]["BTN_CAL"] = $objForm->ge("btn_cal");

        //表示切替
        $chg_val = ($model->show_all == "on")?  "しない" : "" ;

        //補充処理済表示ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_showh",
                            "value"     => "補充処理済みを表示".$chg_val,
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

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "shw_flg",
                            "value"     => $model->show_all ));

        //(補点データ数カウント)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "dataCount",
                            "value"     => $data_count ));

        //--------------------end

        $result->free();
        Query::dbCheckIn($db);

        //対象データの重複又は権限が無ければ閉じる。
        if($error_mode["rp"]){
//            $arg["Closing"] = " closing_window('rp'); ";
        }else if($error_mode["cm"]){
            $arg["Closing"] = " closing_window('cm'); ";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd510kForm1.html", $arg);
    }
}

?>
