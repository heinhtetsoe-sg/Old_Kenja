<?php

require_once('for_php7.php');
class knjx_a125pquery extends Query
{

    //名称マスタ取得
    public function getNameMst($model, $namecd1, $name1 = "")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($name1) {
            $query .= "     AND NAME1 = '".$name1."' ";
        }

        return $query;
    }
    
    //年度一覧
    public function getSelectFieldSQL($model, $flg = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        if ($flg) {
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
            $query .= "     T1.SEMESTER = '".$model->exp_semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model, $flg = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        if ($flg) {
            $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON ";
            $query .= "             T3.YEAR     = T2.YEAR AND ";
            $query .= "             T3.SEMESTER = T2.SEMESTER AND ";
            $query .= "             T3.GRADE    = T2.GRADE AND ";
            $query .= "             T3.HR_CLASS = T2.HR_CLASS AND ";
            $query .= "             T3.SCHREGNO = '".$model->schregno."'";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T2.YEAR || T2.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.SCHOOL_KIND = 'P' ";

        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= " AND (T2.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T2.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T2.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "      T2.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T2.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T2.SUBTR_CD3 = '". STAFFCD ."')";
        }


        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data = array();
            //データセット
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["ANNUAL"][TEXT]           = $data_arr[$i]["ANNUAL"];
            foreach ($model->fieldSize as $key => $val) {
                if (strpos($key, 'RECORD') !== false) {
                    continue;
                }
                if (in_array($key, array("FOREIGNLANGACT1", "FOREIGNLANGACT2", "FOREIGNLANGACT3"))) {
                    if ($data["YEAR"][TEXT] < $model->gaikokugo_change_year) {
                        $data[$key][TEXT]    = $data_arr[$i][$key];
                    }
                } elseif (in_array($key, array("FOREIGNLANGACT1_2"))) {
                    if ($data["YEAR"][TEXT] >= $model->gaikokugo_change_year) {
                        $data["FOREIGNLANGACT1"][TEXT]    = $data_arr[$i][$key];
                    }
                } else {
                    $data[$key][TEXT]    = $data_arr[$i][$key];
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_P_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "HTRAINREMARK_P_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$data_arr[$i]["YEAR"]."' AND ";
                $where .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HTRAINREMARK_P_DAT", $where);
            }
            $db->query($query);

            $data = array();
            //データセット
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            foreach ($model->fieldSize as $key => $val) {
                if (strpos($key, 'RECORD') === false) {
                    continue;
                }

                $query  = " DELETE FROM ";
                $query .= "     BEHAVIOR_DAT ";
                $query .= " WHERE ";
                $query .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $query .= "     YEAR     = '".$data_arr[$i]["YEAR"]."' AND ";
                $query .= "     DIV      = '".substr($key, 6, 1)."' AND ";
                $query .= "     CODE     = '".substr($key, 7, 2)."' ";
                $db->query($query);

                $data["DIV"][TEXT] = substr($key, 6, 1);
                $data["CODE"][TEXT] = substr($key, 7, 2);
                $data["ANNUAL"][TEXT] = $data_arr[$i]['ANNUAL'];
                $data["RECORD"][TEXT] = $data_arr[$i][$key];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                if ($data_arr[$i][$key] == 1) {
                    $query = Query::insertSQL($data, "BEHAVIOR_DAT");
                    $db->query($query);
                }
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     HTRAINREMARK_P_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $db->query($query);
            
            $query  = " DELETE FROM ";
            $query .= "     BEHAVIOR_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     YEAR     = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     DIV      IN ('3','4') ";
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //年次取得
    public function getAnnual($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //存在チェック
    public function checkSchregRegdDat($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' AND ";
        $query .= "     GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";

        return $query;
    }

    //署名チェック
    public function getOpinionsWk($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     CHAGE_OPI_SEQ ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $field = "";
        foreach ($model->fieldSize as $key => $val) {
            if (strpos($key, 'RECORD') === false) {
                if (in_array($key, array("FOREIGNLANGACT1", "FOREIGNLANGACT2","FOREIGNLANGACT3"))) {
                    $field .= " CASE WHEN T3.YEAR < '".$model->gaikokugo_change_year."' THEN T3.".$key." END AS ".$key.",";
                } elseif ($key == "FOREIGNLANGACT1_2") {
                    $field .= " CASE WHEN T3.YEAR >= '".$model->gaikokugo_change_year."' THEN T3.FOREIGNLANGACT1 END AS FOREIGNLANGACT1_2,";
                } else {
                    $field .= "T3.".$key.",";
                }
            }
        }

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.ANNUAL, ";
        $query .= "     ".$field." ";
        
        $query .= "     B1.RECORD as RECORD301, ";
        $query .= "     B2.RECORD as RECORD302, ";
        $query .= "     B3.RECORD as RECORD303, ";
        $query .= "     B4.RECORD as RECORD304, ";
        $query .= "     B5.RECORD as RECORD305, ";
        $query .= "     B6.RECORD as RECORD306, ";
        $query .= "     B7.RECORD as RECORD307, ";
        $query .= "     B8.RECORD as RECORD308, ";
        $query .= "     B9.RECORD as RECORD309, ";
        $query .= "     B10.RECORD as RECORD310, ";
        foreach ($model->itemArray as $key => $value) {
            $query .= "     B20_{$key}.RECORD as RECORD4{$key}, ";
        }
        
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_P_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B1 ON B1.YEAR = T1.YEAR ";
        $query .= "                                         AND B1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B1.DIV = '3' ";
        $query .= "                                         AND B1.CODE = '01' ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B2 ON B2.YEAR = T1.YEAR ";
        $query .= "                                         AND B2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B2.DIV = '3' ";
        $query .= "                                         AND B2.CODE = '02' ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B3 ON B3.YEAR = T1.YEAR ";
        $query .= "                                         AND B3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B3.DIV = '3' ";
        $query .= "                                         AND B3.CODE = '03' ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B4 ON B4.YEAR = T1.YEAR ";
        $query .= "                                         AND B4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B4.DIV = '3' ";
        $query .= "                                         AND B4.CODE = '04' ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B5 ON B5.YEAR = T1.YEAR ";
        $query .= "                                         AND B5.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B5.DIV = '3' ";
        $query .= "                                         AND B5.CODE = '05' ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B6 ON B6.YEAR = T1.YEAR ";
        $query .= "                                         AND B6.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B6.DIV = '3' ";
        $query .= "                                         AND B6.CODE = '06' ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B7 ON B7.YEAR = T1.YEAR ";
        $query .= "                                         AND B7.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B7.DIV = '3' ";
        $query .= "                                         AND B7.CODE = '07' ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B8 ON B8.YEAR = T1.YEAR ";
        $query .= "                                         AND B8.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B8.DIV = '3' ";
        $query .= "                                         AND B8.CODE = '08' ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B9 ON B9.YEAR = T1.YEAR ";
        $query .= "                                         AND B9.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B9.DIV = '3' ";
        $query .= "                                         AND B9.CODE = '09' ";
        
        $query .= "     LEFT JOIN BEHAVIOR_DAT B10 ON B10.YEAR = T1.YEAR ";
        $query .= "                                         AND B10.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                         AND B10.DIV = '3' ";
        $query .= "                                         AND B10.CODE = '10' ";
        
        
        foreach ($model->itemArray as $key => $value) {
            $tname = 'B20_'.$key;
            $query .= "     LEFT JOIN BEHAVIOR_DAT {$tname} ON {$tname}.YEAR = T1.YEAR ";
            $query .= "                                         AND {$tname}.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                         AND {$tname}.DIV = '4' ";
            $query .= "                                         AND {$tname}.CODE = '{$key}' ";
        }
        
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        } else {
            if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
                $query .= "     (T1.GRADE, T1.HR_CLASS) IN (SELECT ";
                $query .= "                     H1.GRADE ";
                $query .= "                   , H1.HR_CLASS ";
                $query .= "                  FROM ";
                $query .= "                     SCHREG_REGD_HDAT H1 ";
                $query .= "                  WHERE ";
                $query .= "                     H1.YEAR = T1.YEAR AND ";
                $query .= "                     H1.SEMESTER = T1.SEMESTER AND ";
                $query .= "                     (H1.TR_CD1 = '". STAFFCD ."' OR";
                $query .= "                      H1.TR_CD2 = '". STAFFCD ."' OR";
                $query .= "                      H1.TR_CD3 = '". STAFFCD ."' OR";
                $query .= "                      H1.SUBTR_CD1 = '". STAFFCD ."' OR";
                $query .= "                      H1.SUBTR_CD2 = '". STAFFCD ."' OR";
                $query .= "                      H1.SUBTR_CD3 = '". STAFFCD ."') ";
                $query .= "                ) AND ";
            }
        }
        $query .= "     T1.GRADE IN (SELECT ";
        $query .= "                     G1.GRADE ";
        $query .= "                  FROM ";
        $query .= "                     SCHREG_REGD_GDAT G1 ";
        $query .= "                  WHERE ";
        $query .= "                     T1.YEAR = G1.YEAR AND ";
        $query .= "                     G1.SCHOOL_KIND = 'P' ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
