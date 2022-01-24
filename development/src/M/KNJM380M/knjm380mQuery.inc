<?php

require_once('for_php7.php');

class knjm380mQuery extends Query
{

    //学籍観点別状況データを取得する
    public function selectQuery($model, $flg)
    {
        $query  = "SELECT ";
        $query .= "  SCH_SEQ_ALL, ";
        $query .= "  SCH_SEQ_MIN ";
        $query .= "FROM ";
        $query .= "  CHAIR_CORRES_DAT ";
        $query .= "WHERE ";
        $query .= "  YEAR = '".$model->Year."' AND ";
        if ($flg == '') {
            $query .= "  CHAIRCD = '".$model->chaircd."' ";
        } else {
            $query .= "  CHAIRCD = '".$flg."' ";
        }
        return $query;
    }
    //年
    public function getSubClasyearQuery()
    {
        $query  = " SELECT DISTINCT YEAR ";
        $query .= " FROM CHAIR_DAT ";

        return $query;
    }

    //科目
    public function readQuery($model)
    {
        $query  = " with sem1(CHAIRCD, ";
        $query .= "           CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           CLASSCD, ";
            $query .= "           SCHOOL_KIND, ";
            $query .= "           CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD ";
        $query .= "          ) as ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD, ";
        $query .= "     CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' AND ";
        $query .= "     SEMESTER = '1' ";
        $query .= " ), ";
        $query .= " sem2(CHAIRCD) as (SELECT ";
        $query .= "                         CHAIRCD ";
        $query .= "                     FROM ";
        $query .= "                         CHAIR_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".$model->Year."' AND ";
        $query .= "                         SEMESTER = '2' ";
        if ($model->control_data["学期数"] == 3) {
            $query .= "                     ), ";
            $query .= " sem3(CHAIRCD) as (SELECT ";
            $query .= "                         CHAIRCD ";
            $query .= "                     FROM ";
            $query .= "                         CHAIR_DAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".$model->Year."' AND ";
            $query .= "                         SEMESTER = '3' ";
        }
        $query .= "                     ) ";
        $query .= " SELECT ";
        $query .= "     t1.CHAIRCD, ";
        $query .= "     t1.CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD, ";
            $query .= "     t1.SCHOOL_KIND, ";
            $query .= "     t1.CURRICULUM_CD, ";
        }
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t2.SCH_SEQ_ALL, ";
        $query .= "     t2.SCH_SEQ_MIN ";
        $query .= " FROM ";
        $query .= "     sem1 t1 left join CHAIR_CORRES_DAT t2 on t1.CHAIRCD = t2.CHAIRCD AND t2.year = '".$model->Year."' ";
        if ($model->control_data["学期数"] > 1) {
            $query .= " WHERE ";
            $query .= "     t1.CHAIRCD in (SELECT ";
            $query .= "                        w1.CHAIRCD ";
            $query .= "                    FROM ";
            if ($model->control_data["学期数"] == 3) {
                $query .= "                        sem2 w1,sem3 w2 ";
                $query .= "                    WHERE ";
                $query .= "                        w1.CHAIRCD = w2.CHAIRCD ";
            } else {
                $query .= "                        sem2 w1 ";
            }
            $query .= "                    ) ";
        }
        $query .= " ORDER BY t1.CHAIRCD ";

        return $query;
    }
    /**
     * UPDATE
     */
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "UPDATE ";
        $query .= "  CHAIR_CORRES_DAT ";
        $query .= "SET ";
        $query .= "  SCH_SEQ_ALL = " .$model->field["SCHCNT"].", ";
        if ($model->field["CHECKCNT"] == null) {
            $query .= "  SCH_SEQ_MIN = null , ";
        } else {
            $query .= "  SCH_SEQ_MIN = " .$model->field["CHECKCNT"].", ";
        }
        $query .= "  REGISTERCD = '" .STAFFCD."', ";
        $query .= "  UPDATED = SYSDATE() ";
        $query .= "WHERE ";
        $query .= "  YEAR     = '".$model->Year."' AND ";
        $query .= "  CHAIRCD = '". $model->chaircd . "' ";
        $query .= " ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    /**
     * INSERT
     */
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "INSERT INTO ";
        $query .= "  CHAIR_CORRES_DAT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " VALUES('".$model->Year."','".$model->chaircd."','".$model->classcd."','".$model->schoolKind."','".$model->curriculumcd."','".$model->subclasscd."', ";
        } else {
            $query .= " VALUES('".$model->Year."','".$model->chaircd."','".$model->subclasscd."', ";
        }
        $query .= " null,null,null, ";
        $query .=   $model->field["SCHCNT"].", ";
        if ($model->field["CHECKCNT"] == null) {
            $query .= " null, ";
        } else {
            $query .=   $model->field["CHECKCNT"].", ";
        }
        $query .= " '".STAFFCD."', ";
        $query .= "  SYSDATE())";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
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


    /***** CSV *****/
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
            $subclasscd = $db->getOne(knjm380mQuery::getSubclasscd($model, $data_arr[$i]["CHAIRCD"]));

            $data["YEAR"][TEXT]            = $model->Year;
            $data["CHAIRCD"][TEXT]         = $data_arr[$i]["CHAIRCD"];
            if ($model->Properties["useCurriculumcd"] == "1") {
                $subArray = explode("-", $subclasscd);

                $data["CLASSCD"][TEXT]         = $subArray[0];
                $data["SCHOOL_KIND"][TEXT]     = $subArray[1];
                $data["CURRICULUM_CD"][TEXT]   = $subArray[2];
                $data["SUBCLASSCD"][TEXT]      = $subArray[3];
            } else {
                $data["SUBCLASSCD"][TEXT]      = $subclasscd;
            }
            $data["SCH_SEQ_ALL"][NUMBER]   = $data_arr[$i]["SCH_SEQ_ALL"];
            $data["SCH_SEQ_MIN"][NUMBER]   = $data_arr[$i]["SCH_SEQ_MIN"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            if (0 < $db->getOne(knjm380mQuery::updCheckChairCorresDat($model, $data_arr[$i]["CHAIRCD"], $subclasscd))) {
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
    public function checkChairDat($model, $chaircd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND CHAIRCD = '".$chaircd."' ";

        return $query;
    }

    //講座コード取得
    public function getSubclasscd($model, $chaircd)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
        } else {
            $query .= "     SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND CHAIRCD = '".$chaircd."' ";

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
