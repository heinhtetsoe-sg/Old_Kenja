<?php

require_once('for_php7.php');

class knjm240mQuery extends Query
{

    //年度取得
    public function getSubClasyearQuery()
    {
        $query  = " SELECT DISTINCT";
        $query .= "     YEAR ";
        $query .= " FROM  ";
        $query .= "     CHAIR_DAT ";
        $query .= " ORDER BY  ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //科目
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
        $query .= "         YEAR        = '".$model->Year."' AND ";
        $query .= "         SEMESTER    = '1' AND ";
        $query .= "         CHAIRCD NOT LIKE '92%' ";
        $query .= " ), SEM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->Year."' AND ";
        $query .= "         SEMESTER    = '2' ";
        if ($model->control_data["学期数"] == 3) {
            $query .= " ), SEM3(CHAIRCD) AS ( ";
            $query .= "     SELECT ";
            $query .= "         CHAIRCD ";
            $query .= "     FROM ";
            $query .= "         CHAIR_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".$model->Year."' AND ";
            $query .= "         SEMESTER    = '3' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
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
        $query .= "                                  AND T2.YEAR    = '".$model->Year."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                              AND T1.CLASSCD         = T2.CLASSCD ";
            $query .= "                              AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
            $query .= "                              AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
        }
        $query .= "                              AND T1.SUBCLASSCD      = T2.SUBCLASSCD ";
        $query .= "     INNER JOIN V_SUBCLASS_MST T3 ON T3.YEAR    = '".$model->Year."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                          AND T1.CLASSCD         = T3.CLASSCD ";
            $query .= "                          AND T1.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                          AND T1.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= "                              AND T1.SUBCLASSCD      = T3.SUBCLASSCD ";
        if ($model->control_data["学期数"] > 1) {
            $query .= " WHERE ";
            $query .= "     T1.CHAIRCD IN (SELECT ";
            $query .= "                        W1.CHAIRCD ";
            $query .= "                    FROM ";
            if ($model->control_data["学期数"] == 3) {
                $query .= "                        SEM2 W1, ";
                $query .= "                        SEM3 W2 ";
                $query .= "                    WHERE ";
                $query .= "                        W1.CHAIRCD = W2.CHAIRCD ";
            } else {
                $query .= "                        SEM2 W1 ";
            }
            $query .= "                    ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //学籍観点別状況データを取得する
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     REP_SEQ_ALL, ";
        $query .= "     REP_LIMIT, ";
        $query .= "     REP_START_SEQ ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CHAIRCD || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->chaircd."' ";
        } else {
            $query .= "  CHAIRCD || SUBCLASSCD = '".$model->chaircd."' ";
        }
        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " UPDATE ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " SET ";
        $query .= "     REP_SEQ_ALL = ".$model->field["SCHCNT"].", ";
        if ($model->field["CHECKCNT"] == null) {
            $query .= "     REP_LIMIT = null , ";
        } else {
            $query .= "     REP_LIMIT = " .$model->field["CHECKCNT"].", ";
        }
        if ($model->field["REP_START_SEQ"] == null || $model->field["REP_START_SEQ"] == 0) {
            $query .= "     REP_START_SEQ = 1 , ";
        } else {
            $query .= "     REP_START_SEQ = " .$model->field["REP_START_SEQ"].", ";
        }
        $query .= "     REGISTERCD = '" .STAFFCD."', ";
        $query .= "     UPDATED = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CHAIRCD || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->chaircd."' ";
        } else {
            $query .= "     CHAIRCD || SUBCLASSCD = '". $model->chaircd . "' ";
        }

        $db->query($query);
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ";
        $query .= "     CHAIR_CORRES_DAT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $subArray = explode("-", $model->subclasscd);
            $query .= " VALUES('".$model->Year."','".substr($model->chaircd, 0, 7)."','".$subArray[0]."','".$subArray[1]."','".$subArray[2]."','".$subArray[3]."', ";
        } else {
            $query .= " VALUES('".$model->Year."','".substr($model->chaircd, 0, 7)."','".$model->subclasscd."', ";
        }
        $query .=   $model->field["SCHCNT"].", ";
        if ($model->field["CHECKCNT"] == null) {
            $query .= " null, ";
        } else {
            $query .=   $model->field["CHECKCNT"].", ";
        }
        if ($model->field["REP_START_SEQ"] == null || $model->field["REP_START_SEQ"] == 0) {
            $query .= " 1, ";
        } else {
            $query .=   $model->field["REP_START_SEQ"].", ";
        }
        $query .= " null,null, ";
        $query .= " '".STAFFCD."', ";
        $query .= "  SYSDATE())";

        $db->query($query);
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }


    //存在チェック -- CHAIR_CORRES_DAT
    public function checkDataExists($model, $year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '".$year."' ";

        $retCnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $retCnt;
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
        $query .= "         TBNAME  = 'CHAIR_CORRES_DAT' ";
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
        $pre_year = $model->Year - 1;

        $query  = "";
        if ($div == "copy") {
            $query .= " INSERT INTO CHAIR_CORRES_DAT (".implode(",", $table_field).") ";
        }
        $query .= " WITH SEM1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->Year."' AND ";
        $query .= "         SEMESTER    = '1' ";
        $query .= " ), SEM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->Year."' AND ";
        $query .= "         SEMESTER    = '2' ";
        if ($model->control_data["学期数"] == 3) {
            $query .= " ), SEM3 AS ( ";
            $query .= "     SELECT ";
            $query .= "         CHAIRCD ";
            $query .= "     FROM ";
            $query .= "         CHAIR_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".$model->Year."' AND ";
            $query .= "         SEMESTER    = '3' ";
        }
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         PRE_T.* ";
        $query .= "     FROM ";
        $query .= "         CHAIR_CORRES_DAT PRE_T ";
        $query .= "         INNER JOIN SEM1 ";
        $query .= "              ON SEM1.CHAIRCD        = PRE_T.CHAIRCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND SEM1.CLASSCD        = PRE_T.CLASSCD ";
            $query .= "             AND SEM1.SCHOOL_KIND    = PRE_T.SCHOOL_KIND ";
            $query .= "             AND SEM1.CURRICULUM_CD  = PRE_T.CURRICULUM_CD ";
        }
        $query .= "             AND SEM1.SUBCLASSCD     = PRE_T.SUBCLASSCD ";
        $query .= "         INNER JOIN V_SUBCLASS_MST S1 ";
        $query .= "              ON S1.YEAR             = '".$model->Year."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND S1.CLASSCD          = PRE_T.CLASSCD ";
            $query .= "             AND S1.SCHOOL_KIND      = PRE_T.SCHOOL_KIND ";
            $query .= "             AND S1.CURRICULUM_CD    = PRE_T.CURRICULUM_CD ";
        }
        $query .= "             AND S1.SUBCLASSCD       = PRE_T.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         PRE_T.YEAR  = '".$pre_year."' ";
        if ($model->control_data["学期数"] > 1) {
            $query .= "     AND PRE_T.CHAIRCD IN (SELECT ";
            $query .= "                             W1.CHAIRCD ";
            $query .= "                         FROM ";
            $query .= "                             SEM2 W1 ";
            if ($model->control_data["学期数"] == 3) {
                $query .= "                            ,SEM3 W2 ";
                $query .= "                         WHERE ";
                $query .= "                             W1.CHAIRCD = W2.CHAIRCD ";
            }
            $query .= "                         ) ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        if ($div == "copy") {
            $sep = "";
            foreach ($table_field as $field) {
                if ($field == "YEAR") {
                    $query .= $sep."'".$model->Year."'";
                } elseif ($field == "REGISTERCD") {
                    $query .= $sep."'".STAFFCD."'";
                } elseif ($field == "UPDATED") {
                    $query .= $sep."sysdate() ";
                } else {
                    $query .= $sep.$field;
                }
                $sep = " , ";
            }
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     MAIN ";

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
        $data = array();
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data["YEAR"][TEXT]            = $model->Year;
            $data["CHAIRCD"][TEXT]         = $data_arr[$i]["CHAIRCD"];
            if ($model->Properties["useCurriculumcd"] == "1") {
                $subArray = explode("-", $data_arr[$i]["SUBCLASSCD"]);

                $data["CLASSCD"][TEXT]         = $subArray[0];
                $data["SCHOOL_KIND"][TEXT]     = $subArray[1];
                $data["CURRICULUM_CD"][TEXT]   = $subArray[2];
                $data["SUBCLASSCD"][TEXT]      = $subArray[3];
            } else {
                $data["SUBCLASSCD"][TEXT]      = $data_arr[$i]["SUBCLASSCD"];
            }
            $data["REP_SEQ_ALL"][NUMBER]   = $data_arr[$i]["REP_SEQ_ALL"];
            $data["REP_LIMIT"][NUMBER]     = $data_arr[$i]["REP_LIMIT"];
            $data["REP_START_SEQ"][NUMBER] = ($data_arr[$i]["REP_START_SEQ"]) ? $data_arr[$i]["REP_START_SEQ"] : 1;
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            if (0 < $db->getOne(knjm240mQuery::updCheckChairCorresDat($model, $data_arr[$i]["CHAIRCD"], $data_arr[$i]["SUBCLASSCD"]))) {
                $where  = " WHERE ";
                $where .= "     YEAR = '".$model->Year."' ";
                $where .= "     AND CHAIRCD = '".$data_arr[$i]["CHAIRCD"]."' ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "     AND CLASSCD = '".$subArray[0]."' ";
                    $where .= "     AND SCHOOL_KIND = '".$subArray[1]."' ";
                    $where .= "     AND CURRICULUM_CD = '".$subArray[2]."' ";
                    $where .= "     AND SUBCLASSCD = '".$subArray[3]."' ";
                } else {
                    $where .= "     AND SUBCLASSCD = '".$data_arr[$i]["SUBCLASSCD"]."' ";
                }

                $query = Query::updateSQL($data, "CHAIR_CORRES_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "CHAIR_CORRES_DAT");
            }
            $db->query($query);

            $cnt++;
        }

        return $cnt;
    }

    //存在チェック -- CHAIR_DAT
    public function checkChairDat($model, $chaircd, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' ";
        $query .= "     AND CHAIRCD = '".$chaircd."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD  = '".$subclasscd."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$subclasscd."' ";
        }

        return $query;
    }

    //存在チェック -- CHAIR_CORRES_DAT
    public function updCheckChairCorresDat($model, $chaircd, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' ";
        $query .= "     AND CHAIRCD = '".$chaircd."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD  = '".$subclasscd."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$subclasscd."' ";
        }

        return $query;
    }
}
