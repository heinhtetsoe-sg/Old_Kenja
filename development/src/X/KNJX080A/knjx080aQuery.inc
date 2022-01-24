<?php

require_once('for_php7.php');
class knjx080aquery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX080A' ";
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
        $query .= "from chair_cls_dat t1 ";
        $query .= "        inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
        $query .= "order by t1.year,t1.semester";

        return $query;
    }

    //講座一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "select distinct t1.chaircd,t2.chairname ";
        $query .= "from chair_cls_dat t1,chair_dat t2 ";
        $query .= "where t1.YEAR || t1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "and t1.year = t2.year ";
        $query .= "and t1.semester = t2.semester ";
        $query .= "and t1.chaircd = t2.chaircd ";
        $query .= "order by t1.chaircd";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = "select distinct t1.trgtgrade,t1.trgtclass,t2.hr_name ";
        $query .= "from chair_cls_dat t1,schreg_regd_hdat t2 ";
        $query .= "where t1.YEAR || t1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "and t1.year = t2.year ";
        $query .= "and t1.semester = t2.semester ";
        $query .= "and t1.trgtgrade = t2.grade ";
        $query .= "and t1.trgtclass = t2.hr_class ";
        $query .= "order by t1.trgtgrade,t1.trgtclass";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = "select ";
        $query .= " T1.YEAR, ";
        $query .= " T1.SEMESTER, ";
        $query .= " T1.CHAIRCD, ";
        $query .= " L1.CHAIRNAME, ";
        $query .= " T1.GROUPCD, ";
        $query .= " L2.GROUPNAME, ";
        $query .= " T1.TRGTGRADE, ";
        $query .= " T1.TRGTCLASS, ";
        $query .= " '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "from ";
        $query .= " chair_cls_dat T1 ";
        $query .= " LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= " LEFT JOIN V_ELECTCLASS_MST L2 ON L2.YEAR = T1.YEAR AND L2.GROUPCD = T1.GROUPCD ";
        $query .= "where T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["CHAIRCD"]!="") {
            $query .= "and T1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        if ($model->field["TRGTCD"]!="") {
            $query .= "and T1.TRGTGRADE || T1.TRGTCLASS = '".$model->field["TRGTCD"]."' ";
        }
        $query .= "order by T1.chaircd,T1.trgtgrade,T1.trgtclass";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($data, $chk_no)
    {
        $flg     = true;
        $query     = "";
        //条件選択
        switch ($chk_no) {
            case "1":
                $where  = "chair_dat where YEAR = '".$data["YEAR"]."' ";
                $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
                $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
                //04/09/28 delete by naka
                //$where .= "and GROUPCD = '".$data["GROUPCD"]."' ";
                break;
            case "2":
                $where  = "schreg_regd_hdat where YEAR = '".$data["YEAR"]."' ";
                $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
                $where .= "and GRADE = '".$data["TRGTGRADE"]."' ";
                $where .= "and HR_CLASS = '".$data["TRGTCLASS"]."' ";
                break;
            //04/09/28 insert by naka
            case "3":
                $where = "v_electclass_mst where GROUPCD = '".$data["GROUPCD"]."' ";
                $where .= "and YEAR = '".$data["YEAR"]."' ";
                break;
            default:
                $flg = false;
                break;
        }
        if ($flg) {
            $query = "SELECT 'X' FROM " . $where;
        }

        return $query;
    }


    //マスタ(追加・更新・削除用)
    public function getExistCheck($data)
    {
        $query  = "select 'X' from chair_cls_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and GROUPCD = '".$data["GROUPCD"]."' ";
        $query .= "and TRGTGRADE = '".$data["TRGTGRADE"]."' ";
        $query .= "and TRGTCLASS = '".$data["TRGTCLASS"]."' ";

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and GROUPCD = '".$data["GROUPCD"]."' ";
        $query .= "and TRGTGRADE = '".$data["TRGTGRADE"]."' ";
        $query .= "and TRGTCLASS = '".$data["TRGTCLASS"]."' ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1["YEAR"][TEXT]                 = $data["YEAR"];
        $data1["SEMESTER"][TEXT]             = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["GROUPCD"][TEXT]             = $data["GROUPCD"];
        $data1["TRGTGRADE"][TEXT]             = $data["TRGTGRADE"];
        $data1["TRGTCLASS"][TEXT]             = $data["TRGTCLASS"];

        $data1["REGISTERCD"][TEXT]             = $registercd;
        $data1["UPDATED"][NUMBER]             = "sysdate()";

        $query = Query::insertSQL($data1, "chair_cls_dat");

        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["YEAR"][TEXT]                 = $data["YEAR"];
        $data1["SEMESTER"][TEXT]             = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["GROUPCD"][TEXT]             = $data["GROUPCD"];
        $data1["TRGTGRADE"][TEXT]             = $data["TRGTGRADE"];
        $data1["TRGTCLASS"][TEXT]             = $data["TRGTCLASS"];

        $data1["REGISTERCD"][TEXT]             = $registercd;
        $data1["UPDATED"][NUMBER]             = "sysdate()";

        $where  = "where YEAR = '".$data["YEAR"]."' ";
        $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $where .= "and GROUPCD = '".$data["GROUPCD"]."' ";
        $where .= "and TRGTGRADE = '".$data["TRGTGRADE"]."' ";
        $where .= "and TRGTCLASS = '".$data["TRGTCLASS"]."' ";

        $query = Query::updateSQL($data1, "chair_cls_dat", $where);

        $result = $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    // 受講クラスの自動生成:select
    public function selectAutoChairCls($year, $semester, $flg)
    {
        $query  = "";

        // HRな講座
        // -- CHAIRCD 単位で処理する
        if ($flg == 0) {
            // -- GROUPCD = '0000'
            // -- 講座にぶら下がる受講クラスがゼロ件であること
            $query .= " WITH GET_CHAIRCD AS ( ";
            $query .= "     select w1.CHAIRCD ";
            $query .= "       from CHAIR_DAT w1 ";
            $query .= "      where w1.YEAR = '$year' ";
            $query .= "        and w1.SEMESTER = '$semester' ";
            $query .= "        and w1.GROUPCD = '0000' ";
            $query .= "        and not exists (select 'X' ";
            $query .= "                          from CHAIR_CLS_DAT w2 ";
            $query .= "                         where w2.YEAR = w1.YEAR ";
            $query .= "                           and w2.SEMESTER = w1.SEMESTER ";
            $query .= "                           and w2.CHAIRCD = w1.CHAIRCD ";
            $query .= "                           and w2.GROUPCD = w1.GROUPCD ) ";
            $query .= "     ) ";
            // -- 名簿を取り込み、受講クラスを算出する
            $query .= " select distinct w1.CHAIRCD, w3.GRADE, w3.HR_CLASS ";
            $query .= "   from CHAIR_STD_DAT w1, ";
            $query .= "        GET_CHAIRCD w2, ";
            $query .= "        SCHREG_REGD_DAT w3 ";
            $query .= "  where w1.YEAR = '$year' ";
            $query .= "    and w1.SEMESTER = '$semester' ";
            $query .= "    and w2.CHAIRCD = w1.CHAIRCD ";
            $query .= "    and w3.SCHREGNO = w1.SCHREGNO ";
            $query .= "    and w3.YEAR = w1.YEAR ";
            $query .= "    and w3.SEMESTER = w1.SEMESTER ";
            $query .= "    and w3.GRADE is not null ";
            $query .= "    and w3.HR_CLASS is not null ";
            $query .= " order by w1.CHAIRCD, w3.GRADE, w3.HR_CLASS ";

        // HRでない講座(群)
        // -- GROUPCD 単位で処理する
        } else {
            // -- GROUPCD <> '0000'
            // -- groupcd に一致する受講クラスがゼロ件であること
            $query .= " WITH GET_CHAIRCD AS ( ";
            $query .= "     select w1.CHAIRCD, w1.GROUPCD ";
            $query .= "       from CHAIR_DAT w1 ";
            $query .= "      where w1.YEAR = '$year' ";
            $query .= "        and w1.SEMESTER = '$semester' ";
            $query .= "        and w1.GROUPCD <> '0000' ";
            $query .= "        and not exists (select 'X' ";
            $query .= "                          from CHAIR_CLS_DAT w2 ";
            $query .= "                         where w2.YEAR = w1.YEAR ";
            $query .= "                           and w2.SEMESTER = w1.SEMESTER ";
            $query .= "                           and w2.CHAIRCD = '0000000' ";
            $query .= "                           and w2.GROUPCD = w1.GROUPCD ) ";
            $query .= "     ) ";
            // -- 上記の全講座の全名簿を取り込み、受講クラスを算出する
            $query .= " select distinct w2.GROUPCD, w3.GRADE, w3.HR_CLASS ";
            $query .= "   from CHAIR_STD_DAT w1, ";
            $query .= "        GET_CHAIRCD w2, ";
            $query .= "        SCHREG_REGD_DAT w3 ";
            $query .= "  where w1.YEAR = '$year' ";
            $query .= "    and w1.SEMESTER = '$semester' ";
            $query .= "    and w2.CHAIRCD = w1.CHAIRCD ";
            $query .= "    and w3.SCHREGNO = w1.SCHREGNO ";
            $query .= "    and w3.YEAR = w1.YEAR ";
            $query .= "    and w3.SEMESTER = w1.SEMESTER ";
            $query .= "    and w3.GRADE is not null ";
            $query .= "    and w3.HR_CLASS is not null ";
            $query .= " order by w2.GROUPCD, w3.GRADE, w3.HR_CLASS ";
        }

        return $query;
    }


    // 受講クラスの自動生成:insert
    public function insertAutoChairCls($data, $year, $semester, $registercd, $flg)
    {
        $auto_data = array();
        $auto_data["YEAR"][TEXT]        = $year;
        $auto_data["SEMESTER"][TEXT]    = $semester;
        $auto_data["CHAIRCD"][TEXT]     = ($flg == 0) ? $data["CHAIRCD"] : "0000000";
        $auto_data["GROUPCD"][TEXT]     = ($flg == 0) ? "0000" : $data["GROUPCD"];
        $auto_data["TRGTGRADE"][TEXT]   = $data["GRADE"];
        $auto_data["TRGTCLASS"][TEXT]   = $data["HR_CLASS"];
        $auto_data["REGISTERCD"][TEXT]  = $registercd;
        $auto_data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($auto_data, "chair_cls_dat");
        return $query;
    }

    public function deleteChairClsDat($year, $semester)
    {
        $query .= " DELETE FROM ";
        $query .= "     chair_cls_dat  ";
        $query .= " where ";
        $query .= "     YEAR = '{$year}'  ";
        $query .= "     and SEMESTER = '{$semester}'  ";
        $query .= "     and CHAIRCD IN (  ";
        $query .= "         SELECT  ";
        $query .= "         CHAIRCD  ";
        $query .= "         FROM chair_dat  ";
        $query .= "         where YEAR = '{$year}'  ";
        $query .= "         and SEMESTER = '{$semester}'  ";
        $query .= "         and GROUPCD <> '0000' ";
        $query .= "     ) ";

        return $query;
    }
    public function getGunCheck($data)
    {
        $query .= " SELECT ";
        $query .= "     GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$data['YEAR']}' ";
        $query .= "     AND SEMESTER='{$data['SEMESTER']}' ";
        $query .= "     AND CHAIRCD ='{$data['CHAIRCD']}' ";

        return $query;
    }
}