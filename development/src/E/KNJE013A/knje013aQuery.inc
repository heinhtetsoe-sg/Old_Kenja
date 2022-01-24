<?php
class knje013aQuery extends Query
{

    //学期取得
    public function getSemesterName()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //クラス取得
    public function getAuthClass($model)
    {
        $query  = "SELECT ";
        $query .= "    GRADE || HR_CLASS AS GRADE_HR_CLASS, ";
        $query .= "    GRADE || HR_CLASS AS VALUE, ";
        $query .= "    HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER ."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    AND (TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "         TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "         TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= "ORDER BY";
        $query .= "    VALUE ";

        return $query;
    }

    //生徒取得
    public function getAuthStudent($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.SCHREGNO AS VALUE, ";
        $query .= "    T1.SCHREGNO || '　' || T1.ATTENDNO ||  '番　' || L1.NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.SEMESTER = '".CTRL_SEMESTER ."' AND ";
        $query .= "    T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"] ."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.ATTENDNO ";

        return $query;
    }

    public function getHtrainremarkDetailData($model, $flg)
    {
        $query  = " WITH SCHREG_TAISHOU AS ( ";
        $query .= " SELECT DISTINCT  ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
        $query .= "           AND REGD.GRADE = GDAT.GRADE ";
        $query .= " WHERE  ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR ."' ";
        $query .= " AND REGD.SEMESTER = '".CTRL_SEMESTER ."'  ";
        if ($model->field["CHOICE"] == "1") {
            $query .= " AND REGD.GRADE || REGD.HR_CLASS IN ('".implode(explode(',', $model->selectdata), "','")."') ";
        } else {
            $query .= " AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }

        $query .= " ) ";
        $query .= " , SCHREG_YEAR AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT REGDG ";
        $query .= "      ON REGD.YEAR = REGDG.YEAR AND REGD.GRADE = REGDG.GRADE ";
        $query .= "     AND REGD.GRADE = REGDG.GRADE ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_TAISHOU SCHKIND ";
        $query .= "      ON REGD.SCHREGNO = SCHKIND.SCHREGNO ";
        $query .= "     AND REGDG.SCHOOL_KIND = SCHKIND.SCHOOL_KIND ";
        $query .= " ) ";

        $query .= " SELECT ";
        if (!$flg) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     M1.SCHREGNO, ";
            $query .= "     T2.YEAR, ";
            for ($seq=$model->fromSeq; $seq<=$model->toSeq; $seq++) {
                $table = "DT".$seq;
                $query .= "     ".$table.".TRAIN_SEQ AS TRAIN_SEQ".$seq.", ";
                $query .= "     ".$table.".REMARK AS REMARK".$seq.", ";
            }
            $query .= "     T2.ANNUAL, ";
            $query .= "     T2.TOTALSTUDYACT, ";
            $query .= "     T2.TOTALSTUDYVAL, ";
            $query .= "     T2.SPECIALACTREMARK, ";
            $query .= "     T2.ATTENDREC_REMARK ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_TAISHOU M1 ";
        $query .= "     INNER JOIN SCHREG_YEAR SCH_YEAR ON M1.SCHREGNO = SCH_YEAR.SCHREGNO ";
        $query .= "     INNER JOIN HTRAINREMARK_DAT T2 ON SCH_YEAR.SCHREGNO = T2.SCHREGNO ";
        $query .= "                                   AND SCH_YEAR.YEAR = T2.YEAR ";
        for ($seq=$model->fromSeq; $seq<=$model->toSeq; $seq++) {
            $table = "DT".$seq;
            $query .= "     LEFT JOIN HTRAINREMARK_TRAINREF_DAT ".$table." ";
            $query .= "             ON ".$table.".SCHREGNO  = SCH_YEAR.SCHREGNO ";
            $query .= "            AND ".$table.".YEAR      = SCH_YEAR.YEAR ";
            $query .= "            AND ".$table.".TRAIN_SEQ = '".$seq."' ";
        }
        $query .= " WHERE ";
        if ($model->field["CHOICE"] != "1") {
            $query .= "    M1.SCHREGNO IN ('".implode(explode(',', $model->selectdata), "','")."') AND ";
        }
        $query .= "    NOT EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_ENT_GRD_HIST_DAT G1 ";
        $query .= "                 WHERE ";
        $query .= "                         G1.SCHREGNO  = M1.SCHREGNO ";
        $query .= "                     AND G1.SCHOOL_KIND  = 'H' ";
        $query .= "                     AND ((G1.ENT_DIV  IN ('4','5') ";
        $query .= "                          AND G1.ENT_DATE  < '".str_replace("/", "-", $model->field["IDOU_DATE"])."' ) ";
        $query .= "                      OR (G1.GRD_DIV  IN ('2','3') ";
        $query .= "                          AND G1.GRD_DATE  < '".str_replace("/", "-", $model->field["IDOU_DATE"])."' )) ";
        $query .= "                 ) ";
        $query .= " AND NOT EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_TRANSFER_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                         S1.SCHREGNO  = M1.SCHREGNO ";
        $query .= "                     AND S1.TRANSFERCD IN ('1','2') ";
        $query .= "                     AND '".str_replace("/", "-", $model->field["IDOU_DATE"])."' BETWEEN S1.TRANSFER_SDATE AND VALUE(S1.TRANSFER_EDATE, '9999-12-31') ";
        $query .= "                 ) ";
        if ($flg) {
            $query .= " ORDER BY ";
            $query .= "     T2.YEAR, ";
            $query .= "     M1.SCHREGNO ";
        }

        return $query;
    }

    //指導上参考となるデータの確認(クラス指定)
    public function getHexamEntremarkCountHrclass($row)
    {
        $query  = " WITH SCHREG_TAISHOU AS ( ";
        $query .= " SELECT  ";
        $query .= "     T1.SCHREGNO  ";
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '".CTRL_YEAR ."' ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER ."'  ";
        $query .= " AND T1.GRADE || T1.HR_CLASS = '".$row["GRADE_HR_CLASS"] ."'  ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_TRAINREF_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_TAISHOU L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.TRAIN_SEQ BETWEEN '101' AND '106' ";
    
        return $query;
    }

    //指導上参考となるデータの確認(生徒指定)
    public function getHexamEntremarkTrainrefCountSchreg($row, $seq, $flg="")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_TRAINREF_DAT T1 ";
        $query .= " WHERE ";
        if ($flg) {
            $query .= "     T1.YEAR = '{$row["YEAR"]}' AND ";
        }
        $query .= "  T1.SCHREGNO = '{$row["SCHREGNO"]}' ";
        if ($seq) {
            $query .= "  AND T1.TRAIN_SEQ = '{$seq}' ";
        } else {
            $query .= "  AND T1.TRAIN_SEQ BETWEEN '101' AND '106' ";
        }
        return $query;
    }

    //指導上参考となるデータの確認(生徒指定)
    public function getHexamEntremarkHCountSchreg($schregNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "  T1.SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    //調査書所見の確認
    public function getHexamEntremarkCountSchreg($row)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '{$row["YEAR"]}' ";
        $query .= "  AND T1.SCHREGNO = '{$row["SCHREGNO"]}' ";

        return $query;
    }

    //取込処理
    public function &getUpdateQuery(&$model)
    {
        //対象コード配列に格納
        $selectArray = explode(",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        if ($selectArray[0] != "") {
            $query = knje013aQuery::getHtrainremarkDetailData($model, "DATA");

            $result = $db->query($query);
            $setHdat = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                //HEXAM_ENTREMARK_HDAT の更新にて使用
                $setHdat[$row["SCHREGNO"]]["TOTALSTUDYACT"] .= $row["TOTALSTUDYACT"];
                $setHdat[$row["SCHREGNO"]]["TOTALSTUDYVAL"] .= $row["TOTALSTUDYVAL"];

                // 更新 -- HEXAM_ENTREMARK_TRAINREF_DAT
                for ($seq=$model->fromSeq; $seq<=$model->toSeq; $seq++) {
                    //対象データのチェック
                    $getCount = $db->getOne(knje013aQuery::getHexamEntremarkTrainrefCountSchreg($row, $seq, "YEAR"));
                    $data = array();
                    $data["REMARK"][TEXT]       = $row["REMARK".$seq];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";
                    if ($getCount == 0) {
                        $data["YEAR"][TEXT]         = $row["YEAR"];
                        $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
                        $data["TRAIN_SEQ"][TEXT]    = $seq;
                        $query = Query::insertSQL($data, "HEXAM_ENTREMARK_TRAINREF_DAT");
                    } else {
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$row["YEAR"]}' ";
                        $where .= " AND SCHREGNO = '{$row["SCHREGNO"]}' ";
                        $where .= " AND TRAIN_SEQ = '{$seq}' ";
                        $query = Query::updateSQL($data, "HEXAM_ENTREMARK_TRAINREF_DAT", $where);
                    }
                    $db->query($query);
                }

                // 更新 -- HEXAM_ENTREMARK_DAT
                //対象データのチェック
                $getCount = $db->getOne(knje013aQuery::getHexamEntremarkCountSchreg($row));
                $data = array();
                $data["SPECIALACTREC"][TEXT] = $row["SPECIALACTREMARK"];
                $data["ATTENDREC_REMARK"][TEXT] = $row["ATTENDREC_REMARK"];
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][NUMBER]     = "sysdate()";
                if ($getCount == 0) {
                    $data["YEAR"][TEXT]         = $row["YEAR"];
                    $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
                    $data["ANNUAL"][TEXT]       = $row["ANNUAL"];
                    $query = Query::insertSQL($data, "HEXAM_ENTREMARK_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR = '{$row["YEAR"]}' ";
                    $where .= " AND SCHREGNO = '{$row["SCHREGNO"]}' ";
                    $query = Query::updateSQL($data, "HEXAM_ENTREMARK_DAT", $where);
                }
                $db->query($query);
            }

            foreach ($setHdat as $schKey => $val) {
                //対象データのチェック
                $getCount = $db->getOne(knje013aQuery::getHexamEntremarkHCountSchreg($schKey));

                $data = array();
                $data["TOTALSTUDYACT"][TEXT]    = $val["TOTALSTUDYACT"];
                $data["TOTALSTUDYVAL"][TEXT]    = $val["TOTALSTUDYVAL"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                if ($getCount == 0) {
                    $data["SCHREGNO"][TEXT]     = $schKey;
                    $query = Query::insertSQL($data, "HEXAM_ENTREMARK_HDAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     SCHREGNO = '{$schKey}' ";
                    $query = Query::updateSQL($data, "HEXAM_ENTREMARK_HDAT", $where);
                }
                $db->query($query);
            }
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
