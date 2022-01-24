<?php

require_once('for_php7.php');

class knjb0110query extends Query {
    //年度一覧
    function getYearList() {
        $nextyear = CTRL_YEAR + 1;

        $query  = " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS VALUE, ";
        $query .= "     '".CTRL_YEAR."' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '".$nextyear."' AS VALUE, ";
        $query .= "     '".$nextyear."' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期一覧取得
    function getSemesterList($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //職名一覧取得
    function getJobmst() {
        $query  = " SELECT ";
        $query .= "     JOBCD AS VALUE, ";
        $query .= "     JOBCD || ':' || JOBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOB_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //職員一覧取得
    function getStaffMst($model) {
        $query  = " SELECT ";
        $query .= "     STAFFCD, ";
        $query .= "     STAFFNAME ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T1 ";
        $query .= "     INNER JOIN CHAIR_STF_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                AND L1.STAFFCD= T1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " AND T1.JOBCD = '".$model->field["JOBCD"]."' ";
        $query .= " AND L1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD ";

        return $query;
    }

    //対象年度に職員が登録されているかを確認
    function checkStaffcd($model, $staffcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " AND T1.STAFFCD = '".$staffcd."' ";
        return $query;
    }
    

    //更新対象の職員の講座コードと担任区分を取得
    function getChairCd($model, $staffcd, $chaircd="") {
        $query  = " SELECT ";
        if (!$chaircd) {
            $query .= "     T1.CHAIRCD, ";
            $query .= "     T1.CHARGEDIV ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND T1.STAFFCD = '".$staffcd."' ";
        if ($chaircd) {
            $query .= " AND T1.CHAIRCD = '".$chaircd."' ";
        }
        if (!$chaircd) {
            $query .= " ORDER BY ";
            $query .= "     T1.CHAIRCD ";
        }
        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();
        $notUpchaircd = "";
        $conma = "";
        for ($i = 0; $i < get_count($model->data["STAFFCD"]); $i++) {
            //更新する職員コードがない場合
            if (!strlen($model->fields["UP_STAFFCD"][$i])) {
                continue;
            }
            $staffcd = $model->data["STAFFCD"][$i];
            //講座コードを取得
            $result = $db->query(knjb0110Query::getChairCd($model, $staffcd));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //既に講座担当職員データがあるかを確認をして登録を行う
                $chackStaffcd = $db->getOne(knjb0110Query::getChairCd($model, $model->fields["UP_STAFFCD"][$i], $row["CHAIRCD"]));
                if ($chackStaffcd == 0) {
                    //更新対象の講座担当職員データを削除
                    $query  = " DELETE FROM ";
                    $query .= "     CHAIR_STF_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR      = '".$model->field["YEAR"]."' AND ";
                    $query .= "     SEMESTER  = '".$model->field["SEMESTER"]."' AND ";
                    $query .= "     CHAIRCD   = '".$row["CHAIRCD"]."' AND ";
                    $query .= "     STAFFCD   = '".$staffcd."' ";
                    $db->query($query);

                    //入力した職員コードで講座担当職員データを作成
                    $data = array();
                    $data["YEAR"][TEXT]          = $model->field["YEAR"];
                    $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
                    $data["CHAIRCD"][TEXT]       = $row["CHAIRCD"];
                    $data["STAFFCD"][TEXT]       = $model->fields["UP_STAFFCD"][$i];
                    $data["CHARGEDIV"][NUMBER]   = $row["CHARGEDIV"];
                    $data["REGISTERCD"][TEXT]    = STAFFCD ;
                    $data["UPDATED"][FUNC]       = "sysdate()";

                    //追加
                    $query = Query::insertSQL($data, "CHAIR_STF_DAT");
                    $db->query($query);
                } else {
                    $notUpchaircd .= $conma.$row["CHAIRCD"];
                    $conma = ",";
                }
            }
        }
        Query::dbCheckIn($db);
        
        return $notUpchaircd;
    }
}
?>
