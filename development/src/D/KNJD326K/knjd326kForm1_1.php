<?php
/********************************************************************/
/* 個人成績一覧票                                   山城 2004/12/02 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：変更内容                                 name yyyy/mm/dd */
/********************************************************************/

class knjd326kForm1_1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd326kindex.php", "", "edit");
        $db     = Query::dbCheckOut();

        //ヘッダ部作成
        $test_data_flg = $this->makeHeadData($arg, $db, $model);

        //表示画面作成
        $this->makeShowdisp($arg, $db, $model, $test_data_flg);

        $model->firstcnt++;
        if ($model->cmd != 'kakudai')$model->first++;
        if ($model->first == 3) $model->first = 1;

        //終了ボタン
        $arg["BUTTON1"]["BTN_END"] = $this->createBtn($objForm, "btn_end", " 戻る ", " onClick=\" endclose();\"");

        //hiddenを作成
        $this->makeHidden($objForm, $model);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd326kForm1.html", $arg);
    }

    //ヘッダ部作成
    function makeHeadData(&$arg, $db, &$model)
    {
        if (!isset($model->schregno)) {
            //ヘッダ部分ラベル作成
            $this->makeTopLabel($arg, "&nbsp;&nbsp;&nbsp;&nbsp;", "&nbsp;", "&nbsp;&nbsp;&nbsp", "&nbsp;&nbsp;&nbsp", "&nbsp;&nbsp;&nbsp;&nbsp;");
        } else {
            //生徒学年クラスを取得
            $result = $db->query(knjd326kQuery::getSchreg_Regd_Dat($model));
            $RowR = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $result->free();
            //modelデータセット
            $this->setModelData($db, $model, $RowR);

            //データ初期値(今年度：学年・クラス・出席番号)
            $this->setShowData($ct_yd, $gr_cl, $hr_cl, $at_no, $test_data_flg,
                               $model->cntl_dt_year, $model->GradeClass, $RowR["HR_CLASS"], $RowR["ATTENDNO"], 0);

            //年度別データフラグと表示画面番号で、表示データの振り分けを行う
            switch($model->nendo_flg) {
                case 1 :
                    if ($model->numkaku == 0) {
                        $this->setShowData($ct_yd, $gr_cl, $hr_cl, $at_no, $test_data_flg,
                                           $model->cntl_last_year, $model->last_grad, $model->last_hrcl, $model->last_atno, 1);
                    }
                    break;
                case 2 :
                    if ($model->numkaku == 0) {
                        $this->setShowData($ct_yd, $gr_cl, $hr_cl, $at_no, $test_data_flg,
                                           $model->cntl_beforelast_year, $model->bfor_grad, $model->bfor_hrcl, $model->bfor_atno, 2);
                    }
                    break;
                case 3 :
                    if ($model->numkaku == 0) {
                        $this->setShowData($ct_yd, $gr_cl, $hr_cl, $at_no, $test_data_flg,
                                           $model->cntl_beforelast_year, $model->bfor_grad, $model->bfor_hrcl, $model->bfor_atno, 2);
                    } else if ($model->numkaku == 1) {
                        $this->setShowData($ct_yd, $gr_cl, $hr_cl, $at_no, $test_data_flg,
                                           $model->cntl_last_year, $model->last_grad, $model->last_hrcl, $model->last_atno, 1);
                    }
                    break;
                default :    //今年度データのみなので、初期値を使用
                    break;
            }
            //ヘッダ部分ラベル作成
            $this->makeTopLabel($arg, $ct_yd, sprintf("%d",$gr_cl), $hr_cl, $at_no, $model->name);
        }

        return $test_data_flg;
    }

    //modelデータ設定
    function setModelData($db, &$model, $RowR)
    {
        if (!$model->name) $model->name = $RowR["NAME"];
        if ($model->flg == 0) {
            $model->flg = 2;
        }
        if ($model->cmd == 'kakudai') {
            $model->numkaku = $model->num;
        }

        $model->GradeClass = $RowR["GRADE"];

        //最初の処理で、過去のデータを退避する。
        if ($model->flg != 1 && $model->flg != 2) {
            //前年度
            $query = knjd326kQuery::getSchreg_Regd_Dat_last($model);
            $this->setModelSub($db, $query, $model, 1);
            //前々年度
            $query = knjd326kQuery::getSchreg_Regd_Dat_befor($model);
            $this->setModelSub($db, $query, $model, 2);
        }

        //表示するデータを変えるために添え字を計算する
        if ($model->flg == 1 && $model->firstcnt > 0) {
            $model->numkaku = $model->numkaku - 1;
        } else if ($model->flg == 2 && $model->firstcnt > 0) {
            $model->numkaku = $model->numkaku + 1;
        }
    }

    //modelデータセット
    function setModelSub($db, $query, &$model, $nendoAdd)
    {
        if ($query) {
            $data = $db->getOne($query);
            if (isset($data)) {
                $model->num += 1;
                $model->gamensu += 1;
                $model->nendo_flg += $nendoAdd;
                $result = $db->query($query);
                $grade_data = $result->fetchRow(DB_FETCHMODE_ASSOC);
                $result->free();
                if ($nendoAdd == 1) {
                    $model->last_grad = $grade_data["GRADE"];
                    $model->last_hrcl = $grade_data["HR_CLASS"];
                    $model->last_atno = $grade_data["ATTENDNO"];
                } else {
                    $model->bfor_grad = $grade_data["GRADE"];
                    $model->bfor_hrcl = $grade_data["HR_CLASS"];
                    $model->bfor_atno = $grade_data["ATTENDNO"];
                }
            }
        }

    }

    //表示データセット
    function setShowData(&$ct_yd, &$gr_cl, &$hr_cl, &$at_no, &$test_data_flg, $year, $grad, $hrcl, $atno, $flg)
    {
        $ct_yd = $year;
        $gr_cl = $grad;
        $hr_cl = $hrcl;
        $at_no = $atno;
        $test_data_flg = $flg;
    }

    //ヘッダ部ラベル作成
    function makeTopLabel(&$arg, $year, $grade, $class, $attend, $name)
    {
        //年度
        $arg["TOP"]["YEAR"] = $year;
        //学年
        $arg["TOP"]["GRADE_SHOW"]  = $grade;
        //組
        $arg["TOP"]["CLASS_SHOW"]  = $class;
        //出席番号
        $arg["TOP"]["ATTEND_SHOW"] = $attend;
        //生徒氏名
        $arg["TOP"]["NAME_SHOW"]   = $name;
    }

    //表示画面作成
    function makeShowdisp(&$arg, $db, &$model, $test_data_flg)
    {
        //表示画面作成
        if ($model->schregno == "") {
            $arg["virtual"] = true;
        } else {
            //カラー設定
            $clr_blue        = "color=\"blue\"";
            $clr_green       = "color=\"#00CC00\"";
            $clr_black       = "color=\"black\"";
            $clr_red         = "color=\"red\"";
            $clr_celect      = "color=\"red\"";

            $disp_count      = 0;   //科目設定添え字

            //表示する科目名の設定
            $disp_count = $this->setTitleData($arg, $disp_count);

            $arg["schregno"] = $model->schregno;

            //各テストデータ設定
            $flg_m1 = $flg_e1 = $flg_s1 = array();    /* 合計・平均用フラグ1学期 */
            $flg_m2 = $flg_e2 = $flg_s2 = array();    /* 合計・平均用フラグ2学期 */
            $flg_e3 = $flg_s3 = $flg_t3 = array();    /* 合計・平均用フラグ3学期 */
            $mid_t1 = $ter_t1 = $sem_t1 = array();    /* 合計・平均用BUFER 1学期 */
            $mid_t2 = $ter_t2 = $sem_t2 = array();    /* 合計・平均用BUFER 2学期 */
            $ter_t3 = $sem_t3 = $ttl_t3 = array();    /* 合計・平均用BUFER 3学期 */
            $ks     = "KS";        /* 欠席     */
            $kk     = "KK";        /* 公欠     */
            $i      = 0;
            $m1fst_flg = true;    /* 席次取得フラグ */

            //初期値設定(全項目にブランク)
            $this->initialVal($arg, $disp_count, $clr_blue);

            //表示年度用テストデータ作成前処理
            if ($test_data_flg == 0) {
                $result = $db->query(knjd326kQuery::get_testscore_data($model));
                $present_year = $model->cntl_dt_year;
            } else if ($test_data_flg == 1) {
                $result = $db->query(knjd326kQuery::get_testscore_data_last($model));
                $present_year = $model->cntl_last_year;
            } else {
                $result = $db->query(knjd326kQuery::get_testscore_data_beforelast($model));
                $present_year = $model->cntl_beforelast_year;
            }

            //異動情報設定
            $this->setTransferInfo($arg, $db, $model, $present_year, $test_data_flg);

            //表示年度用のテストデータを作成
            while ($test_score = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //席次を抽出
                if ($m1fst_flg) {
                    $rankdata = array();    //席次データ
                    //3科目
                    $rankdata["SUB_3"] = $this->getRankData($db, $model, $present_year, 1);
                    //5科目
                    $rankdata["SUB_5"] = $this->getRankData($db, $model, $present_year, 2);
                    //9科目
                    $rankdata["SUB_9"] = $this->getRankData($db, $model, $present_year, 3);
                    //席次は、1度取得すればよい
                    $m1fst_flg = false;
                }
                if (substr($test_score["SUBCLASSCD"],0,2) == '01') $i = 0;
                if (substr($test_score["SUBCLASSCD"],0,2) == '02') $i = 1;
                if (substr($test_score["SUBCLASSCD"],0,2) == '03') $i = 2;
                if (substr($test_score["SUBCLASSCD"],0,2) == '04') $i = 3;
                if (substr($test_score["SUBCLASSCD"],0,2) == '05') $i = 4;
                if (substr($test_score["SUBCLASSCD"],0,2) == '06') $i = 5;
                if (substr($test_score["SUBCLASSCD"],0,2) == '07') $i = 6;
                if (substr($test_score["SUBCLASSCD"],0,2) == '08') $i = 7;
                if (substr($test_score["SUBCLASSCD"],0,2) == '09') $i = 8;
                $scor_avg = array();

                //1学期中間セット
                $flg_m1[$i] = $this->setTestInterTerm($arg, $db, $model, $present_year, $test_score, $mid_t1, "MIDDLE_TEST1", "SEM1_INTER", 1, $i, $kk, $ks, $clr_blue, $clr_red, $clr_celect);
                //1学期期末セット
                $flg_e1[$i] = $this->setTestInterTerm($arg, $db, $model, $present_year, $test_score, $ter_t1, "END_TERM_TEST1", "SEM1_TERM", 1, $i, $kk, $ks, $clr_blue, $clr_red, $clr_celect);
                //2学期中間セット
                $flg_m2[$i] = $this->setTestInterTerm($arg, $db, $model, $present_year, $test_score, $mid_t2, "MIDDLE_TEST2", "SEM2_INTER", 2, $i, $kk, $ks, $clr_blue, $clr_red, $clr_celect);
                //2学期期末セット
                $flg_e2[$i] = $this->setTestInterTerm($arg, $db, $model, $present_year, $test_score, $ter_t2, "END_TERM_TEST2", "SEM2_TERM", 2, $i, $kk, $ks, $clr_blue, $clr_red, $clr_celect);
                //3学期期末セット
                $flg_e3[$i] = $this->setTestInterTerm($arg, $db, $model, $present_year, $test_score, $ter_t3, "END_TERM_TEST3", "SEM3_TERM", 3, $i, $kk, $ks, $clr_blue, $clr_red, $clr_celect);

                //1学期成績設定
                $flg_s1[$i] = $this->setTestSemes($arg, $test_score, $sem_t1, "SEME_TEST1", "SEM1", 1, $i, $kk, $ks, $clr_blue, $clr_green);
                //2学期成績設定
                $flg_s2[$i] = $this->setTestSemes($arg, $test_score, $sem_t2, "SEME_TEST2", "SEM2", 2, $i, $kk, $ks, $clr_blue, $clr_green);
                //3学期成績設定
                $flg_s3[$i] = $this->setTestSemes($arg, $test_score, $sem_t3, "SEME_TEST3", "SEM3", 3, $i, $kk, $ks, $clr_blue, $clr_green);

                if (is_numeric($test_score["GRADE_RECORD"])) {
                    $ttl_t3[$i] = $test_score["GRADE_RECORD"];
                    $this->setTableData($arg, "TOTAL_TEST3", $i, $test_score["GRADE_RECORD"], $clr_blue);
                    $flg_t3[$i] = 1;
                } else {
                    //NO008-->
                    if ($test_score["SEM1_REC_MARK"] + $test_score["SEM2_REC_MARK"] + $test_score["SEM3_REC_MARK"] <= 1) {
                        $this->setTableData($arg, "TOTAL_TEST3", $i, "-", $clr_red);
                    }
                    //NO008<--
                    $flg_t3[$i] = 0;
                }
                $i++;
            }
            $result->free();
            $kei1m = $kei1e = $kei1s = $kei2m = $kei2e = $kei2s = $kei3e = $kei3s = $kei3t = 0;
            $mid_av_1 = $ter_av_1 = $sem_av_1 = 0;    /* 1学期 平均用カウンタ */
            $mid_av_2 = $ter_av_2 = $sem_av_2 = 0;    /* 2学期 平均用カウンタ */
            $ter_av_3 = $sem_av_3 = $ttl_av_3 = 0;    /* 3学期 平均用カウンタ */
            /* roop用 */
            $goukei024 = array(0, 2, 4);
            /* roop用 */
            $goukei13 = array(1, 3);
            for ($t = 9; $t < 12; $t++) {
                switch ($t){
                    case 9:    /* 3科 */
                        $this->setTotalAvgMain($arg, $goukei024, $clr_black, "SUB_3", $t, 15, $rankdata,
                                               $mid_t1, $kei1m, $mid_av_1,$ter_t1, $kei1e, $ter_av_1,
                                               $sem_t1, $kei1s, $sem_av_1,$mid_t2, $kei2m, $mid_av_2,
                                               $ter_t2, $kei2e, $ter_av_2,$sem_t2, $kei2s, $sem_av_2,
                                               $ter_t3, $kei3e, $ter_av_3,$sem_t3, $kei3s, $sem_av_3,
                                               $ttl_t3, $kei3t, $ttl_av_3);
                        break;
                    case 10:    /* 5科 */
                        $this->setTotalAvgMain($arg, $goukei13, $clr_black, "SUB_5", $t, 16, $rankdata,
                                               $mid_t1, $kei1m, $mid_av_1,$ter_t1, $kei1e, $ter_av_1,
                                               $sem_t1, $kei1s, $sem_av_1,$mid_t2, $kei2m, $mid_av_2,
                                               $ter_t2, $kei2e, $ter_av_2,$sem_t2, $kei2s, $sem_av_2,
                                               $ter_t3, $kei3e, $ter_av_3,$sem_t3, $kei3s, $sem_av_3,
                                               $ttl_t3, $kei3t, $ttl_av_3);
                        break;
                    case 11:    /* 9科 */
                        //1学期期末 合計・平均
                        $this->setTotalAvg9($arg, $flg_e1, $ter_t1, $clr_black,
                                            $rankdata["SUB_9"]["SEM1_TERM_REC_RANK"], "END_TERM_TEST1", $t, 17);
                        //1学期成績 合計・平均
                        $this->setTotalAvg9($arg, $flg_s1, $sem_t1, $clr_black,
                                            $rankdata["SUB_9"]["SEM1_REC_RANK"], "SEME_TEST1", $t, 17);
                        //2学期期末 合計・平均
                        $this->setTotalAvg9($arg, $flg_e2, $ter_t2, $clr_black,
                                            $rankdata["SUB_9"]["SEM2_TERM_REC_RANK"], "END_TERM_TEST2", $t, 17);
                        //2学期成績 合計・平均
                        $this->setTotalAvg9($arg, $flg_s2, $sem_t2, $clr_black,
                                            $rankdata["SUB_9"]["SEM2_REC_RANK"], "SEME_TEST2", $t, 17);
                        //3学期期末 合計・平均
                        $this->setTotalAvg9($arg, $flg_e3, $ter_t3, $clr_black,
                                            $rankdata["SUB_9"]["SEM3_TERM_REC_RANK"], "END_TERM_TEST3", $t, 17);
                        //3学期成績 合計・平均
                        $this->setTotalAvg9($arg, $flg_s3, $sem_t3, $clr_black,
                                            $rankdata["SUB_9"]["SEM3_REC_RANK"], "SEME_TEST3", $t, 17);
                        //3学期学年 合計・平均
                        $this->setTotalAvg9($arg, $flg_t3, $ttl_t3, $clr_black,
                                            $rankdata["SUB_9"]["GRADE_RECORD_RANK"], "TOTAL_TEST3", $t, 17);
                        break;
                    default :
                        
                        break;
                }
            }
            //表示年度実力テストデータを作成
            $this->setShamexam($arg, $db, $model, $present_year, $clr_blue, $clr_black);

            //表示時の体裁を整える
            $arg["data"]["width"]  = "width=\"36\"";
            $arg["data"]["width2"] = "width=\"" .(36 * 3). "\"";
            $arg["data"]["width3"] = "width=\"(36 *5)\"";
            $arg["data"]["colspan"] = "colspan=\"" .($disp_count). "\"";
            $arg["data"]["colspan3"] = "colspan=\"3\"";
            $arg["data"]["colspan5"] = "colspan=\"5\"";

            //前ページにデータがある場合にリンク設定
            if ($model->numkaku != 0) {
               //共通関数View::alinkを使うとリンクの色を指定できないので使わずに設定
                  $hash = "knjd326kindex.php?cmd=kaku&FLG=1";
               $arg["hash1"] = "<a href=\"" .$hash. "\" target=\"_self\" >";
                  $arg["end_link1"] = "</a>";
            }
           //次ページにデータがある場合にリンク設定
            if (($hantei=($model->gamensu - $model->numkaku)) != 0) {
               $hash = "knjd326kindex.php?cmd=kaku&FLG=2";
               $arg["hash2"] = "<a href=\"" .$hash. "\" target=\"_self\" >";
               $arg["end_link2"] = "</a>";
            }
        }
    }

    //表示科目作成
    function setTitleData(&$arg, $disp_cnt)
    {
        $elect_color     = "bgcolor=\"#316f9b\"";
        //各教科素点タイトル(9科目)
        $sub_code  = array("国語", "社会", "数学", "理科", "英語", "音楽", "美術", "保体", "技家");
        $disp_cnt  = $this->setTitleSub($arg, $elect_color, $sub_code, $disp_cnt, 1);
        //合計・平均・席次タイトル(科目数)
        $sub_count = array("３科", "５科", "９科");
        $disp_cnt  = $this->setTitleSub($arg, $elect_color, $sub_count, $disp_cnt, 3);
        //偏差値タイトル(5科目)
        $main_code = array("国語", "社会", "数学", "理科", "英語");
        $disp_cnt  = $this->setTitleSub($arg, $elect_color, $main_code, $disp_cnt, 1);

        return $disp_cnt;
    }

    //タイトルセット
    function setTitleSub(&$arg, $color, $name, $disp_cnt, $datacnt)
    {
        for ($cnt = 0; $cnt < $datacnt; $cnt++) {
            for ($i = 0; $i < count($name); $i++) {
                $this->setTableData($arg, "disp", $disp_cnt, $name[$i], $color);
                $disp_cnt++;
            }
        }
        return $disp_cnt;
    }

    //初期値設定(全項目にブランク)
    function initialVal(&$arg, $disp_cnt, $color)
    {
        //テスト種別
        $fieldName = array("MIDDLE_TEST1", "MIDDLE_TEST2",
                           "END_TERM_TEST1", "END_TERM_TEST2", "END_TERM_TEST3",
                           "SEME_TEST1", "SEME_TEST2", "SEME_TEST3", "TOTAL_TEST3",
                           "ONCE_TEST", "TWO_TIMES_TEST", "THREE_TIMES_TEST", "FOUR_TIMES_TEST", "FIVE_TIMES_TEST",
                           "SIX_TIMES_TEST", "SEVEN_TIMES_TEST", "EIGHT_TIMES_TEST", "NINE_TIMES_TEST");

        for ($i = 0; $i < $disp_cnt; $i++) {
            for ($ii = 0; $ii < count($fieldName); $ii++) {
                $this->setTableData($arg, $fieldName[$ii], $i, "", $color);
            }
        }
    }

    //表データ設定
    function setTableData(&$arg, $field, $fieldCnt, $val, $color)
    {
        $arg[$field][$fieldCnt] = array("NAME"        => $val,
                                        "ELECT_COLOR" => $color);
    }

    //異動情報設定
    function setTransferInfo(&$arg, $db, &$model, $present_year, $test_data_flg)
    {
        //休学・留学
        $Rowtrans  = $db->query(knjd326kQuery::Get_transfer($model,$present_year));
        $transdata = $Rowtrans->fetchRow(DB_FETCHMODE_ASSOC);
        if ($test_data_flg == 0) {
            //転学
            $RowBase   = $db->query(knjd326kQuery::getSchreg_Base_Mst($model));
            $basedata = $RowBase->fetchRow(DB_FETCHMODE_ASSOC);
            if ($basedata["GRD_DATE"] >= $transdata["TRANSFER_SDATE"]) {
                if ($basedata["GRD_DIV"] == 3) {
                    $basdate = split("-",$basedata["GRD_DATE"]);
                    $arg["grddata"][0] = array("YEAR"    => $basdate[0],
                                               "MONTH"   => sprintf("%d",$basdate[1]),
                                               "DATE"    => sprintf("%d",$basdate[2]),
                                               "RESON"   => "転学");
                }
            } else {
                //休学・留学データ設定
                $this->setTransData($arg, $transdata);
            }
        } else {
            //休学・留学データ設定
            $this->setTransData($arg, $transdata);
        }
    }

    //休学・留学データ設定
    function setTransData(&$arg, $transdata)
    {
        $basdate = split("-",$transdata["TRANSFER_SDATE"]);
        if ($transdata["TRANSFERCD"] == 1) {
            $reson = "留学";
        } else {
            $reson = "休学";
        }
        if (is_array($transdata)) {
            $arg["grddata"][0] = array("YEAR"    => $basdate[0],
                                       "MONTH"   => sprintf("%d",$basdate[1]),
                                       "DATE"    => sprintf("%d",$basdate[2]),
                                       "RESON"   => $reson);
        }
    }

    //席次取得
    function getRankData($db, $model, $present_year, $div)
    {
        $scor_pd = array();
        $rankdata = $db->getRow(knjd326kQuery::get_Rank($model, $present_year, $div), DB_FETCHMODE_ASSOC);
        foreach ($rankdata as $key => $val) {
            $scor_pd[$key] = $val;
        }
        return $scor_pd;
    }

    //中間期末テスト設定
    function setTestInterTerm(&$arg, $db, $model, $present_year, $test_score, &$score, $field, $semname, $seme, $i, $kk, $ks, $blue, $red, $celect)
    {
        switch ($test_score[$semname."_REC_DI"]) {
            case $kk:
                if (!$test_score[$semname."_REC"]) {
                    $this->setTableData($arg, $field, $i, "(公)", $blue);
                    break;
                }
            case $ks:
                if (!$test_score[$semname."_REC"]) {
                    $this->setTableData($arg, $field, $i, "(欠)", $red);
                    break;
                }
            default :
                if (is_numeric($test_score[$semname."_REC"])) {
                    $score[$i] = $test_score[$semname."_REC"];
                    //グループ平均取得(グループ平均の60％未満は、赤で表示)
                    $avgdata  = 0;
                    $result = $db->query(knjd326kQuery::get_groupavg_data($model,$test_score["SUBCLASSCD"],$present_year,$seme,$semname."_REC"));
                    while ($scor_avgbase = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        if ($scor_avgbase["VALUE"] > 0) {
                            $avgdata = $scor_avgbase["VALUE"];
                        }
                    }
                    $result->free();
                    if ($avgdata < $test_score[$semname."_REC"] || 
                       substr($test_score["SUBCLASSCD"],0,2) == '06' ||
                       substr($test_score["SUBCLASSCD"],0,2) == '07' ||
                       substr($test_score["SUBCLASSCD"],0,2) == '08' ||
                       substr($test_score["SUBCLASSCD"],0,2) == '09' )
                    {
                        $celect = "color=\"blue\"";
                    } else {
                        $celect = "color=\"red\"";
                    }

                    $this->setTableData($arg, $field, $i, $test_score[$semname."_REC"], $celect);

                    $retrnflg = 1;
                } else {
                    $retrnflg = 0;
                }
                break;
        }
        return $retrnflg;
    }

    //学期成績設定
    function setTestSemes(&$arg, $test_score, &$score, $field, $semname, $seme, $i, $kk, $ks, $blue, $green)
    {
        if (is_numeric($test_score[$semname."_REC"])) {
            $score[$i] = $test_score[$semname."_REC"];

            if ($test_score[$semname."_REC_FLG"] == 1) {
                $this->setTableData($arg, $field, $i, $test_score[$semname."_REC"], $green);
            } else {
                $this->setTableData($arg, $field, $i, $test_score[$semname."_REC"], $blue);
            }
            $retrnflg = 1;
        } else {
            $retrnflg = 0;
        }

        return $retrnflg;
    }

    //合計・平均設定メイン
    function setTotalAvgMain(&$arg, $goukei, $clr_black, $rankKey, $t, $rankLine, $rankdata,
                             $mid_t1, $kei1m, $mid_av_1,$ter_t1, $kei1e, $ter_av_1,
                             $sem_t1, $kei1s, $sem_av_1,$mid_t2, $kei2m, $mid_av_2,
                             $ter_t2, $kei2e, $ter_av_2,$sem_t2, $kei2s, $sem_av_2,
                             $ter_t3, $kei3e, $ter_av_3,$sem_t3, $kei3s, $sem_av_3,
                             $ttl_t3, $kei3t, $ttl_av_3)
    {
        //1学期中間 合計・平均
        $this->setTotalAvg($arg, $goukei, $mid_t1, $kei1m, $mid_av_1, $clr_black,
                           $rankdata[$rankKey]["SEM1_INTER_REC_RANK"], "MIDDLE_TEST1", $t, $rankLine);
        //1学期期末 合計・平均
        $this->setTotalAvg($arg, $goukei, $ter_t1, $kei1e, $ter_av_1, $clr_black,
                           $rankdata[$rankKey]["SEM1_TERM_REC_RANK"], "END_TERM_TEST1", $t, $rankLine);
        //1学期成績 合計・平均
        $this->setTotalAvg($arg, $goukei, $sem_t1, $kei1s, $sem_av_1, $clr_black,
                           $rankdata[$rankKey]["SEM1_REC_RANK"], "SEME_TEST1", $t, $rankLine);

        //2学期中間 合計・平均
        $this->setTotalAvg($arg, $goukei, $mid_t2, $kei2m, $mid_av_2, $clr_black,
                           $rankdata[$rankKey]["SEM2_INTER_REC_RANK"], "MIDDLE_TEST2", $t, $rankLine);
        //2学期期末 合計・平均
        $this->setTotalAvg($arg, $goukei, $ter_t2, $kei2e, $ter_av_2, $clr_black,
                           $rankdata[$rankKey]["SEM2_TERM_REC_RANK"], "END_TERM_TEST2", $t, $rankLine);
        //2学期成績 合計・平均
        $this->setTotalAvg($arg, $goukei, $sem_t2, $kei2s, $sem_av_2, $clr_black,
                           $rankdata[$rankKey]["SEM2_REC_RANK"], "SEME_TEST2", $t, $rankLine);

        //3学期期末 合計・平均
        $this->setTotalAvg($arg, $goukei, $ter_t3, $kei3e, $ter_av_3, $clr_black,
                           $rankdata[$rankKey]["SEM3_TERM_REC_RANK"], "END_TERM_TEST3", $t, $rankLine);
        //3学期成績 合計・平均
        $this->setTotalAvg($arg, $goukei, $sem_t3, $kei3s, $sem_av_3, $clr_black,
                           $rankdata[$rankKey]["SEM3_REC_RANK"], "SEME_TEST3", $t, $rankLine);
        //3学期学年 合計・平均
        $this->setTotalAvg($arg, $goukei, $ttl_t3, $kei3t, $ttl_av_3, $clr_black,
                           $rankdata[$rankKey]["GRADE_RECORD_RANK"], "TOTAL_TEST3", $t, $rankLine);
    }

    //合計・平均設定
    function setTotalAvg(&$arg, $subcdArray, $score, $totalval, $avlval, $color, $rankdata, $field, $fno, $line)
    {
        for ($i = 0; $i < count($subcdArray); $i++) {
            if (is_numeric($score[$subcdArray[$i]])) {
                $totalval += $score[$subcdArray[$i]];
                $avlval += 1;
            }
        }
        if ($avlval > 0) {
            $this->setTableData($arg, $field, $fno, $totalval, $color);
            $this->setTableData($arg, $field, $fno+3, round($totalval/$avlval,1), $color);
        }
        //席次
        $this->setTableData($arg, $field, $line, $rankdata, $color);
    }

    //合計・平均設定 9科目用
    function setTotalAvg9(&$arg, $score, $sumarray, $color, $rankdata, $field, $fno, $line)
    {
        $sum = array_sum($score);
        if ($sum > 0) {
            $this->setTableData($arg, $field, $fno, array_sum($sumarray), $color);
            $this->setTableData($arg, $field, $fno+3, round(array_sum($sumarray)/$sum,1), $color);
        }
        //席次
        $this->setTableData($arg, $field, $line, $rankdata, $color);
    }

    //表示年度実力テストデータを作成
    function setShamexam(&$arg, $db, &$model, $present_year, $clr_blue, $clr_black)
    {
        $result = $db->query(knjd326kQuery::get_proficiency_data($model,$present_year));
        while ($test_score = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (substr($test_score["SUBCLASSCD"],1,1) > 0) {
                $cnt = substr($test_score["SUBCLASSCD"],1,1) - 1;
            } else {
                $cnt = 99 ;
            }
            switch ($test_score["SHAMEXAMCD"]){
                case 1:    //第1回
                    $this->setShamexamData($arg, "ONCE_TEST", $cnt, $test_score, $clr_blue, $clr_black);
                    break;
                case 2:    //第2回
                    $this->setShamexamData($arg, "TWO_TIMES_TEST", $cnt, $test_score, $clr_blue, $clr_black);
                    break;
                case 3:    //第3回
                    $this->setShamexamData($arg, "THREE_TIMES_TEST", $cnt, $test_score, $clr_blue, $clr_black);
                    break;
                case 4:    //第4回
                    $this->setShamexamData($arg, "FOUR_TIMES_TEST", $cnt, $test_score, $clr_blue, $clr_black);
                    break;
                case 5:    //第5回
                    $this->setShamexamData($arg, "FIVE_TIMES_TEST", $cnt, $test_score, $clr_blue, $clr_black);
                    break;
                case 6:    //第6回
                    $this->setShamexamData($arg, "SIX_TIMES_TEST", $cnt, $test_score, $clr_blue, $clr_black);
                    break;
                case 7:    //第7回
                    $this->setShamexamData($arg, "SEVEN_TIMES_TEST", $cnt, $test_score, $clr_blue, $clr_black);
                    break;
                case 8:    //第8回
                    $this->setShamexamData($arg, "EIGHT_TIMES_TEST", $cnt, $test_score, $clr_blue, $clr_black);
                    break;
                case 9:    //第9回
                    $this->setShamexamData($arg, "NINE_TIMES_TEST", $cnt, $test_score, $clr_blue, $clr_black);
                    break;
                default :

                    break;
            }
        }
        $result->free();
    }

    //対外模試データセット
    function setShamexamData(&$arg, $field, $cnt, $test_score, $clr_blue, $clr_black)
    {
        if ($cnt != 99) {
            $arg[$field][$cnt] = array("NAME" => $test_score["SCORE"],
                                       "ELECT_COLOR" => $clr_blue);
        }
        //合計点
        if ($test_score["SUBCLASSCD"] == "800101") {
            $arg[$field][9]  = array("NAME"        => $test_score["SCORE"],
                                     "ELECT_COLOR" => $clr_black);
            $arg[$field][15] = array("NAME"        => $test_score["SCHOOL_PRECEDENCE"],
                                     "ELECT_COLOR" => $clr_black);
        } else if ($test_score["SUBCLASSCD"] == "800102") {
            $arg[$field][10] = array("NAME"        => $test_score["SCORE"],
                                     "ELECT_COLOR" => $clr_black);
            $arg[$field][16] = array("NAME"        => $test_score["SCHOOL_PRECEDENCE"],
                                     "ELECT_COLOR" => $clr_black);
        } else if ($test_score["SUBCLASSCD"] == "800201") {
            $arg[$field][12] = array("NAME"        => $test_score["SCORE1"],
                                     "ELECT_COLOR" => $clr_black);
        } else if ($test_score["SUBCLASSCD"] == "800202") {
            $arg[$field][13] = array("NAME"        => $test_score["SCORE1"],
                                     "ELECT_COLOR" => $clr_black);
        }
        if ($cnt < 5) {
            $arg[$field][$cnt+18] = array("NAME" => $test_score["DEVIATION"],
                                          "ELECT_COLOR" => $clr_black);
        }
    }

    //hidden作成
    function makeHidden(&$objForm, $model)
    {
        $objForm->ae($this->createHiddenAe("cmd"));

        $objForm->ae($this->createHiddenAe("num", $model->num));

        $objForm->ae($this->createHiddenAe("numkaku", $model->numkaku));

        $objForm->ae($this->createHiddenAe("gamensu", $model->gamensu));

        $objForm->ae($this->createHiddenAe("nendo_flg", $model->nendo_flg));

        $objForm->ae($this->createHiddenAe("last_grad", $model->last_grad));

        $objForm->ae($this->createHiddenAe("last_hrcl", $model->last_hrcl));

        $objForm->ae($this->createHiddenAe("last_atno", $model->last_atno));

        $objForm->ae($this->createHiddenAe("bfor_grad", $model->bfor_grad));

        $objForm->ae($this->createHiddenAe("bfor_hrcl", $model->bfor_hrcl));

        $objForm->ae($this->createHiddenAe("bfor_atno", $model->bfor_atno));

        $objForm->ae($this->createHiddenAe("SCHREGNO", $model->schregno));

        $objForm->ae($this->createHiddenAe("firstcnt", $model->firstcnt));
    }

    //ボタン作成
    function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
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

}
?>