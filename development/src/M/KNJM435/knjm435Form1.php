<?php

require_once('for_php7.php');

/********************************************************************/
/* 東京都通信制専用成績入力画面                     伊集 2005/09/00 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：変更内容                                 name yyyy/mm/dd */
/********************************************************************/

class knjm435Form1
{
    function main(&$model)
    {
        $objForm = new form;

        $db         = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"]     = CTRL_YEAR;
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);

        //出力順ラジオボタンを作成
        $opt = array(1, 2);
        $model->order = ($model->order == "") ? "2" : $model->order;
        if ($model->order == 1) $disable = "disabled";
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"ORDER{$val}\" onClick=\"btn_submit('change_order')\"");
        }
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->order, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //編集可能学期の判別
        $opt_adm  = array();
        //データセット
        $result = $db->query(knjm435Query::selectContolCodeQuery($model));

        while ($RowA = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_adm[] = $RowA["CONTROL_CODE"];
        }
        $result->free();

        //科目(講座）リスト
        $opt_sub  = array();
        $opt_sub[0] = array('label' => "",
                            'value' => "");

        //科目(講座）リスト・データセット
        $result = $db->query(knjm435Query::ReadQuery($model));

        $subcnt = 1;
        while ($RowR = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sub[$subcnt] = array('label' => $RowR["CHAIRNAME"],
                                      'value' => $RowR["CHAIRCD"].$RowR["SUBCLASSCD"]);
            $subcnt++;
        }
        $result->free();

        //出力順が変わったら、科目（講座）リストはクリアされる
        if ($model->cmd == "change_order") $model->sub = $opt_sub[0]["value"];
        $extra = "onChange=\"return btn_submit('change');\"";
        $arg["TOP"]["SELSUB"] = knjCreateCombo($objForm, "SELSUB", $model->sub, $opt_sub, $extra, 1);

        //講座受講クラスリスト
        $opt_cla  = array();
        $opt_cla[0] = array('label' => "",
                            'value' => "");

        //講座受講クラスリスト・データセット（クラス番号順の場合のみ）
        if ($model->order == 2) {
            $result = $db->query(knjm435Query::ChairClassQuery($model));
            
            $subcla = 1;
            while ($RowC = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_cla[$subcla] = array('label' => $RowC["CLASSNAME"],
                                          'value' => $RowC["CGRADE"].$RowC["CCLASS"]);
                $subcla++;
            }
            $result->free();
        }

        //科目（講座）が変わったら、講座受講クラスリストはクリアされる
        if ($model->cmd == "change") $model->selcla = $opt_cla[0]["value"];
        $extra = "onChange=\"return btn_submit('change_class');\" ".$disable;
        $arg["TOP"]["SELCLA"] = knjCreateCombo($objForm, "SELCLA", $model->selcla, $opt_cla, $extra, 1);

        //評定MAX値
        $assessMax = $db->getOne(knjm435Query::getAssessMst("max"));
        knjCreateHidden($objForm, "ASSESSMAX", $assessMax);
        //評定MIN値
        $assessMin = $db->getOne(knjm435Query::getAssessMst("min"));
        knjCreateHidden($objForm, "ASSESSMIN", $assessMin);

        //DATA***************************************************************************************

        //データ配列
        $sch_array = array();
        $class_date = array();
        $sem1_score = array();
        $sem1_val = array();
        $sem2_score = array();
        $sem2_val = array();
        $grad_val = array();
        $comp_credit = array();
        $get_credit = array();
        $credits = array();
        $authorize_flg = array();

        //成績データのリスト
        $result  = $db->query(knjm435Query::GetRecordDatdata($model));

        //件数カウント用初期化
        $ca = 0;        //データ全件数
        while ($row_array = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ca++;
            //学籍番号
            $sch_array[$ca] = $row_array["SCHREGNO"];
            //学年、組、番号
            $class_date[$ca] = $row_array["GRADE"] . "-". $row_array["HR_CLASS"] . "-" . $row_array["ATTENDNO"];
            //生徒氏名
            $name_date[$ca] = $row_array["NAME_SHOW"];
            //前期素点
            if ($row_array["SEM1_TERM_SCORE_DI"] == "*") {
                $sem1_score[$ca] = $row_array["SEM1_TERM_SCORE_DI"];
            } else {
                $sem1_score[$ca] = $row_array["SEM1_TERM_SCORE"];
            }
            //前期評価
            $sem1_val[$ca] = $row_array["SEM1_VALUE"];
            //後期素点
            if ($row_array["SEM2_TERM_SCORE_DI"] == "*") {
                $sem2_score[$ca] = $row_array["SEM2_TERM_SCORE_DI"];
            } else {
                $sem2_score[$ca] = $row_array["SEM2_TERM_SCORE"];
            }
            //後期評価
            $sem2_val[$ca] = $row_array["SEM2_VALUE"];
            //評定
            $grad_val[$ca] = $row_array["GRAD_VALUE"];

            //RECORD_DATの履修単位数
            $comp_credit[$ca] = $row_array["COMP_CREDIT"];
            //RECORD_DATの修得単位数
            $get_credit[$ca] = $row_array["GET_CREDIT"];

            //単位マスタから取得したその科目の単位数
            $credits[$ca] = $row_array["CREDITS"];
            //単位マスタから取得したその科目の半期認定フラグ
            $authorize_flg[$ca] = $row_array["AUTHORIZE_FLG"];


        }

        //成績レコードが存在する場合
        if ($ca != 0) {

            //ページの１行目のカレント行番号（配列指標）
            if ($model->line == "") {
                $currentline = 1;
            } else {
                if ($model->cmd == "change" or $model->cmd == "change_class") {
                    $currentline = 1;
                } else {
                    $currentline = $model->line;
                }
            }

            //50件表示ループ
            $counts = 1;                            //カレントページ内での行数
            $pageline = 0;                          //このページの最後の行の全件数の中での行数
            $lineall = $currentline + 50;

            for ($pageline=$currentline; $pageline<$lineall; $pageline++,$counts++) {

                //カレント行が全件数を超えたらループ終り
                if ($pageline > $ca) {
                    break;
                }

                //学籍番号
                $row["SCHREGNO"] = $sch_array[$pageline];
                $row["SCH_HIDDEN"] = knjCreateHidden($objForm, "SCHREGNO-".$counts, $sch_array[$pageline]);

                //クラス番号（学年－クラス－出席番号）
                $row["GRA_HR_ATTEND"] =  $class_date[$pageline];

                //生徒氏名
                $row["NAME_SHOW"] =  $name_date[$pageline];

                //前期素点
                if (in_array("0111",$opt_adm) == true) {
                    //前期素点テキストボックス
                    $extra = "STYLE=\"text-align: right\" onblur=\"check(this, true)\" onPaste=\"return showPaste(this);\"";
                    $row["SEM1_TERM_SCORE"] = knjCreateTextBox($objForm, $sem1_score[$pageline], "SEM1_TERM_SCORE-".$counts, 3, 5, $extra);
                } else {
                    //前期素点表示
                    $row["SEM1_TERM_SCORE"] = $sem1_score[$pageline];
                }

                //前期評価
                if (in_array("0112",$opt_adm) == true) {
                    //前期評価テキストボックス
                    $extra = "STYLE=\"text-align: right\" onblur=\"check(this, false)\" onPaste=\"return showPaste(this);\"";
                    $row["SEM1_VALUE"] = knjCreateTextBox($objForm, $sem1_val[$pageline], "SEM1_VALUE-".$counts, 3, 5, $extra);
                } else {
                    //前期評価表示
                    $row["SEM1_VALUE"] = $sem1_val[$pageline];
                    knjCreateHidden($objForm, "SEM1_VALUE-".$counts, $sem1_val[$pageline]);
                }

                //単位素点
                if (in_array("0211",$opt_adm) == true) {
                    //単位素点テキストボックス
                    $extra = "STYLE=\"text-align: right\" onblur=\"check(this, true)\" onPaste=\"return showPaste(this);\"";
                    $row["SEM2_TERM_SCORE"] = knjCreateTextBox($objForm, $sem2_score[$pageline], "SEM2_TERM_SCORE-".$counts, 3, 5, $extra);
                } else {
                    //単位素点表示
                    $row["SEM2_TERM_SCORE"] = $sem2_score[$pageline];
                }
//textbox
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["名前"] = knjCreateTextBox($objForm, "VALUE", "名前", 2, 2, $extra);

                //単位評価
                if (in_array("0212",$opt_adm) == true) {
                    //単位評価テキストボックス
                    $extra = "STYLE=\"text-align: right\" onblur=\"check(this, false)\" onPaste=\"return showPaste(this);\"";
                    $row["SEM2_VALUE"] = knjCreateTextBox($objForm, $sem2_val[$pageline], "SEM2_VALUE-".$counts, 3, 5, $extra);
                } else {
                    //単位評価表示
                    $row["SEM2_VALUE"] = $sem2_val[$pageline];
                    knjCreateHidden($objForm, "SEM2_VALUE-".$counts, $sem2_val[$pageline]);
                }

                //評定
                if (in_array("0882",$opt_adm) == true) {
                    //評定テキストボックス
                    $extra = "STYLE=\"text-align: right\" onblur=\"check(this, false)\" onPaste=\"return showPaste(this);\"";
                    $row["GRAD_VALUE"] = knjCreateTextBox($objForm, $grad_val[$pageline], "GRAD_VALUE-".$counts, 3, 5, $extra);
                } else {
                    //評定表示
                    $row["GRAD_VALUE"] = $grad_val[$pageline];
                    knjCreateHidden($objForm, "GRAD_VALUE-".$counts, $grad_val[$pageline]);
                }

                //単位マスタから取得した単位（表示用）
                $row["CREDITS"] =  $credits[$pageline];
                //単位マスタから取得した単位（hidden）
                $row["CREDITS_HIDDEN"] = knjCreateHidden($objForm, "CREDITS-".$counts, $credits[$pageline]);
                //単位マスタから取得した半期認定フラグ（hidden）
                knjCreateHidden($objForm, "AUTHORIZE_FLG-".$counts, $authorize_flg[$pageline]);

                //RECORD_DATの履修単位（表示）
                $row["COMP_CREDIT"] =  $comp_credit[$pageline];
                //RECORD_DATの修得単位（表示）
                $row["GET_CREDIT"] =  $get_credit[$pageline];

                $arg["data"][] = $row;
            }
        
        } else {    //成績データが存在しない場合（最初に開いた時も）
            $currentline = 0;
        }


        $result->free();

        //件数表示
        if ($ca == 0) {
            $arg["page_count"] = "0-0 / 0";
            $disabled = "disabled ";
        } else {
            $arg["page_count"] = $currentline . "-" . --$pageline . " / " . $ca;
            $disabled = "";
        }
        //ボタン作成
        $extra = $disabled ."onClick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra, "reset");

        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //「前ページ」「次ページ」ボタンはレコード件数がゼロの時は表示しない
        if ($ca != 0) {
            if ($currentline != 1) {
                $extra = "onClick=\"btn_submit('pre');\"";
                $arg["btn_pre"] = knjCreateBtn($objForm, "btn_pre", "前ページ", $extra);
            }
            if ($pageline < $ca) {
                $extra = "onClick=\"btn_submit('next');\"";
                $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", "次ページ", $extra);
            }
        }

        //hidden
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "line", $currentline);
        knjCreateHidden($objForm, "linecounts", --$counts);
        knjCreateHidden($objForm, "admin", implode(",",$opt_adm));
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm435index.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm435Form1.html", $arg); 
    }
}
?>
