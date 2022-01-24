<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd510kQuery.inc 56580 2017-10-22 12:35:29Z maeshiro $
class knjd510kQuery extends Query
{
    //knjd510kQuery start

    //中間テストがあるか？
    function getSchTest($year, $semester, $kindcd, $itemcd, $schregno, $subclasscd)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST L1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T1 ON T1.YEAR      = L1.YEAR ";
        $query .= "                                AND T1.SEMESTER  = L1.SEMESTER ";
        $query .= "                                AND T1.CHAIRCD   = L1.CHAIRCD ";
        $query .= "                                AND T1.SCHREGNO  = '{$schregno}' ";
        $query .= "                                AND L1.EXECUTEDATE BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ON T2.YEAR          = T1.YEAR ";
        $query .= "                            AND T2.SEMESTER      = T1.SEMESTER ";
        $query .= "                            AND T2.CHAIRCD       = T1.CHAIRCD ";
        $query .= "                            AND T2.SUBCLASSCD    = '{$subclasscd}' ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '{$year}' AND ";
        $query .= "     L1.SEMESTER = '{$semester}' AND ";
        $query .= "     L1.TESTKINDCD = '{$kindcd}' AND ";
        $query .= "     L1.TESTITEMCD = '{$itemcd}' ";
        return $query;
    }

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
        return $query;
    }

    //データ取得
    function get_main_data($a_Grade,$a_Year,$a_mode="off")
    {
        //表示フラグ判定
        if($a_mode == "on")
        {
           $show_flg = "";
           $show_flg  = " AND (T3.SEM1_INTER_REC_FLG = '0' OR T3.SEM1_INTER_REC_FLG = '1' OR T3.SEM1_INTER_REC_FLG = '2' OR T3.SEM1_INTER_REC_FLG IS NULL )  ";
           $show_flg .= " AND (T3.SEM1_TERM_REC_FLG  = '0' OR T3.SEM1_TERM_REC_FLG  = '1' OR  T3.SEM1_TERM_REC_FLG  = '2' OR T3.SEM1_TERM_REC_FLG  IS NULL )  ";
           $show_flg .= " AND (T3.SEM1_REC_FLG       = '0' OR T3.SEM1_REC_FLG       = '1' OR T3.SEM1_REC_FLG       = '2' OR T3.SEM1_REC_FLG       IS NULL )  ";
           $show_flg .= " AND (T3.SEM2_INTER_REC_FLG = '0' OR T3.SEM2_INTER_REC_FLG = '1' OR T3.SEM2_INTER_REC_FLG = '2' OR T3.SEM2_INTER_REC_FLG IS NULL )  ";
           $show_flg .= " AND (T3.SEM2_TERM_REC_FLG  = '0' OR T3.SEM2_TERM_REC_FLG  = '1' OR T3.SEM2_TERM_REC_FLG  = '2' OR T3.SEM2_TERM_REC_FLG  IS NULL )  ";
           $show_flg .= " AND (T3.SEM2_REC_FLG       = '0' OR T3.SEM2_REC_FLG       = '1' OR T3.SEM2_REC_FLG       = '2' OR T3.SEM2_REC_FLG       IS NULL )  ";
           $show_flg .= " AND (T3.SEM3_TERM_REC_FLG  = '0' OR T3.SEM3_TERM_REC_FLG  = '1' OR T3.SEM3_TERM_REC_FLG  = '2' OR T3.SEM3_TERM_REC_FLG  IS NULL )  ";
        }else{
            $show_flg  = " AND (T3.SEM1_INTER_REC_FLG = '0' OR T3.SEM1_INTER_REC_FLG IS NULL )  ";
            $show_flg .= " AND (T3.SEM1_TERM_REC_FLG  = '0' OR T3.SEM1_TERM_REC_FLG  IS NULL )  ";
            $show_flg .= " AND (T3.SEM1_REC_FLG       = '0' OR T3.SEM1_REC_FLG       IS NULL )  ";
            $show_flg .= " AND (T3.SEM2_INTER_REC_FLG = '0' OR T3.SEM2_INTER_REC_FLG IS NULL )  ";
            $show_flg .= " AND (T3.SEM2_TERM_REC_FLG  = '0' OR T3.SEM2_TERM_REC_FLG  IS NULL )  ";
            $show_flg .= " AND (T3.SEM2_REC_FLG       = '0' OR T3.SEM2_REC_FLG       IS NULL )  ";
            $show_flg .= " AND (T3.SEM3_TERM_REC_FLG  = '0' OR T3.SEM3_TERM_REC_FLG  IS NULL )  ";
        }

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
                    WHERE T6.GRADE = '".$a_Grade."'
                      AND T3.YEAR  = '".$a_Year."'
                      AND (T3.SEM1_INTER_REC_DI IN ('KK','KS') OR
                           T3.SEM1_TERM_REC_DI  IN ('KK','KS') OR
                           T3.SEM2_INTER_REC_DI IN ('KK','KS') OR
                           T3.SEM2_TERM_REC_DI  IN ('KK','KS') OR
                           T3.SEM3_TERM_REC_DI  IN ('KK','KS'))
                    ".$show_flg."

                    ORDER BY T6.GRADE,T6.HR_CLASS,T6.ATTENDNO,T3.SUBCLASSCD";
                    //2004/12/14 arakaki 近大-作業依頼書20041214-01
                    //ORDER BY T3.SCHREGNO,T3.CHAIRCD";

//echo $query."<BR>";
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
        
        /***** $model->score内の変数の一例(チェックされた行のテキストボックスの値のみを格納)
                [20020353]=>                     ←学籍番号
                  array(1) {
                    ["011400"]=>                 ←科目コード
                    array(5) {
                      ["SEM1_INTER_REC"]=>       ←1学期中間
                      string(2) "44"
                      ["SEM1_TERM_REC"]=>        ←1学期期末
                      string(2) "55"
                      ["SEM1_REC"]=>             ←1学期平均
                      string(2) "90"
                      ["SEM2_INTER_REC"]=>       ←2学期中間
                      string(2) "66"
                      ["SEM2_REC"]=>             ←2学期平均
                      string(0) ""        
        ******************************************************************************************/

        //初期化
        $return_value = true;
        $data = array();

        //0から100までの数値以外で有り得る値(空または出欠情報)
        $vals = array("","KK","KS");

        //各生徒ごとのデータ(チェックされたレコード、フラグも更新)
        foreach ($model->score as $schregno => $subclass)
        {
            //各生徒の各科目の成績データ
            foreach ($subclass as $subclasscd => $val)
            {
                //各生徒の各科目の各テストの成績データ
                foreach ($val as $col => $score)
                {
                    $data[$col][NUMBER] = $score;

                    //学期成績に値が入っている時２を更新する
                    if ($score >= '0') {
                        $data[$col."_FLG"][TEXT] = "2";
                    } else {
                        $data[$col."_FLG"][TEXT] = "";
                    }
                }

                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "SYSDATE()";

                $where  = "WHERE YEAR     = '".$model->cntl_dt_year."'";
                $where .= "AND SUBCLASSCD = '".$subclasscd."'";
                $where .= "AND SCHREGNO   = '".$schregno."'";

                $query = Query::updateSQL($data,"KIN_RECORD_DAT",$where);

                if($db->query($query) != DB_OK){
                    $return_value = false;
                    $db->rollback();
                    break 2;
                }
                $data = array();

                //生徒の成績データを取得
                $query  = " SELECT SEM1_REC,SEM2_REC,SEM3_TERM_REC ";
                $query .= "   FROM KIN_RECORD_DAT ";
                $query .= "  WHERE year = '".CTRL_YEAR."' AND ";
                $query .= "        subclasscd = '".$subclasscd."' AND ";
                $query .= "        schregno = '".$schregno."'";

                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

//                //①１学期成績あり、２学期成績なし、３年生、２学期の編集時の場合
//                if ($row["SEM1_REC"] != "" && $row["SEM2_REC"] == "" && $model->gk_cmb == "03" && CTRL_SEMESTER == "2") {
//
//                    //学年成績更新
//                    $data["GRADE_RECORD"][NUMBER]    = '';
//                    $data["A_PATTERN_ASSESS"][TEXT]  = '';
//                    $data["B_PATTERN_ASSESS"][TEXT]  = '';
//                    $data["C_PATTERN_ASSESS"][TEXT]  = '';
//                    $data["REGISTERCD"][TEXT]        = STAFFCD ;
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
//                        break 2;
//                    }
               //①３年生の場合
               //②１,２,３学期の全て入っている場合
               //③１,２学期のみの成績だけが入っていて、留学情報を持っている１,２年生の場合
               //④２,３学期のみの成績だけが入っていて、留学情報を持っている１,２年生の場合
//               } else if (($model->gk_cmb == "03") || 
               //３年生の場合
               //・１学期を指定した場合
               //　学年平均は「１学期平均と同じ」
               //・２学期を指定した場合
               //　学年平均は「１学期平均と２学期平均から算出」
               if (($model->gk_cmb == "03" && $model->sem_cmb == "1" && $row["SEM1_REC"] != "") || 
                   ($model->gk_cmb == "03" && $model->sem_cmb == "2" && $row["SEM1_REC"] != "" && $row["SEM2_REC"] != "") || 
                   ($row["SEM1_REC"] != "" && $row["SEM2_REC"] != "" && $row["SEM3_TERM_REC"] != "") || 
                   ($row["SEM1_REC"] != "" && $row["SEM2_REC"] != "" && $row["SEM3_TERM_REC"] == "" && $ryugaku[$schregno] == "1" && ($model->gk_cmb == "01" || $model->gk_cmb == "02")) || 
                   ($row["SEM1_REC"] == "" && $row["SEM2_REC"] != "" && $row["SEM3_TERM_REC"] != "" && $ryugaku[$schregno] == "1" && ($model->gk_cmb == "01" || $model->gk_cmb == "02"))) {

                    //2004/12/17 arakaki 学年成績更新
                    $query  = "  UPDATE KIN_RECORD_DAT ";
                    $query .= "  SET (GRADE_RECORD,A_PATTERN_ASSESS,B_PATTERN_ASSESS,C_PATTERN_ASSESS)= ";
                    $query .= "         (SELECT T11.AVG_SEM_REC,TA.A_PATTERN_ASSESS,TB.B_PATTERN_ASSESS,TC.C_PATTERN_ASSESS ";
                    $query .= "             FROM ";
                    $query .= "                  (SELECT ROUND(AVG(DECIMAL(SEM_REC)),0) AS AVG_SEM_REC ";
                    $query .= "                     FROM ";
                    $query .= "                          (SELECT YEAR,SUBCLASSCD,SCHREGNO,SEM1_REC AS SEM_REC ";
                    $query .= "                             FROM KIN_RECORD_DAT ";
                    $query .= "                            WHERE YEAR='".$model->cntl_dt_year."' ";
                    $query .= "                              AND SUBCLASSCD='".$subclasscd."' ";
                    $query .= "                              AND SCHREGNO='".$schregno."' ";
                    if (($model->gk_cmb == "03" && $model->sem_cmb == "1" && $row["SEM1_REC"] != "")) {
                    } else {
                        $query .= "                    UNION ALL ";
                        $query .= "                       SELECT YEAR,SUBCLASSCD,SCHREGNO,SEM2_REC AS SEM_REC ";
                        $query .= "                         FROM KIN_RECORD_DAT ";
                        $query .= "                        WHERE YEAR='".$model->cntl_dt_year."' ";
                        $query .= "                          AND SUBCLASSCD='".$subclasscd."' ";
                        $query .= "                          AND SCHREGNO='".$schregno."' ";
                        $query .= "                    UNION ALL ";
                        $query .= "                       SELECT YEAR,SUBCLASSCD,SCHREGNO,SEM3_TERM_REC AS SEM_REC ";
                        $query .= "                         FROM KIN_RECORD_DAT ";
                        $query .= "                        WHERE YEAR='".$model->cntl_dt_year."'  ";
                        $query .= "                          AND SUBCLASSCD='".$subclasscd."'  ";
                        $query .= "                          AND SCHREGNO='".$schregno."' ";
                    }
                    $query .= "                          ) AS T1  ";
                    $query .= "                   GROUP BY YEAR,SUBCLASSCD,SCHREGNO  ";
                    $query .= "                  ) AS T11 ";
                    $query .= "                 LEFT JOIN ";
                    $query .= "                  (SELECT TYPE_ASSES_LEVEL AS A_PATTERN_ASSESS,TYPE_ASSES_HIGH,TYPE_ASSES_LOW ";
                    $query .= "                    FROM TYPE_ASSES_MST ";
                    $query .= "                   WHERE YEAR = '".$model->cntl_dt_year."' ";
                    $query .= "                     AND TYPE_ASSES_CD='A' ";
                    $query .= "                  ) AS TA ";
                    $query .= "                 ON T11.AVG_SEM_REC <= TA.TYPE_ASSES_HIGH AND T11.AVG_SEM_REC >= TA.TYPE_ASSES_LOW ";
                    $query .= "                 LEFT JOIN ";
                    $query .= "                  (SELECT TYPE_ASSES_LEVEL AS B_PATTERN_ASSESS,TYPE_ASSES_HIGH,TYPE_ASSES_LOW ";
                    $query .= "                    FROM TYPE_ASSES_MST ";
                    $query .= "                   WHERE YEAR = '".$model->cntl_dt_year."' ";
                    $query .= "                     AND TYPE_ASSES_CD='B' ";
                    $query .= "                  ) AS TB ";
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
                    $query .= "  AND SUBCLASSCD='".$subclasscd."' ";
                    $query .= "  AND SCHREGNO='".$schregno."' ";
                    //echo $query ;
                    if($db->query($query) != DB_OK){
                        return false;
                        $db->rollback();
                        break 2;
                    }
               } else {

                    //学年成績更新
                    $data["GRADE_RECORD"][NUMBER]    = '';
                    $data["A_PATTERN_ASSESS"][TEXT]  = '';
                    $data["B_PATTERN_ASSESS"][TEXT]  = '';
                    $data["C_PATTERN_ASSESS"][TEXT]  = '';
                    $data["REGISTERCD"][TEXT]        = STAFFCD ;                                           //登録者コード
                    $data["UPDATED"][FUNC]           = "sysdate()";                                        //更新日付

                    $where  = " WHERE YEAR           = '".$model->cntl_dt_year."' AND ";
                    $where .= "       SUBCLASSCD     = '".$subclasscd."' AND ";
                    $where .= "       SCHREGNO       = '".$schregno."' ";

                    $query = Query::updateSQL($data, "KIN_RECORD_DAT",$where);
                    $data = array();
                    if($db->query($query) != DB_OK){
                        return false;
                        $db->rollback();
                        break 2;
                    }
               }
            }
        }

        //各生徒ごとのデータ(チェックされてないレコード、値が空だったらNULLで更新)
        $data = array();
        foreach ($model->score2 as $schregno => $subclass)
        {
            //各生徒の各科目の成績データ
            foreach ($subclass as $subclasscd => $val)
            {
                //各生徒の各科目の各テストの成績データ
                foreach ($val as $col => $score)
                {
                    if (!strlen($score)) {
                        $data[$col][NUMBER] = $score;
                        $data[$col."_FLG"][TEXT] = "";
                    }
                }

                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "SYSDATE()";

                $where  = "WHERE YEAR     = '".$model->cntl_dt_year."'";
                $where .= "AND SUBCLASSCD = '".$subclasscd."'";
                $where .= "AND SCHREGNO   = '".$schregno."'";

                $query = Query::updateSQL($data,"KIN_RECORD_DAT",$where);

                if($db->query($query) != DB_OK){
                    $return_value = false;
                    $db->rollback();
                    break 2;
                }
                $data = array();

                //生徒の成績データを取得
                $query  = " SELECT SEM1_REC,SEM2_REC,SEM3_TERM_REC ";
                $query .= "   FROM KIN_RECORD_DAT ";
                $query .= "  WHERE year = '".CTRL_YEAR."' AND ";
                $query .= "        subclasscd = '".$subclasscd."' AND ";
                $query .= "        schregno = '".$schregno."'";

                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

//                //①１学期成績あり、２学期成績なし、３年生、２学期の編集時の場合
//                if ($row["SEM1_REC"] != "" && $row["SEM2_REC"] == "" && $model->gk_cmb == "03" && CTRL_SEMESTER == "2") {
//
//                    //学年成績更新
//                    $data["GRADE_RECORD"][NUMBER]    = '';
//                    $data["A_PATTERN_ASSESS"][TEXT]  = '';
//                    $data["B_PATTERN_ASSESS"][TEXT]  = '';
//                    $data["C_PATTERN_ASSESS"][TEXT]  = '';
//                    $data["REGISTERCD"][TEXT]        = STAFFCD ;
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
//                        break 2;
//                    }
               //①３年生の場合
               //②１,２,３学期の全て入っている場合
               //③１,２学期のみの成績だけが入っていて、留学情報を持っている１,２年生の場合
               //④２,３学期のみの成績だけが入っていて、留学情報を持っている１,２年生の場合
//               } else if (($model->gk_cmb == "03") || 
               //３年生の場合
               //・１学期を指定した場合
               //　学年平均は「１学期平均と同じ」
               //・２学期を指定した場合
               //　学年平均は「１学期平均と２学期平均から算出」
               if (($model->gk_cmb == "03" && $model->sem_cmb == "1" && $row["SEM1_REC"] != "") || 
                   ($model->gk_cmb == "03" && $model->sem_cmb == "2" && $row["SEM1_REC"] != "" && $row["SEM2_REC"] != "") || 
                   ($row["SEM1_REC"] != "" && $row["SEM2_REC"] != "" && $row["SEM3_TERM_REC"] != "") || 
                   ($row["SEM1_REC"] != "" && $row["SEM2_REC"] != "" && $row["SEM3_TERM_REC"] == "" && $ryugaku[$schregno] == "1" && ($model->gk_cmb == "01" || $model->gk_cmb == "02")) || 
                   ($row["SEM1_REC"] == "" && $row["SEM2_REC"] != "" && $row["SEM3_TERM_REC"] != "" && $ryugaku[$schregno] == "1" && ($model->gk_cmb == "01" || $model->gk_cmb == "02"))) {

                   //2004/12/17 arakaki 学年成績更新
                    $query  = "  UPDATE KIN_RECORD_DAT ";
                    $query .= "  SET (GRADE_RECORD,A_PATTERN_ASSESS,B_PATTERN_ASSESS,C_PATTERN_ASSESS)= ";
                    $query .= "          (SELECT T11.AVG_SEM_REC,TA.A_PATTERN_ASSESS,TB.B_PATTERN_ASSESS,TC.C_PATTERN_ASSESS ";
                    $query .= "             FROM ";
                    $query .= "                  (SELECT ROUND(AVG(DECIMAL(SEM_REC)),0) AS AVG_SEM_REC ";
                    $query .= "                     FROM ";
                    $query .= "                          (SELECT YEAR,SUBCLASSCD,SCHREGNO,SEM1_REC AS SEM_REC ";
                    $query .= "                             FROM KIN_RECORD_DAT ";
                    $query .= "                            WHERE YEAR='".$model->cntl_dt_year."'  ";
                    $query .= "                              AND SUBCLASSCD='".$subclasscd."'  ";
                    $query .= "                              AND SCHREGNO='".$schregno."' ";
                    if (($model->gk_cmb == "03" && $model->sem_cmb == "1" && $row["SEM1_REC"] != "")) {
                    } else {
                        $query .= "                    UNION ALL ";
                        $query .= "                       SELECT YEAR,SUBCLASSCD,SCHREGNO,SEM2_REC AS SEM_REC ";
                        $query .= "                         FROM KIN_RECORD_DAT ";
                        $query .= "                        WHERE YEAR='".$model->cntl_dt_year."'  ";
                        $query .= "                          AND SUBCLASSCD='".$subclasscd."'  ";
                        $query .= "                          AND SCHREGNO='".$schregno."' ";
                        $query .= "                    UNION ALL ";
                        $query .= "                       SELECT YEAR,SUBCLASSCD,SCHREGNO,SEM3_TERM_REC AS SEM_REC ";
                        $query .= "                         FROM KIN_RECORD_DAT ";
                        $query .= "                        WHERE YEAR='".$model->cntl_dt_year."'  ";
                        $query .= "                          AND SUBCLASSCD='".$subclasscd."' ";
                        $query .= "                          AND SCHREGNO='".$schregno."' ";
                    }
                    $query .= "                          ) AS T1 ";
                    $query .= "                   GROUP BY YEAR,SUBCLASSCD,SCHREGNO ";
                    $query .= "                  ) AS T11 ";
                    $query .= "                  LEFT JOIN ";
                    $query .= "                  (SELECT TYPE_ASSES_LEVEL AS A_PATTERN_ASSESS,TYPE_ASSES_HIGH,TYPE_ASSES_LOW ";
                    $query .= "                    FROM TYPE_ASSES_MST ";
                    $query .= "                   WHERE YEAR = '".$model->cntl_dt_year."' ";
                    $query .= "                     AND TYPE_ASSES_CD='A' ";
                    $query .= "                  ) AS TA ";
                    $query .= "                  ON T11.AVG_SEM_REC <= TA.TYPE_ASSES_HIGH AND T11.AVG_SEM_REC >= TA.TYPE_ASSES_LOW ";
                    $query .= "                  LEFT JOIN ";
                    $query .= "                  (SELECT TYPE_ASSES_LEVEL AS B_PATTERN_ASSESS,TYPE_ASSES_HIGH,TYPE_ASSES_LOW ";
                    $query .= "                    FROM TYPE_ASSES_MST ";
                    $query .= "                   WHERE YEAR = '".$model->cntl_dt_year."' ";
                    $query .= "                     AND TYPE_ASSES_CD='B' ";
                    $query .= "                  ) AS TB ";
                    $query .= "                  ON T11.AVG_SEM_REC <= TB.TYPE_ASSES_HIGH AND T11.AVG_SEM_REC >= TB.TYPE_ASSES_LOW ";
                    $query .= "                  LEFT JOIN ";
                    $query .= "                  (SELECT TYPE_ASSES_LEVEL AS C_PATTERN_ASSESS,TYPE_ASSES_HIGH,TYPE_ASSES_LOW ";
                    $query .= "                    FROM TYPE_ASSES_MST ";
                    $query .= "                   WHERE YEAR = '".$model->cntl_dt_year."' ";
                    $query .= "                     AND TYPE_ASSES_CD='C' ";
                    $query .= "                  ) AS TC ";
                    $query .= "                  ON T11.AVG_SEM_REC <= TC.TYPE_ASSES_HIGH AND T11.AVG_SEM_REC >= TC.TYPE_ASSES_LOW ";
                    $query .= "  ) ";
                    $query .= "  WHERE YEAR='".$model->cntl_dt_year."' ";
                    $query .= "   AND SUBCLASSCD='".$subclasscd."' ";
                    $query .= "   AND SCHREGNO='".$schregno."' ";
                    //echo $query ;
                    if($db->query($query) != DB_OK){
                        return false;
                        $db->rollback();
                        break 2;
                    }
               } else {

                    //学年成績更新
                    $data["GRADE_RECORD"][NUMBER]    = '';
                    $data["A_PATTERN_ASSESS"][TEXT]  = '';
                    $data["B_PATTERN_ASSESS"][TEXT]  = '';
                    $data["C_PATTERN_ASSESS"][TEXT]  = '';
                    $data["REGISTERCD"][TEXT]        = STAFFCD ;
                    $data["UPDATED"][FUNC]           = "sysdate()";

                    $where  = " WHERE YEAR           = '".$model->cntl_dt_year."' AND ";
                    $where .= "       SUBCLASSCD     = '".$subclasscd."' AND ";
                    $where .= "       SCHREGNO       = '".$schregno."' ";

                    $query = Query::updateSQL($data, "KIN_RECORD_DAT",$where);
                    $data = array();
                    if($db->query($query) != DB_OK){
                        return false;
                        $db->rollback();
                        break 2;
                    }
               }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $return_value;
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

    //knjd510kQuery end
}
?>
