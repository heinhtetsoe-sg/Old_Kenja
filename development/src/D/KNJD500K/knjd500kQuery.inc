<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd500kQuery.inc 56580 2017-10-22 12:35:29Z maeshiro $
class knjd500kQuery extends Query
{
    //knjd500kQuery start
    
    //処理年度より学年を取得
    function get_grade_data($a_year)
    {
        $query = false;
        if($a_year != "")
        {
            $query = "
                      SELECT DISTINCT GRADE 
                      FROM SCHREG_REGD_HDAT 
                      WHERE YEAR = '".$a_year."'
                     ";
        }
        return $query;
    }

    //処理年度より学期を取得
    function get_semester_data($a_year)
    {
        $query = false;
        if($a_year != "")
        {
            $query = "
                      SELECT SEMESTER, SEMESTERNAME
                      FROM SEMESTER_MST
                      WHERE YEAR = '".$a_year."' 
                        AND SEMESTER <> '9'
                     ";
        }
        //echo $query;
        return $query;
    }

    //データチェック #固定値が入っています！
    function chk_RepeatError($a_year, $a_sem)
    {
        $query = "";
        $query = "
                    SELECT TESTKINDCD,CHAIRCD,OPERATION_DATE
                    FROM TESTSCORE_HDAT
                    WHERE YEAR ='".$a_year."'
                    AND SEMESTER='".$a_sem."'
                    AND TESTKINDCD IN ('01','02')
                    GROUP BY TESTKINDCD,CHAIRCD,OPERATION_DATE
                    HAVING COUNT(*) >1
                    ";
        
        return $query;
    }

    //データ取得
    function get_main_data($model)
    {
        //表示フラグ判定
        $query =" SELECT DISTINCT 

                    T3.YEAR,T3.CHAIRCD, T3.SCHREGNO,
                    T3.SEM1_INTER_REC,T3.SEM1_TERM_REC,T3.SEM1_REC,
                    T3.SEM2_INTER_REC,T3.SEM2_TERM_REC,T3.SEM2_REC,
                    T3.SEM3_TERM_REC,T3.GRADE_RECORD,T3.SUBCLASSCD,
                    T6.GRADE,T6.HR_CLASS,
                    T5.NAME_SHOW,T7.HR_NAME || '-' || T6.ATTENDNO AS HR_SHOW,T8.SUBCLASSNAME,

                    T3.SEM1_INTER_REC_FLG,T3.SEM1_TERM_REC_FLG,T3.SEM1_REC_FLG,
                    T3.SEM2_INTER_REC_FLG,T3.SEM2_TERM_REC_FLG,T3.SEM2_REC_FLG,
                    T3.SEM3_TERM_REC_FLG
                    ,T3.SEM1_INTER_REC_DI,T3.SEM1_TERM_REC_DI,T3.SEM2_INTER_REC_DI,T3.SEM2_TERM_REC_DI,T3.SEM3_TERM_REC_DI,T6.ATTENDNO

                    FROM
                    KIN_RECORD_DAT AS T3 LEFT OUTER JOIN SCHREG_BASE_MST AS T5
                                                      ON  T3.SCHREGNO = T5.SCHREGNO

                                         LEFT OUTER JOIN SCHREG_REGD_DAT AS T6
                                                      ON T3.YEAR     = T6.YEAR
                                                     AND T3.SCHREGNO = T6.SCHREGNO
                                                     AND T6.SEMESTER = '".CTRL_SEMESTER."'

                                         LEFT OUTER JOIN SCHREG_REGD_HDAT AS T7
                                                      ON  T6.YEAR    = T7.YEAR
                                                     AND T6.SEMESTER = T7.SEMESTER
                                                     AND T6.GRADE    = T7.GRADE
                                                     AND T6.HR_CLASS = T7.HR_CLASS

                                         LEFT OUTER JOIN V_SUBCLASS_MST AS T8
                                                      ON  T3.YEAR      = T8.YEAR
                                                     AND T3.SUBCLASSCD = T8.SUBCLASSCD
                    WHERE T6.GRADE = '".$model->gk_cmb."'
                      AND T3.YEAR  = '".$model->cntl_dt_year."' ";

        //１学期-中間
        if ($model->sem_cmb == "1" && $model->div_cmb == "1") {
            $query .= " AND T3.SEM1_INTER_REC_DI IN ('KK','KS') ";
            if($model->show_all == "on") {
                $query .= " AND (T3.SEM1_INTER_REC_FLG = '0' OR T3.SEM1_INTER_REC_FLG = '1' OR T3.SEM1_INTER_REC_FLG IS NULL )  ";
            } else {
                $query .= " AND (T3.SEM1_INTER_REC_FLG = '0' OR T3.SEM1_INTER_REC_FLG IS NULL )  ";
            }
        //１学期-期末
        } elseif($model->sem_cmb == "1" && $model->div_cmb == "2") {
            $query .= " AND T3.SEM1_TERM_REC_DI  IN ('KK','KS') ";
            if($model->show_all == "on") {
                $query .= " AND (T3.SEM1_TERM_REC_FLG = '0' OR T3.SEM1_TERM_REC_FLG = '1' OR T3.SEM1_TERM_REC_FLG IS NULL )  ";
            } else {
                $query .= " AND (T3.SEM1_TERM_REC_FLG = '0' OR T3.SEM1_TERM_REC_FLG IS NULL )  ";
            }
        //１学期-平均
        } elseif($model->sem_cmb == "1" && $model->div_cmb == "3") {
            $query .= " AND (T3.SEM1_INTER_REC_DI  IN ('KK','KS') OR T3.SEM1_TERM_REC_DI  IN ('KK','KS')) ";
            if($model->show_all == "on") {
                $query .= " AND (T3.SEM1_REC_FLG = '0' OR T3.SEM1_REC_FLG = '1' OR T3.SEM1_REC_FLG IS NULL )  ";
            } else {
                $query .= " AND (T3.SEM1_REC_FLG = '0' OR T3.SEM1_REC_FLG IS NULL )  ";
            }
        //２学期-中間
        } elseif($model->sem_cmb == "2" && $model->div_cmb == "1") {
            $query .= " AND T3.SEM2_INTER_REC_DI IN ('KK','KS') ";
            if($model->show_all == "on") {
                $query .= " AND (T3.SEM2_INTER_REC_FLG = '0' OR T3.SEM2_INTER_REC_FLG = '1' OR T3.SEM2_INTER_REC_FLG IS NULL )  ";
            } else {
                $query .= " AND (T3.SEM2_INTER_REC_FLG = '0' OR T3.SEM2_INTER_REC_FLG IS NULL )  ";
            }
        //２学期-期末
        } elseif($model->sem_cmb == "2" && $model->div_cmb == "2") {
            $query .= " AND T3.SEM2_TERM_REC_DI IN ('KK','KS') ";
            if($model->show_all == "on") {
                $query .= " AND (T3.SEM2_TERM_REC_FLG = '0' OR T3.SEM2_TERM_REC_FLG = '1' OR T3.SEM2_TERM_REC_FLG IS NULL )  ";
            } else {
                $query .= " AND (T3.SEM2_TERM_REC_FLG = '0' OR T3.SEM2_TERM_REC_FLG IS NULL ) ";
            }
        //２学期-平均
        } elseif($model->sem_cmb == "2" && $model->div_cmb == "3") {
            $query .= " AND (T3.SEM2_INTER_REC_DI  IN ('KK','KS') OR T3.SEM2_TERM_REC_DI  IN ('KK','KS')) ";
            if($model->show_all == "on") {
                $query .= " AND (T3.SEM2_REC_FLG = '0' OR T3.SEM2_REC_FLG = '1' OR T3.SEM2_REC_FLG IS NULL )  ";
            } else {
                $query .= " AND (T3.SEM2_REC_FLG = '0' OR T3.SEM2_REC_FLG IS NULL ) ";
            }
        //３学期-期末
        } elseif($model->sem_cmb == "3" && $model->div_cmb == "2") {
            $query .= " AND T3.SEM3_TERM_REC_DI IN ('KK','KS') ";
            if($model->show_all == "on") {
                $query .= " AND (T3.SEM3_TERM_REC_FLG = '0' OR T3.SEM3_TERM_REC_FLG = '1' OR T3.SEM3_TERM_REC_FLG IS NULL )  ";
            } else {
                $query .= " AND (T3.SEM3_TERM_REC_FLG = '0' OR T3.SEM3_TERM_REC_FLG IS NULL )  ";
            }
        }


        $query .= " ORDER BY T6.GRADE,T6.HR_CLASS,T6.ATTENDNO,T3.SUBCLASSCD";

        return $query;
    }

    //一,二学期補点
    function sec_avg(&$a_Row, &$a_arT, $a_Year, $a_Sem, $di_cd)
    {
        $rate = "";
        if($di_cd == "KS") $rate = " * 0.7";

        $query = "SELECT ROUND(ROUND(".$a_Row[$a_arT["base"]]." /
                                       DEC(ROUND(AVG(DEC(T1.".$a_arT["base"]." )),0)) *
                                       DEC(ROUND(AVG(DEC(T1.".$a_arT["target"].")),0)),0) ".$rate.",0)
                    FROM KIN_RECORD_DAT T1 
                   WHERE SCHREGNO IN ( SELECT SCHREGNO
                                         FROM SCHREG_REGD_DAT
                                        WHERE YEAR = '".$a_Year."'
                                          AND SEMESTER = '".$a_Sem."'
                                          AND GRADE = '".$a_Row["GRADE"]."'
                                          AND HR_CLASS = '".$a_Row["HR_CLASS"]."')
                    AND T1.SUBCLASSCD = '".$a_Row["SUBCLASSCD"]."'
                    AND T1.YEAR = '".$a_Year."'";
        //echo $query .'<BR>';
        return $query;
    }

    //３学期補点
    function sec3_avg(&$a_Row, &$a_arT, $a_Year, $di_cd)
    {
        $rate = "";
        if($di_cd == "KS") $rate = " * 0.7";
        $query = "SELECT ROUND(ROUND((T1.SEM3_REC / T4.BASE_AVG) * DECIMAL(T5.SEM3_TERM_AVG) ,0) ".$rate.",0)
                   FROM 
                    (SELECT DECIMAL(SEM1_REC + SEM2_REC) / 2 AS SEM3_REC
                       FROM KIN_RECORD_DAT
                      WHERE SCHREGNO = '".$a_Row["SCHREGNO"]."'
                        AND SUBCLASSCD = '".$a_Row["SUBCLASSCD"]."'
                        AND YEAR = '".$a_Year."'
                    ) AS T1,
                    (SELECT DECIMAL((T2.SEM1_REC_AVG + T3.SEM2_REC_AVG)) / 2 as BASE_AVG
                       FROM 
                        (SELECT ROUND(AVG(DECIMAL(SEM1_REC)),0) AS SEM1_REC_AVG
                           FROM KIN_RECORD_DAT
                          WHERE SCHREGNO IN (SELECT SCHREGNO
                                               FROM SCHREG_REGD_DAT
                                              WHERE YEAR = '".$a_Year."'
                                                AND SEMESTER = '1' 
                                                AND GRADE = '".$a_Row["GRADE"]."' 
                                                AND HR_CLASS = '".$a_Row["HR_CLASS"]."') 
                           AND SUBCLASSCD = '".$a_Row["SUBCLASSCD"]."' 
                           AND YEAR = '".$a_Year."') AS T2,
                        (SELECT ROUND(AVG(DECIMAL(SEM2_REC)),0) AS SEM2_REC_AVG
                           FROM KIN_RECORD_DAT
                          WHERE SCHREGNO IN (SELECT SCHREGNO
                                               FROM SCHREG_REGD_DAT
                                              WHERE YEAR = '".$a_Year."'
                                                AND SEMESTER = '2' 
                                                AND GRADE = '".$a_Row["GRADE"]."' 
                                                AND HR_CLASS = '".$a_Row["HR_CLASS"]."') 
                        AND SUBCLASSCD = '".$a_Row["SUBCLASSCD"]."' 
                        AND YEAR = '".$a_Year."') AS T3
                    ) AS T4,
                    (SELECT ROUND(AVG(DECIMAL(SEM3_TERM_REC)),0) AS SEM3_TERM_AVG
                       FROM KIN_RECORD_DAT
                      WHERE SCHREGNO IN (SELECT SCHREGNO
                                           FROM SCHREG_REGD_DAT
                                          WHERE YEAR = '".$a_Year."'
                                            AND SEMESTER = '3'
                                            AND GRADE = '".$a_Row["GRADE"]."' 
                                            AND HR_CLASS = '".$a_Row["HR_CLASS"]."') 
                        AND SUBCLASSCD = '".$a_Row["SUBCLASSCD"]."' 
                        AND YEAR = '".$a_Year."'
                    ) AS T5 ";


        //echo $query .'<BR>';
        return $query;
    }

    //学期平均(成績)    2004/12/15 arakaki(近大-作業依頼書20041215)
    function sec_rec_avg(&$a_Row, &$a_arT, $a_Year, $a_Sem, $di_cd)
    {
        $rate = "";
        if($di_cd == "KS") $rate = " * 0.7";
        $query = "SELECT ROUND(ROUND((DEC(".$a_Row[$a_arT["base"]].")  / 
                                      DEC(T1.".$a_arT["base"]." )) * 
                                      DEC(T1.".$a_arT["target"] .") ,0) ".$rate.",0)
                   FROM 
                (SELECT T1.SEM1_REC AS SEM1_REC,T2.SEM2_REC AS SEM2_REC,T3.SEM3_REC AS SEM3_REC
                from
                    (SELECT ROUND(AVG(DEC(SEM1_REC)),0) AS SEM1_REC
                       FROM KIN_RECORD_DAT
                      WHERE SCHREGNO IN (SELECT SCHREGNO
                                           FROM SCHREG_REGD_DAT
                                          WHERE YEAR = '".$a_Year."'
                                            AND SEMESTER = '1' 
                                            AND GRADE = '".$a_Row["GRADE"]."' 
                                            AND HR_CLASS = '".$a_Row["HR_CLASS"]."') 
                        AND SUBCLASSCD = '".$a_Row["SUBCLASSCD"]."'
                        AND YEAR = '".$a_Year."') AS T1,
                    (SELECT ROUND(AVG(DEC(SEM2_REC)),0) AS SEM2_REC
                       FROM KIN_RECORD_DAT
                      WHERE SCHREGNO IN (SELECT SCHREGNO
                                           FROM SCHREG_REGD_DAT
                                          WHERE YEAR = '".$a_Year."'
                                            AND SEMESTER = '2' 
                                            AND GRADE = '".$a_Row["GRADE"]."' 
                                            AND HR_CLASS = '".$a_Row["HR_CLASS"]."') 
                        AND SUBCLASSCD = '".$a_Row["SUBCLASSCD"]."' 
                        AND YEAR = '".$a_Year."') AS T2,
                    (SELECT ROUND(AVG(DEC(SEM3_REC)),0) AS SEM3_REC
                       FROM KIN_RECORD_DAT
                      WHERE SCHREGNO IN (SELECT SCHREGNO
                                           FROM SCHREG_REGD_DAT
                                          WHERE YEAR = '".$a_Year."'
                                            AND SEMESTER = '3' 
                                            AND GRADE = '".$a_Row["GRADE"]."' 
                                            AND HR_CLASS = '".$a_Row["HR_CLASS"]."') 
                        AND SUBCLASSCD = '".$a_Row["SUBCLASSCD"]."' 
                        AND YEAR = '".$a_Year."') AS T3
                    ) AS T1";

        //echo $query .'<BR>';
        return $query;
    }

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //留学情報を配列にセット
        $query  = " SELECT SCHREGNO, TRANSFERCD ";
        $query .= " FROM   SCHREG_TRANSFER_DAT T1, ";
        $query .= "        SEMESTER_MST T2 ";
        $query .= " WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "        SEMESTER='9' AND ";
        $query .= "        TRANSFERCD='1' AND ";
        $query .= "       (TRANSFER_SDATE BETWEEN SDATE AND EDATE OR ";
        $query .= "        TRANSFER_EDATE BETWEEN SDATE AND EDATE) ";
        $ryugaku = array();
        $result = $db->query($query);
        while( $Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $ryugaku[$Row["SCHREGNO"]] = $Row["TRANSFERCD"];
        }

        $data = array();

        //0から100までの数値以外で有り得る値(空または出欠情報)
        $vals = array("","KK","KS");

        //各生徒ごとのデータ
        foreach ($model->score as $schregno => $subclass)
        {
            //各生徒の各科目の成績データ
            foreach ($subclass as $subclasscd => $val)
            {
                $data = array();
                $data[$model->sem_div][NUMBER] = $val;

                //学期成績に値が入っていれば、補点済みにする
                if ($val >= '0') {
                    $data[$model->sem_div."_FLG"][TEXT] = "1";
                } else {
                    $data[$model->sem_div."_FLG"][TEXT] = "";
                }

                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "SYSDATE()";

                $where  = "WHERE YEAR     = '".$model->cntl_dt_year."'";
                $where .= "AND SUBCLASSCD = '".$subclasscd."'";
                $where .= "AND SCHREGNO   = '".$schregno."'";

                $query = Query::updateSQL($data,"KIN_RECORD_DAT",$where);

                if($db->query($query) != DB_OK){
                    return false;
                    $db->rollback();
                    break 1;
                }

                //１，２学期の中間または期末なら学期平均を計算して更新
                if ($model->sem_cmb != "3") {
                    if ($model->div_cmb != "3") {
                        $avg_score = "";
                        $col_name = "";
                        $col_avg = "";

                        if ($model->div_cmb == "1") {
                            $col_name = "SEM".$model->sem_cmb."_TERM_REC";
                        } elseif ($model->div_cmb == "2") {
                            $col_name = "SEM".$model->sem_cmb."_INTER_REC";
                        }
                        $col_avg  = "SEM".$model->sem_cmb."_REC";

                        $query  = " SELECT ".$model->sem_div.",".$col_name;
                        $query .= "   FROM KIN_RECORD_DAT ";
                        $query .= "  WHERE YEAR       = '".$model->cntl_dt_year."' AND ";
                        $query .= "        SUBCLASSCD = '".$subclasscd."' AND ";
                        $query .= "        SCHREGNO   = '".$schregno."'"; 
                        $score = $db->getRow($query);

/*                        if($model->div_cmb == "1" && $score[1] != "") {
                            if($score[0] != "") {
                                $avg_score = round(((int)$score[0] + (int)$score[1]) / 2);
                            } else {
                                $avg_score = $score[1];
                            }
                        } elseif($model->div_cmb == "2" && $score[0] != "") {
                            if($score[1] != "") {
                                $avg_score = round(((int)$score[0] + (int)$score[1]) / 2);
                            } else {
                                $avg_score = $score[0];
                            }
                        } else {
                            $avg_score = "";
                        }
*/
                        //中間編集時
                        if($model->div_cmb == "1") {
                            if($score[0] != "" && $score[1] != "") {
                                $avg_score = round(((int)$score[0] + (int)$score[1]) / 2);
                            } else {
                                $avg_score = "";
                            }
                        //期末編集時
                        } else {
                            if($score[0] != "" && $score[1] != "") {
                                $avg_score = round(((int)$score[0] + (int)$score[1]) / 2);
                            } else if($score[0] != "" && $score[1] == "") {
                                $avg_score = $score[0];
                            } else {
                                $avg_score = "";
                            }
                        }

                        $data = array();
                        //学期成績
                        if (strlen($avg_score)) {
                            $data[$col_avg][NUMBER]      = (int)$avg_score;
                            $data[$col_avg."_FLG"][TEXT] = "1";
                        } else {
                            $data[$col_avg][NUMBER]      = "";
                            $data[$col_avg."_FLG"][TEXT] = "";
                        }                            

                        $where  = "WHERE YEAR     = '".$model->cntl_dt_year."'";
                        $where .= "AND SUBCLASSCD = '".$subclasscd."'";
                        $where .= "AND SCHREGNO   = '".$schregno."'";
                        
                        $query = Query::updateSQL($data,"KIN_RECORD_DAT",$where);

                        if($db->query($query) != DB_OK){
                            return false;
                            $db->rollback();
                            break 1;
                        }
                    }
                }

                $data = array();
                //生徒の成績データを取得
                $query  = " SELECT SEM1_REC,SEM2_REC,SEM3_TERM_REC ";
                $query .= "   FROM KIN_RECORD_DAT ";
                $query .= "  WHERE year = '".CTRL_YEAR."' AND ";
                $query .= "        subclasscd = '".$subclasscd."' AND ";
                $query .= "        schregno = '".$schregno."'";

                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

//                //①１学期成績あり、２学期成績なし、３年生、２学期の期末・学期成績編集時の場合
//                if ($row["SEM1_REC"] != "" && $row["SEM2_REC"] == "" && $model->gk_cmb == "03" && $model->sem_cmb == "2" && $model->div_cmb != "1") {
//
//                    //学年成績更新
//                    $data["GRADE_RECORD"][NUMBER]    = '';
//                    $data["A_PATTERN_ASSESS"][TEXT]  = '';
//                    $data["B_PATTERN_ASSESS"][TEXT]  = '';
//                    $data["C_PATTERN_ASSESS"][TEXT]  = '';
//                    $data["REGISTERCD"][TEXT]        = STAFFCD;
//                    $data["UPDATED"][FUNC]           = "sysdate()";
//
//                    $where  = " WHERE YEAR           = '".$model->cntl_dt_year."' AND ";
//                    $where .= "       SUBCLASSCD     = '".$subclasscd."' AND ";
//                    $where .= "       SCHREGNO       = '".$schregno."' ";
//
//                    $query = Query::updateSQL($data, "KIN_RECORD_DAT",$where);
//                    $data = array();
//                    if($db->query($query) != DB_OK){
//                        return false;
//                        $db->rollback();
//                        break 1;
//                    }
                //①３年生の場合
                //②１,２,３学期の全て入っている場合
                //③１,２学期のみの成績だけが入っていて、留学情報を持っている１,２年生の場合
                //④２,３学期のみの成績だけが入っていて、留学情報を持っている１,２年生の場合
//                } else if (($model->gk_cmb == "03") || 
                //３年生の場合
                //・１学期中間、１学期期末の補点
                //　学年平均は「１学期平均と同じ」
                //・１学期平均、２学期中間、２学期期末、２学期平均の補点
                //　学年平均は「１学期平均と２学期平均から算出」
                if (($model->gk_cmb == "03" && $model->sem_cmb == "1" && $model->div_cmb == "1" && $row["SEM1_REC"] != "") || 
                    ($model->gk_cmb == "03" && $model->sem_cmb == "1" && $model->div_cmb == "2" && $row["SEM1_REC"] != "") || 
                    ($model->gk_cmb == "03" && $model->sem_cmb == "1" && $model->div_cmb == "3" && $row["SEM1_REC"] != "" && $row["SEM2_REC"] != "") || 
                    ($model->gk_cmb == "03" && $model->sem_cmb == "2" && $model->div_cmb == "1" && $row["SEM1_REC"] != "" && $row["SEM2_REC"] != "") || 
                    ($model->gk_cmb == "03" && $model->sem_cmb == "2" && $model->div_cmb == "2" && $row["SEM1_REC"] != "" && $row["SEM2_REC"] != "") || 
                    ($model->gk_cmb == "03" && $model->sem_cmb == "2" && $model->div_cmb == "3" && $row["SEM1_REC"] != "" && $row["SEM2_REC"] != "") || 
                    ($row["SEM1_REC"] != "" && $row["SEM2_REC"] != "" && $row["SEM3_TERM_REC"] != "") || 
                    ($row["SEM1_REC"] != "" && $row["SEM2_REC"] != "" && $row["SEM3_TERM_REC"] == "" && $ryugaku[$schregno] == "1" && ($model->gk_cmb == "01" || $model->gk_cmb == "02")) || 
                    ($row["SEM1_REC"] == "" && $row["SEM2_REC"] != "" && $row["SEM3_TERM_REC"] != "" && $ryugaku[$schregno] == "1" && ($model->gk_cmb == "01" || $model->gk_cmb == "02"))) {

                    //2004/12/17 arakaki 学年成績更新
                    $query  = " UPDATE KIN_RECORD_DAT ";
                    $query .= " SET (GRADE_RECORD,A_PATTERN_ASSESS,B_PATTERN_ASSESS,C_PATTERN_ASSESS)= ";
                    $query .= "         (SELECT T11.AVG_SEM_REC,TA.A_PATTERN_ASSESS,TB.B_PATTERN_ASSESS,TC.C_PATTERN_ASSESS ";
                    $query .= "            FROM ";
                    $query .= "                 (SELECT round(AVG(CAST(SEM_REC AS DECIMAL)),0) AS AVG_SEM_REC ";
                    $query .= "                    FROM ";
                    $query .= "                         (SELECT YEAR,SUBCLASSCD,SCHREGNO,SEM1_REC AS SEM_REC ";
                    $query .= "                            FROM KIN_RECORD_DAT ";
                    $query .= "                           WHERE YEAR='".$model->cntl_dt_year."'  ";
                    $query .= "                             AND SUBCLASSCD='".$subclasscd."'  ";
                    $query .= "                             AND SCHREGNO='".$schregno."' ";
                    if (($model->gk_cmb == "03" && $model->sem_cmb == "1" && $model->div_cmb == "1" && $row["SEM1_REC"] != "") || 
                        ($model->gk_cmb == "03" && $model->sem_cmb == "1" && $model->div_cmb == "2" && $row["SEM1_REC"] != "")) {
                    } else {
                        $query .= "                   UNION ALL ";
                        $query .= "                      SELECT YEAR,SUBCLASSCD,SCHREGNO,SEM2_REC AS SEM_REC ";
                        $query .= "                        FROM KIN_RECORD_DAT ";
                        $query .= "                       WHERE YEAR='".$model->cntl_dt_year."'  ";
                        $query .= "                         AND SUBCLASSCD='".$subclasscd."'  ";
                        $query .= "                         AND SCHREGNO='".$schregno."' ";
                        $query .= "                   UNION ALL ";
                        $query .= "                      SELECT YEAR,SUBCLASSCD,SCHREGNO,SEM3_TERM_REC AS SEM_REC ";
                        $query .= "                        FROM KIN_RECORD_DAT ";
                        $query .= "                       WHERE YEAR='".$model->cntl_dt_year."'  ";
                        $query .= "                         AND SUBCLASSCD='".$subclasscd."' ";
                        $query .= "                         AND SCHREGNO='".$schregno."' ";
                    }
                    $query .= "                         ) AS T1 ";
                    $query .= "                  GROUP BY YEAR,SUBCLASSCD,SCHREGNO ";
                    $query .= "                 ) AS T11 ";
                    $query .= "                 LEFT JOIN ";
                    $query .= "                 (SELECT TYPE_ASSES_LEVEL AS A_PATTERN_ASSESS,TYPE_ASSES_HIGH,TYPE_ASSES_LOW ";
                    $query .= "                   FROM TYPE_ASSES_MST ";
                    $query .= "                  WHERE YEAR = '".$model->cntl_dt_year."' ";
                    $query .= "                    AND TYPE_ASSES_CD='A' ";
                    $query .= "                 ) AS TA ";
                    $query .= "                 ON T11.AVG_SEM_REC <= TA.TYPE_ASSES_HIGH AND T11.AVG_SEM_REC >= TA.TYPE_ASSES_LOW ";
                    $query .= "                 LEFT JOIN ";
                    $query .= "                 (SELECT TYPE_ASSES_LEVEL AS B_PATTERN_ASSESS,TYPE_ASSES_HIGH,TYPE_ASSES_LOW ";
                    $query .= "                   FROM TYPE_ASSES_MST ";
                    $query .= "                  WHERE YEAR = '".$model->cntl_dt_year."' ";
                    $query .= "                    AND TYPE_ASSES_CD='B' ";
                    $query .= "                 ) AS TB ";
                    $query .= "                 ON T11.AVG_SEM_REC <= TB.TYPE_ASSES_HIGH AND T11.AVG_SEM_REC >= TB.TYPE_ASSES_LOW ";
                    $query .= "                 LEFT JOIN ";
                    $query .= "                 (SELECT TYPE_ASSES_LEVEL AS C_PATTERN_ASSESS,TYPE_ASSES_HIGH,TYPE_ASSES_LOW ";
                    $query .= "                   FROM TYPE_ASSES_MST ";
                    $query .= "                  WHERE YEAR = '".$model->cntl_dt_year."' ";
                    $query .= "                    AND TYPE_ASSES_CD='C' ";
                    $query .= "                 ) AS TC ";
                    $query .= "                 ON T11.AVG_SEM_REC <= TC.TYPE_ASSES_HIGH AND T11.AVG_SEM_REC >= TC.TYPE_ASSES_LOW ";
                    $query .= " ) ";
                    $query .= " WHERE YEAR='".$model->cntl_dt_year."' ";
                    $query .= "   AND SUBCLASSCD='".$subclasscd."' ";
                    $query .= "   AND SCHREGNO='".$schregno."' ";

                    if($db->query($query) != DB_OK){
                        return false;
                        $db->rollback();
                        break 1;
                    }
                }else{

                    //学年成績更新
                    $data["GRADE_RECORD"][NUMBER]    = '';
                    $data["A_PATTERN_ASSESS"][TEXT]  = '';
                    $data["B_PATTERN_ASSESS"][TEXT]  = '';
                    $data["C_PATTERN_ASSESS"][TEXT]  = '';
                    $data["REGISTERCD"][TEXT]        = STAFFCD;
                    $data["UPDATED"][FUNC]           = "sysdate()";

                    $where  = " WHERE YEAR           = '".$model->cntl_dt_year."' AND ";
                    $where .= "       SUBCLASSCD     = '".$subclasscd."' AND ";
                    $where .= "       SCHREGNO       = '".$schregno."' ";

                    $query = Query::updateSQL($data, "KIN_RECORD_DAT",$where);
                    $data = array();
                    if($db->query($query) != DB_OK){
                        return false;
                        $db->rollback();
                        break 1;
                    }
                }    
            }   
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //在籍期間の取得 2006/02/07
    function getOnTheRegisterPeriod($year,$date,$schregno)
    {
        $query .= " WITH T01 AS( ";
        $query .= "     SELECT CASE WHEN t1.ENT_DATE IS NULL THEN t2.SDATE ELSE t1.ENT_DATE END AS ENT_DATE, ";
        $query .= "            CASE WHEN t1.GRD_DATE IS NULL THEN T2.EDATE ELSE t1.GRD_DATE END AS GRD_DATE ";
        $query .= "       FROM SCHREG_BASE_MST AS t1, ";
        $query .= "            SEMESTER_MST    AS t2 ";
        $query .= "      WHERE t2.YEAR='".$year."' ";
        $query .= "        AND t2.SEMESTER='9' ";
        $query .= "        AND t1.SCHREGNO = '".$schregno."' ";
        $query .= " ) ";
        $query .= " SELECT COUNT(*) AS CNT ";
        $query .= "   FROM T01 ";
        $query .= "  WHERE NOT('".$date."' BETWEEN ENT_DATE AND GRD_DATE) ";
        return $query;
    }
    //異動者情報の取得 2006/02/07
    function getTransferData($year,$date,$schregno)
    {
        $query  = " WITH T01 AS( ";
        $query .= "     SELECT DISTINCT  ";
        $query .= "            t2.TRANSFERCD, ";
        $query .= "            t2.TRANSFER_SDATE, ";
        $query .= "            CASE WHEN t2.TRANSFER_EDATE IS NULL THEN t3.EDATE ELSE t2.TRANSFER_EDATE END AS TRANSFER_EDATE ";
        $query .= "       FROM SCHREG_REGD_DAT t1, ";
        $query .= "            SCHREG_TRANSFER_DAT t2, ";
        $query .= "            SEMESTER_MST t3 ";
        $query .= "      WHERE t1.SCHREGNO = t2.SCHREGNO  ";
        $query .= "        AND t2.TRANSFERCD IN ('1', '2', '3')  ";
        $query .= "        AND t1.YEAR = T3.YEAR  ";
        $query .= "        AND t3.SEMESTER = '9'  ";
        $query .= "        AND t1.YEAR = '".$year."' ";
        $query .= "        AND t1.SCHREGNO = '".$schregno."' ";
        $query .= " ) ";
        $query .= " SELECT COUNT(*) AS CNT";
        $query .= "   FROM T01 ";
        $query .= "  WHERE '".$date."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        return $query;
    }
}
?>
