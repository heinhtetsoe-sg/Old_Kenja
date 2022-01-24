<?php

require_once('for_php7.php');

class knjx060query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX060' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "select distinct t1.year,t1.semester,t2.semestername ";
        $query .= "from chair_dat t1 ";
        $query .= "     inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
        $query .= "order by t1.year,t1.semester";

        return $query;
    }

    //講座一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "select distinct chaircd,chairname ";
        $query .= "from chair_dat ";
        $query .= "where YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "order by chaircd";

        return $query;
    }

    //科目一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBCLASSCD, ";
        }
        $query .= "     T2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
            $query .= "     T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        } else {
            $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD ";
        }
        $query .= " ORDER BY ";
        $query .= "     SUBCLASSCD";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["CHAIRCD"] != "") {
            $query .= "   AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        if ($model->field["SUBCLASSCD"] != "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "   AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //教育課程コード取得
    public function getCurriculumCd()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z018' ";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($model, $data, $chk_no)
    {
        $flg    = true;
        $query  = "";

        if ($model->Properties["useCurriculumcd"] == '1') {
            //条件選択
            switch ($chk_no) {
                case "1":
                    $where  = " SUBCLASS_MST WHERE CLASSCD          = '".$data["CLASSCD"]."' AND ";
                    $where .= "                    SCHOOL_KIND      = '".$data["SCHOOL_KIND"]."' AND ";
                    $where .= "                    CURRICULUM_CD    = '".$data["CURRICULUM_CD"]."' AND ";
                    $where .= "                    SUBCLASSCD       = '".$data["SUBCLASSCD"]."' ";
                    break;
                case "2":
                    $where = "v_electclass_mst where YEAR = '".$data["YEAR"]."' AND GROUPCD = '".$data["GROUPCD"]."' ";
                    break;
                case "3":
                    $where  = " CLASS_MST WHERE CLASSCD         = '".$data["CLASSCD"]."' AND ";
                    $where .= "                 SCHOOL_KIND     = '".$data["SCHOOL_KIND"]."' ";
                    break;
                default:
                    $flg = false;
                    break;
            }
        } else {
            //条件選択
            switch ($chk_no) {
                case "1":
                    $where = "subclass_mst where SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
                    break;
                case "2":
                    $where = "v_electclass_mst where YEAR = '".$data["YEAR"]."' AND GROUPCD = '".$data["GROUPCD"]."' ";
                    break;
                default:
                    $flg = false;
                    break;
            }
        }
        if ($flg) {
            $query = "SELECT 'X' FROM " . $where;
        }

        return $query;
    }


    //マスタ(追加・更新・削除用)
    public function getExistCheck($data)
    {
        $query  = "select GROUPCD from chair_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM chair_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd, $model)
    {
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["CHAIRNAME"][TEXT]           = $data["CHAIRNAME"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data1["CLASSCD"][TEXT]             = $data["CLASSCD"];
            $data1["SCHOOL_KIND"][TEXT]         = $data["SCHOOL_KIND"];
            $data1["CURRICULUM_CD"][TEXT]       = $data["CURRICULUM_CD"];
        }
        $data1["SUBCLASSCD"][TEXT]          = $data["SUBCLASSCD"];
        $data1["GROUPCD"][TEXT]             = $data["GROUPCD"];
        $data1["TAKESEMES"][TEXT]           = $data["TAKESEMES"];
        $data1["LESSONCNT"][NUMBER]         = $data["LESSONCNT"];
        $data1["FRAMECNT"][NUMBER]          = $data["FRAMECNT"];
        $data1["COUNTFLG"][TEXT]            = $data["COUNTFLG"];//NO001

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "chair_dat");

        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd, $model)
    {
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["CHAIRNAME"][TEXT]           = $data["CHAIRNAME"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data1["CLASSCD"][TEXT]             = $data["CLASSCD"];
            $data1["SCHOOL_KIND"][TEXT]         = $data["SCHOOL_KIND"];
            $data1["CURRICULUM_CD"][TEXT]       = $data["CURRICULUM_CD"];
        }
        $data1["SUBCLASSCD"][TEXT]          = $data["SUBCLASSCD"];
        $data1["GROUPCD"][TEXT]             = $data["GROUPCD"];
        $data1["TAKESEMES"][TEXT]           = $data["TAKESEMES"];
        $data1["LESSONCNT"][NUMBER]         = $data["LESSONCNT"];
        $data1["FRAMECNT"][NUMBER]          = $data["FRAMECNT"];
        $data1["COUNTFLG"][TEXT]            = $data["COUNTFLG"];//NO001

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where  = "where YEAR = '".$data["YEAR"]."' ";
        $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

        $query = Query::updateSQL($data1, "chair_dat", $where);

        $result = $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CHAIR_CLS_DATの削除
    public function deleteChairClsDat(&$db, $data)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

        $result = $db->query($query);
    }
}
