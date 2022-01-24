<?php

require_once('for_php7.php');

class knjz173Query extends Query
{

    //左側リスト取得
    public function getList($model, $prm = "")
    {
        $query  = " SELECT ";
        $query .= "   KAIKIN_CD, ";
        $query .= "   KAIKIN_DIV, ";
        $query .= "   KAIKIN_NAME, ";
        $query .= "   REF_YEAR, ";
        $query .= "   PRIORITY ";
        $query .= " FROM ";
        $query .= "   KAIKIN_MST ";
        $query .= " ORDER BY ";
        $query .= "   KAIKIN_CD ";
        return $query;
    }

    //１レコード取得
    public function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     KAIKIN_CD, ";
        $query .= "     KAIKIN_DIV, ";
        $query .= "     KAIKIN_NAME, ";
        $query .= "     REF_YEAR, ";
        $query .= "     KESSEKI_CONDITION, ";
        $query .= "     TIKOKU_CONDITION, ";
        $query .= "     SOUTAI_CONDITION, ";
        $query .= "     KESSEKI_KANSAN, ";
        $query .= "     KEKKA_JISU_CONDITION, ";
        $query .= "     PRIORITY, ";
        $query .= "     KAIKIN_FLG ";
        $query .= " FROM ";
        $query .= "     KAIKIN_MST ";
        $query .= " WHERE ";
        $query .= "     KAIKIN_CD = '{$model->field["KAIKIN_CD"]}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    public function chkUniqPriority($model)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KAIKIN_MST ";
        $query .= " WHERE ";
        $query .= "     KAIKIN_CD       <> '{$model->field["KAIKIN_CD"]}' ";
        $query .= "     AND KAIKIN_DIV      = '{$model->field["KAIKIN_DIV"]}' ";
        $query .= "     AND PRIORITY    = '{$model->field["PRIORITY"]}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    public function getGradeInfo($model)
    {
        $query  = "  SELECT ";
        $query .= "      A023.ABBV2, ";
        $query .= "      GDAT.SCHOOL_KIND, ";
        $query .= "      GDAT.GRADE, ";
        $query .= "      GDAT.GRADE_NAME1 ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_GDAT GDAT ";
        $query .= "      INNER JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ";
        $query .= "            AND GDAT.SCHOOL_KIND = A023.NAME1 ";
        $query .= "  WHERE ";
        $query .= "      GDAT.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode("','", explode(':', $model->selectSchoolKind))."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      A023.ABBV2 DESC, ";
        $query .= "      GDAT.GRADE DESC ";
        return $query;
    }

    public function getTargetGrade($kaikin_cd)
    {
        $query  = "  SELECT ";
        $query .= "    GRADE ";
        $query .= "  FROM ";
        $query .= "    KAIKIN_GRADE_MST ";
        $query .= "  WHERE ";
        $query .= "    KAIKIN_CD = '".$kaikin_cd."' ";
        $query .= "  ORDER BY ";
        $query .= "      GRADE ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $fields = $model->field;
        $data = array();
        $data["KAIKIN_CD"][TEXT]              = $fields["KAIKIN_CD"];
        $data["KAIKIN_NAME"][TEXT]            = $fields["KAIKIN_NAME"];
        $data["KAIKIN_DIV"][TEXT]             = $fields["KAIKIN_DIV"];
        $data["REF_YEAR"][NUMBER]             = $fields["REF_YEAR"];
        $data["KESSEKI_CONDITION"][NUMBER]    = $fields["KESSEKI_CONDITION"];
        $data["TIKOKU_CONDITION"][NUMBER]     = $fields["TIKOKU_CONDITION"];
        $data["SOUTAI_CONDITION"][NUMBER]     = $fields["SOUTAI_CONDITION"];
        $data["KESSEKI_KANSAN"][NUMBER]       = $fields["KESSEKI_KANSAN"];
        $data["KEKKA_JISU_CONDITION"][NUMBER] = $fields["KEKKA_JISU_CONDITION"];
        $data["PRIORITY"][TEXT]               = $fields["PRIORITY"];
        $data["KAIKIN_FLG"][TEXT]             = $fields["KAIKIN_FLG"];

        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";

        $query = Query::insertSQL($data, "KAIKIN_MST");
        $db->query($query);

        knjz173Query::updateKaikinGradeMst($db, $model);

        Query::dbCheckIn($db);
        return true;
    }

    //INSERT
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $fields = $model->field;
        $data = array();
        $data["KAIKIN_NAME"][TEXT]            = $fields["KAIKIN_NAME"];
        $data["KAIKIN_DIV"][TEXT]             = $fields["KAIKIN_DIV"];
        $data["REF_YEAR"][NUMBER]             = $fields["REF_YEAR"];
        $data["KESSEKI_CONDITION"][NUMBER]    = $fields["KESSEKI_CONDITION"];
        $data["TIKOKU_CONDITION"][NUMBER]     = $fields["TIKOKU_CONDITION"];
        $data["SOUTAI_CONDITION"][NUMBER]     = $fields["SOUTAI_CONDITION"];
        $data["KESSEKI_KANSAN"][NUMBER]       = $fields["KESSEKI_KANSAN"];
        $data["KEKKA_JISU_CONDITION"][NUMBER] = $fields["KEKKA_JISU_CONDITION"];
        $data["PRIORITY"][TEXT]               = $fields["PRIORITY"];
        $data["KAIKIN_FLG"][TEXT]             = $fields["KAIKIN_FLG"];

        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";

        $where  = " WHERE ";
        $where .= "  KAIKIN_CD = '{$fields["KAIKIN_CD"]}' ";

        $query = Query::updateSQL($data, "KAIKIN_MST", $where);

        $db->query($query);

        knjz173Query::updateKaikinGradeMst($db, $model);

        Query::dbCheckIn($db);

        return true;
    }

    //DELETE INSERT 対象学年を記録
    public function &updateKaikinGradeMst($db, $model)
    {

        $fields = $model->field;
        //削除
        $query = "  DELETE ";
        $query .= "  FROM ";
        $query .= "    KAIKIN_GRADE_MST ";
        $query .= "  WHERE ";
        $query .= "    KAIKIN_CD = '{$fields["KAIKIN_CD"]}' ";
        $db->query($query);

        //取得対象学年で「指定しない」にチェックした場合、追加・更新の際に全学年をレコードに登録する
        if ($model->field["SET_PREFATTEND_GRADE"] == "2") {
            $this->mrgearry = $db->getCol(knjz173Query::getGradeInfo($model), "GRADE");
        }

        //追加
        foreach ($this->mrgearry as $targetGrade) {
            $data = array();
            $data["KAIKIN_CD"][TEXT]            = $fields["KAIKIN_CD"];
            $data["GRADE"][TEXT]                = $targetGrade;

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "KAIKIN_GRADE_MST");
            $db->query($query);
        }
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $fields = $model->field;
        $query  = " DELETE FROM KAIKIN_MST ";
        $query .= " WHERE ";
        $query .= "  KAIKIN_CD = '{$fields["KAIKIN_CD"]}' ";
        $db->query($query);
        Query::dbCheckIn($db);
        
        return true;
    }
}
