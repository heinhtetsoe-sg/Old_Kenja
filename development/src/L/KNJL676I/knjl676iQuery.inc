<?php

require_once('for_php7.php');

class knjl676iQuery extends Query
{
    //塾マスタ取得
    public function getPrischoolMst()
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOLCD AS VALUE, ";
        $query .= "     PRISCHOOLCD || ':' || PRISCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //塾マスタより1レコード取得
    public function getPriSchoolRow($priSchoolCd)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";

        return $query;
    }

    //出身学校マスタ取得
    public function getFinshoolMst()
    {
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD AS VALUE, ";
        $query .= "     FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOLCD = '".$cd."' ";

        return $query;
    }

    //一覧取得
    public function getSelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     ADVICE.EXAMNO AS VALUE, ";
        $query .= "     ADVICE.EXAMNO || '：' || ADVICE.NAME || '：' || FIN.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT ADVICE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "            ON FIN.FINSCHOOLCD = ADVICE.FS_CD ";
        $query .= " WHERE ";
        $query .= "     ADVICE.ENTEXAMYEAR = '".$model->entexamyear."' ";
        if ($model->field["FINSCHOOLCD"] != "") {
            $query .= "     AND ADVICE.FS_CD = '".$model->field["FINSCHOOLCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ADVICE.EXAMNO ";

        return $query;
    }

    //塾生取得
    public function getSelectQueryLeft($model)
    {
        $query  = " SELECT ";
        $query .= "     ADVICE.EXAMNO, ";
        $query .= "     ADVICE.NAME, ";
        $query .= "     FIN.FINSCHOOL_NAME ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT ADVICE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "            ON FIN.FINSCHOOLCD = ADVICE.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT D007 ";
        $query .= "             ON D007.ENTEXAMYEAR = ADVICE.ENTEXAMYEAR ";
        $query .= "            AND D007.EXAMNO      = ADVICE.EXAMNO ";
        $query .= "            AND D007.SEQ         = '007' ";
        $query .= " WHERE ";
        $query .= "     ADVICE.ENTEXAMYEAR = '".$model->entexamyear."' ";
        if ($model->field["FINSCHOOLCD"] != "") {
            $query .= "     AND ADVICE.FS_CD = '".$model->field["FINSCHOOLCD"]."' ";
        }
        $query .= "     AND D007.REMARK1 = '".$model->field["PRISCHOOLCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     ADVICE.EXAMNO ";

        return $query;
    }

    //対象外の中学生取得(※選択中の塾コード以外に塾コード登録済みの生徒も中学生一覧の対象外)
    public function getSelectQueryLeft2($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT T1 ";
        $query .= "     INNER JOIN PRISCHOOL_MST L1 ";
        $query .= "             ON L1.PRISCHOOLCD  = T1.REMARK1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND T1.SEQ     = '007' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model, $flg)
    {
        //リストが空の場合
        if ($flg == "left" && !$model->selectLeft) {
            return false;
        } elseif ($flg == "right" && !$model->selectRight) {
            return false;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $selectSchArr = array();

        if ($flg == "left") {
            //左側リスト
            $remark1 = $model->field["PRISCHOOLCD"];
            $selectSchArr = explode(",", $model->selectLeft);
        } elseif ($flg == "right") {
            //右側リスト
            $remark1 = "";
            $selectSchArr = explode(",", $model->selectRight);
        }

        foreach ($selectSchArr as $examno) {
            //存在チェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR  = '".$model->entexamyear."' ";
            $query .= "     AND EXAMNO   = '".$examno."' ";
            $query .= "     AND SEQ      = '007' ";
            $dataCnt = $db->getOne($query);

            $data = array();
            $data["REMARK1"][TEXT]      = $remark1;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR  = '".$model->entexamyear."' ";
                $where .= "   AND EXAMNO   = '".$examno."' ";
                $where .= "   AND SEQ      = '007' ";
                if ($flg == "right") {
                    $where .= "   AND ( ";
                    $where .= "       (REMARK1 = '".$model->field["PRISCHOOLCD"]."' ) ";
                    $where .= "       OR (REMARK1 IS NULL) ";
                    $where .= "       ) ";
                }
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->entexamyear;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = '007';
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
