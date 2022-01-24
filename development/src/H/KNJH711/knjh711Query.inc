<?php

require_once('for_php7.php');

class knjh711Query extends Query
{
    //テスト名称取得
    public function getTestName()
    {
        $query  = "   SELECT TESTID   AS VALUE ";
        $query .= "        , TESTNAME AS LABEL ";
        $query .= "     FROM ACADEMICTEST_MST AM ";
        $query .= "    WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY TESTID ";

        return $query;
    }

    //時限取得
    public function getPeriod()
    {
        $query  = "   SELECT NAMECD2 AS VALUE ";
        $query .= "        , ABBV1   AS LABEL ";
        $query .= "     FROM NAME_MST ";
        $query .= "    WHERE NAMECD1 = 'H321' ";
        $query .= " ORDER BY VALUE";

        return $query;
    }

    //科目取得
    public function getSubclass($model)
    {
        $query  = "    SELECT SCH.BUNRIDIV || ',' || SCH.CLASSCD || ',' || SCH.SUBCLASSCD AS VALUE ";
        $query .= "         , SUB.SUBCLASSNAME AS LABEL ";
        $query .= "      FROM ACADEMICTEST_SCH_DAT SCH ";
        $query .= " LEFT JOIN ACADEMICTEST_SUBCLASS_DAT SUB ";
        $query .= "        ON SUB.YEAR       = SCH.YEAR ";
        $query .= "       AND SUB.CLASSCD    = SCH.CLASSCD ";
        $query .= "       AND SUB.SUBCLASSCD = SCH.SUBCLASSCD ";
        $query .= "     WHERE SCH.YEAR     = '".CTRL_YEAR."' ";
        $query .= "       AND SCH.TESTID   = '".$model->field["TESTID"]."' ";
        $query .= "       AND SCH.PERIODID = '".$model->field["PERIODID"]."' ";
        $query .= "       AND SUB.TESTDIV  = '".substr($model->field["TESTID"], 0, 1)."' ";
        $query .= "  ORDER BY VALUE";

        return $query;
    }

    //教室取得
    public function getFacility($model)
    {
        $query  = "    SELECT STF.FACCD        AS VALUE ";
        $query .= "         , FAC.FACILITYNAME AS LABEL ";
        $query .= "      FROM ACADEMICTEST_STF_DAT STF ";
        $query .= " LEFT JOIN FACILITY_MST FAC ";
        $query .= "        ON FAC.FACCD = STF.FACCD ";
        $query .= " LEFT JOIN ACADEMICTEST_FAC_DAT AFD ";
        $query .= "        ON AFD.YEAR  = STF.YEAR ";
        $query .= "       AND AFD.FACCD = FAC.FACCD ";
        $query .= "     WHERE STF.YEAR     = '".CTRL_YEAR."' ";
        $query .= "       AND STF.TESTID   = '".$model->field["TESTID"]."' ";
        $query .= "       AND STF.PERIODID = '".$model->field["PERIODID"]."' ";
        $query .= "  GROUP BY STF.FACCD ";
        $query .= "         , FAC.FACILITYNAME ";
        $query .= "         , AFD.FACCD ";
        //教室に生徒が登録されている場合、その教室を優先的に表示する
        $query .= "  ORDER BY CASE WHEN AFD.FACCD IS NOT NULL THEN 1 ";
        $query .= "                ELSE 2 ";
        $query .= "           END ";
        $query .= "         , VALUE";

        return $query;
    }

    //クラス取得
    //教室に生徒が登録されている場合、その生徒のクラスを優先的に表示する
    //科目が選択されていれば、教室は選択されている
    //科目が選択されていない場合は、教室が選択されていない場合もあるので
    //科目が選択されているかで判断している
    public function getHrClass($model)
    {
        $selectSubclassFlg = $model->field["SUBCLASSCD"] != "" ? true : false;

        $query  = "  WITH SRD AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "          , HR_CLASS ";
        $query .= "       FROM SCHREG_REGD_DAT SRD ";
        $query .= "      WHERE YEAR  = '".CTRL_YEAR."' ";
        $query .= "        AND GRADE = '03' ";
        $query .= "   GROUP BY SCHREGNO ";
        $query .= "          , HR_CLASS ";
        if ($selectSubclassFlg == true) {
            $query .= "  ), AFD AS ( ";
            $query .= "     SELECT SRD.HR_CLASS ";
            $query .= "       FROM ACADEMICTEST_FAC_DAT AFD ";
            $query .= "  LEFT JOIN SRD ";
            $query .= "         ON SRD.SCHREGNO = AFD.SCHREGNO ";
            $query .= "   GROUP BY SRD.HR_CLASS ";
        }
        $query .= "  ), LAST_UPDATE AS ( ";
        $query .= "     SELECT HR_CLASS ";
        $query .= "          , MAX(UPDATED) AS UPDATED ";
        $query .= "       FROM SCHREG_REGD_HDAT ";
        $query .= "      WHERE YEAR  = '".CTRL_YEAR."' ";
        $query .= "        AND GRADE = '03' ";
        $query .= "   GROUP BY HR_CLASS ";
        $query .= "  ) ";
        $query .= "     SELECT SRH.HR_CLASS AS VALUE ";
        $query .= "          , SRH.HR_NAME  AS LABEL ";
        $query .= "       FROM SCHREG_REGD_HDAT SRH ";
        $query .= " INNER JOIN LAST_UPDATE ";
        $query .= "         ON SRH.HR_CLASS = LAST_UPDATE.HR_CLASS ";
        $query .= "        AND SRH.UPDATED  = LAST_UPDATE.UPDATED ";
        if ($selectSubclassFlg == true) {
            $query .= "  LEFT JOIN AFD ";
            $query .= "         ON AFD.HR_CLASS = SRH.HR_CLASS ";
        }
        $query .= "      WHERE SRH.YEAR  = '".CTRL_YEAR."' ";
        $query .= "        AND SRH.GRADE = '03' ";
        $query .= "   GROUP BY SRH.HR_CLASS ";
        $query .= "          , SRH.HR_NAME ";
        if ($selectSubclassFlg == true) {
            $query .= "          , AFD.HR_CLASS ";
        }
        //教室に生徒が登録されている場合、その生徒のクラスを優先的に表示する
        $query .= "   ORDER BY ";
        if ($selectSubclassFlg == true) {
            $query .= "            CASE WHEN AFD.HR_CLASS IS NOT NULL THEN 1 ";
            $query .= "                 ELSE 2 ";
            $query .= "            END, ";
        }
        $query .= "            VALUE";

        return $query;
    }

    //定員取得
    public function getCapacity($model)
    {
        $query  = " SELECT NVL(CAPACITY, 0) AS CAPACITY ";
        $query .= "   FROM FACILITY_MST ";
        $query .= "  WHERE FACCD = '".$model->field["FACCD"]."' ";

        return $query;
    }

    //登録済み生徒取得
    public function getExistData($model)
    {
        //BUNRIDIV,CLASSCD,SUBCLASSCDに分割
        $subClassCd = explode(",", $model->field["SUBCLASSCD"]);

        $query  = " SELECT SCHREGNO ";
        $query .= "   FROM ACADEMICTEST_FAC_DAT ";
        $query .= "  WHERE YEAR       = '".CTRL_YEAR."' ";
        $query .= "    AND TESTID     = '".$model->field["TESTID"]."' ";
        $query .= "    AND PERIODID   = '".$model->field["PERIODID"]."' ";
        $query .= "    AND FACCD      = '".$model->field["FACCD"]."' ";
        $query .= "    AND BUNRIDIV   = '".$subClassCd[0]."' ";
        $query .= "    AND CLASSCD    = '".$subClassCd[1]."' ";
        $query .= "    AND SUBCLASSCD = '".$subClassCd[2]."' ";

        return $query;
    }

    //生徒取得
    public function getStudents($model, $schregnoList, $existData)
    {
        $query  = " WITH SRD AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "          , GRADE ";
        $query .= "          , HR_CLASS ";
        $query .= "          , ATTENDNO ";
        $query .= "       FROM SCHREG_REGD_DAT SRD ";
        $query .= "      WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "        AND GRADE    = '03' ";
        $query .= "        AND HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        $query .= "   GROUP BY SCHREGNO ";
        $query .= "          , GRADE ";
        $query .= "          , HR_CLASS ";
        $query .= "          , ATTENDNO ";
        $query .= " ) ";
        $query .= "     SELECT SAD.SCHREGNO ";
        $query .= "          , SRD.GRADE ";
        $query .= "          , SRD.HR_CLASS ";
        $query .= "          , SRD.ATTENDNO ";
        $query .= "          , SBM.NAME ";
        $query .= "       FROM SCHREG_ACADEMICTEST_DAT SAD ";
        $query .= " INNER JOIN ACADEMICTEST_SUBCLASS_DAT ASD ";
        $query .= "         ON ASD.YEAR       = SAD.YEAR ";
        $query .= "        AND ASD.BUNRIDIV   = SAD.BUNRIDIV ";
        $query .= "        AND ASD.CLASSCD    = SAD.CLASSCD ";
        $query .= "        AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= " INNER JOIN SRD ";
        $query .= "         ON SRD.SCHREGNO   = SAD.SCHREGNO ";
        $query .= "  LEFT JOIN SCHREG_BASE_MST SBM ";
        $query .= "         ON SBM.SCHREGNO   = SAD.SCHREGNO ";
        $query .= "      WHERE SAD.YEAR       = '".CTRL_YEAR."' ";
        $query .= "        AND SAD.BUNRIDIV || ',' || SAD.CLASSCD || ',' || SAD.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= "        AND ASD.TESTDIV = '".substr($model->field["TESTID"], 0, 1)."' ";
        if ($existData === true) {
            $query .= "        AND SAD.SCHREGNO IN (".$schregnoList.") ";
        } else {
            $query .= "        AND SAD.SCHREGNO NOT IN (".$schregnoList.") ";
        }
        $query .= "   ORDER BY SRD.ATTENDNO ";

        return $query;
    }

    //時限、教室、生徒での重複チェック
    public function duplicateCheck($model)
    {
        $schregnos = str_replace(",", "','", $model->selectdata); //学籍番号

        $query  = "";
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= "   FROM ACADEMICTEST_FAC_DAT ";
        $query .= "  WHERE YEAR     = '" . CTRL_YEAR . "' ";
        $query .= "    AND TESTID   = '" .$model->field["TESTID"]. "' ";
        $query .= "    AND PERIODID = '" .$model->field["PERIODID"]. "' ";
        $query .= "    AND FACCD    <> '" .$model->field["FACCD"]."' ";
        $query .= "    AND SCHREGNO IN ( '" .$schregnos. "') ";

        return $query;
    }

    //更新
    public function getUpdateQuery($model)
    {
        //配列に格納
        $array = explode(",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //BUNRIDIV,CLASSCD,SUBCLASSCDに分割
        $subClassCd = explode(",", $model->field["SUBCLASSCD"]);

        $query  = "DELETE FROM ACADEMICTEST_FAC_DAT ";
        $query .= "      WHERE YEAR       = '" .CTRL_YEAR."' ";
        $query .= "        AND TESTID     = '" .$model->field["TESTID"]."' ";
        $query .= "        AND PERIODID   = '" .$model->field["PERIODID"]."' ";
        $query .= "        AND FACCD      = '" .$model->field["FACCD"]."' ";
        $query .= "        AND BUNRIDIV   = '" .$subClassCd[0]."' ";
        $query .= "        AND CLASSCD    = '" .$subClassCd[1]."' ";
        $query .= "        AND SUBCLASSCD = '" .$subClassCd[2]."' ";

        $db->query($query);

        //名称年度データの登録
        if ($array[0] !="") {
            $query  = "INSERT INTO ACADEMICTEST_FAC_DAT";
            $query .= "(YEAR, TESTID, PERIODID, FACCD, BUNRIDIV, CLASSCD, SUBCLASSCD, SCHREGNO, REGISTERCD,UPDATED) ";
            $query .= "VALUES";
            for ($i = 0; $i < get_count($array); $i++) {
                $sp = ",";
                if ($i == get_count($array) - 1) {
                    $sp = "";
                }

                $query .= "('" .CTRL_YEAR."'";                  //年度
                $query .= ", '" .$model->field["TESTID"]."'";   //テストID
                $query .= ", '" .$model->field["PERIODID"]."'"; //時限コード
                $query .= ", '" .$model->field["FACCD"]."'";    //施設コード
                $query .= ", '" .$subClassCd[0]."'";            //文理区分
                $query .= ", '" .$subClassCd[1]."'";            //教科コード
                $query .= ", '" .$subClassCd[2]."'";            //科目コード
                $query .= ", '" .$array[$i]."'";                //学籍番号
                $query .= ", '" .STAFFCD ."'";                  //職員コード
                $query .= ", sysdate())" .$sp;                  //更新日付
            }
            $db->query($query);
        } else {
            $all_light = true;
        }
        $db->commit(); // ﾄﾗﾝｻﾞｸｼｮﾝをコミットする。

        Query::dbCheckIn($db);
    }
}
