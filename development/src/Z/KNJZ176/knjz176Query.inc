<?php

require_once('for_php7.php');


class knjz176Query extends Query {

    //学期を取得
    function getSemester()
    {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT ";
            $query .= "    GRADE || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR='".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER='".$model->semester."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT ";
            $query .= "    GRADE || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR='".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER='".$model->semester."' ";
            $query .= "    AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getStudent($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     value(L2.COUNTFLG,'1') as COUNTFLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DETAIL L2 ";
        $query .= "            ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "           AND L2.YEAR = T1.YEAR ";
        $query .= "           AND L2.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->hrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }

    //存在
    function getDetail($schno, $semester)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= "   FROM SCHREG_REGD_DETAIL";
        $query .= "  WHERE SCHREGNO = '".$schno."'"; 
        $query .= "    AND YEAR     = '".CTRL_YEAR."'";
        $query .= "    AND SEMESTER = '$semester'";
        return $query;
    }

    //更新
    function getUpdateDetail($schno, $countFlg, $semester)
    {
        $query  = " UPDATE SCHREG_REGD_DETAIL ";
        $query .= "   SET ";
        $query .= "      COUNTFLG   = '$countFlg', ";
        $query .= "      REGISTERCD = '".STAFFCD."', ";
        $query .= "      UPDATED    = SYSDATE() "; 
        $query .= " WHERE SCHREGNO = '$schno'"; 
        $query .= "   AND YEAR     = '".CTRL_YEAR."'";
        $query .= "   AND SEMESTER = '$semester'";
        return $query;
    }

    //追加
    function getInsertDetail($schno, $countFlg, $semester)
    {
        $query  = " INSERT INTO SCHREG_REGD_DETAIL ";
        $query .= " VALUES( ";
        $query .= "      '$schno', ";
        $query .= "      '".CTRL_YEAR."', ";
        $query .= "      '$semester', ";
        $query .= "      '$countFlg', ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() "; 
        $query .= "     ) ";
        return $query;
    }

    //実行
    function &getUpdateQuery($model)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //生徒一覧(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($array as $schno) {
            $exists = $db->getOne(knjz176Query::getDetail($schno, $model->semester));
            $countFlg = "0";
            //更新
            if ($exists) {
                $query = knjz176Query::getUpdateDetail($schno, $countFlg, $model->semester);
            //追加
            } else {
                $query = knjz176Query::getInsertDetail($schno, $countFlg, $model->semester);
            }
            $db->query($query);
        }

        foreach ($array2 as $schno) {
            $exists = $db->getOne(knjz176Query::getDetail($schno, $model->semester));
            $countFlg = "1";
            //更新
            if ($exists) {
                $query = knjz176Query::getUpdateDetail($schno, $countFlg, $model->semester);
            //追加
            } else {
                $query = knjz176Query::getInsertDetail($schno, $countFlg, $model->semester);
            }
            $db->query($query);
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }

    //コピー共通ＳＱＬ
    function getCopyCommon($model, $mode)
    {
        $query  = "";
        $query .= " WITH T_HRCLASS AS ( ";
        $query .= knjz176Query::getAuth($model);
        $query .= " ) ";
        $query .= " SELECT ";
        if ($mode == "copy") {
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     L2.COUNTFLG, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     SYSDATE() ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DETAIL L2 ";
        $query .= "             ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "            AND L2.YEAR = T1.YEAR ";
        if ($mode == "copy") {
            $preSemester = (int) $model->semester - 1; //前学期
            $query .= "            AND L2.SEMESTER = '".$preSemester."' "; //前学期
        } else {
            $query .= "            AND L2.SEMESTER = T1.SEMESTER ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN (SELECT VALUE FROM T_HRCLASS) ";
        return $query;
    }

    //コピー前チェック
    function getDetailExists($model)
    {
        $query  = "";
        $query .= knjz176Query::getCopyCommon($model, "exists");

        $db = Query::dbCheckOut();
        $dataCnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $dataCnt;
    }

    //コピー
    function getCopyQuery($model)
    {
        $query  = " INSERT INTO SCHREG_REGD_DETAIL ";
        $query .= knjz176Query::getCopyCommon($model, "copy");

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
