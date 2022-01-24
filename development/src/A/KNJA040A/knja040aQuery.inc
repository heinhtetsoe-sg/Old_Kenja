<?php

require_once('for_php7.php');

class knja040aQuery extends Query
{

    //動作条件チェック
    public function checktoStart()
    {
        return "SELECT COUNT(*) FROM V_COURSE_MAJOR_MST";
    }

    public function getCsvTableData()
    {
        $query  = " WITH MAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     COLNO, ";
        $query .= "     NAME, ";
        $query .= "     LENGTH ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBCREATOR = 'DB2INST1' ";
        $query .= " AND TBNAME = 'NYUGAKU_LIST_DAT' ";
        $query .= " AND NAME NOT IN ('NEN', 'KUMI', 'BAN', 'STUDENT_FULLKANA') ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     NAME, ";
        $query .= "     LENGTH ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";
        return $query;
    }

    public function getSeireki()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L007' ";
        $query .= "     AND NAMECD2 = '4' ";    //とりあえず平成固定

        return $query;
    }

    public function insertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = "DELETE FROM freshman_dat";
        $query .= " WHERE coursecd  = '".$model->course."'";
        $query .= "   AND majorcd   = '".$model->major."'";
        $query .= "   AND enteryear = '".$model->new_year."'";
        $db->query($query);

        //INSERT
        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            $data["ENTERYEAR"][TEXT] = $model->data_arr[$i]["ENTERYEAR"];
            $data["SCHREGNO"][TEXT]  = $model->data_arr[$i]["SCHREGNO"];
            $data["ENT_DIV"][TEXT]   = (0 != $model->data_arr[$i]["ENT_DIV"]) ? $model->data_arr[$i]["ENT_DIV"] : "2";
            $data["HR_CLASS"][TEXT]  = $model->data_arr[$i]["HR_CLASS"];
            $data["ATTENDNO"][TEXT]  = $model->data_arr[$i]["ATTENDNO"];
            $data["INOUTCD"][TEXT]   = $model->data_arr[$i]["INOUTCD"];
            $data["COURSECD"][TEXT]  = $model->data_arr[$i]["COURSECD"];
            $data["MAJORCD"][TEXT]   = sprintf("%03d", $model->data_arr[$i]["MAJORCD"]);
            $data["COURSECODE"][TEXT]= sprintf("%04d", $model->data_arr[$i]["COURSECODE"]);  //NO001
            $data["NAME"][TEXT]      = $model->data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT] = $model->data_arr[$i]["NAME_KANA"];

            if (strlen($model->data_arr[$i]["BIRTHDAY"])>0) {
                $data["BIRTHDAY"][FUNC] = "date('".str_replace("/", "-", $model->data_arr[$i]["BIRTHDAY"])."')";
            } else {
                $data["BIRTHDAY"][FUNC] = "";
            }

            $data["SEX"][TEXT]  = $model->data_arr[$i]["SEX"];
            $data["FINSCHOOLCD"][TEXT] = $model->data_arr[$i]["FINSCHOOLCD"];

            if (strlen($model->data_arr[$i]["FINSCHOOLGRADDATE"])>0) {
                $data["FINSCHOOLGRADDATE"][FUNC] = "date('".str_replace("/", "-", $model->data_arr[$i]["FINSCHOOLGRADDATE"])."')";
            } else {
                $data["FINSCHOOLGRADDATE"][FUNC] = "CAST(NULL as date)";
            }

            $data["ADDR_FLG"][TEXT]       = $model->field["GUARD_ADDR_FLG"] ? $model->field["GUARD_ADDR_FLG"] : null;
            $data["ISSUEDATE"][TEXT]      = str_replace("/", "-", $model->field["GUARD_ISSUEDATE"]);
            $data["EXPIREDATE"][TEXT]     = str_replace("/", "-", $model->field["GUARD_EXPIREDATE"]);
            $data["ZIPCD"][TEXT]          = $model->data_arr[$i]["ZIPCD"];
            $data["ADDR1"][TEXT]          = $model->data_arr[$i]["ADDR1"];
            $data["ADDR2"][TEXT]          = $model->data_arr[$i]["ADDR2"];
            $data["TELNO"][TEXT]          = $model->data_arr[$i]["TELNO"];
            $data["FAXNO"][TEXT]          = $model->data_arr[$i]["FAXNO"];
            $data["EMAIL"][TEXT]          = $model->data_arr[$i]["EMAIL"];
            $data["EMERGENCYCALL"][TEXT]  = $model->data_arr[$i]["EMERGENCYCALL"];
            $data["EMERGENCYTELNO"][TEXT] = $model->data_arr[$i]["EMERGENCYTELNO"];
            $data["CURRICULUM_YEAR"][TEXT] = $model->data_arr[$i]["CURRICULUM_YEAR"];
            $data["SCALASHIPDIV"][TEXT]   = ($model->data_arr[$i]["SCALASHIPDIV"]) ? sprintf("%02d", $model->data_arr[$i]["SCALASHIPDIV"]) : ""; //NO001
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "SYSDATE()";

            $query = Query::insertSQL($data, "freshman_dat");
            $db->query($query);
        }

        //コントロールの更新
        $query  = "UPDATE control_mst";
        $query .= "   SET ctrl_date = DATE(SYSDATE())";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //処理年度以前の在籍データに同じ学籍番号があるかチェックする用
    public function schregnoExist($model, $schregno, $db)
    {
        //SCHREG_BASE_MSTをチェック
        $query  = "SELECT SCHREGNO FROM SCHREG_BASE_MST";
        $query .= " WHERE SCHREGNO = '".$schregno."'";
        $ret_val1 = $db->getOne($query);

        if (isset($ret_val1)) {
            return true;
        }
    }

    //名称マスタの入学区分の取得
    public function getNameMst($db, $year, $cd1, $cd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd1."' ";
        $query .= "     AND NAMECD2 = '".$cd2."' ";

        $rtnOne = $db->getOne($query);
        return $rtnOne;
    }

    public function getCourseMajor($year)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || '-' || MAJORCD AS VALUE, ";
        $query .= "     COURSECD || MAJORCD || '　' || COURSENAME || '　' || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."'";
        $query .= " ORDER BY ";
        $query .= "     COURSECD, MAJORCD";

        return $query;
    }

    public function getCourseCode($model, $coursecode)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    V_COURSECODE_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->new_year."' ";
        $query .= "    AND COURSECODE = '".$coursecode."' ";

        return $query;
    }

    public function getCourseCd($model, $coursecd)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    V_COURSE_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->new_year."' ";
        $query .= "    AND COURSECD = '".$coursecd."' ";

        return $query;
    }

    public function getMajorCd($model, $majorcd)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    V_MAJOR_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->new_year."' ";
        $query .= "    AND MAJORCD = '".$majorcd."' ";

        return $query;
    }

    public function getMaxUpdate($db)
    {
        $query = "SELECT DATE(MAX(UPDATED)),TIME(MAX(UPDATED)) FROM FRESHMAN_DAT";
        $tmp   = $db->getRow($query);

        $date1 = ($tmp[0]=="") ? null : strftime("%Y年%m月%d日 %H時%M分", strtotime($tmp[0]." ".$tmp[1]));
        return $date1;
    }

    // クラス編成データへの追加
    public function insertClassFormationDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象データを削除
        $query  = "DELETE FROM CLASS_FORMATION_DAT";
        $query .= " WHERE YEAR      = '".$model->new_year."'";
        $query .= "   AND SEMESTER  = '1'";
        $query .= "   AND GRADE     = '01'";
        $query .= "   AND COURSECD  = '".$model->course."'";
        $query .= "   AND MAJORCD   = '".$model->major."'";
        $query .= "   AND REMAINGRADE_FLG != '1'";
        $db->query($query);

        // クラス編成データへの追加
        $query  = "INSERT INTO class_formation_dat";
        $query .= " SELECT schregno";                             //SCHREGNO
        $query .= "       ,'".$model->new_year."'";               //YEAR
        $query .= "       ,'1'";                                  //SEMESTER
        $query .= "       ,'01'";                                 //GRADE
        $query .= "       ,HR_CLASS";                             //HR_CLASS
        $query .= "       ,ATTENDNO";                             //ATTENDNO
        $query .= "       ,COURSECD";                             //COURSECD
        $query .= "       ,MAJORCD";                              //MAJORCD
        $query .= "       ,COURSECODE";                           //COURSECODE NO001
        $query .= "       ,'0'";                                  //REMAINGRADE_FLG
        $query .= "       ,cast(NULL as varchar(8))";             //OLD_SCHREGNO NO002
        $query .= "       ,cast(NULL as varchar(2))";             //OLD_GRADE
        $query .= "       ,cast(NULL as varchar(2))";             //OLD_HR_CLASS
        $query .= "       ,cast(NULL as varchar(3))";             //OLD_ATTENDNO
        $query .= "       ,cast(NULL as decimal(4,1))";           //SCORE NO002
        $query .= "       ,'".STAFFCD."'";                        //REGISTERCD
        $query .= "       ,SYSDATE()";
        $query .= " FROM freshman_dat";
        $query .= " WHERE enteryear = '".$model->new_year."'";
        $query .= "   AND coursecd  = '".$model->course."'";
        $query .= "   AND majorcd   = '".$model->major."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //MAX学期名を抽出
    public function getMaxSemester($model)
    {
        $db    = Query::dbCheckOut();
        $query  = "SELECT SEMESTERNAME FROM SEMESTER_MST ";
        $query .= "WHERE YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER = '".$model->max_semester."'";
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);
        return $row[0];
    }

    //エラーデータの削除
    public function deleteQueryNyugakuList()
    {
        $query  = "DELETE FROM NYUGAKU_LIST_DAT WHERE ENTERYEAR = '".(CTRL_YEAR + 1)."' ";
        return $query;
    }

    //エラーデータの削除
    public function insertNyugakuList($dataList)
    {

        foreach ($dataList as $key => $val) {
            $data[$key][TEXT] = $val;
        }
        $query = Query::insertSQL($data, "NYUGAKU_LIST_DAT");
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data = array();
            //データセット
            $data["SCHREGNO"][TEXT]                = $data_arr[$i]["SCHREGNO"];
            $data["RELATIONSHIP"][TEXT]            = $data_arr[$i]["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]              = $data_arr[$i]["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]              = $data_arr[$i]["GUARD_KANA"];
            $data["GUARD_SEX"][TEXT]               = $data_arr[$i]["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]          = str_replace("/", "-", $data_arr[$i]["GUARD_BIRTHDAY"]);
            $data["GUARD_ZIPCD"][TEXT]             = $data_arr[$i]["GUARD_ZIPCD"];
            $data["GUARD_ADDR1"][TEXT]             = $data_arr[$i]["GUARD_ADDR1"];
            $data["GUARD_ADDR2"][TEXT]             = $data_arr[$i]["GUARD_ADDR2"];
            $data["GUARD_TELNO"][TEXT]             = $data_arr[$i]["GUARD_TELNO"];
            $data["GUARD_FAXNO"][TEXT]             = $data_arr[$i]["GUARD_FAXNO"];
            $data["GUARD_E_MAIL"][TEXT]            = $data_arr[$i]["GUARD_E_MAIL"];
            $data["GUARD_JOBCD"][TEXT]             = $data_arr[$i]["GUARD_JOBCD"];
            $data["GUARD_WORK_NAME"][TEXT]         = $data_arr[$i]["GUARD_WORK_NAME"];
            $data["GUARD_WORK_TELNO"][TEXT]        = $data_arr[$i]["GUARD_WORK_TELNO"];
            $data["GUARANTOR_RELATIONSHIP"][TEXT]  = $data_arr[$i]["GUARANTOR_RELATIONSHIP"];
            $data["GUARANTOR_NAME"][TEXT]          = $data_arr[$i]["GUARANTOR_NAME"];
            $data["GUARANTOR_KANA"][TEXT]          = $data_arr[$i]["GUARANTOR_KANA"];
            $data["GUARANTOR_SEX"][TEXT]           = $data_arr[$i]["GUARANTOR_SEX"];
            $data["GUARANTOR_ZIPCD"][TEXT]         = $data_arr[$i]["GUARANTOR_ZIPCD"];
            $data["GUARANTOR_ADDR1"][TEXT]         = $data_arr[$i]["GUARANTOR_ADDR1"];
            $data["GUARANTOR_ADDR2"][TEXT]         = $data_arr[$i]["GUARANTOR_ADDR2"];
            $data["GUARANTOR_TELNO"][TEXT]         = $data_arr[$i]["GUARANTOR_TELNO"];
            $data["GUARANTOR_JOBCD"][TEXT]         = $data_arr[$i]["GUARANTOR_JOBCD"];
            $data["PUBLIC_OFFICE"][TEXT]           = $data_arr[$i]["PUBLIC_OFFICE"];
            $data["REGISTERCD"][TEXT]              = STAFFCD;
            $data["UPDATED"][NUMBER]               = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM GUARDIAN_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "GUARDIAN_DAT");
            } else {
                $where  = "WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
            }
            $db->query($query);

            $query = "DELETE FROM GUARDIAN_ADDRESS_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]       = $data_arr[$i]["SCHREGNO"];
            $data["ISSUEDATE"][TEXT]      = str_replace("/", "-", $model->field["GUARD_ISSUEDATE"]);
            $data["EXPIREDATE"][TEXT]     = str_replace("/", "-", $model->field["GUARD_EXPIREDATE"]);
            $data["GUARD_ZIPCD"][TEXT]    = $data_arr[$i]["GUARD_ZIPCD"];
            $data["GUARD_ADDR1"][TEXT]    = $data_arr[$i]["GUARD_ADDR1"];
            $data["GUARD_ADDR2"][TEXT]    = $data_arr[$i]["GUARD_ADDR2"];
            $data["GUARD_ADDR_FLG"][TEXT] = $model->field["GUARD_ADDR_FLG"] ? $model->field["GUARD_ADDR_FLG"] : null;
            $data["GUARD_TELNO"][TEXT]    = $data_arr[$i]["GUARD_TELNO"];
            $data["GUARD_FAXNO"][TEXT]    = $data_arr[$i]["GUARD_FAXNO"];
            $data["GUARD_E_MAIL"][TEXT]   = $data_arr[$i]["GUARD_E_MAIL"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "SYSDATE()";

            $query = Query::insertSQL($data, "GUARDIAN_ADDRESS_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM GUARDIAN_DAT ";
            $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            $query  = " DELETE FROM GUARDIAN_ADDRESS_DAT ";
            $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //名称マスタ存在チェックSQL
    public function checkMstData($namecd1, $namecd2)
    {
        $query  = "SELECT COUNT(*)";
        $query .= " FROM NAME_MST";
        $query .= " WHERE NAMECD1 = '".$namecd1."' ";
        $query .= "       AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //名称マスタ学校取得
    public function checkSchool()
    {
        $query  = "SELECT NAME1, NAMESPARE2 ";
        $query .= " FROM NAME_MST";
        $query .= " WHERE NAMECD1 = 'Z010' ";
        $query .= "       AND NAMECD2 = '00' ";

        return $query;
    }

    //追加（法政用）
    public function insertQueryHousei($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //ループ
        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            //freshman_dat(DELETE)
            $query  = "DELETE FROM freshman_dat";
            $query .= " WHERE enteryear = '".$model->new_year."'";
            $query .= "   AND schregno  = '".$model->data_arr[$i]["SCHREGNO"]."'";
            $db->query($query);
            //freshman_dat(INSERT)
            if ($model->field["SHORI_MEI"] == "1") {
                $data["ENTERYEAR"][TEXT] = $model->data_arr[$i]["ENTERYEAR"];
                $data["SCHREGNO"][TEXT]  = $model->data_arr[$i]["SCHREGNO"];
                $data["ENT_DIV"][TEXT]   = (0 != $model->data_arr[$i]["ENT_DIV"]) ? $model->data_arr[$i]["ENT_DIV"] : "2";
                $data["HR_CLASS"][TEXT]  = $model->data_arr[$i]["HR_CLASS"];
                $data["ATTENDNO"][TEXT]  = $model->data_arr[$i]["ATTENDNO"];
                $data["INOUTCD"][TEXT]   = $model->data_arr[$i]["INOUTCD"];
                $data["COURSECD"][TEXT]  = $model->data_arr[$i]["COURSECD"];
                $data["MAJORCD"][TEXT]   = sprintf("%03d", $model->data_arr[$i]["MAJORCD"]);
                $data["COURSECODE"][TEXT]= sprintf("%04d", $model->data_arr[$i]["COURSECODE"]);
                $data["NAME"][TEXT]      = $model->data_arr[$i]["NAME"];
                $data["NAME_KANA"][TEXT] = $model->data_arr[$i]["NAME_KANA"];
                if (strlen($model->data_arr[$i]["BIRTHDAY"]) > 0) {
                    $data["BIRTHDAY"][FUNC] = "date('".str_replace("/", "-", $model->data_arr[$i]["BIRTHDAY"])."')";
                } else {
                    $data["BIRTHDAY"][FUNC] = "";
                }
                $data["SEX"][TEXT]  = $model->data_arr[$i]["SEX"];
                $data["FINSCHOOLCD"][TEXT] = $model->data_arr[$i]["FINSCHOOLCD"];
                if (strlen($model->data_arr[$i]["FINSCHOOLGRADDATE"]) > 0) {
                    $data["FINSCHOOLGRADDATE"][FUNC] = "date('".str_replace("/", "-", $model->data_arr[$i]["FINSCHOOLGRADDATE"])."')";
                } else {
                    $data["FINSCHOOLGRADDATE"][FUNC] = "CAST(NULL as date)";
                }
                $data["ADDR_FLG"][TEXT]       = $model->field["GUARD_ADDR_FLG"] ? $model->field["GUARD_ADDR_FLG"] : null;
                $data["ISSUEDATE"][TEXT]      = str_replace("/", "-", $model->field["GUARD_ISSUEDATE"]);
                $data["EXPIREDATE"][TEXT]     = str_replace("/", "-", $model->field["GUARD_EXPIREDATE"]);
                $data["ZIPCD"][TEXT]          = $model->data_arr[$i]["ZIPCD"];
                $data["ADDR1"][TEXT]          = $model->data_arr[$i]["ADDRESS1"];
                $data["ADDR2"][TEXT]          = $model->data_arr[$i]["ADDRESS2"];
                $data["TELNO"][TEXT]          = $model->data_arr[$i]["TELNO"];
                $data["FAXNO"][TEXT]          = $model->data_arr[$i]["FAXNO"];
                $data["EMAIL"][TEXT]          = $model->data_arr[$i]["EMAIL"];
                $data["EMERGENCYCALL"][TEXT]  = $model->data_arr[$i]["EMERGENCYCALL"];
                $data["EMERGENCYTELNO"][TEXT] = $model->data_arr[$i]["EMERGENCYTELNO"];
                $data["SCALASHIPDIV"][TEXT]   = ($model->data_arr[$i]["SCALASHIPDIV"]) ? sprintf("%02d", $model->data_arr[$i]["SCALASHIPDIV"]) : ""; //NO001
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                $query = Query::insertSQL($data, "freshman_dat");
                $db->query($query);
            }
            //class_formation_dat(DELETE)
            $query  = "DELETE FROM CLASS_FORMATION_DAT";
            $query .= " WHERE YEAR      = '".$model->new_year."'";
            $query .= "   AND SEMESTER  = '1'";
            $query .= "   AND schregno  = '".$model->data_arr[$i]["SCHREGNO"]."'";
            $db->query($query);
            //class_formation_dat(INSERT)
            if ($model->field["SHORI_MEI"] == "1") {
                $query  = "INSERT INTO class_formation_dat";
                $query .= " SELECT schregno";                             //SCHREGNO
                $query .= "       ,'".$model->new_year."'";               //YEAR
                $query .= "       ,'1'";                                  //SEMESTER
                $query .= "       ,'".$model->grade."'";                  //GRADE
                $query .= "       ,HR_CLASS";                             //HR_CLASS
                $query .= "       ,ATTENDNO";                             //ATTENDNO
                $query .= "       ,COURSECD";                             //COURSECD
                $query .= "       ,MAJORCD";                              //MAJORCD
                $query .= "       ,COURSECODE";                           //COURSECODE NO001
                $query .= "       ,'0'";                                  //REMAINGRADE_FLG
                $query .= "       ,cast(NULL as varchar(8))";             //OLD_SCHREGNO NO002
                $query .= "       ,cast(NULL as varchar(2))";             //OLD_GRADE
                $query .= "       ,cast(NULL as varchar(2))";             //OLD_HR_CLASS
                $query .= "       ,cast(NULL as varchar(3))";             //OLD_ATTENDNO
                $query .= "       ,cast(NULL as decimal(4,1))";           //SCORE NO002
                $query .= "       ,'".STAFFCD."'";                        //REGISTERCD
                $query .= "       ,SYSDATE()";
                $query .= " FROM freshman_dat";
                $query .= " WHERE enteryear = '".$model->new_year."'";
                $query .= "   AND schregno  = '".$model->data_arr[$i]["SCHREGNO"]."'";
                $db->query($query);
            }
        }//for
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
