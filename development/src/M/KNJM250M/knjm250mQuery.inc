<?php

require_once('for_php7.php');

class knjm250mQuery extends Query
{

    //データ読込
    public function readQuery($model)
    {
        $query  = " WITH SEM1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->ObjYear."' AND ";
        $query .= "         SEMESTER    = '1' AND ";
        $query .= "         CHAIRCD NOT LIKE '92%' ";
        $query .= " ), SEM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->ObjYear."' AND ";
        $query .= "         SEMESTER    = '2' ";
        if ($model->control_data["学期数"] == 3) {
            $query .= " ), SEM3 AS ( ";
            $query .= "     SELECT ";
            $query .= "         CHAIRCD ";
            $query .= "     FROM ";
            $query .= "         CHAIR_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".$model->ObjYear."' AND ";
            $query .= "         SEMESTER    = '3' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.REP_SEQ_ALL, ";
        $query .= "     T2.REP_LIMIT, ";
        $query .= "     T2.REP_START_SEQ, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T3.SUBCLASSCD, ";
        }
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SEM1 T1 ";
        $query .= "     LEFT JOIN CHAIR_CORRES_DAT T2 ON T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "                                  AND T2.YEAR    = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                              AND T1.CLASSCD         = T2.CLASSCD ";
            $query .= "                              AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
            $query .= "                              AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            $query .= "                              AND T1.SUBCLASSCD      = T2.SUBCLASSCD ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD      = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                          AND T1.CLASSCD         = T3.CLASSCD ";
            $query .= "                          AND T1.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                          AND T1.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        if ($model->control_data["学期数"] > 1) {
            $query .= " WHERE ";
            $query .= "     T1.CHAIRCD IN ( SELECT ";
            $query .= "                         W1.CHAIRCD ";
            $query .= "                     FROM ";
            if ($model->control_data["学期数"] == 3) {
                $query .= "                         SEM2 W1, ";
                $query .= "                         SEM3 W2 ";
                $query .= "                     WHERE ";
                $query .= "                         W1.CHAIRCD = W2.CHAIRCD ";
            } else {
                $query .= "                         SEM2 W1 ";
            }
            $query .= "                    ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //名称
    public function getName($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //回数取得
    public function getperiod($model, $chrsubcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REP_SEQ_ALL, ";
        $query .= "     REP_START_SEQ ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($chrsubcd, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($chrsubcd, 7)."' ";
        }

        return $query;
    }

    //提出基準データ
    public function getRepdata($model, $subcnt)
    {
        $query  = " SELECT  ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     STANDARD_SEQ, ";
        $query .= "     CHAIRCD, ";
        $query .= "     REPORTDIV, ";
        $query .= "     STANDARD_DATE, ";
        $query .= "     DEADLINE_DATE, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     REP_STANDARDDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     year = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->sub, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($model->sub, 7)."' ";
        }
        $query .= "     AND STANDARD_SEQ = ".$subcnt ;

        return $query;
    }

    //DELETE(更新前処理)
    public function getDeleteQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     REP_STANDARDDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->sub, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($model->sub, 7)."' ";
        }
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //UPDATE(更新処理)
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setdata = array();
        if ($model->Properties["useCurriculumcd"] == "1") {
            $subArray = explode("-", substr($model->sub, 7));
        }
        for ($i = $model->repstartcnt; $i < ($model->repstartcnt + $model->repcntall); $i++) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->ObjYear;
            if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $subArray[0];
                $data["SCHOOL_KIND"][TEXT]      = $subArray[1];
                $data["CURRICULUM_CD"][TEXT]    = $subArray[2];
                $data["SUBCLASSCD"][TEXT]       = $subArray[3];
            } else {
                $data["SUBCLASSCD"][TEXT]       = substr($model->sub, 7, 6);
            }
            $data["STANDARD_SEQ"][NUMBER]   = $i;
            $data["CHAIRCD"][TEXT]          = substr($model->sub, 0, 7);
            $data["REPORTDIV"][TEXT]        = $model->setdata["REPDIV"][$i];
            $data["STANDARD_DATE"][TEXT]    = str_replace("/", "-", $model->setdata["BASEDAY"][$i]);
            $data["DEADLINE_DATE"][TEXT]    = str_replace("/", "-", $model->setdata["ENDDAY"][$i]);
            $data["REMARK"][TEXT]           = $model->setdata["REMARK"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "REP_STANDARDDATE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }


    //存在チェック -- CHAIR_CORRES_DAT
    public function checkDataExists($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     REP_STANDARDDATE_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //フィールド一覧取得
    public function getFieldList()
    {
        $db = Query::dbCheckOut();

        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         COLNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SYSIBM.SYSCOLUMNS ";
        $query .= "     WHERE ";
        $query .= "         TBNAME  = 'REP_STANDARDDATE_DAT' ";
        $query .= "     ORDER BY ";
        $query .= "         COLNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";

        $retArray = $db->getCol($query);
        Query::dbCheckIn($db);
        return $retArray;
    }

    //前年度からコピー
    public function getCopyQuery($model, $table_field, $div)
    {
        $db = Query::dbCheckOut();
        $pre_year = $model->ObjYear - 1;

        $query  = "";
        if ($div == "copy") {
            $query .= " INSERT INTO REP_STANDARDDATE_DAT (".implode(",", $table_field).") ";
        }
        $query .= " SELECT ";
        if ($div == "copy") {
            $sep = "";
            foreach ($table_field as $field) {
                if ($field == "YEAR") {
                    $query .= $sep."'".$model->ObjYear."'";
                } elseif (in_array($field, array("STANDARD_DATE", "RETURN_DATE", "DEADLINE_DATE"))) {
                    $query .= $sep."CASE WHEN T1.".$field." IS NOT NULL THEN T1.".$field." + 1 YEAR END";
                } elseif ($field == "REGISTERCD") {
                    $query .= $sep."'".STAFFCD."'";
                } elseif ($field == "UPDATED") {
                    $query .= $sep."sysdate() ";
                } else {
                    $query .= $sep."T1.".$field;
                }
                $sep = " , ";
            }
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     REP_STANDARDDATE_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_CORRES_DAT T2 ";
        $query .= "          ON T2.YEAR             = '".$model->ObjYear."' ";
        $query .= "         AND T2.CLASSCD          = T1.CLASSCD ";
        $query .= "         AND T2.SCHOOL_KIND      = T1.SCHOOL_KIND ";
        $query .= "         AND T2.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        $query .= "         AND T2.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= "         AND T2.CHAIRCD          = T1.CHAIRCD ";
        $query .= "         AND T1.STANDARD_SEQ    <= (VALUE(T2.REP_START_SEQ,1) + VALUE(T2.REP_SEQ_ALL,0)) ";
        $query .= "     INNER JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR     = '".$model->ObjYear."' ";
        $query .= "         AND N1.NAMECD1  = 'M002' ";
        $query .= "         AND N1.NAMECD2  = T1.REPORTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$pre_year."' AND ";
        $query .= "     T1.REPORTDIV IS NOT NULL AND ";
        $query .= "     T1.STANDARD_DATE IS NOT NULL ";

        $cnt = "";
        if ($div == "copy") {
            $db->autoCommit(false);
            $db->query($query);
            $db->commit();          // トランザクションをコミットする。
        } else {
            $cnt = $db->getOne($query);
        }

        Query::dbCheckIn($db);
        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CSV取込
    public function insertQueryCsv(&$db, $model, $data_arr)
    {

        //削除
        $query  = " DELETE FROM ";
        $query .= "     REP_STANDARDDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->sub, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($model->sub, 7)."' ";
        }
        $db->query($query);

        //登録
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data = array();

            $data["YEAR"][TEXT]            = $model->ObjYear;
            if ($model->Properties["useCurriculumcd"] == "1") {
                $subArray = explode("-", $data_arr[$i]["SUBCLASSCD"]);

                $data["CLASSCD"][TEXT]         = $subArray[0];
                $data["SCHOOL_KIND"][TEXT]     = $subArray[1];
                $data["CURRICULUM_CD"][TEXT]   = $subArray[2];
                $data["SUBCLASSCD"][TEXT]      = $subArray[3];
            } else {
                $data["SUBCLASSCD"][TEXT]      = $data_arr[$i]["SUBCLASSCD"];
            }
            $data["STANDARD_SEQ"][NUMBER]   = $data_arr[$i]["STANDARD_SEQ"];
            $data["CHAIRCD"][TEXT]          = $data_arr[$i]["CHAIRCD"];
            $data["REPORTDIV"][TEXT]        = $data_arr[$i]["REPDIV"];
            $data["STANDARD_DATE"][DATE]    = str_replace("/", "-", $data_arr[$i]["BASEDAY"]);
            $data["DEADLINE_DATE"][DATE]    = str_replace("/", "-", $data_arr[$i]["ENDDAY"]);
            $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "REP_STANDARDDATE_DAT");
            $db->query($query);

            $cnt++;
        }

        return $cnt;
    }

    public function checkReportdiv($model, $reportdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'M002' ";
        if (strlen($reportdiv)) {
            $query .= "     AND NAMECD2 = '".$reportdiv."' ";
        }

        return $query;
    }

    public function getChairname($model)
    {
        $query  = " WITH SEM1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRNAME, ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->ObjYear."' AND ";
        $query .= "         SEMESTER    = '1' AND ";
        $query .= "         CHAIRCD NOT LIKE '92%' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CHAIRCD || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sub."' ";
        } else {
            $query .= "       AND CHAIRCD || SUBCLASSCD = '".$model->sub."' ";
        }
        $query .= " ), SEM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->ObjYear."' AND ";
        $query .= "         SEMESTER    = '2' ";
        if ($model->control_data["学期数"] == 3) {
            $query .= " ), SEM3 AS ( ";
            $query .= "     SELECT ";
            $query .= "         CHAIRCD ";
            $query .= "     FROM ";
            $query .= "         CHAIR_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".$model->ObjYear."' AND ";
            $query .= "         SEMESTER    = '3' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     SEM1 T1 ";
        if ($model->control_data["学期数"] > 1) {
            $query .= " WHERE ";
            $query .= "     T1.CHAIRCD IN ( SELECT ";
            $query .= "                         W1.CHAIRCD ";
            $query .= "                     FROM ";
            if ($model->control_data["学期数"] == 3) {
                $query .= "                         SEM2 W1, ";
                $query .= "                         SEM3 W2 ";
                $query .= "                     WHERE ";
                $query .= "                         W1.CHAIRCD = W2.CHAIRCD ";
            } else {
                $query .= "                         SEM2 W1 ";
            }
            $query .= "                    ) ";
        }

        return $query;
    }
}
